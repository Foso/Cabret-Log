package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog
import test.commonLog


fun main() {

    logit("hh")
    commonLog("hh")


}

data class Person(val firstName: String, val lastName: String)


@DebugLog(logLevel = Cabret.LogLevel.ERROR, tag = "Hallo")
fun logit(name: String, person: Person = Person("Jens", "Klingenberg")): String {

    return name
}


@DebugLog(logLevel = Cabret.LogLevel.ERROR)
fun logitT2(name: String): String {

    return name
}