package com.example.myapplication.model

import android.content.Context
import kotlinx.serialization.json.Json

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    val text = assets.open(fileName)
        .bufferedReader()
        .use { it.readText() }
    // Cria um parser que ignora chaves desconhecidas no JSON
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(text)
}
