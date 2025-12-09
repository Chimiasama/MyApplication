package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class MonstroTemplate(
    val id: String,
    val nome: String,
    val descricao: String,
    val atributos_bonus: Map<String, Int> = emptyMap(),
    val habilidades: List<MonstroHabilidade> = emptyList(),
    val complicacoes: List<String> = emptyList()
)

@Serializable
data class MonstroHabilidade(
    val nome: String,
    val descricao: String
)
