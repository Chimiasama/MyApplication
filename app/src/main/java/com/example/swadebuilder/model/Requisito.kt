package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Requisito(
    @SerialName("estagio")
    val estagio: String = "",

    @SerialName("atributos")
    val atributoMin: Map<String, Int> = emptyMap(),

    @SerialName("pericias")
    val periciaMin: Map<String, @Serializable(with = IntOrStringSerializer::class) Int> = emptyMap(),

    @SerialName("periciaMinOpcional")
    val periciaMinOpcional: Map<String, Int> = emptyMap(),

    @SerialName("vantagens_previas")
    val vantagensPrevias: List<String> = emptyList(),

    @SerialName("observacoes")
    val observacoes: String = "",

    @SerialName("choiceOptions")
    val choiceOptions: List<String> = emptyList()
) {
    val exigeCS: Boolean
        get() = observacoes.contains("Carta Selvagem", ignoreCase = true)
}
