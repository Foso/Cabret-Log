package de.jensklingenberg.testAnnotations


@Target(AnnotationTarget.FUNCTION)
annotation class DebugLog()

@Target(AnnotationTarget.FUNCTION)
annotation class IrDump()

object DebuglogHandler {

    enum class Servity {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    var listener: Listener? = null

    interface Listener {
        fun log(name: String, servity: Servity)
    }

    fun onLog(name: String, servity: String) {

        if (listener == null) {
            println(name + " " + servity)
        } else {
            val serv = Servity.valueOf(servity)

            listener?.log(name, serv)
        }

    }


    fun addListener(listener: Listener) {
        this.listener = listener
    }


}