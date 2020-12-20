package de.jensklingenberg.cabret.compiler


import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog
import de.jensklingenberg.cabret.LogHandler
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.buildStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetEnumValueImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName
import java.io.File

class DebugLogData(val logLevel: Cabret.LogLevel = Cabret.LogLevel.DEBUG, val tag: String = "")

class CabretLogTransformer(
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    val irFactory: IrFactory,
) : IrElementTransformerVoidWithContext(), FileLoweringPass {
    private lateinit var file: IrFile
    private lateinit var fileSource: String
    private val debugLogAnnoation: String = DebugLog::class.java.name
    val logReturnEnabled = true


    private val classMonotonic =
        context.referenceClass(FqName("kotlin.time.TimeSource.Monotonic"))!!

    private val funMarkNow =
        context.referenceFunctions(FqName("kotlin.time.TimeSource.markNow"))
            .single()

    private val funElapsedNow =
        context.referenceFunctions(FqName("kotlin.time.TimeMark.elapsedNow"))
            .single()


    override fun lower(irFile: IrFile) {
        file = irFile
        fileSource = File(irFile.path).readText()

        irFile.transformChildrenVoid()
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {

        if (validateSignature(declaration)) {
            return super.visitSimpleFunction(transformFunction(declaration))

        }
        return super.visitSimpleFunction(declaration)
    }

    private fun validateSignature(declaration: IrSimpleFunction): Boolean =
        declaration.hasAnnotation(FqName(debugLogAnnoation))

    private fun IrClassSymbol.getFunctions(name: String): List<IrSimpleFunctionSymbol> {
        return functions.toList()
            .filter { (it.signature as IdSignature.PublicSignature).declarationFqName.substringAfterLast(".") == name }
    }

    private fun transformFunction(irSimpleFunction: IrSimpleFunction): IrSimpleFunction {

        val cabretLogHandlerSymbol: IrClassSymbol =
            context.referenceClass(FqName(LogHandler::class.java.name)) ?: return irSimpleFunction

        val debugLogData = mapToDebugLog(irSimpleFunction)

        /**
         * Find the symbol for onLog(), we need it to create the irCall
         */

        irSimpleFunction.body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
            with(context.irBuilder(irSimpleFunction.symbol)) {
                irBlockBody {
                    val startTimer = irTemporary(irCall(funMarkNow).also { call ->
                        call.dispatchReceiver = irGetObject(classMonotonic)
                    })
                    statements += addStartTimer(startTimer)

                    statements += addParameterLogging(irSimpleFunction, cabretLogHandlerSymbol, debugLogData)

                    if (logReturnEnabled) {
                        transformReturnValue(irSimpleFunction, cabretLogHandlerSymbol, startTimer, debugLogData)
                    }

                    //Add all other statements of the body
                    statements += irSimpleFunction.body?.statements ?: emptyList()
                }
            }
        }

        return irSimpleFunction
    }

    /**
     * TODO: I need to find better way read the arguments from the Annotation
     */
    fun mapToDebugLog(irSimpleFunction: IrSimpleFunction): DebugLogData {
        val annotation = irSimpleFunction.getAnnotation(FqName(DebugLog::class.java.name))!!
        fun findByName(name: String): Pair<IrValueParameter, IrExpression>? {
            return annotation.getArgumentsWithIr().find { it.first.name.asString() == name }
        }

        val logLevelString =
            (findByName("logLevel")?.second as IrGetEnumValueImpl).symbol.signature.asPublic()?.declarationFqName?.substringAfterLast(
                "."
            ) ?: ""

        val logLevel = Cabret.LogLevel.values().find { it.name == logLevelString } ?: Cabret.LogLevel.DEBUG

        val tag = (findByName(("tag"))?.second as? IrConstImpl<String>)?.value
            ?: irSimpleFunction.parentClassOrNull?.name?.asString() ?: irSimpleFunction.file.name

        return DebugLogData(tag = tag, logLevel = logLevel)
    }

    private fun IrBlockBodyBuilder.addParameterLogging(
        irSimpleFunction: IrSimpleFunction,
        cabretLogHandlerSymbol: IrClassSymbol,
        test: DebugLogData
    ) = buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

        val onLogSymbol = cabretLogHandlerSymbol.getFunctions("onLog").first { it.owner.valueParameters.size == 3 }

        irCall(
            onLogSymbol
        ).apply {

            dispatchReceiver = irGetObject(cabretLogHandlerSymbol)
            putValueArgument(0, irString(test.tag))

            val conc = irConcat()
            conc.addArgument(irString("-> ${irSimpleFunction.name}( "))
            //Read all parameter names and add them to the logstring
            irSimpleFunction.valueParameters.forEachIndexed { index, irValueParameter ->

                conc.addArgument(irString(irValueParameter.name.asString() + "= "))
                conc.addArgument(irGet(irValueParameter))

                if ((index + 1) < irSimpleFunction.valueParameters.size) {
                    conc.addArgument(irString(", "))
                }

            }
            conc.addArgument(irString(")"))
            putValueArgument(1, conc)
            putValueArgument(2, irString(test.logLevel.name))
        }
    }

    private fun IrBlockBodyBuilder.transformReturnValue(
        irSimpleFunction: IrSimpleFunction,
        cabretLogHandlerSymbol: IrClassSymbol,
        start: IrVariable,
        debugLogData: DebugLogData
    ) {
        val logReturnSymbol =
            cabretLogHandlerSymbol.getFunctions("logReturn").first { it.owner.valueParameters.size == 3 }

        irSimpleFunction.body?.transformChildren(object : IrElementTransformerVoidWithContext() {


            /**
             * Get every "return value"
             * If return logging is enbabled
             * Every expression gets transformerd
             *
             * return x
             * gets transformed to:
             *
             * return DebugLogHandler.logReturn(x)
             */
            override fun visitReturn(expression: IrReturn): IrExpression {
                val call = irCall(
                    logReturnSymbol, expression.value.type
                ).apply {
                    // fun <T> logReturn(returnObject: T, tag: String, servity: String): T {
                    dispatchReceiver = irGetObject(cabretLogHandlerSymbol)

                    //tag
                    val conc = irConcat()
                    conc.addArgument(irString(debugLogData.tag))
                    conc.addArgument(irString(" <- ${irSimpleFunction.name}() ["))

                    //Get the time
                    conc.addArgument(irCall(funElapsedNow).also { call ->
                        call.dispatchReceiver = irGet(start)
                    })
                    conc.addArgument(irString("] = "))

                    putValueArgument(0, conc)

                    //returnObject
                    putValueArgument(1, expression.value)

                    //LogLevel
                    putValueArgument(2, irString(debugLogData.logLevel.name))

                    putTypeArgument(0, expression.value.type)
                }
                return super.visitReturn(irReturn(call))
            }
        }, null)
    }

    private fun addStartTimer(start: IrVariable) = start
}


// ./gradlew :example:clean :example:compileKotlinJvm --no-daemon -Dorg.gradle.debug=true -Dkotlin.compiler.execution.strategy="in-process" -Dkotlin.daemon.jvm.options="-Xdebug,-Xrunjdwp:transport=dt_socket\,address=5005\,server=y\,suspend=n"