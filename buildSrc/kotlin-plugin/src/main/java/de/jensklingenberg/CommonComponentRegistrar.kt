package de.jensklingenberg

import com.google.auto.service.AutoService
import de.jensklingenberg.debuglog.MyIrGenerationExtension

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

//context.referenceClass(FqName("de.jensklingenberg.testAnnotations.DebuglogHandler")).functions.toList()
@AutoService(ComponentRegistrar::class)
class CommonComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
            project: MockProject,
            configuration: CompilerConfiguration
    ) {

        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        //Debuglog
        IrGenerationExtension.registerExtension(project, MyIrGenerationExtension(messageCollector))

    }
}

