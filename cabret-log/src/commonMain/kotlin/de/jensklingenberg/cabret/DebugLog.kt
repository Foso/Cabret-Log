package de.jensklingenberg.cabret

@Target(AnnotationTarget.FUNCTION)
annotation class DebugLog(val logLevel: Cabret.LogLevel = Cabret.LogLevel.DEBUG, val tag: String = "")