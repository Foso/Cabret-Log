package de.jensklingenberg.cabret

import android.util.Log

class AndroidListener : Cabret.Logger {
    override fun log(data: LogData) {
        when (data.logLevel) {
            Cabret.LogLevel.VERBOSE -> {
                Log.v(data.tag, data.msg)
            }
            Cabret.LogLevel.DEBUG -> {
                Log.d(data.tag, data.msg)
            }
            Cabret.LogLevel.INFO -> {
                Log.i(data.tag, data.msg)
            }
            Cabret.LogLevel.WARN -> {
                Log.w(data.tag, data.msg)
            }
            Cabret.LogLevel.ERROR -> {
                Log.e(data.tag, data.msg)
            }
        }
    }


}

