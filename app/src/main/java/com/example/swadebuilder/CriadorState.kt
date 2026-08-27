package com.example.swadebuilder

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.swadebuilder.model.AdvantageSnapshot
import com.example.swadebuilder.model.AnaoCiberTraitCatalog
import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.ArcaneConfig
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CiberneticoItem
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.ComplicacaoSnapshot
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.CustomAncestryVariant
import com.example.swadebuilder.model.EquipFilter
import com.example.swadebuilder.model.EquipSuperType
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MechaItem
import com.example.swadebuilder.model.ModifierEngine
import com.example.swadebuilder.model.ModifierTarget
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.PersonagemSnapshot
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitEffect
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.SnapshotAtributos
import com.example.swadebuilder.model.SnapshotFlags
import com.example.swadebuilder.model.SnapshotPericias
import com.example.swadebuilder.model.SnapshotProgresso
import com.example.swadebuilder.model.SnapshotRecursos
import com.example.swadebuilder.model.SnapshotSelecoes
import com.example.swadebuilder.model.SnapshotSupers
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.TOTAL_PROGRESS_LIMIT
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.model.dynamicStageCaps
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.ids.ModuleIds
import com.example.swadebuilder.model.ids.PathfinderCurrencyIds
import com.example.swadebuilder.model.listaDeEstagios
import com.example.swadebuilder.model.nivelParaEstagio
import com.example.swadebuilder.model.usecase.AdjustAttributesForAncestryChangeUseCase
import com.example.swadebuilder.model.usecase.ApplyAncestryChangeCoordinatorUseCase
import com.example.swadebuilder.model.usecase.ApplyHumanAncestryTransitionUseCase
import com.example.swadebuilder.model.usecase.RebuildSkillStacksUseCase
import com.example.swadebuilder.model.usecase.RemoveInvalidAdvantagesAfterAncestryChangeUseCase
import com.example.swadebuilder.model.usecase.ResolveActiveAncestryCandidatesUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestryComplicationsSnapshotUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestryRacialPackageUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestrySpecificAdjustmentsUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestryTransitionBootstrapUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestryTransitionContextUseCase
import com.example.swadebuilder.model.usecase.ResolveAncestryVariantUseCase
import com.example.swadebuilder.model.usecase.ResolveGrantedAncestryAdvantagesUseCase
import com.example.swadebuilder.model.usecase.ResolveRacialAutomaticComplicationsUseCase
import com.example.swadebuilder.registry.AncestryVariantRegistry
import com.example.swadebuilder.ui.MainSection
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.debugLog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

enum class TabStyle { ICONES, TEXTO }

class CriadorState {
    private val resolveActiveAncestryCandidatesUseCase = ResolveActiveAncestryCandidatesUseCase()
    private val applyHumanAncestryTransitionUseCase = ApplyHumanAncestryTransitionUseCase()
    private val adjustAttributesForAncestryChangeUseCase = AdjustAttributesForAncestryChangeUseCase()
    private val resolveRacialAutomaticComplicationsUseCase = ResolveRacialAutomaticComplicationsUseCase()
    private val resolveAncestryComplicationsSnapshotUseCase = ResolveAncestryComplicationsSnapshotUseCase(
        resolveRacialAutomaticComplicationsUseCase = resolveRacialAutomaticComplicationsUseCase
    )
    private val removeInvalidAdvantagesAfterAncestryChangeUseCase = RemoveInvalidAdvantagesAfterAncestryChangeUseCase()
    private val resolveAncestrySpecificAdjustmentsUseCase = ResolveAncestrySpecificAdjustmentsUseCase()
    private val resolveAncestryTransitionContextUseCase = ResolveAncestryTransitionContextUseCase()
    private val resolveAncestryTransitionBootstrapUseCase = ResolveAncestryTransitionBootstrapUseCase(
        resolveAncestryTransitionContextUseCase = resolveAncestryTransitionContextUseCase,
        applyHumanAncestryTransitionUseCase = applyHumanAncestryTransitionUseCase
    )
    private val resolveGrantedAncestryAdvantagesUseCase = ResolveGrantedAncestryAdvantagesUseCase()
    private val resolveAncestryRacialPackageUseCase = ResolveAncestryRacialPackageUseCase(
        resolveGrantedAncestryAdvantagesUseCase = resolveGrantedAncestryAdvantagesUseCase,
        resolveAncestrySpecificAdjustmentsUseCase = resolveAncestrySpecificAdjustmentsUseCase
    )
    private val applyAncestryChangeCoordinatorUseCase = ApplyAncestryChangeCoordinatorUseCase(
        resolveAncestryTransitionBootstrapUseCase = resolveAncestryTransitionBootstrapUseCase,
        adjustAttributesForAncestryChangeUseCase = adjustAttributesForAncestryChangeUseCase,
        resolveAncestryRacialPackageUseCase = resolveAncestryRacialPackageUseCase,
        resolveAncestryComplicationsSnapshotUseCase = resolveAncestryComplicationsSnapshotUseCase,
        removeInvalidAdvantagesAfterAncestryChangeUseCase = removeInvalidAdvantagesAfterAncestryChangeUseCase
    )
    private val rebuildSkillStacksUseCase = RebuildSkillStacksUseCase()
    private val validateSelectionUseCase = com.example.swadebuilder.model.usecase.ValidateSelectionUseCase()
    private val resolveAncestryVariantUseCase = ResolveAncestryVariantUseCase()

    // --- Game Data Properties (Replaces Globals) ---
    var listaAtributos by mutableStateOf<List<String>>(emptyList())
    var listaPericias by mutableStateOf<List<Pericia>>(emptyList())
    var listaVantagens by mutableStateOf<List<Vantagem>>(emptyList())
    var listaComplicacoes by mutableStateOf<List<Complicacao>>(emptyList())
    var listaTropos by mutableStateOf<List<Tropo>>(emptyList())
    var listaEquipamentos by mutableStateOf<List<EquipamentoItem>>(emptyList())
    var listaPoderes by mutableStateOf<List<Poder>>(emptyList())
    var listaSuperPoderes by mutableStateOf<List<SuperPoder>>(emptyList())
    var listaAncestralidadesJson by mutableStateOf<List<RacialModifier>>(emptyList())
    var listaVariantesRaciaisCustom by mutableStateOf<List<CustomAncestryVariant>>(emptyList())
    var listaMonstroTemplates by mutableStateOf<List<MonstroTemplate>>(emptyList())
    var listaCoracoesCrystal by mutableStateOf<List<CrystalHeart>>(emptyList())

