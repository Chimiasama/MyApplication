package com.example.swadebuilder.model

import android.content.Context
import androidx.annotation.RawRes
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PericiaJson(
    val nome: String,
    val atributo: String = "",
    val basica: Boolean = false,
    val origem: String? = null
)

@Serializable
data class PericiaDescricaoJson(
    val nome: String,
    val descricao: String
)

@Serializable
data class PericiaList(
    val pericias: List<PericiaJson>
)

fun loadPericiasDescriptions(
    context: Context,
    @RawRes resId: Int
): Map<String, String> {
    val json = context.resources
        .openRawResource(resId)
        .bufferedReader()
        .use { it.readText() }

    val lista = Json.decodeFromString<List<PericiaDescricaoJson>>(json)

    return lista.associate { pericia ->
        val key = pericia.nome.uppercase().semAcentos()
        key to pericia.descricao.trim()
    }
}
