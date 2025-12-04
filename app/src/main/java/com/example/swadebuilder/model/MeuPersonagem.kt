package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import com.example.swadebuilder.model.SuperInvestment

@Serializable
data class MeuPersonagem(
    val nome: String,
    val ancestralidade: String,
    val celestialAAMilagresDesabilitado: Boolean,

    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,

    // Atributos Derivados
    val aparar: Int,
    val resistencia: Int,
    val tamanho: Int,
    val movimento: Int,
    val armadura: Int,

    // Vantagens e Complicações (por nome, para exibição)
    val vantagens: List<String>,
    val complicacoes: List<String>,
    val desvantagensRaciais: List<String>,

    // Equipamentos
    val equipamentos: List<EquipamentoItem>,
    val dinheiro: Int,

    // Poderes
    val poderes: Map<String, List<String>>,

    // Pontos
    val pontosRestantes: Int,

    // Supers
    val modoSupers: Boolean = false,
    val superPontosTotais: Int = 0,
    val superPontosDisponiveis: Int = 0,
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val poderFavoritoId: String? = null,
    val superInvestments: List<SuperInvestment> = emptyList(),
    val bonusPararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),
    val gastosPorPoder: Map<String, Int> = emptyMap(),
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // Anotações
    val anotacoes: String = ""
)