package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class EspecializacoesDto(
    val principal: String? = null,
    val lista: List<String> = emptyList()
)
