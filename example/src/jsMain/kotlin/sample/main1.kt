package sample

import de.jensklingenberg.testAnnotations.DebuglogHandler
import de.jensklingenberg.testAnnotations.IrDump


import sample.doSomething

@IrDump
fun test(){
  doSomething("Jens")
}

fun main() {
  DebuglogHandler.addListener(object :DebuglogHandler.Listener{
    override fun log(name: String, servity: DebuglogHandler.Servity) {
      console.log("CONSOLE"+name)
    }

  })
  doSomething("Jens",5)
}