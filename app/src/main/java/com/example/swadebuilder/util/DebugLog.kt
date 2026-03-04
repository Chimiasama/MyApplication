package com.example.swadebuilder.util

import android.util.Log

fun debugLog(tag: String, message: String) {
    runCatching {
        Log.d(tag, message)
    }
}
