// CriadorViewModel.kt
package com.example.swadebuilder.model

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.CharacterPortraitStorage
import com.example.swadebuilder.util.CharacterStorage
import com.example.swadebuilder.util.CustomCrystalHeartStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.model.usecase.EnsureDefaultSpecializationsUseCase
import com.example.swadebuilder.model.usecase.RemoveCrystalHeartUseCase
import com.example.swadebuilder.model.usecase.UpsertCrystalHeartUseCase
import com.example.swadebuilder.model.usecase.GenerateSequentialNameUseCase
import com.example.swadebuilder.model.usecase.ValidatePowerInvestmentUseCase
import com.example.swadebuilder.model.usecase.ValidateSpecialPowerRequirementsUseCase
import com.example.swadebuilder.model.usecase.ValidateSuperAdvantageInvestmentUseCase
import com.example.swadebuilder.model.usecase.ValidateSuperAttributeInvestmentUseCase
import com.example.swadebuilder.model.usecase.ApplySuperAttributeDeltaUseCase
import com.example.swadebuilder.model.usecase.CalculatePerPowerLimitUseCase
import com.example.swadebuilder.model.usecase.CalculateSuperSkillRawAfterRevertUseCase
import com.example.swadebuilder.model.usecase.ResolveDependentPowerRemovalUseCase
import com.example.swadebuilder.model.usecase.AdjustNonNegativeBonusUseCase
import com.example.swadebuilder.model.usecase.ValidatePowerInvestmentWorkflowUseCase
import com.example.swadebuilder.model.usecase.RebuildSkillStacksUseCase
import com.example.swadebuilder.model.usecase.CalculateCurrentSuperSkillStepsUseCase
import com.example.swadebuilder.model.usecase.ResolveAdvantageByIdUseCase
import com.example.swadebuilder.model.usecase.NormalizeArcaneBackgroundChoiceUseCase
import com.example.swadebuilder.model.ids.AdvantageIds
import com.example.swadebuilder.model.ids.ArcaneBackgroundIds
import com.example.swadebuilder.model.ids.CrystalHeartIds
import com.example.swadebuilder.model.ids.ModuleIds
import com.example.swadebuilder.model.ids.PathfinderCurrencyIds
import com.example.swadebuilder.model.ids.PowerIds
import com.example.swadebuilder.model.rules.RulesResolver
import com.example.swadebuilder.model.ids.SkillIds
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.ArcanoInfo

// ---- OBJETOS DE RETORNO ----
data class InvestCheck(val ok: Boolean, val motivoBloqueio: String? = null)
data class InvestResult(val ok: Boolean, val mensagem: String)

/**
 * ViewModel que gerencia o estado de criação de personagem.
 */
