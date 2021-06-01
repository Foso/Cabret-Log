package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog
import de.jensklingenberg.cabret.LogData
import test.commonLog


fun main() {
    Cabret.addLogger(object :Cabret.Logger{

        override fun log(data: LogData) {
            console.log(data.msg)
        }
    })
    logit("Hello World")
    commonLog("Hallo")
}


@DebugLog(logLevel = Cabret.LogLevel.DEBUG)
fun logit(name:String): String {

    return name
}