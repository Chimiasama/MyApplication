package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Complicacao(
    val id: String,
    val name: String,
    val severity: String,
    val description: String,
    val origem: String,
    val observacoes: String = "",
    @SerialName("vantagens_previas")
    val vantagensPrevias: List<String> = emptyList()
)
