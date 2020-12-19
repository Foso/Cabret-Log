package de.jensklingenberg.cabret


@Target(AnnotationTarget.FUNCTION)
annotation class DebugLog(val logReturn: Boolean = false)

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
            val serv = Servity.valueOf(servity)

            DefaultListener().log(name, serv)
        } else {
            val serv = Servity.valueOf(servity)

            listener?.log(name, serv)
        }
    }

    fun addListener(listener: Listener) {
        DebuglogHandler.listener = listener
    }

    fun <T> logReturn(returnObject: T, tag: String, servity: String): T {
        onLog(tag + " " + returnObject.toString(), Servity.DEBUG.name)
        return returnObject
    }
}

expect class DefaultListener() {
    fun log(name: String, servity: DebuglogHandler.Servity)
}

class CommonListener() : DebuglogHandler.Listener {
    override fun log(name: String, servity: DebuglogHandler.Servity) {
        println(name)
    }
}