    var equipamentoCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())
    var superequipCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())

    var mapaAtributosDisplay by mutableStateOf<Map<String, String>>(emptyMap())
    var mapaPericias by mutableStateOf<Map<String, Pericia>>(emptyMap())
    var racialAttrMinMap by mutableStateOf<Map<String, Map<String, Int>>>(emptyMap())
    var racialSkillStartMap by mutableStateOf<Map<String, Map<String, Int>>>(emptyMap())
    var arcanoInfo by mutableStateOf<Map<String, Triple<Int, Int, String>>>(emptyMap())

    // Optimization: Cache ancestry lookup to avoid O(N) filtering on every access.
    // Maps keyify(name) -> List of candidates.
    // Keep as snapshot state so derived computations depending on ancestry definitions
    // (e.g. Adaptável slot availability) are invalidated immediately after game data load.
    private var ancestryMap by mutableStateOf<Map<String, List<RacialModifier>>>(emptyMap())

    // listaDeEstagios is imported from model (static rules)

    fun addCustomVantagem(vant: Vantagem) {
        if (listaVantagens.none { it.id == vant.id }) {
            listaVantagens = listaVantagens + vant
        }
    }

    fun addCustomComplicacao(comp: Complicacao) {
        if (listaComplicacoes.none { it.id == comp.id }) {
            listaComplicacoes = listaComplicacoes + comp
        }
    }

    fun addCustomEquipamento(item: EquipamentoItem) {
        if (listaEquipamentos.none { it.nome == item.nome }) {
            listaEquipamentos = listaEquipamentos + item
        }
    }

    fun addCustomPoder(poder: Poder) {
        if (listaPoderes.none { it.id == poder.id }) {
            listaPoderes = listaPoderes + poder
        }
    }

    fun addCustomSuperPoder(superPoder: SuperPoder) {
        if (listaSuperPoderes.none { it.nome.equals(superPoder.nome, ignoreCase = true) }) {
            listaSuperPoderes = listaSuperPoderes + superPoder
        }
    }

    fun updateGameData(snapshot: GameDataSnapshot) {
        this.listaAtributos = snapshot.listaAtributos
        this.listaPericias = snapshot.listaPericias
        this.listaVantagens = snapshot.listaVantagens
        this.listaComplicacoes = snapshot.listaComplicacoes
        this.listaTropos = snapshot.listaTropos
        this.listaEquipamentos = snapshot.listaEquipamentos
        this.listaPoderes = snapshot.listaPoderes
        this.listaSuperPoderes = snapshot.listaSuperPoderes
        this.listaAncestralidadesJson = snapshot.listaAncestralidadesJson
        this.listaVariantesRaciaisCustom = snapshot.listaVariantesRaciaisCustom
        // Build the cache once when data loads.
        this.ancestryMap = this.listaAncestralidadesJson.groupBy { it.nome.keyify() }
        this.listaMonstroTemplates = snapshot.listaMonstroTemplates
        this.listaCoracoesCrystal = snapshot.listaCoracoesCrystal
        this.equipamentoCategorias = snapshot.equipamentoCategorias
        this.superequipCategorias = snapshot.superequipCategorias
        this.mapaAtributosDisplay = snapshot.mapaAtributosDisplay
        this.mapaPericias = snapshot.mapaPericias
        this.racialAttrMinMap = snapshot.racialAttrMinMap
        this.racialSkillStartMap = snapshot.racialSkillStartMap

        this.arcanoInfo = snapshot.arcanoInfo.associate {
            it.key.uppercase().trim() to Triple(it.slots, it.pp, it.foco)
        }

        ensureAllAtributosRegistered()
        ensureAllPericiasRegistered()

        hasFreeAdaptavelSlotNow(debugSource = "updateGameData")
    }

    private val ameacadorComplicacoesLiberadoras = setOf(
        "sanguinario",
        "desagradavel",
        "sem_escrupulos",
        "feio",
        "sombrio",
        "sinistro"
    ).map { it.keyify() }.toSet()

    private val ameacadorId = "ameacador".keyify()

    private fun atendePreviasPorComplicacaoParaAmeacador(v: Vantagem): Boolean {
        if (v.id.keyify() != ameacadorId) return false

        val requisitadas = v.requisitos.vantagensPrevias.map { it.keyify() }.toSet()
        val liberadoras = (ameacadorComplicacoesLiberadoras + requisitadas)
        val selecionadas = complicacoesSelecionadas.keys.map { it.id.keyify() }.toSet()

        return selecionadas.any { it in liberadoras }
    }

    private fun atendeVantagensPrevias(v: Vantagem): Boolean {
        if (v.requisitos.vantagensPrevias.isEmpty()) return true

        if (atendePreviasPorComplicacaoParaAmeacador(v)) return true

        val faltam = v.requisitos.vantagensPrevias.any { prevId ->
            when (prevId.keyify().replace(" ", "_")) {
                "ANTECEDENTE_ARCANO", "ANTECEDENTE_ARCANO:*" -> {
                    vantagensSelecionadas.none { poss ->
                        poss.id.startsWith("antecedente_arcano_") ||
                                poss.id.startsWith("aa_") ||
                                (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                    }
                }
                else -> {
                    vantagensSelecionadas.none { poss ->
                        poss.id.keyify().replace(" ", "_") == prevId.keyify().replace(" ", "_")
                    }
                }
            }
        }
        return !faltam
    }

    var appTheme by mutableStateOf(AppTheme.DEFAULT)
    var mostrarIdentificadorLivro by mutableStateOf(true)
    var estiloAbas by mutableStateOf(TabStyle.TEXTO)
    var mostrarDescricaoHome by mutableStateOf(true)
    var hapticStrength by mutableIntStateOf(DEFAULT_HAPTIC_STRENGTH)
    var soundVolume by mutableIntStateOf(DEFAULT_SOUND_VOLUME)
    var showSystemMessages by mutableStateOf(true)
    var pularSelecaoRegras by mutableStateOf(false)
    var modoSupers by mutableStateOf(false)
    var compendioFantasiaAtivo by mutableStateOf(false)
    var compendioHorrorAtivo by mutableStateOf(false)
    var compendioSciFiAtivo by mutableStateOf(false)
    var compendioScifiMechasAtivo by mutableStateOf(false)
    var compendioScifiCiberneticosAtivo by mutableStateOf(false)
    var compendioScifiMechasCiberneticosAtivo: Boolean
        get() = compendioScifiMechasAtivo || compendioScifiCiberneticosAtivo
        set(value) {
            compendioScifiMechasAtivo = value
            compendioScifiCiberneticosAtivo = value
        }

    val mechasSelecionados = mutableStateListOf<MechaItem>()
    val ciberneticosInstalados = mutableStateListOf<CiberneticoItem>()
    var compendioPathfinderAtivo by mutableStateOf(false)
    var compendioDeadlandsAtivo by mutableStateOf(false)
    var compendioCrystalHeartAtivo by mutableStateOf(false)
    var compendioArteDaGuerraAtivo by mutableStateOf(false)
    var compendioCidadeSolVaporAtivo by mutableStateOf(false)
    var compendioWiseguysAtivo by mutableStateOf(false)
    var optRegraRiqueza by mutableStateOf(false)
    var optRegraCosaNostra by mutableStateOf(false)
    var optRegraFama by mutableStateOf(false)
    // Regra de livro: mostra as Variantes de raça (reconfiguração de cenário
    // feita pelo mestre, ex.: Anões "Ciber") na seleção de ancestralidade.
    // Desligada por padrão. Seleções (escolhas do próprio jogador dentro da
    // raça, ex.: Umvee "Dom da Natureza") não são afetadas por este toggle —
    // continuam sempre visíveis.
    var optVariantesDeRacaAtivo by mutableStateOf(false)
    var modoOficialAtivo by mutableStateOf(false)
    var modoLivre by mutableStateOf(false)
    var isNpcExibicao by mutableStateOf(false)
    var modoMonstroAtivo by mutableStateOf(false)
    var tipoMonstroSelecionado by mutableStateOf<String?>(null)
    var grandesResponsabilidades by mutableStateOf(false)
    var signoAdgSelecionado by mutableStateOf<String?>(null)
    var pacoteCulturalFantasiaSelecionado by mutableStateOf("Humano padrão")
    var povoDoMarOpcao by mutableStateOf<String?>(null)
    var senhoresCavalosExtra by mutableStateOf(false)
    var senhoresCavalosCompensacao by mutableStateOf<String?>(null)
    var protagonistaRollTecnicas by mutableStateOf<Int?>(null)
    var protagonistaRollPericia by mutableStateOf<Int?>(null)
    var protagonistaRollVantagem by mutableStateOf<Int?>(null)
    var protagonistaRollQualidade by mutableStateOf<Int?>(null)
    var protagonistaRollHabilidade by mutableStateOf<Int?>(null)
    var protagonistaPericiasEscolhidas by mutableStateOf<List<String>>(emptyList())
    var protagonistaPericiasPaixao by mutableStateOf<List<String>>(emptyList())
    var protagonistaBonusPv by mutableStateOf(false)
    var artistaMarcialJutsuOpcao by mutableStateOf(ARTISTA_MARCIAL_JUTSU_D6)
    var artistaMarcialPotencialFisico by mutableStateOf<String?>(null)
    val artistaMarcialTecnicasSelecionadas = mutableStateListOf<String>()
    var buXistaCaminhoSelecionado by mutableStateOf<String?>(null)
    var elementalistaElementoSelecionado by mutableStateOf<String?>(null)
    var kuiFerramentaSelecionada by mutableStateOf<String?>(null)
    var samuraiPericiaEscolhida by mutableStateOf<String?>(null)
    var samuraiVantagemEscolhida by mutableStateOf<String?>(null)
    val samuraiPosturasSelecionadas = mutableStateListOf<String>()
    val samuraiCombatSlotIds = mutableStateListOf<String>()
    var shinobiTalentoSelecionado by mutableStateOf<String?>(null)
    var shinobiTreinamentoSelecionado by mutableStateOf<String?>(null)
    var youxiaJutsuSelecionado by mutableStateOf<String?>(null)
    var youxiaHistoricoSelecionado by mutableStateOf<String?>(null)
    var descendenteElementalSelecionado by mutableStateOf<String?>(null)
    var anoesScifiSelecionado by mutableStateOf<String?>(null)
    var scifiVariant by mutableStateOf<String?>(null)
    var humanoMineradorAtributo by mutableStateOf<String?>(null)
    /** Id de CustomAncestryVariant selecionada pra raça atual (Variante custom, ver Tarefa #18). */
    var customVarianteRacialSelecionadaId by mutableStateOf<String?>(null)
    var anaoCiberTracosSelecionados by mutableStateOf<List<AnaoCiberTraitSelection>>(emptyList())
    var gnomoPericiaEscolhida by mutableStateOf<String?>(null)
    var kitsunemimiPericiaEscolhida by mutableStateOf<String?>(null)
    var usagimimiPericiaEscolhida by mutableStateOf<String?>(null)
    var signoSerpentePericiaEscolhida by mutableStateOf("Jogar")
    var dominioClerigoSelecionado by mutableStateOf<String?>(null)
    var dominioClerigoPathfinderSelecionado by mutableStateOf<String?>(null)

    fun getActiveModuleKeys(): Set<String> {
        if (modoLivre) {
            return buildSet {
                add(ModuleIds.FANTASIA)
                add(ModuleIds.HORROR)
                add(ModuleIds.SCI_FI)
                add(ModuleIds.SUPER)
                add(ModuleIds.PATHFINDER)
                add(ModuleIds.DEADLANDS)
                add(ModuleIds.CRYSTAL_HEART)
                add(ModuleIds.ARTE_DA_GUERRA)
                add(ModuleIds.CIDADE_SOL_VAPOR)
                add(ModuleIds.WISEGUYS)
            }
        }
        val keys = mutableSetOf<String>()
        if (compendioFantasiaAtivo) keys.add(ModuleIds.FANTASIA)
        if (compendioHorrorAtivo) keys.add(ModuleIds.HORROR)
        if (compendioSciFiAtivo) keys.add(ModuleIds.SCI_FI)
        if (modoSupers) keys.add(ModuleIds.SUPER)
        if (compendioPathfinderAtivo) keys.add(ModuleIds.PATHFINDER)
        if (compendioDeadlandsAtivo) keys.add(ModuleIds.DEADLANDS)
        if (compendioCrystalHeartAtivo) keys.add(ModuleIds.CRYSTAL_HEART)
        if (compendioArteDaGuerraAtivo) keys.add(ModuleIds.ARTE_DA_GUERRA)
        if (compendioCidadeSolVaporAtivo) keys.add(ModuleIds.CIDADE_SOL_VAPOR)
        if (compendioWiseguysAtivo) keys.add(ModuleIds.WISEGUYS)
        return keys
    }

    fun getMonstroSelecionado(): MonstroTemplate? {
        if (!modoMonstroAtivo || tipoMonstroSelecionado == null) return null
        return listaMonstroTemplates.firstOrNull { it.id == tipoMonstroSelecionado }
    }

    fun aplicarTipoMonstro(novoId: String?): List<String> {
        val feedback = mutableListOf<String>()

        tipoMonstroSelecionado = novoId

        if (modoMonstroAtivo) {
            val selectedTemplateKey = novoId?.keyify()
            val toRemove = vantagensSelecionadas
                .filter { it.categoria == Categoria.MONSTRUOSAS }
                .filter { it.requisitos.templatesRequired.isNotEmpty() }
                .filter { v ->
                    val required = v.requisitos.templatesRequired.map { it.keyify() }
                    selectedTemplateKey == null || selectedTemplateKey !in required
                }
                .toList()

            toRemove.forEach { vantagem ->
                var refundMessage: String? = null
                venderVantagem(vantagem) { msg -> refundMessage = msg }
                val suffix = refundMessage?.let { " $it" } ?: ""
                feedback.add("Vantagem '${vantagem.nome}' removida automaticamente por incompatibilidade com o tipo de monstro selecionado.$suffix")
            }
        }

        recalcularPontosAtributo(feedback)
        rebuildAllPericiaStacks(feedback)

        if (feedback.isNotEmpty()) {
            anotacoes += "\n• " + feedback.joinToString("\n• ")
        }

        return feedback
    }

    /**
     * Optimized: Memoize the ancestry definition for the currently selected ancestry.
     * This avoids re-running filters and variant application logic (which creates object copies)
     * every time a UI element needs to check racial traits (e.g. for complications, edges, attributes).
     */
    val currentAncestryDef: RacialModifier? by derivedStateOf {
        getAncestralidadeDef(ancestralidade)
    }

    fun getAncestralidadeDef(name: String): RacialModifier? {
        val key = name.keyify()
        val baseNoSuffix = name.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").keyify()
        val lookupKeys = linkedSetOf(key, baseNoSuffix).apply {
            if (baseNoSuffix.endsWith("S")) add(baseNoSuffix.removeSuffix("S")) else if (baseNoSuffix.isNotBlank()) add("${baseNoSuffix}S")
            if (key.endsWith("S")) add(key.removeSuffix("S")) else if (key.isNotBlank()) add("${key}S")
        }

        // Optimized O(1) lookup using cached map (with resilient fallbacks for aliases/suffixes)
        val candidates = lookupKeys.firstNotNullOfOrNull { ancestryMap[it] }
        if (candidates.isNullOrEmpty()) {
            debugLog("AdaptavelDebug", "[getAncestralidadeDef] não encontrada para '$name' keys=$lookupKeys")
            return null
        }

        if (lookupKeys.first() != lookupKeys.firstOrNull { ancestryMap[it] != null }) {
            debugLog("AdaptavelDebug", "[getAncestralidadeDef] fallback de chave para '$name' keys=$lookupKeys")
        }

        // Toda raça de candidato único, EXCETO Umvee, mantém o curto-circuito
        // original: sai aqui sem passar por applyAncestryVariantAdjustments.
        // Umvee precisa passar por ele mesmo tendo um candidato só — o Dom da
        // Natureza "Gatoruja" injeta PERCEBER_D6/OCULTISMO_D4 ali, e cair fora
        // antes disso deixava esse traço de fora (bug real, pego pelo
        // ScifiAncestryVariantSyncTest). Já o Meio-Elfo do Pathfinder (também
        // candidato único) depende do contrário — de sair aqui — pra NÃO entrar
        // no ramo Herança/Adaptável de applyAncestryVariantAdjustments, pensado
        // pra variante Meio-Elfo de outros livros (CriadorStateRacialTraitDrivenAttributesTest).
        if (candidates.size == 1 && !key.contains("UMVEE")) {
            return applyCustomAncestryVariantIfSelected(candidates.first())
        }

        val selected = if (candidates.size == 1) {
            candidates.first()
        } else {
            val ancestryFlags = ResolveActiveAncestryCandidatesUseCase.Flags(
                compendioFantasiaAtivo = compendioFantasiaAtivo,
                compendioHorrorAtivo = compendioHorrorAtivo,
                compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                compendioWiseguysAtivo = compendioWiseguysAtivo,
                compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                compendioSciFiAtivo = compendioSciFiAtivo,
                compendioPathfinderAtivo = compendioPathfinderAtivo
            )

            val activeCandidates = candidates.filter { item ->
                resolveActiveAncestryCandidatesUseCase.isOriginActive(item.origem, ancestryFlags)
            }

            (if (activeCandidates.isEmpty()) {
                candidates.firstOrNull()
            } else {
                activeCandidates.maxByOrNull { getOriginPriority(it.origem) }
            }) ?: return null
        }

        val withVariant = if (selected.origem == "FC" || selected.origem == "SCI_FI" || key.contains("UMVEE")) {
            applyAncestryVariantAdjustments(selected, key)
        } else if ((key.contains("MEIO-ELFOS") || key.contains("MEIO-ELFO")) && !key.contains("PATHFINDER")) {
            applyAncestryVariantAdjustments(selected, key)
        } else if (canonicalOriginKey(selected.origem) == "FANTASIA" && (key.contains("HUMANO") || key == "DESCENDENTE ELEMENTAL" || key == "DESC_ELEMENTAL")) {
            applyAncestryVariantAdjustments(selected, key)
        } else {
            selected
        }

        return applyCustomAncestryVariantIfSelected(withVariant)
    }

    /**
     * Aplica, se houver, a CustomAncestryVariant selecionada pra raça `base`: remove os
     * traços/vantagens-grátis/desvantagens indicados e adiciona os traços bespoke, Vantagens
     * e Complicações escolhidos na criação da Variante. Roda por cima do RacialModifier já
     * resolvido (após applyAncestryVariantAdjustments), então todo o resto do app — que lê
     * habilidades/vantagensGratis/desvantagens de currentAncestryDef — passa a refletir a
     * Variante automaticamente, sem precisar tocar em ResolveAncestrySpecificAdjustmentsUseCase.
     */
    private fun applyCustomAncestryVariantIfSelected(base: RacialModifier): RacialModifier {
        val variantId = customVarianteRacialSelecionadaId ?: return base
        val variant = listaVariantesRaciaisCustom.firstOrNull { it.id == variantId } ?: return base
        if (variant.ancestralidadeId != base.nome.keyify()) return base

        val tracosRemovidosKeys = variant.tracosRemovidosIds.map { it.keyify() }.toSet()
        val newHabilidades = base.habilidades.filterNot { hab ->
            val idKey = hab.id?.keyify()
            idKey != null && idKey in tracosRemovidosKeys
        }.toMutableList()

        val vantagensGratisRemovidasKeys = variant.vantagensGratisRemovidas.map { it.keyify() }.toSet()
        val newVantagensGratis = base.vantagensGratis.filterNot { it.keyify() in vantagensGratisRemovidasKeys }.toMutableList()

        val desvantagensRemovidasKeys = variant.desvantagensRemovidas.map { it.keyify() }.toSet()
        val newDesvantagens = base.desvantagens.filterNot { entry ->
            entry.keyify() in desvantagensRemovidasKeys || entry.substringBefore("(").trim().keyify() in desvantagensRemovidasKeys
        }.toMutableList()

        variant.tracosAdicionados.forEach { trait ->
            newHabilidades.add(
                com.example.swadebuilder.model.RacialAbility(
                    nome = trait.nome,
                    descricao = trait.descricao,
                    id = trait.nome.lowercase().replace(" ", "_"),
                    category = if (trait.custo >= 0) "racial_trait_positive" else "racial_trait_negative"
                )
            )
        }

        variant.vantagensAdicionadasIds.forEach { vantagemId ->
            val grant = listaVantagens.firstOrNull { it.id == vantagemId }?.id ?: vantagemId
            if (newVantagensGratis.none { it.keyify() == grant.keyify() }) {
                newVantagensGratis.add(grant)
            }
        }

        variant.complicacoesAdicionadas.forEach { escolha ->
            val complicacao = listaComplicacoes.firstOrNull { it.id == escolha.complicacaoId } ?: return@forEach
            val severidade = if (escolha.comoMaior) "Maior" else "Menor"
            val entry = "${complicacao.name} ($severidade)"
            if (newDesvantagens.none { it.keyify() == entry.keyify() }) {
                newDesvantagens.add(entry)
            }
        }

        return base.copy(
            habilidades = newHabilidades,
            vantagensGratis = newVantagensGratis,
            desvantagens = newDesvantagens
        )
    }

    private fun applyAncestryVariantAdjustments(base: RacialModifier, key: String): RacialModifier {
        if (canonicalOriginKey(base.origem) == "FANTASIA" && key.contains("HUMANO")) {
            if (pacoteCulturalFantasiaSelecionado != "Humano padrão") {
                val newHabilidades = base.habilidades.toMutableList()

                newHabilidades.removeAll {
                    val idKey = (it.id ?: "").keyify()
                    val nameKey = it.nome.keyify()
                    idKey == "ADAPTAVEL" || nameKey == "ADAPTAVEL"
                }

                val newVantagensGratis = base.vantagensGratis.filter {
                    it.keyify() != "ADAPTAVEL"
                }

                when (pacoteCulturalFantasiaSelecionado) {
                    "Nômades do Deserto" -> newHabilidades.add(com.example.swadebuilder.model.RacialAbility(nome = "Fraqueza Ambiental (Frio)", descricao = "Nômades do deserto possuem fraqueza ambiental ao frio.", id = "FRAQUEZA_AMBIENTAL", category = "racial_trait_negative"))
                    "Povo da Montanha" -> newHabilidades.add(com.example.swadebuilder.model.RacialAbility(nome = "Fraqueza Ambiental (Calor)", descricao = "O povo da montanha possui fraqueza ambiental ao calor.", id = "FRAQUEZA_AMBIENTAL", category = "racial_trait_negative"))
                    "Povo do Mar" -> {
                        if (povoDoMarOpcao == "Penalidade em Cavalgar") {
                            newHabilidades.add(com.example.swadebuilder.model.RacialAbility(nome = "Penalidade em Cavalgar", descricao = "Subtrai 1 de rolagens de Cavalgar.", id = "PENALIDADE_CAVALGAR", category = "racial_trait_negative"))
                        }
                    }
                }

                return base.copy(habilidades = newHabilidades, vantagensGratis = newVantagensGratis)
            }
            return base
        }

        if ((key.contains("MEIO-ELFOS") || key.contains("MEIO-ELFO")) && !key.contains("PATHFINDER")) {
            val newHabilidades = base.habilidades.toMutableList()
            newHabilidades.removeAll { it.id == "HERANCA" || it.nome.keyify() == "HERANCA" }

            // Ensure explicit removal from legacy list
            val newVantagensGratis = base.vantagensGratis.filter { it.keyify() != "HERANCA" }

            if (meioElfoAgil) {
                if (newHabilidades.none { it.id == "AGIL" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Ágil",
                            descricao = "Meio-elfos ágeis começam com d6 em Agilidade em vez de d4. Isso aumenta a Agilidade máxima para d12+1.",
                            id = "AGIL",
                            category = "racial_trait_positive"
                        )
                    )
                }
            } else {
                if (newHabilidades.none { it.id == "ADAPTAVEL" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Adaptável",
                            descricao = "Meio-elfos adaptáveis começam com uma Vantagem de Estágio Novato à sua escolha (os requisitos da Vantagem devem ser atendidos normalmente).",
                            id = "ADAPTAVEL",
                            category = "racial_trait_positive"
                        )
                    )
                }
            }
            return base.copy(habilidades = newHabilidades, vantagensGratis = newVantagensGratis)
        }

        val variant = resolveSciFiVariantSelectionFor(base.nome, base.opcoes) ?: return base
        val newHabilidades = base.habilidades.toMutableList()

        fun removeByIdOrName(id: String, nameKey: String) {
            newHabilidades.removeAll {
                it.id == id || it.nome.keyify() == nameKey.keyify()
            }
        }

        // Insetoides "Vespa" variant: "ARMADURA" is not in JSON base (injected via UseCase for Padrão), so no need to remove here.
        // Mineradores "Zero G" variant: "EM FORMA" retained per feedback.
        // Sáurios "Cuspidor" variant: "MORDIDA" is not in JSON base (injected via UseCase for Padrão), so no need to remove here.

        if (key == "DESCENDENTE ELEMENTAL" || key == "DESC_ELEMENTAL") {
            // Always remove generic resistance, as it will be replaced by specific one from selection
            removeByIdOrName("RESISTENCIA_AMBIENTAL", "RESISTÊNCIA AMBIENTAL")

            when (descendenteElementalSelecionado) {
                "Água" -> {
                    removeByIdOrName("AR_INTERNO", "AR INTERNO")
                    removeByIdOrName("rapido", "RÁPIDO")
                    removeByIdOrName("SOLIDO_COMO_ROCHA", "SÓLIDO COMO ROCHA")
                }
                "Ar" -> {
                    removeByIdOrName("AQUATICO", "AQUÁTICO")
                    removeByIdOrName("rapido", "RÁPIDO")
                    removeByIdOrName("SOLIDO_COMO_ROCHA", "SÓLIDO COMO ROCHA")
                }
                "Fogo" -> {
                    removeByIdOrName("AQUATICO", "AQUÁTICO")
                    removeByIdOrName("AR_INTERNO", "AR INTERNO")
                    removeByIdOrName("SOLIDO_COMO_ROCHA", "SÓLIDO COMO ROCHA")
                }
                "Terra" -> {
                    removeByIdOrName("AQUATICO", "AQUÁTICO")
                    removeByIdOrName("AR_INTERNO", "AR INTERNO")
                    removeByIdOrName("rapido", "RÁPIDO")
                }
                // If null (not selected yet), arguably show all or none. Showing all lets user see options.
                // But removing generic resistance avoids duplication if logic adds it elsewhere?
                // Logic in selecionarDescendenteElemental adds specific one. If none selected, none added.
                // So removing generic here is correct if we want to enforce selection.
                // But if selection is null, we show filtered list (all - generic).
            }
        }

        if (key == "QUADROIDES" && variant == "Habilidoso") {
            newHabilidades.removeAll {
                it.id == "ACAO_ADICIONAL" || it.nome.keyify().contains("ACAO ADICIONAL")
            }
        }

        if (canonicalOriginKey(base.origem) == "ARTE_DA_GUERRA" && key.contains("UMVEE")) {
            removeByIdOrName("DONS_DA_NATUREZA", "DONS DA NATUREZA")

            when (variant) {
                "Ápice" -> if (newHabilidades.none { it.id == "GARRAS" || it.nome.keyify() == "GARRAS" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Garras",
                            descricao = "Ápice concede garras naturais que causam For+d4 de dano.",
                            id = "GARRAS",
                            category = "racial_trait_positive"
                        )
                    )
                }
                "Vínculo Bestial" -> if (newHabilidades.none { it.id == "SENHOR_DAS_FERAS" || it.nome.keyify() == "SENHOR DAS FERAS" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Senhor das Feras",
                            descricao = "Vínculo Bestial concede a vantagem Senhor das Feras.",
                            id = "SENHOR_DAS_FERAS",
                            category = "racial_edge"
                        )
                    )
                }
                "Pele Iluminada pela Lua" -> if (newHabilidades.none { it.nome.keyify() == "APARAR +1" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Aparar +1",
                            descricao = "Pele iluminada pela lua concede +1 de Aparar.",
                            id = "APARAR_1",
                            category = "racial_trait_positive"
                        )
                    )
                }
                "Gatoruja" -> {
                    if (newHabilidades.none { it.id == "VISAO_NO_ESCURO" || it.nome.keyify() == "VISAO NO ESCURO" }) {
                        newHabilidades.add(
                            com.example.swadebuilder.model.RacialAbility(
                                nome = "Visão no Escuro",
                                descricao = "Gatoruja concede visão no escuro.",
                                id = "VISAO_NO_ESCURO",
                                category = "racial_trait_positive"
                            )
                        )
                    }
                    if (newHabilidades.none { it.nome.keyify() == "PERCEBER D6" }) {
                        newHabilidades.add(
                            com.example.swadebuilder.model.RacialAbility(
                                nome = "Perceber d6",
                                descricao = "Gatoruja aumenta o valor inicial de Perceber para d6 e seu máximo para d12+1.",
                                id = "PERCEBER_D6",
                                category = "racial_trait_positive"
                            )
                        )
                    }
                    if (newHabilidades.none { it.id == "OCULTISMO_D4" }) {
                        newHabilidades.add(
                            com.example.swadebuilder.model.RacialAbility(
                                nome = "Ocultismo d4",
                                descricao = "Gatoruja aumenta o valor inicial de Ocultismo para d4.",
                                id = "OCULTISMO_D4",
                                category = "racial_trait_positive"
                            )
                        )
                    }
                }
                "Correnteza" -> if (newHabilidades.none { it.nome.keyify() == "MOVIMENTACAO +2" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "MOVIMENTAÇÃO +2",
                            descricao = "Correnteza concede +2 em Movimentação.",
                            id = "MOVIMENTACAO_2",
                            category = "racial_trait_positive"
                        )
                    )
                }
                "Pedregoso" -> {
                    if (newHabilidades.none { it.nome.keyify() == "RESISTENCIA +1" }) {
                        newHabilidades.add(
                            com.example.swadebuilder.model.RacialAbility(
                                nome = "Resistência +1",
                                descricao = "Pedregoso concede +1 de Resistência.",
                                id = "RESISTENCIA",
                                category = "racial_trait_positive"
                            )
                        )
                    }
                    if (newHabilidades.none { it.nome.keyify() == "ARMADURA +2" }) {
                        newHabilidades.add(
                            com.example.swadebuilder.model.RacialAbility(
                                nome = "Armadura +2",
                                descricao = "Pedregoso concede +2 de Armadura.",
                                id = "ARMADURA",
                                category = "racial_trait_positive"
                            )
                        )
                    }
                }
            }
        }

        if (key == "AQUARIANOS" && variant.equals("Semi-aquáticos", ignoreCase = true)) {
            removeByIdOrName("AQUATICO", "AQUATICO")
            removeByIdOrName("RESISTENCIA", "RESISTENCIA")

            if (newHabilidades.none { it.id == "SEMIAQUATICO" || it.nome.keyify() == "SEMIAQUATICO" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Semiaquático",
                        descricao = "Podem respirar na água e no ar. Seus deslocamentos na água usam a Movimentação normal.",
                        id = "SEMIAQUATICO",
                        category = "racial_trait_positive"
                    )
                )
            }

            if (newHabilidades.none { it.id == "TOQUE_VENENOSO" || it.nome.keyify() == "TOQUE VENENOSO" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Toque Venenoso",
                        descricao = "Possuem secreções urticantes ou venenosas para contato próximo.",
                        id = "TOQUE_VENENOSO",
                        category = "racial_trait_positive"
                    )
                )
            }


        }

        if (key == "ELFOS" && variant.equals("Comunitário", ignoreCase = true)) {
            removeByIdOrName("DESASTRADO", "DESASTRADO")

            if (newHabilidades.none { it.id == "COMUNITARIO" || it.nome.keyify() == "COMUNITARIO" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Comunitário",
                        descricao = "Elfos comunitários recebem +2 em rolagens de Espírito quando outro elfo estiver a até 12 quadros (24m).",
                        id = "COMUNITARIO",
                        category = "racial_trait_positive"
                    )
                )
            }
        }

        if (key == "RAKASHANOS" && variant.equals("Brincalhão", ignoreCase = true)) {
            removeByIdOrName("SANGUINARIO", "SANGUINARIO")
            if (newHabilidades.none { it.id == "CURIOSO" || it.nome.keyify() == "CURIOSO" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Curioso",
                        descricao = "Sua natureza exploratória e brincalhona faz com que se metam onde não são chamados.",
                        id = "CURIOSO",
                        category = "racial_hindrance",
                        severity = "Maior"
                    )
                )
            }
        }

        if (key == "AVIANOS" && variant.equals("Ave de rapina", ignoreCase = true)) {
            removeByIdOrName("FRAGIL", "FRAGIL")
            removeByIdOrName("NAO_SABE_NADAR", "NAO SABE NADAR")

            if (newHabilidades.none { it.id == "HABITANTE_DE_GRAVIDADE_BAIXA" || it.nome.keyify() == "HABITANTE DE GRAVIDADE BAIXA" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Habitante de Gravidade Baixa",
                        descricao = "Corpos adaptados à baixa gravidade sofrem em gravidade padrão ou alta. Subtraia 1 das rolagens de Característica em ambientes de gravidade padrão ou maior sem equipamento apropriado.",
                        id = "HABITANTE_DE_GRAVIDADE_BAIXA",
                        category = "racial_trait_negative"
                    )
                )
            }

            if (newHabilidades.none { it.id == "FORMA_ALIENIGENA" || it.nome.keyify() == "FORMA ALIENIGENA" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "Forma Alienígena",
                        descricao = "O tamanho e a forma destes seres são incompatíveis com a maioria dos equipamentos e veículos usados no cenário. Só podem usar armaduras personalizadas e subtraem 1 das rolagens de Característica ao usar equipamentos e veículos não personalizados. Os itens podem ser personalizados para funcionar para a personagem por 100% do custo base (a critério do Mestre). Se a criatura também for Grande (veja Savage Worlds Edição Aventura), use apenas essa habilidade.",
                        id = "FORMA_ALIENIGENA",
                        category = "racial_trait_negative"
                    )
                )
            }
        }

        if (key == "CENTAUX" && variant.equals("Gazela", ignoreCase = true)) {
            removeByIdOrName("GRANDE", "GRANDE")
            removeByIdOrName("TAMANHO_MAIS_2", "TAMANHO +2")
            removeByIdOrName("MOVIMENTACAO_2", "MOVIMENTACAO +2")

            if (newHabilidades.none { it.nome.keyify() == "MOVIMENTACAO +4" }) {
                newHabilidades.add(
                    com.example.swadebuilder.model.RacialAbility(
                        nome = "MOVIMENTAÇÃO +4",
                        descricao = "Gazelas são extremamente rápidas. +4 em Movimentação e d10 no dado de corrida.",
                        id = "MOVIMENTACAO",
                        category = "racial_trait_positive"
                    )
                )
            }
        }

        if (key == "SERES SINTETICOS" || key == "SERES_SINTETICOS") {
            if (variant.equals("Máquina (Procurado)", ignoreCase = true)) {
                removeByIdOrName("PROGRAMADO", "PROGRAMADO")
                if (newHabilidades.none { it.id == "PROCURADO" || it.nome.keyify().contains("PROCURADO") }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Procurado (Maior)",
                            descricao = "A personagem é procurada pelas autoridades ou por uma facção poderosa.",
                            id = "PROCURADO",
                            category = "racial_hindrance",
                            severity = "Maior"
                        )
                    )
                }
            } else if (variant.equals("Máquina (Forasteiro)", ignoreCase = true)) {
                removeByIdOrName("PROGRAMADO", "PROGRAMADO")
                if (newHabilidades.none { it.id == "FORASTEIRO" || it.nome.keyify().contains("FORASTEIRO") }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Forasteiro (Maior)",
                            descricao = "A personagem não tem direitos ou é perseguida em quase toda parte.",
                            id = "FORASTEIRO",
                            category = "racial_hindrance",
                            severity = "Maior"
                        )
                    )
                }
            }
        }

        if (key.contains("SOLDADOS GENETICOS") || key.contains("SOLDADO GENETICO")) {
            if (variant.equals("Fuzileiro Zero G", ignoreCase = true)) {
                removeByIdOrName("NERVOS_DE_ACO", "NERVOS DE AÇO")
                if (newHabilidades.none { it.id == "adaptacao_gravitacional" || it.nome.keyify() == "ADAPTACAO GRAVITACIONAL" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Adaptação Gravitacional",
                            descricao = "Ignora a penalidade de -2 para Agilidade e perícias baseadas em Agilidade ao agir em gravidade diferente da sua.",
                            id = "adaptacao_gravitacional",
                            category = "racial_edge"
                        )
                    )
                }
            }
        }

        // Humanos (Sci-Fi): as variantes não têm traço estruturado no JSON base
        // (só o texto livre "variantes"), então o bônus de atributo virava hardcode
        // de nome de raça em atributoBaseRacial(). Injeta o traço aqui para que o
        // cálculo de atributo possa ler o traço em vez do nome.
        if (key.contains("HUMANO")) {
            if (variant.equals("Baixa Gravidade", ignoreCase = true)) {
                if (newHabilidades.none { it.id == "BAIXA_GRAVIDADE_AGIL" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Habitante de Gravidade Baixa",
                            descricao = "Habitantes de estações espaciais começam com d6 em Agilidade em vez de d4. Isso aumenta o máximo de Agilidade para d12+1.",
                            id = "BAIXA_GRAVIDADE_AGIL",
                            category = "racial_trait_positive"
                        )
                    )
                }
            } else if (variant.equals("Minerador", ignoreCase = true)) {
                if (newHabilidades.none { it.id == "MINERADOR_ATRIBUTO" }) {
                    newHabilidades.add(
                        com.example.swadebuilder.model.RacialAbility(
                            nome = "Planeta de Mineração",
                            descricao = "Habitantes de planetas de mineração começam com d6 em Força ou Vigor (à escolha) em vez de d4. Isso aumenta o máximo do atributo escolhido para d12+1.",
                            id = "MINERADOR_ATRIBUTO",
                            category = "racial_trait_positive"
                        )
                    )
                }
            }
        }

        // Mineradores Genéticos "Zero G": a variante Padrão tem "FORTE" (Força d6)
        // fixo no JSON; a variante Zero G não deveria ter esse bônus. Remove aqui
        // em vez de resetar a base em Kotlin por nome de raça.
        if (key.contains("MINERADOR") && key.contains("GENETICO") && variant.equals("Zero G", ignoreCase = true)) {
            removeByIdOrName("FORTE", "FORTE")
        }

        // Ferais (Sci-Fi): "ESPIRITUOSO" (Espírito d6) é fixo no JSON da raça, mas
        // só vale para a variante Padrão.
        if (key == "FERAIS" && variant.equals("Menor", ignoreCase = true)) {
            removeByIdOrName("ESPIRITUOSO", "ESPIRITUOSO")
        }

        return base.copy(habilidades = newHabilidades)
    }


    fun resolveSciFiVariantSelectionFor(
        ancestryName: String,
        availableOptions: List<String>,
        overrideSelection: String? = null
    ): String? {
        if (availableOptions.isEmpty()) return null
        val selected = overrideSelection ?: scifiVariant
        // Mesmo fallback usado por ResolveAncestrySpecificAdjustmentsUseCase.execute()
        // (que recebe anoesScifiSelecionado como legacySelectedVariant) — antes esta
        // função sempre passava null aqui, então os dois cálculos da "variante
        // efetiva" podiam divergir se scifiVariant e anoesScifiSelecionado nunca
        // estivessem 100% sincronizados (ex.: logo após restaurar um save).
        // Para raças que não são Anões, anoesScifiSelecionado normalmente é null,
        // então isso não muda nada além do caso relevante.
        val legacySelection: String? = anoesScifiSelecionado
        return resolveAncestryVariantUseCase.execute(
            ResolveAncestryVariantUseCase.Input(
                selectedVariant = selected,
                legacySelectedVariant = legacySelection,
                availableOptions = availableOptions
            )
        ).normalizedSelection
    }

    private fun resolveCurrentSciFiVariantSelection(ancestryName: String = ancestralidade): String? {
        val ancDef = if (ancestryName == ancestralidade) currentAncestryDef else getAncestralidadeDef(ancestryName)
        if (ancDef == null) return scifiVariant
        return resolveSciFiVariantSelectionFor(
            ancestryName = ancestryName,
            availableOptions = ancDef.opcoes
        )
    }

    private fun withBaselineCounterpartMechanics(
        selected: RacialModifier,
        ancestryKey: String
    ): RacialModifier {
        val selectedOrigin = canonicalOriginKey(selected.origem)
        val shouldNormalize = selectedOrigin in setOf("FANTASIA", "HORROR", "SCIFI", "SCI_FI", "FC", "SUPER")
        if (!shouldNormalize) return selected

        val baseline = listaAncestralidadesJson.firstOrNull {
            canonicalOriginKey(it.origem) == "BASICO" && it.nome.keyify() == ancestryKey
        } ?: return selected

        return selected.copy(
            atributos = baseline.atributos,
            pericias = baseline.pericias,
            vantagensGratis = baseline.vantagensGratis,
            desvantagens = baseline.desvantagens,
            habilidades = baseline.habilidades
        )
    }

    fun isAttributeRankLimitReached(): Boolean {
        val stageIndex = currentProgressStageIndex()
        val lendarioIndex = listaDeEstagios.indexOfFirst { it.nome.equals("Lendário", ignoreCase = true) }
            .takeIf { it >= 0 } ?: listaDeEstagios.lastIndex
        val totalAttrPurchases = comprasAttrPorEstagio.values.sum()
        val baseAllowance = (stageIndex + 1).coerceAtMost(lendarioIndex)
        val remainingBaseAttrs = (baseAllowance - totalAttrPurchases).coerceAtLeast(0)
        return remainingBaseAttrs <= 0
    }

    fun isAttributeFreeForMonster(attr: String): Boolean {
        if (!modoMonstroAtivo) return false
        val key = attr.keyify()
        // Antes: lista fixa (Agilidade/Força/Vigor) que só batia com os templates
        // Lobisomem/Monstro de Retalhos/Múmia/Vampiro. Anjo, Demônio, Fantasma e
        // Revivido bonificam Espírito (ou nem tocam os 3 atributos da lista), e
        // ficavam sem o benefício — ou ganhavam à toa em atributos que o
        // template escolhido nem bonifica. Agora deriva do template selecionado.
        val validAttrKeys = setOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        if (key !in validAttrKeys) return false
        val monstro = getMonstroSelecionado() ?: return false
        return monstro.atributos_bonus.keys.any { it.keyify() == key }
    }

    val vantagensAutomaticasDoSigno = mutableStateListOf<String>()
    val vantagensAutomaticasDoElemento = mutableStateListOf<String>()
    val habilidadesRaciaisDoElemento = mutableStateListOf<String>()
    val vantagensAutomaticasDoPotencialFisico = mutableStateListOf<String>()

    val fixedPowersByArcano = mapOf(
        "ABENCOADO" to listOf("simbolo_sagrado"),
        "MESTRE DO CHI" to listOf("deflexao"),
        "BARDO" to listOf("aumentar_reduzir_caracteristica", "som_silencio"),
        "CLERIGO" to listOf("cura", "santuario"),
        "DEMONIO" to listOf("disfarce_demoniaco"),
        "DIABOLISTA" to listOf("banir", "devastacao", "conjurar_aliado"),
        "DRUIDA" to listOf("amigo_das_feras", "protecao_ambiental", "mudanca_de_forma"),
        "ELEMENTALISTA" to listOf("manipulacao_elemental", "protecao_ambiental"),
        "ILUSIONISTA" to listOf("ilusao", "iluminar_obscurecer", "som_silencio"),
        "INVOCADOR" to listOf("amigo_das_feras", "aumentar_reduzir_caracteristica", "conjurar_aliado"),
        "MAGO" to listOf("detectar_ocultar_arcano", "dissipar", "trancar_destrancar"),
        "NECROMANTE" to listOf("detectar_ocultar_arcano", "dissipar", "zumbi"),
        "XAMA" to listOf("protecao_arcana", "ajuda"),
        "MISTICO_BARBARO" to listOf("aumentar_reduzir_caracteristica", "ferir", "morosidade_velocidade"),
        "MISTICO_GUERREIRO" to listOf("aumentar_reduzir_caracteristica", "ferir", "protecao"),
        "MISTICO_LADRAO" to listOf("andar_nas_paredes", "aumentar_reduzir_caracteristica", "trancar_destrancar", "visao_sombria"),
        "MISTICO_MONGE" to listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir"),
        "MISTICO_PALADINO" to listOf("aumentar_reduzir_caracteristica", "cura", "ferir", "protecao", "santuario"),
        "MISTICO_PATRULHEIRO" to listOf("amigo_das_feras", "aumentar_reduzir_caracteristica", "enredar", "visao_distante"),
        "MISTICO_ARAUTO" to listOf("adivinhacao", "aumentar_reduzir_caracteristica", "cura", "videncia"),
        "MISTICO_MORTE" to listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir", "protecao"),
        "MISTICO_INVOCADOR" to listOf("conjurar_aliado", "conjurar_demonio", "protecao", "zumbi"),
        "MISTICO_POSSESSOR" to listOf("aumentar_reduzir_caracteristica", "fantoche", "maldicao", "pesadelos"),
        "MISTICO_SEDUTOR" to listOf("aumentar_reduzir_caracteristica", "disfarce", "empatia", "leitura_de_mente"),
        "MISTICO_TRAPACEIRO" to listOf("disfarce", "deflexao", "horrores_ilusorios", "medo"),
        "MISTICO_ARQUITETO" to listOf("barreira", "detectar_ocultar_arcano", "telecinese", "trancar_destrancar"),
        "MISTICO_REGIO" to listOf("explosao", "rajada", "rancor"),
        "MISTICO_GUERREIRO_ESTELAR" to listOf("aumentar_reduzir_caracteristica", "deflexao", "devastacao", "protecao", "telecinese"),
        "MISTICO_TELEPATA" to listOf("ajuda", "atordoar", "confusao", "empatia", "leitura_de_mente"),
        // Pathfinder specifics
        "MISTICO_MONGE_PF" to listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir", "morosidade_velocidade"),
        "MISTICO_MONGE_GRANDE_KI_PF" to listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir", "morosidade_velocidade", "andar_nas_paredes", "dadiva_do_guerreiro", "protecao"),
        "MISTICO_PALADINO_PF" to listOf("ajuda", "aumentar_reduzir_caracteristica", "cura", "ferir"),
        "MISTICO_PATRULHEIRO_PF" to listOf("amigo_das_feras", "aumentar_reduzir_caracteristica", "enredar", "dadiva_do_guerreiro"),
        "MISTICO_FORCA_SOMBRIA_PF" to listOf("rajada", "ilusao", "conjurar_aliado", "teleporte")
    )

    private fun getEffectiveKeyForMistico(vant: Vantagem): String {
        if (!vant.choice.isNullOrBlank()) {
            return "MISTICO_${vant.choice!!.normAAKey()}"
        }
        val n = vant.nome.normAAKey()
        if (compendioPathfinderAtivo) {
            if ("MONGE" in n) {
                val hasGrandeKi = vantagensSelecionadas.any { it.id == "grande_ki" }
                return if (hasGrandeKi) "MISTICO_MONGE_GRANDE_KI_PF" else "MISTICO_MONGE_PF"
            }
            if ("PALADINO" in n) return "MISTICO_PALADINO_PF"
            if ("PATRULHEIRO" in n) return "MISTICO_PATRULHEIRO_PF"
            if ("FORCA SOMBRIA" in n || "DANCARINO DAS SOMBRAS" in n) return "MISTICO_FORCA_SOMBRIA_PF"
        }
        return "MISTICO"
    }

    fun isFixedPower(arcanoKey: String, powerId: String?): Boolean {
        if (powerId == null) return false
        val arcKeyNorm = arcanoKey.normAAKey()
        if (
            ancestralidade.keyify() == "TRANSMORFOS" &&
            arcKeyNorm == "DOM" &&
            powerId.keyify() == "DISFARCE"
        ) {
            return true
        }

        val effectiveKey = if (arcKeyNorm == "MISTICO") {
            val vant = vantagensSelecionadas.find { it.toArcanoKey()?.normAAKey() == "MISTICO" }
            if (vant != null) getEffectiveKeyForMistico(vant) else arcKeyNorm
        } else {
            arcKeyNorm
        }

        val fixedList = fixedPowersByArcano[effectiveKey] ?: return false
        return fixedList.contains(powerId)
    }

    companion object {
        fun getOriginPriority(origin: String?): Int {
            val o = canonicalOriginKey(origin)
            return when {
                o == "HORROR" -> 1000
                o == "FANTASIA" -> 900
                o == "ARTE_DA_GUERRA" -> 800
                o == "DEADLANDS" -> 800
                o == "WISEGUYS" -> 800
                o == "CIDADE_SOL_VAPOR" -> 800
                o.contains("TRILHADOR") || o.contains("PATHFINDER") -> 800
                o == "FC" || o == "SCIFI" || o == "SCI_FI" -> 800
                o == "CRYSTAL_HEART" -> 800
                o == "BASICO" -> 0
                else -> 100
            }
        }

        const val BASE_SP_POOL = 15
        const val DEFAULT_HAPTIC_STRENGTH = 70
        const val DEFAULT_SOUND_VOLUME = 70
        const val ARTISTA_MARCIAL_JUTSU_D6 = "D6"
        const val ARTISTA_MARCIAL_JUTSU_D4_D4 = "D4_D4"
        val SIGNOS_ADG = listOf(
            "Nenhum", "Basabasa", "Boi", "Tigre", "Lebre", "Garça", "Serpente",
            "Dragão", "Kirin", "Macaco", "Raposa", "Lobo", "Tartaruga", "Urso"
        )
        val PACOTES_CULTURAIS_FANTASIA = listOf(
            "Humano padrão",
            "Nômades do Deserto",
            "Povo da Montanha",
            "Povo do Mar",
            "Senhores dos Cavalos"
        )
        val PACOTES_CULTURAIS_FANTASIA_DESC = mapOf(
            "Humano padrão" to "Mantém o pacote padrão de humanos de Fantasia: Adaptável (uma Vantagem Novato à escolha).",
            "Nômades do Deserto" to "Começam com d6 em Sobrevivência e Resistência Ambiental (Calor). Também possuem Fraqueza Ambiental (Frio).",
            "Povo da Montanha" to "Começam com Vigor d6 e Resistência Ambiental (Frio). Também possuem Fraqueza Ambiental (Calor).",
            "Povo do Mar" to "Começam com d6 em Atletismo e Navegar. Em algumas campanhas, podem ter penalidade em Cavalgar ou Procurado (Maior), a critério do Mestre.",
            "Senhores dos Cavalos" to "Começam com d6 em Cavalgar. Alguns grupos também concedem Nascido na Sela e/ou complicações culturais como Código de Honra, Sem Escrúpulos e Analfabeto, a critério do Mestre."
        )
        // Resumos genéricos para a edição Lite (não reproduzem o texto do livro original).
        val PACOTES_CULTURAIS_FANTASIA_DESC_LITE = mapOf(
            "Humano padrão" to "Segue o pacote humano genérico: recebe uma Vantagem de Novato à sua escolha.",
            "Nômades do Deserto" to "Iniciam com Sobrevivência d6 e resistência a ambientes quentes, mas sofrem penalidade em climas frios.",
            "Povo da Montanha" to "Vigor inicial d6 e tolerância ao frio, compensados por uma fraqueza a ambientes quentes.",
            "Povo do Mar" to "Atletismo e Navegar iniciam em d6; dependendo da campanha, o Mestre pode aplicar penalidade em Cavalgar ou a Complicação Procurado (Maior).",
            "Senhores dos Cavalos" to "Cavalgar inicial d6; a critério do Mestre, o grupo pode ainda conceder Nascido na Sela ou complicações culturais como Código de Honra, Sem Escrúpulos ou Analfabeto."
        )
        val SIGNOS_ADG_DESC = mapOf(
            "Nenhum" to "Sem signo de nascença. Você mantém os benefícios de Humano Adaptável (15 pontos de perícia e slot gratuito de Adaptável).",
            "Basabasa" to "Aqueles que nasceram no primeiro mês sob o signo de Basabasa geralmente são indivíduos honestos e ambiciosos, conhecidos por uma beleza sobrenatural. Tal alinhamento celestial é ofuscado por uma oscilação de humores excêntricos. Começam as coisas com entusiasmo e logo perdem o interesse, tornando-se voláteis. Um Basabasa tem a Vantagem Atraente e escolhe na criação do personagem entre adicionar +1 às rolagens de Provocar ou Intimidar contra alvos que se sintam atraídos ou desprezem o Herói.",
            "Boi" to "Aqueles que nasceram sob o signo do Boi são grandes e imponentes, conhecidos por serem diretos e persistentes. Falhas comuns incluem teimosia, franqueza excessiva e inabilidade em expressar emoções. Um Boi recebe +1 em rolagens em Atletismo quando utilizado em situações que podem exigir Força (como escalar ou nadar). Caso o personagem possua a Vantagem Brutamontes, esse benefício se aplica a todas as rolagens de Atletismo. Além disso, este benefício aumenta a Força em um tipo de dado e aumenta seu limite máximo no atributo em d12+1.",
            "Tigre" to "A herança do signo do Tigre faz com que se tornem destemidos e precisos, realizando atos cavalheirescos dignos de respeito enquanto assumem a liderança. Tigres são naturalmente temperamentais. Em um papel de liderança ou posição de autoridade, tomarão decisões para obter o melhor resultado possível, sem considerar o efeito sobre os outros. Um Tigre tem um alcance de comando de +4 quadros, adiciona +1 nas rolagens de Medo e subtrai 1 dos resultados da Tabela de Medo (isso acumula com a Vantagem Corajoso).",
            "Lebre" to "Heróis nascidos sob o signo da Lebre exibem qualidades gentis, amáveis e compassivas, com um toque de modéstia. Lebres podem demonstrar características comportamentais de sonhar acordado, escapismo, falta de perspectiva ou timidez em interações sociais. Uma Lebre tem um toque natural (Cura d6) e, com os suprimentos médicos adequados, pode gastar um Bene para tratar um Ferimento com horas ou dias (até 4 dias), como se estivesse sendo tratada dentro da Hora de Ouro. Um personagem pode se beneficiar desse tratamento uma única vez por aventura.",
            "Garça" to "Nascidos com o signo da Garça, os indivíduos buscam uma vida de perfeito equilíbrio entre os altos e baixos que ela oferece. Uma Garça é agraciada com graça em seus movimentos e se destaca contra adversários em todas as formas. A Garça possui a fraqueza da insegurança e depende muito dos outros em momentos de dúvida. As garças recebem +1 em Aparar, d4 em Acrobacia e aumentam Atletismo em um tipo de dado.",
            "Serpente" to "Muitos veem a serpente como astuta e sorrateira, no entanto, o signo da Serpente é um símbolo de sabedoria e mantém um alto nível de astúcia. Serpentes são consideradas sensíveis e emotivas, a maioria é talentosa nas artes. Com essa sensibilidade vem a hesitação e pequenos surtos de leve paranoia. Uma Serpente começa com Jogar d6 ou Performance d6. Usando Jogar, uma Serpente adiciona +1 ao total da diferença se vencer e -1 ao total da diferença se perder. Usando Performance para captação de recursos, altera a porcentagem para 30% e 40% com um sucesso.",
            "Dragão" to "Nascidos sob o signo do Dragão, os indivíduos são respeitados por serem animados, pacientes e sábios em sua experiência. Muitos dos melhores estrategistas da história são do signo do Dragão. Os dragões tendem a odiar hipocrisia, fofocas e calúnias, e desprezam ser usados ou controlados pelos outros. O Dragão aumenta seu Espírito em um tipo de dado e aumenta seu máximo neste atributo para d12+1. Dragões se beneficiam de +1 em rolagens de Conhecimento Geral quando estão em situações desconhecidas.",
            "Kirin" to "O nascimento de um Kirin coincide com o final da estação de verão à medida que se aproxima o outono, representando um tempo de coleta e colheita. Um Kirin é proativo e percebe a malícia dos outros por meio de ações independentes. No entanto, um Kirin tende a fazer o que é necessário por conta própria, desconfiando que os outros cumpram suas obrigações. Um Kirin precisa de um incentivo a mais para prosseguir, começando com +1 em sua Reserva de Chi e uma Bene adicional em cada sessão.",
            "Macaco" to "O signo do Macaco está associado ao ser cheio de vida, de raciocínio rápido e versátil. Um Macaco é conhecido por tirar o máximo de qualquer situação, mas muitas vezes olha com menosprezo àqueles que não aprendem rapidamente. Muitas vezes, um temperamento impetuoso será a causa das ações de um Macaco. Com este signo de nascença, a Astúcia de um Macaco aumenta em um tipo de dado e seu máximo em aumenta d12+1. Um Macaco rola d4+1 nas perícias não treinadas baseadas em Astúcia, este bônus não se aplica ao dado selvagem.",
            "Raposa" to "Dizem que a Raposa possui uma intuição incrível. Capaz de ler situações sociais e saber exatamente o que as outras pessoas precisam ouvir. Isso não quer dizer que a Raposa seja falsa, é uma demonstração de habilidade e grande cuidado em aspectos de \"Manter as Aparências\". Traição e confiança são preocupações comuns de uma Raposa, levando-a a questionar a lealdade e a amizade de outros. Uma Raposa começa com a Vantagem Elevar a Moral e recebe +1 em Persuadir e nas rolagens da Tabela de Reação.",
            "Lobo" to "Um lobo é um animal social que se sente em casa quando pertence a uma matilha, assim como é verdadeiro para aqueles nascidos sob o signo do Lobo. Um Lobo exibe risos, alegria e comportamento solidário entre amigos, preferindo estar em companhia a sobreviver sozinho. Um Lobo pode sobreviver sozinho, mas prospera dentro de um grupo. Um Lobo começa com as Vantagens Elo Comum e adiciona +1 nas rolagens da Tabela de Reação para Reação Inicial..",
            "Tartaruga" to "Uma Tartaruga de casca dura é vista como lenta e covarde pelos outros, no entanto, uma Tartaruga possui mais longevidade, paciência e consciência do que aqueles que estão à sua volta. Hesitações na hora de tomar decisões frequentemente fazem uma Tartaruga perder oportunidades. Nascer sob o signo da Tartaruga concede +1 à Resistência. Aqueles que tentarem realizar a manobra “Finalização” em uma Tartaruga recebem -1 nas rolagens de ataque e dano na tentativa.",
            "Urso" to "Nascido no inverno, um Urso é considerado focado nas necessidades de sobrevivência. Na verdade, um Urso é centrado na família e focado na sobrevivência de cada membro. Isso pode significar que um Urso seja isolacionista e indiferente àqueles que não conhece. Por essa razão, o Vigor de um Urso aumenta em um tipo de dado e seu máximo aumenta para d12+1. Ursos reduzem a penalidade recebida de Exausto para -1 em vez de -2."
        )
        // Resumos genéricos para a edição Lite (não reproduzem o texto do livro original).
        val SIGNOS_ADG_DESC_LITE = mapOf(
            "Nenhum" to "Não possui signo; conserva os benefícios padrão do humano Adaptável (15 pontos de perícia e um slot gratuito de Adaptável).",
            "Basabasa" to "Concede a Vantagem Atraente; na criação, escolha +1 em Provocar ou em Intimidar contra alvos que se sintam atraídos ou enojados pelo herói.",
            "Boi" to "Dá +1 em Atletismo em testes baseados em Força (ou em todos os testes de Atletismo, com a Vantagem Brutamontes); também eleva a Força em um tipo de dado, com máximo em d12+1.",
            "Tigre" to "Aumenta o Raio de Comando em +4 quadros e concede +1 em testes de Medo, reduzindo em 1 o resultado na Tabela de Medo (cumulativo com Corajoso).",
            "Lebre" to "Começa com Cura d6 e pode gastar um Bene, uma vez por aventura, para tratar um Ferimento até 4 dias depois como se ainda estivesse dentro da Hora de Ouro (com os suprimentos médicos certos).",
            "Garça" to "Concede +1 em Aparar, Acrobacia inicial d4 e eleva Atletismo em um tipo de dado.",
            "Serpente" to "Escolha entre Jogar d6 ou Performance d6 iniciais; em Jogar, soma +1 ao saldo se vencer e -1 se perder, e em Performance para arrecadar fundos, os percentuais sobem para 30%/40% em caso de sucesso.",
            "Dragão" to "Eleva o Espírito em um tipo de dado (máximo d12+1) e concede +1 em Conhecimento Geral em situações desconhecidas.",
            "Kirin" to "Começa com +1 na Reserva de Chi e recebe um Bene extra a cada sessão.",
            "Macaco" to "Eleva a Astúcia em um tipo de dado (máximo d12+1) e usa d4+1, em vez do padrão, em perícias não treinadas baseadas em Astúcia (o Dado Selvagem não recebe esse bônus).",
            "Raposa" to "Concede a Vantagem Elevar a Moral, além de +1 em Persuadir e nos testes da Tabela de Reação.",
            "Lobo" to "Concede a Vantagem Elo Comum e +1 na Tabela de Reação usada na Reação Inicial.",
            "Tartaruga" to "Concede +1 na Resistência; quem tentar a manobra Finalização contra esse personagem sofre -1 no ataque e no dano dessa tentativa.",
            "Urso" to "Eleva o Vigor em um tipo de dado (máximo d12+1) e reduz a penalidade de Exausto para -1 em vez de -2."
        )
    }
    var maisPontosPericias by mutableStateOf(true)
    var cartaSelvagem       by mutableStateOf(true)
    var dinheiro by mutableIntStateOf(500)
    var requisicao by mutableIntStateOf(1)
    val carteiraPathfinder = mutableStateMapOf(PathfinderCurrencyIds.PL to 0, PathfinderCurrencyIds.PO to 0, PathfinderCurrencyIds.PP to 0, PathfinderCurrencyIds.PC to 0)

    fun updateTotalPathfinderMoney() {
        if (!compendioPathfinderAtivo) return
        val pl = carteiraPathfinder[PathfinderCurrencyIds.PL] ?: 0
        val po = carteiraPathfinder[PathfinderCurrencyIds.PO] ?: 0
        val pp = carteiraPathfinder[PathfinderCurrencyIds.PP] ?: 0
        val pc = carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0
        dinheiro = (pl * 1000) + (po * 100) + (pp * 10) + pc
    }

    fun addPathfinderMoney(amountInCopper: Int) {
        if (amountInCopper <= 0) return

        // Strategy: Maximize PO (Gold), avoid PL (Platinum).
        // 1 PO = 100 CP. 1 PP = 10 CP.

        var remaining = amountInCopper

        // Add PO
        val poToAdd = remaining / 100
        remaining %= 100

        // Add PP
        val ppToAdd = remaining / 10
        remaining %= 10

        // Add PC
        val pcToAdd = remaining

        if (poToAdd > 0) carteiraPathfinder[PathfinderCurrencyIds.PO] = (carteiraPathfinder[PathfinderCurrencyIds.PO] ?: 0) + poToAdd
        if (ppToAdd > 0) carteiraPathfinder[PathfinderCurrencyIds.PP] = (carteiraPathfinder[PathfinderCurrencyIds.PP] ?: 0) + ppToAdd
        if (pcToAdd > 0) carteiraPathfinder[PathfinderCurrencyIds.PC] = (carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0) + pcToAdd

        updateTotalPathfinderMoney()
    }

    fun spendPathfinderMoney(amountInCopper: Int): Boolean {
        if (amountInCopper <= 0) return true // No cost
        updateTotalPathfinderMoney() // Ensure sync
        if (dinheiro < amountInCopper) return false

        var costRemaining = amountInCopper

        // 1. Spend PC
        val currentPC = carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0
        if (currentPC >= costRemaining) {
            carteiraPathfinder[PathfinderCurrencyIds.PC] = currentPC - costRemaining
            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PC
            carteiraPathfinder[PathfinderCurrencyIds.PC] = 0
            costRemaining -= currentPC
        }

        // 2. Spend PP (1 PP = 10 PC)
        // Need X CP.
        // 1 PP covers 10 CP.
        // We need ceil(costRemaining / 10.0) PPs.
        val neededPP = (costRemaining + 9) / 10
        val currentPP = carteiraPathfinder[PathfinderCurrencyIds.PP] ?: 0

        if (currentPP >= neededPP) {
            carteiraPathfinder[PathfinderCurrencyIds.PP] = currentPP - neededPP
            val change = (neededPP * 10) - costRemaining
            if (change > 0) {
                carteiraPathfinder[PathfinderCurrencyIds.PC] = (carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0) + change
            }
            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PP
            carteiraPathfinder[PathfinderCurrencyIds.PP] = 0
            costRemaining -= (currentPP * 10)
        }

        // 3. Spend PO (1 PO = 100 PC)
        val neededPO = (costRemaining + 99) / 100
        val currentPO = carteiraPathfinder[PathfinderCurrencyIds.PO] ?: 0

        if (currentPO >= neededPO) {
            carteiraPathfinder[PathfinderCurrencyIds.PO] = currentPO - neededPO
            val changeTotal = (neededPO * 100) - costRemaining
            // Change needs to be broken down into PP and PC
            val changePP = changeTotal / 10
            val changePC = changeTotal % 10

            if (changePP > 0) carteiraPathfinder[PathfinderCurrencyIds.PP] = (carteiraPathfinder[PathfinderCurrencyIds.PP] ?: 0) + changePP
            if (changePC > 0) carteiraPathfinder[PathfinderCurrencyIds.PC] = (carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0) + changePC

            updateTotalPathfinderMoney()
            return true
        } else {
            // Spend all PO
            carteiraPathfinder[PathfinderCurrencyIds.PO] = 0
            costRemaining -= (currentPO * 100)
        }

        // 4. Spend PL (1 PL = 1000 PC)
        val neededPL = (costRemaining + 999) / 1000
        val currentPL = carteiraPathfinder[PathfinderCurrencyIds.PL] ?: 0

        if (currentPL >= neededPL) {
            carteiraPathfinder[PathfinderCurrencyIds.PL] = currentPL - neededPL
            val changeTotal = (neededPL * 1000) - costRemaining

            // Change breakdown (PO, PP, PC)
            val changePO = changeTotal / 100
            val rem1 = changeTotal % 100
            val changePP = rem1 / 10
            val changePC = rem1 % 10

            if (changePO > 0) carteiraPathfinder[PathfinderCurrencyIds.PO] = (carteiraPathfinder[PathfinderCurrencyIds.PO] ?: 0) + changePO
            if (changePP > 0) carteiraPathfinder[PathfinderCurrencyIds.PP] = (carteiraPathfinder[PathfinderCurrencyIds.PP] ?: 0) + changePP
            if (changePC > 0) carteiraPathfinder[PathfinderCurrencyIds.PC] = (carteiraPathfinder[PathfinderCurrencyIds.PC] ?: 0) + changePC

            updateTotalPathfinderMoney()
            return true
        }

        // Should not reach here if dinheiro check passed
        return false
    }

    fun compactPathfinderMoney() {
        updateTotalPathfinderMoney()
        var remaining = dinheiro

        val pl = remaining / 1000
        remaining %= 1000
        val po = remaining / 100
        remaining %= 100
        val pp = remaining / 10
        val pc = remaining % 10

        carteiraPathfinder[PathfinderCurrencyIds.PL] = pl
        carteiraPathfinder[PathfinderCurrencyIds.PO] = po
        carteiraPathfinder[PathfinderCurrencyIds.PP] = pp
        carteiraPathfinder[PathfinderCurrencyIds.PC] = pc
    }

    var famaManual by mutableIntStateOf(0)
    val poderesSelecionados = mutableStateListOf<String>()
    val manifestacoesPoderes = mutableStateMapOf<String, String>()
    val equipamentosComprados = mutableStateListOf<EquipamentoItem>()
    private val _maxedTraits = mutableStateListOf<String>()
    val maxedTraits: List<String> get() = _maxedTraits
    var idAtual by mutableStateOf<String?>(null)

    // UI State Persistence
    var vantSearchQuery by mutableStateOf("")
    val vantSelectedCategories = mutableStateListOf<Categoria>()
    var vantFilter by mutableStateOf(VantFilter())

    var equipSearchQuery by mutableStateOf("")
    val equipSelectedSuperTypes = mutableStateListOf<EquipSuperType>()
    var equipFilter by mutableStateOf(EquipFilter())
    val equipExpandedTypes = mutableStateMapOf<String, Boolean>()
    var equipSectionFilters = mutableStateMapOf<EquipSuperType, Set<String>>()

    var anotacoes by mutableStateOf("")
    var portraitFileName by mutableStateOf<String?>(null)
    // expandirRetrato: Se true, ocupa 50% da largura no Resumo. Se false, ocupa menos espaço (default).
    var expandirRetrato by mutableStateOf(false)
    var portraitScaleType by mutableStateOf("CROP") // CROP, FIT
    var portraitAlignment by mutableStateOf("CENTER") // TOP, CENTER, BOTTOM

    var coracaoCrystalSelecionado by mutableStateOf<CrystalHeart?>(null)

    val comprasPpPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    val superInvestments = mutableStateListOf<SuperInvestment>()
    var superNivelCampanha by mutableStateOf<Int?>(null)
    var usarSemPontosDePoder by mutableStateOf(false)

    var superPontosTotais by mutableIntStateOf(0)
    var superPontosDisponiveis by mutableIntStateOf(0)
    var superLimite by mutableIntStateOf(0)
    var superLimitePorPoder by mutableIntStateOf(0)
    var poderFavoritoId by mutableStateOf<String?>(null)
    val oMelhorQueHaSelecionada by derivedStateOf {
        vantagensSelecionadas.any { it.id == "o_melhor_que_ha" }
    }
    val limitePorPoderPadrao: Int
        get() = kotlin.math.floor(superPontosTotais / 3.0).toInt()
    val limiteFavorecido: Int
        get() = kotlin.math.ceil(superPontosTotais / 2.0).toInt()
    var limiteDePoderDaCampanha by mutableIntStateOf(Int.MAX_VALUE)

    var faseSupersAtiva by mutableStateOf(false)

    var bonusApararFromPower by mutableIntStateOf(0)
    var bonusResFromPower  by mutableIntStateOf(0)
    var armorFromPower     by mutableIntStateOf(0)

    var bonusMovimentacaoFromPower by mutableIntStateOf(0)

    val vantagensDePoder   = mutableStateSetOf<String>()
    val gastosPorPoder     = mutableStateMapOf<String, Int>()
    var naturalArmorFromRace by mutableIntStateOf(0)
    var soldadoCargaAtivo by mutableStateOf(true)

    // PROMPT 3: Transtornos Gratuitos (Horror Mode)
    val transtornos: SnapshotStateList<Complicacao> = mutableStateListOf()
    // PROMPT 5: Notas de Perícia (Sci-Fi / Wiseguys)
    val notasPericia: SnapshotStateMap<String, String> = mutableStateMapOf()

    val origemPersonagem: String?
        get() = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == ancestralidade }
            ?.origem
            ?.uppercase()

    val usaRiqueza: Boolean
        get() = optRegraRiqueza || origemPersonagem == "WISEGUYS" || optRegraCosaNostra

    val usaRequisicao: Boolean
        get() = compendioCrystalHeartAtivo || origemPersonagem == "CRYSTAL_HEART"

    var riquezaModifier by mutableIntStateOf(0)

    val dadoRiqueza: Int
        get() {
            var die = 6
            val hasPodreDeRico = vantagensSelecionadas.any { it.nome.keyify() == "PODRE DE RICO" }
            val hasRico = vantagensSelecionadas.any { it.nome.keyify() == "RICO" }
            val hasPobreza = complicacoesSelecionadas.keys.any { it.id.keyify() == "POBREZA" }

            die = when {
                hasPodreDeRico -> 10
                hasRico -> 8
                else -> 6
            }

            if (hasPobreza) {
                die = 4
            }

            val steps = listOf(4, 6, 8, 10, 12)
            val baseIndex = steps.indexOf(die).takeIf { it >= 0 } ?: 1
            val finalIndex = (baseIndex + riquezaModifier).coerceIn(0, steps.lastIndex)

            return steps[finalIndex]
        }

    fun aplicarRegrasWiseguys() {
        if (!optRegraCosaNostra) return

        // Conexões (Máfia)
        val conexoes = listaVantagens.firstOrNull { it.id == "conexoes" }
        if (conexoes != null) {
            val v = conexoes.copy()
            v.choice = "Máfia"
            // Verifica se já não tem essa vantagem com essa escolha
            val jaTem = vantagensSelecionadas.any { it.id == "conexoes" && it.choice.equals("Máfia", ignoreCase = true) }
            if (!jaTem) {
                vantagensSelecionadas.add(v)
                // Marca como automática para não ser removível ou custar pontos
                vantagensAutomaticas.add(v.nome)
            }
        }

        // Obrigação (Maior) - Servir a Máfia
        val obrigacao = listaComplicacoes.firstOrNull { it.id == "obrigacao" }
        if (obrigacao != null) {
            val jaTem = complicacoesSelecionadas.keys.any { it.id == "obrigacao" }
            if (!jaTem) {
                // Adiciona como Maior
                complicacoesSelecionadas[obrigacao] = "Maior"
                desvantagensAutomaticas.add(obrigacao.name)
                // Nota: O texto "servir a Máfia" idealmente iria nas anotações ou como um custom field,
                // mas a estrutura de Complicacao é rígida. Vamos assumir que o usuário entende pelo contexto Wiseguys.
                if (!anotacoes.contains("Obrigação: Servir a Máfia")) {
                    anotacoes += "\n• Obrigação: Servir a Máfia"
                }
            }
        }
    }

    fun adicionarVantagemCavaleiro(vant: Vantagem, armorChoice: String) {
        adicionarVantagem(vant)

        val targets = mutableListOf<String>()
        targets.add("Cavalo de Guerra")
        targets.add("Lança")
        targets.add("Espada Longa")
        targets.add("Escudo (Médio)")

        if (armorChoice.contains("Completa", true)) {
            targets.add("Armadura de Placas")
        } else {
            targets.add("Cota de Malha")
        }

        fun findItem(target: String): EquipamentoItem? {
            val tKey = target.keyify()
            listaEquipamentos.firstOrNull { it.nome.keyify() == tKey }?.let { return it }

            if (tKey.contains("CAVALO")) {
                return listaEquipamentos.firstOrNull {
                    val k = it.nome.keyify()
                    k.contains("CAVALO") && k.contains("GUERRA")
                }
            }
            if (tKey.contains("LANCA")) {
                listaEquipamentos.firstOrNull { it.nome.equals("Lança", true) }?.let { return it }
                return listaEquipamentos.firstOrNull { it.nome.keyify().contains("LANCA") }
            }
            if (tKey.contains("ESCUDO")) {
                return listaEquipamentos.firstOrNull {
                    val k = it.nome.keyify()
                    k.contains("ESCUDO") && k.contains("MEDIO")
                }
            }
            return null
        }

        targets.forEach { t ->
            val itemProto = findItem(t)
            if (itemProto != null) {
                val newItem = itemProto.copy(
                    custo = JsonPrimitive(0),
                    origemGrant = "CAVALEIRO"
                )
                equipamentosComprados.add(newItem)
            }
        }
    }

    fun valorMovimentacao(): Int {
        val base = 6
        val mods = ModifierEngine.sum(this, ModifierTarget.PACE)
        return (base + mods).coerceAtLeast(0)
    }

    fun totalTensaoCibernetica(): Int =
        ciberneticosInstalados.sumOf { it.strain_custo } + equipamentosComprados.sumOf { it.tensao ?: 0 }

    fun totalTensaoAtual(): Int = totalTensaoCibernetica()

    fun valorLimiteTensao(): Pair<Int, Int> {
        val espirito = valoresAtributos["ESPIRITO"]?.intValue ?: 4
        val vigor = valoresAtributos["VIGOR"]?.intValue ?: 4

        // Base = half the die type of lower attribute (Standard: Die/2)
        // Or if we treat die value as the number: (e.g. d6 -> 6). 6/2 = 3.
        // My intValue stores 4, 6, 8, 10, 12. So directly dividing by 2 works.
        val lower = minOf(espirito, vigor)
        var baseLimit = lower / 2
        var maxLimit = baseLimit * 2 // Implied by 3->6 example

        // Modifiers
        val hasCibertolerancia = vantagensSelecionadas.any { it.nome.keyify() == "CIBERTOLERANCIA" }
        val hasCibersamurai = vantagensSelecionadas.any { it.nome.keyify() == "CIBERSAMURAI" }
        val hasCiborgue = vantagensSelecionadas.any { it.nome.keyify() == "CIBORGUE" }
        val hasCiberResistencia = complicacoesSelecionadas.keys.any { it.id.keyify() == "CIBER_RESISTENCIA" }

        if (hasCibertolerancia) {
            baseLimit += 2
            maxLimit += 2
        }
        if (hasCibersamurai) {
            baseLimit += 2
            maxLimit += 2
        }
        if (hasCiborgue) {
            baseLimit += 4
            maxLimit += 4
        }
        if (hasCiberResistencia) {
            baseLimit -= 2
        }

        return baseLimit.coerceAtLeast(0) to maxLimit.coerceAtLeast(0)
    }

    fun totalSlotsMecha(): Int =
        equipamentosComprados.sumOf { (it.mods_slots as? JsonPrimitive)?.content?.toIntOrNull() ?: 0 }

    fun isPersonagemRobotico(): Boolean {
        val ancestral = currentAncestryDef
        val nomeKey = (ancestral?.nome ?: ancestralidade).keyify()
        val robotByName = listOf("ANDROID", "CONSTRUTO", "CONSTRUCTO").any { nomeKey.contains(it) }
        val robotBySkill = ancestral?.habilidades?.any {
            val k = it.nome.keyify()
            k == "MODIFICACOES" || it.id == "ROBO" || it.id == "CONSTRUTO"
        } == true
        val robotByAdvantage = ancestral?.let { effectiveVantagensGratis(it) }?.any {
            val k = it.keyify()
            k == "CONSTRUTO" || k == "ROBO"
        } == true

        return robotByName || robotBySkill || robotByAdvantage
    }

    fun limiteModsRoboticos(): Int {
        val baseSlots = (valorTamanho() + 1).coerceAtLeast(1)
        val bonusSlots = vantagensSelecionadas.count {
            val nomeKey = it.nome.keyify()
            nomeKey.contains("MODS") && nomeKey.contains("ROBOT")
        }
        return (baseSlots + bonusSlots).coerceAtLeast(0)
    }

    fun valorAparar(): Int {
        val perLutar = mapaPericias["LUTAR"]
        val lutarRaw = perLutar?.let { rawTotalComSupers(it) } ?: 0
        // "Jutsu" não existe como perícia própria no catálogo (mapaPericias["JUTSU"] nunca
        // resolvia nada) — as categorias extras vivem em jutsuExtras, uma por especialização
        // de arma além do slot base (que é a própria perícia Lutar).
        val jutsuRaw = if (compendioArteDaGuerraAtivo) {
            jutsuExtras.maxOfOrNull { rawTotalComSupers(it) } ?: 0
        } else 0
        val melhorLuta = maxOf(lutarRaw, jutsuRaw)
        val base     = 2 + (melhorLuta / 2)

        val mods = ModifierEngine.sum(this, ModifierTarget.PARRY)

        val finalValue = base + mods
        return finalValue.coerceAtLeast(0)
    }

    // Engine Delegation
    fun tamanhoExibido(): Int = ModifierEngine.sizeDisplay(this)
    fun tamanhoParaResistencia(): Int = ModifierEngine.sizeForToughness(this)
    fun resistenciaBase(): Int = ModifierEngine.toughnessBase(this)

    fun valorResistenciaBase(): Int {
        // Agora delega para o engine
        // O engine já inclui bônus de poder, mas se quisermos manter "Base" vs "Final"
        // para outras lógicas, teríamos que ter cuidado.
        // O prompt pede para usar engine.
        return resistenciaBase()
    }

    fun valorResistenciaFinal(): Int {
        // resistenciaBase() do engine j inclui bonusResFromPower (via TOUGHNESS_FLAT)
        return resistenciaBase()
    }

    fun calculaAtaqueDesarmado(): Pair<String, String> {
        val modifiers = mutableListOf<String>()
        var steps = 0

        // Check Martial Artist (id: artista_marcial)
        if (vantagensSelecionadas.any { it.id == "artista_marcial" }) {
            modifiers.add("Artista Marcial")
            steps++
        }

        // Check Brawler (id: brigao)
        if (vantagensSelecionadas.any { it.id == "brigao" }) {
            modifiers.add("Brigão")
            steps++
        }

        // Check Claws (Garra)
        val ancestry = currentAncestryDef
        val hasRacialClaws = ancestry?.habilidades?.any { it.nome.keyify().contains("GARRA") } == true ||
                ancestry?.vantagensGratis?.any { it.keyify().contains("GARRA") } == true

        // Check Edge Claws
        val hasEdgeClaws = vantagensSelecionadas.any { it.nome.keyify().contains("GARRA") }

        if (hasRacialClaws || hasEdgeClaws) {
            modifiers.add("Garra")
            steps++
        }

        val damageStr = if (steps == 0) {
            "For"
        } else {
            // Base die for natural weapon is d4. Steps increase it: d4->d6->d8->d10->d12->d12+1...
            val dieType = when (steps) {
                1 -> "d4"
                2 -> "d6"
                3 -> "d8"
                4 -> "d10"
                5 -> "d12"
                else -> "d12+${steps-5}"
            }
            "For+$dieType"
        }

        return damageStr to modifiers.joinToString(", ")
    }

    fun extrairArmasNaturais(): List<EquipamentoItem> {
        val weapons = mutableListOf<EquipamentoItem>()
        val ancestralidadeObj = currentAncestryDef
            ?: return emptyList()

        // Map keyword to expected ID for robust lookup
        val keywordToIdMap = mapOf(
            "Garras" to "GARRAS",
            "Mordida" to "MORDIDA",
            "Chifres" to "CHIFRES",
            "Cabeça Dura" to "CABECADA",
            "Cascos" to "CASCOS",
            "Toque Arrepiante" to "TOQUE_ARREPIANTE",
            "Toque da Morte" to "TOQUE_DA_MORTE",
            "Ferrão" to "FERRAO",
            "Toque Venenoso" to "TOQUE_VENENOSO"
        )

        val keywords = keywordToIdMap.keys.toList()
        val addedTypes = mutableSetOf<String>()

        // Sources for name-based fallback
        val sources = effectiveVantagensGratis(ancestralidadeObj) +
            ancestralidadeObj.habilidades.map { it.nome } +
            vantagensRaciais +
            vantagensSelecionadas.map { it.nome }

        // Helper to find description for a keyword or ID
        fun findDesc(keyword: String, targetId: String?): String {
            // 1. Try Ability (Habilidade Racial) by ID first, then Name
            val habById = if (targetId != null) ancestralidadeObj.habilidades.find { it.id == targetId } else null
            if (habById != null) return habById.descricao

            val habByName = ancestralidadeObj.habilidades.find { it.nome.contains(keyword, ignoreCase = true) }
            if (habByName != null) return habByName.descricao

            // 2. Try Free Edge / Racial Advs (Vantagem Grátis / Raciais)
            // These are strings (names or IDs). Check if any matches ID or Keyword.
            val allGrantStrings = effectiveVantagensGratis(ancestralidadeObj) + vantagensRaciais
            val matchedString = allGrantStrings.firstOrNull { s ->
                val sKey = s.keyify()
                (targetId != null && sKey == targetId.keyify()) || s.contains(keyword, ignoreCase = true)
            }

            if (matchedString != null) {
                // Try to resolve as Edge description from global list
                val edge = listaVantagens.firstOrNull {
                    it.id == matchedString || it.nome.keyify() == matchedString.keyify() || it.nome.contains(keyword, ignoreCase = true)
                }
                if (edge != null) return edge.descricao
                // Fallback: use the string itself if it looks like a description (unlikely for IDs) but rare
            }

            return ""
        }

        // Check for Martial Artist / Brawler (used for upgrading damage)
        val hasMartialArtist = vantagensSelecionadas.any { it.id == "artista_marcial" }
        val hasBrawler = vantagensSelecionadas.any { it.id == "brigao" || it.id == "guerreiro_marcial" }

        // Helper to upgrade die type string (e.g. "For+d4" -> "For+d6")
        fun upgradeDie(dmg: String): String {
            val dieMap = listOf("d4", "d6", "d8", "d10", "d12")
            val match = Regex("""d(\d+)""").find(dmg) ?: return dmg
            val currentDie = match.value
            val index = dieMap.indexOf(currentDie)
            if (index != -1 && index < dieMap.lastIndex) {
                return dmg.replace(currentDie, dieMap[index + 1])
            }
            if (index == -1 && !dmg.contains("+d")) {
                // Handle plain "For" -> "For+d4" case if passed here, though usually handled explicitly
                return "$dmg+d4"
            }
            return dmg
        }

        // Monster Natural Weapons
        getMonstroSelecionado()?.let { monstro ->
            monstro.habilidades.forEach { hab ->
                val nomeKey = hab.nome.keyify()
                val hasNaturalAttack = nomeKey.contains("GARRA") || nomeKey.contains("MORDIDA") || nomeKey.contains("CHIFRE") || nomeKey.contains("CASCO")
                if (!hasNaturalAttack) return@forEach

                val dmgRegex = Regex("""(For|Str|Força|Strength)(\s*\+\s*)?d\d+""", RegexOption.IGNORE_CASE)
                val paRegex = Regex("""PA\s*\d+""", RegexOption.IGNORE_CASE)
                val baseDamage = dmgRegex.find(hab.descricao)?.value?.replace(" ", "") ?: "For+d4"
                val basePa = paRegex.find(hab.descricao)?.value?.replace("PA", "", ignoreCase = true)?.trim()?.toIntOrNull() ?: 0

                fun addNaturalWeapon(name: String, canScaleClaws: Boolean) {
                    var dmgMatch = baseDamage
                    var paValue = basePa

                    if (vantagensSelecionadas.any { it.id == "mordida_garras_aprimorada" } && (name.equals("Garras", true) || name.equals("Mordida", true))) {
                        dmgMatch = "For+d8"
                        paValue = maxOf(paValue, 4)
                    }

                    if (canScaleClaws && (hasMartialArtist || hasBrawler)) {
                        dmgMatch = upgradeDie(dmgMatch)
                    }

                    weapons.add(
                        EquipamentoItem(
                            nome = name,
                            dano = JsonPrimitive(dmgMatch),
                            pa = if (paValue > 0) JsonPrimitive(paValue) else null,
                            distancia = JsonPrimitive("Toque"),
                            peso = JsonPrimitive(0),
                            custo = JsonPrimitive(0)
                        )
                    )
                }

                if (nomeKey.contains("MORDIDA") && nomeKey.contains("GARRA")) {
                    addNaturalWeapon("Mordida", canScaleClaws = false)
                    addNaturalWeapon("Garras", canScaleClaws = true)
                } else if (nomeKey.contains("GARRA")) {
                    addNaturalWeapon("Garras", canScaleClaws = true)
                } else if (nomeKey.contains("MORDIDA")) {
                    addNaturalWeapon("Mordida", canScaleClaws = false)
                } else if (nomeKey.contains("CHIFRE")) {
                    addNaturalWeapon("Chifres", canScaleClaws = false)
                } else if (nomeKey.contains("CASCO")) {
                    addNaturalWeapon("Cascos", canScaleClaws = false)
                }
            }
        }

        // Parse logic
        keywords.forEach { key ->
            val keyToken = key.keyify()
            val targetId = keywordToIdMap[key]

            // Check if weapon is already added
            val alreadyPresent = weapons.any { weapon ->
                val nameKey = weapon.nome.keyify()
                when (keyToken) {
                    "TOQUE ARREPIANTE" -> nameKey.contains("TOQUE ARREPIANTE")
                    "TOQUE DA MORTE" -> nameKey.contains("TOQUE DA MORTE")
                    "FERRAO" -> nameKey.contains("FERRAO")
                    "TOQUE VENENOSO" -> nameKey.contains("TOQUE VENENOSO")
                    else -> nameKey.contains(keyToken)
                }
            }
            if (alreadyPresent) return@forEach

            // Variant-specific exclusions (Legacy checks + ID checks)
            // Note: Since we use IDs now, we could check IDs directly, but let's keep robust logic
            if (compendioSciFiAtivo) {
                // Sáurios Cuspidor removes MORDIDA (via ID or name)
                if (ancestralidade.keyify() == "SAURIOS" && resolveCurrentSciFiVariantSelection() == "Cuspidor" && keyToken == "MORDIDA") return@forEach
                // Insetoides Vespa removes GARRAS
                if (ancestralidade.keyify() == "INSETOIDES" && resolveCurrentSciFiVariantSelection() == "Vespa" && keyToken == "GARRAS") return@forEach
            }

            // Check presence via ID (Strong match) or Name (Legacy/Fallback)
            val hasIdMatch = targetId != null && (
                ancestralidadeObj.habilidades.any { it.id == targetId } ||
            effectiveVantagensGratis(ancestralidadeObj).any { it.keyify() == targetId.keyify() } ||
                vantagensRaciais.any { it.keyify() == targetId.keyify() } ||
                vantagensSelecionadas.any { it.id == targetId }
            )

            val matchedSource = if (hasIdMatch) key else sources.firstOrNull { it.contains(key, ignoreCase = true) }

            if (matchedSource != null) {
                // Try to get description from Selected Edge first (if applicable)
                val selectedAdvDesc = if (targetId != null) {
                    vantagensSelecionadas.firstOrNull { it.id == targetId }?.descricao
                } else {
                    vantagensSelecionadas.firstOrNull { it.nome.equals(matchedSource, ignoreCase = true) }?.descricao
                }.orEmpty()

                var desc = selectedAdvDesc.ifBlank { findDesc(key, targetId) }
                if (desc.isBlank()) {
                    // Fallback to source string if it was a name match and description is missing
                    desc = if (!hasIdMatch) matchedSource else ""
                }

                // Regex to find damage like "For+d4", "Str+d4", "For+d6", allowing for spaces
                val dmgRegex = Regex("""(For|Str|Força|Strength)(\s*\+\s*)?d\d+""", RegexOption.IGNORE_CASE)
                val paRegex = Regex("""PA\s*\d+""", RegexOption.IGNORE_CASE)

                var dmgMatch = dmgRegex.find(desc)?.value?.replace(" ", "") ?: "For+d4"
                var paFinal = paRegex.find(desc)?.value?.replace("PA", "", ignoreCase = true)?.trim()?.toIntOrNull() ?: 0

                val garrasDemonioCount = vantagensSelecionadas.count { it.id == "garras_demonio" }
                val mordidaDemonioCount = vantagensSelecionadas.count { it.id == "mordida_demonio" }
                val hasGarrasVampiro = vantagensSelecionadas.any { it.id == "garras_vampiro" }
                val hasLobisomemAprimorado = vantagensSelecionadas.any { it.id == "mordida_garras_aprimorada" }

                if (key.equals("Toque Arrepiante", ignoreCase = true)) {
                    dmgMatch = "For+d4"
                }

                if (key.equals("Toque da Morte", ignoreCase = true)) {
                    dmgMatch = "For+d4/For+2d6"
                }

                if (key.equals("Ferrão", ignoreCase = true)) {
                    dmgMatch = "For+d4"
                }

                if (key.equals("Toque Venenoso", ignoreCase = true)) {
                    // Usually just an effect, but if treated as weapon
                    dmgMatch = "-" // Or specific damage if described
                    // Vespa text says "trate como Mordida (For+d4)" for Ferrão. Toque Venenoso separate.
                    // If Toque Venenoso is just effect, maybe dist "Toque" is enough.
                    // If matchedSource has description "Cuspidor", it might be Ranged?
                    // "Cuspidor (Toque Venenoso...)" - Name implies Spitter but effect is Poison Touch?
                    // Actually prompt says "Cuspidor (Cuspidor)". Maybe trait is called "Cuspidor"?
                    // If source is "TOQUE VENENOSO (Cuspidor)", description might say "Alcance Cone" or something.
                    // But here we just default.
                    if (matchedSource.contains("Cuspidor", ignoreCase = true)) {
                        // Might be ranged
                        // We leave dmgMatch as regex found (likely none)
                    }
                }

                if (hasLobisomemAprimorado && (key.equals("Garras", true) || key.equals("Mordida", true))) {
                    dmgMatch = "For+d8"
                    paFinal = maxOf(paFinal, 4)
                }

                if (key.equals("Garras", ignoreCase = true) && garrasDemonioCount > 0) {
                    if (garrasDemonioCount >= 2) {
                        dmgMatch = "For+d6"
                        paFinal = maxOf(paFinal, 2)
                    } else {
                        dmgMatch = "For+d4"
                    }
                }

                // Insetoides Padrão: For+d4 e PA 2
                if (key.equals("Garras", ignoreCase = true) && ancestralidade.keyify() == "INSETOIDES") {
                    dmgMatch = "For+d4"
                    paFinal = maxOf(paFinal, 2)
                }

                // Insetoides Vespa Variante (Ferrão/Mordida): Tem PA -
                if (key.equals("Ferrão", ignoreCase = true) && ancestralidade.keyify() == "INSETOIDES") {
                    paFinal = 0
                }

                if (key.equals("Garras", ignoreCase = true) && hasGarrasVampiro) {
                    dmgMatch = "For+d6"
                    paFinal = maxOf(paFinal, 2)
                }

                if (key.equals("Mordida", ignoreCase = true) && mordidaDemonioCount > 0) {
                    dmgMatch = "For+d6"
                    if (mordidaDemonioCount >= 2) {
                        paFinal = maxOf(paFinal, 2)
                    }
                }

                // Apply scaling to "Garras" if Martial Artist or Brawler is present
                if (key.equals("Garras", ignoreCase = true)) {
                    if (hasMartialArtist || hasBrawler) {
                        dmgMatch = upgradeDie(dmgMatch)
                    }
                }

                var finalName = key
                if (matchedSource.contains("(") && matchedSource.contains(")")) {
                    val prefix = matchedSource.substringBefore("(").trim()
                    if (prefix.isNotBlank() && !prefix.equals(key, ignoreCase = true)) {
                        finalName = prefix.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    }
                }

                if (addedTypes.add(key.uppercase())) {
                    weapons.add(
                        EquipamentoItem(
                            nome = finalName,
                            dano = JsonPrimitive(dmgMatch),
                            pa = if (paFinal > 0) JsonPrimitive(paFinal) else null,
                            distancia = JsonPrimitive("Toque"),
                            peso = JsonPrimitive(0),
                            custo = JsonPrimitive(0)
                        )
                    )
                }
            }
        }

        // Always add "Ataque Natural" (Unarmed) using central logic - Filter if specific natural weapons exist
        val hasSpecificNaturalWeapons = weapons.any { weapon ->
            val key = weapon.nome.keyify()
            key.contains("GARRA") ||
                key.contains("MORDIDA") ||
                key.contains("CHIFRE") ||
                key.contains("CASCO") ||
                key.contains("TOQUE ARREPIANTE") ||
                key.contains("TOQUE DA MORTE") ||
                key.contains("CABECA DURA")
        }
        val isInsectoid = ancestralidade.keyify().contains("INSETOIDE")

        if (!hasSpecificNaturalWeapons && !isInsectoid) {
            val (unarmedDmg, unarmedNotes) = calculaAtaqueDesarmado()
            weapons.add(
                EquipamentoItem(
                    nome = "Ataque Natural",
                    dano = JsonPrimitive(unarmedDmg),
                    distancia = JsonPrimitive("Toque"),
                    peso = JsonPrimitive(0),
                    custo = JsonPrimitive(0),
                    observacoes = if (unarmedNotes.isNotBlank()) JsonPrimitive(unarmedNotes) else null
                )
            )
        }

        return weapons
    }

    fun valorChi(): Int {
        return reservaChi
    }

    fun valorDominio(): Int {
        val espiritoRaw = valoresAtributos["ESPIRITO"]?.intValue ?: 4
        // Domínio inicial para ressuscitados é geralmente o dado de Espírito
        return espiritoRaw
    }

    fun valorArmaduraEfetiva(): Int {
        // Soma armadura de equipamentos (via Engine, já filtrando Mechas) com a
        // variável 'armadura' de estado (override manual/legado usado por raças
        // como Sáurios), usa o maior entre isso e a armadura de Poderes, e soma
        // a armadura natural da raça.
        val armorFromEquipment = ModifierEngine.sum(this, ModifierTarget.ARMOR)
        val totalEquipmentArmor = armorFromEquipment + armadura

        val bestArmor = kotlin.math.max(armorFromPower, totalEquipmentArmor)
        return (bestArmor + naturalArmorFromRace).coerceAtLeast(0)
    }

    fun valorTamanho(): Int = tamanhoExibido()

    fun valorFama(): Int {
        // Base 0 + Modifiers (Traits) + Manual
        // TODO: Add traits logic if needed later
        return famaManual
    }

    // PROMPT 2: Brawny (Brutamontes) Carga calculation
    fun valorCargaMaxima(): Float {
        val strengthRaw = valoresAtributos["FORCA"]?.intValue ?: 4
        val hasSoldado = vantagensSelecionadas.any { it.nome.keyify() == "SOLDADO" }
        val hasMusculoso = vantagensSelecionadas.any { it.nome.keyify() == "MUSCULOSO" }
        val hasDwarfLoadBonus = compendioPathfinderAtivo && ancestralidade.keyify() == "ANAO"
        // PROMPT 2: "Brawny treats Strength as one die type higher"
        val hasBrutamontes = vantagensSelecionadas.any {
            val nk = it.nome.keyify()
            nk == "BRUTAMONTES" || nk == "BRAWNY"
        }

        var effectiveStrength = strengthRaw

        // Brawny logic: treat as one die higher
        if (hasBrutamontes) {
             effectiveStrength = if (effectiveStrength < 12) effectiveStrength + 2 else effectiveStrength + 1
        }

        // Soldier logic: treat as one die higher (if active)
        if (hasSoldado && soldadoCargaAtivo) {
             effectiveStrength = if (effectiveStrength < 12) effectiveStrength + 2 else effectiveStrength + 1
        }

        // Pathfinder Dwarf logic: treat as one die higher
        if (hasDwarfLoadBonus) {
             effectiveStrength = if (effectiveStrength < 12) effectiveStrength + 2 else effectiveStrength + 1
        }

        val bonusCapacity = if (hasMusculoso) 10f else 0f
        val baseLimit = if (effectiveStrength >= 4) ((effectiveStrength - 2) / 2) * 10f else 0f
        return baseLimit + bonusCapacity
    }

    fun gastarPcParaRecursos(): Boolean {
        if (pontosComplicacao - pontosComplicacaoGastos < 1) return false
        pontosComplicacaoGastos += 1
        cpRecursosStack.add(Unit)
        dinheiro += if (compendioPathfinderAtivo) 60000 else 500
        return true
    }

    fun devolverPcDeRecursos() {
        if (cpRecursosStack.isNotEmpty()) {
            cpRecursosStack.removeAt(cpRecursosStack.lastIndex)
            pontosComplicacaoGastos -= 1
            val amount = if (compendioPathfinderAtivo) 60000 else 500
            dinheiro = (dinheiro - amount).coerceAtLeast(0)
        }
    }

    fun checkAndRefundResourcePb() {
        if (usaRiqueza || usaRequisicao) return
        val rate = if (compendioPathfinderAtivo) 60000 else 500
        while (cpRecursosStack.isNotEmpty() && dinheiro >= rate) {
            devolverPcDeRecursos()
        }
    }

    fun gastarPcParaVantagem(): Boolean {
        // custo de 2 PC para 1 PV
        if (pontosComplicacao - pontosComplicacaoGastos < 2) return false

        pontosComplicacaoGastos += 2
        cpPvStack.add(Unit)   // registra que 1 vantagem foi comprada
        pontosVantagem += 1
        return true
    }

    fun devolverPcDeVantagem() {
        if (cpPvStack.isNotEmpty()) {
            cpPvStack.removeAt(cpPvStack.lastIndex)
            pontosComplicacaoGastos -= 2

            val removedAdvantage = if (pontosVantagem == 0) {
                removerUltimaVantagemCompradaComPv()
            } else false

            if (removedAdvantage) {
                pontosVantagem += 1 // devolve o PV gasto pela vantagem removida
            }

            pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
        }
    }

    fun gastarPcParaAtributo(): Boolean {
        // Custo de 2 Pontos de Complicação para 1 aumento de atributo
        val custo = 2
        val disponivel = (pontosComplicacao - pontosComplicacaoGastos)

        if (disponivel < custo) return false

        cpPaStack.add("PB")              // registra 1 compra de atributo com PC
        pontosComplicacaoGastos += custo // soma 2 ao total gasto
        recalcularPontosAtributo()       // recalcula PA restantes e stacks

        return true
    }

    fun devolverPcDeAtributo() {
        if (cpPaStack.isNotEmpty()) {
            cpPaStack.removeAt(cpPaStack.lastIndex)
            pontosComplicacaoGastos = (pontosComplicacaoGastos - 2).coerceAtLeast(0)
            recalcularPontosAtributo()
        }
    }

    fun devolverPcDePericia() {
        if (cpSpStack.isNotEmpty()) {
            cpSpStack.removeAt(cpSpStack.lastIndex)
            pontosComplicacaoGastos = (pontosComplicacaoGastos - 1).coerceAtLeast(0)
            rebuildAllPericiaStacks()
        }
    }

    fun gastarPcParaPericia(): Boolean {
        if (pontosComplicacao - pontosComplicacaoGastos < 1) return false
        pontosComplicacaoGastos += 1
        cpSpStack.add(Unit)
        // O pool de perícia atualiza automaticamente via derivedStateOf
        return true
    }

    fun isPathfinderEligible(v: Vantagem): Boolean {
        if (!compendioPathfinderAtivo) return false
        return when (v.categoria) {
            Categoria.CLASSE, Categoria.PROFISSIONAL, Categoria.ANTECEDENTE -> true
            else -> false
        }
    }

    private fun normalizeAutoKey(value: String): String =
        value.keyify()
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")

    private fun buildAutomaticAdvantageContextForTransition(): List<String> {
        val automaticTokens = linkedSetOf<String>()

        fun addRawTokens(values: Collection<String>) {
            values.forEach { value ->
                if (value.isNotBlank()) automaticTokens.add(value)
            }
        }

        fun addTokensFromIds(ids: Collection<String>) {
            ids.forEach { id ->
                if (id.isBlank()) return@forEach
                automaticTokens.add(id)
                listaVantagens.firstOrNull { it.id == id }?.let { vantagem ->
                    automaticTokens.add(vantagem.id)
                    automaticTokens.add(vantagem.nome)
                }
            }
        }

        addRawTokens(vantagensAutomaticas)
        addRawTokens(vantagensRaciais)
        addTokensFromIds(vantagensAutomaticasDoSigno)
        addTokensFromIds(vantagensAutomaticasDoElemento)
        addTokensFromIds(vantagensAutomaticasDoPotencialFisico)
        addTokensFromIds(vantagensAutomaticasDoTropo)
        addTokensFromIds(vantagensAutomaticasDoProtagonista)
        addTokensFromIds(vantagensSlotProtagonista)
        addTokensFromIds(samuraiCombatSlotIds)
        addTokensFromIds(listOfNotNull(pathfinderFreeSlotId, vantagemAdaptavelSelecionadaId))

        vantagensSelecionadas
            .asSequence()
            .filter { isVantagemAutomatica(it) }
            .forEach { vantagem ->
                automaticTokens.add(vantagem.id)
                automaticTokens.add(vantagem.nome)
            }

        return automaticTokens.toList()
    }

    fun isVantagemAutomatica(v: Vantagem): Boolean {
        val key = normalizeAutoKey(v.nome.substringBefore("(").trim())
        val autoKeys = (vantagensAutomaticas + vantagensRaciais)
            .map { normalizeAutoKey(it.substringBefore("(").trim()) }
            .toSet()

        // Also check raw IDs in vantagensRaciais/Automaticas because some JSONs use IDs directly
        // like "aa_agente_syn" which doesn't match the name "ANTECEDENTE ARCANO"
        val autoIds = (vantagensAutomaticas + vantagensRaciais).map { normalizeAutoKey(it) }.toSet()

        val autoTropoIds = vantagensAutomaticasDoTropo.map { normalizeAutoKey(it) }.toSet()
        val autoProtagonistaIds = vantagensAutomaticasDoProtagonista.map { normalizeAutoKey(it) }.toSet()
        val autoSignoIds = vantagensAutomaticasDoSigno.map { normalizeAutoKey(it) }.toSet()
        val autoElementoIds = vantagensAutomaticasDoElemento.map { normalizeAutoKey(it) }.toSet()
        val autoPotFisIds = vantagensAutomaticasDoPotencialFisico.map { normalizeAutoKey(it) }.toSet()

        val kirinSorteAutomatica =
            compendioArteDaGuerraAtivo &&
            ancestralidade.keyify().contains("HUMANO") &&
            signoAdgSelecionado.equals("Kirin", ignoreCase = true) &&
            v.id == "sorte"

        return kirinSorteAutomatica ||
                key in autoKeys ||
                normalizeAutoKey(v.id) in autoIds ||
                normalizeAutoKey(v.id) in autoTropoIds ||
                normalizeAutoKey(v.id) in autoProtagonistaIds ||
                normalizeAutoKey(v.id) in autoSignoIds ||
                normalizeAutoKey(v.id) in autoElementoIds ||
                normalizeAutoKey(v.id) in autoPotFisIds ||
                (v.id == "conexoes" && v.choice?.equals("Máfia", ignoreCase = true) == true)
    }

    fun isProtagonistaEligible(v: Vantagem): Boolean {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_protagonista") return false
        return when (protagonistaRollVantagem) {
            1 -> v.categoria == Categoria.CHI
            2 -> v.categoria == Categoria.ESTRANHAS
            3 -> v.categoria == Categoria.ANTECEDENTE
            4 -> v.categoria == Categoria.COMBATE
            5 -> v.categoria == Categoria.SOCIAIS
            6 -> true
            8 -> true
            else -> false
        }
    }

    val protagonistaSlotAvailable: Boolean by derivedStateOf {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_protagonista") false
        else {
            vantagensSlotProtagonista.isEmpty()
        }
    }

    val samuraiCombatSlotAvailable: Boolean by derivedStateOf {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_samurai") false
        else {
            samuraiVantagemEscolhida == "Combate" && samuraiCombatSlotIds.isEmpty()
        }
    }

    var pathfinderFreeSlotId: String? by mutableStateOf(null)

    val pathfinderSlotAvailable: Boolean by derivedStateOf {
        compendioPathfinderAtivo && pathfinderFreeSlotId == null
    }

    private fun Vantagem.isBrutamontes(): Boolean {
        val idKey = id.keyify()
        val nameKey = nome.keyify()
        return idKey == "BRUTAMONTES" || idKey == "BRAWNY" || nameKey == "BRUTAMONTES" || nameKey == "BRAWNY"
    }

    private fun rawValuesBeforeArcaneSkillGrant(v: Vantagem): Map<String, Int>? {
        if (modoProgressaoAtivo) return null
        if (!compendioFantasiaAtivo && !compendioHorrorAtivo && !compendioPathfinderAtivo && !compendioSciFiAtivo) return null
        if (v.toArcanoKey() == null) return null

        return periciasComIdiomas().associate { per -> per.nome to rawTotal(per) }
    }

    fun comprarVantagem(v: Vantagem, onFeedback: (String) -> Unit = {}): Boolean {
        // Special case: Power Points
        val isPowerPoint = v.nome.contains("Pontos de Poder", true) || v.nomeExibicao.contains("Pontos de Poder", true)

        if (isPowerPoint) {
            if (!podeSelecionar(v)) return false
            comprarPontoDePoder(v)
            onFeedback("Vantagem ${v.nome} (Pontos de Poder) adicionada.")
            return true
        }

        // Standard Advantage
        val isFreePathfinder = pathfinderSlotAvailable && isPathfinderEligible(v)
        val isFreeProtagonista = protagonistaSlotAvailable && isProtagonistaEligible(v)
        val isFreeSamuraiCombat = samuraiCombatSlotAvailable && v.categoria == Categoria.COMBATE

        val adaptavelFreeSlot = hasFreeAdaptavelSlotNow(debugSource = "comprarVantagem:${v.id}")
        val isFreeAdaptavel = adaptavelFreeSlot &&
            (v.requisitos.estagio.isBlank() || v.requisitos.estagio.equals("Novato", ignoreCase = true)) &&
            !isVantagemAutomatica(v)

        if (!modoLivre && !isFreePathfinder && !isFreeProtagonista && !isFreeSamuraiCombat && !isFreeAdaptavel && pontosVantagem <= 0) return false // No points

        val rawBeforeArcaneSkillGrant = rawValuesBeforeArcaneSkillGrant(v)

        applyVantagemDinheiro(v)
        checkAndRefundResourcePb()
        adicionarVantagem(v)

        if (isFreeAdaptavel) {
            vantagemAdaptavelSelecionadaId = v.id
            debugLog("AdaptavelDebug", "[comprarVantagem:${v.id}] slot consumido por ${v.nome}")
            onFeedback("Vantagem ${v.nome} adicionada (Vantagem bônus de ${getAdaptavelLabel()}).")
        } else if (isFreePathfinder) {
            pathfinderFreeSlotId = v.id
            onFeedback("Vantagem ${v.nome} adicionada (Vantagem gratuita de Classe).")
        } else if (isFreeProtagonista) {
            vantagensSlotProtagonista.add(v.id)
            onFeedback("Vantagem ${v.nome} adicionada (Vantagem gratuita de Protagonista).")
        } else if (isFreeSamuraiCombat) {
            samuraiCombatSlotIds.add(v.id)
            onFeedback("Vantagem ${v.nome} adicionada (Vantagem de Combate gratuita de Samurai).")
        } else {
            pontosVantagem--
            onFeedback("Vantagem ${v.nome} adicionada.")
        }

        val enforcePoolLimit = !v.isBrutamontes()
        rebuildAllPericiaStacks(
            enforcePoolLimit = enforcePoolLimit,
            desiredRawValues = rawBeforeArcaneSkillGrant
        )

        return true
    }

    fun venderVantagem(v: Vantagem, onFeedback: (String) -> Unit = {}) {
        val isPowerPoint = v.nome.contains("Pontos de Poder", true) || v.nomeExibicao.contains("Pontos de Poder", true)

        if (isPowerPoint) {
            removerPontosDePoder(v)
            pontosVantagem++
            rebuildAllPericiaStacks(enforcePoolLimit = true)
            onFeedback("Vantagem ${v.nome} removida.")
            return
        }

        if (v.id in vantagensAutomaticasDoProtagonista) {
            onFeedback("Vantagem automática do Protagonista (use a rolagem do tropo para alterar).")
            return
        }

        // Standard Advantage
        val wasPathfinderSlot = v.id == pathfinderFreeSlotId
        val wasProtagonistaEligible = v.id in vantagensSlotProtagonista
        val wasSamuraiEligible = v.id in samuraiCombatSlotIds
        val wasAdaptavelSlot = v.id == vantagemAdaptavelSelecionadaId

        removeVantagemDinheiro(v)
        removerVantagem(v)

        var shouldRefund = true

        if (wasAdaptavelSlot) {
            vantagemAdaptavelSelecionadaId = null
            shouldRefund = false
        } else if (wasPathfinderSlot) {
            pathfinderFreeSlotId = null
            shouldRefund = false
        }

        if (wasProtagonistaEligible && compendioArteDaGuerraAtivo) {
            vantagensSlotProtagonista.remove(v.id)
            shouldRefund = false
        }
        if (wasSamuraiEligible && compendioArteDaGuerraAtivo) {
            samuraiCombatSlotIds.remove(v.id)
            shouldRefund = false
        }

        if (shouldRefund) {
            pontosVantagem++
        }

        if (v.id == "o_melhor_que_ha") {
            poderFavoritoId = null
        }

        val enforcePoolLimit = !v.isBrutamontes()
        rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
        onFeedback(if (shouldRefund) "Vantagem removida (+1 PV)." else "Vantagem gratuita removida.")
    }

    fun adicionarVantagem(v: Vantagem) {
        vantagensSelecionadas.add(v)
        ensurePowerSlotsFor(v)

        if (v.id == "escolhido") {
            val inimigo = listaComplicacoes.firstOrNull { it.id == "inimigo" }
            if (inimigo != null) {
                if (complicacoesSelecionadas.keys.none { it.id == "inimigo" }) {
                    complicacoesSelecionadas[inimigo] = "Maior"
                    desvantagensAutomaticas.add(inimigo.name)
                    if (!anotacoes.contains("Inimigo (Maior) adicionado por Escolhido")) {
                        anotacoes += "\n• Inimigo (Maior) adicionado automaticamente pela Vantagem Escolhido."
                    }
                }
            }

        }
    }

    private fun ensurePowerSlotsFor(v: Vantagem) {
        v.toArcanoKey()?.let { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()

            // Initialize slots for ANY Arcane Background, not just fixed ones
            val slots = poderSlotsPorArcano.getOrPut(arcKey) {
                val count = getSlotsCountForArcano(arcKey)
                mutableStateListOf<String?>().apply { repeat(count) { add(null) } }
            }

            val effectiveKey = if (arcKey == "MISTICO") {
                getEffectiveKeyForMistico(v)
            } else {
                arcKey
            }

            fixedPowersByArcano[effectiveKey]?.let { fixedList ->
                // Ensure slots size
                // Fix: Mystic Powers might have more fixed powers than the default slot count
                val requiredSize = getEffectiveSlotsCountForArcano(arcKey)
                while (slots.size < requiredSize) slots.add(null)

                fixedList.forEachIndexed { index, powerId ->
                    if (index < slots.size) {
                        slots[index] = powerId
                    }
                }
            }

            // Transmorfos (Changeling) Logic: Fixed 'Disfarce' power in the first slot
            if (ancestralidade.keyify() == "TRANSMORFOS" && arcKey.normAAKey() == "DOM") {
                if (slots.size > 0) {
                    slots[0] = "disfarce"
                }
            }

            syncPoderesSelecionadosFromSlots()
        }
    }

    fun removerVantagem(v: Vantagem) {
        vantagensSelecionadas.remove(v)

        // Safety check for Mystic Powers cleanup
        if (v.nome.normAAKey().contains("PODERES MISTICOS")) {
            val anyMystic = vantagensSelecionadas.any { it.nome.normAAKey().contains("PODERES MISTICOS") }
            if (!anyMystic) {
                poderSlotsPorArcano.remove("MISTICO")
                novosPoderesStacksPorArcano.remove("MISTICO")
                syncPoderesSelecionadosFromSlots()
            }
        }

        val arcKey = v.toArcanoKey()?.normAAKey()
        if (arcKey != null) {
            val remainingWithSameKey = vantagensSelecionadas.any { it.toArcanoKey()?.normAAKey() == arcKey }
            if (!remainingWithSameKey) {
                poderSlotsPorArcano.remove(arcKey)
                novosPoderesStacksPorArcano.remove(arcKey)
                syncPoderesSelecionadosFromSlots()
            }
        }

        if (v.nome.keyify() == "CAVALEIRO") {
            equipamentosComprados.removeAll { it.origemGrant == "CAVALEIRO" }
        }

        if (v.id == "escolhido") {
            val inimigo = listaComplicacoes.firstOrNull { it.id == "inimigo" }
            if (inimigo != null) {
                // Remove apenas se estiver marcado como automático
                if (desvantagensAutomaticas.contains(inimigo.name)) {
                    complicacoesSelecionadas.remove(inimigo)
                    desvantagensAutomaticas.remove(inimigo.name)

                    // Remove a anotação se presente
                    val note = "\n• Inimigo (Maior) adicionado automaticamente pela Vantagem Escolhido."
                    if (anotacoes.contains(note)) {
                        anotacoes = anotacoes.replace(note, "")
                    }
                }
            }
        }
    }

    fun removerVantagemPorSuper(v: Vantagem) {
        vantagensSelecionadas.remove(v)
        vantagensDePoder.remove(v.id)
    }

    fun adicionarComplicacao(comp: Complicacao, nivel: String) {
        if (comp.id.keyify() == "CEGO") {
            pontosVantagem += 1
        }
        complicacoesSelecionadas[comp] = nivel
    }

    fun removerComplicacao(comp: Complicacao, onFeedback: (String) -> Unit = {}) {
        val key = comp.id.keyify()

        if (key == "IDOSO") {
             if (pontosPericia < 5) {
                 onFeedback("Não é possível remover Idoso pois os pontos de perícia extras já foram gastos. Remova pontos em perícias de Astúcia primeiro.")
                 return
             }
        }

        if (key == "CEGO") {
            if (pontosVantagem > 0) {
                pontosVantagem -= 1
            } else {
               // Try to remove a purchased advantage to balance
               val removed = removerUltimaVantagemCompradaComPv()
               if (!removed) {
                   onFeedback("Não é possível remover Cego pois o Ponto de Vantagem extra já foi gasto.")
                   return
               }
            }
        }
        complicacoesSelecionadas.remove(comp)
    }

    fun adicionarVantagemPorSuper(v: Vantagem): Boolean {
        if (v.categoria == Categoria.LENDARIAS) return false

        val progressoAnterior = overrideStageForVantagem
        overrideStageForVantagem = "Lendário"

        val permitido = podeSelecionar(v)
        overrideStageForVantagem = progressoAnterior

        if (!permitido) return false

        if (!vantagensSelecionadas.contains(v)) {
            vantagensSelecionadas += v
            vantagensDePoder += v.id
            return true
        }
        return false
    }

    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        if (raw <= 0 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        return raw
    }

    fun atributoRawComSupers(attrKey: String): Int {
        return valoresAtributos[attrKey]?.intValue ?: 4
    }

    /** Respeita o teto de mitigação por supers (clampa apenas a soma dos componentes de supers) */
    /** Facilita adicionar/remover efeitos de um PoderId no ledger */
    fun registrarGastoDePoder(poderId: String, custo: Int) {
        val atual = gastosPorPoder[poderId] ?: 0
        gastosPorPoder[poderId] = atual + custo
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun desfazerGastoDePoder(poderId: String, custo: Int) {
        val atual = (gastosPorPoder[poderId] ?: 0) - custo
        if (atual <= 0) gastosPorPoder.remove(poderId) else gastosPorPoder[poderId] = atual
        superPontosDisponiveis = (superPontosTotais - gastosPorPoder.values.sum()).coerceAtLeast(0)
    }

    fun updateBonusApararFromPower(value: Int) {
        bonusApararFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusResFromPower(value: Int) {
        bonusResFromPower = value.coerceAtLeast(0)
    }

    fun updateArmorFromPower(value: Int) {
        armorFromPower = value.coerceAtLeast(0)
    }

    fun updateBonusMovimentacaoFromPower(value: Int) {
        bonusMovimentacaoFromPower = value.coerceAtLeast(0)
    }

    fun rawTotalComSupers(per: Pericia): Int {
        val base = rawTotal(per)
        val incs = superInvestments
            .map { it.effect }
            .filterIsInstance<PowerEffect.SuperPericia>()
            .filter { it.periciaKey.keyify() == per.nome.keyify() }
            .sumOf { it.steps }
        return applySuperStepsFrom(base, incs)
    }

    var regraMultiplosIdiomas by mutableStateOf(false)

    private val idiomaSlotRegex = Regex("^Idiomas\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    private val idiomasExtras = mutableStateListOf<Pericia>()

    // PROMPT 5: Jutsu Skill Logic (Arte da Guerra)
    private val jutsuSlotRegex = Regex("^Jutsu\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    private val jutsuExtras = mutableStateListOf<Pericia>()

    // Computed property for basic filtering before injecting dynamic slots (Idioms, Jutsu)
    val periciasFiltradasPorCompendio: List<Pericia> by derivedStateOf {
            val activeOrigins = getActiveOrigins()
            val filteredByOrigin = listaPericias.filter {
                val o = (it.origem ?: "").ifBlank { "BASICO" }.uppercase().semAcentos()
                o in activeOrigins
            }

            // Unify duplicates by Key, choosing the most relevant source
            val unifiedList = filteredByOrigin.groupBy { it.nome.keyify() }
                .mapNotNull { (_, group) ->
                    group.maxByOrNull { getOriginPriority(it.origem) }
                }

            if (compendioPathfinderAtivo) {
                // If Pathfinder active:
                val forbiddenIds = setOf("ELETRONICA", "FOCO", "HACKEAR", "PSIONICOS", "IDIOMAS")
                unifiedList.filter { per ->
                    val key = per.nome.keyify()
                    key !in forbiddenIds && per.origem != "ARTE_DA_GUERRA"
                }
            } else if (compendioArteDaGuerraAtivo) {
                // If AdG active:
                // 1. Remove standard skills that don't exist in AdG
                val forbiddenIds = setOf(
                    "HACKEAR", "FE", "ELETRONICA", "CIENCIA ESTRANHA", "PSIONICOS", "CONJURAR", "CIENCIA_ESTRANHA"
                )

                unifiedList.filter { per ->
                    val key = per.nome.keyify()

                    // Exclude specific forbidden skills
                    if (key in forbiddenIds) return@filter false

                    // Special case for FOCO:
                    // If we have an AdG version (origem="ARTE_DA_GUERRA"), use that.
                    // If it's the standard Foco (no origem or BASICO), exclude it IF we have an AdG replacement.
                    // Actually, simpler: if per.nome is FOCO, only keep if it is the AdG version.
                    if (key == "FOCO") {
                        per.origem == "ARTE_DA_GUERRA"
                    } else if (key == "TRANSICAO") {
                        // Transição is exclusive to Elementalista trope
                        tropoSelecionado?.id == "tropo_elementalista"
                    } else {
                        // Keep other AdG skills
                        if (per.origem == "ARTE_DA_GUERRA") return@filter true
                        // Keep standard skills (unless forbidden above or handled by replacement)
                        true
                    }
                }
            } else if (compendioWiseguysAtivo) {
                // If Wiseguys active:
                // Filter out specific arcane skills
                val forbiddenForWiseguys = setOf(
                    "FE", "PSIONICOS", "CIENCIA ESTRANHA", "CONJURAR", "CIENCIA_ESTRANHA", "OCULTISMO", "FOCO"
                )

                val filtered = unifiedList.filter { per ->
                    val key = per.nome.keyify()
                    if (key in forbiddenForWiseguys) return@filter false
                    // Also filter AdG skills if AdG is not active
                    per.origem != "ARTE_DA_GUERRA"
                }

                filtered
                    .distinctBy { it.nome.keyify() }
                    .sortedBy { it.nome }
            } else {
                // If neither AdG nor Wiseguys specific filtering is active:
                // Hide any skill marked with ARTE_DA_GUERRA
                unifiedList.filter { per ->
                    per.origem != "ARTE_DA_GUERRA"
                }.sortedBy { it.nome }
            }
        }

    fun isPericiaBasicaEfetiva(per: Pericia): Boolean {
        if (!per.basica) return false
        if (compendioFantasiaAtivo && ancestralidade.keyify() == "GOLENS") {
            val key = per.nome.keyify()
            if (key == "CONHECIMENTO GERAL" || key == "PERSUADIR" || key == "FURTIVIDADE") {
                return false
            }
        }
        if (compendioSciFiAtivo && ancestralidade.keyify() == "ROBOS" && resolveCurrentSciFiVariantSelection() == "Limitado") {
            return false // Removes d4 from all basic skills
        }
        return true
    }

    fun periciaStartRaw(anc: String, per: Pericia): Int =
        periciaStartRawInternal(anc, per, includeArcaneVantage = { true })

    private fun periciaStartRawInternal(
        anc: String,
        per: Pericia,
        includeArcaneVantage: ((Vantagem) -> Boolean)?
    ): Int {
        val ancKey = anc.keyify()
        val perKey = per.nome.keyify()

        var defaultBase = 0
        if (per.basica) {
            val isGolemRestricted = compendioFantasiaAtivo && ancKey == "GOLENS" &&
                    (perKey == "CONHECIMENTO GERAL" || perKey == "PERSUADIR" || perKey == "FURTIVIDADE")
            val isRobotLimited = compendioSciFiAtivo && ancKey == "ROBOS" && resolveCurrentSciFiVariantSelection() == "Limitado"

            if (!isGolemRestricted && !isRobotLimited) {
                defaultBase = 4
            }
        }

        val base = racialSkillStartMap[ancKey]?.get(perKey) ?: defaultBase

        var modifiedBase = base

        // Monster Bonus
        getMonstroSelecionado()?.let { monstro ->
            val monsterKey = per.nome.keyify()
            val bonusEntry = monstro.atributos_bonus.entries.firstOrNull {
                it.key.keyify() == monsterKey
            }
            if (bonusEntry != null) {
                // Mapping: 1 -> d4 (4), 2 -> d6 (6), 3 -> d8 (8), etc.
                val steps = bonusEntry.value
                val bonusRaw = when(steps) {
                    1 -> 4
                    2 -> 6
                    3 -> 8
                    4 -> 10
                    5 -> 12
                    else -> 4
                }
                modifiedBase = maxOf(modifiedBase, bonusRaw)
            }
        }

        // Arte da Guerra - Signos (only for Humans)
        if (compendioArteDaGuerraAtivo && ancKey.contains("HUMANO")) {
            val sign = signoAdgSelecionado
            if (sign != null) {
                // Lebre: Cura d6
                if (sign.equals("Lebre", ignoreCase = true) && perKey == "CURAR") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                // Garça: Acrobacia d4, Atletismo +1 die type (from base)
                if (sign.equals("Garça", ignoreCase = true)) {
                    if (perKey == "ACROBACIA") modifiedBase = maxOf(modifiedBase, 4)
                    if (perKey == "ATLETISMO") modifiedBase = maxOf(modifiedBase, 6) // Base d4 -> d6
                }
                // Serpente: Jogar OR Performance d6
                if (sign.equals("Serpente", ignoreCase = true)) {
                    val chosen = signoSerpentePericiaEscolhida.keyify()
                    if (perKey == chosen) {
                        modifiedBase = maxOf(modifiedBase, 6)
                    }
                }
                // Macaco: Unskilled d4+1 (Not represented in start raw)
            }
        }

        // Fantasia - Pacotes Culturais (only for Fantasy Humans)
        if (isHumanoFantasiaSelecionado()) {
            when (pacoteCulturalFantasiaSelecionado) {
                "Nômades do Deserto" -> {
                    if (perKey == "SOBREVIVENCIA") modifiedBase = maxOf(modifiedBase, 6)
                }
                "Povo do Mar" -> {
                    if (perKey == "ATLETISMO" || perKey == "NAVEGAR") {
                        modifiedBase = maxOf(modifiedBase, 6)
                    }
                }
                "Senhores dos Cavalos" -> {
                    if (perKey == "CAVALGAR") modifiedBase = maxOf(modifiedBase, 6)
                }
            }
        }

        // Arte da Guerra - Protagonista
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_protagonista") {
            val pericias = protagonistaPericiasDoTropo()
            if (perKey in pericias) {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        // Arte da Guerra - Bu Xista
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_buxista") {
            if (perKey == "CONVENCAO" || perKey == "OCULTISMO") {
                modifiedBase = if (modifiedBase > 0) {
                    maxOf(modifiedBase, applySuperStepsFrom(modifiedBase, 1))
                } else {
                    maxOf(modifiedBase, 4)
                }
            }
        }

        // Arte da Guerra - Samurai
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_samurai") {
            val samuraiChoice = samuraiPericiaEscolhida?.keyify()
            val isJutsuChoice = samuraiChoice == "JUTSU"
            val chosenKey = if (isJutsuChoice) "LUTAR" else samuraiChoice
            if (chosenKey != null && perKey == chosenKey) {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        // Arte da Guerra - Youxia (Kensai)
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_youxia") {
            if (perKey == "LUTAR" && !youxiaJutsuSelecionado.isNullOrBlank()) {
                modifiedBase = maxOf(modifiedBase, 4)
            }
        }

        // Arte da Guerra - Artista Marcial (Jutsu inicial)
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_artista_marcial") {
            val slotIndex = jutsuSlotIndex(per)
            if (slotIndex != null) {
                when (artistaMarcialJutsuOpcao) {
                    ARTISTA_MARCIAL_JUTSU_D6 -> if (slotIndex == 1) {
                        modifiedBase = maxOf(modifiedBase, 6)
                    }
                    ARTISTA_MARCIAL_JUTSU_D4_D4 -> if (slotIndex == 2) {
                        modifiedBase = maxOf(modifiedBase, 4)
                    }
                }
            }
        }

        // Arte da Guerra - Tropos
        if (compendioArteDaGuerraAtivo) {
            tropoSelecionado?.let { tropo ->
                val bonusMap = tropo.periciasGratuitas.mapKeys {
                    val k = it.key.keyify()
                    if (k == "JUTSU") "LUTAR" else k
                }
                val bonus = bonusMap[perKey]
                if (bonus != null) {
                    modifiedBase = maxOf(modifiedBase, bonus)
                }
            }
        }

        // Traços que concedem d4/d6 inicial numa perícia à escolha do jogador, ou
        // numa perícia fixa — lidos de habilidades[] (id do traço), não do nome da
        // raça. O traço só decide QUE a raça tem o bônus; qual perícia foi
        // escolhida continua vindo do state dedicado, como antes.
        val habilidadeIdsPericia = (if (anc == ancestralidade) currentAncestryDef else getAncestralidadeDef(anc))
            ?.habilidades
            ?.mapNotNull { it.id?.keyify() }
            ?.toSet()
            ?: emptySet()

        // Gnomo Buscatrilha - Obsessivos (d4 em perícia de Astúcia à escolha)
        if (habilidadeIdsPericia.contains("OBSESSIVOS")) {
            val chosen = gnomoPericiaEscolhida?.keyify()
            if (chosen != null && perKey == chosen) {
                modifiedBase = maxOf(modifiedBase, 4)
            }
        }

        // Kitsunemimi (ADG) - Preparado (d4 em 1 perícia à escolha)
        if (habilidadeIdsPericia.contains("PREPARADO")) {
            val chosen = kitsunemimiPericiaEscolhida?.keyify()
            if (chosen != null && perKey == chosen) {
                modifiedBase = maxOf(modifiedBase, 4)
            }
        }

        if (compendioArteDaGuerraAtivo && ancKey.contains("UMVEE")) {
            // Guarantia base de Sobrevivência d4 para Umvee (não é um traço à
            // parte em habilidades[], é característico da raça em si).
            if (perKey == "SOBREVIVENCIA") {
                modifiedBase = maxOf(modifiedBase, 4)
            }
        }
        // Gatoruja (Dom da Natureza de Umvee OU Feral): Perceber d6 + Ocultismo d4.
        // Antes só funcionava para Umvee porque o código comparava o nome da raça;
        // como Feral compartilha o mesmo Dom da Natureza, ele nunca recebia o
        // bônus mesmo escolhendo Gatoruja. Ler o traço em vez do nome corrige isso
        // para as duas raças automaticamente.
        if (habilidadeIdsPericia.contains("PERCEBER_D6") && perKey == "PERCEBER") {
            modifiedBase = maxOf(modifiedBase, 6)
        }
        if (habilidadeIdsPericia.contains("OCULTISMO_D4") && perKey == "OCULTISMO") {
            modifiedBase = maxOf(modifiedBase, 4)
        }

        // Usagimimi (ADG) - Definido pelo Ofício (d6 em 1 perícia da AdG à escolha)
        if (habilidadeIdsPericia.contains("DEFINIDO_PELO_OFICIO")) {
            val chosen = usagimimiPericiaEscolhida?.keyify()
            if (chosen != null && perKey == chosen) {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        includeArcaneVantage?.let { predicate ->
            modifiedBase = maxOf(modifiedBase, arcaneSkillStartRawFor(per, predicate))
        }

        return modifiedBase
    }

    private fun arcaneSkillStartRawFor(
        per: Pericia,
        includeVantage: (Vantagem) -> Boolean = { true }
    ): Int {
        if (!compendioFantasiaAtivo && !compendioHorrorAtivo && !compendioPathfinderAtivo && !compendioSciFiAtivo) return 0

        val perKey = per.nome.keyify()
        val absVantages = vantagensSelecionadas.filter { includeVantage(it) && it.toArcanoKey() != null }

        val absToConsider = absVantages

        val grantsArcaneSkill = absToConsider.any { vant ->
            val abKey = vant.toArcanoKey()?.normAAKey()
            val info = abKey?.let { arcanoInfo[it] }
            info?.third?.keyify() == perKey
        }

        return if (grantsArcaneSkill) 4 else 0
    }

    private fun rawFromStartAndIncrements(startRaw: Int, increments: Int): Int {
        if (startRaw == 0 && increments == 0) return 0

        val (startForSteps, steps) = if (startRaw == 0) {
            4 to (increments - 1).coerceAtLeast(0)
        } else {
            startRaw to increments.coerceAtLeast(0)
        }

        return applySuperStepsFrom(startForSteps, steps)
    }

    private fun xpArcaneAdvantageIds(): Set<String> {
        val historyIds = advancementHistory
            .filterIsInstance<com.example.swadebuilder.model.AdvancementAction.SpendOnAdvantage>()
            .filter { it.arcanoKey != null }
            .map { it.advantageId }
        val pendingId = advantageForCurrentAdvancement?.takeIf { arcanoEmCompraViaXpKey != null }
        return (historyIds + listOfNotNull(pendingId)).toSet()
    }

    private val _periciasComIdiomas: List<Pericia> by derivedStateOf {
        // Use the filtered list as base instead of raw global listaPericias
        val baseList = periciasFiltradasPorCompendio

        val idiomaBase = idiomaBasePericia()
        if (idiomaBase == null) {
            periciasComJutsu(baseList) // fallback if no idioms
        } else {
            val extrasOrdenados = idiomasExtras.sortedBy { idiomaSlotIndex(it) ?: Int.MAX_VALUE }

            // Combine base list + idioms first
            val listWithIdioms = buildList {
                baseList.forEach { per ->
                    add(per)
                    if (per == idiomaBase) {
                        addAll(extrasOrdenados)
                    }
                }
            }

            // Then inject Jutsu extras
            // Base skill is "Lutar". If Arte da Guerra is active, we treat "Lutar" as "Jutsu".
            // The extras should appear after "Lutar".
            val lutarBase = listWithIdioms.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) }

            if (lutarBase != null && compendioArteDaGuerraAtivo) {
                val jutsuExtrasOrdenados = jutsuExtras.sortedBy { jutsuSlotIndex(it) ?: Int.MAX_VALUE }
                buildList {
                    listWithIdioms.forEach { per ->
                        add(per)
                        if (per == lutarBase) {
                            addAll(jutsuExtrasOrdenados)
                        }
                    }
                }
            } else {
                listWithIdioms
            }
        }
    }

    fun periciasComIdiomas(): List<Pericia> = _periciasComIdiomas

    // Helper used above if Idiomas base is missing (unlikely but safe)
    private fun periciasComJutsu(baseList: List<Pericia> = periciasFiltradasPorCompendio): List<Pericia> {
        val lutarBase = baseList.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) }
            ?: return baseList

        if (!compendioArteDaGuerraAtivo) return baseList

        val jutsuExtrasOrdenados = jutsuExtras.sortedBy { jutsuSlotIndex(it) ?: Int.MAX_VALUE }
        return buildList {
            baseList.forEach { per ->
                add(per)
                if (per == lutarBase) {
                    addAll(jutsuExtrasOrdenados)
                }
            }
        }
    }

    fun isIdiomaPericia(per: Pericia): Boolean =
        per.nome.equals("Idiomas", ignoreCase = true) || idiomaSlotRegex.matches(per.nome)

    fun idiomaSlotIndex(per: Pericia): Int? {
        return if (per.nome.equals("Idiomas", ignoreCase = true)) {
            1
        } else {
            idiomaSlotRegex.find(per.nome)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun idiomaSlotIndexFromName(name: String): Int? {
        return if (name.equals("Idiomas", ignoreCase = true)) {
            1
        } else {
            idiomaSlotRegex.find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    // Jutsu Logic Implementation
    fun isJutsuPericia(per: Pericia): Boolean {
        if (!compendioArteDaGuerraAtivo) return false
        return per.nome.equals("LUTAR", ignoreCase = true) || jutsuSlotRegex.matches(per.nome)
    }

    fun jutsuSlotIndex(per: Pericia): Int? {
        return if (per.nome.equals("LUTAR", ignoreCase = true)) {
            1
        } else {
            jutsuSlotRegex.find(per.nome)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun jutsuSlotIndexFromName(name: String): Int? {
        return if (name.equals("LUTAR", ignoreCase = true)) {
            1
        } else {
            jutsuSlotRegex.find(name)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
    }

    private fun jutsuSlotName(index: Int): String = "Jutsu $index"

    private fun ensureJutsuSlotCount(totalSlots: Int) {
        val base = listaPericias.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) } ?: return
        val desired = totalSlots.coerceAtLeast(1)
        while (jutsuExtras.size < desired - 1) {
             val nextIndex = (jutsuExtras.mapNotNull { jutsuSlotIndex(it) }.maxOrNull() ?: 1) + 1
             val novo = Pericia(nome = jutsuSlotName(nextIndex), atributo = base.atributo, basica = false) // Extras aren't basic
             jutsuExtras.add(novo)
             ensurePericiaEntry(novo)
        }
    }

    private fun trimJutsuSlots(desiredSlots: Int) {
        val desired = desiredSlots.coerceAtLeast(1)
        while (jutsuExtras.size > desired - 1) {
            val ultimo = jutsuExtras.maxByOrNull { jutsuSlotIndex(it) ?: 0 } ?: break
            if (rawTotal(ultimo) > 0 || compIncsPorPericia.getValue(ultimo) > 0) break
            jutsuExtras.remove(ultimo)
            baseIncsPorPericia.remove(ultimo)
            compIncsPorPericia.remove(ultimo)
            compCostStackPorPericia.remove(ultimo)
            spCostStackPorPericia.remove(ultimo)
            notasPericia.remove(ultimo.nome)
        }
    }

    fun syncJutsuSlots() {
        if (!compendioArteDaGuerraAtivo) {
            return
        }
        val lutarBase = listaPericias.firstOrNull { it.nome.equals("LUTAR", ignoreCase = true) } ?: return
        ensurePericiaEntry(lutarBase)

        val allJutsu = listOf(lutarBase) + jutsuExtras
        val jutsusComprados = allJutsu.count { rawTotal(it) > 0 }

        val desiredSlots = if (jutsusComprados > 0) jutsusComprados + 1 else 1

        ensureJutsuSlotCount(desiredSlots)
        trimJutsuSlots(desiredSlots)
    }

    private fun ensureJutsuSlotsFromSnapshot(keys: Set<String>) {
        val maxIndex = keys.mapNotNull { jutsuSlotIndexFromName(it) }.maxOrNull() ?: 1
        if (maxIndex > 1) {
            ensureJutsuSlotCount(maxIndex)
        }
    }


    fun idiomaDefaultLabel(per: Pericia): String {
        val idx = idiomaSlotIndex(per) ?: 1
        return "Idioma $idx"
    }

    private fun idiomaSlotName(index: Int): String = "Idiomas $index"

    private fun idiomaBasePericia(): Pericia? =
        listaPericias.firstOrNull { it.nome.keyify() == "IDIOMAS" }

    private fun idiomaSlotsOrdenados(): List<Pericia> {
        val base = idiomaBasePericia() ?: return emptyList()
        return listOf(base) + idiomasExtras.sortedBy { idiomaSlotIndex(it) ?: Int.MAX_VALUE }
    }

    fun ensureAllPericiasRegistered() {
        listaPericias.forEach { ensurePericiaEntry(it) }
    }

    fun ensurePericiasRegistered(pericias: List<Pericia>) {
        pericias.forEach { ensurePericiaEntry(it) }
    }

    fun ensureAllAtributosRegistered() {
        listaAtributos.forEach { nome ->
            if (!valoresAtributos.containsKey(nome)) {
                valoresAtributos[nome] = mutableIntStateOf(4)
            }
            if (!paCostStackPorAtributo.containsKey(nome)) {
                paCostStackPorAtributo[nome] = mutableListOf()
            }
        }
    }

    private fun ensurePericiaEntry(per: Pericia) {
        if (!baseIncsPorPericia.containsKey(per)) baseIncsPorPericia[per] = 0
        if (!compIncsPorPericia.containsKey(per)) compIncsPorPericia[per] = 0
        if (!compCostStackPorPericia.containsKey(per)) compCostStackPorPericia[per] = mutableListOf()
        if (!spCostStackPorPericia.containsKey(per)) spCostStackPorPericia[per] = mutableStateListOf()
    }

    private fun ensureIdiomaSlotCount(totalSlots: Int) {
        val base = idiomaBasePericia() ?: return
        val desired = totalSlots.coerceAtLeast(1)
        while (idiomasExtras.size < desired - 1) {
            val nextIndex = (idiomasExtras.mapNotNull { idiomaSlotIndex(it) }.maxOrNull() ?: 1) + 1
            val novo = Pericia(nome = idiomaSlotName(nextIndex), atributo = base.atributo, basica = base.basica)
            idiomasExtras.add(novo)
            ensurePericiaEntry(novo)
        }
    }

    private fun trimIdiomaSlots(desiredSlots: Int) {
        val desired = desiredSlots.coerceAtLeast(1)
        // Sort explicitly to remove from end
        while (idiomasExtras.size > desired - 1) {
            // Find candidates for removal (extras)
            val candidates = idiomasExtras.sortedByDescending { idiomaSlotIndex(it) ?: 0 }
            val ultimo = candidates.firstOrNull() ?: break

            // Safety check: if we somehow have points, stop trimming
            if (rawTotal(ultimo) > 0 || (compIncsPorPericia[ultimo] ?: 0) > 0) break

            idiomasExtras.remove(ultimo)
            baseIncsPorPericia.remove(ultimo)
            compIncsPorPericia.remove(ultimo)
            compCostStackPorPericia.remove(ultimo)
            spCostStackPorPericia.remove(ultimo)
            notasPericia.remove(ultimo.nome)
        }
    }

    private fun ensureIdiomaSlotsFromSnapshot(keys: Set<String>) {
        val maxIndex = keys.mapNotNull { idiomaSlotIndexFromName(it) }.maxOrNull() ?: 1
        if (maxIndex > 1) {
            ensureIdiomaSlotCount(maxIndex)
        }
    }

    fun syncIdiomaSlots() {
        val idiomaBase = idiomaBasePericia() ?: return
        ensurePericiaEntry(idiomaBase)
        val idiomasComprados = idiomaSlotsOrdenados().count { rawTotal(it) > 0 }
        val linguistaCount = linguistaLanguageCount()
        val filled = maxOf(linguistaCount, idiomasComprados)
        val desiredSlots = if (filled > 0) filled + 1 else 1
        ensureIdiomaSlotCount(desiredSlots)
        trimIdiomaSlots(desiredSlots)
    }

    private fun linguistaLanguageCount(): Int {
        val hasLinguista = vantagensSelecionadas.any { it.id == "linguista" }
        val astuciaRaw = valoresAtributos["ASTUCIA"]?.intValue ?: 0
        return when {
            regraMultiplosIdiomas && hasLinguista -> astuciaRaw
            regraMultiplosIdiomas -> astuciaRaw / 2
            hasLinguista -> astuciaRaw / 2
            else -> 0
        }
    }

    fun linguistaMinRawFor(pericia: Pericia): Int {
        val count = linguistaLanguageCount()
        if (count <= 0 || !isIdiomaPericia(pericia)) return 0
        val idx = idiomaSlotIndex(pericia) ?: return 0
        return if (idx <= count) 6 else 0
    }

    private fun stepsToReach(per: Pericia, targetRaw: Int): Int {
        var curr = periciaStartRaw(ancestralidade, per)
        var steps = 0
        while (curr < targetRaw) {
            curr = if (curr == 0) 4 else curr + 2
            steps++
        }
        return steps
    }

    private fun syncLinguistaIdiomas() {
        val idiomaBase = idiomaBasePericia() ?: return
        ensurePericiaEntry(idiomaBase)
        val linguistaCount = linguistaLanguageCount()
        if (linguistaCount <= 0) {
            idiomaSlotsOrdenados().forEach { per ->
                compIncsPorPericia[per] = 0
            }
            syncIdiomaSlots()
            return
        }

        ensureIdiomaSlotCount(linguistaCount)
        val slots = idiomaSlotsOrdenados()
        slots.forEachIndexed { index, per ->
            if (index < linguistaCount) {
                val totalSteps = stepsToReach(per, 6)
                val baseSteps = baseIncsPorPericia.getValue(per)
                val freeSteps = (totalSteps - baseSteps).coerceAtLeast(0)
                compIncsPorPericia[per] = freeSteps
                if (notasPericia[per.nome].isNullOrBlank()) {
                    notasPericia[per.nome] = idiomaDefaultLabel(per)
                }
            } else {
                compIncsPorPericia[per] = 0
            }
        }

        syncIdiomaSlots()
    }

    var pvFromXpOutstanding by mutableIntStateOf(0)
    var overrideStageForVantagem by mutableStateOf<String?>(null)
    var openVantagensAfterGrant by mutableStateOf(false)
    var superPoderEmFoco by mutableStateOf<String?>(null)

    var arcanoEmCompraViaXpKey by mutableStateOf<String?>(null)
    var arcanoSnapshotAntesDaCompra: List<String?>? = null

    var ancestralidadeEmFoco by mutableStateOf<String?>(null)

    fun removerSuperPoder(
        poder: SuperInvestment,
        desfazerNoLedger: Boolean = true
    ) {
        if (superInvestments.remove(poder)) {
            if (desfazerNoLedger) {
                desfazerGastoDePoder(poder.powerId, poder.cost)
            }
        }
    }

    fun grantVantagemPointFromXp(stageName: String) {
        pontosVantagem += 1
        pvFromXpOutstanding += 1

        overrideStageForVantagem = stageName

        openVantagensAfterGrant = true
        mostrandoVantagensProgresso = true
    }

    fun grantSkillPointsFromXp() {
        spFromProgress += 2
        mostrandoPericiasProgresso = true
    }


    fun maxComprasPpAteAgora(): Int {
        return listaDeEstagios.indexOf(estagioAtual()) + 1
    }

    private fun selecionarPontosDePoder(v: Vantagem) {
        val estagio = estagioAtual().nome
        val totalFeitas = comprasPpPorEstagio.values.sum()

        if (totalFeitas >= maxComprasPpAteAgora()) return

        val feitasNoEstagio = comprasPpPorEstagio[estagio] ?: 0
        comprasPpPorEstagio[estagio] = feitasNoEstagio + 1

        val ganho = if (totalFeitas < 4) 5 else 2
        bonusPoderExtra += ganho

        vantagensSelecionadas += v
    }

    fun removerPontosDePoder(v: Vantagem, estagioOverride: String? = null) {
        if (!vantagensSelecionadas.remove(v)) return

        val totalAntes = comprasPpPorEstagio.values.sum()
        if (totalAntes == 0) return

        val estagio = estagioOverride ?: estagioAtual().nome
        val feitas = comprasPpPorEstagio[estagio] ?: 0
        if (feitas > 0) {
            comprasPpPorEstagio[estagio] = feitas - 1
        } else {
            val fallback = comprasPpPorEstagio.entries.lastOrNull { it.value > 0 }
            fallback?.let {
                comprasPpPorEstagio[it.key] = it.value - 1
            }
        }

        val ganhoRemovido = if (totalAntes <= 4) 5 else 2
        bonusPoderExtra = (bonusPoderExtra - ganhoRemovido).coerceAtLeast(0)
    }

    private fun removerUltimaVantagemCompradaComPv(): Boolean {
        val autoKeys = (vantagensAutomaticas + vantagensRaciais)
            .map { normalizeAutoKey(it.substringBefore("(").trim()) }
            .toSet()
        val autoIds = vantagensAutomaticasDoTropo.toSet()

        val candidate = vantagensSelecionadas
            .asReversed()
            .firstOrNull { vant ->
                val key = normalizeAutoKey(vant.nome.substringBefore("(").trim())
                key !in autoKeys && vant.id !in autoIds
            }
            ?: return false

        removeVantagemDinheiro(candidate)
        removerVantagem(candidate)
        if (candidate.id == "o_melhor_que_ha") {
            poderFavoritoId = null
        }

        rebuildAllPericiaStacks()
        return true
    }

    fun comprarPontoDePoder(v: Vantagem) {
        if (!podeSelecionar(v)) return
        selecionarPontosDePoder(v)
        vantagensSelecionadas += v
    }

    val comprasAttrPorEstagio = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    fun identifyMaxedTraits() {
        _maxedTraits.clear()

        listaAtributos.forEach { attrKey ->
            val current    = valoresAtributos[attrKey]?.intValue ?: return@forEach
            val maxAllowed = atributoMaxRaw(attrKey)
            if (current == maxAllowed) {
                _maxedTraits.add(attrKey)
            }
        }

        periciasComIdiomas().forEach { per ->
            val current    = rawTotal(per)
            val maxAllowed = periciaCapRaw(per)
            if (current == maxAllowed) {
                _maxedTraits.add(per.nome.keyify())
            }
        }
    }

    private fun getBaseWealth(): Int {
        return if (compendioFantasiaAtivo) 300 else 500
    }

    fun applyVantagemDinheiro(v: Vantagem) {
        if (usaRiqueza) return
        val nomeKey = v.nome.trim().keyify()
        val id = v.id.keyify()

        val base = getBaseWealth()

        val amount = when {
            id == "RICO" || nomeKey == "RICO" -> {
                if (compendioArteDaGuerraAtivo) 2000 else (2 * base)
            }
            id == "PODRE_DE_RICO" || nomeKey == "PODRE DE RICO" -> {
                val hasRico = vantagensSelecionadas.any { it.nome.keyify() == "RICO" || it.id.keyify() == "RICO" }
                if (hasRico) (2 * base) else (4 * base)
            }
            else -> 0
        }
        dinheiro += amount
    }

    fun removeVantagemDinheiro(vant: Vantagem) {
        if (usaRiqueza) return
        val key = vant.nome.trim().keyify()
        val id = vant.id.keyify()
        val base = getBaseWealth()

        val amount = when {
            id == "RICO" || key == "RICO" -> {
                if (compendioArteDaGuerraAtivo) 2000 else (2 * base)
            }
            id == "PODRE_DE_RICO" || key == "PODRE DE RICO" -> {
                 val hasRico = vantagensSelecionadas.any {
                     val ok = it.nome.keyify() == "RICO" || it.id.keyify() == "RICO"
                     ok && it != vant
                 }
                 if (hasRico) (2 * base) else (4 * base)
            }
            else -> 0
        }
        if (amount <= 0) return

        while (dinheiro < amount && equipamentosComprados.isNotEmpty()) {
            val eq = equipamentosComprados.removeAt(equipamentosComprados.lastIndex)
            val custo = (eq.custo as? JsonPrimitive)
                ?.content
                ?.toIntOrNull()
                ?: 0
            dinheiro += custo
        }

        dinheiro = (dinheiro - amount).coerceAtLeast(0)
    }

    val minAttrPorVantagem by derivedStateOf {
        val resultado = mutableMapOf<String, Int>()
        vantagensSelecionadas.forEach { vant ->
            vant.requisitos.atributoMin.forEach { (atributo, valorMin) ->
                val atual = resultado[atributo]
                if (atual == null || valorMin > atual) {
                    resultado[atributo] = valorMin
                }
            }
        }
        resultado.toMap()
    }

    val minPericiaPorVantagem: Map<Pericia, Int> by derivedStateOf {
        vantagensSelecionadas.flatMap { vant ->
            val obrigatorias = vant.requisitos.periciaMin   // se for null, vira um Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    getBestPericia(nomeRaw)
                        ?.let { per -> per to min }
                }

            val opcionais = vant.requisitos.periciaMinOpcional   // se null, vira Map vazio
                .mapNotNull { (nomeRaw, min) ->
                    getBestPericia(nomeRaw)
                        ?.let { per -> per to min }
                }

            val fav = run {
                val choiceSnapshot = vant.choice
                val isYouxiaAuto =
                    compendioArteDaGuerraAtivo &&
                        tropoSelecionado?.id == "tropo_youxia" &&
                        vant.id == "arma_predileta" &&
                        vantagensAutomaticasDoTropo.contains(vant.id)
                if (
                    vant.nome.trim().equals("Arma Predileta", ignoreCase = true)
                    && choiceSnapshot != null
                    && !isYouxiaAuto
                ) {
                    getBestPericia(choiceSnapshot)
                        ?.let { per -> listOf(per to 8) }
                        .orEmpty()
                } else {
                    emptyList()
                }
            }

            obrigatorias + opcionais + fav
        }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, listaMinimos) ->
                listaMinimos.maxOrNull() ?: 0
            }
    }

    private val incompatibilidades: Map<String, Set<String>> = mapOf(
        "LENTO"   to setOf("LIGEIRO"),
        "LIGEIRO" to setOf("LENTO"),
        "OBESO"      to setOf("MUSCULOSO"),
        "MUSCULOSO"  to setOf("OBESO"),
        "COMP ALMA PENHORADA" to setOf("ANTECEDENTE ARCANO MILAGRES", "AA MILAGRES"),
        "COMP ALMA VENDIDA" to setOf("ANTECEDENTE ARCANO MILAGRES", "AA MILAGRES"),
        "ANTECEDENTE ARCANO MILAGRES" to setOf("COMP ALMA PENHORADA", "COMP ALMA VENDIDA"),
        "AA MILAGRES" to setOf("COMP ALMA PENHORADA", "COMP ALMA VENDIDA"),
        "COMP MALDICAO GREMLIN" to setOf("ANTECEDENTE ARCANO TECNOMAGIA", "AA TECNOMAGIA"),
        "ANTECEDENTE ARCANO TECNOMAGIA" to setOf("COMP MALDICAO GREMLIN"),
        "AA TECNOMAGIA" to setOf("COMP MALDICAO GREMLIN"),
        "COMP TECNOFOBIA" to setOf("TARO ENGENHEIRO", "MESTRE DAS CALDEIRAS", "MECANICO CEGO"),
        "TARO ENGENHEIRO" to setOf("COMP TECNOFOBIA"),
        "MESTRE DAS CALDEIRAS" to setOf("COMP TECNOFOBIA"),
        "MECANICO CEGO" to setOf("COMP TECNOFOBIA"),
        "POBREZA"        to setOf("RICO", "PODRE DE RICO"),
        "RICO"           to setOf("POBREZA"),
        "PODRE DE RICO"  to setOf("POBREZA"),
        "ESCOLHIDO"      to setOf("INIMIGO"),
        "INIMIGO"        to setOf("ESCOLHIDO")
    )

    fun mensagemConflitoParaVantagem(vantagem: Vantagem): String? {
        val keys = setOf(vantagem.nome.keyify(), vantagem.id.keyify())
        val compsConfl = keys.flatMap { incompatibilidades[it].orEmpty() }.toSet()
        if (compsConfl.isEmpty()) return null
        val conflito = complicacoesSelecionadas.keys.firstOrNull { comp ->
            comp.id.keyify() in compsConfl
        }
        return conflito?.let { "Remova ${it.name} para pegar ${vantagem.nome}." }
    }

    fun mensagemConflitoParaComplicacao(complicacao: Complicacao): String? {
        val key = complicacao.id.keyify()
        val vantConfl = incompatibilidades[key] ?: return null
        val conflito = vantagensSelecionadas.firstOrNull { vant ->
            vant.nome.keyify() in vantConfl || vant.id.keyify() in vantConfl
        }
        return conflito?.let { "Remova ${it.nome} para pegar ${complicacao.name}." }
    }

    val poderSlotsPorArcano = mutableStateMapOf<String, SnapshotStateList<String?>>()

    val novosPoderesStacksPorArcano = mutableStateMapOf<String, MutableList<List<String>>>()

    fun registrarNovosPoderes(versionKey: String, escolhas: List<String>) {
        val pilha = novosPoderesStacksPorArcano.getOrPut(versionKey) { mutableListOf() }
        pilha.add(escolhas)
    }

    fun desfazerUltimosNovosPoderes(versionKey: String, initialSlots: Int) {
        val pilha = novosPoderesStacksPorArcano[versionKey] ?: return
        if (pilha.isEmpty()) return

        val ultima = pilha.removeAt(pilha.lastIndex)
        val slots = poderSlotsPorArcano[versionKey] ?: return

        ultima.forEach { poderId ->
            val idx = slots.indexOfLast { it == poderId }
            if (idx >= 0) slots[idx] = null
        }

        val extrasAinda = pilha.sumOf { it.size }
        val tamanhoMinimo = (initialSlots + extrasAinda).coerceAtLeast(initialSlots)

        while (slots.size > tamanhoMinimo && slots.lastOrNull() == null) {
            slots.removeAt(slots.lastIndex)
        }

        syncPoderesSelecionadosFromSlots()
    }

    fun syncPoderesSelecionadosFromSlots() {
        ensureTransmorfoFixedDisguisePower()
        ensureDemonioFixedPower()
        poderesSelecionados.apply {
            clear()
            addAll(poderSlotsPorArcano.values.flatMap { it.filterNotNull() })
        }
    }

    private fun ensureTransmorfoFixedDisguisePower() {
        if (ancestralidade.keyify() != "TRANSMORFOS") return

        val slots = poderSlotsPorArcano.getOrPut("DOM") { mutableStateListOf() }
        val requiredSlots = getEffectiveSlotsCountForArcano("DOM")

        while (slots.size < requiredSlots) {
            slots.add(null)
        }

        if (slots.isEmpty()) {
            slots.add("disfarce")
        } else {
            slots[0] = "disfarce"
        }
    }

    private fun ensureDemonioFixedPower() {
        if (!compendioCidadeSolVaporAtivo || ancestralidade.keyify() != "DEMONIOS") return

        val slots = poderSlotsPorArcano.getOrPut("DEMONIO") { mutableStateListOf() }
        val requiredSlots = getEffectiveSlotsCountForArcano("DEMONIO")

        while (slots.size < requiredSlots) {
            slots.add(null)
        }

        if (slots.isEmpty()) {
            slots.add("disfarce_demoniaco")
        } else {
            slots[0] = "disfarce_demoniaco"
        }
    }

    fun iniciarCompraArcanoViaXp(arcKeyRaw: String) {
        val arcKey = arcKeyRaw.normAAKey()
        arcanoEmCompraViaXpKey = arcKey
        arcanoSnapshotAntesDaCompra = poderSlotsPorArcano[arcKey]?.toList()

        // Usa a contagem dinâmica que inclui Novos Poderes, em vez de apenas o valor inicial
        val totalSlots = getSlotsCountForArcano(arcKey)
        val slots = poderSlotsPorArcano.getOrPut(arcKey) { mutableStateListOf() }
        while (slots.size < totalSlots) { slots.add(null) }

        mostrandoPoderesProgresso = true
    }

    fun restoreArcanoSlots(arcKey: String, snapshot: List<String?>?) {
        if (snapshot == null) {
            poderSlotsPorArcano.remove(arcKey)
            novosPoderesStacksPorArcano.remove(arcKey)
        } else {
            poderSlotsPorArcano[arcKey] = mutableStateListOf<String?>().apply { addAll(snapshot) }
        }
        syncPoderesSelecionadosFromSlots()
    }

    fun limparCompraArcanoViaXp(restaurarSnapshot: Boolean) {
        arcanoEmCompraViaXpKey?.let { arcKey ->
            if (restaurarSnapshot) {
                restoreArcanoSlots(arcKey, arcanoSnapshotAntesDaCompra)
            }
        }
        arcanoEmCompraViaXpKey = null
        arcanoSnapshotAntesDaCompra = null
        mostrandoPoderesProgresso = false
    }

    fun getSlotsCountForArcano(arcKey: String): Int {
        val arcKeyNorm = arcKey.normAAKey()
        if (usaPoderesDisponiveisPorEstagio(arcKeyNorm)) return 0
        val isCidadeSolVaporDemonAncestry =
            compendioCidadeSolVaporAtivo &&
                ancestralidade.keyify().contains("DEMONIOS") &&
                arcKeyNorm == "DEMONIO"
        val hasArcanoVantagem = vantagensSelecionadas.any { it.toArcanoKey()?.normAAKey() == arcKeyNorm }
        val usaTecnicasTropo = compendioArteDaGuerraAtivo &&
            arcKeyNorm == "MESTRE DO CHI" &&
            !hasArcanoVantagem &&
            (tropoSelecionado?.tecnicasIniciais ?: 0) > 0
        // Todos os 45 Antecedentes Arcanos oficiais têm entrada em geral_arcano_info.json,
        // então esse "?: 3" só é alcançado por um Antecedente Arcano Customizado (categoria
        // "Antecedente Arcano" no criador de conteúdo) sem entrada própria — 3 poderes
        // iniciais é a regra padrão do livro básico pra Antecedente Arcano, então serve de
        // fallback razoável em vez de deixar a vantagem customizada sem nenhum poder pra
        // escolher.
        val base = if (usaTecnicasTropo) 0 else (arcanoInfo[arcKeyNorm]?.first ?: 3)
        var bonusSlots = 0

        vantagensSelecionadas
            .filter { it.id == "novos_poderes" }
            .forEach { vant ->
                val choice = vant.choice
                if (choice.isNullOrBlank()) {
                    // Legacy or Single AB: counts for this one if it's the only one or default
                    // If we have multiple ABs and blank choice, it might be ambiguous,
                    // but usually means it belongs to the primary/only one.
                    // To be safe, if blank, we assume it adds +2 if this is the only AB?
                    // Or we let the new logic handle it.
                    // For now, if blank, +2 (Standard behavior)
                    bonusSlots += 2
                } else {
                    if (choice.contains("&")) {
                        // Split logic: "Key1 & Key2"
                        // Normalize each part individually
                        if (choice.split("&").any { it.normAAKey() == arcKeyNorm }) {
                            bonusSlots += 1
                        }
                    } else {
                        // Single target
                        if (choice.normAAKey() == arcKeyNorm) {
                            bonusSlots += 2
                        }
                    }
                }
            }

        val bonusTecnicas = if (arcKeyNorm == "MESTRE DO CHI") tecnicasIniciaisFromTropo else 0
        val totalSlots = base + bonusSlots + bonusTecnicas
        return if (isCidadeSolVaporDemonAncestry) maxOf(totalSlots, 4) else totalSlots
    }

    fun getEffectiveSlotsCountForArcano(arcKey: String): Int {
        if (usaPoderesDisponiveisPorEstagio(arcKey)) return 0
        val baseCount = getSlotsCountForArcano(arcKey)
        val arcKeyNorm = arcKey.normAAKey()

        val vant = vantagensSelecionadas.find { it.toArcanoKey()?.normAAKey() == arcKeyNorm }

        if (vant != null && arcKeyNorm == "MISTICO") {
            val effectiveKey = getEffectiveKeyForMistico(vant)
            val fixedList = fixedPowersByArcano[effectiveKey]
            if (fixedList != null) {
                return maxOf(baseCount, fixedList.size)
            }
        }
        return baseCount
    }

    fun arcanoCompraPendente(): Boolean {
        val arcKey = arcanoEmCompraViaXpKey ?: return false
        val required = getSlotsCountForArcano(arcKey)
        if (required == 0) return false
        val slots = poderSlotsPorArcano[arcKey] ?: return true
        val filled = slots.count { it != null }
        return filled < required
    }

    var permiteMultiAntecedenteArcano by mutableStateOf(false)
    var usarEspecializacoesDePericia by mutableStateOf(false)

    val especializacoesPorPericia: SnapshotStateMap<String, com.example.swadebuilder.model.EspecializacoesDto> = mutableStateMapOf()

    var bonusPoderExtra by mutableIntStateOf(0)

    var obesoBonusSize by mutableIntStateOf(0)
    var obesoMalusMov by mutableIntStateOf(0)

    var idosoBonusSp by mutableIntStateOf(0)

    var jovemAutoPequeno by mutableStateOf(false)

    private var jovemMalusPa by mutableIntStateOf(0)
    private var jovemMalusSp by mutableIntStateOf(0)

    fun syncFromCPRefund(pa: Boolean = false, sp: Boolean = false, feedbackMessages: MutableList<String>) {
        if (pa) recalcularPontosAtributo(feedbackMessages)
        if (sp) rebuildAllPericiaStacks(feedbackMessages)
    }

    val cpPaStack       = mutableStateListOf<String>()  // você já trocou pra add("PB")
    var paFromProgress by mutableIntStateOf(0)
    var legendaryAttrReservations by mutableIntStateOf(0)
    val cpSpStack       = mutableStateListOf<Unit>()
    var spFromProgress by mutableIntStateOf(0)
    val cpPvStack       = mutableStateListOf<Unit>()
    val cpRecursosStack = mutableStateListOf<Unit>()

    val totalSpPool: Int
        get() {
            // PROMPT: Arte da Guerra skill points adjustment
            if (compendioArteDaGuerraAtivo) {
                // If AdG active:
                // Base: 12 points
                // Humans with "Nenhum" sign: +3 points (15 total)
                // Ignore "maisPontosPericias" checkbox
                val isHuman = ancestralidade.keyify().contains("HUMANO")
                val base = 12 + if (isHuman && signoAdgSelecionado.equals("Nenhum", ignoreCase = true)) 3 else 0
                return (base + cpSpStack.size + spFromProgress + idosoBonusSp - jovemMalusSp).coerceAtLeast(0)
            } else {
                // Standard Logic
                val base = if (maisPontosPericias) BASE_SP_POOL else (BASE_SP_POOL - 3)
                return (base + cpSpStack.size + spFromProgress + idosoBonusSp - jovemMalusSp)
                    .coerceAtLeast(0)
            }
        }

    val pontosPericia by derivedStateOf {
        val used = spCostStackPorPericia.values.sumOf { it.sum() } +
                compCostStackPorPericia.values.sumOf { it.sum() }
        totalSpPool - used
    }

    val tecnicasIniciaisFromTropo by derivedStateOf {
        val base = tropoSelecionado?.tecnicasIniciais ?: 0
        var total = if (tropoSelecionado?.id == "tropo_protagonista") {
            base + tecnicasIniciaisProtagonista()
        } else {
            base
        }
        if (tropoSelecionado?.id == "tropo_shinobi" && shinobiTalentoSelecionado?.keyify() == "MISTICO") {
            total += 1
        }
        total
    }

    val reservaChi by derivedStateOf {
        // PROMPT: Chi = 2 + (Spirit/2) + bonuses
        val espiritoRaw = valoresAtributos["ESPIRITO"]?.intValue ?: 0
        val racialPenalty = if (ancestralidade.keyify() == "TERRACOTA") 1 else 0
        val bonusFromChiEdges = vantagensSelecionadas.count { it.categoria == Categoria.CHI }
        val bonusFromTropo = if (compendioArteDaGuerraAtivo) tecnicasIniciaisFromTropo else 0
        val bonusFromSign = if (compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO") && signoAdgSelecionado?.equals("Kirin", ignoreCase = true) == true) 1 else 0

        // Base 2 added as requested
        val baseChi = if (compendioArteDaGuerraAtivo) 2 else 0

        (baseChi + espiritoRaw / 2 - racialPenalty + bonusFromChiEdges + bonusFromTropo + bonusFromSign).coerceAtLeast(0)
    }

    var nomePersonagem by mutableStateOf("")

    var progresso by mutableIntStateOf(0)
    fun estagioAtual(): Estagio {
        return listaDeEstagios.first { progresso in it.minProgress .. it.maxProgress }
    }

    private fun Vantagem.isStageBasedArcanoVariant(key: String): Boolean {
        val normalizedKey = key.normAAKey()
        val origin = canonicalOriginKey(origem)
        val isCidadeSolVaporDemonAncestry =
            compendioCidadeSolVaporAtivo && ancestralidade.keyify().contains("DEMONIOS")
        return when (normalizedKey) {
            "FEITICEIRO" -> id == "aa_magia_negra"
            "DEMONIO" -> id == "aa_demonio" && !isCidadeSolVaporDemonAncestry
            "MILAGRES" -> {
                id == "aa_milagres" ||
                    (id == "antecedente_arcano_milagres" && origin == "CIDADE_SOL_VAPOR")
            }
            else -> false
        }
    }

    fun usaPoderesDisponiveisPorEstagio(arcKey: String): Boolean {
        val key = arcKey.normAAKey()
        return vantagensSelecionadas.any { vantagem ->
            vantagem.toArcanoKey()?.normAAKey() == key && vantagem.isStageBasedArcanoVariant(key)
        }
    }

    fun bloqueiaNovosPoderesPorAntecedente(): Boolean =
        vantagensSelecionadas.any {
            when (it.toArcanoKey()?.normAAKey()) {
                "FEITICEIRO" -> usaPoderesDisponiveisPorEstagio("FEITICEIRO")
                "DEMONIO" -> usaPoderesDisponiveisPorEstagio("DEMONIO")
                "MILAGRES" -> usaPoderesDisponiveisPorEstagio("MILAGRES")
                else -> false
            }
        }

    fun estagioAtinge(estagioNome: String): Boolean {
        val atualIdx = listaDeEstagios.indexOf(estagioAtual())
        val requeridoIdx = listaDeEstagios.indexOfFirst { it.nome.equals(estagioNome, ignoreCase = true) }
        return requeridoIdx >= 0 && atualIdx >= requeridoIdx
    }

    fun poderesDisponiveisPorEstagioParaArcano(arcKey: String): Map<String, String> {
        val key = arcKey.normAAKey()
        if (!usaPoderesDisponiveisPorEstagio(key)) return emptyMap()
        return ArcaneConfig.getStageBasedPowersByStage(key)
    }

    fun requisitoEspecialDePoderPorArcano(arcKey: String, powerId: String): String? {
        val key = arcKey.normAAKey()
        if (!usaPoderesDisponiveisPorEstagio(key)) return null
        return ArcaneConfig.getStageBasedPowerRequirement(key, powerId)
    }

    fun atendeRequisitoEspecialDePoderPorArcano(arcKey: String, powerId: String): Boolean {
        val requiredAdvantageId = requisitoEspecialDePoderPorArcano(arcKey, powerId) ?: return true
        return vantagensSelecionadas.any { it.id == requiredAdvantageId }
    }

    private fun effectiveProgressoParaVantagens(): Int {
        val stName = overrideStageForVantagem ?: return progresso
        val st = listaDeEstagios.firstOrNull { it.nome.equals(stName, ignoreCase = true) }
        return st?.minProgress ?: progresso
    }

    private fun currentProgressStageIndex(): Int {
        var firstOpen = -1

        listaDeEstagios.forEachIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            val cap = if (idx < listaDeEstagios.lastIndex) {
                st.maxProgress - prevMax
            } else {
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
            }
            val spentHere = stageXpSpent[st.nome] ?: 0
            if (spentHere < cap && firstOpen == -1) {
                firstOpen = idx
            }
        }

        return if (firstOpen >= 0) firstOpen else listaDeEstagios.lastIndex
    }

    var ancestralidade by mutableStateOf("HUMANOS")
    var celestialAAMilagresDesabilitado by mutableStateOf(false)
    var meioElfoAgil by mutableStateOf(false)
    var meioOrcForca by mutableStateOf(false)

    var tropoSelecionado by mutableStateOf<Tropo?>(null)
    val vantagensAutomaticasDoTropo = mutableStateListOf<String>()
    val vantagensAutomaticasDoProtagonista = mutableStateListOf<String>()
    val vantagensSlotProtagonista = mutableStateListOf<String>()

    val vantagensAutomaticas = mutableStateListOf<String>()
    val vantagensRaciais = mutableStateListOf<String>()
    val desvantagensRaciais = mutableStateListOf<String>()

    var pontosVantagem by mutableIntStateOf(0)

    val desvantagensAutomaticas = mutableStateListOf<String>()

    var frozenAdvantageCount by mutableIntStateOf(0)

    var pontosAtributo by mutableIntStateOf(5)

    var armadura by mutableIntStateOf(0)

    var nasceUmHeroi by mutableStateOf(false)

    val valoresAtributos = mutableStateMapOf<String, androidx.compose.runtime.MutableIntState>().apply {
        listaAtributos.forEach { put(it, mutableIntStateOf(4)) }
    }

    val complicacoesSelecionadas: SnapshotStateMap<Complicacao, String?> = mutableStateMapOf()
    val reservasComplicacaoMaior: SnapshotStateMap<String, Boolean> = mutableStateMapOf()

    private fun effectiveVantagensGratis(rm: RacialModifier): List<String> {
        val fromList = rm.vantagensGratis
        val fromHabilidades = rm.habilidades
            .filter { it.category == "racial_edge" }
            .map { it.id ?: it.nome }
        return fromList + fromHabilidades
    }

    private fun effectiveDesvantagens(rm: RacialModifier): List<String> {
        val fromList = rm.desvantagens
        val fromHabilidades = rm.habilidades
            .filter { it.category == "racial_hindrance" }
            .map {
                val sev = it.severity
                if (sev != null && !it.nome.contains("($sev)", ignoreCase = true)) {
                    "${it.nome} ($sev)"
                } else {
                    it.nome
                }
            }
        return fromList + fromHabilidades
    }

    val pontosComplicacao: Int
        get() {
            val ancestryDef = currentAncestryDef
            val ancestryAuto = ancestryDef?.let { effectiveDesvantagens(it) }.orEmpty()
            val autoKeys = (desvantagensAutomaticas + desvantagensRaciais + ancestryAuto)
                .map { normalizeAutoKey(it.substringBefore("(").trim()) }
                .toSet()

            var total = 0
            var temMaior = false

            for ((comp, tipo) in complicacoesSelecionadas) {
                if (normalizeAutoKey(comp.id) in autoKeys || normalizeAutoKey(comp.name) in autoKeys) continue
                // PROMPT 3: Ignora complicações (Transtornos) ganhos em progresso para cálculo de PC
                if (transtornos.any { it.id == comp.id }) continue

                when (tipo) {
                    "Maior" -> { total += 2; temMaior = true }
                    "Menor" -> { total += 1 }
                }
            }

            val teto = if (grandesResponsabilidades && temMaior) 6 else 4
            return minOf(total, teto)
        }

    val vantagensSelecionadas      = mutableStateListOf<Vantagem>()
    var vantagemAdaptavelSelecionadaId: String? by mutableStateOf(null)

    fun getAdaptavelLabel(): String {
        return if (ancestralidade.keyify() == "GOBLINS" && compendioFantasiaAtivo) "Sobrevivente" else "Adaptável"
    }

    fun temAdaptavel(): Boolean {
        if (isHumanoFantasiaSelecionado() && pacoteCulturalFantasiaSelecionado != "Humano padrão") {
            return false
        }

        val ancDef = currentAncestryDef
        if (ancDef == null) {
            return false
        }

        val free = effectiveVantagensGratis(ancDef)
        // 1. Explicitly in Free Edges (legacy list or racial_edge)
        if (free.any { it.keyify() == "ADAPTAVEL" }) {
            return true
        }

        // 2. Explicit ID or Name in Abilities (e.g. Basic Humans, Guardians)
        if (ancDef.habilidades.any { it.id?.keyify() == "ADAPTAVEL" || it.nome.keyify() == "ADAPTAVEL" }) {
            return true
        }

        // 3. Half-Elves Special Logic: "Herança" acts as Adaptable if Agility d6 is
        // NOT selected. Lido só pelo traço (id "HERANCA"), não mais pelo nome da
        // raça — Meio-Elfo (Pathfinder) tem "Flexibilidade" em vez de "Herança" e
        // não deveria cair aqui (antes caía, por engano, via checagem de nome).
        val temHeranca = ancDef.habilidades.any { it.id?.keyify() == "HERANCA" }

        if (temHeranca && !meioElfoAgil) {
            return true
        }

        // 4. Arte da Guerra Human: "Nenhum" sign grants Adaptável
        if (compendioArteDaGuerraAtivo && ancDef.habilidades.any { it.id?.keyify() == "ADAPTAVEL_OU_SIGNO" }) {
            if (signoAdgSelecionado == null || signoAdgSelecionado.equals("Nenhum", ignoreCase = true)) {
                return true
            }
        }

        return false
    }

    val adaptavelSlotAvailable: Boolean by derivedStateOf {
        if (!temAdaptavel()) false
        else vantagemAdaptavelSelecionadaId == null
    }

    fun hasFreeAdaptavelSlotNow(debugSource: String? = null): Boolean {
        val hasAdaptavel = temAdaptavel()
        val slotAvailable = hasAdaptavel && vantagemAdaptavelSelecionadaId == null
        if (debugSource != null) {
            val anc = ancestralidade
            val ancDef = currentAncestryDef
            debugLog(
                "AdaptavelDebug",
                "[$debugSource] hasAdaptavel=$hasAdaptavel slotAvailable=$slotAvailable selectedId=$vantagemAdaptavelSelecionadaId ancestralidade=$anc origem=${ancDef?.origem}"
            )
        }
        return slotAvailable
    }

    // controla quais categorias da seção de Vantagens estão expandidas
    val categoriasVantagensExpandidas: SnapshotStateMap<Categoria, Boolean> =
        mutableStateMapOf<Categoria, Boolean>().apply {
            Categoria.entries.forEach { this[it] = false }
        }

    val sectionsExpanded: SnapshotStateMap<MainSection, Boolean> =
        mutableStateMapOf<MainSection, Boolean>().apply {
            MainSection.entries.forEach { this[it] = false }
            // Opcional: deixar algumas abertas por padrão
            this[MainSection.RESUMO] = true
        }

    fun toggleSection(section: MainSection) {
        val current = sectionsExpanded[section] ?: false
        sectionsExpanded[section] = !current
    }

    // guarda o nome da vantagem que está em foco (usada ao voltar da tela de detalhes)
    var vantagemEmFoco by mutableStateOf<String?>(null)

    fun temAntecedenteArcano(): Boolean {
        return vantagensSelecionadas.any { it.toArcanoKey() != null } ||
            ancestralidade.keyify() == "TRANSMORFOS" ||
            (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_elementalista")
    }

    fun getBestPericia(nome: String): Pericia? {
        val key = nome.keyify()
        val direct = periciasComIdiomas().firstOrNull { it.nome.keyify() == key }
            ?: mapaPericias[key]

        // Em Arte da Guerra, "Jutsu" é Lutar com Especialização de Perícia: cada categoria
        // (Jutsu 2, Jutsu 3, ...) é uma perícia própria, mas qualquer uma delas deve poder
        // satisfazer um requisito de "Lutar nível X" usando a categoria mais alta do
        // personagem, não só o slot base (que é literalmente a perícia "Lutar").
        if (key == "LUTAR" && compendioArteDaGuerraAtivo && jutsuExtras.isNotEmpty()) {
            return (listOfNotNull(direct) + jutsuExtras).maxByOrNull { rawTotal(it) }
        }
        return direct
    }

    fun podeRemoverPoderDoSlot(poderId: String): Pair<Boolean, String?> {
        if (modoLivre) return true to null
        val normalizedId = poderId.replace('_', ' ').keyify()

        if (ancestralidade.keyify() == "TRANSMORFOS" && normalizedId == "DISFARCE") {
            return false to "Poder racial fixo."
        }

        val linkedAdvantage = vantagensSelecionadas.firstOrNull { vant ->
            if (vant.id == "poder_favorito" && !vant.choice.isNullOrBlank()) {
                val choiceKey = vant.choice!!.replace('_', ' ').keyify()
                choiceKey == normalizedId
            } else {
                false
            }
        }

        if (linkedAdvantage != null) {
            return false to "Remova a vantagem Poder Favorito antes de devolver o poder ${linkedAdvantage.choice}."
        }
        return true to null
    }

    fun podeSelecionarComplicacao(complicacao: Complicacao): Pair<Boolean, String?> {
        if (modoLivre) return true to null
        if (complicacao.id == "talisma" && !temAntecedenteArcano()) {
            return false to "Talismã requer um Antecedente Arcano."
        }

        if (compendioCrystalHeartAtivo) {
            val forbidden = setOf(
                "incredulo", "ganancioso", "analfabeto", "pobreza",
                "forasteiro", "inimigo", "lento", "procurado", "um_braco_so",
                "obrigacao" // Block generic obligation to favor specific ones if needed, or keeping it?
                // Prompt said "Normalmente não pode ser escolhida... mas alguns casos raros...".
                // If blocked, users can't take it. Maybe I should NOT block obligacao?
                // But text says "Normalmente não pode ser escolhida".
                // And points to "Dependente".
                // I'll block it to force setting compliance, assuming "rare cases" are handled by GM override or using Dependente.
                // Re-reading: "Veja também a nova Complicação, Dependente."
                // I'll block standard 'obrigacao' since description is generic.
            )
            if (complicacao.id.keyify() in forbidden) {
                return false to "Não utilizada em Crystal Heart (ou substituída por versão específica)."
            }
        }

        return true to null
    }

    fun podeRemoverComplicacao(comp: Complicacao, tipo: String? = null): Pair<Boolean, String?> {
        if (modoLivre) return true to null
        // Locked check
        if (criacaoBasicaCongelada && !modoProgressaoAtivo) return false to "Criação finalizada."

        // Automatic checks
        val ancestryAuto = currentAncestryDef?.let { effectiveDesvantagens(it) }.orEmpty()
        val autoKeys = (desvantagensAutomaticas + desvantagensRaciais + ancestryAuto)
            .map { normalizeAutoKey(it.substringBefore("(").trim()) }
            .toSet()
        if (normalizeAutoKey(comp.id) in autoKeys || normalizeAutoKey(comp.name) in autoKeys) return false to "Complicação automática (Racial ou de Cenário)."

        // Young check
        if (comp.id == "pequeno" && jovemAutoPequeno) return false to "Adicionado automaticamente por Jovem (Maior)."

        // Knight Check
        val currentType = tipo ?: complicacoesSelecionadas[comp]
        if (comp.id.keyify() == "OBRIGACAO" && currentType == "Maior") {
             if (vantagensSelecionadas.any { it.nome.keyify() == "CAVALEIRO" }) {
                 return false to "Remova a vantagem Cavaleiro para remover esta Obrigação."
             }
        }

        // Points check
        val cost = if (currentType == "Maior") 2 else 1
        if (!modoProgressaoAtivo && pontosComplicacaoGastos > pontosComplicacao - cost) {
             return false to "Pontos em uso. Desfaça compras para liberar."
        }

        return true to null
    }

    fun podeRemoverVantagem(vantagem: Vantagem): Pair<Boolean, String?> {
        if (modoLivre) return true to null
        val keyId = normalizeAutoKey(vantagem.id)
        val keyNome = normalizeAutoKey(vantagem.nome.substringBefore("("))
        val automaticKeys = vantagensAutomaticas.map { normalizeAutoKey(it.substringBefore("(")) }.toSet()
        val racialKeys = vantagensRaciais.map { normalizeAutoKey(it.substringBefore("(")) }.toSet()

        if (keyId in automaticKeys || keyId in racialKeys || keyNome in racialKeys || keyNome in automaticKeys) {
            return false to "Vantagem automática (Racial ou de Cenário)."
        }

        if (vantagem.id in vantagensAutomaticasDoProtagonista) {
            return false to "Vantagem automática do Protagonista."
        }

        val kirinSorteAutomatica =
            compendioArteDaGuerraAtivo &&
            ancestralidade.keyify().contains("HUMANO") &&
            signoAdgSelecionado.equals("Kirin", ignoreCase = true) &&
            vantagem.id == "sorte"
        if (kirinSorteAutomatica) {
            return false to "Vantagem automática do Signo."
        }

        if (vantagem.id in vantagensAutomaticasDoSigno) {
            return false to "Vantagem automática do Signo."
        }
        if (vantagem.toArcanoKey() != null) {
            val temOutro = vantagensSelecionadas.any { it != vantagem && it.toArcanoKey() != null }
            if (!temOutro) {
                val temTalisma = complicacoesSelecionadas.keys.any { it.id == "talisma" }
                if (temTalisma) {
                    return false to "Remova a complicação Talismã antes de remover o Antecedente Arcano."
                }
            }
        }
        return true to null
    }

    val validationContext: com.example.swadebuilder.model.usecase.ValidateSelectionUseCase.Context by derivedStateOf {
        com.example.swadebuilder.model.usecase.ValidateSelectionUseCase.Context(
            ancestralidade = ancestralidade,
            ancestralidadeDef = currentAncestryDef,
            compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
            compendioFantasiaAtivo = compendioFantasiaAtivo,
            compendioPathfinderAtivo = compendioPathfinderAtivo,
            compendioHorrorAtivo = compendioHorrorAtivo,
            compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
            valoresAtributos = valoresAtributos.mapValues { it.value.intValue },
            pericias = periciasComIdiomas(),
            rawTotalPericia = { rawTotal(it) },
            tipoMonstroSelecionado = tipoMonstroSelecionado,
            cartaSelvagem = cartaSelvagem,
            complicacoesSelecionadas = complicacoesSelecionadas.toMap(),
            ppPurchasesThisRank = comprasPpPorEstagio[estagioAtual().nome] ?: 0,
            maxPpPurchasesAllowed = maxComprasPpAteAgora(),
            vantagensSelecionadas = vantagensSelecionadas.toList(),
            emProgresso = emProgresso,
            superInvestments = superInvestments.toList(),
            listaAtributos = listaAtributos,
            atributoMaxRaw = { atributoMaxRaw(it) },
            periciaCapRaw = { periciaCapRaw(it) },
            permiteMultiAntecedenteArcano = permiteMultiAntecedenteArcano,
            estagioAtual = estagioAtual(),
            listaDeEstagios = listaDeEstagios,
            overrideStageForVantagem = overrideStageForVantagem,
            effectiveProgressoParaVantagens = effectiveProgressoParaVantagens(),
            nivelParaEstagio = nivelParaEstagio,
            nasceUmHeroi = nasceUmHeroi,
            pvFromXpOutstanding = pvFromXpOutstanding,
            tropoSelecionadoId = tropoSelecionado?.id,
            getBestPericia = { getBestPericia(it) },
            modoLivre = modoLivre
        )
    }

    fun podeSelecionar(v: Vantagem): Boolean {
        if (modoLivre) return true
        return validateSelectionUseCase.execute(v, validationContext)
    }

    private fun shouldIgnoreLeadershipStage(v: Vantagem): Boolean {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_samurai") return false
        if (v.categoria != Categoria.LIDERANCA) return false
        val pericia = getBestPericia("Conhecimento de Batalha") ?: return false
        return rawTotal(pericia) >= 8
    }

    var pontosComplicacaoGastos by mutableIntStateOf(0)
    val baseIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    private val compIncsPorPericia = mutableStateMapOf<Pericia, Int>().also { m -> listaPericias.forEach { m[it] = 0 } }
    val compCostStackPorPericia = mutableStateMapOf<Pericia, MutableList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableListOf() }
    }
    val paCostStackPorAtributo = mutableStateMapOf<String, MutableList<Int>>().also { m ->
        listaAtributos.forEach { m[it] = mutableListOf() }
    }
    val spCostStackPorPericia = mutableStateMapOf<Pericia, SnapshotStateList<Int>>().also { m ->
        listaPericias.forEach { m[it] = mutableStateListOf() }
    }

    fun rebuildPericias(desiredRaw: Map<Pericia, Int>) {
        syncLinguistaIdiomas()
        val poolSize = totalSpPool // Updated getter usage
        var cumulativeCost = 0

        periciasComIdiomas().forEach { per ->

            val cap = periciaCapRaw(per)
            val target = (desiredRaw[per] ?: rawTotal(per)).coerceAtMost(cap)

            val stack = spCostStackPorPericia.getValue(per)
            stack.clear()
            baseIncsPorPericia[per] = 0

            var curr = periciaStartRaw(ancestralidade, per)
            var freeSteps = compIncsPorPericia.getValue(per)

            while (curr < target && cumulativeCost < poolSize) {
                val next = when {
                    curr == 0 -> 4
                    curr < 12 -> curr + 2
                    else      -> curr + 1
                }

                val attrKey = atributoBaseParaPericia(per)
                val cost    = if (next <= valoresAtributos[attrKey]!!.intValue) 1 else 2
                if (cumulativeCost + cost > poolSize) break

                if (freeSteps > 0) {
                    freeSteps -= 1
                } else {
                    stack.add(cost)
                    baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
                    cumulativeCost += cost
                }
                curr = next
            }
        }
    }

    fun decreasePericia(per: Pericia) {
        val spStack = spCostStackPorPericia.getValue(per)
        val idx = spStack.indexOfLast { it > 0 }
        if (idx >= 0) {
            spStack.removeAt(idx)
            val newIncs = baseIncsPorPericia.getValue(per) - 1
            baseIncsPorPericia[per] = newIncs

            if (newIncs == 0) {
                spStack.clear()
                especializacoesPorPericia.remove(per.nome)
                notasPericia.remove(per.nome)
            }

            if (skillAdvancementInProgress) {
                skillsForCurrentAdvancement.remove(per.nome)
            }
        }

        if (isIdiomaPericia(per)) {
            syncIdiomaSlots()
        }
        if (isJutsuPericia(per)) {
            syncJutsuSlots()
        }
    }

    private fun atributoBaseRacial(a: String): Int {
        // Fix: Use keyified ancestry to match DataLoader map keys
        val base = racialAttrMinMap[ancestralidade.keyify()]?.get(a.keyify()) ?: 4

        var modifiedBase = base
        val attrKey = a.keyify()

        // Traços que concedem d6 inicial num atributo específico, lidos direto de
        // habilidades[] da raça (já com os ajustes de variante aplicados por
        // applyAncestryVariantAdjustments/getAncestralidadeDef) em vez de comparar
        // o nome da raça — assim o bônus segue o traço, não o rótulo da raça.
        val habilidadeIds = currentAncestryDef?.habilidades
            ?.mapNotNull { it.id?.keyify() }
            ?.toSet()
            ?: emptySet()

        // Monster Bonus
        getMonstroSelecionado()?.let { monstro ->
            val bonusEntry = monstro.atributos_bonus.entries.firstOrNull {
                it.key.keyify() == attrKey
            }
            if (bonusEntry != null) {
                // Steps: 1 -> d6, 2 -> d8. Base is d4 (4).
                val steps = bonusEntry.value
                var monsterBase = 4
                repeat(steps) {
                    monsterBase = if (monsterBase < 12) monsterBase + 2 else monsterBase + 1
                }
                modifiedBase = maxOf(modifiedBase, monsterBase)
            }
        }

        // Traços de alvo fixo (a raça sempre sobe o mesmo atributo quando o traço
        // está presente): o traço só precisa estar na raça, quem diz QUAL
        // atributo sobe e QUANTOS passos é o próprio RacialTraitEffect.AtributoStep
        // do catálogo — não mais um "if" por traço/atributo (Ágil, Sólido como
        // Rocha, Forte, Espirituoso, Habitante de Gravidade Baixa etc. e
        // qualquer novo traço desse tipo já entram automaticamente).
        habilidadeIds.forEach { id ->
            val efeito = RacialTraitPointCatalog.efeitoDe(id)
            if (efeito is RacialTraitEffect.AtributoStep && efeito.atributo.keyify() == attrKey) {
                modifiedBase = maxOf(modifiedBase, 4 + 2 * efeito.passos)
            }
        }

        // Traços de alvo escolhido pelo jogador entre 2-3 atributos: o traço só
        // decide QUE a raça tem a escolha; qual atributo foi escolhido continua
        // vindo do state dedicado (mesmo padrão usado no restante do app).
        if (habilidadeIds.contains("ENDURECIDO")) {
            // Meio-Orc: escolha entre Vigor d6 ou Força d6
            if (attrKey == "VIGOR") {
                modifiedBase = if (meioOrcForca) 4 else 6
            }
            if (attrKey == "FORCA") {
                modifiedBase = if (meioOrcForca) 6 else 4
            }
        }
        if (habilidadeIds.contains("PRIMITIVO") || habilidadeIds.contains("MINERADOR_ATRIBUTO")) {
            // Feral (Arte da Guerra): escolha entre Força/Vigor/Agilidade.
            // Humano Sci-Fi "Minerador": escolha entre Força/Vigor.
            val chosen = humanoMineradorAtributo ?: "Força"
            if (attrKey == chosen.keyify()) {
                modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        val currentSciFiVariant = if (compendioSciFiAtivo) resolveCurrentSciFiVariantSelection() else scifiVariant

        // Sci-Fi Attribute Variants (Padrão vs Variant) — Drakens e Elementais
        // ainda não têm o traço "Forte"/substituto estruturado no JSON (a
        // ambientação só descreve a troca em texto livre), então continuam
        // hardcoded por nome de raça por enquanto; ver nota na revisão.
        if (compendioSciFiAtivo) {
            val ancKey = ancestralidade.keyify()

            // Drakens: Padrão (Forte - Str d6), Dragão (No Forte - Str d4)
            if (ancKey == "DRAKENS") {
                if (a.keyify() == "FORCA") {
                    // JSON was cleared to d4. Padrão grants "Forte" (Start d6). Variant grants "Arma de Sopro".
                    // If Padrão (or default), start d6 (6). If Dragão, start d4 (4).
                    val variant = currentSciFiVariant ?: "Padrão"
                    if (variant == "Padrão") {
                        modifiedBase = maxOf(modifiedBase, 6)
                    } else {
                        modifiedBase = 4 // Reset to d4
                    }
                }
            }

            // Elementais: base JSON já é d8 (Padrão, "Forte"). Seleção "Ar,
            // Fogo ou Água" troca Forte por Forma de Energia (ver
            // AncestryVariantRegistry.elementaisScifi) — sem foco físico,
            // volta pra d4. Numérico, então fica aqui como exceção pontual
            // (mesmo padrão do naturalArmorFromRace de Pedregoso/Umvee), não
            // faz parte do ResolvedTraitPackage genérico.
            if (ancKey == "ELEMENTAIS") {
                if (a.keyify() == "FORCA") {
                    val variant = currentSciFiVariant ?: "Padrão"
                    if (variant != "Padrão") {
                        modifiedBase = 4 // Reset to d4
                    }
                }
            }

            // Mineradores Genéticos (FORTE) e Ferais Sci-Fi (ESPIRITUOSO) agora são
            // resolvidos genericamente acima via habilidadeIds — a remoção do
            // traço nas variantes Zero G / Menor acontece em
            // applyAncestryVariantAdjustments, então não precisa de hardcode aqui.
        }

        // Descendente Elemental (Terra) agora é resolvido genericamente acima via
        // habilidadeIds.contains("SOLIDO_COMO_ROCHA") — esse traço já só existe em
        // habilidades[] quando "Terra" está selecionado (ver
        // applyAncestryVariantAdjustments), então não precisa comparar o nome da
        // raça nem reler descendenteElementalSelecionado aqui.

        // Arte da Guerra - Signos (only for Humans)
        if (compendioArteDaGuerraAtivo && ancestralidade.keyify().contains("HUMANO")) {
            val sign = signoAdgSelecionado
            val attrKey = a.keyify()
            if (sign != null) {
                if (sign.equals("Boi", ignoreCase = true) && attrKey == "FORCA") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Dragão", ignoreCase = true) && attrKey == "ESPIRITO") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Macaco", ignoreCase = true) && attrKey == "ASTUCIA") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
                if (sign.equals("Urso", ignoreCase = true) && attrKey == "VIGOR") {
                    modifiedBase = maxOf(modifiedBase, 6)
                }
            }
        }

        // Arte da Guerra - Protagonista (Qualidades de Herói)
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_protagonista") {
            val attrKey = a.keyify()
            when (protagonistaRollQualidade) {
                2 -> if (attrKey == "ASTUCIA") modifiedBase = maxOf(modifiedBase, 6)
                4 -> if (attrKey == "FORCA") modifiedBase = maxOf(modifiedBase, 6)
                6 -> if (attrKey == "ESPIRITO") modifiedBase = maxOf(modifiedBase, 6)
                8 -> if (attrKey == "AGILIDADE") modifiedBase = maxOf(modifiedBase, 6)
                10 -> if (attrKey == "VIGOR") modifiedBase = maxOf(modifiedBase, 6)
            }
        }

        if (isHumanoFantasiaSelecionado() &&
            pacoteCulturalFantasiaSelecionado == "Povo da Montanha" &&
            a.keyify() == "VIGOR"
        ) {
            modifiedBase = maxOf(modifiedBase, 6)
        }

        return modifiedBase
    }

    fun atributoMaxRawNaCriacao(a: String, forceStandard: Boolean = false): Int {
        if (modoLivre && !forceStandard) return 100
        val baseCap = atributoMaxRaw(a, forceStandard)
        if (modoProgressaoAtivo) return baseCap
        // Feral (Arte da Guerra) "Mente Primitiva": teto de Astúcia travado em d6 na
        // criação. Lido do traço em habilidades[], não do nome da raça.
        val temMentePrimitiva = currentAncestryDef?.habilidades?.any { it.id?.keyify() == "MENTE_PRIMITIVA" } == true
        if (temMentePrimitiva && a.keyify() == "ASTUCIA") {
            return minOf(baseCap, 6)
        }
        return baseCap
    }

    private fun isHumanoFantasiaSelecionado(): Boolean {
        if (!compendioFantasiaAtivo) return false
        if (!ancestralidade.keyify().contains("HUMANO")) return false
        val ancDef = currentAncestryDef ?: return false
        return canonicalOriginKey(ancDef.origem) == "FANTASIA"
    }

    fun atributoMinRaw(a: String): Int =
        atributoBaseRacial(a)

    fun atributoMaxRaw(a: String, forceStandard: Boolean = false): Int {
        if (modoLivre && !forceStandard) return 100
        val minRaw = atributoMinRaw(a)

        var extras = ((minRaw - 4).coerceAtLeast(0) / 2)
        val baseCap = 12 + extras

        val chave = a.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        var finalCap = baseCap + (profCount + espCount) * 2

        // Limite de Força por Tamanho (Diminutos/Pequenos)
        // Se Tamanho <= -2 (Pequeno/Muito Pequeno): Força Máxima = d8.
        // Se Tamanho <= -3 (Muito Pequeno/Minúsculo): Força Máxima = d6.
        // Se Tamanho <= -4 (Minúsculo/Diminuto): Força Máxima = d4.
        if (chave == "FORCA") {
            val rawSize = ModifierEngine.sizeRawDisplay(this)
            val sizeCap = when {
                rawSize <= -4 -> 4 // d4
                rawSize == -3 -> 6 // d6
                rawSize == -2 -> 8 // d8
                else -> 100 // Sem limite por tamanho
            }
            if (sizeCap < finalCap) {
                finalCap = sizeCap
            }
        }

        return finalCap
    }

    fun periciaCapRaw(per: Pericia, forceStandard: Boolean = false): Int {
        if (modoLivre && !forceStandard) return 100
        val startRaw = periciaStartRaw(ancestralidade, per)

        // Half-Orc Buscatrilha Intimidate Exception (starts d4 but gets cap increase)
        val isHalfOrcIntimidate = compendioPathfinderAtivo &&
                ancestralidade.keyify().contains("MEIO-ORC") &&
                per.nome.keyify() == "INTIMIDAR"

        val baseCap = if (startRaw >= 6 || isHalfOrcIntimidate) 13 else 12

        val chave = per.nome.keyify()
        val profCount = vantagensSelecionadas.count {
            it.nome.keyify() == "PROFISSIONAL" && it.choice?.keyify() == chave
        }
        val espCount = vantagensSelecionadas.count {
            it.nome.keyify() == "ESPECIALISTA" && it.choice?.keyify() == chave
        }

        return baseCap + (profCount + espCount) * 2
    }

    fun rawTotal(per: Pericia): Int {
        val startRaw = periciaStartRaw(ancestralidade, per)
        val normalIncs = baseIncsPorPericia[per] ?: 0
        val complicsIncs = compIncsPorPericia[per] ?: 0
        val totalIncs = normalIncs + complicsIncs

        if (modoProgressaoAtivo) {
            val xpArcaneIds = xpArcaneAdvantageIds()
            if (xpArcaneIds.isNotEmpty()) {
                val startWithoutXpArcane = periciaStartRawInternal(
                    ancestralidade,
                    per,
                    includeArcaneVantage = { it.id !in xpArcaneIds }
                )
                val xpArcaneStart = if (startRaw > startWithoutXpArcane) startRaw else 0

                if (xpArcaneStart > 0) {
                    val frozenIncs = (frozenSkillIncrements[per.nome] ?: 0).coerceIn(0, normalIncs)
                    val progressionIncs = (normalIncs - frozenIncs).coerceAtLeast(0)
                    val preXpRaw = rawFromStartAndIncrements(startWithoutXpArcane, frozenIncs + complicsIncs)
                    val effectiveStart = maxOf(preXpRaw, xpArcaneStart)
                    return rawFromStartAndIncrements(effectiveStart, progressionIncs)
                }
            }
        }

        return rawFromStartAndIncrements(startRaw, totalIncs)
    }

    fun aplicarAncestralidade(anc: String, feedbackMessages: MutableList<String>, autoRefund: Boolean = true) {
        debugLog(
            "AdaptavelDebug",
            "[aplicarAncestralidade:start] ancAtual=$ancestralidade ancNovo=$anc selectedId=$vantagemAdaptavelSelecionadaId"
        )
        val prevAnc = ancestralidade

        val prevAncDef = getAncestralidadeDef(prevAnc)
        val ancDef = getAncestralidadeDef(anc)
        val effectiveScifiVariant = resolveSciFiVariantSelectionFor(
            ancestryName = anc,
            availableOptions = ancDef?.opcoes ?: emptyList()
        )
        if (compendioSciFiAtivo && !ancDef?.opcoes.isNullOrEmpty() && scifiVariant != effectiveScifiVariant) {
            scifiVariant = effectiveScifiVariant
        }

        // Traços negativos escolhidos só fazem sentido para Anões (Ciber); limpa a
        // seleção sempre que o jogador troca de raça OU muda de variante (inclusive
        // Ciber -> Básico dentro da mesma raça), para não vazar escolha entre raças.
        val isAnoesCiberAgora = anc.keyify().contains("ANOES") && effectiveScifiVariant == "Ciber"
        if (!isAnoesCiberAgora && anaoCiberTracosSelecionados.isNotEmpty()) {
            anaoCiberTracosSelecionados = emptyList()
        }

        // Limpa a Variante custom selecionada se ela não pertencer à raça de destino.
        if (customVarianteRacialSelecionadaId != null) {
            val selectedVariant = listaVariantesRaciaisCustom.firstOrNull { it.id == customVarianteRacialSelecionadaId }
            if (selectedVariant == null || selectedVariant.ancestralidadeId != anc.keyify()) {
                customVarianteRacialSelecionadaId = null
            }
        }

        val paAntes = pontosAtributo
        val spAntes = pontosPericia
        val pvAntes = pontosVantagem

        periciasComIdiomas().associateWith { rawTotal(it) }

        val ancestryChangeCoordination = applyAncestryChangeCoordinatorUseCase.execute(
            ApplyAncestryChangeCoordinatorUseCase.Params(
                previousAncestry = prevAnc,
                targetAncestry = anc,
                previousAncestryDef = prevAncDef,
                targetAncestryDef = ancDef,
                currentAutomaticAdvantages = buildAutomaticAdvantageContextForTransition(),
                previousAutomaticDisadvantages = desvantagensRaciais.toList(),
                pontosVantagemAtuais = pontosVantagem,
                vantagensSelecionadas = vantagensSelecionadas.toList(),
                attributeNames = listaAtributos,
                attributeCaps = listaAtributos.associateWith { nome ->
                    AdjustAttributesForAncestryChangeUseCase.AttributeCap(
                        minRaw = atributoMinRaw(nome),
                        maxRaw = atributoMaxRawNaCriacao(nome)
                    )
                },
                paCostStacks = listaAtributos.associateWith { nome ->
                    paCostStackPorAtributo.getValue(nome).toList()
                },
                descendenteElementalSelecionado = descendenteElementalSelecionado,
                allAdvantages = listaVantagens,
                availableComplications = listaComplicacoes,
                selectedComplications = complicacoesSelecionadas,
                automaticTropoAdvantageIds = vantagensAutomaticasDoTropo.toSet(),
                meetsRequirements = { atendeRequisitosMantidos(it) },
                originPriorityResolver = { getOriginPriority(it) },
                compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                compendioSciFiAtivo = compendioSciFiAtivo,
                compendioScifiMechasCiberneticosAtivo = compendioScifiMechasCiberneticosAtivo,
                signoAdgSelecionado = signoAdgSelecionado,
                modoSupers = modoSupers,
                meioElfoAgil = meioElfoAgil,
                anoesScifiSelecionado = anoesScifiSelecionado,
                scifiVariant = effectiveScifiVariant,
                humanoMineradorAtributo = humanoMineradorAtributo,
                anaoCiberTracosSelecionados = anaoCiberTracosSelecionados
            )
        )

        ancestryChangeCoordination.humanTransition.vantagemRemovida?.let { toRemove ->
            vantagensSelecionadas.remove(toRemove)
            feedbackMessages.add("Vantagem ${toRemove.nome} removida para compensar a troca de Ancestralidade.")
        }
        pontosVantagem = ancestryChangeCoordination.humanTransition.novosPontosVantagem

        // Troca efetiva da ancestralidade
        ancestralidade = anc

        // SAFETY: Force removal of "Herança" edge for Fantasy Half-Elves if it slipped through
        if ((anc.keyify().contains("MEIO-ELFO") || anc.keyify().contains("MEIO-ELFOS")) && !anc.keyify().contains("PATHFINDER")) {
            val herancaEdge = vantagensSelecionadas.find { it.id == "heranca" || it.nome.keyify() == "HERANCA" }
            if (herancaEdge != null) {
                removerVantagem(herancaEdge)
            }
        }

        // Check if Adaptavel was lost
        val hasAdaptavel = temAdaptavel()
        val lostAdaptavel = !hasAdaptavel && vantagemAdaptavelSelecionadaId != null

        var removedAdaptavelId: String? = null

        if (lostAdaptavel) {
            val toRemove = vantagensSelecionadas.find { it.id == vantagemAdaptavelSelecionadaId }
            if (toRemove != null) {
                removeVantagemDinheiro(toRemove)
                removerVantagem(toRemove)
                val label = if (prevAnc.keyify() == "GOBLINS" && compendioFantasiaAtivo) "Sobrevivente" else "Adaptável"
                feedbackMessages.add("Vantagem '${toRemove.nome}' (Vantagem bônus de $label) removida.")
                removedAdaptavelId = toRemove.id
            }
            vantagemAdaptavelSelecionadaId = null
        }

        val attributeAdjustmentResult = ancestryChangeCoordination.attributeAdjustmentResult

        attributeAdjustmentResult.adjustmentsByAttribute.forEach { entry ->
            val nome = entry.key
            val adjustment = entry.value
            val stack = paCostStackPorAtributo.getValue(nome)
            stack.clear()
            stack.addAll(adjustment.adjustedStack)

            if (adjustment.refundedPoints > 0) {
                feedbackMessages.add("${adjustment.refundedPoints} ponto(s) de atributo devolvido(s) de $nome.")
            }

            valoresAtributos[nome]!!.intValue = adjustment.newRaw
        }

        when (ancestryChangeCoordination.signoAction) {
            ApplyAncestryChangeCoordinatorUseCase.SignoAction.SELECT_NONE -> selecionarSigno("Nenhum")
            ApplyAncestryChangeCoordinatorUseCase.SignoAction.CLEAR -> selecionarSigno(null)
            ApplyAncestryChangeCoordinatorUseCase.SignoAction.KEEP -> Unit
        }
        if (!isHumanoFantasiaSelecionado()) {
            pacoteCulturalFantasiaSelecionado = "Humano padrão"
        }
        celestialAAMilagresDesabilitado = ancestryChangeCoordination.celestialAAMilagresDesabilitado
        if (ancestryChangeCoordination.resetMeioElfoAgil) {
            meioElfoAgil = false
        }
        if (ancestryChangeCoordination.resetMeioOrcForca) {
            meioOrcForca = false
        }
        if (ancestryChangeCoordination.clearDescendenteElemental) {
            selecionarDescendenteElemental(null)
        }
        if (ancestryChangeCoordination.resetAnoesScifi) {
            anoesScifiSelecionado = null
        }
        if (ancestryChangeCoordination.resetScifiVariant) {
            scifiVariant = null
            humanoMineradorAtributo = null
        }
        if (ancestryChangeCoordination.clearPericiaGnomo) {
            selecionarPericiaGnomo(null)
        }

        val racialPackage = ancestryChangeCoordination.racialPackage

        vantagensSelecionadas.clear()
        val advantagesToRestore = if (removedAdaptavelId != null) {
            racialPackage.selectedAdvantages.filter { it.id != removedAdaptavelId }
        } else {
            racialPackage.selectedAdvantages
        }
        vantagensSelecionadas.addAll(advantagesToRestore)

        desvantagensAutomaticas.clear()
        desvantagensAutomaticas.addAll(ancDef?.desvantagens ?: emptyList())

        vantagensAutomaticas.clear()
        vantagensAutomaticas.addAll(racialPackage.vantagensAutomaticas)

        vantagensRaciais.clear()
        vantagensRaciais.addAll(racialPackage.vantagensRaciais)

        desvantagensRaciais.clear()
        desvantagensRaciais.addAll(racialPackage.desvantagensRaciais)

        syncPacoteCulturalFantasia()

        naturalArmorFromRace = racialPackage.naturalArmorFromRace
        if (racialPackage.forceArmorZero) {
            armadura = 0
        }

        when (racialPackage.elementalAction) {
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.SELECT_DEFAULT -> {
                if (anc.keyify() == "DESCENDENTE ELEMENTAL") selecionarDescendenteElemental("Água")
                val ancKey = anc.keyify()
                val defaultOption = ancDef?.opcoes?.firstOrNull()
                if (!defaultOption.isNullOrBlank()) {
                    val normalizedDefault = resolveSciFiVariantSelectionFor(
                        ancestryName = anc,
                        availableOptions = ancDef.opcoes,
                        overrideSelection = defaultOption
                    )
                    scifiVariant = normalizedDefault
                    if (ancKey.contains("ANOES")) {
                        anoesScifiSelecionado = normalizedDefault
                    }
                }
                if (compendioSciFiAtivo && ancKey == "HUMANOS" && humanoMineradorAtributo == null) {
                    humanoMineradorAtributo = "Força"
                }
                if (compendioArteDaGuerraAtivo && ancKey == "FERAL" && humanoMineradorAtributo == null) {
                    humanoMineradorAtributo = "Força"
                }
            }
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.REAPPLY_CURRENT -> {
                val current = descendenteElementalSelecionado
                descendenteElementalSelecionado = null
                selecionarDescendenteElemental(current)
            }
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE -> Unit
        }

        if (compendioSciFiAtivo && anc.keyify() == "AVIANOS" && resolveCurrentSciFiVariantSelection(anc).equals("Ave de rapina", ignoreCase = true)) {
            anotacoes = anotacoes
                .replace("\n• Forma Alienígena.", "")
                .replace("• Forma Alienígena.\n", "")
                .replace("• Forma Alienígena.", "")
        }

        hasFreeAdaptavelSlotNow(debugSource = "aplicarAncestralidade:end")

        if (racialPackage.anotacoesToAdd.isNotEmpty()) {
            val newNotes = racialPackage.anotacoesToAdd.filter { !anotacoes.contains(it) }
            if (newNotes.isNotEmpty()) {
                anotacoes += "\n" + newNotes.joinToString("\n") { "• $it" }
            }
        }

        complicacoesSelecionadas.clear()
        complicacoesSelecionadas.putAll(ancestryChangeCoordination.complicationsSnapshot.selectedComplications)

        // Recalcula pontos de atributo/perícias após o ajuste racial
        recalcularPontosAtributo(feedbackMessages)
        if (autoRefund) {
            while (pontosAtributo > 0 && cpPaStack.isNotEmpty()) {
                devolverPcDeAtributo()
            }
        }

        syncOraculoVariant()

        rebuildAllPericiaStacks(feedbackMessages)
        if (autoRefund) {
            while (pontosPericia > 0 && cpSpStack.isNotEmpty()) {
                devolverPcDePericia()
            }
        }

        val paDepois = pontosAtributo
        val spDepois = pontosPericia
        val pvDepois = pontosVantagem

        if (paDepois > paAntes) feedbackMessages.add("${paDepois - paAntes} ponto(s) de atributo devolvido(s).")
        if (spDepois > spAntes) feedbackMessages.add("${spDepois - spAntes} ponto(s) de perícia devolvido(s).")
        if (pvDepois > pvAntes) feedbackMessages.add("${pvDepois - pvAntes} ponto(s) de vantagem devolvido(s).")

        // Validar requisitos das vantagens existentes
        val invalidAdvantagesResolution = ancestryChangeCoordination.invalidAdvantagesResolution

        invalidAdvantagesResolution.removedAdvantages.forEach { removed ->
            removeVantagemDinheiro(removed)
            removerVantagem(removed)

            // Only refund the PV if this advantage was actually purchased by the player (not granted for free by the previous ancestry/tropo)
            val wasFreeFromPreviousContext = ancestryChangeCoordination.previousFreeAdvantageKeys.contains(removed.nome.keyify()) ||
                                             ancestryChangeCoordination.previousFreeAdvantageKeys.contains(removed.id.keyify()) ||
                                             (removed.id == "poderes_misticos" && "PODERES_MISTICOS" in ancestryChangeCoordination.previousFreeAdvantageKeys) ||
                                             ancestryChangeCoordination.invalidAdvantagesResolution.removedAdvantages.any { it.id == removed.id && ancestryChangeCoordination.previousFreeAdvantageKeys.contains(it.nome.keyify()) }
            if (!wasFreeFromPreviousContext && !vantagensAutomaticasDoTropo.contains(removed.id)) {
                pontosVantagem++
            }
            feedbackMessages.add("Vantagem '${removed.nome}' removida (requisitos não atendidos).")
        }

        if (autoRefund) {
            while (pontosVantagem > 0 && cpPvStack.isNotEmpty()) {
                devolverPcDeVantagem()
            }
            checkAndRefundResourcePb()
        }

        if (pontosVantagem != pvDepois) {
            rebuildAllPericiaStacks(feedbackMessages)
        }

        val activeArcaneKeys = vantagensSelecionadas.mapNotNull { it.toArcanoKey()?.normAAKey() }.toMutableSet()
        if (ancestralidade.keyify() == "TRANSMORFOS") activeArcaneKeys.add("DOM")
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_elementalista") activeArcaneKeys.add("ELEMENTALISTA")
        if (compendioArteDaGuerraAtivo && (tropoSelecionado?.tecnicasIniciais ?: 0) > 0) activeArcaneKeys.add("MESTRE DO CHI")

        val keysToRemove = poderSlotsPorArcano.keys.filter { it !in activeArcaneKeys }
        keysToRemove.forEach {
            poderSlotsPorArcano.remove(it)
            novosPoderesStacksPorArcano.remove(it)
        }

        vantagensSelecionadas.forEach { ensurePowerSlotsFor(it) }

        if (ancestralidade.keyify() == "TRANSMORFOS") {
            val arcKey = "DOM"
            if (!poderSlotsPorArcano.containsKey(arcKey)) {
                val count = getSlotsCountForArcano(arcKey)
                poderSlotsPorArcano[arcKey] = mutableStateListOf<String?>().apply { repeat(count) { add(null) } }
            }
        }

        syncPoderesSelecionadosFromSlots()
    }

    private fun atendeRequisitosMantidos(v: Vantagem): Boolean {
        // Estágio
        val estagioRequerido = listaDeEstagios.firstOrNull { it.nome.equals(v.requisitos.estagio, ignoreCase = true) }
        if (estagioRequerido != null) {
            val atual = estagioAtual()
            if (!shouldIgnoreLeadershipStage(v) &&
                listaDeEstagios.indexOf(atual) < listaDeEstagios.indexOf(estagioRequerido)
            ) return false
        }

        // Prévias
        if (!atendeVantagensPrevias(v)) return false

        // Atributos
        if (v.requisitos.atributoMin.any { (nome, min) ->
                val chaveNorm = nome.uppercase().semAcentos().trim()
                val attrKey = mapaAtributosDisplay.keys.firstOrNull { it.equals(chaveNorm, ignoreCase = true) } ?: chaveNorm
                (valoresAtributos[attrKey]?.intValue ?: 0) < min
            }) return false

        // Perícias
        val periciaMinMap = v.requisitos.periciaMin
        if (v.vinculadoPericia && periciaMinMap.isNotEmpty()) {
            val atende = periciaMinMap.any { (nome, min) ->
                val p = getBestPericia(nome) ?: return@any false
                rawTotal(p) >= min
            }
            if (!atende) return false
        } else {
            if (periciaMinMap.any { (nome, min) ->
                    val p = getBestPericia(nome) ?: return@any false
                    rawTotal(p) < min
                }) return false
        }

        // Opcionais
        val periciaMinOpcMap = v.requisitos.periciaMinOpcional
        if (periciaMinOpcMap.isNotEmpty()) {
            if (v.vinculadoPericia && !v.choice.isNullOrBlank()) {
                val choiceKey = v.choice!!.keyify()
                val matchEntry = periciaMinOpcMap.entries.firstOrNull { it.key.keyify() == choiceKey }
                if (matchEntry == null) return false
                val per = getBestPericia(choiceKey) ?: return false
                if (rawTotal(per) < matchEntry.value) return false
            } else {
                val atende = periciaMinOpcMap.any { (nome, min) ->
                    val p = getBestPericia(nome) ?: return@any false
                    rawTotal(p) >= min
                }
                if (!atende) return false
            }
        }

        // Tags
        if (v.requisitos.tags.isNotEmpty()) {
            val ancDef = currentAncestryDef
            if (ancDef == null || !ancDef.tags.containsAll(v.requisitos.tags)) return false
        }

        // Template Monstruoso
        if (v.requisitos.templatesRequired.isNotEmpty()) {
            val selected = tipoMonstroSelecionado
            if (selected == null || selected !in v.requisitos.templatesRequired) {
                return false
            }
        }

        // Tiro Duplo Aprimorado
        if (v.id == "tiro_duplo_aprimorado") {
            val base = vantagensSelecionadas.firstOrNull { it.id == "tiro_duplo" } ?: return false
            val choice = base.choice
            if (choice.isNullOrBlank()) return false
            val skill = getBestPericia(choice) ?: return false
            if (rawTotal(skill) < 10) return false
        }

        return true
    }

    fun spendProgressAtStage(stageName: String, n: Int) {
        val current = stageXpSpent.getValue(stageName)
        stageXpSpent[stageName] = current + n
        recomputeAvailableProgress()
    }

    fun refundProgressAcrossStages(n: Int) {
        var remaining = n
        reachedStages()
            .mapIndexed { idx, est -> idx to est }
            .asReversed()
            .forEach { (_, est) ->
                if (remaining == 0) return@forEach
                val spent = stageXpSpent.getValue(est.nome)
                if (spent > 0) {
                    val refund = spent.coerceAtMost(remaining)
                    stageXpSpent[est.nome] = spent - refund
                    remaining -= refund
                }
            }
        recomputeAvailableProgress()
    }

    fun refundProgressAtStage(stageName: String, n: Int) {
        val current = stageXpSpent.getValue(stageName)
        stageXpSpent[stageName] = (current - n).coerceAtLeast(0)
        recomputeAvailableProgress()
    }

    fun recomputeAvailableProgress() {
        val totalSpent = stageXpSpent.values.sum()
        val availableByProgress = (progresso - totalSpent).coerceAtLeast(0)

        val remainingStageCapacity = reachedStages()
            .sumOf { stage ->
                val stageIndex = listaDeEstagios.indexOf(stage)
                val cap = dynamicStageCaps[stageIndex]
                val spentHere = stageXpSpent.getValue(stage.nome)
                (cap - spentHere).coerceAtLeast(0)
            }

        progressosDisponiveis = kotlin.math.min(availableByProgress, remainingStageCapacity)
    }

    fun checkFreeze() {
        val idx = currentProgressStageIndex()
        val est = listaDeEstagios[idx]
        val cap = dynamicStageCaps[idx]
        val spent = stageXpSpent.getValue(est.nome)
        if (spent == cap) {
            frozenAdvantageCount = vantagensSelecionadas.size
        }
    }

    val isAdgLockedMode: Boolean
        get() = compendioArteDaGuerraAtivo && tropoSelecionado == null

    fun isSectionEnabled(section: MainSection): Boolean {
        if (modoProgressaoAtivo) return true
        if (!compendioArteDaGuerraAtivo) return true

        return if (tropoSelecionado == null) {
            // "Locked Mode" (No Trope selected yet):
            // Can see Summary, Ancestry, and Trope selection.
            // Other tabs are disabled.
            when (section) {
                MainSection.RESUMO, MainSection.ANCESTRALIDADES, MainSection.TROPOS -> true
                else -> false
            }
        } else {
            // "Unlocked Mode" (Trope selected):
            // Ancestry is now LOCKED (disabled).
            // Trope is ENABLED (to change back to 'None').
            // All other tabs are ENABLED.
            when (section) {
                MainSection.ANCESTRALIDADES -> false
                else -> true
            }
        }
    }

    // PROMPT 1: Explicit calculation: (Current Step - Racial Base Step)
    fun calcularPontosAtributoRestantes(): Int {
        if (modoLivre) return 0
        var usados = 0

        for (nome in listaAtributos) {
            val atual = valoresAtributos[nome]!!.intValue
            val base = atributoBaseRacial(nome)
            val stackSize = paCostStackPorAtributo[nome]?.size ?: 0
            val valorSemBonus = applySuperStepsFrom(base, stackSize)
            val valorParaCusto = if (atual > valorSemBonus) valorSemBonus else atual

            // PROMPT 1: Explicit calculation: (Current Step - Racial Base Step)
            // Steps count: d4=0, d6=1, d8=2, d10=3, d12=4
            // Since we store values as (4, 6, 8, 10, 12), we can iterate or calculate directly.
            // The previous loop logic was: loop from base to current, incrementing cost.
            // This IS the correct logic for "cost is investment above racial base".
            // Refactoring to be clearer/more explicit if needed, but the loop is robust for d12+ handling.

            var cur = base
            while (cur < valorParaCusto) {
                cur += if (cur < 12) 2 else 1
                usados += 1
            }
        }

        val isPathfinderHuman = compendioPathfinderAtivo &&
                (ancestralidade.equals("Humano", ignoreCase = true) || ancestralidade.equals("Humano (Pathfinder)", ignoreCase = true))
        val isPathfinderHalfElf = compendioPathfinderAtivo &&
                ancestralidade.keyify().contains("MEIO-ELFO")

        val basePoints = if (isPathfinderHuman || isPathfinderHalfElf) 6 else 5

        return (basePoints + cpPaStack.size + paFromProgress - jovemMalusPa) - usados
    }

    private fun artistaMarcialPotencialFisicoAttrKey(): String? {
        if (!compendioArteDaGuerraAtivo || tropoSelecionado?.id != "tropo_artista_marcial") return null
        return when (artistaMarcialPotencialFisico?.keyify()) {
            "AGILIDADE" -> "AGILIDADE"
            "FORCA" -> "FORCA"
            "VIGOR" -> "VIGOR"
            else -> null
        }
    }

    private fun shouldApplyArtistaMarcialBonus(attrKey: String): Boolean {
        val selected = artistaMarcialPotencialFisicoAttrKey() ?: return false
        if (selected != attrKey) return false
        val base = atributoBaseRacial(attrKey)
        val stackSize = paCostStackPorAtributo[attrKey]?.size ?: 0
        val valueFromStack = applySuperStepsFrom(base, stackSize)
        return valueFromStack < atributoMaxRaw(attrKey)
    }

    fun recalcularPontosAtributo(feedbackMessages: MutableList<String> = mutableListOf()) {

        // Ensure current values meet the new racial base (e.g. if Sign increased base from d4 to d6)
        listaAtributos.forEach { attrKey ->
            val min = atributoBaseRacial(attrKey)
            val state = valoresAtributos[attrKey]
            val stack = paCostStackPorAtributo[attrKey] ?: emptyList()

            var newValue = min
            repeat(stack.size) {
                newValue = if (newValue < 12) newValue + 2 else newValue + 1
            }

            if (state != null) {
                state.intValue = newValue
            }
        }

        listaAtributos.forEach { attrKey ->
            val state = valoresAtributos[attrKey] ?: return@forEach
            if (shouldApplyArtistaMarcialBonus(attrKey)) {
                val max = atributoMaxRaw(attrKey)
                state.intValue = minOf(applySuperStepsFrom(state.intValue, 1), max)
            }
        }

        // Apply automatic super steps (e.g. from Edges/Powers) BEFORE calculating cost and trimming
        // We need to apply super modifiers to Attributes if relevant (though usually Attributes are modified by Race/Points).
        // If there are Super Attribute modifiers (e.g. Super Strength), they should be layered on top?
        // Current logic: `valoresAtributos` stores the purchased value (base + points).
        // Supers are usually handled via `atributoRawComSupers` for checks, but `valoresAtributos` is the source of truth for cost.
        // So no changes needed here unless `valoresAtributos` is expected to include Super bonuses directly (which would break cost calc).

        pontosAtributo = calcularPontosAtributoRestantes()

        trimAttributeStacks(feedbackMessages)

        // Pass 'enforcePoolLimit = false' during creation/race change to avoid aggressive clamping
        // of skills that might temporarily exceed limits due to attribute changes or trait swaps.
        // The UI/User will resolve points.
        // Actually, rebuildAllPericiaStacks calculates costs and sets values based on affordability.
        // We WANT to enforce the pool limit to keep points valid.
        rebuildAllPericiaStacks(feedbackMessages, enforcePoolLimit = true)

        // Auto-refund surplus Skill Points (e.g. from PB) if attributes made skills cheaper
        while (pontosPericia > 0 && cpSpStack.isNotEmpty()) {
             devolverPcDePericia()
             feedbackMessages.add("Ponto de Perícia (PB) devolvido devido ao aumento de Atributo.")
        }
    }

    fun selecionarSigno(novoSigno: String?) {
        if (signoAdgSelecionado == novoSigno) return

        // 1. Remove old edges from previous sign
        if (vantagensAutomaticasDoSigno.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoSigno }
            vantagensAutomaticasDoSigno.clear()
        }

        signoAdgSelecionado = novoSigno

        // 2. Add new edges
        if (novoSigno != null) {
            val edgesToAdd = mutableListOf<String>()
            when (novoSigno) {
                "Basabasa" -> edgesToAdd.add("atraente")
                "Raposa" -> edgesToAdd.add("elevar_o_moral")
                "Lobo" -> edgesToAdd.add("elo_comum")
                "Kirin" -> edgesToAdd.add("sorte")
            }

            edgesToAdd.forEach { edgeId ->
                val vant = listaVantagens.firstOrNull { it.id == edgeId }
                if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas.add(vant)
                    vantagensAutomaticasDoSigno.add(vant.id)
                }
            }
        }

        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    fun selecionarAnoesScifi(opcao: String?) {
        val ancDef = getAncestralidadeDef("ANÕES")
        val normalized = if (opcao == null) null else resolveSciFiVariantSelectionFor(
            ancestryName = "ANÕES",
            availableOptions = ancDef?.opcoes ?: emptyList(),
            overrideSelection = opcao
        )
        if (anoesScifiSelecionado == normalized && scifiVariant == normalized) return
        anoesScifiSelecionado = normalized
        if (scifiVariant != normalized) scifiVariant = normalized
        if (ancestralidade.keyify().contains("ANOES")) {
            val msgs = mutableListOf<String>()
            aplicarAncestralidade(ancestralidade, msgs)
        }
    }

    fun selecionarScifiVariant(opcao: String?) {
        val ancDef = currentAncestryDef
        val normalized = if (opcao == null) null else resolveSciFiVariantSelectionFor(
            ancestryName = ancestralidade,
            availableOptions = ancDef?.opcoes ?: emptyList(),
            overrideSelection = opcao
        )
        if (scifiVariant == normalized) return
        scifiVariant = normalized
        // Also sync legacy state if applicable to avoid mismatches
        if (ancestralidade.keyify().contains("ANOES") && anoesScifiSelecionado != normalized) {
            anoesScifiSelecionado = normalized
        }
        val msgs = mutableListOf<String>()
        aplicarAncestralidade(ancestralidade, msgs)
    }

    /**
     * Seleciona (ou limpa, com `variantId = null`) a CustomAncestryVariant ativa pra raça
     * atual e reaplica a ancestralidade pra recomputar tudo que depende de currentAncestryDef
     * (vantagensRaciais/desvantagensRaciais, atributos iniciais, etc — ver selecionarScifiVariant).
     */
    fun selecionarVarianteRacialCustom(variantId: String?) {
        if (customVarianteRacialSelecionadaId == variantId) return
        customVarianteRacialSelecionadaId = variantId
        val msgs = mutableListOf<String>()
        aplicarAncestralidade(ancestralidade, msgs)
    }

    fun selecionarHumanoMineradorAtributo(atributo: String?) {
        if (humanoMineradorAtributo == atributo) return
        humanoMineradorAtributo = atributo
        val msgs = mutableListOf<String>()
        aplicarAncestralidade(ancestralidade, msgs)
        recalcularPontosAtributo(msgs) // Ensure re-calc happens as attribute base changes
    }

    /**
     * Atualiza os traços raciais negativos escolhidos para Anões (variante Ciber).
     * Rejeita silenciosamente qualquer seleção que estoure o orçamento de
     * [AnaoCiberTraitCatalog.MAX_PONTOS] pontos — a UI já deve impedir isso, mas a
     * checagem aqui garante que o estado nunca fique inconsistente com a regra.
     */
    fun selecionarAnaoCiberTracos(novosTracos: List<AnaoCiberTraitSelection>) {
        if (AnaoCiberTraitCatalog.pontosUsados(novosTracos) > AnaoCiberTraitCatalog.MAX_PONTOS) return
        if (anaoCiberTracosSelecionados == novosTracos) return
        anaoCiberTracosSelecionados = novosTracos
        val msgs = mutableListOf<String>()
        aplicarAncestralidade(ancestralidade, msgs)
    }

    fun isFeralAdgSelecionado(): Boolean =
        compendioArteDaGuerraAtivo && ancestralidade.keyify() == "FERAL"

    private fun syncOraculoVariant() {
        if (ancestralidade.keyify() != "ORACULOS") return
        if (resolveCurrentSciFiVariantSelection() == "Aterrorizado") {
            val idx = vantagensSelecionadas.indexOfFirst { it.id == "poderes_misticos" }
            if (idx >= 0) {
                val current = vantagensSelecionadas[idx]
                if (current.choice != "Telepata") {
                    vantagensSelecionadas[idx] = current.copy(choice = "Telepata")
                    // Force refresh of Mystic Powers slots
                    val arcKey = "MISTICO"
                    if (poderSlotsPorArcano.containsKey(arcKey)) {
                        poderSlotsPorArcano.remove(arcKey)
                    }
                    // Initialize empty slots manually without calling adicionarVantagem to avoid duplicates
                    val count = getSlotsCountForArcano(arcKey)
                    val initialSlots = mutableStateListOf<String?>().apply { repeat(count) { add(null) } }
                    poderSlotsPorArcano[arcKey] = initialSlots
                }
            }
        }
    }

    fun selecionarPacoteCulturalFantasia(novoPacote: String) {
        if (pacoteCulturalFantasiaSelecionado == novoPacote) return
        pacoteCulturalFantasiaSelecionado = novoPacote

        // Reset sub-options when changing package
        povoDoMarOpcao = null
        senhoresCavalosExtra = false
        senhoresCavalosCompensacao = null

        if (!temAdaptavel() && vantagemAdaptavelSelecionadaId != null) {
            val toRemove = vantagensSelecionadas.find { it.id == vantagemAdaptavelSelecionadaId }
            if (toRemove != null) {
                removerVantagem(toRemove)
            }
            vantagemAdaptavelSelecionadaId = null
        }

        syncPacoteCulturalFantasia()
        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    fun selecionarPovoDoMarOpcao(opcao: String?): String? {
        if (povoDoMarOpcao == opcao) return null

        if (opcao == "Procurado (Maior)") {
            val temManual = complicacoesSelecionadas.keys.any {
                it.id.keyify() == "PROCURADO" && complicacoesSelecionadas[it] == "Maior" && !desvantagensRaciais.contains(it.name)
            }
            if (temManual) {
                return "Remova 'Procurado (Maior)' das complicações manuais antes de escolher esta opção."
            }
        }

        povoDoMarOpcao = opcao
        syncPacoteCulturalFantasia()
        rebuildAllPericiaStacks()
        return null
    }

    fun toggleSenhoresCavalosExtra(checked: Boolean): String? {
        if (senhoresCavalosExtra == checked) return null

        if (checked) {
            val jaTemNascido = vantagensSelecionadas.any { it.id == "nascido_na_sela" && !vantagensRaciais.contains("nascido_na_sela") }
            if (jaTemNascido) {
                return "Remova a Vantagem 'Nascido na Sela' manual antes de escolher esta opção."
            }
        }

        senhoresCavalosExtra = checked
        if (!checked) senhoresCavalosCompensacao = null
        syncPacoteCulturalFantasia()
        rebuildAllPericiaStacks()
        return null
    }

    fun selecionarSenhoresCavalosCompensacao(opcao: String?): String? {
        if (senhoresCavalosCompensacao == opcao) return null

        if (opcao == "Código de Honra") {
            val temManual = complicacoesSelecionadas.keys.any {
                it.id.keyify() == "CODIGO DE HONRA" && !desvantagensRaciais.contains(it.name)
            }
            if (temManual) {
                return "Remova 'Código de Honra' das complicações manuais antes de escolher esta opção."
            }
        } else if (opcao == "Sem Escrúpulos e Analfabeto") {
             val temSemEscrupulos = complicacoesSelecionadas.keys.any { it.id.keyify() == "SEM_ESCRUPULOS" && !desvantagensRaciais.contains(it.name) }
             val temAnalfabeto = complicacoesSelecionadas.keys.any { it.id.keyify() == "ANALFABETO" && !desvantagensRaciais.contains(it.name) }

             if (temSemEscrupulos && temAnalfabeto) return "Remova 'Sem Escrúpulos' e 'Analfabeto' das complicações manuais antes de escolher esta opção."
             if (temSemEscrupulos) return "Remova 'Sem Escrúpulos' das complicações manuais antes de escolher esta opção."
             if (temAnalfabeto) return "Remova 'Analfabeto' das complicações manuais antes de escolher esta opção."
        }

        senhoresCavalosCompensacao = opcao
        syncPacoteCulturalFantasia()
        rebuildAllPericiaStacks()
        return null
    }

    private fun syncPacoteCulturalFantasia() {
        if (!isHumanoFantasiaSelecionado()) return

        val ancDef = currentAncestryDef

        // --- Atualiza Vantagens Raciais ---
        val baseVantagens = ancDef?.let { effectiveVantagensGratis(it) } ?: emptyList()
        val extrasVantagens = mutableListOf<String>()

        when (pacoteCulturalFantasiaSelecionado) {
            "Nômades do Deserto" -> extrasVantagens.add("RESISTÊNCIA AMBIENTAL (Calor)")
            "Povo da Montanha" -> extrasVantagens.add("RESISTÊNCIA AMBIENTAL (Frio)")
            "Senhores dos Cavalos" -> {
                if (senhoresCavalosExtra) {
                    extrasVantagens.add("nascido_na_sela")
                }
            }
        }

        // Remove "ADAPTAVEL" se não for Humano Padrão (embora temAdaptavel() já trate a lógica,
        // é bom limpar a lista visual se estiver sendo usada para display)
        val filteredBaseVantagens = if (pacoteCulturalFantasiaSelecionado != "Humano padrão") {
            baseVantagens.filter { it.keyify() != "ADAPTAVEL" }
        } else {
            baseVantagens
        }

        vantagensRaciais.clear()
        vantagensRaciais.addAll(filteredBaseVantagens + extrasVantagens)

        // --- Atualiza Desvantagens Raciais ---
        val baseDesvantagens = ancDef?.let { effectiveDesvantagens(it) } ?: emptyList()
        val extrasDesvantagens = mutableListOf<String>()

        when (pacoteCulturalFantasiaSelecionado) {
            "Povo do Mar" -> {
                if (povoDoMarOpcao == "Procurado (Maior)") {
                    extrasDesvantagens.add("PROCURADO (Maior)")
                }
            }
            "Senhores dos Cavalos" -> {
                if (senhoresCavalosExtra) {
                    if (senhoresCavalosCompensacao == "Código de Honra") {
                        extrasDesvantagens.add("CODIGO DE HONRA")
                    } else if (senhoresCavalosCompensacao == "Sem Escrúpulos e Analfabeto") {
                        extrasDesvantagens.add("SEM ESCRÚPULOS (Menor)")
                        extrasDesvantagens.add("ANALFABETO")
                    }
                }
            }
        }

        val oldAuto = desvantagensRaciais.toList()
        desvantagensRaciais.clear()
        desvantagensRaciais.addAll(baseDesvantagens + extrasDesvantagens)

        val snapshot = resolveAncestryComplicationsSnapshotUseCase.execute(
            ResolveAncestryComplicationsSnapshotUseCase.Params(
                previousAutomaticDisadvantages = oldAuto,
                currentAutomaticDisadvantages = desvantagensRaciais.toList(),
                availableComplications = listaComplicacoes,
                selectedComplications = complicacoesSelecionadas,
                originPriorityResolver = { getOriginPriority(it) }
            )
        )
        complicacoesSelecionadas.clear()
        complicacoesSelecionadas.putAll(snapshot.selectedComplications)
    }

    private fun syncArtistaMarcialPotencialFisico() {
        if (vantagensAutomaticasDoPotencialFisico.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoPotencialFisico }
            vantagensAutomaticasDoPotencialFisico.clear()
        }

        if (tropoSelecionado?.id != "tropo_artista_marcial") return

        val edgeId = when (artistaMarcialPotencialFisico?.keyify()) {
            "AGILIDADE" -> "esquiva"
            "FORCA" -> "bloquear"
            "VIGOR" -> "reflexos_de_combate"
            else -> null
        } ?: return

        val vant = listaVantagens.firstOrNull { it.id == edgeId }
        if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
            vantagensSelecionadas.add(vant)
            vantagensAutomaticasDoPotencialFisico.add(vant.id)
        }
    }

    fun atualizarArtistaMarcialJutsuOpcao(novaOpcao: String) {
        if (artistaMarcialJutsuOpcao == novaOpcao) return
        artistaMarcialJutsuOpcao = novaOpcao
        rebuildAllPericiaStacks()
        syncJutsuSlots()
    }

    fun atualizarArtistaMarcialPotencialFisico(novoPotencial: String?) {
        if (artistaMarcialPotencialFisico == novoPotencial) return
        artistaMarcialPotencialFisico = novoPotencial
        syncArtistaMarcialPotencialFisico()
        recalcularPontosAtributo()
    }

    private fun syncYouxiaKensai() {
        if (tropoSelecionado?.id != "tropo_youxia") return
        val choice = "Lutar"
        val idx = vantagensSelecionadas.indexOfFirst { it.id == "arma_predileta" }
        if (idx >= 0) {
            val current = vantagensSelecionadas[idx]
            if (current.choice != choice) {
                vantagensSelecionadas[idx] = current.copy(choice = choice)
            }
        } else {
            val vant = listaVantagens.firstOrNull { it.id == "arma_predileta" } ?: return
            vantagensSelecionadas.add(vant.copy(choice = choice))
            vantagensAutomaticasDoTropo.add(vant.id)
        }

        if (youxiaJutsuSelecionado.isNullOrBlank()) {
            notasPericia.remove("Lutar")
        } else {
            notasPericia["Lutar"] = youxiaJutsuSelecionado!!
        }
    }

    fun atualizarYouxiaJutsuSelecionado(novoJutsu: String) {
        val normalized = novoJutsu.trim().ifBlank { null }
        if (youxiaJutsuSelecionado == normalized) return
        youxiaJutsuSelecionado = normalized
        syncYouxiaKensai()
        rebuildAllPericiaStacks()
    }

    fun atualizarYouxiaHistoricoSelecionado(novoHistorico: String) {
        if (youxiaHistoricoSelecionado == novoHistorico) return
        youxiaHistoricoSelecionado = novoHistorico
    }

    private fun syncSamuraiVantagemEscolhida() {
        if (tropoSelecionado?.id != "tropo_samurai") return
        if (samuraiVantagemEscolhida == "Combate") {
            val comando = listaVantagens.firstOrNull { it.id == "comando" }
            if (comando != null) {
                vantagensSelecionadas.removeAll { it.id == comando.id }
            }
            vantagensAutomaticasDoTropo.removeAll { it == "comando" }
        } else {
            val comando = listaVantagens.firstOrNull { it.id == "comando" }
            if (comando != null && vantagensSelecionadas.none { it.id == comando.id }) {
                vantagensSelecionadas.add(comando)
            }
            if (!vantagensAutomaticasDoTropo.contains("comando")) {
                vantagensAutomaticasDoTropo.add("comando")
            }
            if (samuraiCombatSlotIds.isNotEmpty()) {
                vantagensSelecionadas.removeAll { it.id in samuraiCombatSlotIds }
                samuraiCombatSlotIds.clear()
            }
        }
    }

    fun atualizarSamuraiVantagemEscolhida(nova: String) {
        if (samuraiVantagemEscolhida == nova) return
        samuraiVantagemEscolhida = nova
        syncSamuraiVantagemEscolhida()
    }

    fun atualizarSamuraiPericiaEscolhida(nova: String) {
        if (samuraiPericiaEscolhida == nova) return
        samuraiPericiaEscolhida = nova
        rebuildAllPericiaStacks()
    }

    fun toggleSamuraiPostura(postura: String) {
        if (samuraiPosturasSelecionadas.contains(postura)) {
            samuraiPosturasSelecionadas.remove(postura)
            return
        }
        if (samuraiPosturasSelecionadas.size >= 2) return
        samuraiPosturasSelecionadas.add(postura)
    }

    fun toggleArtistaMarcialTecnica(tecnica: String) {
        if (artistaMarcialTecnicasSelecionadas.contains(tecnica)) {
            artistaMarcialTecnicasSelecionadas.remove(tecnica)
            return
        }
        if (artistaMarcialTecnicasSelecionadas.size >= 3) return
        artistaMarcialTecnicasSelecionadas.add(tecnica)
    }

    fun selecionarPericiaGnomo(pericia: String?) {
        if (gnomoPericiaEscolhida == pericia) return
        gnomoPericiaEscolhida = pericia
        rebuildAllPericiaStacks()
    }

    fun selecionarPericiaKitsunemimi(pericia: String?) {
        if (kitsunemimiPericiaEscolhida == pericia) return
        kitsunemimiPericiaEscolhida = pericia
        rebuildAllPericiaStacks()
    }

    fun selecionarPericiaUsagimimi(
        pericia: String?,
        feedbackMessages: MutableList<String> = mutableListOf()
    ) {
        if (usagimimiPericiaEscolhida == pericia) return
        usagimimiPericiaEscolhida = pericia
        if (usagimimiPericiaEscolhida?.keyify() == "TRANSICAO") {
            feedbackMessages.add("Perícia favorita Transição selecionada: apenas Elementalista (ou Sem Tropo) fica disponível.")
        }
        if (isUsagimimiTransicaoRestrictionActive() && tropoSelecionado?.id != "tropo_elementalista") {
            feedbackMessages.add("Tropo atual removido por incompatibilidade com Transição. As perícias foram recalculadas.")
            selecionarTropo(null, feedbackMessages)
            return
        }
        rebuildAllPericiaStacks(feedbackMessages)
    }

    fun isUsagimimiTransicaoRestrictionActive(): Boolean {
        return compendioArteDaGuerraAtivo &&
            ancestralidade.keyify().contains("USAGIMIMI") &&
            usagimimiPericiaEscolhida?.keyify() == "TRANSICAO"
    }

    fun podeSelecionarTropoPorRestricoesAtuais(tropo: Tropo?): Boolean {
        if (modoLivre) return true
        if (!isUsagimimiTransicaoRestrictionActive()) return true
        return tropo == null || tropo.id == "tropo_elementalista"
    }

    fun updateProtagonistaRollTecnicas(value: Int?) {
        if (protagonistaRollTecnicas == value) return
        protagonistaRollTecnicas = value?.coerceIn(1, 4)
        syncMestreDoChiSlots()
    }

    fun updateProtagonistaRollPericia(value: Int?) {
        if (protagonistaRollPericia == value) return
        protagonistaRollPericia = value?.coerceIn(1, 6)
        if (protagonistaRollPericia != 1) {
            protagonistaPericiasEscolhidas = emptyList()
        }
        rebuildAllPericiaStacks()
    }

    fun updateProtagonistaRollVantagem(value: Int?) {
        if (protagonistaRollVantagem == value) return
        protagonistaRollVantagem = value?.coerceIn(1, 8)
    }

    fun updateProtagonistaRollQualidade(value: Int?) {
        if (protagonistaRollQualidade == value) return
        protagonistaRollQualidade = value?.coerceIn(1, 10)
        if (protagonistaRollQualidade != 7) {
            protagonistaPericiasPaixao = emptyList()
        }
        atualizarProtagonistaAutoVantagens()
        recalcularPontosAtributo()
    }

    fun updateProtagonistaRollHabilidade(value: Int?) {
        if (protagonistaRollHabilidade == value) return
        protagonistaRollHabilidade = value?.coerceIn(1, 12)
        syncProtagonistaBonusPv()
    }

    fun updateProtagonistaPericiasEscolhidas(value: List<String>) {
        protagonistaPericiasEscolhidas = value
        rebuildAllPericiaStacks()
    }

    fun updateProtagonistaPericiasPaixao(value: List<String>) {
        protagonistaPericiasPaixao = value
        rebuildAllPericiaStacks()
    }

    private fun tecnicasIniciaisProtagonista(): Int {
        return when (protagonistaRollTecnicas) {
            1 -> 1
            2 -> 2
            3 -> 2
            4 -> 3
            else -> 0
        }
    }

    private fun protagonistaPericiasDoTropo(): Set<String> {
        val roll = protagonistaRollPericia ?: return emptySet()
        val paixaoAtiva = protagonistaRollQualidade == 7
        val paixaoList = if (paixaoAtiva) protagonistaPericiasPaixao else emptyList()
        return if (roll == 1) {
            (protagonistaPericiasEscolhidas + paixaoList)
                .map { it.keyify() }
                .toSet()
        } else {
            val pericia = when (roll) {
                2 -> "Atletismo"
                3 -> "Conhecimento Geral"
                4 -> "Perceber"
                5 -> "Persuadir"
                6 -> "Furtividade"
                else -> null
            }
            val baseList = buildList {
                if (pericia != null) add(pericia)
                addAll(paixaoList)
            }
            baseList.map { it.keyify() }.toSet()
        }
    }

    private fun atualizarProtagonistaAutoVantagens() {
        if (vantagensAutomaticasDoProtagonista.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoProtagonista }
            vantagensAutomaticasDoProtagonista.clear()
        }

        val qualidade = protagonistaRollQualidade ?: return
        val edgesToAdd = when (qualidade) {
            1 -> listOf("corajoso", "elevar_o_moral")
            3 -> listOf("confiavel", "comando")
            else -> emptyList()
        }

        edgesToAdd.forEach { edgeId ->
            val vant = listaVantagens.firstOrNull { it.id == edgeId }
            if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                vantagensSelecionadas.add(vant)
                vantagensAutomaticasDoProtagonista.add(vant.id)
            }
        }
    }

    private fun syncProtagonistaBonusPv() {
        val shouldHaveBonus = compendioArteDaGuerraAtivo &&
            tropoSelecionado?.id == "tropo_protagonista" &&
            protagonistaRollHabilidade == 4

        if (shouldHaveBonus && !protagonistaBonusPv) {
            pontosVantagem += 1
            protagonistaBonusPv = true
        } else if (!shouldHaveBonus && protagonistaBonusPv) {
            if (pontosVantagem > 0) {
                pontosVantagem = (pontosVantagem - 1).coerceAtLeast(0)
            } else {
                removerUltimaVantagemCompradaComPv()
            }
            protagonistaBonusPv = false
        }
    }

    private fun syncMestreDoChiSlots() {
        rebuildAllPericiaStacks()
        poderSlotsPorArcano["MESTRE DO CHI"]?.let { slots ->
            val required = getSlotsCountForArcano("MESTRE DO CHI")
            while (slots.size < required) slots.add(null)
            while (slots.size > required && slots.lastOrNull() == null) {
                slots.removeAt(slots.lastIndex)
            }
            syncPoderesSelecionadosFromSlots()
        }
    }

    fun selecionarDescendenteElemental(novoElemento: String?) {
        if (descendenteElementalSelecionado == novoElemento) return

        // 1. Remove old edges
        if (vantagensAutomaticasDoElemento.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoElemento }
            vantagensAutomaticasDoElemento.clear()
        }
        if (habilidadesRaciaisDoElemento.isNotEmpty()) {
            vantagensRaciais.removeAll(habilidadesRaciaisDoElemento.toSet())
            habilidadesRaciaisDoElemento.clear()
        }

        descendenteElementalSelecionado = novoElemento

        // 2. Add new edges
        if (novoElemento != null) {
            val edgesToAdd = mutableListOf<String>()
            val racialTraits = mutableListOf<String>()
            when (novoElemento) {
                "Ar" -> {
                    // AR INTERNO is preserved in base list, so we don't need to add it again
                    racialTraits += listOf("RESISTÊNCIA AMBIENTAL (Ar)")
                }
                "Água" -> {
                    // AQUÁTICO is preserved in base list
                    racialTraits += listOf("RESISTÊNCIA AMBIENTAL (Água)")
                }
                "Fogo" -> {
                    edgesToAdd.add("rapido")
                    // RÁPIDO (trait text) is preserved in base list
                    racialTraits += listOf("RESISTÊNCIA AMBIENTAL (Fogo)")
                }
                "Terra" -> {
                    // SÓLIDO COMO ROCHA is preserved in base list
                    racialTraits += listOf("RESISTÊNCIA AMBIENTAL (Terra)")
                }
            }

            racialTraits.forEach { trait ->
                if (vantagensRaciais.none { it.keyify() == trait.keyify() }) {
                    vantagensRaciais.add(trait)
                    habilidadesRaciaisDoElemento.add(trait)
                }
            }

            edgesToAdd.forEach { edgeId ->
                val vant = listaVantagens.firstOrNull { it.id == edgeId || it.nome.keyify() == edgeId.keyify() }
                if (vant != null && vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas.add(vant)
                    vantagensAutomaticasDoElemento.add(vant.id)
                }
            }
        }

        recalcularPontosAtributo()
        rebuildAllPericiaStacks()
    }

    fun selecionarTropo(
        novoTropo: Tropo?,
        feedbackMessages: MutableList<String> = mutableListOf()
    ) {
        if (tropoSelecionado?.id == novoTropo?.id) return
        if (!podeSelecionarTropoPorRestricoesAtuais(novoTropo)) {
            feedbackMessages.add("Tropo bloqueado: com Transição favorita, apenas Elementalista (ou Sem Tropo) pode ser selecionado.")
            return
        }

        if (vantagensAutomaticasDoTropo.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoTropo }
            vantagensAutomaticasDoTropo.clear()
        }
        if (vantagensAutomaticasDoPotencialFisico.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoPotencialFisico }
            vantagensAutomaticasDoPotencialFisico.clear()
        }
        if (vantagensAutomaticasDoProtagonista.isNotEmpty()) {
            vantagensSelecionadas.removeAll { it.id in vantagensAutomaticasDoProtagonista }
            vantagensAutomaticasDoProtagonista.clear()
        }
        if (tropoSelecionado?.id != "tropo_artista_marcial") {
            artistaMarcialTecnicasSelecionadas.clear()
        }
        if (tropoSelecionado?.id != "tropo_buxista") {
            buXistaCaminhoSelecionado = null
        }
        if (tropoSelecionado?.id != "tropo_elementalista") {
            elementalistaElementoSelecionado = null
        }
        if (tropoSelecionado?.id != "tropo_kui") {
            kuiFerramentaSelecionada = null
        }
        if (tropoSelecionado?.id != "tropo_samurai") {
            samuraiPericiaEscolhida = null
            samuraiVantagemEscolhida = null
            samuraiPosturasSelecionadas.clear()
            samuraiCombatSlotIds.clear()
        }
        if (tropoSelecionado?.id != "tropo_shinobi") {
            shinobiTalentoSelecionado = null
            shinobiTreinamentoSelecionado = null
        }
        if (tropoSelecionado?.id != "tropo_youxia") {
            if (!youxiaJutsuSelecionado.isNullOrBlank() && notasPericia["Lutar"] == youxiaJutsuSelecionado) {
                notasPericia.remove("Lutar")
            }
            youxiaJutsuSelecionado = null
            youxiaHistoricoSelecionado = null
        }

        tropoSelecionado = novoTropo

        if (novoTropo != null) {
            novoTropo.ganhaAoComprar.forEach { vantId ->
                val vant = listaVantagens.firstOrNull { it.id == vantId } ?: return@forEach
                if (vantagensSelecionadas.none { it.id == vant.id }) {
                    vantagensSelecionadas += vant
                    vantagensAutomaticasDoTropo += vant.id
                }
            }

            // PROMPT: Equipamentos de Tropo
            // Se encontrar equipamento com ID "kit_[nome_tropo_limpo]", adiciona.
            // ID trope example: tropo_samurai -> clean: samurai -> kit_samurai
            if (compendioArteDaGuerraAtivo) {
                val suffix = novoTropo.id.removePrefix("tropo_")

                // Remove previous kits?
                // Better to assume user manages inventory, but if switching tropes repeatedly it might clutter.
                // For now, just add.
                val kitItem = listaEquipamentos.firstOrNull {
                    val key = it.nome.keyify()
                    key == "kit_$suffix" || key == "kit_de_$suffix"
                }
                if (kitItem != null) {
                    equipamentosComprados.add(kitItem)
                }
            }
            if (novoTropo.id != "tropo_protagonista") {
                protagonistaRollTecnicas = null
                protagonistaRollPericia = null
                protagonistaRollVantagem = null
                protagonistaRollQualidade = null
                protagonistaRollHabilidade = null
                protagonistaPericiasEscolhidas = emptyList()
                protagonistaPericiasPaixao = emptyList()
                vantagensSlotProtagonista.clear()
                syncProtagonistaBonusPv()
            } else {
                atualizarProtagonistaAutoVantagens()
                syncProtagonistaBonusPv()
            }
            if (novoTropo.id == "tropo_artista_marcial" && artistaMarcialPotencialFisico == null) {
                artistaMarcialPotencialFisico = "Agilidade"
            }
            syncArtistaMarcialPotencialFisico()
            if (novoTropo.id != "tropo_artista_marcial") {
                artistaMarcialTecnicasSelecionadas.clear()
            }
            if (novoTropo.id == "tropo_buxista" && buXistaCaminhoSelecionado == null) {
                buXistaCaminhoSelecionado = "Equilibrado"
            }
            if (novoTropo.id == "tropo_elementalista" && elementalistaElementoSelecionado == null) {
                elementalistaElementoSelecionado = "Fogo"
            }
            if (novoTropo.id == "tropo_kui" && kuiFerramentaSelecionada == null) {
                kuiFerramentaSelecionada = "Armas Abençoadas"
            }
            if (novoTropo.id == "tropo_samurai") {
                if (samuraiPericiaEscolhida == null) {
                    samuraiPericiaEscolhida = "Jutsu"
                }
                if (samuraiVantagemEscolhida == null) {
                    samuraiVantagemEscolhida = "Comando"
                }
                syncSamuraiVantagemEscolhida()
            }
            if (novoTropo.id == "tropo_shinobi") {
                if (shinobiTalentoSelecionado == null) {
                    shinobiTalentoSelecionado = "Alteração"
                }
                if (shinobiTreinamentoSelecionado == null) {
                    shinobiTreinamentoSelecionado = "Infiltrador"
                }
            }
            if (novoTropo.id == "tropo_youxia") {
                if (youxiaJutsuSelecionado == null) {
                    youxiaJutsuSelecionado = "Espada"
                }
                if (youxiaHistoricoSelecionado == null) {
                    youxiaHistoricoSelecionado = "Ancestral"
                }
                syncYouxiaKensai()
            }
        } else {
            protagonistaRollTecnicas = null
            protagonistaRollPericia = null
            protagonistaRollVantagem = null
            protagonistaRollQualidade = null
            protagonistaRollHabilidade = null
            protagonistaPericiasEscolhidas = emptyList()
            protagonistaPericiasPaixao = emptyList()
            vantagensSlotProtagonista.clear()
            syncProtagonistaBonusPv()
            syncArtistaMarcialPotencialFisico()
            artistaMarcialTecnicasSelecionadas.clear()
            buXistaCaminhoSelecionado = null
            elementalistaElementoSelecionado = null
            kuiFerramentaSelecionada = null
            samuraiPericiaEscolhida = null
            samuraiVantagemEscolhida = null
            samuraiPosturasSelecionadas.clear()
            samuraiCombatSlotIds.clear()
            shinobiTalentoSelecionado = null
            shinobiTreinamentoSelecionado = null
            if (!youxiaJutsuSelecionado.isNullOrBlank() && notasPericia["Lutar"] == youxiaJutsuSelecionado) {
                notasPericia.remove("Lutar")
            }
            youxiaJutsuSelecionado = null
            youxiaHistoricoSelecionado = null
        }

        syncMestreDoChiSlots()
        recalcularPontosAtributo()
        rebuildAllPericiaStacks(feedbackMessages)
        syncJutsuSlots()
    }

    private fun trimAttributeStacks(feedbackMessages: MutableList<String> = mutableListOf()) {

        listaAtributos.forEach { nomeAttr ->
            val stack = paCostStackPorAtributo[nomeAttr] ?: return@forEach
            var maxAllowed = atributoMaxRawNaCriacao(nomeAttr)
            var current = valoresAtributos[nomeAttr]?.intValue ?: return@forEach

            while (current > maxAllowed && stack.isNotEmpty()) {
                stack.removeAt(stack.lastIndex)
                current = if (current > 12) current - 1 else current - 2
                valoresAtributos[nomeAttr]?.intValue = current.coerceAtLeast(atributoBaseRacial(nomeAttr))
                feedbackMessages.add("Atributo $nomeAttr reduzido para respeitar o limite racial.")
                pontosAtributo = calcularPontosAtributoRestantes()
                maxAllowed = atributoMaxRawNaCriacao(nomeAttr)
                current = valoresAtributos[nomeAttr]?.intValue ?: current
            }

            if (current > maxAllowed) {
                valoresAtributos[nomeAttr]?.intValue = maxAllowed
                feedbackMessages.add("Atributo $nomeAttr ajustado para o limite racial.")
                pontosAtributo = calcularPontosAtributoRestantes()
            }
        }

        while (pontosAtributo < 0) {
            val entry = paCostStackPorAtributo
                .entries
                .firstOrNull { it.value.isNotEmpty() }
                ?: break

            val nomeAttr = entry.key
            val stack    = entry.value

            stack.removeAt(stack.size - 1)

            val base = atributoBaseRacial(nomeAttr)

            val atual = valoresAtributos[nomeAttr]!!.intValue

            val novo = if (atual > 12) atual - 1 else atual - 2
            valoresAtributos[nomeAttr]!!.intValue = novo.coerceAtLeast(base)

            feedbackMessages.add("Atributo $nomeAttr reduzido para ${novo.coerceAtLeast(base).toDiceString()} para compensar pontos.")

            pontosAtributo = calcularPontosAtributoRestantes()
        }
    }

    fun applyYoungMinor() {
        jovemAutoPequeno = false
        jovemMalusPa = 1
        jovemMalusSp = 2
        recalcularPontosAtributo()
    }

    fun applyYoungMajor(pequComp: Complicacao) {
        jovemAutoPequeno = true
        jovemMalusPa = 2
        jovemMalusSp = 2
        desvantagensAutomaticas.add(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas[pequComp] = "Menor"
        recalcularPontosAtributo()
    }

    fun removeYoung(pequComp: Complicacao) {
        jovemAutoPequeno = false
        jovemMalusPa = 0
        jovemMalusSp = 0
        desvantagensAutomaticas.remove(pequComp.id.substringBefore("(").trim())
        complicacoesSelecionadas.remove(pequComp)
        recalcularPontosAtributo()
    }

    /**
     * Zera os modificadores derivados de Complicações de idade/peso e o bônus
     * de Ponto de Poder de trópicos. `jovemMalusPa`/`jovemMalusSp` são private,
     * então isso precisa viver aqui (não dá pra zerar direto de fora) — usado ao
     * iniciar um personagem novo, já que a mesma instância de [CriadorState] é
     * reaproveitada entre personagens e esses campos não voltam ao padrão
     * sozinhos.
     */
    fun resetComplicationDerivedModifiers() {
        jovemAutoPequeno = false
        jovemMalusPa = 0
        jovemMalusSp = 0
        idosoBonusSp = 0
        obesoBonusSize = 0
        obesoMalusMov = 0
        bonusPoderExtra = 0
    }

    var emProgresso by mutableStateOf(false)
    var modoProgressaoAtivo by mutableStateOf(false)
    var mostrandoVantagensProgresso by mutableStateOf(false)
    var mostrandoPericiasProgresso by mutableStateOf(false)
    var mostrandoAtributosProgresso by mutableStateOf(false)
    var mostrandoPoderesProgresso by mutableStateOf(false)
    val frozenSkillIncrements = mutableStateMapOf<String, Int>()

    // Novas variáveis para rastrear o avanço de perícias
    var skillAdvancementInProgress by mutableStateOf(false)
    val skillsForCurrentAdvancement = mutableStateListOf<String>()

    // Novas variáveis para rastrear o avanço de vantagens
    var advantageAdvancementInProgress by mutableStateOf(false)
    var advantageForCurrentAdvancement by mutableStateOf<String?>(null)

    // Avanço de atributos
    var attributeAdvancementInProgress by mutableStateOf(false)
    var attributeStageForCurrentAdvancement by mutableStateOf<String?>(null)
    var stageNameForCurrentAdvancement by mutableStateOf<String?>(null)
    var attributeStacksBeforeAdvancement by mutableStateOf<Map<String, Int>?>(null)
    var attributeUsedReservation by mutableStateOf(false)

    val advancementHistory = mutableStateListOf<com.example.swadebuilder.model.AdvancementAction>()

    fun updateEmProgressoFlag() {
        emProgresso =
            skillAdvancementInProgress ||
                    advantageAdvancementInProgress ||
                    attributeAdvancementInProgress
    }

    fun snapshotFrozenSkillIncrements() {
        frozenSkillIncrements.clear()
        baseIncsPorPericia.forEach { (pericia, incs) ->
            frozenSkillIncrements[pericia.nome] = incs
        }
    }

    fun increasePericiaFromAdvancement(per: Pericia, cost: Int, feedbackMessages: MutableList<String>? = null) {
        // Safety check for creation mode + Idoso
        if (!modoProgressaoAtivo && !modoLivre) {
             val hasIdoso = complicacoesSelecionadas.keys.any { it.id.keyify() == "IDOSO" }
             if (hasIdoso && per.atributo != "ASTUCIA") {
                 val spentOnSmarts = periciasComIdiomas()
                     .filter { it.atributo == "ASTUCIA" }
                     .sumOf { spCostStackPorPericia[it]?.sum() ?: 0 }
                 if (spentOnSmarts < 5) {
                     feedbackMessages?.add("Distribua ao menos 5 pontos em perícias de astúcia antes.")
                     return
                 }
             }
        }

        if (skillAdvancementInProgress) {
            skillsForCurrentAdvancement.add(per.nome)
        }

        // Ensure entry exists before accessing
        if (!baseIncsPorPericia.containsKey(per)) baseIncsPorPericia[per] = 0
        if (!spCostStackPorPericia.containsKey(per)) spCostStackPorPericia[per] = mutableStateListOf()

        baseIncsPorPericia[per] = baseIncsPorPericia.getValue(per) + 1
        spCostStackPorPericia.getValue(per).add(cost)
        if (isIdiomaPericia(per)) {
            if (notasPericia[per.nome].isNullOrBlank()) {
                notasPericia[per.nome] = idiomaDefaultLabel(per)
            }
            syncIdiomaSlots()
        }
        if (isJutsuPericia(per)) {
            syncJutsuSlots()
        }
    }

    fun baseCreationComplete(): Boolean = pontosAtributo == 0 &&
            pontosPericia == 0 &&
            pontosVantagem == 0 &&
            (pontosComplicacao - pontosComplicacaoGastos).coerceAtLeast(0) == 0

    fun checkIdosoConstraint(pericia: Pericia): Boolean {
        // Only active if Idoso complication is present
        val idosoKey = "IDOSO"
        val hasIdoso = complicacoesSelecionadas.keys.any { it.id.keyify() == idosoKey }
        if (!hasIdoso) return true

        // If trying to reduce a Smarts-based skill
        if (pericia.atributo == "ASTUCIA") {
            // Check if removing this point would violate the 5-point minimum IF we have spent points on non-Smarts skills?
            // Actually, simply returning true allows reduction. The constraint is checked at completion.
            return true
        }
        return true
    }

    fun creationComplete(): Boolean {
        // "Ficha básica completa": todos os pontos iniciais foram distribuídos.
        // Em campanha supers, também exige ter zerado os Pontos de Super.
        val supersProntos = !modoSupers || (superPontosTotais > 0 && superPontosDisponiveis == 0)

        // Check Arcane Background Powers
        if (temAntecedenteArcano()) {
             // Check if powers are selected for each AB
             val pending = poderSlotsPorArcano.any { (_, slots) -> slots.any { it == null } }
             if (pending) return false
        }

        // Check Adaptavel Slot (includes Sobrevivente)
        if (temAdaptavel() && vantagemAdaptavelSelecionadaId == null) {
            return false
        }

        // Check Pathfinder Free Slot
        if (pathfinderSlotAvailable) {
            return false
        }

        // Check Protagonista Free Slot
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_protagonista" && vantagensSlotProtagonista.isEmpty()) {
            return false
        }

        // Check Samurai Free Combat Slot
        if (compendioArteDaGuerraAtivo && tropoSelecionado?.id == "tropo_samurai" && samuraiVantagemEscolhida == "Combate" && samuraiCombatSlotIds.isEmpty()) {
            return false
        }

        // Check Idoso constraint
        val hasIdoso = complicacoesSelecionadas.keys.any { it.id.keyify() == "IDOSO" }
        if (hasIdoso) {
             // "ter gasto ao menos 5 sp em perícias de Astúcia"
             val spentOnSmarts = periciasComIdiomas()
                 .filter { it.atributo == "ASTUCIA" }
                 .sumOf { spCostStackPorPericia[it]?.sum() ?: 0 }

             if (spentOnSmarts < 5) return false
        }

        return baseCreationComplete() && supersProntos
    }

    val criacaoBasicaCongelada: Boolean
        get() {
            val supersEmDistribuicao =
                !modoProgressaoAtivo &&
                    faseSupersAtiva &&
                    superNivelCampanha != null &&
                    superPontosTotais > 0

            if (supersEmDistribuicao) return true

            return creationComplete() && modoProgressaoAtivo
        }

    val criacaoBasicaCongeladaComXp: Boolean
        get() = criacaoBasicaCongelada && !emProgresso

    val stageXpSpent: SnapshotStateMap<String, Int> = mutableStateMapOf<String, Int>().apply {
        listaDeEstagios.forEach { this[it.nome] = 0 }
    }

    var progressosDisponiveis by mutableIntStateOf(0)

    val xpSlots = mutableStateListOf<Boolean>().apply {
        repeat(20) { add(false) }
    }

    private fun reachedStages(): List<Estagio> =
        listaDeEstagios.filter { progresso >= it.minProgress }

    fun atributoRawBaseSemSupers(attrKey: String): Int {
        val key = attrKey.uppercase().trim()
        val mods = racialAttrMinMap[ancestralidade] ?: emptyMap()
        val baseMin = mods[key] ?: 4

        // Quantos "steps" base foram comprados na criação
        val stepsBase = paCostStackPorAtributo[key]?.size ?: 0

        var raw = baseMin
        repeat(stepsBase) {
            raw += if (raw < 12) 2 else 1
        }
        return raw
    }

    fun snapshotAttributeStacks(): Map<String, Int> =
        paCostStackPorAtributo.mapValues { (_, stack) -> stack.size }

    fun restoreAttributeStacks(snapshot: Map<String, Int>) {
        listaAtributos.forEach { attr ->
            val stack = paCostStackPorAtributo.getValue(attr)
            val target = snapshot[attr] ?: 0
            while (stack.size > target) {
                stack.removeAt(stack.lastIndex)
                val current = valoresAtributos[attr]!!.intValue
                val prev = if (current > 12) current - 1 else current - 2
                valoresAtributos[attr]!!.intValue = prev
            }
        }
        recalcularPontosAtributo()
    }

    fun rebuildAllPericiaStacks(
        feedbackMessages: MutableList<String> = mutableListOf(),
        enforcePoolLimit: Boolean = true,
        desiredRawValues: Map<String, Int>? = null
    ) {
        if (modoProgressaoAtivo) return

        syncLinguistaIdiomas()
        syncJutsuSlots()

        val pericias = periciasComIdiomas()
        val activeSkills = pericias.toSet()

        // Reset hidden/filtered skills so points invested in unavailable skills
        // (e.g. Transição without Elementalista) are refunded on rebuild.
        val hiddenSkills = spCostStackPorPericia.keys.filter { it !in activeSkills }
        val hiddenWithInvestments = hiddenSkills.filter { per ->
            (spCostStackPorPericia[per]?.sum() ?: 0) > 0 ||
                (compCostStackPorPericia[per]?.sum() ?: 0) > 0 ||
                (baseIncsPorPericia[per] ?: 0) > 0 ||
                (compIncsPorPericia[per] ?: 0) > 0
        }
        hiddenSkills.forEach { per ->
            spCostStackPorPericia.getValue(per).clear()
            compCostStackPorPericia[per]?.clear()
            baseIncsPorPericia[per] = 0
            compIncsPorPericia[per] = 0
            especializacoesPorPericia.remove(per.nome)
            notasPericia.remove(per.nome)
        }
        if (hiddenWithInvestments.isNotEmpty()) {
            val nomes = hiddenWithInvestments.joinToString { it.nome }
            feedbackMessages.add("Rebuild de perícias aplicado: investimentos removidos de perícias indisponíveis ($nomes) e pontos devolvidos.")
        }

        val input = RebuildSkillStacksUseCase.Input(
            pericias = pericias,
            totalSpPool = totalSpPool,
            currentRawValues = desiredRawValues ?: pericias.associate { it.nome to rawTotal(it) },
            startRawValues = pericias.associate { it.nome to periciaStartRaw(ancestralidade, it) },
            capRawValues = pericias.associate { it.nome to periciaCapRaw(it) },
            minRawValues = pericias.associate { it.nome to maxOf(if (isPericiaBasicaEfetiva(it)) 4 else 0, linguistaMinRawFor(it)) },
            freeStepsMap = compIncsPorPericia.mapKeys { it.key.nome },
            effectiveAttributeValues = listaAtributos.associateWith { attrKey ->
                if (faseSupersAtiva && !emProgresso) {
                    atributoRawBaseSemSupers(attrKey)
                } else {
                    valoresAtributos[attrKey]!!.intValue
                }
            },
            skillAttributeMap = pericias.associate { it.nome to atributoBaseParaPericia(it) },
            enforcePoolLimit = enforcePoolLimit
        )

        val result = rebuildSkillStacksUseCase.execute(input)

        pericias.forEach { per ->
            val newStack = result.spCostStacks[per.nome] ?: emptyList()
            val stateStack = spCostStackPorPericia.getValue(per)
            stateStack.clear()
            stateStack.addAll(newStack)

            val newBase = result.baseIncs[per.nome] ?: 0
            baseIncsPorPericia[per] = newBase
        }

        feedbackMessages.addAll(result.feedbackMessages)
    }

    fun toSnapshot(): PersonagemSnapshot {
        val snapshotId = idAtual ?: UUID.randomUUID().toString()

        return PersonagemSnapshot(
            version = 2,
            id = snapshotId,
            nome = nomePersonagem,
            timestamp = System.currentTimeMillis(),
            appTheme = appTheme.name,
            hapticStrength = hapticStrength,
            soundVolume = soundVolume,
            anotacoes = anotacoes,
            flags = SnapshotFlags(
                cartaSelvagem = cartaSelvagem,
                maisPontosPericias = maisPontosPericias,
                modoSupers = modoSupers,
                compendioFantasiaAtivo = compendioFantasiaAtivo,
                compendioHorrorAtivo = compendioHorrorAtivo,
                compendioSciFiAtivo = compendioSciFiAtivo,
                compendioScifiMechasCiberneticosAtivo = compendioScifiMechasCiberneticosAtivo,
                compendioScifiMechasAtivo = compendioScifiMechasAtivo,
                compendioScifiCiberneticosAtivo = compendioScifiCiberneticosAtivo,
                compendioPathfinderAtivo = compendioPathfinderAtivo,
                compendioDeadlandsAtivo = compendioDeadlandsAtivo,
                compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
                compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
                compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
                compendioWiseguysAtivo = compendioWiseguysAtivo,
                optRegraFama = optRegraFama,
                optVariantesDeRacaAtivo = optVariantesDeRacaAtivo,
                modoOficialAtivo = modoOficialAtivo,
                modoMonstroAtivo = modoMonstroAtivo,
                tipoMonstroSelecionado = tipoMonstroSelecionado,
                usarEspecializacoesDePericia = usarEspecializacoesDePericia,
                grandesResponsabilidades = grandesResponsabilidades,
                nasceUmHeroi = nasceUmHeroi,
                soldadoCargaAtivo = soldadoCargaAtivo,
                permiteMultiAntecedenteArcano = permiteMultiAntecedenteArcano,
                meioElfoAgil = meioElfoAgil,
                meioOrcForca = meioOrcForca,
                celestialAAMilagresDesabilitado = celestialAAMilagresDesabilitado,
                jovemAutoPequeno = jovemAutoPequeno,
                jovemMalusPa = jovemMalusPa,
                jovemMalusSp = jovemMalusSp,
                idosoBonusSp = idosoBonusSp,
                obesoBonusSize = obesoBonusSize,
                obesoMalusMov = obesoMalusMov,
                bonusPoderExtra = bonusPoderExtra,
                optRegraRiqueza = optRegraRiqueza,
                optRegraCosaNostra = optRegraCosaNostra,
                modoLivre = modoLivre,
                isNpcExibicao = isNpcExibicao
            ),
            recursos = SnapshotRecursos(
                dinheiro = dinheiro,
                requisicao = requisicao,
                pontosVantagem = pontosVantagem,
                pontosAtributo = pontosAtributo,
                pontosComplicacaoGastos = pontosComplicacaoGastos,
                famaManual = famaManual,
                paFromProgress = paFromProgress,
                spFromProgress = spFromProgress,
                legendaryAttrReservations = legendaryAttrReservations,
                cpPaStack = cpPaStack.toList(),
                cpSpStack = cpSpStack.map { 1 },
                cpPvStack = cpPvStack.map { 1 },
                cpRecursosStack = cpRecursosStack.map { 1 },
                riquezaModifier = riquezaModifier,
                carteiraPathfinder = carteiraPathfinder.toMap()
            ),
            atributos = SnapshotAtributos(
                ancestralidade = ancestralidade,
                valoresAtributos = valoresAtributos.mapValues { it.value.intValue },
                paCostStackPorAtributo = paCostStackPorAtributo.mapValues { it.value.toList() }
            ),
            pericias = SnapshotPericias(
                baseIncsPorPericia = baseIncsPorPericia.mapKeys { it.key.nome },
                compIncsPorPericia = compIncsPorPericia.mapKeys { it.key.nome },
                spCostStackPorPericia = spCostStackPorPericia.mapKeys { it.key.nome }.mapValues { it.value.toList() },
                compCostStackPorPericia = compCostStackPorPericia.mapKeys { it.key.nome }.mapValues { it.value.toList() },
                especializacoesPorPericia = especializacoesPorPericia.toMap(),
                // PROMPT 5: Persist Skill Notes
                notasPericia = notasPericia.toMap()
            ),
            selecoes = SnapshotSelecoes(
                vantagens = vantagensSelecionadas.map { AdvantageSnapshot(it.id, it.choice) },
                vantagensAutomaticas = vantagensAutomaticas.toList(),
                vantagensRaciais = vantagensRaciais.toList(),
                desvantagensAutomaticas = desvantagensAutomaticas.toList(),
                desvantagensRaciais = desvantagensRaciais.toList(),
                complicacoesSelecionadas = complicacoesSelecionadas.map { (comp, nivel) ->
                    ComplicacaoSnapshot(comp.id, nivel)
                },
                // PROMPT 3: Transtornos
                transtornos = transtornos.map { ComplicacaoSnapshot(it.id, null) },
                reservasComplicacaoMaior = reservasComplicacaoMaior.toMap(),
                poderesSelecionados = poderesSelecionados.toList(),
                manifestacoesPoderes = manifestacoesPoderes.toMap(),
                poderSlotsPorArcano = poderSlotsPorArcano.mapValues { it.value.toList() },
                novosPoderesStacksPorArcano = novosPoderesStacksPorArcano
                    .mapValues { (_, pilhas) -> pilhas.map { it.toList() } },
                arcanoEmCompraViaXpKey = arcanoEmCompraViaXpKey,
                arcanoSnapshotAntesDaCompra = arcanoSnapshotAntesDaCompra,
                equipamentosComprados = equipamentosComprados.toList(),
                mechasSelecionados = mechasSelecionados.toList(),
                ciberneticosInstalados = ciberneticosInstalados.toList(),
                coracaoCrystalId = coracaoCrystalSelecionado?.id,
                tropoSelecionadoId = tropoSelecionado?.id,
                vantagensTropoAutomaticas = vantagensAutomaticasDoTropo.toList(),
                tecnicasIniciaisTropo = tecnicasIniciaisFromTropo,
                retratoFileName = portraitFileName,
                expandirRetrato = expandirRetrato,
                portraitScaleType = portraitScaleType,
                portraitAlignment = portraitAlignment,
                signoAdgSelecionado = signoAdgSelecionado,
                pacoteCulturalFantasiaSelecionado = pacoteCulturalFantasiaSelecionado,
                povoDoMarOpcao = povoDoMarOpcao,
                senhoresCavalosExtra = senhoresCavalosExtra,
                senhoresCavalosCompensacao = senhoresCavalosCompensacao,
                artistaMarcialJutsuOpcao = artistaMarcialJutsuOpcao,
                artistaMarcialPotencialFisico = artistaMarcialPotencialFisico,
                artistaMarcialTecnicasSelecionadas = artistaMarcialTecnicasSelecionadas.toList(),
                buXistaCaminhoSelecionado = buXistaCaminhoSelecionado,
                elementalistaElementoSelecionado = elementalistaElementoSelecionado,
                kuiFerramentaSelecionada = kuiFerramentaSelecionada,
                samuraiPericiaEscolhida = samuraiPericiaEscolhida,
                samuraiVantagemEscolhida = samuraiVantagemEscolhida,
                samuraiPosturasSelecionadas = samuraiPosturasSelecionadas.toList(),
                samuraiCombatSlotIds = samuraiCombatSlotIds.toList(),
                shinobiTalentoSelecionado = shinobiTalentoSelecionado,
                shinobiTreinamentoSelecionado = shinobiTreinamentoSelecionado,
                youxiaJutsuSelecionado = youxiaJutsuSelecionado,
                youxiaHistoricoSelecionado = youxiaHistoricoSelecionado,
                protagonistaRollTecnicas = protagonistaRollTecnicas,
                protagonistaRollPericia = protagonistaRollPericia,
                protagonistaRollVantagem = protagonistaRollVantagem,
                protagonistaRollQualidade = protagonistaRollQualidade,
                protagonistaRollHabilidade = protagonistaRollHabilidade,
                protagonistaPericiasEscolhidas = protagonistaPericiasEscolhidas,
                protagonistaPericiasPaixao = protagonistaPericiasPaixao,
                protagonistaSlotAdvantageIds = vantagensSlotProtagonista.toList(),
                gnomoPericiaEscolhida = gnomoPericiaEscolhida,
                kitsunemimiPericiaEscolhida = kitsunemimiPericiaEscolhida,
                usagimimiPericiaEscolhida = usagimimiPericiaEscolhida,
                dominioClerigoSelecionado = dominioClerigoSelecionado,
                dominioClerigoPathfinderSelecionado = dominioClerigoPathfinderSelecionado,
                anoesScifiSelecionado = anoesScifiSelecionado,
                scifiVariant = scifiVariant,
                humanoMineradorAtributo = humanoMineradorAtributo,
                anaoCiberTracosSelecionados = anaoCiberTracosSelecionados,
                vantagemAdaptavelSelecionadaId = vantagemAdaptavelSelecionadaId,
                customVarianteRacialSelecionadaId = customVarianteRacialSelecionadaId
            ),
            progresso = SnapshotProgresso(
                progresso = progresso,
                progressosDisponiveis = progressosDisponiveis,
                stageXpSpent = stageXpSpent.toMap(),
                xpSlots = xpSlots.toList(),
                advancementHistory = advancementHistory.toList(),
                frozenSkillIncrements = frozenSkillIncrements.toMap(),
                skillAdvancementInProgress = skillAdvancementInProgress,
                skillsForCurrentAdvancement = skillsForCurrentAdvancement.toList(),
                advantageAdvancementInProgress = advantageAdvancementInProgress,
                advantageForCurrentAdvancement = advantageForCurrentAdvancement,
                attributeAdvancementInProgress = attributeAdvancementInProgress,
                attributeStageForCurrentAdvancement = attributeStageForCurrentAdvancement,
                stageNameForCurrentAdvancement = stageNameForCurrentAdvancement,
                attributeStacksBeforeAdvancement = attributeStacksBeforeAdvancement,
                attributeUsedReservation = attributeUsedReservation,
                overrideStageForVantagem = overrideStageForVantagem,
                emProgresso = emProgresso,
                modoProgressaoAtivo = modoProgressaoAtivo,
                mostrandoVantagensProgresso = mostrandoVantagensProgresso,
                mostrandoPericiasProgresso = mostrandoPericiasProgresso,
                mostrandoAtributosProgresso = mostrandoAtributosProgresso,
                mostrandoPoderesProgresso = mostrandoPoderesProgresso,
                frozenAdvantageCount = frozenAdvantageCount,
                stageNameForCurrentAdvancementSnapshot = stageNameForCurrentAdvancement
            ),
            supers = SnapshotSupers(
                superInvestments = superInvestments.toList(),
                superNivelCampanha = superNivelCampanha,
                usarSemPontosDePoder = usarSemPontosDePoder,
                superPontosTotais = superPontosTotais,
                superPontosDisponiveis = superPontosDisponiveis,
                superLimite = superLimite,
                superLimitePorPoder = superLimitePorPoder,
                poderFavoritoId = poderFavoritoId,
                limiteDePoderDaCampanha = limiteDePoderDaCampanha,
                bonusApararFromPower = bonusApararFromPower,
                bonusResFromPower = bonusResFromPower,
                armorFromPower = armorFromPower,
                bonusMovimentacaoFromPower = bonusMovimentacaoFromPower,
                vantagensDePoder = vantagensDePoder.toList(),
                gastosPorPoder = gastosPorPoder.toMap(),
                faseSupersAtiva = faseSupersAtiva,
                comprasPpPorEstagio = comprasPpPorEstagio.toMap(),
                comprasAttrPorEstagio = comprasAttrPorEstagio.toMap(),
                superPontosDisponiveisFlag = superPontosDisponiveis > 0
            )
        )
    }

    fun restoreFromSnapshot(
        snapshot: PersonagemSnapshot,
        feedbackMessages: MutableList<String> = mutableListOf()
    ) {
        val flags = snapshot.flags
        cartaSelvagem = flags.cartaSelvagem
        maisPontosPericias = flags.maisPontosPericias
        modoSupers = flags.modoSupers
        compendioFantasiaAtivo = flags.compendioFantasiaAtivo
        compendioHorrorAtivo = flags.compendioHorrorAtivo
        compendioSciFiAtivo = flags.compendioSciFiAtivo
        compendioScifiMechasAtivo = flags.compendioScifiMechasAtivo || flags.compendioScifiMechasCiberneticosAtivo
        compendioScifiCiberneticosAtivo = flags.compendioScifiCiberneticosAtivo || flags.compendioScifiMechasCiberneticosAtivo
        compendioPathfinderAtivo = flags.compendioPathfinderAtivo
        compendioDeadlandsAtivo = flags.compendioDeadlandsAtivo
        compendioCrystalHeartAtivo = flags.compendioCrystalHeartAtivo
        compendioArteDaGuerraAtivo = flags.compendioArteDaGuerraAtivo
        compendioCidadeSolVaporAtivo = flags.compendioCidadeSolVaporAtivo
        compendioWiseguysAtivo = flags.compendioWiseguysAtivo
        optRegraFama = flags.optRegraFama
        optVariantesDeRacaAtivo = flags.optVariantesDeRacaAtivo
        optRegraRiqueza = flags.optRegraRiqueza
        optRegraCosaNostra = flags.optRegraCosaNostra
        modoLivre = flags.modoLivre
        isNpcExibicao = flags.isNpcExibicao
        modoOficialAtivo = flags.modoOficialAtivo
        modoMonstroAtivo = flags.modoMonstroAtivo
        usarEspecializacoesDePericia = flags.usarEspecializacoesDePericia
        grandesResponsabilidades = flags.grandesResponsabilidades

        idAtual = snapshot.id
        nomePersonagem = snapshot.nome
        anotacoes = snapshot.anotacoes
        appTheme = AppTheme.valueOf(snapshot.appTheme)
        portraitFileName = snapshot.selecoes.retratoFileName
        expandirRetrato = snapshot.selecoes.expandirRetrato
        portraitScaleType = snapshot.selecoes.portraitScaleType
        portraitAlignment = snapshot.selecoes.portraitAlignment

        // Flags adicionais
        nasceUmHeroi = flags.nasceUmHeroi
        soldadoCargaAtivo = flags.soldadoCargaAtivo
        permiteMultiAntecedenteArcano = flags.permiteMultiAntecedenteArcano
        meioElfoAgil = flags.meioElfoAgil
        meioOrcForca = flags.meioOrcForca
        celestialAAMilagresDesabilitado = flags.celestialAAMilagresDesabilitado
        jovemAutoPequeno = flags.jovemAutoPequeno
        jovemMalusPa = flags.jovemMalusPa
        jovemMalusSp = flags.jovemMalusSp
        idosoBonusSp = flags.idosoBonusSp
        obesoBonusSize = flags.obesoBonusSize
        obesoMalusMov = flags.obesoMalusMov
        bonusPoderExtra = flags.bonusPoderExtra
        tipoMonstroSelecionado = flags.tipoMonstroSelecionado
        signoAdgSelecionado = snapshot.selecoes.signoAdgSelecionado

        // Restore sign automatic advantages logic
        vantagensAutomaticasDoSigno.clear()
        if (signoAdgSelecionado != null) {
            when (signoAdgSelecionado) {
                "Basabasa" -> vantagensAutomaticasDoSigno.add("atraente")
                "Raposa" -> vantagensAutomaticasDoSigno.add("elevar_o_moral")
                "Lobo" -> vantagensAutomaticasDoSigno.add("elo_comum")
                "Kirin" -> vantagensAutomaticasDoSigno.add("sorte")
            }
        }

        pacoteCulturalFantasiaSelecionado = snapshot.selecoes.pacoteCulturalFantasiaSelecionado ?: "Humano padrão"
        povoDoMarOpcao = snapshot.selecoes.povoDoMarOpcao
        senhoresCavalosExtra = snapshot.selecoes.senhoresCavalosExtra ?: false
        senhoresCavalosCompensacao = snapshot.selecoes.senhoresCavalosCompensacao
        artistaMarcialJutsuOpcao = snapshot.selecoes.artistaMarcialJutsuOpcao ?: ARTISTA_MARCIAL_JUTSU_D6
        artistaMarcialPotencialFisico = snapshot.selecoes.artistaMarcialPotencialFisico
        artistaMarcialTecnicasSelecionadas.clear()
        artistaMarcialTecnicasSelecionadas.addAll(snapshot.selecoes.artistaMarcialTecnicasSelecionadas)
        buXistaCaminhoSelecionado = snapshot.selecoes.buXistaCaminhoSelecionado
        elementalistaElementoSelecionado = snapshot.selecoes.elementalistaElementoSelecionado
        kuiFerramentaSelecionada = snapshot.selecoes.kuiFerramentaSelecionada
        samuraiPericiaEscolhida = snapshot.selecoes.samuraiPericiaEscolhida
        samuraiVantagemEscolhida = snapshot.selecoes.samuraiVantagemEscolhida
        samuraiPosturasSelecionadas.clear()
        samuraiPosturasSelecionadas.addAll(snapshot.selecoes.samuraiPosturasSelecionadas)
        samuraiCombatSlotIds.clear()
        samuraiCombatSlotIds.addAll(snapshot.selecoes.samuraiCombatSlotIds)
        shinobiTalentoSelecionado = snapshot.selecoes.shinobiTalentoSelecionado
        shinobiTreinamentoSelecionado = snapshot.selecoes.shinobiTreinamentoSelecionado
        youxiaJutsuSelecionado = snapshot.selecoes.youxiaJutsuSelecionado
        youxiaHistoricoSelecionado = snapshot.selecoes.youxiaHistoricoSelecionado
        protagonistaRollTecnicas = snapshot.selecoes.protagonistaRollTecnicas
        protagonistaRollPericia = snapshot.selecoes.protagonistaRollPericia
        protagonistaRollVantagem = snapshot.selecoes.protagonistaRollVantagem
        protagonistaRollQualidade = snapshot.selecoes.protagonistaRollQualidade
        protagonistaRollHabilidade = snapshot.selecoes.protagonistaRollHabilidade
        protagonistaPericiasEscolhidas = snapshot.selecoes.protagonistaPericiasEscolhidas
        protagonistaPericiasPaixao = snapshot.selecoes.protagonistaPericiasPaixao
        vantagensSlotProtagonista.clear()
        vantagensSlotProtagonista.addAll(snapshot.selecoes.protagonistaSlotAdvantageIds)
        gnomoPericiaEscolhida = snapshot.selecoes.gnomoPericiaEscolhida
        kitsunemimiPericiaEscolhida = snapshot.selecoes.kitsunemimiPericiaEscolhida
        usagimimiPericiaEscolhida = snapshot.selecoes.usagimimiPericiaEscolhida
        dominioClerigoSelecionado = snapshot.selecoes.dominioClerigoSelecionado
        dominioClerigoPathfinderSelecionado = snapshot.selecoes.dominioClerigoPathfinderSelecionado
        anoesScifiSelecionado = snapshot.selecoes.anoesScifiSelecionado
        scifiVariant = snapshot.selecoes.scifiVariant
        humanoMineradorAtributo = snapshot.selecoes.humanoMineradorAtributo
        anaoCiberTracosSelecionados = snapshot.selecoes.anaoCiberTracosSelecionados
        vantagemAdaptavelSelecionadaId = snapshot.selecoes.vantagemAdaptavelSelecionadaId
        customVarianteRacialSelecionadaId = snapshot.selecoes.customVarianteRacialSelecionadaId

        dinheiro = snapshot.recursos.dinheiro
        requisicao = snapshot.recursos.requisicao
        famaManual = snapshot.recursos.famaManual
        pontosVantagem = snapshot.recursos.pontosVantagem
        pontosComplicacaoGastos = snapshot.recursos.pontosComplicacaoGastos
        paFromProgress = snapshot.recursos.paFromProgress
        spFromProgress = snapshot.recursos.spFromProgress
        legendaryAttrReservations = snapshot.recursos.legendaryAttrReservations

        riquezaModifier = snapshot.recursos.riquezaModifier

        carteiraPathfinder.clear()
        if (snapshot.recursos.carteiraPathfinder.isNotEmpty()) {
            carteiraPathfinder.putAll(snapshot.recursos.carteiraPathfinder)
        } else if (compendioPathfinderAtivo && snapshot.recursos.dinheiro > 0) {
            val pl = snapshot.recursos.dinheiro / 1000
            var rem = snapshot.recursos.dinheiro % 1000
            val po = rem / 100
            rem %= 100
            val pp = rem / 10
            val pc = rem % 10
            carteiraPathfinder[PathfinderCurrencyIds.PL] = pl
            carteiraPathfinder[PathfinderCurrencyIds.PO] = po
            carteiraPathfinder[PathfinderCurrencyIds.PP] = pp
            carteiraPathfinder[PathfinderCurrencyIds.PC] = pc
        }

        aplicarAncestralidade(snapshot.atributos.ancestralidade, feedbackMessages, autoRefund = false)

        // Compatibilidade com saves salvos antes da regra "Variantes de Raça"
        // existir: se o personagem já tinha uma Variante de verdade escolhida
        // (não Seleção — Terracota/Umvee/Elementais guardam a resposta da
        // Seleção nesses mesmos campos hoje, mas essas ficam sempre visíveis
        // independente do toggle, então não precisam forçar nada aqui), liga
        // o toggle automaticamente pra não esconder a escolha que o jogador
        // já tinha feito.
        if (!optVariantesDeRacaAtivo) {
            val config = AncestryVariantRegistry.get(snapshot.atributos.ancestralidade.keyify())
            val isSelecaoPura = config != null && config.grupoVariante == null
            if (!isSelecaoPura) {
                val scifiVariantJaEscolhida = scifiVariant != null && scifiVariant !in setOf("Básico", "Padrão")
                val anoesLegadoJaEscolhido = anoesScifiSelecionado != null && anoesScifiSelecionado != "Básico"
                if (scifiVariantJaEscolhida || anoesLegadoJaEscolhido) {
                    optVariantesDeRacaAtivo = true
                }
            }
        }

        cpPaStack.apply { clear(); addAll(snapshot.recursos.cpPaStack) }
        cpSpStack.apply { clear(); repeat(snapshot.recursos.cpSpStack.size) { add(Unit) } }
        cpPvStack.apply { clear(); repeat(snapshot.recursos.cpPvStack.size) { add(Unit) } }
        cpRecursosStack.apply { clear(); repeat(snapshot.recursos.cpRecursosStack.size) { add(Unit) } }

        paCostStackPorAtributo.forEach { (attr, stack) ->
            stack.clear()
            stack.addAll(snapshot.atributos.paCostStackPorAtributo[attr].orEmpty())
            val base = racialAttrMinMap[snapshot.atributos.ancestralidade]?.get(attr) ?: 4
            valoresAtributos[attr]!!.intValue = applySuperStepsFrom(base, stack.size)
        }
        pontosAtributo = snapshot.recursos.pontosAtributo

        especializacoesPorPericia.clear()
        ensureIdiomaSlotsFromSnapshot(snapshot.pericias.baseIncsPorPericia.keys)

        // --- Restore Loop ---
        periciasComIdiomas().forEach { per ->
            baseIncsPorPericia[per] = snapshot.pericias.baseIncsPorPericia[per.nome] ?: 0
            compIncsPorPericia[per] = snapshot.pericias.compIncsPorPericia[per.nome] ?: 0

            spCostStackPorPericia.getValue(per).apply {
                clear()
                addAll(snapshot.pericias.spCostStackPorPericia[per.nome].orEmpty())
            }
            compCostStackPorPericia.getValue(per).apply {
                clear()
                addAll(snapshot.pericias.compCostStackPorPericia[per.nome].orEmpty())
            }

            snapshot.pericias.especializacoesPorPericia[per.nome]?.let { dto ->
                especializacoesPorPericia[per.nome] = dto
            }
        }

        // Restore Jutsu
        ensureJutsuSlotsFromSnapshot(snapshot.pericias.baseIncsPorPericia.keys)

        // Second pass for Jutsu Extras that were just added
        periciasComIdiomas().forEach { per ->
            if (!baseIncsPorPericia.containsKey(per)) {
                baseIncsPorPericia[per] = snapshot.pericias.baseIncsPorPericia[per.nome] ?: 0
                compIncsPorPericia[per] = snapshot.pericias.compIncsPorPericia[per.nome] ?: 0
                ensurePericiaEntry(per)

                spCostStackPorPericia.getValue(per).apply {
                    clear()
                    addAll(snapshot.pericias.spCostStackPorPericia[per.nome].orEmpty())
                }
                compCostStackPorPericia.getValue(per).apply {
                    clear()
                    addAll(snapshot.pericias.compCostStackPorPericia[per.nome].orEmpty())
                }
            }
        }

        notasPericia.clear()
        notasPericia.putAll(snapshot.pericias.notasPericia ?: emptyMap())
        syncIdiomaSlots()
        syncJutsuSlots()


        vantagensSelecionadas.clear()
        snapshot.selecoes.vantagens.forEach { snap ->
            listaVantagens.firstOrNull { it.id == snap.id }?.let { vant ->
                val newVant = vant.copy()
                newVant.choice = snap.choice
                vantagensSelecionadas.add(newVant)
            }
        }

        // Proposital: sobrescreve o que aplicarAncestralidade() acabou de recalcular
        // (linha acima) com os valores exatos gravados no save. Decisão de produto
        // confirmada: um personagem salvo deve continuar exatamente como estava
        // quando salvo, mesmo que a mecânica da raça tenha mudado depois — não
        // "migrar" silenciosamente vantagens/desvantagens automáticas para a regra
        // atual só por reabrir o personagem. Não remover isto achando que é bug.
        vantagensAutomaticas.apply { clear(); addAll(snapshot.selecoes.vantagensAutomaticas) }
        vantagensRaciais.apply { clear(); addAll(snapshot.selecoes.vantagensRaciais) }
        desvantagensAutomaticas.apply { clear(); addAll(snapshot.selecoes.desvantagensAutomaticas) }
        desvantagensRaciais.apply { clear(); addAll(snapshot.selecoes.desvantagensRaciais) }

        complicacoesSelecionadas.clear()
        snapshot.selecoes.complicacoesSelecionadas.forEach { compSnap ->
            listaComplicacoes.firstOrNull { it.id == compSnap.id }?.let { comp ->
                complicacoesSelecionadas[comp] = compSnap.nivel
            }
        }
        reservasComplicacaoMaior.clear()
        reservasComplicacaoMaior.putAll(snapshot.selecoes.reservasComplicacaoMaior)

        // PROMPT 3: Restore Transtornos
        transtornos.clear()
        snapshot.selecoes.transtornos?.forEach { compSnap ->
            listaComplicacoes.firstOrNull { it.id == compSnap.id }?.let { comp ->
                transtornos.add(comp)
            }
        }

        equipamentosComprados.apply { clear(); addAll(snapshot.selecoes.equipamentosComprados) }
        mechasSelecionados.apply { clear(); addAll(snapshot.selecoes.mechasSelecionados) }
        ciberneticosInstalados.apply { clear(); addAll(snapshot.selecoes.ciberneticosInstalados) }

        tropoSelecionado = snapshot.selecoes.tropoSelecionadoId?.let { id ->
            listaTropos.firstOrNull { it.id == id }
        }
        vantagensAutomaticasDoTropo.apply {
            clear()
            addAll(snapshot.selecoes.vantagensTropoAutomaticas)
        }
        if (tropoSelecionado?.id == "tropo_samurai") {
            syncSamuraiVantagemEscolhida()
        }
        vantagensAutomaticasDoProtagonista.clear()
        if (tropoSelecionado?.id == "tropo_protagonista") {
            atualizarProtagonistaAutoVantagens()
        }
        protagonistaBonusPv = tropoSelecionado?.id == "tropo_protagonista" &&
            protagonistaRollHabilidade == 4
        syncArtistaMarcialPotencialFisico()
        if (tropoSelecionado?.id == "tropo_youxia") {
            if (notasPericia["Lutar"].isNullOrBlank() && !youxiaJutsuSelecionado.isNullOrBlank()) {
                notasPericia["Lutar"] = youxiaJutsuSelecionado!!
            }
            syncYouxiaKensai()
        }

        poderSlotsPorArcano.clear()
        snapshot.selecoes.poderSlotsPorArcano.forEach { (key, slots) ->
            poderSlotsPorArcano[key] = mutableStateListOf<String?>().apply { addAll(slots) }
        }
        novosPoderesStacksPorArcano.clear()
        snapshot.selecoes.novosPoderesStacksPorArcano.forEach { (key, pilhas) ->
            novosPoderesStacksPorArcano[key] = pilhas.map { it.toMutableList() }.toMutableList()
        }
        poderesSelecionados.apply { clear(); addAll(snapshot.selecoes.poderesSelecionados) }
        manifestacoesPoderes.apply {
            clear()
            putAll(snapshot.selecoes.manifestacoesPoderes)
        }
        arcanoEmCompraViaXpKey = snapshot.selecoes.arcanoEmCompraViaXpKey
        arcanoSnapshotAntesDaCompra = snapshot.selecoes.arcanoSnapshotAntesDaCompra

        progresso = snapshot.progresso.progresso
        progressosDisponiveis = snapshot.progresso.progressosDisponiveis
        stageXpSpent.keys.forEach { stage -> stageXpSpent[stage] = snapshot.progresso.stageXpSpent[stage] ?: 0 }
        xpSlots.apply { clear(); addAll(snapshot.progresso.xpSlots) }
        advancementHistory.apply { clear(); addAll(snapshot.progresso.advancementHistory) }

        frozenSkillIncrements.clear()
        frozenSkillIncrements.putAll(snapshot.progresso.frozenSkillIncrements)

        skillAdvancementInProgress = snapshot.progresso.skillAdvancementInProgress
        skillsForCurrentAdvancement.apply { clear(); addAll(snapshot.progresso.skillsForCurrentAdvancement) }
        advantageAdvancementInProgress = snapshot.progresso.advantageAdvancementInProgress
        advantageForCurrentAdvancement = snapshot.progresso.advantageForCurrentAdvancement
        attributeAdvancementInProgress = snapshot.progresso.attributeAdvancementInProgress
        attributeStageForCurrentAdvancement = snapshot.progresso.attributeStageForCurrentAdvancement
        stageNameForCurrentAdvancement = snapshot.progresso.stageNameForCurrentAdvancement
        attributeStacksBeforeAdvancement = snapshot.progresso.attributeStacksBeforeAdvancement
        attributeUsedReservation = snapshot.progresso.attributeUsedReservation
        overrideStageForVantagem = snapshot.progresso.overrideStageForVantagem
        emProgresso = snapshot.progresso.emProgresso
        modoProgressaoAtivo = snapshot.progresso.modoProgressaoAtivo
        mostrandoVantagensProgresso = snapshot.progresso.mostrandoVantagensProgresso
        mostrandoPericiasProgresso = snapshot.progresso.mostrandoPericiasProgresso
        mostrandoAtributosProgresso = snapshot.progresso.mostrandoAtributosProgresso
        mostrandoPoderesProgresso = snapshot.progresso.mostrandoPoderesProgresso
        frozenAdvantageCount = snapshot.progresso.frozenAdvantageCount

        superInvestments.apply { clear(); addAll(snapshot.supers.superInvestments) }
        superNivelCampanha = snapshot.supers.superNivelCampanha
        usarSemPontosDePoder = snapshot.supers.usarSemPontosDePoder
        superPontosTotais = snapshot.supers.superPontosTotais
        superPontosDisponiveis = snapshot.supers.superPontosDisponiveis
        superLimite = snapshot.supers.superLimite
        superLimitePorPoder = snapshot.supers.superLimitePorPoder
        poderFavoritoId = snapshot.supers.poderFavoritoId
        limiteDePoderDaCampanha = snapshot.supers.limiteDePoderDaCampanha
        bonusApararFromPower = snapshot.supers.bonusApararFromPower
        bonusResFromPower = snapshot.supers.bonusResFromPower
        armorFromPower = snapshot.supers.armorFromPower
        bonusMovimentacaoFromPower = snapshot.supers.bonusMovimentacaoFromPower
        vantagensDePoder.apply { clear(); addAll(snapshot.supers.vantagensDePoder) }
        gastosPorPoder.apply { clear(); putAll(snapshot.supers.gastosPorPoder) }
        faseSupersAtiva = snapshot.supers.faseSupersAtiva

        comprasPpPorEstagio.keys.forEach { comprasPpPorEstagio[it] = snapshot.supers.comprasPpPorEstagio[it] ?: 0 }
        comprasAttrPorEstagio.keys.forEach { comprasAttrPorEstagio[it] = snapshot.supers.comprasAttrPorEstagio[it] ?: 0 }

        snapshot.selecoes.coracaoCrystalId?.let { cid ->
            coracaoCrystalSelecionado = listaCoracoesCrystal.find { it.id == cid }
        }

        trimAttributeStacks(feedbackMessages)
        rebuildAllPericiaStacks(feedbackMessages)
        updateEmProgressoFlag()
        syncPoderesSelecionadosFromSlots()
    }
}
