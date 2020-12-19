package sample

import de.jensklingenberg.cabret.DebuglogHandler

import sample.doSomething


fun test(){
   doSomething("Hans")
}

fun main() {
   DebuglogHandler.addListener(object :DebuglogHandler.Listener{
      override fun log(name: String, servity: DebuglogHandler.Servity) {
         println(name+ " "+servity)
      }
   })
   test()
}