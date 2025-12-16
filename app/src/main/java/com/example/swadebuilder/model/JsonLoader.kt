package com.example.swadebuilder.model

import android.content.Context
import kotlinx.serialization.json.Json

val sharedJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    coerceInputValues = true
}

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val text = assets.open(fileName).bufferedReader().use { it.readText() }
    return sharedJson.decodeFromString(text)
}

inline fun <reified T> Context.loadOptionalList(fileName: String): List<T> {
    return try {
        loadJsonAsset<List<T>>(fileName)
    } catch (_: Exception) {
        emptyList()
    }
}
