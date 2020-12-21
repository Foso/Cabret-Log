package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog
import test.commonLog


fun main() {
    Cabret.addListener(object :Cabret.Listener{
        override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel) {
            console.log(msg)
        }
    })
    logit("dddd")
    commonLog("Hallo")
}


@DebugLog(logLevel = Cabret.LogLevel.DEBUG)
fun logit(name:String): String {

    return name
}