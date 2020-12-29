package de.jensklingenberg.cabret

import android.util.Log

class Test : Cabret.Listener {
    override fun log(tag: String, msg: String, logLevel: Cabret.LogLevel) {
        Log.d(tag,msg)
    }


}

actual typealias DefaultListener = Test
