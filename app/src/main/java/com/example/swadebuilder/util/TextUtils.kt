package com.example.swadebuilder.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

private val assetJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    coerceInputValues = true
}

// Versão síncrona: para chamadores fora de contexto de corrotina, ex. dentro de um
// `remember { }` de Composable (que não pode chamar função suspend). Lê e decodifica na
// thread de chamada.
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    return assets.open(fileName).use { input -> assetJson.decodeFromStream(input) }
}

// Versão suspend: para chamadores em contexto de corrotina (ex. `produceState { }`),
// despacha a leitura/decode pra Dispatchers.IO em vez de bloquear a thread de chamada.
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
suspend inline fun <reified T> Context.loadJsonAssetAsync(fileName: String): T {
    return withContext(Dispatchers.IO) {
        assets.open(fileName).use { input -> assetJson.decodeFromStream(input) }
    }
}
