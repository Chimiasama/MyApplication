package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String
)

@Serializable
data class AncestryVariant(
    val id: String,
    val nome: String,
    val isDefault: Boolean = false,
    val removeHabilidades: List<String> = emptyList(),
    val removeDesvantagens: List<String> = emptyList(),
    val removeVantagens: List<String> = emptyList(),
    val addHabilidades: List<RacialAbility> = emptyList(),
    val addDesvantagens: List<String> = emptyList(),
    val addVantagens: List<String> = emptyList(),
    val naturalArmorBonus: Int? = null,
    val forceArmorZero: Boolean? = null,
    val anotacoes: List<String> = emptyList()
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
    val movimentacao: Int = 0,
    val tags: List<String> = emptyList(),
    val opcoes: List<String> = emptyList(),
    val variantes: List<AncestryVariant> = emptyList(),
    val naturalArmorBonus: Int = 0,
    val forceArmorZero: Boolean = false
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
