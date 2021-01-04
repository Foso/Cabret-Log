package de.jensklingenberg.cabret

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object LogHandler {

    private var logger: Cabret.Logger = DefaultLogger()

    /**
     * This function is used by the compiler plugin to log the method call
     * Needs to be public
     */
    fun onLog(tag: String, name: String, logLevel: String) {
        val serv = Cabret.LogLevel.valueOf(logLevel)
        logger.log(LogData(tag, name, serv))
    }

    fun addLogger(logger: Cabret.Logger) {
        this.logger = logger
    }

    /**
     * This is used to log the return values
     */
    fun <T> logReturn(tag: String, returnObject: T, logLevel: String): T {
        onLog(tag, returnObject.toString(), logLevel)
        return returnObject
    }

    fun removeLogger() {
        logger = DefaultLogger()
    }
}

