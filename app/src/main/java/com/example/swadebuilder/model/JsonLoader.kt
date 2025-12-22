package com.example.swadebuilder.model

import android.content.Context
import kotlinx.serialization.json.decodeFromStream

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        explicitNulls = false      // <- alinhar com VantagensSection
        isLenient = true           // opcional: ajuda se tiver números/strings misturados
        coerceInputValues = true   // opcional: converte tipos quando possível
    }
    return assets.open(fileName).use { input -> json.decodeFromStream(input) }
}
