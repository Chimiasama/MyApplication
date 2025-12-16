package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class TecnicaChi(
    val id: String,
    val nome: String,
    val custo: Int,
    val duracao: String,
    val distancia: String,
    val estagio: String,
    val origem: String,
    val descricao: String
)
