package sample

import de.jensklingenberg.testAnnotations.DebugLog
import de.jensklingenberg.testAnnotations.DebuglogHandler
import de.jensklingenberg.testAnnotations.IrDump


data class Jens(val name: String) {




}


@DebugLog(logReturn = true)
fun doSomething(name: String, age: Int = 5): Int? {

    return if(name=="Hans"){
        when{
            name.startsWith("H")->{
                null
            }
            else->{
                2
            }
        }
    }else{
        3
    }

}

