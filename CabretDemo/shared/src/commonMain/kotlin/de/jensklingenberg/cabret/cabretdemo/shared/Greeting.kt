package de.jensklingenberg.cabret.cabretdemo.shared

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog


class Greeting {

    @DebugLog(tag = "Hallo",logLevel = Cabret.LogLevel.INFO)
    fun greeting( test:String ="Nene"): String {
        return "Hello, ${Platform().platform}!"
    }
}
