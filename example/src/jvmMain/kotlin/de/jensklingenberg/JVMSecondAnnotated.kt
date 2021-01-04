package de.jensklingenberg

import de.jensklingenberg.cabret.Cabret
import de.jensklingenberg.cabret.DebugLog

fun main() {

    logit("hh")


}

data class Person(val firstName: String, val lastName: String)


@DebugLog(logLevel = Cabret.LogLevel.ERROR, tag = "Hallo")
fun logit(name: String, person: Person = Person("Jens", "Klingenberg")): String {
    return name

}
