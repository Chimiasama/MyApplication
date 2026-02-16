package com.example.swadebuilder.model

data class Pericia(
    val nome: String,
    val atributo: String,
    val basica: Boolean,
    val origem: String? = null,
    val descricao: String? = null
)
