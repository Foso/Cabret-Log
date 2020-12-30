package de.jensklingenberg.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

open class CabretGradleExtension {
    var enabled: Boolean = true
    var version: String = "1.0.2"
}


class CabretGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private var gradleExtension : CabretGradleExtension = CabretGradleExtension()

    companion object {
        const val SERIALIZATION_GROUP_NAME = "de.jensklingenberg.cabret"
        const val ARTIFACT_NAME = "cabret-compiler-plugin"
        const val NATIVE_ARTIFACT_NAME = "cabret-compiler-plugin-native"
    }

    override fun apply(target: Project) {
        target.extensions.create(
            "cabret",
            CabretGradleExtension::class.java
        )
        super.apply(target)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        gradleExtension = kotlinCompilation.target.project.extensions.findByType(CabretGradleExtension::class.java)
            ?: CabretGradleExtension()
        val project = kotlinCompilation.target.project

        return project.provider {
            val options = mutableListOf<SubpluginOption>(SubpluginOption("enabled", gradleExtension.enabled.toString()))
            options
        }
    }

    /**
     * Just needs to be consistent with the key for CommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "cabretPlugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId = ARTIFACT_NAME,
        version = gradleExtension.version // remember to bump this version before any release!
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getPluginArtifactForNative() = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId = NATIVE_ARTIFACT_NAME,
        version = gradleExtension.version // remember to bump this version before any release!
    )
}