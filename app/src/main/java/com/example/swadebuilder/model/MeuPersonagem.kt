package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class MeuPersonagem(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,
    val vantagens: List<String>,
    val complicacoes: List<String>,
    val equipamentos: List<EquipamentoItem>,
    val poderes: Map<String, List<String>>,
    val dinheiro: Int,
    val pontosRestantes: Int
)