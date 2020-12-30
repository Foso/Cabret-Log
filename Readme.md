
<h1 align="center"> Cabret </h1>

[![jCenter](https://img.shields.io/badge/Apache-2.0-green.svg
)](https://github.com/Foso/DebugLog/blob/master/LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![jCenter](https://img.shields.io/badge/Kotlin-1.4.20-green.svg
)](https://github.com/Foso/Sheasy/blob/master/LICENSE)



## Introduction 🙋‍♂️

This is an Kotlin Library that enables Annotation-triggered method call logging for Kotlin Multiplatform. Inspired by [Hugo](https://github.com/JakeWharton/hugo), [Hunter-Debug](https://github.com/Leaking/Hunter/blob/master/README_hunter_debug.md) and the [blog posts](https://blog.bnorm.dev/) by [bnorm](https://github.com/bnorm) .

Simply add **@DebugLog** to your methods and it will automatically log all arguments that are passed to the function the return value and the time the function needed to excecute.

When the following function gets called:

```kotlin
@DebugLog
fun exampleFun(
    first: String,
    last: String,
    age: Int = 31,
    isLoggedIn: Boolean = false
): String = "$first $last"

fun main(){
  exampleFun("Jens","Klingenberg")
}
```

It will automatically log:
```kotlin
Example -> exampleFun( first= Jens, last= Klingenberg, age= 31, isLoggedIn= false)
Example <- exampleFun() [2.63ms] =  Jens Klingenberg
```

### Show some :heart: and star the repo to support the project

[![GitHub stars](https://img.shields.io/github/stars/Foso/Cabret-Log.svg?style=social&label=Star)](https://github.com/Foso/Cabret-Log) [![GitHub forks](https://img.shields.io/github/forks/Foso/Cabret-Log.svg?style=social&label=Fork)](https://github.com/Foso/Cabret-Log/fork) [![GitHub watchers](https://img.shields.io/github/watchers/Foso/Cabret-Log.svg?style=social&label=Watch)](https://github.com/Foso/Cabret-Log) [![Twitter Follow](https://img.shields.io/twitter/follow/jklingenberg_.svg?style=social)](https://twitter.com/jklingenberg_)

## Setup
> You can take a look at [DemoProject](https://github.com/Foso/Cabret-Log/tree/master/CabretDemo) as an example

### 1) Gradle Plugin

Add the dependency to your buildscript

```groovy
buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath "de.jensklingenberg.cabret:cabret-gradle:1.0.3"
    }
}

```
#### 2) Apply the plugin


Kotlin DSL:

```kotlin
plugins {
     id("de.jensklingenberg.cabret")
}

configure<de.jensklingenberg.gradle.CabretGradleExtension> {
    enabled = true
}
```       

Groovy DSL:

```gradle
plugins {
    id 'de.jensklingenberg.cabret'
}

cabret {
    enabled = true
}
```

The plugin will only be active when **enabled** is set to **true**

### 3) Log Library
To be able to use the DebugLog annotation, you also need add the dependecies on cabret-log.

#### Multiplatform (Common, JS, Native)

You can add dependency to the required module right to the common source set:
```gradle
commonMain {
    dependencies {
        implementation "de.jensklingenberg.cabret:cabret-log:$cabretVersion"
    }
}
```
The same artifact coordinates can be used to depend on platform-specific artifact in platform-specific source-set.

#### Platform-specific 
You can also add platform-specific dependecies

```gradle
sourceSets {
    jvmMain {
            dependencies {
                 implementation "de.jensklingenberg.cabret:cabret-log-jvm:1.0.3"
            }
   }
}
```

Here's a list of all available targets:
```gradle
def cabretVersion = "1.0.3"

implementation "de.jensklingenberg.cabret:cabret-log-jvm:$cabretVersion"
implementation "de.jensklingenberg.cabret:cabret-log-js:$cabretVersion"
implementation "de.jensklingenberg.cabret:cabret-log-android:$cabretVersion"
implementation "de.jensklingenberg.cabret:cabret-log-iosx64:$cabretVersion"
implementation "de.jensklingenberg.cabret:cabret-log-iosarm64:$cabretVersion"
implementation "de.jensklingenberg.cabret:cabret-log-linux:$cabretVersion"

```

### 4) Enable IR
Cabret is using a Kotlin Compiler Plugin that is using the IR Backend. For Native targets it's already enabled, but you need to activate it in your build.gradle for Kotlin JVM/JS

##### Kotlin/JVM
```kotlin
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    useIR = true
  }
}
```

##### Kotlin/JS
```kotlin
target {
  js(IR) {
  }
}
```

## Logging

### Tag

```kotlin
@DebugLog( tag = "MyTag")
```

You can add a tag to the DebugLog annotation under which you can find your logged data. When you don't add a custom tag, Cabret will use the file name for top level function and the class name for class functions as the tag.

### LogLevel
```kotlin
@DebugLog(logLevel = Cabret.LogLevel.ERROR)
```

You can set a LogLevel to the DebugLog Annotation. You can choose between VERBOSE, DEBUG, INFO, WARN or ERROR. By default DEBUG is selected. 


### 👷 Project Structure
* <kbd>androidSample</kbd> - A basic Android app that is using the debuglog compiler plugin
* <kbd>src</kbd> - A Kotlin Multiplatform project that is using the debuglog compiler plugin


#### buildSrc
 *  <kbd>kotlin-compiler-native-plugin</kbd> - This module contains the Kotlin Compiler Plugin for native targets
 *  <kbd>kotlin-compiler-plugin</kbd> - This module contains the Kotlin Compiler Plugin for JVM/JS targets
 *  <kbd>gradle-plugin</kbd> - This module contains the gradle plugin which trigger the two compiler plugins
 *  <kbd>annotations</kbd> - This module contains the debuglog annotations


### Find this project useful ? :heart:
* Support it by clicking the :star: button on the upper right of this page. :v:

## 📜 License

-------

This project is licensed under Apache License, Version 2.0

    Copyright 2020 Jens Klingenberg

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

