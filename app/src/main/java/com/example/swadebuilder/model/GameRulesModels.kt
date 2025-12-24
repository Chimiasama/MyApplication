package com.example.swadebuilder.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
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
data class SuperPoder(
    val nome: String,
    val estagio: String = "iniciante",
    val custoBase: String? = null,
    val modificadores: List<String>? = null,
    val descricao: String? = null,
    val manifestacoes: JsonElement? = null
)

@Serializable
data class Vantagem(
    val id: String,
    val nome: String,
    val originalName: String? = null,
    val origem: String, // ex: "BASICO", "FANTASIA"
    val categoria: Categoria,
    val requisitos: Requisito,
    val descricao: String,
    val originalDescription: String? = null,
    val template: JsonElement? = null,
    // Transient user choice for 'Novos Poderes' or similar
    var choice: String? = null
)

@Serializable
data class Complicacao(
    val id: String, // Should correspond to "name" in JSON if needed, or separate id field
    val name: String,
    val originalName: String? = null,
    val severity: String, // "Major", "Minor"
    val description: String,
    val originalDescription: String? = null,
    val origem: String? = "BASICO"
)

@Serializable
data class Atributo(
    val nome: String,
    val dado: Int,
    val id: String = "" // Added to help mapping if needed
)

@Serializable
data class AtributoJson(
    val nome: String,
    val id: String,
    val descricao: String
)

@Serializable
data class Pericia(
    val nome: String,
    val atributo: String,
    val descricao: String,
    val base: Boolean = false, // If true, starts at d4-2 (core skills)
    val id: String = ""
)

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String
)

@Serializable
data class RacialModifier(
    val id: String,
    val nome: String,
    val originalName: String? = null,
    val descricao: String,
    val originalDescription: String? = null,
    val origem: String = "BASICO",
    val atributos: Map<String, Int> = emptyMap(),
    val pericias: Map<String, Int> = emptyMap(),
    val vantagensGratis: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val habilidades: List<RacialAbility> = emptyList(),
    val movimento: Int = 6,
    val tamanho: Int = 0, // Tamanho modifier (e.g., -1 for Small)
    val aparar: Int = 0,
    val resistencia: Int = 0,
    val armadura: Int = 0,
    val carisma: Int = 0,
    val pontosPoder: Int = 0, // Extra Power Points
    val pontosPericia: Int = 0 // Extra Skill Points
)

@Serializable
data class Requisito(
    val estagio: String = "", // N, E, V, H, L
    val atributoMin: Map<String, Int> = emptyMap(), // ex: {"agilidade": 8} => d8
    val periciaMin: Map<String, Int> = emptyMap(),  // ex: {"lutar": 8} => d8
    val periciaMinOpcional: Map<String, Int> = emptyMap(), // OR condition for skills
    val vantagens: List<String> = emptyList(),      // IDs of required Edges
    val observacoes: String = "",                   // Textual requirement
    val cartaSelvagem: Boolean = false              // Requires Wild Card status
)

@Serializable
data class EquipamentoItem(
    val id: String,
    val nome: String,
    val originalName: String? = null,
    val custo: JsonElement? = null, // String or Int
    val peso: JsonElement? = null,  // String or Int
    val descricao: String,
    val origem: String? = null,
    val subtipo: String? = null,
    val subsubtipo: String? = null,
    // Weapon fields
    val dano: JsonElement? = null,
    val pa: JsonElement? = null,    // AP
    val cdt: JsonElement? = null,   // RoF
    val distancia: JsonElement? = null,
    val tiros: JsonElement? = null, // Shots
    val forcaMin: JsonElement? = null, // Min Str
    // Armor/Shield fields
    val armadura: JsonElement? = null,
    val aparar: JsonElement? = null,   // Parry bonus
    val cobertura: JsonElement? = null, // Cover
    // Vehicle fields
    val velMaxima: JsonElement? = null,    // Top Speed
    val aceleracao: JsonElement? = null,   // Accel
    val manobrabilidade: JsonElement? = null, // Handling
    val tamanho: JsonElement? = null,      // Size
    val resistencia: JsonElement? = null,  // Toughness
    val tripulacao: JsonElement? = null,   // Crew
    val passageiros: JsonElement? = null,
    // Special/Notes
    val observacoes: JsonElement? = null,
    // Cybernetics / Sci-Fi
    val tensao: Int? = null, // Strain
    val mods_slots: Int? = null, // Slots used/provided
    // Deadlands
    val malfuncionamento: String? = null, // Malfunction text
    // Steam Sun
    val pmf: Int? = null // PMF value
)

@Serializable
data class EquipamentoCategoria(
    val categoria: String,
    val itens: List<EquipamentoItem>
)

@Serializable
data class Tropo(
    val id: String,
    val nome: String,
    val categoria: String,
    val origem: String,
    @SerialName("tecnicas_iniciais")
    val tecnicasIniciais: Int = 0,
    val descricao: String = "",
    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList()
)

@Serializable
data class CrystalHeart(
    val id: String,
    val nome: String,
    val estagio: String, // "Novato", "Experiente", etc.
    val pontos_poder: Int,
    val slots: Int,
    val habilidade_passiva: String? = null,
    val poderes: List<String> = emptyList(),
    val complicacao_inerente: String? = null,
    val origem: String? = "CRYSTAL_HEART",
    val descricao: String? = null
)

@Serializable
data class MonstroTemplate(
    val id: String,
    val nome: String,
    val descricao: String,
    val atributos_bonus: Map<String, Int> = emptyMap(),
    val habilidades: List<MonstroHabilidade> = emptyList(),
    val complicacoes: List<String> = emptyList()
)

@Serializable
data class MonstroHabilidade(
    val nome: String,
    val descricao: String
)
