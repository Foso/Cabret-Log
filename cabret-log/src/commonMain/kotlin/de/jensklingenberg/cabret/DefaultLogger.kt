package de.jensklingenberg.cabret

expect class DefaultLogger() : Cabret.Logger {
    override fun log(data: LogData)
}