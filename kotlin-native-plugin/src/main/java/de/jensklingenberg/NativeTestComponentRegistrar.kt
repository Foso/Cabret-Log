package de.jensklingenberg

import com.google.auto.service.AutoService
import com.intellij.mock.MockProject
import de.jensklingenberg.common.KEY_ENABLED
import de.jensklingenberg.cabret.compiler.MyIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLogLevel
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(ComponentRegistrar::class)
class NativeTestComponentRegistrar : ComponentRegistrar {


    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {

        if (configuration[KEY_ENABLED] == false) {
            return
        }

        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)

        configuration.kotlinSourceRoots.forEach {
            messageCollector.report(
                CompilerMessageLogLevel.WARNING,
                "*** Hello from ***" + it.path
            )
        }

        //Debuglog
        IrGenerationExtension.registerExtension(project, MyIrGenerationExtension(messageCollector))

    }
}
