package com.example.swadebuilder.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serializador que aceita tanto literal numérico (ex: 8) quanto string (ex: "8" ou "").
 * - Se JSON vier como número (ex: 8), lê via content e converte em Int.
 * - Se JSON vier como string numérica (ex: "10"), devolve 10.
 * - Se JSON vier como string vazia ou não-numérica (ex: ""), devolve 0.
 */
object IntOrStringSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntOrStringSerializer", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Int {
        // Garante que estamos usando o parser JSON
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("IntOrStringSerializer só funciona com JSON")

        // Pega o elemento inteiro/primitive do JSON
        return when (val element: JsonElement = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive -> {
                // Se for string, tenta converter; se for objeto numérico, content traz o dígito
                val content = element.content.trim()
                content.toIntOrNull() ?: 0
            }
            else -> 0
        }
    }

    override fun serialize(encoder: Encoder, value: Int) {
        // Ao serializar, basta escrever como inteiro
        encoder.encodeInt(value)
    }
}
