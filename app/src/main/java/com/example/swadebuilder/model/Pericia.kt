package com.example.swadebuilder.model

data class Pericia(
    val nome: String,
    val atributo: String,
    val basica: Boolean,
    val origem: String? = null,
    val descricao: String? = null,
    // Id estável (slug do nome, gerado a partir de pericias.json) — vazio só para instâncias
    // construídas em código (idiomas/Jutsu extras) que não vêm do catálogo JSON.
    val id: String = ""
)