class CriadorViewModel(
    private val gameDataRepository: GameDataRepository = AssetGameDataRepository()
) : ViewModel() {

    companion object {
        private const val DEFAULT_CHARACTER_NAME = "Nome"
    }

    val state = CriadorState()

    private val _feedbackMessages = mutableStateListOf<String>()
    val feedbackMessages: List<String> = _feedbackMessages

    val gameDataStore = GameDataStore()
    private val ensureDefaultSpecializationsUseCase = EnsureDefaultSpecializationsUseCase()
    private val upsertCrystalHeartUseCase = UpsertCrystalHeartUseCase()
    private val removeCrystalHeartUseCase = RemoveCrystalHeartUseCase()
    private val generateSequentialNameUseCase = GenerateSequentialNameUseCase(defaultName = DEFAULT_CHARACTER_NAME)
    private val validatePowerInvestmentUseCase = ValidatePowerInvestmentUseCase()
    private val validateSpecialPowerRequirementsUseCase = ValidateSpecialPowerRequirementsUseCase()
    private val validateSuperAdvantageInvestmentUseCase = ValidateSuperAdvantageInvestmentUseCase()
    private val validateSuperAttributeInvestmentUseCase = ValidateSuperAttributeInvestmentUseCase()
    private val applySuperAttributeDeltaUseCase = ApplySuperAttributeDeltaUseCase()
    private val calculatePerPowerLimitUseCase = CalculatePerPowerLimitUseCase()
    private val calculateSuperSkillRawAfterRevertUseCase = CalculateSuperSkillRawAfterRevertUseCase()
    private val resolveDependentPowerRemovalUseCase = ResolveDependentPowerRemovalUseCase()
    private val adjustNonNegativeBonusUseCase = AdjustNonNegativeBonusUseCase()
    private val validatePowerInvestmentWorkflowUseCase = ValidatePowerInvestmentWorkflowUseCase()
    private val calculateCurrentSuperSkillStepsUseCase = CalculateCurrentSuperSkillStepsUseCase()
    private val resolveAdvantageByIdUseCase = ResolveAdvantageByIdUseCase()
    private val normalizeArcaneBackgroundChoiceUseCase = NormalizeArcaneBackgroundChoiceUseCase()
    private val rulesResolver = RulesResolver()

    private fun periciasData() = gameDataStore.getPericias()
    private fun vantagensData() = gameDataStore.getVantagens()
    private fun complicacoesData() = gameDataStore.getComplicacoes()
    private fun coracoesData() = gameDataStore.getCoracoesCrystal()
    private fun periciasMapData() = gameDataStore.getPericiasMap()

    private fun moduleKeysFromFlags(flags: SnapshotFlags): Set<String> = buildSet {
        if (flags.compendioFantasiaAtivo) add(ModuleIds.FANTASIA)
        if (flags.compendioHorrorAtivo) add(ModuleIds.HORROR)
        if (flags.compendioSciFiAtivo) add(ModuleIds.SCI_FI)
        if (flags.compendioPathfinderAtivo) add(ModuleIds.PATHFINDER)
        if (flags.compendioDeadlandsAtivo) add(ModuleIds.DEADLANDS)
        if (flags.compendioCrystalHeartAtivo) add(ModuleIds.CRYSTAL_HEART)
        if (flags.compendioArteDaGuerraAtivo) add(ModuleIds.ARTE_DA_GUERRA)
        if (flags.compendioCidadeSolVaporAtivo) add(ModuleIds.CIDADE_SOL_VAPOR)
        if (flags.compendioWiseguysAtivo) add(ModuleIds.WISEGUYS)
        if (flags.modoSupers) add(ModuleIds.SUPER)
    }

    suspend fun carregarDadosDeJogo(context: Context, activeModules: Set<String>): GameDataSnapshot {
        return gameDataRepository.load(context, activeModules).also {
            gameDataStore.updateSnapshot(it)
            state.updateGameData(it)
        }
    }

    internal fun aplicarGameDataSnapshot(snapshot: GameDataSnapshot) {
        gameDataStore.updateSnapshot(snapshot)
        state.updateGameData(snapshot)
    }
    fun logFeedback(message: String) {
        _feedbackMessages.add(message)
    }

    fun clearFeedbackMessages() {
        _feedbackMessages.clear()
    }

    fun ensureDefaultSpecializations() {
        val pericias = periciasData()
        state.ensurePericiasRegistered(pericias)

        val atualizado = ensureDefaultSpecializationsUseCase.execute(
            usarEspecializacoesDePericia = state.usarEspecializacoesDePericia,
            pericias = pericias,
            rawTotalProvider = { per -> state.rawTotal(per) },
            atual = state.especializacoesPorPericia.toMap()
        )

        state.especializacoesPorPericia.clear()
        state.especializacoesPorPericia.putAll(atualizado)
    }

    // === NOVO: toggle global (por enquanto via MainActivity) ===
    var multiplosAAHabilitados: Boolean = false
        private set

    fun setMultiplosAAHabilitados(enabled: Boolean) {
        multiplosAAHabilitados = enabled
    }

    fun setAppTheme(theme: com.example.swadebuilder.ui.theme.AppTheme) {
        state.appTheme = theme
    }

    private fun resetUiState() {
        state.vantSearchQuery = ""
        state.vantSelectedCategories.clear()
        state.vantFilter = VantFilter()
        state.equipSearchQuery = ""
        state.equipSelectedSuperTypes.clear()
        state.equipFilter = EquipFilter()
        state.equipExpandedTypes.clear()
    }

    fun resetToEmptyState() {
        resetStateParaNovoPersonagem(
            cartaSelvagem = true,
            maisPontosPericias = true,
            modoSupers = false,
            compendioFantasiaAtivo = false,
            compendioHorrorAtivo = false,
            compendioSciFiAtivo = false,
            compendioScifiMechasCiberneticosAtivo = false,
            compendioPathfinderAtivo = false,
            compendioDeadlandsAtivo = false,
            compendioCrystalHeartAtivo = false,
            compendioArteDaGuerraAtivo = false,
            compendioCidadeSolVaporAtivo = false,
            compendioWiseguysAtivo = false,
            modoMonstroAtivo = false,
            usarEspecializacoesDePericia = false,
            grandesResponsabilidades = false,
            regraMultiplosIdiomas = false,
                optRegraFama = false,
                optRegraRiqueza = false,
                optRegraCosaNostra = false
        )
    }

    suspend fun prepararNomeInicial(context: Context) {
        state.nomePersonagem = generateSequentialNameUseCase.execute(
            baseName = DEFAULT_CHARACTER_NAME,
            existingNames = listarPersonagensSalvos(context).map { it.nome },
            usarParenteses = false
        )
    }

    suspend fun salvarPersonagem(
        context: Context,
        nomePersonalizado: String? = null,
        silent: Boolean = false,
        criarCopia: Boolean = false
    ): CharacterStorage.SaveEntry {
        val previousSnapshot = if (!criarCopia) {
            state.idAtual?.let { id ->
                when (val result = CharacterStorage.load(context, id)) {
                    is CharacterStorage.LoadResult.Success -> result.snapshot
                    else -> null
                }
            }
        } else null

        val desiredName = (nomePersonalizado?.takeIf { it.isNotBlank() } ?: state.nomePersonagem)
            .ifBlank { DEFAULT_CHARACTER_NAME }

        val savedEntries = listarPersonagensSalvos(context)
        val idToIgnore = if (criarCopia) null else state.idAtual
        val otherNames = savedEntries
            .filter { it.id != idToIgnore }
            .map { it.nome }

        val finalName = if (desiredName.equals(DEFAULT_CHARACTER_NAME, ignoreCase = true)) {
            generateSequentialNameUseCase.execute(DEFAULT_CHARACTER_NAME, otherNames, usarParenteses = false)
        } else {
            generateSequentialNameUseCase.execute(desiredName, otherNames, usarParenteses = true)
        }

        state.nomePersonagem = finalName

        val idParaSalvar = if (criarCopia) java.util.UUID.randomUUID().toString() else (state.idAtual ?: java.util.UUID.randomUUID().toString())
        val snapshot = state.toSnapshot().copy(nome = finalName, id = idParaSalvar)

        val entry = CharacterStorage.save(context, snapshot)
        state.idAtual = entry.id

        // Cleanup only if we are overwriting (not creating a copy)
        if (!criarCopia) {
            val previousPortrait = previousSnapshot?.selecoes?.retratoFileName
            val currentPortrait = snapshot.selecoes.retratoFileName
            if (previousPortrait != null && previousPortrait != currentPortrait) {
                CharacterPortraitStorage.deleteIfUnused(context, previousPortrait)
            }
        }

        if (!silent) {
            logFeedback("Personagem salvo: ${entry.nome}")
        }
        return entry
    }

    suspend fun listarPersonagensSalvos(context: Context): List<CharacterStorage.SaveEntry> {
        return CharacterStorage.listSaves(context)
    }

    data class LoadOutcome(
        val success: Boolean,
        val message: String? = null
    )

    suspend fun carregarPersonagem(context: Context, saveId: String): LoadOutcome {
        val snapshot = when (val result = CharacterStorage.load(context, saveId)) {
            is CharacterStorage.LoadResult.Success -> result.snapshot
            is CharacterStorage.LoadResult.Failure -> return LoadOutcome(
                success = false,
                message = result.message
            )
            CharacterStorage.LoadResult.NotFound -> return LoadOutcome(
                success = false,
                message = "Arquivo de personagem não encontrado."
            )
        }
        resetUiState()
        clearFeedbackMessages()
        val flags = snapshot.flags

        val snapshotData = gameDataRepository.load(context, moduleKeysFromFlags(flags))
        aplicarGameDataSnapshot(snapshotData)

        resetStateParaNovoPersonagem(
            cartaSelvagem = flags.cartaSelvagem,
            maisPontosPericias = flags.maisPontosPericias,
            modoSupers = flags.modoSupers,
            compendioFantasiaAtivo = flags.compendioFantasiaAtivo,
            compendioHorrorAtivo = flags.compendioHorrorAtivo,
            compendioSciFiAtivo = flags.compendioSciFiAtivo,
            compendioScifiMechasCiberneticosAtivo = flags.compendioScifiMechasCiberneticosAtivo,
            compendioPathfinderAtivo = flags.compendioPathfinderAtivo,
            compendioDeadlandsAtivo = flags.compendioDeadlandsAtivo,
            compendioCrystalHeartAtivo = flags.compendioCrystalHeartAtivo,
            compendioArteDaGuerraAtivo = flags.compendioArteDaGuerraAtivo,
            compendioCidadeSolVaporAtivo = flags.compendioCidadeSolVaporAtivo,
            compendioWiseguysAtivo = flags.compendioWiseguysAtivo,
            modoMonstroAtivo = flags.modoMonstroAtivo,
            usarEspecializacoesDePericia = flags.usarEspecializacoesDePericia,
            grandesResponsabilidades = flags.grandesResponsabilidades,
            optRegraFama = flags.optRegraFama,
            optRegraRiqueza = flags.optRegraRiqueza,
            optRegraCosaNostra = flags.optRegraCosaNostra
        )
        state.restoreFromSnapshot(snapshot, mutableListOf())
        state.idAtual = saveId
        return LoadOutcome(success = true)
    }

    fun normalizeArcanoIdsNoCarregamento() {

        val convertidos = state.vantagensSelecionadas.map { v ->
            if (v.id == ArcaneBackgroundIds.BASE && v.choice != null) {
                val novoId = normalizeArcaneBackgroundChoiceUseCase.execute(v.choice)
                val novo = vantagensData().find { it.id == novoId }
                novo ?: v
            } else v
        }
        state.vantagensSelecionadas.clear()
        state.vantagensSelecionadas.addAll(convertidos.distinctBy { it.id })
    }

    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioHorrorAtivo: Boolean = false,
        compendioSciFiAtivo: Boolean = false,
        compendioScifiMechasCiberneticosAtivo: Boolean = false,
        compendioPathfinderAtivo: Boolean = false,
        compendioDeadlandsAtivo: Boolean = false,
        compendioCrystalHeartAtivo: Boolean = false,
        compendioArteDaGuerraAtivo: Boolean = false,
        compendioCidadeSolVaporAtivo: Boolean = false,
        compendioWiseguysAtivo: Boolean = false,
        modoMonstroAtivo: Boolean = false,
        usarEspecializacoesDePericia: Boolean = false,
        grandesResponsabilidades: Boolean = false,
        regraMultiplosIdiomas: Boolean = false,
        optRegraFama: Boolean = false,
        optRegraRiqueza: Boolean = false,
        optRegraCosaNostra: Boolean = false
    ) {

        state.appTheme = com.example.swadebuilder.ui.theme.AppTheme.DEFAULT

        state.modoSupers = modoSupers
        state.compendioFantasiaAtivo = compendioFantasiaAtivo
        state.compendioHorrorAtivo = compendioHorrorAtivo
        state.compendioSciFiAtivo = compendioSciFiAtivo
        state.compendioScifiMechasCiberneticosAtivo = compendioScifiMechasCiberneticosAtivo
        state.compendioPathfinderAtivo = compendioPathfinderAtivo
        state.compendioDeadlandsAtivo = compendioDeadlandsAtivo
        state.compendioCrystalHeartAtivo = compendioCrystalHeartAtivo
        state.compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo
        state.compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo
        state.compendioWiseguysAtivo = compendioWiseguysAtivo
        state.optRegraRiqueza = optRegraRiqueza
        state.optRegraCosaNostra = optRegraCosaNostra
        state.optRegraFama = optRegraFama
        state.famaManual = 0
        state.modoMonstroAtivo = modoMonstroAtivo
        state.tipoMonstroSelecionado = if (modoMonstroAtivo) "anjo" else null
        state.grandesResponsabilidades = grandesResponsabilidades
        state.soldadoCargaAtivo = true
        state.regraMultiplosIdiomas = regraMultiplosIdiomas

        state.idAtual = null
        state.nomePersonagem = DEFAULT_CHARACTER_NAME
        state.anotacoes = ""
        state.portraitFileName = null

        state.coracaoCrystalSelecionado = null

        state.nasceUmHeroi = false // Fix: reset flag

        state.tipoMonstroSelecionado = if (modoMonstroAtivo) "anjo" else null
        state.tropoSelecionado = null
        state.signoAdgSelecionado = null
        state.artistaMarcialJutsuOpcao = CriadorState.ARTISTA_MARCIAL_JUTSU_D6
        state.artistaMarcialPotencialFisico = null
        state.artistaMarcialTecnicasSelecionadas.clear()
        state.buXistaCaminhoSelecionado = null
        state.elementalistaElementoSelecionado = null
        state.kuiFerramentaSelecionada = null
        state.samuraiPericiaEscolhida = null
        state.samuraiVantagemEscolhida = null
        state.samuraiPosturasSelecionadas.clear()
        state.samuraiCombatSlotIds.clear()
        state.shinobiTalentoSelecionado = null
        state.shinobiTreinamentoSelecionado = null
        state.youxiaJutsuSelecionado = null
        state.youxiaHistoricoSelecionado = null
        state.descendenteElementalSelecionado = null
        state.gnomoPericiaEscolhida = null
        state.dominioClerigoSelecionado = null

        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()

        // 1. Limpeza Completa de Estado (Stacks, Listas, Atributos, Perícias)
        // Isso deve ocorrer ANTES de aplicar qualquer regra ou ancestralidade
        state.vantagensSelecionadas.clear()
        state.complicacoesSelecionadas.clear()
        state.reservasComplicacaoMaior.clear()
        state.vantagensAutomaticas.clear()
        state.desvantagensAutomaticas.clear()
        state.vantagensAutomaticasDoTropo.clear()
        state.vantagensAutomaticasDoSigno.clear()
        state.vantagensAutomaticasDoElemento.clear()
        state.vantagensAutomaticasDoPotencialFisico.clear()
        state.transtornos.clear()
        state.notasPericia.clear()
        state.manifestacoesPoderes.clear()

        state.vantagemEmFoco = null
        state.categoriasVantagensExpandidas.keys.forEach { cat ->
            state.categoriasVantagensExpandidas[cat] = false
        }
        state.sectionsExpanded.keys.forEach { section ->
            state.sectionsExpanded[section] = false
        }
        state.sectionsExpanded[com.example.swadebuilder.ui.MainSection.RESUMO] = true

        state.equipamentosComprados.clear()
        state.pontosComplicacaoGastos = 0
        state.cpRecursosStack.clear()
        state.cpPaStack.clear()
        state.paFromProgress = 0
        state.spFromProgress = 0
        state.pvFromXpOutstanding = 0
        state.legendaryAttrReservations = 0
        state.cpPvStack.clear()
        state.cpSpStack.clear()
        state.comprasPpPorEstagio.keys.forEach   { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach { state.comprasAttrPorEstagio[it] = 0 }
        state.paCostStackPorAtributo.values.forEach  { it.clear() }
        state.compCostStackPorPericia.values.forEach { it.clear() }
        state.spCostStackPorPericia.values.forEach   { it.clear() }
        state.poderSlotsPorArcano.clear()
        state.novosPoderesStacksPorArcano.clear()
        state.attributeAdvancementInProgress = false
        state.attributeStageForCurrentAdvancement = null
        state.attributeStacksBeforeAdvancement = null
        state.attributeUsedReservation = false
        state.skillAdvancementInProgress = false
        state.skillsForCurrentAdvancement.clear()
        state.advantageAdvancementInProgress = false
        state.advantageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.overrideStageForVantagem = null
        state.openVantagensAfterGrant = false
        state.arcanoEmCompraViaXpKey = null
        state.arcanoSnapshotAntesDaCompra = null
        state.mostrandoPoderesProgresso = false
        state.mostrandoAtributosProgresso = false

        // Reset Supers
        state.superInvestments.clear()
        state.superNivelCampanha = null
        state.usarSemPontosDePoder = false
        state.superPontosTotais = 0
        state.superPontosDisponiveis = 0
        state.superLimite = 0
        state.superLimitePorPoder = 0
        state.poderFavoritoId = null
        state.limiteDePoderDaCampanha = Int.MAX_VALUE
        state.faseSupersAtiva = false
        state.bonusApararFromPower = 0
        state.bonusResFromPower = 0
        state.armorFromPower = 0
        state.bonusMovimentacaoFromPower = 0
        state.vantagensDePoder.clear()
        state.gastosPorPoder.clear()
        state.naturalArmorFromRace = 0

        // Reset Progresso
        state.progresso = 0
        state.progressosDisponiveis = 0
        state.stageXpSpent.keys.forEach { state.stageXpSpent[it] = 0 }
        state.xpSlots.fill(false)
        state.frozenAdvantageCount = 0
        state.advancementHistory.clear()
        state.emProgresso = false
        state.modoProgressaoAtivo = false
        state.mostrandoVantagensProgresso = false
        state.mostrandoPericiasProgresso = false
        state.frozenSkillIncrements.clear()
        resetUiState()

        // Reset Atributos e Perícias para o base (sem raça ainda)
        state.valoresAtributos.forEach { (_, holder) -> holder.intValue = 4 }
        state.recalcularPontosAtributo(mutableListOf())

        periciasData().forEach { per ->
            state.baseIncsPorPericia[per] = 0
            state.spCostStackPorPericia.getValue(per).clear()
            state.compCostStackPorPericia[per]?.clear()
        }
        state.rebuildAllPericiaStacks(mutableListOf())

        // 2. Zerar PV explicitamente antes de aplicar ancestralidade
        state.pontosVantagem = 0

        // 3. Resolver Regras e Ancestralidade
        val selectedRules = rulesResolver.resolve(
            compendioPathfinderAtivo = compendioPathfinderAtivo,
            compendioSciFiAtivo = compendioSciFiAtivo,
            compendioDeadlandsAtivo = compendioDeadlandsAtivo,
            compendioFantasiaAtivo = compendioFantasiaAtivo,
            compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
            compendioHorrorAtivo = compendioHorrorAtivo,
            compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
            compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
            compendioWiseguysAtivo = compendioWiseguysAtivo
        )

        // Fix: Force transition from empty string to ensure aplicarAncestralidade logic runs fully
        state.ancestralidade = ""
        val targetAncestralidade = selectedRules.defaultAncestralidade()

        // Fix: Ensure all loaded skills are registered in the state maps to prevent crashes in rawTotal
        state.ensureAllPericiasRegistered()
        state.ensureAllAtributosRegistered()

        // Aplica ancestralidade (agora com stacks limpos, então autoRefund não vai comer pontos)
        state.aplicarAncestralidade(targetAncestralidade, mutableListOf())

        // 4. Itens Obrigatórios e Defaults
        val mandatoryEdgeIds = buildSet {
            addAll(selectedRules.mandatoryAdvantageIds())
            if (state.modoSupers) add(AdvantageIds.SUPERPODERES)
        }

        mandatoryEdgeIds.forEach { edgeId ->
            vantagensData().firstOrNull { it.id == edgeId }?.let { edge ->
                if (state.vantagensSelecionadas.none { it.id == edgeId }) {
                    state.vantagensSelecionadas.add(edge.copy())
                }
            }
        }

        if (state.compendioCrystalHeartAtivo) {
            val starterHeartId = selectedRules.defaultCrystalHeartId() ?: CrystalHeartIds.HEART_STARTER
            val basicHeart = coracoesData().firstOrNull { it.id == starterHeartId }
            if (basicHeart != null) {
                state.coracaoCrystalSelecionado = basicHeart
            }
        }

        // 5. Recursos Iniciais
        val startingResources = selectedRules.startingResources()
        state.dinheiro = startingResources.dinheiro

        if (compendioPathfinderAtivo) {
            state.carteiraPathfinder.clear()
            state.carteiraPathfinder.putAll(startingResources.carteiraPathfinder)
            state.updateTotalPathfinderMoney()
        }

        // Note: Points logic (ADAPTAVEL +1 PV) is now handled correctly by aplicarAncestralidade
        // because we force a transition from "" to targetAncestralidade.

        if (state.optRegraCosaNostra) {
            state.aplicarRegrasWiseguys()
            state.recalcularPontosAtributo(mutableListOf()) // Garantir consistência se houver impacto
        }

        state.riquezaModifier = 0
    }

    suspend fun atualizarRetrato(context: Context, sourceUri: Uri?) {
        if (sourceUri == null) {
            state.portraitFileName = null
            return
        }

        val fileName = CharacterPortraitStorage.savePortrait(context, sourceUri)
        if (fileName != null) {
            state.portraitFileName = fileName
        }
    }

    fun perPowerLimit(poderId: String): Int {
        return calculatePerPowerLimitUseCase.execute(
            CalculatePerPowerLimitUseCase.Input(
                favoritePowerId = state.poderFavoritoId,
                targetPowerId = poderId,
                favoriteLimit = state.limiteFavorecido,
                defaultLimit = state.limitePorPoderPadrao
            )
        )
    }

    fun definirPoderFavorecido(poderId: String?) {
        state.poderFavoritoId = poderId
    }

    fun podeSubirAtributoPorSuper(attrKey: String, steps: Int): InvestCheck {
        val erro = validateSuperAttributeInvestmentUseCase.execute(
            ValidateSuperAttributeInvestmentUseCase.Input(
                currentRaw = state.atributoRawComSupers(attrKey),
                steps = steps,
                applySteps = { raw, delta -> state.applySuperStepsFrom(raw, delta) }
            )
        )
        return if (erro == null) InvestCheck(true) else InvestCheck(false, erro)
    }

    private fun bloqueioClasseExclusiva(vant: Vantagem): String? {
        return if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant)) {
            MENSAGEM_EXCLUSIVIDADE_CLASSE
        } else {
            null
        }
    }

    fun canInvestInPower(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestCheck {
        val effectInput = when (efeito) {
            is PowerEffect.SuperAtributo -> {
                ValidatePowerInvestmentWorkflowUseCase.EffectInput.SuperAtributo(
                    currentRaw = state.atributoRawComSupers(efeito.attrKey),
                    steps = efeito.steps,
                    applySteps = { raw, delta -> state.applySuperStepsFrom(raw, delta) }
                )
            }

            is PowerEffect.SuperVantagem -> {
                val vant = resolveAdvantageByIdUseCase.execute(
                    vantagens = vantagensData(),
                    vantagemId = efeito.vantagemId
                )

                val permitido = if (vant != null) {
                    val progressoAnterior = state.overrideStageForVantagem
                    state.overrideStageForVantagem = "Lendário"
                    val podeSelecionar = state.podeSelecionar(vant)
                    state.overrideStageForVantagem = progressoAnterior
                    podeSelecionar
                } else {
                    false
                }

                ValidatePowerInvestmentWorkflowUseCase.EffectInput.SuperVantagem(
                    vantagemIdSolicitada = efeito.vantagemId,
                    vantagemNome = vant?.nome,
                    mensagemBloqueioClasse = vant?.let { bloqueioClasseExclusiva(it) },
                    jaPossuiVantagem = vant != null && state.vantagensSelecionadas.any { it.id == vant.id },
                    requisitosAtendidosIgnorandoEstagio = permitido
                )
            }

            is PowerEffect.Generico -> {
                val pericias = periciasMapData()
                ValidatePowerInvestmentWorkflowUseCase.EffectInput.Generico(
                    effectNameKey = efeito.nome.keyify(),
                    ocultismoRaw = pericias[SkillIds.OCULTISMO]?.let { state.rawTotalComSupers(it) },
                    cienciaRaw = pericias[SkillIds.CIENCIA]?.let { state.rawTotalComSupers(it) }
                )
            }

            else -> ValidatePowerInvestmentWorkflowUseCase.EffectInput.Other
        }

        val erro = validatePowerInvestmentWorkflowUseCase.execute(
            ValidatePowerInvestmentWorkflowUseCase.Input(
                poderId = poderId,
                custo = custo,
                superPontosDisponiveis = state.superPontosDisponiveis,
                gastosPorPoder = state.gastosPorPoder,
                limitePorPoder = perPowerLimit(poderId),
                limiteCompartilhadoArmaduraResistencia = state.limiteDePoderDaCampanha,
                effect = effectInput
            )
        )

        return if (erro == null) InvestCheck(true, null) else InvestCheck(false, erro)
    }

    /**
     * Aplica o investimento no ledger e atualiza derivados.
     * NÃO chama validação; chame canInvestInPower antes.
     */
    fun applyPowerInvestment(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestResult {
        // 1) registra o gasto no ledger
        state.registrarGastoDePoder(poderId, custo)

        // 2) aplica o efeito em estado observável
        when (efeito) {
            is PowerEffect.SuperAtributo -> {
                val key = efeito.attrKey.uppercase().trim()

                // (b) refletir imediatamente no atributo visível:
                // +2 por step até d12; +1 por step acima de d12.
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    val antes = holder.intValue
                    holder.intValue = applySuperAttributeDeltaUseCase.execute(
                        ApplySuperAttributeDeltaUseCase.Input(
                            currentRaw = holder.intValue,
                            steps = efeito.steps,
                            direction = ApplySuperAttributeDeltaUseCase.Direction.INCREASE
                        )
                    )
                    logFeedback("Atributo $key aumentado de d$antes para d${holder.intValue}.")
                }
            }

            is PowerEffect.SuperPericia -> {
                // O efeito visual é agora derivado da lista de investimentos,
                // então não é mais necessário aplicar diretamente aqui.
                // A atualização da UI será automática.
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.armorFromPower, efeito.value)))
                logFeedback("Armadura aumentada em ${efeito.value}.")
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusResFromPower, efeito.value)))
                logFeedback("Resistência aumentada em ${efeito.value}.")
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusApararFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusApararFromPower, efeito.value)))
                logFeedback("Aparar aumentado em ${efeito.value}.")
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusMovimentacaoFromPower, efeito.value))
                )
                logFeedback("Movimentação aumentada em ${efeito.value}.")
            }

            is PowerEffect.SuperVantagem -> {
                resolveAdvantageByIdUseCase.execute(
                    vantagens = vantagensData(),
                    vantagemId = efeito.vantagemId
                )?.let { v ->
                    state.adicionarVantagemPorSuper(v.copy()) // Fix: Use copy
                    logFeedback("Vantagem ${v.nome} adicionada.")
                }
            }

            is PowerEffect.Generico -> {
                logFeedback("${efeito.nome} adquirido.")
            }
        }

        // 3) derivados de perícia / etc.
        state.rebuildAllPericiaStacks()
        // IMPORTANTE: NÃO recalcular atributos básicos aqui,
        // para não “somar de novo” os supers nem mexer na etapa de criação com PAs.

        return InvestResult(true, "Investimento aplicado.")
    }

    fun revertPowerInvestment(
        poderId: String,
        custo: Int,
        efeito: PowerEffect
    ): InvestResult {
        // desfaz o gasto no ledger
        state.desfazerGastoDePoder(poderId, custo)

        when (efeito) {
            is PowerEffect.SuperAtributo -> {
                val key = efeito.attrKey.uppercase().trim()

                // espelha a aplicação: -1 por step se > d12; -2 por step quando <= d12
                val holder = state.valoresAtributos[key]
                if (holder != null) {
                    val antes = holder.intValue
                    holder.intValue = applySuperAttributeDeltaUseCase.execute(
                        ApplySuperAttributeDeltaUseCase.Input(
                            currentRaw = holder.intValue,
                            steps = efeito.steps,
                            direction = ApplySuperAttributeDeltaUseCase.Direction.DECREASE
                        )
                    )
                    logFeedback("Atributo $key reduzido de d$antes para d${holder.intValue}.")
                }
            }

            is PowerEffect.SuperPericia -> {
                val perObj = periciasMapData()[efeito.periciaKey.keyify()]
                if (perObj != null) {
                    val baseRaw = state.rawTotal(perObj)
                    val incsAtuais = calculateCurrentSuperSkillStepsUseCase.execute(
                        CalculateCurrentSuperSkillStepsUseCase.Input(
                            targetSkillName = perObj.nome,
                            investments = state.superInvestments.mapNotNull { inv ->
                                val effect = inv.effect as? PowerEffect.SuperPericia ?: return@mapNotNull null
                                CalculateCurrentSuperSkillStepsUseCase.Investment(
                                    skillKey = effect.periciaKey,
                                    steps = effect.steps
                                )
                            }
                        )
                    )
                    val rawDepois = calculateSuperSkillRawAfterRevertUseCase.execute(
                        CalculateSuperSkillRawAfterRevertUseCase.Input(
                            baseRaw = baseRaw,
                            currentSuperSteps = incsAtuais,
                            revertingSteps = efeito.steps,
                            applySteps = { raw, steps -> state.applySuperStepsFrom(raw, steps) }
                        )
                    )

                    val perKey = perObj.nome.keyify()
                    val dependentPowerToRemove = resolveDependentPowerRemovalUseCase.execute(
                        ResolveDependentPowerRemovalUseCase.Input(
                            skillKey = perKey,
                            skillRawAfterRevert = rawDepois
                        )
                    )
                    if (dependentPowerToRemove != null) {
                        val dep = state.superInvestments.firstOrNull {
                            it.displayName.keyify() == dependentPowerToRemove.dependentDisplayNameKey
                        }
                        if (dep != null) {
                            val res = revertPowerInvestment(dep.powerId, dep.cost, dep.effect)
                            if (res.ok) {
                                state.removerSuperPoder(dep, desfazerNoLedger = false)
                                logFeedback(dependentPowerToRemove.feedbackMessage)
                            }
                        }
                    }
                }
            }

            is PowerEffect.BonusArmadura -> {
                state.updateArmorFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.armorFromPower, -efeito.value)))
                logFeedback("Armadura reduzida em ${efeito.value}.")
            }

            is PowerEffect.BonusResistencia -> {
                state.updateBonusResFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusResFromPower, -efeito.value)))
                logFeedback("Resistência reduzida em ${efeito.value}.")
            }

            is PowerEffect.BonusAparar -> {
                state.updateBonusApararFromPower(adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusApararFromPower, -efeito.value)))
                logFeedback("Aparar reduzido em ${efeito.value}.")
            }

            is PowerEffect.BonusMovimentacao -> {
                state.updateBonusMovimentacaoFromPower(
                    adjustNonNegativeBonusUseCase.execute(AdjustNonNegativeBonusUseCase.Input(state.bonusMovimentacaoFromPower, -efeito.value))
                )
                logFeedback("Movimentação reduzida em ${efeito.value}.")
            }

            is PowerEffect.SuperVantagem -> {
                resolveAdvantageByIdUseCase.execute(
                    vantagens = vantagensData(),
                    vantagemId = efeito.vantagemId
                )?.let { v ->
                    state.removerVantagemPorSuper(v)
                    logFeedback("Vantagem ${v.nome} removida.")
                }
            }
            is PowerEffect.Generico -> {
                logFeedback("${efeito.nome} removido.")
            }
        }

        // Atualiza apenas derivados que dependem de supers / perícias
        state.rebuildAllPericiaStacks()
        // De novo: nada de recalcular atributos de criação aqui.

        return InvestResult(true, "Investimento revertido.")
    }

    fun selecionarCrystalHeart(heart: CrystalHeart) {
        state.coracaoCrystalSelecionado = heart
    }

    fun salvarCrystalHeartPersonalizado(context: Context, heart: CrystalHeart): CrystalHeart? {
        val saved = CustomCrystalHeartStorage.saveCustomHeart(context, heart) ?: return null
        val updated = upsertCrystalHeartUseCase.execute(coracoesData(), saved)
        state.listaCoracoesCrystal = updated
        gameDataStore.withUpdatedCoracoesCrystal(updated)
        return saved
    }

    fun removerCrystalHeartPersonalizado(context: Context, heartId: String): Boolean {
        val removed = CustomCrystalHeartStorage.deleteCustomHeart(context, heartId)
        if (!removed) return false

        val result = removeCrystalHeartUseCase.execute(
            current = coracoesData(),
            heartIdToRemove = heartId,
            currentlySelectedId = state.coracaoCrystalSelecionado?.id
        )

        state.listaCoracoesCrystal = result.updated
        gameDataStore.withUpdatedCoracoesCrystal(result.updated)
        state.coracaoCrystalSelecionado = result.newSelected
        return true
    }

    /**
     * Função genérica "façade" para a UI: tenta investir e retorna mensagem pronta.
     * Use um poderId estável por alvo (ex.: "sp_pericia_LUTAR", "sp_attr_FORCA", PowerIds.ARMOR).
     */
    fun tentarInvestirSuper(investment: SuperInvestment): InvestResult {
        val check = canInvestInPower(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
        if (!check.ok) {
            return InvestResult(false, check.motivoBloqueio ?: "Não foi possível investir.")
        }

        // Adiciona o investimento à lista principal do estado
        state.superInvestments.add(investment)

        return applyPowerInvestment(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
    }

    /** Façade para desfazer o investimento feito. */
    fun desfazerInvestimentoSuper(investment: SuperInvestment): InvestResult {
        return revertPowerInvestment(
            poderId = investment.powerId,
            custo = investment.cost,
            efeito = investment.effect
        )
    }

    fun startSkillAdvancement(slotIndex: Int, stageName: String) {
        if (state.progressosDisponiveis >= 1) {
            resetUiState()
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.stageNameForCurrentAdvancement = stageName
            state.xpSlots[slotIndex] = true
            state.skillAdvancementInProgress = true
            state.skillsForCurrentAdvancement.clear()
            state.grantSkillPointsFromXp()
            state.updateEmProgressoFlag()
        }
    }


    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun finishSkillAdvancement() {
        if (state.skillAdvancementInProgress) {
            val skills = state.skillsForCurrentAdvancement.toList()
            if (skills.isEmpty()) {
                // If nothing was selected, cancel the advancement to free the slot
                cancelAdvancementInProgress()
                return
            }

            val stageName = state.stageNameForCurrentAdvancement ?: state.estagioAtual().nome
            val skillValuesSnapshot = skills.associateWith { skillName ->
                val key = skillName.keyify()
                val pericia = state.periciasComIdiomas().firstOrNull { it.nome.keyify() == key }
                    ?: periciasMapData()[key]
                pericia?.let { state.rawTotal(it) }
            }.filterValues { it != null }.mapValues { it.value!! }
            state.advancementHistory.add(
                AdvancementAction.SpendOnSkills(
                    skillsIncreased = skills,
                    recordedSkillValues = skillValuesSnapshot,
                    stageName = stageName
                )
            )
            state.skillAdvancementInProgress = false
            state.skillsForCurrentAdvancement.clear()
            state.stageNameForCurrentAdvancement = null
            if (state.modoProgressaoAtivo) {
                state.snapshotFrozenSkillIncrements()
            }
            state.updateEmProgressoFlag()
            state.mostrandoPericiasProgresso = false
        }
    }

    fun increaseSkillForAdvancement(skill: com.example.swadebuilder.model.Pericia): Boolean {
        if (!state.skillAdvancementInProgress) return false

        // Check if user has points
        val cost = if (state.rawTotal(skill) >= state.valoresAtributos[skill.atributo]!!.intValue) 2 else 1
        if (state.spFromProgress < cost) {
            logFeedback("Pontos insuficientes para aumentar ${skill.nome} (Custo: $cost, Disponível: ${state.spFromProgress}).")
            return false
        }

        // Apply increase
        state.increasePericiaFromAdvancement(skill, cost)
        state.spFromProgress -= cost

        // Rebuild stacks to update derived stats
        state.rebuildAllPericiaStacks()

        logFeedback("${skill.nome} aumentada para ${state.rawTotal(skill).toDiceString()}.")
        return true
    }

    fun decreaseSkillForAdvancement(skill: com.example.swadebuilder.model.Pericia): Boolean {
        if (!state.skillAdvancementInProgress) return false

        // Ensure the skill was actually increased in this session
        if (!state.skillsForCurrentAdvancement.contains(skill.nome)) return false

        // Get the cost that was paid (peek the stack)
        val stack = state.spCostStackPorPericia[skill]
        val costToRefund = stack?.lastOrNull { it > 0 } ?: return false

        state.decreasePericia(skill)
        state.spFromProgress += costToRefund

        state.rebuildAllPericiaStacks()
        logFeedback("${skill.nome} reduzida.")
        return true
    }

    fun startAdvantageAdvancement(slotIndex: Int, est: String) {
        if (state.progressosDisponiveis >= 1) {
            resetUiState()
            state.progresso++
            state.spendProgressAtStage(est, 1)
            state.stageNameForCurrentAdvancement = est
            state.xpSlots[slotIndex] = true
            state.advantageAdvancementInProgress = true
            state.advantageForCurrentAdvancement = null
            state.mostrandoPoderesProgresso = false
            state.arcanoEmCompraViaXpKey = null
            state.arcanoSnapshotAntesDaCompra = null
            state.grantVantagemPointFromXp(est)
            state.updateEmProgressoFlag()
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun finishAdvantageAdvancement() {
        if (state.advantageAdvancementInProgress) {
            if (state.arcanoCompraPendente()) return
            val advantageId = state.advantageForCurrentAdvancement
            if (advantageId == null) {
                // If nothing selected, revert
                cancelAdvancementInProgress()
                return
            }

            val stageName = state.stageNameForCurrentAdvancement ?: state.estagioAtual().nome
            state.advancementHistory.add(
                AdvancementAction.SpendOnAdvantage(
                    advantageId = advantageId,
                    stageName = stageName,
                    arcanoKey = state.arcanoEmCompraViaXpKey,
                    previousArcanoSlots = state.arcanoSnapshotAntesDaCompra
                )
            )
            if (state.pvFromXpOutstanding > 0) {
                state.pvFromXpOutstanding--
            }
            state.advantageAdvancementInProgress = false
            state.advantageForCurrentAdvancement = null
            state.stageNameForCurrentAdvancement = null
            state.limparCompraArcanoViaXp(restaurarSnapshot = false)
            state.updateEmProgressoFlag()
            state.mostrandoVantagensProgresso = false
        }
    }

    fun selectAdvantageForAdvancement(vantagem: Vantagem) {
        if (state.advantageAdvancementInProgress) {
            if (state.advantageForCurrentAdvancement != null) {
                val currentAdvantageId = state.advantageForCurrentAdvancement!!
                val currentAdvantage = state.vantagensSelecionadas.find { it.id == currentAdvantageId }
                if (currentAdvantage != null) {
                    val (pode, msg) = state.podeRemoverVantagem(currentAdvantage)
                    if (!pode) {
                        logFeedback(msg ?: "Não é possível remover a vantagem.")
                        return
                    }

                    val currentArcKey = currentAdvantage.toArcanoKey()?.normAAKey()
                    if (currentArcKey != null && currentArcKey == state.arcanoEmCompraViaXpKey) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    }

                    if (currentAdvantage.nome.contains("Pontos de Poder", true)) {
                        state.removerPontosDePoder(currentAdvantage)
                    } else {
                        state.removeVantagemDinheiro(currentAdvantage)
                        state.removerVantagem(currentAdvantage)
                    }
                    state.pontosVantagem++
                }
            }

            // Fix: Use copy() to avoid shared reference mutation
            val vantagemCopia = vantagem.copy()

            // Identify if this is a new Arcane Background acquisition (e.g. Mystic Powers)
            // We must snapshot the slots state BEFORE adding the advantage, because adding it
            // might auto-populate fixed powers (Mystic Powers) into the slots.
            // If we snapshot AFTER, the snapshot will contain the fixed powers, preventing clean undo.
            val arcKeyToInit = vantagemCopia.toArcanoKey()
            val isNovosPoderes = vantagemCopia.id == "novos_poderes"

            if (arcKeyToInit != null && !isNovosPoderes) {
                state.iniciarCompraArcanoViaXp(arcKeyToInit)
            }

            if (vantagemCopia.nome.keyify() == "CAVALEIRO" && !vantagemCopia.choice.isNullOrBlank()) {
                state.adicionarVantagemCavaleiro(vantagemCopia, vantagemCopia.choice!!)
            } else if (vantagemCopia.nome.contains("Pontos de Poder", true)) {
                state.comprarPontoDePoder(vantagemCopia)
            } else {
                state.applyVantagemDinheiro(vantagemCopia)
                state.checkAndRefundResourcePb()
                state.adicionarVantagem(vantagemCopia)
            }
            state.pontosVantagem--
            state.advantageForCurrentAdvancement = vantagemCopia.id

            // Check if it's "Novos Poderes" to trigger the flow
            if (isNovosPoderes) {
                // Find target arcane background
                // 1. Try choice if set
                val choiceKey = advantageArcaneKey(vantagemCopia)
                // 2. If not, try to find the first existing arcane background
                val arcKey = choiceKey ?: state.vantagensSelecionadas.firstNotNullOfOrNull {
                    it.toArcanoKey()?.normAAKey()
                }

                if (arcKey != null) {
                    state.iniciarCompraArcanoViaXp(arcKey)
                } else {
                    state.limparCompraArcanoViaXp(restaurarSnapshot = false)
                }
            } else {
                if (arcKeyToInit != null) {
                    // Already initialized above. Do nothing.
                } else {
                    state.limparCompraArcanoViaXp(restaurarSnapshot = false)
                }
            }

            state.rebuildAllPericiaStacks()
        }
    }

    private fun advantageArcaneKey(v: Vantagem): String? {
        return v.choice?.normAAKey()
    }

    fun startAttributeAdvancement(
        slotIndex: Int,
        stageName: String,
        consumesLegendaryReservation: Boolean
    ) {
        if (state.progressosDisponiveis >= 1) {
            resetUiState()
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.stageNameForCurrentAdvancement = stageName
            state.attributeStageForCurrentAdvancement = stageName
            state.xpSlots[slotIndex] = true

            if (consumesLegendaryReservation) {
                state.legendaryAttrReservations =
                    (state.legendaryAttrReservations - 1).coerceAtLeast(0)
            }
            state.attributeUsedReservation = consumesLegendaryReservation

            state.paFromProgress += 1
            state.recalcularPontosAtributo()

            state.attributeAdvancementInProgress = true
            state.attributeStacksBeforeAdvancement = state.snapshotAttributeStacks()
            state.mostrandoAtributosProgresso = true
            state.updateEmProgressoFlag()
        }
    }

    fun reserveLegendaryAttribute(slotIndex: Int, stageName: String) {
        if (state.progressosDisponiveis >= 1 && state.legendaryAttrReservations == 0) {
            state.progresso++
            state.spendProgressAtStage(stageName, 1)
            state.xpSlots[slotIndex] = true
            state.legendaryAttrReservations += 1
            state.advancementHistory.add(
                AdvancementAction.ReserveLegendaryAttribute(stageName = stageName)
            )
            state.recomputeAvailableProgress()
        }
    }

    fun increaseAttributeForAdvancement(attributeKey: String) {
        if (!state.attributeAdvancementInProgress) return
        if (state.paFromProgress <= 0) {
            logFeedback("Sem pontos de atributo disponíveis.")
            return
        }

        // Apply increase using generic PA logic
        val stack = state.paCostStackPorAtributo.getOrPut(attributeKey) { mutableListOf() }
        stack.add(1) // dummy cost, recalculated later

        state.recalcularPontosAtributo()
        state.checkFreeze()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun finishAttributeAdvancement() {
        if (state.attributeAdvancementInProgress) {
            val before = state.attributeStacksBeforeAdvancement ?: emptyMap()
            val increases = mutableListOf<String>()
            state.paCostStackPorAtributo.forEach { (attr, stack) ->
                val diff = stack.size - (before[attr] ?: 0)
                repeat(diff.coerceAtLeast(0)) { increases.add(attr) }
            }

            if (increases.isEmpty()) {
                // Nothing increased, so nothing to finish. Revert.
                cancelAdvancementInProgress()
                return
            }

            val stageName = state.attributeStageForCurrentAdvancement ?: state.estagioAtual().nome
            var reservationAvailable = state.attributeUsedReservation
            increases.forEach { attr ->
                val usedReservation = reservationAvailable
                if (reservationAvailable) reservationAvailable = false
                if (!state.isAttributeFreeForMonster(attr)) {
                    val prev = state.comprasAttrPorEstagio[stageName] ?: 0
                    state.comprasAttrPorEstagio[stageName] = prev + 1
                }
                state.advancementHistory.add(
                    AdvancementAction.IncreaseAttribute(
                        attributeName = attr,
                        usedLegendaryReservation = usedReservation,
                        stageName = stageName,
                        progressCost = 1
                    )
                )
            }

            state.attributeAdvancementInProgress = false
            state.attributeStageForCurrentAdvancement = null
            state.stageNameForCurrentAdvancement = null
            state.attributeStacksBeforeAdvancement = null
            state.attributeUsedReservation = false
            state.mostrandoAtributosProgresso = false
            state.recomputeAvailableProgress()
            state.checkFreeze()
            state.updateEmProgressoFlag()
            state.mostrandoAtributosProgresso = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun cancelAdvancementInProgress() {
        // Roda apenas se houver um avanço em andamento para ser cancelado.
        if (
            !state.skillAdvancementInProgress &&
            !state.advantageAdvancementInProgress &&
            !state.attributeAdvancementInProgress
        ) {
            return
        }

        val stageName = state.stageNameForCurrentAdvancement
            ?: state.attributeStageForCurrentAdvancement
            ?: state.estagioAtual().nome

        val lastUsedIndex = state.xpSlots.indexOfLast { it }
        if (lastUsedIndex != -1) {
            state.xpSlots[lastUsedIndex] = false
            state.progresso--
            state.refundProgressAtStage(stageName, 1)
        }

        if (state.skillAdvancementInProgress) {
            // Revert changes made during this session
            state.skillsForCurrentAdvancement.forEach { skillName ->
                val skill = state.periciasComIdiomas().firstOrNull { it.nome.keyify() == skillName.keyify() }
                    ?: periciasMapData()[skillName.keyify()]
                if (skill != null) {
                    state.decreasePericia(skill)
                }
            }
            state.skillsForCurrentAdvancement.clear()
            state.spFromProgress = (state.spFromProgress - 2).coerceAtLeast(0)
            state.rebuildAllPericiaStacks()
        }

        if (state.advantageAdvancementInProgress) {
            state.advantageForCurrentAdvancement?.let { advId ->
                state.vantagensSelecionadas.firstOrNull { it.id == advId }?.let { vant ->
                    val arcKey = vant.toArcanoKey()?.normAAKey()
                    // Se a vantagem for um antecedente arcano sendo comprado OU
                    // se for uma vantagem que acionou o fluxo de compra de poderes (ex: Novos Poderes)
                    if (state.arcanoEmCompraViaXpKey != null) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    } else if (arcKey != null && arcKey == state.arcanoEmCompraViaXpKey) {
                        state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                    }

                    if (vant.nome.contains("Pontos de Poder", true)) {
                        state.removerPontosDePoder(vant)
                    } else {
                        state.removeVantagemDinheiro(vant)
                        state.removerVantagem(vant)
                    }
                    state.pontosVantagem++
                }
            }
            state.pontosVantagem = (state.pontosVantagem - 1).coerceAtLeast(0)
            state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
        }

        if (state.attributeAdvancementInProgress) {
            state.paFromProgress = (state.paFromProgress - 1).coerceAtLeast(0)
            state.attributeStacksBeforeAdvancement?.let { state.restoreAttributeStacks(it) }
            if (state.attributeUsedReservation) {
                state.legendaryAttrReservations += 1
            }
            state.attributeUsedReservation = false
            state.attributeStageForCurrentAdvancement = null
            state.attributeStacksBeforeAdvancement = null
            state.mostrandoAtributosProgresso = false
        }

        state.skillAdvancementInProgress = false
        state.advantageAdvancementInProgress = false
        state.attributeAdvancementInProgress = false
        state.advantageForCurrentAdvancement = null
        state.stageNameForCurrentAdvancement = null
        state.mostrandoPericiasProgresso = false
        state.mostrandoVantagensProgresso = false
        state.mostrandoPoderesProgresso = false
        state.updateEmProgressoFlag()
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun revertLastAdvancement() {
        if (state.advancementHistory.isEmpty()) return

        val lastAction = state.advancementHistory.removeLast()
        val stageName = lastAction.stageName

        // Reverte o slot de XP e o contador de progresso
        val lastUsedIndex = state.xpSlots.indexOfLast { it }
        if (lastUsedIndex != -1) {
            state.xpSlots[lastUsedIndex] = false
            state.progresso--
        }

        when (lastAction) {
            is AdvancementAction.SpendOnAdvantage -> {
                // Reverte o gasto E a concessão do ponto de vantagem
                // Usa lastOrNull para pegar a instância mais recente (importante para Pontos de Poder que pode ter várias)
                val advantage = state.vantagensSelecionadas.lastOrNull { it.id == lastAction.advantageId }
                if (advantage != null) {
                    if (advantage.nome.contains("Pontos de Poder", true)) {
                        state.removerPontosDePoder(advantage, estagioOverride = lastAction.stageName)
                    } else {
                        state.removeVantagemDinheiro(advantage)
                        state.removerVantagem(advantage)
                    }
                }
                lastAction.arcanoKey?.let { arcKey ->
                    state.restoreArcanoSlots(arcKey, lastAction.previousArcanoSlots)
                }
                // Não decrementamos pontosVantagem aqui porque:
                // 1) Ao remover a vantagem da lista, estamos "estornando" o gasto (o que incrementaria +1).
                // 2) Ao desfazer o avanço, estamos removendo a concessão do ponto (o que decrementaria -1).
                // Saldo líquido = 0. Então basta não mexer em pontosVantagem.

                state.pvFromXpOutstanding = (state.pvFromXpOutstanding - 1).coerceAtLeast(0)
                state.frozenAdvantageCount = state.vantagensSelecionadas.size
            }
            is AdvancementAction.IncreaseAttribute -> {
                val stack = state.paCostStackPorAtributo[lastAction.attributeName]
                if (stack != null && stack.isNotEmpty()) {
                    stack.removeLast()
                    val current = state.valoresAtributos[lastAction.attributeName]!!.intValue
                    val prevRaw = if (current > 12) current - 1 else current - 2
                    state.valoresAtributos[lastAction.attributeName]!!.intValue = prevRaw

                    if (!state.isAttributeFreeForMonster(lastAction.attributeName)) {
                        val prev = state.comprasAttrPorEstagio[lastAction.stageName] ?: 0
                        if (prev > 0) {
                            state.comprasAttrPorEstagio[lastAction.stageName] = prev - 1
                        }
                    }

                    state.paFromProgress = (state.paFromProgress - 1).coerceAtLeast(0)
                    state.recalcularPontosAtributo()
                    if (lastAction.usedLegendaryReservation) {
                        state.legendaryAttrReservations += 1
                    }
                }
            }
            is AdvancementAction.SpendOnSkills -> {
                // Reverte o gasto dos pontos de perícia
                lastAction.skillsIncreased.forEach { skillName ->
                    val skill = state.periciasComIdiomas().firstOrNull { it.nome.keyify() == skillName.keyify() }
                        ?: periciasMapData()[skillName.keyify()]
                    if (skill != null) {
                        state.decreasePericia(skill)
                    }
                }
                // Reverte a concessão dos pontos de perícia
                state.spFromProgress = (state.spFromProgress - 2).coerceAtLeast(0)

                state.rebuildAllPericiaStacks()
            }
            is AdvancementAction.RemoveHindrance -> {
                val hindrance = complicacoesData().first { it.id == lastAction.hindranceId }
                when (lastAction.changeType) {
                    HindranceChangeType.RESERVATION -> {
                        state.reservasComplicacaoMaior.remove(hindrance.id)
                    }

                    HindranceChangeType.REDUCE_TO_MINOR -> {
                        val previous = lastAction.previousLevel ?: "Maior"
                        state.complicacoesSelecionadas[hindrance] = previous
                    }

                    HindranceChangeType.REMOVE -> {
                        val previous = lastAction.previousLevel
                        if (previous != null) {
                            state.complicacoesSelecionadas[hindrance] = previous
                        }
                        if (lastAction.usedReservation) {
                            state.reservasComplicacaoMaior[hindrance.id] = true
                        }
                    }
                }
            }
            is AdvancementAction.ReserveLegendaryAttribute -> {
                state.legendaryAttrReservations =
                    (state.legendaryAttrReservations - 1).coerceAtLeast(0)
            }
        }
        // Devolve o ponto de avanço ao "pool"
        state.refundProgressAtStage(stageName, lastAction.progressCost)
        if (state.modoProgressaoAtivo) {
            state.snapshotFrozenSkillIncrements()
        }
        state.updateEmProgressoFlag()
    }
}
