package com.example.swadebuilder.model

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