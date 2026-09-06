package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class PericiaJson(
    val nome: String,
    val atributo: String = "",
    val basica: Boolean = false,
    val origem: String? = null,
    val descricao: String? = null,
    val id: String = ""
)
