package de.jensklingenberg.debuglog

import de.jensklingenberg.common.runOnFileInOrder

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.platform.jvm.isJvm

class MyIrGenerationExtension3(private val messageCollector: MessageCollector) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        if (moduleFragment.descriptor.platform.isJvm()) {
            for (file in moduleFragment.files) {
                DebugLogTransformer(pluginContext, messageCollector).runOnFileInOrder(file)
            }
        }
    }

}