package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.GenericNameMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNames

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
    // @JsonNames aceita as variantes "forca_min"/"for_min" que aparecem em ~98 itens do
    // catálogo (armas/armaduras/escudos de alguns sourcebooks) — sem isso, ignoreUnknownKeys
    // engolia essas chaves em silêncio e a Força Mínima desses itens nunca chegava ao app.
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @JsonNames("forca_min", "for_min")
    val forcaMin: JsonElement? = null,
    val armadura: JsonElement? = null,
    val aparar: JsonElement? = null,
    // Penalidade de Ataque Chamado ao usar o escudo pra se defender (ex.: "-2", "-4") —
    // regra de Escudos do livro básico, separada do bônus de Aparar.
    val cobertura: JsonElement? = null,
    // Modelo/tamanho de Área de Efeito (ex.: "MPE"/"MME"/"MGE" = Modelo Pequeno/Médio/Grande
    // de Explosão) de granadas, bombas e itens alquímicos explosivos.
    val explosao: JsonElement? = null,
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
    val id: String = "",
    // "tipo" de EquipamentoCategoria (ex.: "Armas de Fogo", "Armaduras", "Veículos") a usar
    // quando este item é mesclado numa categoria em DataLoader.updateActiveModules().
    // Só preenchido por itens criados no formulário de Equipamento customizado (ver
    // SettingsDialog.kt) — itens do catálogo oficial já carregam o `tipo` na própria
    // EquipamentoCategoria que os contém e não precisam disso. Nulo cai em "Equipamento Geral".
    @SerialName("categoria_tipo")
    val categoriaTipo: String? = null,
    // Override explícito de "esta arma à distância também serve corpo a corpo/arremesso"
    // (ex.: machado/adaga de arremesso) — definido no formulário de Equipamento
    // customizado (ver SettingsDialog.kt), onde o jogador marca isso diretamente em vez
    // de o app adivinhar pelo nome. Nulo = sem override, ResumoSection.kt cai de volta na
    // heurística de ForcaMinimaCalculator.ehArmaDeArremesso() usada pelo catálogo oficial
    // (que não tem esse campo).
    @SerialName("usavel_corpo_a_corpo")
    val usavelCorpoACorpo: Boolean? = null
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
