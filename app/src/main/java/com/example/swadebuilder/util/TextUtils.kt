package com.example.swadebuilder.util

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    return assets.open(fileName).use { input -> json.decodeFromStream(input) }
}
