package de.jensklingenberg.cabret

object Cabret {

    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }

    interface Logger {
        fun log(data: LogData)
    }

    fun addLogger(listener: Cabret.Logger) {
        LogHandler.addLogger(listener)
    }

    fun removeLogger() {
        LogHandler.removeLogger()
    }
}