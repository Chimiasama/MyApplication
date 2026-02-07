package com.example.swadebuilder.model

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
    val descricao: String? = null
)
