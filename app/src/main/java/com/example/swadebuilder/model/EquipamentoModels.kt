package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.GenericNameMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class EquipamentoItem(
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    @SerialName("custo")
    val custo: JsonElement? = null,
    val peso: JsonElement? = null,
    val origem: String? = null,
    val subtipo: String? = null,
    val subsubtipo: String? = null,
    val forcaMin: JsonElement? = null,
    val armadura: JsonElement? = null,
    val aparar: JsonElement? = null,
    val observacoes: JsonElement? = null,
    // Resumo genérico para "observacoes" na edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val dano: JsonElement? = null,
    val pa: JsonElement? = null,
    val cdt: JsonElement? = null,
    val distancia: JsonElement? = null,
    val tiros: JsonElement? = null,
    val tamanho: JsonElement? = null,
    val manobrabilidade: JsonElement? = null,
    val velMaxima: JsonElement? = null,
    val resistencia: JsonElement? = null,
    val tripulacao: JsonElement? = null,
    val pmf: JsonElement? = null,
    val malfuncionamento: JsonElement? = null,
    val tensao: Int? = null,
    @SerialName("mods_slots")
    val modsSlots: JsonElement? = null,
    val origemGrant: String? = null,
    // Id estável (slug do nome, gerado a partir de equipamentos.json) — permite endereçar
    // um item por id em vez de comparar nome/texto. Vazio só para instâncias construídas em
    // código (armas naturais, itens sintéticos) que nunca passaram pelo catálogo JSON.
    val id: String = ""
) {
    val nomeExibicao: String
        get() = if (EditionConfig.isFullEdition) {
            nome
        } else {
            GenericNameMapper.map(nome)
        }
}

@Serializable
data class EquipamentoCategoria(
    val tipo: String,
    val subtipo: String,
    val origem: String? = null,
    val subsubtipo: String? = null,
    val itens: List<EquipamentoItem>
)
