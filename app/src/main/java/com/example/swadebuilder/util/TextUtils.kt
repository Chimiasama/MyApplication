package com.example.swadebuilder.util

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromString
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val jsonText = assets.open(fileName)
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        coerceInputValues = true
    }

    return json.decodeFromString(jsonText)
}

fun JsonElement?.toStringList(): List<String> {
    return when (this) {
        is JsonArray -> {
            this.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull }
        }
        is JsonPrimitive -> {
            this.jsonPrimitive.content
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        else -> emptyList()
    }
}