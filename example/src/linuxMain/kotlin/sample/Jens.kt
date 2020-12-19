package sample

import de.jensklingenberg.cabret.DebugLog
import de.jensklingenberg.cabret.IrDump


data class Jens(val name: String) {




}

@IrDump
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

