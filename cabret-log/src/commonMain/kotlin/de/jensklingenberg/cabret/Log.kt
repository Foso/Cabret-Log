package de.jensklingenberg.cabret


@Target(AnnotationTarget.FUNCTION)
annotation class DebugLog(val logReturn: Boolean = false, val logLevel: Cabret.LogLevel = Cabret.LogLevel.DEBUG)


object LogHandler {

    private var listener: Cabret.Listener = DefaultListener()

    fun onLog(tag: String, name: String, servity: String) {
        val serv = Cabret.LogLevel.valueOf(servity)
        listener.log(tag, name, serv)
    }

    fun addListener(listener: Cabret.Listener) {
        this.listener = listener
    }

    /**
     * This is used to log the retun values
     */
    fun <T> logReturn(returnObject: T, tag: String, servity: String): T {
        onLog(tag, returnObject.toString(), servity)
        return returnObject
    }
}

expect class DefaultListener() : Cabret.Listener {
    override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel)
}

class CommonListener : Cabret.Listener {
    override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel) {
        println(tag + " " + msg + " " + logLevel)
    }
}

object Cabret {

    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    interface Listener {
        fun log(tag: String, msg: String, logLevel: Cabret.LogLevel)
    }

    fun addListener(listener: Cabret.Listener) {
        LogHandler.addListener(listener)
    }
}