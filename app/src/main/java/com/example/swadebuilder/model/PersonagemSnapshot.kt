package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonagemSnapshot(
    // Metadata
    val nomePersonagem: String,
    val anotacoes: String,
    val timestamp: Long = System.currentTimeMillis(),

    // Core Stats
    val ancestralidade: String,
    val dinheiro: Int,
    val valoresAtributos: Map<String, Int>,
    val pontosAtributo: Int,

    // Skills (Perícias)
    // Map skill name -> raw increments (baseIncs)
    val baseIncsPorPericia: Map<String, Int>,
    // Map skill name -> list of costs paid (1 or 2)
    val spCostStackPorPericia: Map<String, List<Int>>,

    // Selections
    val vantagens: List<VantagemSaveDto>,
    val complicacoes: List<ComplicacaoSaveDto>,
    val equipamentos: List<EquipamentoItem>,
    val poderesSelecionados: List<String>,

    // "Back-ending" Stacks (History for Undo/Recalc)
    val paCostStackPorAtributo: Map<String, List<Int>>,
    val cpPaStack: List<String>,
    // Saving count is enough for Unit lists as they just track quantity
    val cpPvStackCount: Int,
    val cpSpStackCount: Int,
    val pontosComplicacaoGastos: Int,

    // Derived/State flags that persist
    val modoSupers: Boolean,
    val modoSuperComplicacoes: Boolean,
    val modoSuperequip: Boolean,
    val compendioFantasiaAtivo: Boolean,
    val compendioHorrorAtivo: Boolean,
    val modoMonstroAtivo: Boolean,
    val tipoMonstroSelecionado: String?,
    val grandesResponsabilidades: Boolean,
    val maisPontosPericias: Boolean,
    val cartaSelvagem: Boolean,
    val heroisSemArmadura: Boolean,
    val soldadoCargaAtivo: Boolean,
    val regraMultiplosIdiomas: Boolean,
    val permiteMultiAntecedenteArcano: Boolean,
    val usarEspecializacoesDePericia: Boolean,
    val celestialAAMilagresDesabilitado: Boolean,
    val meioElfoAgil: Boolean,
    val nasceUmHeroi: Boolean,

    // Progression Phase
    val progresso: Int,
    val stageXpSpent: Map<String, Int>,
    val advancementHistory: List<AdvancementAction>,
    val frozenSkillIncrements: Map<String, Int>,
    val frozenAdvantageCount: Int,
    val legendaryAttrReservations: Int,
    val xpSlots: List<Boolean>, // Visual state of slots
    val pvFromXpOutstanding: Int,

    // Supers Phase
    val superInvestments: List<SuperInvestment>,
    val superPontosTotais: Int,
    val superNivelCampanha: Int?,
    val poderFavoritoId: String?,
    val gastosPorPoder: Map<String, Int>,
    val bonusPararFromPower: Int,
    val bonusResFromPower: Int,
    val armorFromPower: Int,
    val bonusMovimentacaoFromPower: Int,
    val naturalArmorFromRace: Int,

    // Magic/Arcane State
    val poderSlotsPorArcano: Map<String, List<String?>>,
    val novosPoderesStacksPorArcano: Map<String, List<List<String>>>,
    val comprasPpPorEstagio: Map<String, Int>,
    val bonusPoderExtra: Int,

    // Especializações
    val especializacoesPorPericia: Map<String, EspecializacoesDto>
)

@Serializable
data class VantagemSaveDto(
    val id: String,
    val choice: String?
)

@Serializable
data class ComplicacaoSaveDto(
    val id: String,
    val severity: String? // "Menor" or "Maior"
)
