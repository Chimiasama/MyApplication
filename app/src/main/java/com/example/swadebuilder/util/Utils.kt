package com.example.swadebuilder.util

import android.content.Context
import androidx.annotation.RawRes
import java.io.BufferedReader
import java.io.InputStreamReader

fun loadRawText(context: Context, @RawRes resId: Int): String {
    val inputStream = context.resources.openRawResource(resId)
    val reader = BufferedReader(InputStreamReader(inputStream))
    return reader.readText()
}
