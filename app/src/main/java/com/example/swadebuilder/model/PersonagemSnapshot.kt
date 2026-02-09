package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class PersonagemSnapshot(
    val version: Int = 1,
    val id: String,
    val nome: String,
    val timestamp: Long,
    val appTheme: String,
    val hapticStrength: Int = 70,
    val soundVolume: Int = 70,
    val anotacoes: String,
    val flags: SnapshotFlags,
    val recursos: SnapshotRecursos,
    val atributos: SnapshotAtributos,
    val pericias: SnapshotPericias,
    val selecoes: SnapshotSelecoes,
    val progresso: SnapshotProgresso,
    val supers: SnapshotSupers,
    val checksum: String? = null
)

@Serializable
data class SnapshotFlags(
    val cartaSelvagem: Boolean,
    val maisPontosPericias: Boolean,
    val modoSupers: Boolean,
    val compendioFantasiaAtivo: Boolean,
    val compendioHorrorAtivo: Boolean,
    val compendioSciFiAtivo: Boolean = false,
    val compendioScifiMechasCiberneticosAtivo: Boolean = false,
    val compendioPathfinderAtivo: Boolean = false,
    val compendioDeadlandsAtivo: Boolean = false,
    val compendioCrystalHeartAtivo: Boolean = false,
    val compendioArteDaGuerraAtivo: Boolean = false,
    val compendioCidadeSolVaporAtivo: Boolean = false,
    val compendioWiseguysAtivo: Boolean = false,
    val optRegraFama: Boolean = false,
    val modoOficialAtivo: Boolean = false,
    val modoMonstroAtivo: Boolean,
    val tipoMonstroSelecionado: String?,
    val usarEspecializacoesDePericia: Boolean,
    val grandesResponsabilidades: Boolean,
    val nasceUmHeroi: Boolean,
    val soldadoCargaAtivo: Boolean,
    val permiteMultiAntecedenteArcano: Boolean,
    val meioElfoAgil: Boolean,
    val meioOrcForca: Boolean = false,
    val celestialAAMilagresDesabilitado: Boolean,
    val jovemAutoPequeno: Boolean,
    val jovemMalusPa: Int,
    val jovemMalusSp: Int,
    val idosoBonusSp: Int,
    val obesoBonusSize: Int,
    val obesoMalusMov: Int,
    val bonusPoderExtra: Int,
    val optRegraRiqueza: Boolean = false,
    val optRegraCosaNostra: Boolean = false
)

@Serializable
data class SnapshotRecursos(
    val dinheiro: Int,
    val requisicao: Int = 1,
    val pontosVantagem: Int,
    val pontosAtributo: Int,
    val pontosComplicacaoGastos: Int,
    val famaManual: Int = 0,
    val paFromProgress: Int,
    val spFromProgress: Int,
    val legendaryAttrReservations: Int,
    val cpPaStack: List<String>,
    val cpSpStack: List<Int>,
    val cpPvStack: List<Int>,
    val cpRecursosStack: List<Int>,
    val riquezaModifier: Int = 0,
    val carteiraPathfinder: Map<String, Int> = emptyMap()
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
    val especializacoesPorPericia: Map<String, EspecializacoesDto>,
    val notasPericia: Map<String, String>? = null
)

@Serializable
data class AdvantageSnapshot(
    val id: String,
    val choice: String? = null
)

@Serializable
data class ComplicacaoSnapshot(
    val id: String,
    val nivel: String?
)

@Serializable
data class SnapshotSelecoes(
    val vantagens: List<AdvantageSnapshot>,
    val vantagensAutomaticas: List<String>,
    val vantagensRaciais: List<String>,
    val desvantagensAutomaticas: List<String>,
    val desvantagensRaciais: List<String>,
    val complicacoesSelecionadas: List<ComplicacaoSnapshot>,
    val transtornos: List<ComplicacaoSnapshot>? = null,
    val reservasComplicacaoMaior: Map<String, Boolean>,
    val poderesSelecionados: List<String>,
    val manifestacoesPoderes: Map<String, String> = emptyMap(),
    val poderSlotsPorArcano: Map<String, List<String?>>, 
    val novosPoderesStacksPorArcano: Map<String, List<List<String>>>,
    val arcanoEmCompraViaXpKey: String?,
    val arcanoSnapshotAntesDaCompra: List<String?>?,
    val equipamentosComprados: List<EquipamentoItem> = emptyList(),
    val coracaoCrystalId: String? = null,
    val tropoSelecionadoId: String? = null,
    val vantagensTropoAutomaticas: List<String> = emptyList(),
    val tecnicasIniciaisTropo: Int = 0,
    val retratoFileName: String? = null,
    val expandirRetrato: Boolean = false,
    val portraitScaleType: String = "CROP",
    val portraitAlignment: String = "CENTER",
    val signoAdgSelecionado: String? = null,
    val artistaMarcialJutsuOpcao: String? = null,
    val artistaMarcialPotencialFisico: String? = null,
    val artistaMarcialTecnicasSelecionadas: List<String> = emptyList(),
    val buXistaCaminhoSelecionado: String? = null,
    val elementalistaElementoSelecionado: String? = null,
    val kuiFerramentaSelecionada: String? = null,
    val samuraiPericiaEscolhida: String? = null,
    val samuraiVantagemEscolhida: String? = null,
    val samuraiPosturasSelecionadas: List<String> = emptyList(),
    val samuraiCombatSlotIds: List<String> = emptyList(),
    val shinobiTalentoSelecionado: String? = null,
    val shinobiTreinamentoSelecionado: String? = null,
    val youxiaJutsuSelecionado: String? = null,
    val youxiaHistoricoSelecionado: String? = null,
    val protagonistaRollTecnicas: Int? = null,
    val protagonistaRollPericia: Int? = null,
    val protagonistaRollVantagem: Int? = null,
    val protagonistaRollQualidade: Int? = null,
    val protagonistaRollHabilidade: Int? = null,
    val protagonistaPericiasEscolhidas: List<String> = emptyList(),
    val protagonistaPericiasPaixao: List<String> = emptyList(),
    val protagonistaSlotAdvantageIds: List<String> = emptyList(),
    val gnomoPericiaEscolhida: String? = null,
    val dominioClerigoSelecionado: String? = null,
    val dominioClerigoPathfinderSelecionado: String? = null
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
    val stageNameForCurrentAdvancementSnapshot: String?
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
    @JsonNames("bonusPararFromPower")
    val bonusApararFromPower: Int,
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
