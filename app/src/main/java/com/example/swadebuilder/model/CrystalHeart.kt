package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.Serializable

@Serializable
data class CrystalHeart(
    val id: String,
    val nome: String,
    val estagio: String, // "Novato", "Experiente", etc.
    val habilidade_passiva: String? = null,
    val poderes: List<String> = emptyList(),
    val complicacao_inerente: String? = null,
    val origem: String? = "CRYSTAL_HEART",
    val descricao: String? = null,
    val custom: Boolean = false,
    val placeholder: Boolean = false,
    // Resumos genéricos para a edição Lite (não reproduzem o texto do livro original).
    val descricaoLite: String? = null,
    val habilidadePassivaLite: String? = null,
    val complicacaoInerenteLite: String? = null,
    // Mesmas strings de "poderes" (posição a posição), reescritas para a edição Lite.
    val poderesLite: List<String>? = null
) {
    fun exibido(): CrystalHeart {
        if (EditionConfig.isFullEdition || placeholder) return this
        val poderesExibidos = if (poderesLite != null && poderesLite.size == poderes.size) {
            poderes.indices.map { i -> poderesLite[i].takeIf { it.isNotBlank() } ?: poderes[i] }
        } else poderes
        return copy(
            descricao = descricaoLite?.takeIf { it.isNotBlank() } ?: descricao,
            habilidade_passiva = habilidadePassivaLite?.takeIf { it.isNotBlank() } ?: habilidade_passiva,
            complicacao_inerente = complicacaoInerenteLite?.takeIf { it.isNotBlank() } ?: complicacao_inerente,
            poderes = poderesExibidos
        )
    }
}
