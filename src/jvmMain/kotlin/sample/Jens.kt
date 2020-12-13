package sample

import de.jensklingenberg.testAnnotations.DebugLog

data class Jens(val name: String) {

    override fun toString(): String {

        return "ddd"
    }

    @DebugLog
    fun addPrint(test: String, name: Int, te: String = "hhaa",wuhu:String) {

    }

}

