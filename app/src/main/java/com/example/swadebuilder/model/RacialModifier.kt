package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String
)

@Serializable
data class RacialModifier(
    val id: String? = null,
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    val descricao: String? = null,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val habilidades: List<RacialAbility> = emptyList(),
    val origem: String = "BASICO",
    val movimentacao: Int = 0
)

@Serializable
data class HabilidadeCriacao(
    val nome: String,
    val custo: Int,
    val descricao: String
)

@Serializable
data class TabelaCriacaoRaca(
    val pontos_iniciais: Int,
    val habilidades: List<HabilidadeCriacao>
)

@Serializable
data class RegrasCriacaoRacaJson(
    val tabela_criacao: TabelaCriacaoRaca
)
