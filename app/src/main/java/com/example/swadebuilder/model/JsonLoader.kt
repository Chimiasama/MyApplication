package com.example.swadebuilder.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.decodeFromStream

suspend inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    return withContext(Dispatchers.IO) {
        val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            coerceInputValues = true
        }
        assets.open(fileName).use { input -> json.decodeFromStream(input) }
    }
}
