package sample.example2

import de.jensklingenberg.testAnnotations.DebuglogHandler
import de.jensklingenberg.testAnnotations.IrDump


import sample.doSomething


fun test(){
  doSomething("Jens")
}

fun main() {
    DebuglogHandler.addListener(object :DebuglogHandler.Listener{
        override fun log(name: String, servity: DebuglogHandler.Servity) {

            println("HEY! "  +name)
        }
    })
   test()
}