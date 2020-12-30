package de.jensklingenberg.cabret.cabretdemo.shared

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog


class Greeting {

    fun greeting(greetingText: String = "Hello"): String {
        /**
         * Cabret.addListener(object :Cabret.Listener{
        override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel) {
        println(tag + " " +msg)
        }
        })
         */
        return realgreeting(greetingText)
    }

    @DebugLog(tag = "MyTestTag",logLevel = Cabret.LogLevel.WARN)
    fun realgreeting(greetingText: String = "Hello"): String {
        return "Hello, ${Platform().platform}!"
    }
}
