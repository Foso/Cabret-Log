package de.jensklingenberg.testAnnotations

import android.util.Log

class Test : DebuglogHandler.Listener{
    override fun log(name: String, servity: DebuglogHandler.Servity) {
        Log.d("HAUS",name)
    }

}

actual typealias DefaultListener = Test
