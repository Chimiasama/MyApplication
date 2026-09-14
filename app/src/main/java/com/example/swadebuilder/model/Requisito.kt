package com.example.swadebuilder.model

import com.example.swadebuilder.util.debugLog
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

const val MENSAGEM_EXCLUSIVIDADE_CLASSE =
    "Você já adquiriu uma Classe ou Prestígio neste Estágio. Aguarde o próximo Estágio para adquirir outra."

// "Pelo menos N destas opções" — ex.: Bando de Guerra (Fantasia) e Ordem-Unida (Arte da
// Guerra) exigem Comando + pelo menos 2 outras Vantagens de Liderança. Cada item de `opcoes`
// é um id de Vantagem OU Complicação (mesma sintaxe de `vantagensPrevias`, incluindo o
// sentinela "ANTECEDENTE_ARCANO" pra "qualquer Antecedente Arcano específico"). Verificado em
// conjunto (E) com `vantagensPrevias` — não o substitui.
@Serializable
data class GrupoMinimo(
    @SerialName("opcoes")
    val opcoes: List<String> = emptyList(),
    @SerialName("minimo")
    val minimo: Int = 1
)

// "Isto OU aquilo" — cada alternativa é um pacote de Vantagens/Complicações (E dentro dela)
// e/ou perícias mínimas; a Vantagem libera se QUALQUER UMA das alternativas for satisfeita
// por completo. Ex.: Pathfinder "Antecedente Arcano (qualquer um) OU Poderes Místicos
// (qualquer um)" vira duas alternativas de 1 vantagem cada; "Magomecânico OU Consertar d10+ e
// Ciência d10+" (Cidade do Sol a Vapor) vira uma alternativa de vantagem e outra de 2 perícias.
@Serializable
data class GrupoAlternativo(
    @SerialName("vantagens")
    val vantagens: List<String> = emptyList(),
    @SerialName("pericias")
    val pericias: Map<String, Int> = emptyMap()
)

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

    // Ids de CategoriaCustomizada (ver model/CategoriaCustomizada.kt) — pré-requisito
    // "genérico" além de vantagensPrevias (vantagem específica): exige que o
    // personagem já tenha ao menos uma Vantagem de CADA categoria customizada
    // listada aqui, sem precisar saber o id exato de qual vantagem daquela
    // categoria (ver ValidateCustomCategoryPrerequisiteUseCase). Só faz sentido
    // pra vantagens customizadas — o catálogo oficial nunca preenche isto.
    @SerialName("categoriasCustomizadasRequeridas")
    val categoriasCustomizadasRequeridas: List<String> = emptyList(),

    @SerialName("template")
    val template: JsonElement? = null,

    @SerialName("grupoMinimo")
    val grupoMinimo: GrupoMinimo? = null,

    @SerialName("gruposAlternativos")
    val gruposAlternativos: List<GrupoAlternativo> = emptyList()
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
        element<List<String>>("categoriasCustomizadasRequeridas", isOptional = true)
        element<JsonElement?>("template", isOptional = true)
        element<GrupoMinimo?>("grupoMinimo", isOptional = true)
        element<List<GrupoAlternativo>>("gruposAlternativos", isOptional = true)
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
        @SerialName("categoriasCustomizadasRequeridas")
        val categoriasCustomizadasRequeridas: List<String> = emptyList(),
        @SerialName("template")
        val template: JsonElement? = null,
        @SerialName("grupoMinimo")
        val grupoMinimo: GrupoMinimo? = null,
        @SerialName("gruposAlternativos")
        val gruposAlternativos: List<GrupoAlternativo> = emptyList()
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
            categoriasCustomizadasRequeridas = categoriasCustomizadasRequeridas,
            template = template,
            grupoMinimo = grupoMinimo,
            gruposAlternativos = gruposAlternativos
        )
    }

    override fun deserialize(decoder: Decoder): Requisito {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("RequisitoSerializer only supports JSON")
        return when (val element = jsonDecoder.decodeJsonElement()) {
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
            categoriasCustomizadasRequeridas = value.categoriasCustomizadasRequeridas,
            template = value.template,
            grupoMinimo = value.grupoMinimo,
            gruposAlternativos = value.gruposAlternativos
        )
        jsonEncoder.encodeJsonElement(jsonEncoder.json.encodeToJsonElement(RequisitoRaw.serializer(), raw))
    }
}

internal fun parseStageFromLegacyRequirement(raw: String): String {
    val knownStages = listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário", "Lendario")
    return knownStages.firstOrNull { stage ->
        raw.split(',').firstOrNull()?.trim()?.equals(stage, ignoreCase = true) == true
    }?.let { if (it.equals("Lendario", ignoreCase = true)) "Lendário" else it } ?: ""
}

fun Vantagem.isFamiliaClassePathfinder(): Boolean =
    categoria == Categoria.CLASSE ||
        categoria == Categoria.VANTAGEM_DE_CLASSE ||
        categoria == Categoria.PRESTIGIO

fun List<Vantagem>.classeExclusivaBloqueada(nova: Vantagem): Boolean {
    if (!nova.isFamiliaClassePathfinder()) return false
    return any { it.isFamiliaClassePathfinder() }
}

fun List<AdvancementAction>.atingiuLimiteClasseOuPrestigioNoEstagio(
    stageName: String,
    nova: Vantagem,
    vantagensCatalogo: List<Vantagem>,
    vantagensSelecionadas: List<Vantagem> = emptyList()
): Boolean {
    if (!nova.isFamiliaClassePathfinder()) return false

    fun debug(msg: String) {
        debugLog("RequisitoClasse", msg)
    }

    val idsFamiliaClasse = vantagensCatalogo
        .asSequence()
        .filter { it.isFamiliaClassePathfinder() }
        .map { it.id }
        .toSet()

    val hasCompraViaXpNoEstagio = any { acao ->
        acao is AdvancementAction.SpendOnAdvantage &&
            acao.stageName.equals(stageName, ignoreCase = true) &&
            acao.advantageId in idsFamiliaClasse
    }

    if (hasCompraViaXpNoEstagio) {
        debug("Bloqueio por histórico: stage=$stageName nova=${nova.id}")
        return true
    }

    // Criação de personagem acontece em Novato e pode conceder Classe/Prestígio
    // fora do histórico de avanço por XP.
    if (!stageName.equals("Novato", ignoreCase = true)) return false

    val idsFamiliaClasseViaXp = asSequence()
        .filterIsInstance<AdvancementAction.SpendOnAdvantage>()
        .map { it.advantageId }
        .filter { it in idsFamiliaClasse }
        .toSet()

    val temCompraFamiliaClasseDeCriacao = vantagensSelecionadas
        .asSequence()
        .filter { it.isFamiliaClassePathfinder() }
        .map { it.id }
        .any { it !in idsFamiliaClasseViaXp }

    if (temCompraFamiliaClasseDeCriacao) {
        debug("Bloqueio por criação em Novato: stage=$stageName nova=${nova.id}")
    }

    return temCompraFamiliaClasseDeCriacao
}
