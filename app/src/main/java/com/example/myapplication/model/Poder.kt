package com.example.myapplication.model

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
    val manifestacoes: List<String>,
    val descricao: String,
    val modificadores: List<Modificador>
)

@Serializable
data class PoderesList(
    val poderes: List<Poder>
)
