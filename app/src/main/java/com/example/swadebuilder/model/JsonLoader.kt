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
        val text = assets.open(fileName).bufferedReader().use { it.readText() }
        sharedJson.decodeFromString(text)
    } catch (e: Exception) {
        emptyList()
    }
}
