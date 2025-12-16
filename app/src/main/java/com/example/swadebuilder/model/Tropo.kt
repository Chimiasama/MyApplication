package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class Tropo(
    val id: String,
    val nome: String,
    val categoria: String,
    val origem: String,
    val tecnicas_iniciais: Int,
    val descricao: String,
    val ganha_ao_comprar: List<String>
)
