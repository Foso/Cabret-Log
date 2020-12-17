package de.jensklingenberg

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

@AutoService(CommandLineProcessor::class) // don't forget!
class NativeCommandLineProcessor : CommandLineProcessor {

    override val pluginId: String = "helloWorldPlugin"

    override val pluginOptions: Collection<CliOption> = emptyList()

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) =  configuration.put(KEY_ENABLED, true)

}

val KEY_ENABLED = CompilerConfigurationKey<Boolean>("whether the plugin is enabled")
