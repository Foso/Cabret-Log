
<h1 align="center">DebugLog </h1>

[![jCenter](https://img.shields.io/badge/Apache-2.0-green.svg
)](https://github.com/Foso/DebugLog/blob/master/LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![jCenter](https://img.shields.io/badge/Kotlin-1.4.20-green.svg
)](https://github.com/Foso/Sheasy/blob/master/LICENSE)



## Introduction 🙋‍♂️

This is an Kotlin Compiler Plugin that enables Annotation-triggered method call logging. 
 
Simply add **@DebugLog** to your methods and it will automatically log all arguments that are passed to the function.

When the following function gets called:

```kotlin
@DebugLog
fun doSomething(name: String, age: Int, isLoggedIn: Boolean = false) {
    //Do something
}

fun test(){
  doSomething("Jens",31)
}
```

It will log:
```kotlin
doSomething() name: Jens age: 31 isLoggedIn: false
```
## How does it work?
At compiling, the compiler plugin checks in the IrGeneration Phase for the @DebugLog annotation. Then it [rewrites the body the function](https://github.com/Foso/DebugLog/blob/6152ffe4a516010a029c2956f8f1ae878712030e/buildSrc/kotlin-plugin/src/main/java/de/jensklingenberg/debuglog/DebugLogTransformer.kt#L90). 

The function:

```kotlin
@DebugLog
fun doSomething(name: String, age: Int, isLoggedIn: Boolean = false) {
    //Do something
}
```

will be rewritten to:

```kotlin
@DebugLog
fun doSomething(name: String, age: Int, isLoggedIn: Boolean = false) {
    println("doSomething() name: $name age: $age isLoggedIn: $isLoggedIn"
    //Do something
}
```

In Android builds it will use **Log.d** instead of **println**

This rewrite will only happen inside the compiler plugin at compile time. No .kt/source files will be changed.



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

