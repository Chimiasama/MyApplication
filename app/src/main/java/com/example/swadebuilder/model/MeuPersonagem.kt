package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class MeuPersonagem(
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,
    val celestialAAMilagresDesabilitado: Boolean,

    // Vantagens e Complicações já persistidas por ID:
    val vantagens: List<String>,
    val complicacoes: List<String>,
    val desvantagensRaciais: List<String>,

    // Equipamentos com dados para impressão:
    val equipamentos: List<EquipamentoItem>,

    // Poderes arcanos, como antes:
    val poderes: Map<String, List<String>>,

    // Dinheiro e PV restantes (vantagens):
    val dinheiro: Int,
    val dadoRiqueza: Int? = null,
    val pontosRestantes: Int,

    val naturalArmorFromRace: Int = 0,
    val armorBase: Int = 0,
    val advantageChoices: Map<String, List<String>> = emptyMap(),
    val vantagensRaciais: List<String> = emptyList(),

    // ===== NOVOS CAMPOS (SUPERS) =====
    val modoSupers: Boolean = false,
    val modoMonstroAtivo: Boolean = false,
    val tipoMonstroSelecionado: String? = null,
    val superPontosTotais: Int = 0,
    val superPontosDisponiveis: Int = 0,

    // Limites & favorecido
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val poderFavoritoId: String? = null,

    // Ledger de efeitos de poder (somente supers)
    val superInvestments: List<SuperInvestment> = emptyList(),
    val bonusPararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),         // ids de vantagens “compradas” por super
    val gastosPorPoder: Map<String, Int> = emptyMap(),      // PoderId -> custo gasto (para auditoria)

    // Para conferência de teto de mitigação definido pela campanha:
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // ===== ANOTAÇÕES LIVRES PARA RESUMO / FICHA =====
    val anotacoes: String = "",

    val soldadoCargaAtivo: Boolean = true,
    val modoOficialAtivo: Boolean = false,
    val compendioArteDaGuerraAtivo: Boolean = false
)