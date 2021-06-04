import org.gradle.internal.impldep.org.bouncycastle.asn1.iana.IANAObjectIdentifiers.mail
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
    maven
    id("org.jetbrains.dokka")
    signing
}
group = "de.jensklingenberg.cabret"
version = "1.0.4-RC2"

kotlin {

    android() {
        publishLibraryVariants("release", "debug")
    }
    js(IR){

    }
    ios() {
        binaries {
            framework {
                baseName = "library"
            }
        }
    }
    jvm()
    linuxX64("linux")
    macosX64("macOS")
    watchos()
    tvos()
    sourceSets {
        val commonMain by getting
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

android {
    compileSdkVersion(30)
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
    }

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
    }
}

signing {
    setRequired(provider { gradle.taskGraph.hasTask("publish") })
    sign(publishing.publications)
}



publishing {

    repositories {
        if (
            hasProperty("sonatypeUsername") &&
            hasProperty("sonatypePassword") &&
            hasProperty("sonatypeSnapshotUrl") &&
            hasProperty("sonatypeReleaseUrl")
        ) {
            maven {
                val url = when {
                    "SNAPSHOT" in version.toString() -> property("sonatypeSnapshotUrl")
                    else -> property("sonatypeReleaseUrl")
                } as String
                setUrl(url)
                credentials {
                    username = property("sonatypeUsername") as String
                    password = property("sonatypePassword") as String
                }
            }
        }
        maven {
            name = "buildfolder"
            setUrl("file://${rootProject.buildDir}/localMaven")
        }

        maven {
            name = "mavCentral"
            setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = properties["mavenCentralUsername"].toString()
                password = properties["mavenCentralPassword"].toString()
            }
        }
    }

    publications.withType<MavenPublication>{
// Stub javadoc.jar artifact
        artifact(javadocJar.get())
            pom {
                name.set(project.name)
                description.set("Method call logging for Kotlin Multiplatform ")
                url.set("https://github.com/Foso/Cabret-Log")

                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://github.com/Foso/Cabret-Log/blob/master/LICENSE")
                    }
                }
                scm {
                    url.set("https://github.com/Foso/Cabret-Log")
                    connection.set("scm:git://github.com/Foso/Cabret-Log.git")
                }
                developers {
                    developer {
                        id.set("Foso")
                        name.set("Jens Klingenberg")
                        email.set("mail@jensklingenberg.de")
                    }
                }
            }

    }


}
