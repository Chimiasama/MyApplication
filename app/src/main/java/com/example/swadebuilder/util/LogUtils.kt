package com.example.swadebuilder.util

import android.util.Log

object LogUtils {
    fun d(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (e: RuntimeException) {
            // Fallback for unit tests where android.util.Log is not mocked
            println("$tag: $msg")
        }
    }
}
