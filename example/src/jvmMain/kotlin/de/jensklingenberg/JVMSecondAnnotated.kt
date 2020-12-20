package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog



fun main() {

    logit("hh")
}

@DebugLog
fun logit(name:String): String {

    return name
}

