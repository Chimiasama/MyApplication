package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class PersonagemSalvo(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,

    // IDs de vantagens
    val vantagens: List<String>,

    // IDs de complicações
    val complicacoes: List<String>,

    // --- NOVOS CAMPOS: Persistência dos gastos de Pontos de Complicação ---
    val cpPaCount: Int = 0,       // Quantos aumentos de Atributo (PA) via PC
    val cpPvCount: Int = 0,       // Quantos Pontos de Vantagem (PV) via PC
    val cpSpCount: Int = 0,       // Quantos Pontos de Perícia (SP) via PC
    val cpRecursosCount: Int = 0, // Quantos aumentos de dinheiro ($$) via PC
    // ----------------------------------------------------------------------

    // Equipamentos por nome (como antes)
    val equipamentos: List<String>,

    // Poderes arcanos (slots por chave de AA) — como antes
    val poderes: Map<String, List<String>>,

    val dinheiro: Int,
    val pontosRestantes: Int,
    val naturalArmorFromRace: Int = 0,
    val armorBase: Int = 0,
    val vantagemChoices: Map<String, List<String>> = emptyMap(),
    val vantagensRaciais: List<String> = emptyList(),
    val maisPontosPericias: Boolean,
    val cartaSelvagem: Boolean,
    val heroisSemArmadura: Boolean = false,
    val soldadoCargaAtivo: Boolean = true,
    val semPontosDePoder: Boolean = false,

    // Especializações existentes
    val usarEspecializacoesDePericia: Boolean = false,
    val especializacoesPorPericia: Map<String, EspecializacoesDto> = emptyMap(),

    // ===== MODOS SUPER =====
    val modoSupers: Boolean = false,
    val modoSuperequip: Boolean = false,
    val modoSuperComplicacoes: Boolean = false,

    val superInvestments: List<SuperInvestment> = emptyList(),

    // ===== NOVOS CAMPOS (SUPERS) =====
    val superPontosTotais: Int = 0,
    val superPontosDisponiveis: Int = 0,

    // Limites & favorecido
    val limitePorPoderPadrao: Int = 0,
    val limiteFavorecido: Int = 0,
    val poderFavoritoId: String? = null,

    // Ledger de efeitos de poder
    val bonusPararFromPower: Int = 0,
    val bonusResFromPower: Int = 0,
    val armorFromPower: Int = 0,
    val bonusMovimentacaoFromPower: Int = 0,
    val vantagensDePoder: Set<String> = emptySet(),
    val gastosPorPoder: Map<String, Int> = emptyMap(),

    // Teto de mitigação por supers (campanha)
    val limiteDePoderDaCampanha: Int = Int.MAX_VALUE,

    // ===== ANOTAÇÕES LIVRES DO JOGADOR =====
    val anotacoes: String = ""
)

@Serializable
data class EspecializacoesDto(
    val principal: String? = null,
    val lista: List<String> = emptyList()
)