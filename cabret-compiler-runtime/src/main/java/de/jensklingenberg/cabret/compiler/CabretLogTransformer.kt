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
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.file
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName

import java.io.File

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

            /**
             * Find the symbol for onLog(), we need it to create the irCall
             */

        irSimpleFunction.body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
            with(context.irBuilder(irSimpleFunction.symbol)) {
                irBlockBody {
                   val startTimer= irTemporary(irCall(funMarkNow).also { call ->
                        call.dispatchReceiver = irGetObject(classMonotonic)
                    })
                    statements += addStartTimer(startTimer)

                    statements += addParameterLogging(irSimpleFunction, cabretLogHandlerSymbol)

                    if (logReturnEnabled) {
                        transformReturnValue(irSimpleFunction, cabretLogHandlerSymbol,startTimer)
                    }

                    //Add all other statements of the body
                    statements += irSimpleFunction.body?.statements ?: emptyList()
                }
            }
        }

        return irSimpleFunction
    }

    private fun IrBlockBodyBuilder.addParameterLogging(
        irSimpleFunction: IrSimpleFunction,
        cabretLogHandlerSymbol: IrClassSymbol
    ) = buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
        val onLogSymbol = cabretLogHandlerSymbol.getFunctions("onLog").first { it.owner.valueParameters.size == 3 }
        val tagName = irSimpleFunction.file.name
        irCall(
            onLogSymbol
        ).apply {
            val conc = irConcat()
            conc.addArgument(irString("-> ${irSimpleFunction.name} "))
            //Read all parameter names and add them to the logstring
            irSimpleFunction.valueParameters.forEach {
                conc.addArgument(irString(it.name.asString() + ": "))
                conc.addArgument(irGet(it))
            }
            dispatchReceiver = irGetObject(cabretLogHandlerSymbol)
            putValueArgument(0, irString(tagName))
            putValueArgument(1, conc)
            putValueArgument(2, irString(Cabret.LogLevel.DEBUG.name))
        }
    }

    private fun IrBlockBodyBuilder.transformReturnValue(
        irSimpleFunction: IrSimpleFunction,
        cabretLogHandlerSymbol: IrClassSymbol,
        start: IrVariable
    ) {
        val logReturnSymbol =
            cabretLogHandlerSymbol.getFunctions("logReturn").first { it.owner.valueParameters.size == 3 }
        val tagName = irSimpleFunction.file.name

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
                    conc.addArgument(irString(tagName))
                    //Get the time
                    conc.addArgument(irCall(funElapsedNow).also { call ->
                        call.dispatchReceiver = irGet(start)
                    })
                    putValueArgument(0, conc)

                    //returnObject
                    putValueArgument(1, expression.value)

                    //LogLevel
                    putValueArgument(2, irString(Cabret.LogLevel.DEBUG.name))

                    putTypeArgument(0, expression.value.type)
                }
                return super.visitReturn(irReturn(call))
            }
        }, null)
    }

    private fun addStartTimer(start: IrVariable) = start
}

