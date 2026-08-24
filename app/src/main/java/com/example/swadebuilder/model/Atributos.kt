package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable

data class AtributoJson(
    val nome: String,
    val min: Int = 4,
    val descricao: String? = null,
    // Resumo genérico usado na edição Lite, para não reproduzir o texto do livro original
    // (evita risco de direitos autorais). Cai para `descricao` enquanto não for escrito.
    val descricaoLite: String? = null
)

@Serializable

data class AtributoList(
    val atributos: List<AtributoJson>
)