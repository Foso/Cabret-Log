package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog

import de.jensklingenberg.cabret.LogHandler


fun main() {
    Cabret.addListener(object :Cabret.Listener{
        override fun log(tag: String, msg: String, servity: LogHandler.Servity) {
            println(tag+ " "+msg)
        }

    })
    logit("hh")
}


fun logit(name:String): String {

    return name
}