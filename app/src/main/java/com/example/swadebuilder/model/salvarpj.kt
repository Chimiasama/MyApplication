package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import java.util.UUID

import com.example.swadebuilder.model.AdvancementAction

@Serializable
data class PersonagemSalvo(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val ancestralidade: String,

    // IDs de vantagens
    val vantagens: List<String>,

    // Metadados completos de vantagens (id + escolha), preserva ordem e escolhas individuais
    val vantagensDetalhadas: List<VantagemSalva> = emptyList(),

    // IDs de complicações
    val complicacoes: List<String>,
    val complicacoesNiveis: Map<String, String> = emptyMap(),
    val reservasComplicacaoMaior: Set<String> = emptySet(),

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
    val compendioFantasiaAtivo: Boolean = false,
    val compendioHorrorAtivo: Boolean = false,
    val modoMonstroAtivo: Boolean = false,
    val tipoMonstroSelecionado: String? = null,
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
    val anotacoes: String = "",

    // ===== ESTADO DE PROGRESSOS E XP =====
    val progresso: Int = 0,
    val progressosDisponiveis: Int = 0,
    val stageXpSpent: Map<String, Int> = emptyMap(),
    val xpSlots: List<Boolean> = emptyList(),
    val modoProgressaoAtivo: Boolean = false,
    val emProgresso: Boolean = false,
    val mostrandoVantagensProgresso: Boolean = false,
    val mostrandoPericiasProgresso: Boolean = false,
    val mostrandoAtributosProgresso: Boolean = false,
    val mostrandoPoderesProgresso: Boolean = false,
    val frozenAdvantageCount: Int = 0,
    val frozenSkillIncrements: Map<String, Int> = emptyMap(),
    val paFromProgress: Int = 0,
    val pvFromXpOutstanding: Int = 0,
    val comprasAttrPorEstagio: Map<String, Int> = emptyMap(),
    val comprasPpPorEstagio: Map<String, Int> = emptyMap(),

    val skillAdvancementInProgress: Boolean = false,
    val skillsForCurrentAdvancement: List<String> = emptyList(),
    val advantageAdvancementInProgress: Boolean = false,
    val advantageForCurrentAdvancement: String? = null,
    val attributeAdvancementInProgress: Boolean = false,
    val attributeStageForCurrentAdvancement: String? = null,
    val stageNameForCurrentAdvancement: String? = null,
    val attributeStacksBeforeAdvancement: Map<String, Int>? = null,
    val attributeUsedReservation: Boolean = false,

    val advancementHistory: List<AdvancementAction> = emptyList()
)

@Serializable
data class VantagemSalva(
    val id: String,
    val choice: String? = null
)

@Serializable
data class EspecializacoesDto(
    val principal: String? = null,
    val lista: List<String> = emptyList()
)