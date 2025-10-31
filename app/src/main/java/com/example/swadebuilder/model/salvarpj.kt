package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PersonagemSalvo(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,
    val vantagens: List<String>,
    val complicacoes: List<String>,
    val equipamentos: List<String>,
    val poderes: Map<String, List<String>>,
    val dinheiro: Int,
    val pontosRestantes: Int,
    val maisPontosPericias: Boolean,
    val cartaSelvagem: Boolean,
    val heroisSemArmadura: Boolean = false
)