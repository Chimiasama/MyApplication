package com.example.swadebuilder.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

inline fun <reified T> Context.loadJsonAsset(fileName: String): T {
    // 1) Primeiro carrega o texto bruto do arquivo
    val jsonText = assets.open(fileName)
        .bufferedReader(Charsets.UTF_8)
        .use { it.readText() }

    // 2) Configura o Gson para “entender” JsonElement do kotlinx.serialization
    val gson = GsonBuilder()
        .registerTypeAdapter(
            JsonElement::class.java,
            JsonDeserializer { jsonEl, _, _ ->
                Json.parseToJsonElement(jsonEl.toString())
            }
        )
        .create()

    return gson.fromJson(jsonText, object : TypeToken<T>() {}.type)
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