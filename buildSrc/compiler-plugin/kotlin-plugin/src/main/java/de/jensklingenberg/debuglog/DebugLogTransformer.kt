package de.jensklingenberg.debuglog


import de.jensklingenberg.common.irBuilder
import de.jensklingenberg.testAnnotations.DebugLog
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.FqName
import java.io.File

class DebugLogTransformer(
        private val context: IrPluginContext,
        private val messageCollector: MessageCollector,
) : IrElementTransformerVoidWithContext(), FileLoweringPass {
    private lateinit var file: IrFile
    private lateinit var fileSource: String
    private val debugLogAnnoation: String = DebugLog::class.java.name
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
        return functions.toList().filter { (it.signature as IdSignature.PublicSignature).declarationFqName.substringAfterLast(".") == name }
    }

    enum class Target {
        Android, Other
    }

    private fun transformFunction(irSimpleFunction: IrSimpleFunction): IrSimpleFunction {

        if (irSimpleFunction.hasAnnotation(FqName(debugLogAnnoation))) {
            val typeUnit = context.irBuiltIns.unitType

            var target = Target.Other

            /**
             * Find the symbol for Log.d()
             */
            val typeNullableAny = context.irBuiltIns.anyNType

            val funLogD = context.referenceClass(FqName("android.util.Log"))
                    ?.getFunctions("d")?.first { it.owner.valueParameters.size == 2 }

            if (funLogD != null) {
                /**
                 * TODO: Find better way to detect Android
                 */
                target = Target.Android
            }

            when (target) {
                Target.Android -> {
                    funLogD ?: return irSimpleFunction

                    /**
                     * Here we rewrite the body of the annotated function
                     */

                    irSimpleFunction.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

                        with(context.irBuilder(irSimpleFunction.symbol)) {
                            irBlockBody {
                                statements += buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                                    /**
                                     * Here we create an the irCall for Log.d()
                                     * The first argument is the name of containing class
                                     * The second argument is a String with the name and value of every function
                                     * parameter.
                                     * Example:
                                     * For: fun exampleLog(name:String, age: Int)
                                     * This String will be passed to Log.d: "name: $name age: $age"
                                     */

                                    val argCall = irCall(
                                            funLogD
                                    ).apply {
                                        putValueArgument(0, irString("MyFirstFragment"))

                                        val conc = irConcat()
                                        irSimpleFunction.valueParameters.forEach {
                                            conc.addArgument(irString(" " + it.name.asString() + ": "))
                                            conc.addArgument(irGet(it))
                                        }

                                        putValueArgument(1, conc)
                                    }
                                    typeOperator(
                                            typeOperator = IrTypeOperator.IMPLICIT_COERCION_TO_UNIT,
                                            resultType = typeUnit,
                                            argument = argCall,
                                            typeOperand = typeUnit
                                    )

                                }
                                /**
                                 * Here we add all the other statements of the body, when there are any.
                                 */
                                statements += irSimpleFunction.body?.statements ?: emptyList()
                            }
                        }


                    }

                }
                Target.Other -> {

                    /**
                     * Find the symbol for printLn()
                     */

                    val funPrintln = context.referenceFunctions(FqName("kotlin.io.println"))
                            .single {
                                val parameters = it.owner.valueParameters
                                parameters.size == 1 && parameters[0].type == typeNullableAny
                            }

                    irSimpleFunction.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

                        with(context.irBuilder(irSimpleFunction.symbol)) {
                            irBlockBody {
                                statements += buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

                                    irCall(
                                            funPrintln
                                    ).apply {
                                        val conc = irConcat()
                                        irSimpleFunction.valueParameters.forEach {
                                            conc.addArgument(irString(" " + it.name.asString() + ": "))
                                            conc.addArgument(irGet(it))
                                        }

                                        this.putValueArgument(0, conc)

                                    }

                                }
                                statements += irSimpleFunction.body?.statements ?: emptyList()
                            }
                        }

                    }
                }


            }
            return irSimpleFunction
        }
        return irSimpleFunction
    }


}

