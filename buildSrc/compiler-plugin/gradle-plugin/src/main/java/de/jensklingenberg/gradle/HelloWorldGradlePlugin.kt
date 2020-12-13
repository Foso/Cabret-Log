package de.jensklingenberg.gradle

import org.gradle.api.Project

open class HelloWorldGradlePlugin : org.gradle.api.Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(
            "helloWorld",
            TestCompilerExtension::class.java
        )
    }
}

open class TestCompilerExtension {
    var enabled: Boolean = true
}
