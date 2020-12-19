package de.jensklingenberg.debuglog


import de.jensklingenberg.common.irBuilder
import de.jensklingenberg.testAnnotations.DebugLog
import de.jensklingenberg.testAnnotations.DebuglogHandler
import de.jensklingenberg.testAnnotations.IrDump
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
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.dump
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

    override fun lower(irFile: IrFile) {
        file = irFile
        fileSource = File(irFile.path).readText()

        irFile.transformChildrenVoid()
    }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.hasAnnotation(FqName(IrDump::class.java.name))) {
            println(declaration.dump())
        }

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

        val cabretLogHandlerSymbol: IrClassSymbol? = context.referenceClass(FqName(DebuglogHandler::class.java.name))

        /**
         * Find the symbol for onLog(), we need it to create the irCall
         */

        val onLogSymbol = cabretLogHandlerSymbol?.getFunctions("onLog")?.first { it.owner.valueParameters.size == 2 }
            ?: return irSimpleFunction



        irSimpleFunction.body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

            with(context.irBuilder(irSimpleFunction.symbol)) {
                irBlockBody {
                    statements += buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                        irCall(
                            onLogSymbol
                        ).apply {
                            val conc = irConcat()

                            //Read all parameter names and add them to the logstring
                            irSimpleFunction.valueParameters.forEach {
                                conc.addArgument(irString(" " + it.name.asString() + ": "))
                                conc.addArgument(irGet(it))
                            }
                            dispatchReceiver = irGetObject(cabretLogHandlerSymbol)
                            putValueArgument(0, conc)
                            putValueArgument(1, irString(DebuglogHandler.Servity.DEBUG.name))
                        }
                    }

                    if (logReturnEnabled) {
                        transformReturnValue(irSimpleFunction, cabretLogHandlerSymbol)
                    }
                    //Add all other statements of the body
                    statements += irSimpleFunction.body?.statements ?: emptyList()
                }
            }

        }


        if (irSimpleFunction.body!!.dump() != "BLOCK_BODY\n" +
            "  RETURN type=kotlin.Nothing from='public final fun doSomething (name: kotlin.String, age: kotlin.Int): kotlin.Int declared in sample'\n" +
            "    CALL 'public final fun retu <T> (age: T of sample.retu): T of sample.retu declared in sample' type=kotlin.Int origin=null\n" +
            "      <T>: kotlin.Int\n" +
            "      age: GET_VAR 'age: kotlin.Int declared in sample.doSomething' type=kotlin.Int origin=null\n"
        ) {
            println("NOT EQUAL " + irSimpleFunction.body!!.dump())
        } else {
            println("??????????????????????????????????????????????????????????????????????????????????????????????????????")

        }
        return irSimpleFunction
    }

    private fun IrBlockBodyBuilder.transformReturnValue(
        irSimpleFunction: IrSimpleFunction,

        cabretLogHandlerSymbol: IrClassSymbol?
    ) {
        val logReturnSymbol =
            cabretLogHandlerSymbol?.getFunctions("logReturn").first { it.owner.valueParameters.size == 3 }
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
                    dispatchReceiver = irGetObject(cabretLogHandlerSymbol)

                    putValueArgument(0, expression.value)
                    putValueArgument(1, irString(DebuglogHandler.Servity.DEBUG.name))
                    putValueArgument(2, irString(DebuglogHandler.Servity.DEBUG.name))

                    putTypeArgument(0, expression.value.type)
                }

                return super.visitReturn(irReturn(call))


            }
        }, null)
    }
}

