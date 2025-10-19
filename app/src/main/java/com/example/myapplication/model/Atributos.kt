package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable

data class AtributoJson(
    val nome: String,
    val min: Int = 4
)

@Serializable

data class AtributoList(
    val atributos: List<AtributoJson>
)