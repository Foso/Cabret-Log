package de.jensklingenberg.cabret

import android.util.Log

class AndroidListener : Cabret.Listener {
    override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel) {
        when (logLevel) {
            Cabret.LogLevel.VERBOSE -> {
                Log.v(tag, msg)
            }
            Cabret.LogLevel.DEBUG -> {
                Log.d(tag, msg)
            }
            Cabret.LogLevel.INFO -> {
                Log.i(tag, msg)
            }
            Cabret.LogLevel.WARN -> {
                Log.w(tag, msg)
            }
            Cabret.LogLevel.ERROR -> {
                Log.e(tag, msg)
            }
        }
    }


}

actual typealias DefaultListener = AndroidListener
