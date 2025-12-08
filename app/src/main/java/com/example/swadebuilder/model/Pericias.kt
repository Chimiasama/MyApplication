package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable

@Serializable
data class PericiaJson(
    val nome: String,
    val atributo: String = "",
    val basica: Boolean = false,
    val descricao: String = ""
)

@Serializable
data class PericiaList(
    val pericias: List<PericiaJson>
)

fun loadPericiasDescriptions(
    context: Context
): Map<String, String> {
    val lista = context.loadJsonAsset<PericiaList>("pericias.json").pericias

    return lista.associate { pericia ->
        val key = "${pericia.nome} (${pericia.atributo})".uppercase().semAcentos()
        key to pericia.descricao.trim()
    }
}
