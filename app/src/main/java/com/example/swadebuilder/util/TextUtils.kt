package com.example.swadebuilder.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

// Versão síncrona: para chamadores fora de contexto de corrotina, ex. dentro de um
// `remember { }` de Composable (que não pode chamar função suspend). Lê e decodifica na
// thread de chamada.
//
// A config do Json fica duplicada nas duas funções (em vez de extraída pra um val
// compartilhado) de propósito: uma função `inline` pública não pode acessar uma
// propriedade privada do mesmo arquivo (erro do compilador Kotlin "Public-API inline
// function cannot access non-public-API property") — o corpo dela é copiado pro
// bytecode de quem chama, inclusive fora deste arquivo/módulo.
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

// Versão suspend: para chamadores em contexto de corrotina (ex. `produceState { }`),
// despacha a leitura/decode pra Dispatchers.IO em vez de bloquear a thread de chamada.
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
suspend inline fun <reified T> Context.loadJsonAssetAsync(fileName: String): T {
    return withContext(Dispatchers.IO) {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            coerceInputValues = true
        }
        assets.open(fileName).use { input -> json.decodeFromStream(input) }
    }
}
