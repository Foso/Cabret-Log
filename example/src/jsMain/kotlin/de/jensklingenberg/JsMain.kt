package de.jensklingenberg

import de.jensklingenberg.cabret.DebugLog


fun main() {
    logit("dddd")
}


@DebugLog
fun logit(name:String): String {

    return name
}