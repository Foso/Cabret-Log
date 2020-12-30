buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath ("de.jensklingenberg.cabret:cabret-gradle:1.0.3")

    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}