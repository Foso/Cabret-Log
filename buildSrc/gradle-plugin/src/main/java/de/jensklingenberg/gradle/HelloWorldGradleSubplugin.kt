package de.jensklingenberg.gradle

import com.google.auto.service.AutoService
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.compile.AbstractCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinGradleSubplugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

@AutoService(HelloWorldGradleSubplugin::class)
class HelloWorldGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    companion object {
        const val SERIALIZATION_GROUP_NAME = "de.jensklingenberg"
        const val ARTIFACT_NAME = "kotlin-compiler-plugin"
        const val NATIVE_ARTIFACT_NAME = "kotlin-compiler-native-plugin"
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        return project.provider {
            val options = mutableListOf<SubpluginOption>()
            options
        }
    }


    /**
     * Just needs to be consistent with the key for CommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "helloWorldPlugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId =ARTIFACT_NAME,
        version = "0.0.1" // remember to bump this version before any release!
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getPluginArtifactForNative() = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId = NATIVE_ARTIFACT_NAME,
        version = "0.0.1" // remember to bump this version before any release!
    )
}