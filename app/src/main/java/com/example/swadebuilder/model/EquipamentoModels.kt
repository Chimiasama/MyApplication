package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EquipamentoItem(
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    @SerialName("custo")
    val custo: JsonElement? = null,
    val peso: JsonElement? = null,
    val origem: String? = null,
    val subtipo: String? = null,
    val subsubtipo: String? = null,
    val forcaMin: JsonElement? = null,
    val armadura: JsonElement? = null,
    val aparar: JsonElement? = null,
    val observacoes: JsonElement? = null,
    val dano: JsonElement? = null,
    val pa: JsonElement? = null,
    val cdt: JsonElement? = null,
    val distancia: JsonElement? = null,
    val tiros: JsonElement? = null,
    val tamanho: JsonElement? = null,
    val manobrabilidade: JsonElement? = null,
    val velMaxima: JsonElement? = null,
    val resistencia: JsonElement? = null,
    val tripulacao: JsonElement? = null,
    val malfuncionamento: JsonElement? = null,
    val tensao: Int? = null,
    val mods_slots: Int? = null,
    val pmf: Int? = null
)

@Serializable
data class EquipamentoCategoria(
    val tipo: String,
    val subtipo: String,
    val origem: String? = null,
    val subsubtipo: String? = null,
    val itens: List<EquipamentoItem>
)
