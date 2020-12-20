package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog


fun main() {

    logit("dddd")
}


@DebugLog(logLevel = Cabret.LogLevel.DEBUG)
fun logit(name:String): String {

    return name
}