package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class Requisito(
    @SerialName("estagio")
    val estagio: String = "",

    @SerialName("atributos")
    private val _atributoMin: Map<String, JsonElement> = emptyMap(),

    @SerialName("pericias")
    private val _periciaMin: Map<String, JsonElement> = emptyMap(),

    @SerialName("periciaMinOpcional")
    private val _periciaMinOpcional: Map<String, JsonElement> = emptyMap(),

    @SerialName("vantagens_previas")
    val vantagensPrevias: List<String> = emptyList(),

    @SerialName("observacoes")
    val observacoes: String = "",

    @SerialName("choiceOptions")
    val choiceOptions: List<String> = emptyList()
) {
    val exigeCS: Boolean
        get() = observacoes.contains("Carta Selvagem", ignoreCase = true)

    val atributoMin: Map<String, Int>
        get() = _atributoMin.mapValues { parseJsonInt(it.value) }

    val periciaMin: Map<String, Int>
        get() = _periciaMin.mapValues { parseJsonInt(it.value) }

    val periciaMinOpcional: Map<String, Int>
        get() = _periciaMinOpcional.mapValues { parseJsonInt(it.value) }

    private fun parseJsonInt(element: JsonElement): Int {
        if (element is JsonPrimitive) {
            return element.content.trim().toIntOrNull() ?: 0
        }
        return 0
    }
}
