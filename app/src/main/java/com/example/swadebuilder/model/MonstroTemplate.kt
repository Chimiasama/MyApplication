package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.Serializable

@Serializable
data class MonstroTemplate(
    val id: String,
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val atributos_bonus: Map<String, Int> = emptyMap(),
    val habilidades: List<MonstroHabilidade> = emptyList(),
    val complicacoes: List<String> = emptyList(),
    // Mesmas strings de "complicacoes" (posição a posição), reescritas para a edição Lite.
    val complicacoesLite: List<String>? = null
) {
    fun exibido(): MonstroTemplate {
        if (EditionConfig.isFullEdition) return this
        val descricaoExibida = descricaoLite?.takeIf { it.isNotBlank() } ?: descricao
        val complicacoesExibidas = if (complicacoesLite != null && complicacoesLite.size == complicacoes.size) {
            complicacoes.indices.map { i -> complicacoesLite[i].takeIf { it.isNotBlank() } ?: complicacoes[i] }
        } else complicacoes
        return copy(
            descricao = descricaoExibida,
            habilidades = habilidades.map { it.exibida() },
            complicacoes = complicacoesExibidas
        )
    }
}

@Serializable
data class MonstroHabilidade(
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null
) {
    fun exibida(): MonstroHabilidade =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}
