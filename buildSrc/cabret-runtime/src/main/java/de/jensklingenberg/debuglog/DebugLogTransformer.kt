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
import org.jetbrains.kotlin.ir.builders.buildStatement
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.dump
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

    enum class Target {
        Android, Other
    }

    private fun transformFunction(irSimpleFunction: IrSimpleFunction): IrSimpleFunction {
        val tt = context.referenceClass(FqName("de.jensklingenberg.testAnnotations.DebuglogHandler"))
        val irFactory = context.irFactory

        val ter = tt?.getFunctions("onLog")?.first { it.owner.valueParameters.size == 2 } ?: return irSimpleFunction


        if (irSimpleFunction.hasAnnotation(FqName(debugLogAnnoation))) {
            val typeUnit = context.irBuiltIns.unitType


            var target = Target.Other

            /**
             * Find the symbol for Log.d(), we need it to create the irCall
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

                Target.Other -> {

                    /**
                     * Find the symbol for printLn()
                     */

                    irSimpleFunction.body = irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

                        with(context.irBuilder(irSimpleFunction.symbol)) {
                            irBlockBody {
                                statements += buildStatement(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {

                                    irCall(
                                        ter
                                    ).apply {
                                        val conc = irConcat()

                                        irSimpleFunction.valueParameters.forEach {
                                            conc.addArgument(irString(" " + it.name.asString() + ": "))
                                            conc.addArgument(irGet(it))
                                        }
                                        this.dispatchReceiver = irGetObject(tt!!)
                                        this.putValueArgument(0, conc)
                                        this.putValueArgument(1, irString(DebuglogHandler.Servity.DEBUG.name))

                                        //  this.putValueArgument(1, irString("DEBUG"))

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

