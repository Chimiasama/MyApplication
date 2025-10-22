package com.example.swadebuilder.model

import android.content.Context

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val text = assets.open(fileName).bufferedReader().use { it.readText() }
    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        explicitNulls = false      // <- alinhar com VantagensSection
        isLenient = true           // opcional: ajuda se tiver números/strings misturados
        coerceInputValues = true   // opcional: converte tipos quando possível
    }
    return json.decodeFromString(text)
}
