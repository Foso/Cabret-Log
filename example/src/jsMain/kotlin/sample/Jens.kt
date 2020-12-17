package sample

import de.jensklingenberg.testAnnotations.DebugLog
import de.jensklingenberg.testAnnotations.DebuglogHandler


data class Jens(val name: String) {

    override fun toString(): String {

        return "ddd"
    }


}


fun doSomething(name: String, age: Int = 5): Int {
  return age
}