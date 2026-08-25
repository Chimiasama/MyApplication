package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SuperPoder(
    val nome: String,
    val estagio: String = "iniciante",
    val custoBase: String? = null,
    val modificadores: List<String>? = null,
    val descricao: String? = null,
    val manifestacoes: JsonElement? = null,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    // Mesmas strings de "modificadores" (posição a posição, "Nome (+custo): texto"),
    // reescritas para a edição Lite.
    val modificadoresLite: List<String>? = null
) {
    fun exibido(): SuperPoder {
        if (EditionConfig.isFullEdition) return this
        val descricaoExibida = descricaoLite?.takeIf { it.isNotBlank() } ?: descricao
        val modificadoresExibidos = if (modificadores != null && modificadoresLite != null && modificadoresLite.size == modificadores.size) {
            modificadores.indices.map { i -> modificadoresLite[i].takeIf { it.isNotBlank() } ?: modificadores[i] }
        } else modificadores
        return copy(descricao = descricaoExibida, modificadores = modificadoresExibidos)
    }
}
