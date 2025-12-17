package com.example.swadebuilder.model

import com.example.swadebuilder.ui.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable
data class PersonagemSnapshot(
    val id: String,
    val nome: String,
    val timestamp: Long,
    val appTheme: String,
    // showHelpMessages removido (defaults to false for compatibility)
    val showHelpMessages: Boolean = false,
    val anotacoes: String,
    val flags: SnapshotFlags,
    val recursos: SnapshotRecursos,
    val atributos: SnapshotAtributos,
    val pericias: SnapshotPericias,
    val selecoes: SnapshotSelecoes,
    val progresso: SnapshotProgresso,
    val supers: SnapshotSupers
)

@Serializable
data class SnapshotFlags(
    val cartaSelvagem: Boolean,
    val maisPontosPericias: Boolean,
    val modoSupers: Boolean,
    val compendioFantasiaAtivo: Boolean,
    val compendioHorrorAtivo: Boolean,
    val compendioSciFiAtivo: Boolean,
    val compendioTrilhadorAtivo: Boolean,
    val compendioDeadlandsAtivo: Boolean,
    val compendioCrystalHeartAtivo: Boolean,
    val compendioArteDaGuerraAtivo: Boolean,
    val compendioCidadeSolVaporAtivo: Boolean,
    val compendioWiseguysAtivo: Boolean,
    val modoOficialAtivo: Boolean,
    val modoMonstroAtivo: Boolean,
    val tipoMonstroSelecionado: String? = null,
    val usarEspecializacoesDePericia: Boolean,
    val grandesResponsabilidades: Boolean,
    val modoSuperComplicacoes: Boolean = false,
    val modoSuperequip: Boolean = false,
    val heroisSemArmadura: Boolean = false,
    val nasceUmHeroi: Boolean = false,
    val soldadoCargaAtivo: Boolean = true,
    val permiteMultiAntecedenteArcano: Boolean = false,
    val meioElfoAgil: Boolean = false,
    val celestialAAMilagresDesabilitado: Boolean = false,
    val jovemAutoPequeno: Boolean = false,
    val jovemMalusPa: Int = 0,
    val jovemMalusSp: Int = 0,
    val idosoBonusSp: Int = 0,
    val obesoBonusSize: Int = 0,
    val obesoMalusMov: Int = 0,
    val bonusPoderExtra: Int = 0
)

@Serializable
data class SnapshotRecursos(
    val dinheiro: Int,
    val pontosVantagem: Int,
    val pontosAtributo: Int,
    val pontosComplicacaoGastos: Int,
    val paFromProgress: Int,
    val spFromProgress: Int,
    val legendaryAttrReservations: Int,
    val cpPaStack: List<String>,
    val cpSpStack: List<Int>, // dummy list of 1s
    val cpPvStack: List<Int>, // dummy list of 1s
    val cpRecursosStack: List<Int> // dummy list of 1s
)

@Serializable
data class SnapshotAtributos(
    val ancestralidade: String,
    val valoresAtributos: Map<String, Int>,
    val paCostStackPorAtributo: Map<String, List<Int>>
)

@Serializable
data class SnapshotPericias(
    val baseIncsPorPericia: Map<String, Int>,
    val compIncsPorPericia: Map<String, Int>,
    val spCostStackPorPericia: Map<String, List<Int>>,
    val compCostStackPorPericia: Map<String, List<Int>>,
    val especializacoesPorPericia: Map<String, EspecializacoesDto>
)

@Serializable
data class SnapshotSelecoes(
    val vantagens: List<AdvantageSnapshot>,
    val vantagensAutomaticas: List<String>,
    val vantagensRaciais: List<String>,
    val desvantagensAutomaticas: List<String>,
    val desvantagensRaciais: List<String>,
    val complicacoesSelecionadas: List<ComplicacaoSnapshot>,
    val reservasComplicacaoMaior: Map<String, Boolean>,
    val poderesSelecionados: List<String>,
    val poderSlotsPorArcano: Map<String, List<String?>>,
    val novosPoderesStacksPorArcano: Map<String, List<List<String>>>,
    val arcanoEmCompraViaXpKey: String?,
    val arcanoSnapshotAntesDaCompra: List<String?>?,
    val equipamentosComprados: List<EquipamentoItem>,
    val coracaoCrystalId: String? = null
)

@Serializable
data class AdvantageSnapshot(
    val id: String,
    val choice: String?
)

@Serializable
data class ComplicacaoSnapshot(
    val id: String,
    val nivel: String? // "Menor" or "Maior"
)

@Serializable
data class SnapshotProgresso(
    val progresso: Int,
    val progressosDisponiveis: Int,
    val stageXpSpent: Map<String, Int>,
    val xpSlots: List<Boolean>,
    val advancementHistory: List<AdvancementAction>,
    val frozenSkillIncrements: Map<String, Int>,
    val skillAdvancementInProgress: Boolean,
    val skillsForCurrentAdvancement: List<String>,
    val advantageAdvancementInProgress: Boolean,
    val advantageForCurrentAdvancement: String?,
    val attributeAdvancementInProgress: Boolean,
    val attributeStageForCurrentAdvancement: String?,
    val stageNameForCurrentAdvancement: String?,
    val attributeStacksBeforeAdvancement: Map<String, Int>?,
    val attributeUsedReservation: Boolean,
    val overrideStageForVantagem: String?,
    val emProgresso: Boolean,
    val modoProgressaoAtivo: Boolean,
    val mostrandoVantagensProgresso: Boolean,
    val mostrandoPericiasProgresso: Boolean,
    val mostrandoAtributosProgresso: Boolean,
    val mostrandoPoderesProgresso: Boolean,
    val frozenAdvantageCount: Int,
    val stageNameForCurrentAdvancementSnapshot: String? = null
)

@Serializable
data class SnapshotSupers(
    val superInvestments: List<SuperInvestment>,
    val superNivelCampanha: Int?,
    val usarSemPontosDePoder: Boolean,
    val superPontosTotais: Int,
    val superPontosDisponiveis: Int,
    val superLimite: Int,
    val superLimitePorPoder: Int,
    val poderFavoritoId: String?,
    val limiteDePoderDaCampanha: Int,
    val bonusPararFromPower: Int,
    val bonusResFromPower: Int,
    val armorFromPower: Int,
    val bonusMovimentacaoFromPower: Int,
    val vantagensDePoder: List<String>,
    val gastosPorPoder: Map<String, Int>,
    val faseSupersAtiva: Boolean,
    val comprasPpPorEstagio: Map<String, Int>,
    val comprasAttrPorEstagio: Map<String, Int>,
    val superPontosDisponiveisFlag: Boolean
)
