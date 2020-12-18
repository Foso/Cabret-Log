package sample

import de.jensklingenberg.testAnnotations.DebugLog
import de.jensklingenberg.testAnnotations.DebuglogHandler
import de.jensklingenberg.testAnnotations.IrDump


data class Jens(val name: String) {




}

@IrDump
@DebugLog(logReturn = true)
fun doSomething(name: String, age: Int = 5): Jens {

    return Jens("KORO")
}

