package com.example.swadebuilder.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

object StringOrIntSerializer : KSerializer<String> {
    override val descriptor = PrimitiveSerialDescriptor("StringOrInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        require(decoder is JsonDecoder)
        return when (val element: JsonElement = decoder.decodeJsonElement()) {
            is JsonPrimitive -> element.content
            else -> element.toString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {

        encoder.encodeString(value)
    }
}

@Serializable
data class Modificador(
    val nome: String,
    val custo: String,
    val descricao: String
)

@Serializable
data class Poder(
    val id: String,
    val nome: String,
    val origem: String,            // Ex.: "BASICO"
    val estagio: String,           // Ex.: "Novato", "Experiente"...
    @Serializable(with = StringOrIntSerializer::class)
    val pontosDePoder: String,     // agora sempre String, mas aceita número ou texto no JSON
    val distancia: String,
    val duracao: String,
    val manifestacoes: List<String> = emptyList(),
    val descricao: String,
    val modificadores: List<Modificador> = emptyList(),
    // Id de CategoriaCustomizada (ver model/CategoriaCustomizada.kt) — Poder não tem
    // categoria oficial fixa, então isto é a única categorização possível, e só se
    // aplica a Poderes customizados criados pelo Mestre.
    val categoriaCustomizadaId: String? = null
)

object ModificadoresGlobaisDePoder {
    val LISTA: List<Modificador> = listOf(
        Modificador("Dano Adicional (+2)", "+1 PP", "Adiciona +2 de dano ao poder de ataque."),
        Modificador("Dano Adicional (+4)", "+2 PP", "Adiciona +4 de dano ao poder de ataque."),
        Modificador("Alcance Expandido", "+1 PP", "Dobra o Alcance/Distância do poder."),
        Modificador("Duração Expandida", "+1 PP", "Dobra a Duração base do poder."),
        Modificador("Área de Efeito (MPE)", "+1 PP", "Afeta todos no Modelo Pequeno de Explosão."),
        Modificador("Área de Efeito (MME)", "+2 PP", "Afeta todos no Modelo Médio de Explosão."),
        Modificador("Área de Efeito (MGE)", "+3 PP", "Afeta todos no Modelo Grande de Explosão."),
        Modificador("Glow / Som", "+1 PP", "Gera efeito luminoso ou acústico chamativo ao usar o poder.")
    )
}
