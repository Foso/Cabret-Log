package de.jensklingenberg.cabret

class CommonLogger : Cabret.Logger {
    override fun log(data: LogData) {
        println(data.tag + " " + data.msg)
    }
}