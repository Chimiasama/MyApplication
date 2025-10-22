package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class RacialModifier(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String>,
    val desvantagens: List<String>
)
