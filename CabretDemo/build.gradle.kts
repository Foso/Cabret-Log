buildscript {
    repositories {
        mavenLocal()

        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("com.android.tools.build:gradle:4.0.2")
        classpath ("de.jensklingenberg.cabret:cabret-gradle:1.0.3")

    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        mavenCentral()
    }
}