package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val choiceOptions: List<String> = emptyList()
) {
    val exigeCS: Boolean
        get() = observacoes.contains("Carta Selvagem", ignoreCase = true)
}

fun Vantagem.isClasseOuPrestigio(): Boolean =
    categoria == Categoria.CLASSE || categoria == Categoria.PRESTIGIO

fun List<Vantagem>.temMulticlasse(): Boolean = any { it.id == MULTICLASSE_VANTAGEM_ID }

fun List<Vantagem>.classeExclusivaBloqueada(nova: Vantagem): Boolean =
    nova.isClasseOuPrestigio() && !temMulticlasse() && any { it.isClasseOuPrestigio() }
