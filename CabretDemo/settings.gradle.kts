pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()

        mavenCentral()
    }
    
}
rootProject.name = "CabretDemo"


include(":androidApp")
include(":shared")

