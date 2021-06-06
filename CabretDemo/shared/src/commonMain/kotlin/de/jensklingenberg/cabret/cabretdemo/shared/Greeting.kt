package de.jensklingenberg.cabret.cabretdemo.shared

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog
import de.jensklingenberg.cabret.LogData


class Greeting {

    fun greeting(greetingText: String = "Hello"): String {

        /*
        Cabret.addLogger(object : Cabret.Logger {
            override fun log(data: LogData) {
                //Add your logger here
                println(data.tag + " " + data.msg)
            }
        })
*/
        return realgreeting(greetingText)
    }

    @DebugLog(tag = "MyTestTag", logLevel = Cabret.LogLevel.WARN)
    fun realgreeting(greetingText: String = "Hello"): String {
        return "Hello, ${Platform().platform}!"
    }
}
