package com.example.swadebuilder.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

const val MULTICLASSE_VANTAGEM_ID = "multiclasse"
const val MENSAGEM_EXCLUSIVIDADE_CLASSE =
    "Você já possui uma Classe. Requer a vantagem Multiclasse para adicionar outra."

@Serializable(with = RequisitoSerializer::class)
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

object RequisitoSerializer : KSerializer<Requisito> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Requisito") {
        element<String>("estagio", isOptional = true)
        element<Map<String, Int>>("atributos", isOptional = true)
        element<Map<String, Int>>("pericias", isOptional = true)
        element<Map<String, Int>>("periciaMinOpcional", isOptional = true)
        element<List<String>>("vantagens_previas", isOptional = true)
        element<String>("observacoes", isOptional = true)
        element<List<String>>("choiceOptions", isOptional = true)
        element<List<String>>("tags", isOptional = true)
        element<JsonElement?>("template", isOptional = true)
    }

    @Serializable
    private data class RequisitoRaw(
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
        fun toDomain() = Requisito(
            estagio = estagio,
            atributoMin = atributoMin,
            periciaMin = periciaMin,
            periciaMinOpcional = periciaMinOpcional,
            vantagensPrevias = vantagensPrevias,
            observacoes = observacoes,
            choiceOptions = choiceOptions,
            tags = tags,
            template = template
        )
    }

    override fun deserialize(decoder: Decoder): Requisito {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("RequisitoSerializer only supports JSON")
        val element = jsonDecoder.decodeJsonElement()

        return when (element) {
            is JsonObject -> jsonDecoder.json.decodeFromJsonElement(RequisitoRaw.serializer(), element).toDomain()
            is JsonPrimitive -> {
                val raw = element.contentOrNull.orEmpty().trim()
                val stage = parseStageFromLegacyRequirement(raw)
                Requisito(estagio = stage, observacoes = raw)
            }
            else -> Requisito()
        }
    }

    override fun serialize(encoder: Encoder, value: Requisito) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("RequisitoSerializer only supports JSON")
        val raw = RequisitoRaw(
            estagio = value.estagio,
            atributoMin = value.atributoMin,
            periciaMin = value.periciaMin,
            periciaMinOpcional = value.periciaMinOpcional,
            vantagensPrevias = value.vantagensPrevias,
            observacoes = value.observacoes,
            choiceOptions = value.choiceOptions,
            tags = value.tags,
            template = value.template
        )
        jsonEncoder.encodeJsonElement(jsonEncoder.json.encodeToJsonElement(RequisitoRaw.serializer(), raw))
    }
}

private fun parseStageFromLegacyRequirement(raw: String): String {
    val knownStages = listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário", "Lendario")
    return knownStages.firstOrNull { stage ->
        raw.split(',').firstOrNull()?.trim()?.equals(stage, ignoreCase = true) == true
    }?.let { if (it.equals("Lendario", ignoreCase = true)) "Lendário" else it } ?: ""
}

fun Vantagem.isClasseOuPrestigio(): Boolean =
    categoria == Categoria.CLASSE || categoria == Categoria.PRESTIGIO

fun List<Vantagem>.temMulticlasse(): Boolean = any { it.id == MULTICLASSE_VANTAGEM_ID }

fun List<Vantagem>.classeExclusivaBloqueada(nova: Vantagem, isPathfinderActive: Boolean): Boolean {
    if (isPathfinderActive) return false
    return nova.isClasseOuPrestigio() && !temMulticlasse() && any { it.isClasseOuPrestigio() }
}
