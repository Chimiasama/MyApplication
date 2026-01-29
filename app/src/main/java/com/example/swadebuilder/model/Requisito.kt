package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

const val MULTICLASSE_VANTAGEM_ID = "multiclasse"
const val MENSAGEM_EXCLUSIVIDADE_CLASSE =
    "Você já possui uma Classe. Requer a vantagem Multiclasse para adicionar outra."

@Serializable
data class Requisito(
    @SerialName("estagio")
    val estagio: String = "",

    @SerialName("atributos")
    val atributoMin: Map<String, Int> = emptyMap(),

    @SerialName("pericias")
    val periciaMin: Map<String, Int> = emptyMap(),

    @SerialName("periciaMinOpcional")
    val periciaMinOpcional: Map<String, Int> = emptyMap(),

    @SerialName("vantagens_previas")
    val vantagensPrevias: List<String> = emptyList(),

    @SerialName("observacoes")
    val observacoes: String = "",

    @SerialName("choiceOptions")
    val choiceOptions: List<String> = emptyList(),

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("template")
    val template: JsonElement? = null
) {
    val exigeCS: Boolean
        get() = observacoes.contains("Carta Selvagem", ignoreCase = true)

    val templatesRequired: List<String>
        get() = when (template) {
            is JsonPrimitive -> listOfNotNull(template.contentOrNull)
            is JsonArray -> template.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            else -> emptyList()
        }
}

fun Vantagem.isClasseOuPrestigio(): Boolean =
    categoria == Categoria.CLASSE || categoria == Categoria.PRESTIGIO

fun List<Vantagem>.temMulticlasse(): Boolean = any { it.id == MULTICLASSE_VANTAGEM_ID }

fun List<Vantagem>.classeExclusivaBloqueada(nova: Vantagem): Boolean =
    nova.isClasseOuPrestigio() && !temMulticlasse() && any { it.isClasseOuPrestigio() }
