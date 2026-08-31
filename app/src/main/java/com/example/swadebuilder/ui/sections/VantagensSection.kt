package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.getDisplayName
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.model.explainVantagemVisibility
import com.example.swadebuilder.model.isVantagemVisible
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.ChoiceButtonRow
import com.example.swadebuilder.ui.components.CollapsibleSection
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.ui.theme.LocalAppThemeData
import com.example.swadebuilder.util.debugLog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.ptBrCollator
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VantFilterDialog(
    allEstagios: List<String>,
    allAtributos: List<String>,
    allPericias: List<String>,
    current: VantFilter,
    onChange: (VantFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros Avançados") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp)
            ) {
                Text("Estágio", fontWeight = FontWeight.Bold)
                allEstagios.forEach { e ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = e in current.estagios,
                            onCheckedChange = {
                                val s = current.estagios.toMutableSet()
                                if (it) s += e else s -= e
                                onChange(current.copy(estagios = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(e)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Atributos", fontWeight = FontWeight.Bold)
                allAtributos.forEach { a ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = a in current.atributos,
                            onCheckedChange = {
                                val s = current.atributos.toMutableSet()
                                if (it) s += a else s -= a
                                onChange(current.copy(atributos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(a)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Perícias", fontWeight = FontWeight.Bold)
                allPericias.forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = p in current.pericias,
                            onCheckedChange = {
                                val s = current.pericias.toMutableSet()
                                if (it) s += p else s -= p
                                onChange(current.copy(pericias = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(p)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VantagensContent(
    state: CriadorState,
    multiplosAAHabilitados: Boolean,
    viewModel: CriadorViewModel = viewModel(),
    allAdvantages: List<Vantagem>,
    allSkills: List<Pericia>,
    allEstagios: List<Estagio>,
    onUserFeedback: () -> Unit = {}
) {
    val context = LocalContext.current
    val powerCacheState by androidx.compose.runtime.produceState<Map<String, List<Poder>>?>(initialValue = null) {
        withContext(Dispatchers.IO) {
            value = com.example.swadebuilder.model.poderesPorOrigem(context)
        }
    }
    val powerCache: Map<String, List<Poder>> = powerCacheState ?: emptyMap()
    val isPowersLoading = powerCacheState == null

    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo

    val listaVantagens: List<Vantagem> =
        remember(multiplosAAHabilitados, allAdvantages) {
            allAdvantages
        }

    remember(state.modoSupers) {
        buildSet {
            add("BASICO")
            if (state.modoSupers) add("SUPER")
        }
    }

    val listaVantagensAtivas: List<Vantagem> = remember(
        listaVantagens,
        state.modoSupers,
        state.compendioFantasiaAtivo,
        state.compendioHorrorAtivo,
        state.compendioSciFiAtivo,
        state.modoMonstroAtivo,
        state.compendioPathfinderAtivo,
        state.compendioDeadlandsAtivo,
        state.compendioCrystalHeartAtivo,
        state.compendioArteDaGuerraAtivo,
        state.compendioCidadeSolVaporAtivo,
        state.compendioWiseguysAtivo,
        multiplosAAHabilitados
    ) {
        listaVantagens
            .filter { vant -> state.isVantagemVisible(vant, multiplosAAHabilitados) }
            .groupBy { it.id.keyify() }
            .map { (_, duplicates) ->
                duplicates.maxByOrNull { CriadorState.getOriginPriority(it.origem) }!!
            }
            .sortedWith(
                compareBy<Vantagem> { it.categoria }
                    .thenBy(ptBrCollator) { it.nomeExibicao }
            )
    }

    LaunchedEffect(
        state.compendioCidadeSolVaporAtivo,
        listaVantagens,
        listaVantagensAtivas,
        multiplosAAHabilitados
    ) {
        if (!state.compendioCidadeSolVaporAtivo) return@LaunchedEffect

        val steamAll = listaVantagens.filter { canonicalOriginKey(it.origem) == "CIDADE_SOL_VAPOR" }
        val steamVisible = listaVantagensAtivas.filter { canonicalOriginKey(it.origem) == "CIDADE_SOL_VAPOR" }
        val hiddenSteam = steamAll.filterNot { state.isVantagemVisible(it, multiplosAAHabilitados) }

        debugLog(
            "SWADE_DEBUG",
            "[SolVapor] vantagens totais=${listaVantagens.size}, sol_vapor_total=${steamAll.size}, " +
                "sol_vapor_visiveis=${steamVisible.size}, multiAA=$multiplosAAHabilitados"
        )

        hiddenSteam.take(30).forEach { vant ->
            debugLog(
                "SWADE_DEBUG",
                "[SolVapor] hidden id=${vant.id}, nome=${vant.nomeExibicao}, origem=${vant.origem}, " +
                    "reason=${state.explainVantagemVisibility(vant, multiplosAAHabilitados)}"
            )
        }
    }

    val idParaNome = remember(allAdvantages) {
        allAdvantages.associate { it.id to it.nomeExibicao.toFancyTitleCase() }
    }

    // --- Search & Filter State ---
    val searchQuery = state.vantSearchQuery
    var debouncedSearchQuery by remember { mutableStateOf(searchQuery) }

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(300)
        debouncedSearchQuery = searchQuery
    }

    val selectedCategories = state.vantSelectedCategories
    val filter = state.vantFilter
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    // --- Interaction State ---
    var pendingVantagem by remember { mutableStateOf<Vantagem?>(null) }
    var showChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var dialogMostrandoAntecedente by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoPoderesMisticos by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoCavaleiro by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoMontaria by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoNovosPoderes by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoPoderFavorito by remember { mutableStateOf<Vantagem?>(null) }
    var subOpcaoSelecionada by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val expandedMap = state.categoriasVantagensExpandidas
    val vantagemEmFoco = state.vantagemEmFoco

    LaunchedEffect(vantagemEmFoco) {
        if (!vantagemEmFoco.isNullOrBlank()) {
            val v = listaVantagensAtivas.firstOrNull { it.nome == vantagemEmFoco || it.nomeExibicao == vantagemEmFoco }
            if (v != null) {
                expandedMap[v.categoria] = true
            }
        }
    }

    var initialCount by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(state.emProgresso) {
        if (state.emProgresso) initialCount = state.vantagensSelecionadas.size
    }

    val locked = state.criacaoBasicaCongeladaComXp
    val allowLongTexts = booleanResource(com.example.swadebuilder.R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }

    val pcTotal = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val pvUsados = state.cpPvStack.size

    val isSearching = searchQuery.isNotBlank()
    val isFilteringCategories = selectedCategories.isNotEmpty()
    val browsingMode = !isSearching && !isFilteringCategories

    // --- PERFORMANCE OPTIMIZATION: Calculations moved up ---
    val hasProfissional = state.vantagensSelecionadas.any { it.id == "profissional" }

    val filteredListGlobal = remember(
        listaVantagensAtivas,
        state.modoSupers,
        state.modoMonstroAtivo,
        state.tipoMonstroSelecionado,
        hasProfissional,
        filter,
        multiplosAAHabilitados
    ) {
        listaVantagensAtivas.filter { vant ->
            // Monster mode: for MONSTRUOSAS, only show matching template edges + generic ones (without template)
            if (state.modoMonstroAtivo && vant.categoria == Categoria.MONSTRUOSAS) {
                val requiredTemplates = vant.requisitos.templatesRequired.map { it.keyify() }
                if (requiredTemplates.isNotEmpty()) {
                    val selectedTemplate = state.tipoMonstroSelecionado?.keyify()
                    if (selectedTemplate == null || selectedTemplate !in requiredTemplates) return@filter false
                }
            }

            // Professional/Specialist Dependency
            if (vant.id == "especialista" && !hasProfissional) return@filter false

            // Advanced Filters
            if (!filter.isEmpty()) {
                val vantOrigem = canonicalOriginKey(vant.origem)
                if (filter.origens.isNotEmpty() && vantOrigem !in filter.origens.map(::canonicalOriginKey).toSet()) return@filter false
                if (filter.estagios.isNotEmpty() && vant.requisitos.estagio !in filter.estagios) return@filter false
                if (filter.atributos.isNotEmpty() && filter.atributos.intersect(vant.requisitos.atributoMin.keys).isEmpty()) return@filter false
                if (filter.pericias.isNotEmpty()) {
                    val filterPericias = if (state.compendioArteDaGuerraAtivo) {
                        filter.pericias + if ("Jutsu" in filter.pericias) setOf("Lutar") else emptySet()
                    } else {
                        filter.pericias
                    }
                    val reqMin = vant.requisitos.periciaMin.keys
                    val reqOpt = vant.requisitos.periciaMinOpcional.keys
                    val vinc = if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
                    if (filterPericias.intersect(reqMin + reqOpt + vinc).isEmpty()) return@filter false
                }
            }
            true
        }
    }

    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.ancestralidade, state.vantagemAdaptavelSelecionadaId, state.vantagensSelecionadas.size) {
        state.hasFreeAdaptavelSlotNow(debugSource = "VantagensSection:LaunchedEffect")
        debugLog(
            "AdaptavelDebug",
            "[VantagensSection:LaunchedEffect] pontosVantagem=${state.pontosVantagem} vantagensSelecionadas=${state.vantagensSelecionadas.size}"
        )
    }

    val protagonistaSlotCategoria = when (state.protagonistaRollVantagem) {
        1 -> "Chi"
        2 -> "Estranhas"
        3 -> "Antecedente"
        4 -> "Combate"
        5 -> "Sociais"
        else -> null
    }

    // Helper to auto-spend BP and buy
    val attemptPurchase: (Vantagem, () -> Unit) -> Unit = { vantToBuy, onSuccess ->
        val isFreePathfinder = state.pathfinderSlotAvailable && state.isPathfinderEligible(vantToBuy)
        val isFreeProtagonista = state.protagonistaSlotAvailable && state.isProtagonistaEligible(vantToBuy)
        val isFreeSamurai = state.samuraiCombatSlotAvailable && vantToBuy.categoria == Categoria.COMBATE
        val isFreeAdaptavel = state.hasFreeAdaptavelSlotNow(debugSource = "VantagensSection:attemptPurchase:${vantToBuy.id}") &&
                (vantToBuy.requisitos.estagio.isBlank() || vantToBuy.requisitos.estagio.equals("Novato", ignoreCase = true)) &&
                !state.isVantagemAutomatica(vantToBuy)

        val needsPoints = !state.modoLivre && !isFreePathfinder && !isFreeProtagonista && !isFreeSamurai && !isFreeAdaptavel

        var failed = false
        if (needsPoints && state.pontosVantagem <= 0) {
            if (pcLivres >= 2) {
                state.gastarPcParaVantagem()
            } else {
                failed = true
                viewModel.logFeedback("Pontos insuficientes.")
            }
        }

        if (!failed) {
            if (state.advantageAdvancementInProgress) {
                viewModel.selectAdvantageForAdvancement(vantToBuy)
                onUserFeedback()
                viewModel.logFeedback("Vantagem ${vantToBuy.nome} adicionada.")
                onSuccess()
            } else {
                val bought = state.comprarVantagem(vantToBuy) { msg ->
                    viewModel.logFeedback(msg)
                    onUserFeedback()
                }
                if (bought) onSuccess()
            }
        }
    }

    // Reusable Header Content
    val headerContent: @Composable () -> Unit = {
        Column {
            if (!state.modoLivre) {
                SectionHeader(
                    onHelpClick = null,
                    centerText = "Pontos de Vantagem: ${state.pontosVantagem}${if (!locked && pcLivres >= 2) " (+${pcLivres / 2} via PB)" else ""}",
                    onListaCompletaClick = null,
                    listaCompletaText = ""
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.size(4.dp))

            // Sticky Header for Selected Advantages
            if (state.vantagensSelecionadas.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .alpha(if (locked && !state.modoProgressaoAtivo) 0.6f else 1f)
                ) {
                    if (state.regraMultiplosIdiomas) {
                        AssistChip(
                            onClick = { },
                            enabled = false,
                            label = { Text("LINGUISTA (Regra de Ambientação)", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    state.vantagensSelecionadas.forEachIndexed { index, vant ->
                        val isAutomatic = state.isVantagemAutomatica(vant)

                        val requiredByAnother = state.vantagensSelecionadas.any { other ->
                            other != vant && other.requisitos.vantagensPrevias.any { reqId ->
                                reqId == vant.id
                            }
                        }

                        val isFromSuperPoder = state.vantagensDePoder.contains(vant.id)
                        val isSuperpoderesLocked = state.modoSupers && vant.id == "superpoderes"
                        val isCrystalHeartLocked = state.compendioCrystalHeartAtivo && vant.id == "aa_agente_syn"

                        val baseRemovable = !locked &&
                                when (vant.id) {
                                    "o_melhor_que_ha" -> {
                                        val gastoAtual = state.poderFavoritoId?.let { state.gastosPorPoder[it] } ?: 0
                                        val limitePadrao = state.limitePorPoderPadrao
                                        gastoAtual <= limitePadrao
                                    }
                                    else -> true
                                } &&
                                index >= initialCount &&
                                index >= state.frozenAdvantageCount &&
                                !isAutomatic &&
                                !requiredByAnother &&
                                !isFromSuperPoder &&
                                !isSuperpoderesLocked &&
                                !isCrystalHeartLocked

                        val canRemove =
                            baseRemovable && !(state.emProgresso && vant.id == "novos_poderes")

                        val isCelestialAAMilagresDesabilitado = state.celestialAAMilagresDesabilitado &&
                                vant.id == "antecedente_arcano_milagres"

                        val arcKey = vant.toArcanoKey()

                        AssistChip(
                            onClick = {
                                if (!canRemove) return@AssistChip

                                val (pode, msg) = state.podeRemoverVantagem(vant)
                                if (!pode) {
                                    viewModel.logFeedback(msg ?: "Não é possível remover.")
                                    onUserFeedback()
                                    return@AssistChip
                                }

                                state.venderVantagem(vant) { msg ->
                                    viewModel.logFeedback(msg)
                                    onUserFeedback()
                                }

                                // Auto-Refund Logic
                                if (state.pontosVantagem > 0 && state.cpPvStack.isNotEmpty()) {
                                    state.devolverPcDeVantagem()
                                }

                                if (arcKey != null && arcKey == state.arcanoEmCompraViaXpKey) {
                                    state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                                    if (state.advantageForCurrentAdvancement == vant.id) {
                                        state.advantageForCurrentAdvancement = null
                                    }
                                }
                            },
                            enabled = canRemove,
                            label = {
                                val baseName = vant.nomeExibicao.toFancyTitleCase()
                                val subtipo = vant.subtipoArcano?.toFancyTitleCase()
                                val adjustedName = if (!subtipo.isNullOrBlank() && !baseName.contains(subtipo, ignoreCase = true)) {
                                    "$baseName ($subtipo)"
                                } else {
                                    baseName
                                }

                                val labelText = when {
                                    vant.id == "arma_predileta" &&
                                        state.compendioArteDaGuerraAtivo &&
                                        state.tropoSelecionado?.id == "tropo_youxia" &&
                                        !state.youxiaJutsuSelecionado.isNullOrBlank() ->
                                        "$adjustedName (${state.youxiaJutsuSelecionado})"
                                    else -> vant.choice?.let { "$adjustedName ($it)" } ?: adjustedName
                                }
                                val finalText = if (isCelestialAAMilagresDesabilitado) {
                                    "$labelText (DESABILITADO)"
                                } else {
                                    labelText
                                }
                                Text(finalText, style = MaterialTheme.typography.labelSmall)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = if (canRemove) "Remover" else "",
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
            }

            if (!state.emProgresso) {
                if (state.temAdaptavel()) {
                    val slotAvailable = state.vantagemAdaptavelSelecionadaId == null
                    val label = state.getAdaptavelLabel()
                    val (color, text) = if (slotAvailable) {
                        MaterialTheme.colorScheme.primaryContainer to "Vantagem de $label disponível"
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant to "Vantagem de $label utilizada"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                }

                if (state.compendioPathfinderAtivo) {
                    val slotAvailable = state.pathfinderSlotAvailable
                    val (color, text) = if (slotAvailable) {
                        MaterialTheme.colorScheme.primaryContainer to "Vantagem de Classe/Profissional/Antecedente gratuita DISPONÍVEL"
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant to "Vantagem de Classe/Profissional/Antecedente gratuita UTILIZADA"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                }

                if (state.compendioArteDaGuerraAtivo && state.tropoSelecionado?.id == "tropo_protagonista") {
                    val slotAvailable = state.protagonistaSlotAvailable
                    val slotPrefix = protagonistaSlotCategoria?.let {
                        "Vantagem de $it de Protagonista gratuita"
                    } ?: "Vantagem de Protagonista gratuita"
                    val (color, text) = if (slotAvailable) {
                        MaterialTheme.colorScheme.primaryContainer to "$slotPrefix DISPONÍVEL"
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant to "$slotPrefix UTILIZADA"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                }

                // Legacy PB buttons removed

                Spacer(Modifier.size(8.dp))
            }

            if (state.nasceUmHeroi && !state.emProgresso) {
                AssistChip(
                    onClick = { },
                    label = { Text("Nasce um Herói ativo: Estágio ignorado na criação") },
                    leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = "Nasce um Herói ativo") }
                )
                Spacer(Modifier.size(8.dp))
            }

            // --- Search and Filters ---
            ExpandableSearchFilter(
                query = searchQuery,
                onQueryChange = { state.vantSearchQuery = it },
                isExpanded = isSearchExpanded,
                onExpandedChange = { isSearchExpanded = it },
                onClear = {
                    state.vantSearchQuery = ""
                    state.vantSelectedCategories.clear()
                    state.vantFilter = VantFilter()
                },
                placeholder = "Pesquisar Vantagens..."
            ) {
                Spacer(Modifier.size(8.dp))

                // Category Chips - moved outside LazyRow
                val activeCategories = remember(listaVantagensAtivas) {
                    listaVantagensAtivas.map { it.categoria }.toSet()
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Advanced Filters Chip
                    item(key = "advanced_filters", contentType = "filter_chip") {
                        FilterChip(
                            selected = !filter.isEmpty(),
                            onClick = { showFilterDialog = true },
                            label = { Text("Filtros Avançados${if(!filter.isEmpty()) " (!)" else ""}") }
                        )
                    }

                    items(
                        items = Categoria.entries.toTypedArray(),
                        key = { it.name },
                        contentType = { "category_chip" }
                    ) { cat ->
                        if (state.modoSupers && cat == Categoria.PODER) return@items
                        if (cat !in activeCategories) return@items

                        FilterChip(
                            selected = cat in selectedCategories,
                            onClick = {
                                if (cat in selectedCategories) selectedCategories.remove(cat)
                                else selectedCategories.add(cat)
                            },
                            label = {
                                Text(cat.getDisplayName())
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.size(8.dp))
        }
    }

    // Refactored Layout: Fixed Header + Scrollable List
    Column(modifier = Modifier.fillMaxWidth()) {
        headerContent()

        if (browsingMode) {
            // Browse Mode (Accordions) - NOW OPTIMIZED WITH LAZYCOLUMN
            val categoriasBy = remember(filteredListGlobal) {
                filteredListGlobal.groupBy { it.categoria }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // REMOVED: item { headerContent() } - now fixed at top

                Categoria.entries.forEach { cat ->
                    val lista = categoriasBy[cat]
                    if (lista == null) return@forEach // Skip empty

                    if (state.modoSupers && cat == Categoria.PODER) return@forEach
                    // Logic already handled by active list, but keeping explicit checks safe

                    val expanded = expandedMap[cat] ?: false

                    // Category Header
                    item(key = "header_${cat.name}") {
                        Column {
                            CollapsibleSection(
                                title = cat.getDisplayName(),
                                expanded = expanded,
                                onToggle = { expandedMap[cat] = !expanded },
                                onToggleFeedback = onUserFeedback
                            ) {} // Empty content, we use CollapsibleSection just for header rendering

                            Spacer(Modifier.size(8.dp))
                        }
                    }

                    // Category Content (Only if expanded)
                    if (expanded) {
                        items(lista, key = { it.id }, contentType = { "vantagem_item" }) { vant ->
                             // We need a wrapper to provide the padding used in the original Column
                             Column(modifier = Modifier.padding(start = 8.dp, bottom = 0.dp)) {
                                 VantagemItem(
                                     vant = vant,
                                     state = state,
                                     allEstagios = allEstagios,
                                     locked = locked,
                                     allowLongTexts = allowLongTexts,
                                     showOfficialNames = showOfficialNames,
                                     idParaNome = idParaNome,
                                     detalhesExpandidos = detalhesExpandidos,
                                     protagonistaSlotCategoria = protagonistaSlotCategoria,
                                     pcLivres = pcLivres,
                                     onSelect = {
                                        if (vant.vinculadoPericia) {
                                            pendingVantagem = vant
                                            showChoiceDialog = true
                                        } else if (vant.id == "antecedente_arcano") {
                                            dialogMostrandoAntecedente = vant
                                        } else if (vant.id == "poderes_misticos" || vant.id == "poderes_misticos_anjo" || vant.id == "poderes_misticos_demonio" || vant.id == "poderes_misticos_mumia") {
                                            dialogMostrandoPoderesMisticos = vant
                                        } else if (vant.nome.keyify() == "CAVALEIRO") {
                                            dialogMostrandoCavaleiro = vant
                                        } else if (vant.nome.keyify() == "MONTARIA") {
                                            dialogMostrandoMontaria = vant
                                        } else if (vant.id == "novos_poderes") {
                                            val activeABs = state.vantagensSelecionadas
                                                .mapNotNull { it.toArcanoKey() }
                                                .distinct()
                                            if (activeABs.size > 1) {
                                                dialogMostrandoNovosPoderes = vant
                                            } else {
                                                attemptPurchase(vant) {}
                                            }
                                        } else if (vant.id == "poder_favorito") {
                                            val ownedPowers = state.poderesSelecionados.filterNotNull()
                                            if (ownedPowers.isEmpty()) {
                                                viewModel.logFeedback("Escolha ao menos um poder na seção de Poderes!")
                                                onUserFeedback()
                                            } else {
                                                dialogMostrandoPoderFavorito = vant
                                            }
                                        } else {
                                            attemptPurchase(vant) {}
                                        }
                                     },
                                     onError = { msg ->
                                         viewModel.logFeedback(msg)
                                         onUserFeedback()
                                     }
                                 )
                             }
                        }
                    }
                }
            }
        } else {
            // Search Mode (Flat List)
            val isDebouncedSearching = debouncedSearchQuery.isNotBlank()
            val flatList = remember(filteredListGlobal, selectedCategories, debouncedSearchQuery) {
                val q = if (isDebouncedSearching) debouncedSearchQuery.semAcentos().lowercase() else ""
                filteredListGlobal.filter { vant ->
                    if (selectedCategories.isNotEmpty() && vant.categoria !in selectedCategories) return@filter false
                    if (isDebouncedSearching) {
                        val n = vant.nomeExibicao.semAcentos().lowercase()
                        val d = vant.descricao.semAcentos().lowercase()
                        val original = vant.nome.semAcentos().lowercase()
                        if (!n.contains(q) && !d.contains(q) && !original.contains(q)) return@filter false
                    }
                    true
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Fill remaining space
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                 if (flatList.isEmpty()) {
                     item(key = "empty_list", contentType = "message") {
                         com.example.swadebuilder.ui.components.EmptyState(message = "Nenhuma vantagem encontrada.")
                     }
                 } else {
                     items(flatList, key = { it.id }, contentType = { "vantagem_item" }) { vant ->
                         VantagemItem(
                             vant = vant,
                             state = state,
                             allEstagios = allEstagios,
                             locked = locked,
                             allowLongTexts = allowLongTexts,
                             showOfficialNames = showOfficialNames,
                             idParaNome = idParaNome,
                             detalhesExpandidos = detalhesExpandidos,
                             protagonistaSlotCategoria = protagonistaSlotCategoria,
                             pcLivres = pcLivres,
                             onSelect = {
                                if (vant.vinculadoPericia) {
                                    pendingVantagem = vant
                                    showChoiceDialog = true
                                } else if (vant.id == "antecedente_arcano") {
                                    dialogMostrandoAntecedente = vant
                                } else if (vant.id == "poderes_misticos" || vant.id == "poderes_misticos_anjo" || vant.id == "poderes_misticos_demonio" || vant.id == "poderes_misticos_mumia") {
                                    dialogMostrandoPoderesMisticos = vant
                                } else if (vant.nome.keyify() == "CAVALEIRO") {
                                    dialogMostrandoCavaleiro = vant
                                } else if (vant.nome.keyify() == "MONTARIA") {
                                    dialogMostrandoMontaria = vant
                                } else if (vant.id == "novos_poderes") {
                                    val activeABs = state.vantagensSelecionadas
                                        .mapNotNull { it.toArcanoKey() }
                                        .distinct()
                                    if (activeABs.size > 1) {
                                        dialogMostrandoNovosPoderes = vant
                                    } else {
                                        attemptPurchase(vant) {}
                                    }
                                } else if (vant.id == "poder_favorito") {
                                    val ownedPowers = state.poderesSelecionados.filterNotNull()
                                    if (ownedPowers.isEmpty()) {
                                        viewModel.logFeedback("Escolha ao menos um poder na seção de Poderes!")
                                        onUserFeedback()
                                    } else {
                                        dialogMostrandoPoderFavorito = vant
                                    }
                                } else {
                                    attemptPurchase(vant) {}
                                }
                            },
                             onError = { msg ->
                                 viewModel.logFeedback(msg)
                                 onUserFeedback()
                             }
                         )
                     }
                 }
            }
        }
    }

    if (showFilterDialog) {
        val allEstagiosNames = allEstagios.map { it.nome }
        val allAtributos = state.mapaAtributosDisplay.values.toList()
        val requiredPericias = listaVantagensAtivas.flatMap { vant ->
            vant.requisitos.periciaMin.keys +
                    vant.requisitos.periciaMinOpcional.keys +
                    if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
        }.distinct()

        val visibleSkills = state.periciasComIdiomas().filter { per ->
            if (per.nome.equals("Jutsu", ignoreCase = true)) {
                false
            } else if (per.nome.equals("Alquimia", ignoreCase = true)) {
                state.compendioFantasiaAtivo || state.compendioHorrorAtivo
            } else if (state.compendioPathfinderAtivo) {
                val n = per.nome.keyify()
                n != "FOCO" && n !in com.example.swadebuilder.model.SAVAGE_PATHFINDER_BLOCKED_SKILLS
            } else {
                true
            }
        }.map { it.nome }

        val allPericias = allSkills
            .map { it.nome }
            .filter { it in requiredPericias && it in visibleSkills }
            .map { applyJutsuSkinToSkillName(it, state) }
            .distinct()

        VantFilterDialog(
            allEstagios = allEstagiosNames,
            allAtributos = allAtributos,
            allPericias = allPericias,
            current = filter,
            onChange = { state.vantFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (dialogMostrandoPoderesMisticos != null) {
        val vantOriginal = dialogMostrandoPoderesMisticos!!
        val isAnjoMysticPowers = vantOriginal.id == "poderes_misticos_anjo"
        val isDemonioMysticPowers = vantOriginal.id == "poderes_misticos_demonio"
        val isMumiaMysticPowers = vantOriginal.id == "poderes_misticos_mumia"
        val options = if (isAnjoMysticPowers) {
            listOf(
                "Arauto" to "Adivinhação, Aumentar/Reduzir Característica, Cura, Vidência",
                "Morte" to "Aumentar/Reduzir Característica (si mesmo), Deflexão, Ferir, Proteção (si mesmo)"
            )
        } else {
            if (isDemonioMysticPowers) {
                listOf(
                    "Invocador" to "Conjurar aliado, conjurar demônio, proteção, zumbi",
                    "Possessor" to "Aumentar/Reduzir Característica, fantoche, maldição, pesadelos",
                    "Sedutor" to "Aumentar/Reduzir Característica, disfarce, empatia, leitura de mente",
                    "Trapaceiro" to "Disfarce, deflexão (si mesmo), horrores ilusórios, medo"
                )
            } else if (isMumiaMysticPowers) {
                listOf(
                    "Arquiteto" to "Barreira, Detectar/Ocultar Arcano, Telecinese, Trancar/Destrancar",
                    "Régio" to "Explosão, Rajada, Rancor"
                )
            } else {
                listOf(
                    "Bárbaro" to "Força d8+",
                    "Guerreiro" to "${applyJutsuSkinToSkillName("Lutar", state)} d8+",
                    "Ladrão" to "Ladinagem d8+",
                    "Monge" to "Atletismo d8+",
                    "Paladino" to "Espírito d8+",
                    "Patrulheiro" to "Sobrevivência d8+"
                )
            }
        }

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoPoderesMisticos = null
                subOpcaoSelecionada = null
            },
            title = { Text(if (isAnjoMysticPowers) "Poderes Místicos (Anjo): Escolha o Pacote" else if (isDemonioMysticPowers) "Poderes Místicos (Demônio): Escolha o Pacote" else if (isMumiaMysticPowers) "Poderes Místicos (Múmia): Escolha o Pacote" else "Poderes Místicos: Escolha a Classe") },
            text = {
                Column {
                    Text(if (isAnjoMysticPowers) "Escolha o pacote de poderes para o anjo:" else if (isDemonioMysticPowers) "Escolha o pacote de poderes para o demônio:" else if (isMumiaMysticPowers) "Escolha o pacote de poderes para a múmia:" else "Escolha a classe para definir seus poderes e requisitos:")
                    Spacer(Modifier.size(8.dp))
                    options.forEach { (opcao, requisito) ->
                        ChoiceButtonRow(
                            label = opcao,
                            subtitle = requisito,
                            selected = (subOpcaoSelecionada == opcao)
                        ) {
                            subOpcaoSelecionada = opcao
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val choice = subOpcaoSelecionada!!

                        // Validate specific requirements
                        var reqMet = false
                        var failMsg = ""

                        // Helper to find the active instance of a skill to ensure we check the correct point pool
                        fun getSkillTotal(nameKey: String): Int {
                            val activePer = state.getBestPericia(nameKey)
                            return activePer?.let { state.rawTotal(it) } ?: 0
                        }

                        when (choice) {
                            "Bárbaro" -> {
                                val str = state.valoresAtributos["FORCA"]?.intValue ?: 4
                                if (str >= 8) reqMet = true
                                else failMsg = "Requer Força d8+"
                            }
                            "Guerreiro" -> {
                                val lut = getSkillTotal("LUTAR")
                                if (lut >= 8) reqMet = true
                                else failMsg = "Requer ${applyJutsuSkinToSkillName("Lutar", state)} d8+"
                            }
                            "Ladrão" -> {
                                val lad = getSkillTotal("LADINAGEM")
                                if (lad >= 8) reqMet = true
                                else failMsg = "Requer Ladinagem d8+"
                            }
                            "Monge" -> {
                                val atl = getSkillTotal("ATLETISMO")
                                if (atl >= 8) reqMet = true
                                else failMsg = "Requer Atletismo d8+"
                            }
                            "Paladino" -> {
                                val esp = state.valoresAtributos["ESPIRITO"]?.intValue ?: 4
                                if (esp >= 8) reqMet = true
                                else failMsg = "Requer Espírito d8+"
                            }
                            "Patrulheiro" -> {
                                val sob = getSkillTotal("SOBREVIVENCIA")
                                if (sob >= 8) reqMet = true
                                else failMsg = "Requer Sobrevivência d8+"
                            }
                            else -> reqMet = isAnjoMysticPowers || isDemonioMysticPowers || isMumiaMysticPowers
                        }

                        if (!reqMet) {
                            viewModel.logFeedback(failMsg)
                            onUserFeedback()
                            return@TextButton
                        }

                        val vantToAdd = vantOriginal.copy(choice = choice)

                        attemptPurchase(vantToAdd) {
                            dialogMostrandoPoderesMisticos = null
                            subOpcaoSelecionada = null
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoPoderesMisticos = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (dialogMostrandoAntecedente != null) {
        val vantOriginal = dialogMostrandoAntecedente!!

        val formatarRequisitos = { vant: Vantagem ->
            val attrs = vant.requisitos.atributoMin.entries.map { "${it.key} ${it.value.toDiceString()}" }
            val skills = vant.requisitos.periciaMin.entries.map {
                "${applyJutsuSkinToSkillName(it.key, state)} ${it.value.toDiceString()}"
            }
            val all = attrs + skills
            if (all.isNotEmpty()) " (${all.joinToString(", ")})" else ""
        }

        // Fantasy Override: Show list of Fantasy + Basic ABs with requirements
        val opcoesArcano: List<Pair<String, Vantagem>> = if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
            allAdvantages
                .filter {
                    val isAb = it.id.startsWith("antecedente_arcano_")
                    val isSrc = (state.compendioFantasiaAtivo && (it.origem.equals("FANTASIA", ignoreCase = true) || it.origem.equals("BASICO", ignoreCase = true))) ||
                            (state.compendioHorrorAtivo && it.origem.equals("HORROR", ignoreCase = true))
                    isAb && isSrc
                }
                .map { vant ->
                    val nameInParens = Regex("\\((.*?)\\)").find(vant.nome)?.groupValues?.get(1)
                    val baseName = nameInParens?.toFancyTitleCase()
                        ?: vant.subtipoArcano?.toFancyTitleCase()
                        ?: vant.nome.removePrefix("ANTECEDENTE ARCANO ").replace("(", "").replace(")", "").trim().toFancyTitleCase()

                    val reqs = formatarRequisitos(vant)
                    "$baseName$reqs" to vant
                }
                .sortedBy { it.first }
        } else {
            emptyList()
        }

        // Pathfinder Override: Show Magic and Miracles options
        val opcoesPathfinder: List<Pair<String, Vantagem>> = if (state.compendioPathfinderAtivo) {
            val map = mapOf(
                "Magia" to "antecedente_arcano_magia_pf",
                "Milagres" to "antecedente_arcano_milagres_pf"
            )
            map.mapNotNull { (label, id) ->
                val v = allAdvantages.firstOrNull { it.id == id }
                if (v != null) {
                    val reqs = formatarRequisitos(v)
                    "$label$reqs" to v
                } else null
            }
        } else emptyList()

        // Deadlands Override: Show Deadlands ABs
        val opcoesDeadlands: List<Pair<String, Vantagem>> = if (state.compendioDeadlandsAtivo) {
            val map = mapOf(
                "Abençoado" to "antecedente_arcano_abencoado",
                "Bruxa" to "antecedente_arcano_bruxa",
                "Cientista Louco" to "antecedente_arcano_cientista_louco",
                "Mascate" to "antecedente_arcano_mascate",
                "Mestre do Chi" to "antecedente_arcano_mestre_do_chi",
                "Voduísta" to "antecedente_arcano_vuduismo",
                "Xamã" to "antecedente_arcano_xama"
            )
            map.mapNotNull { (label, id) ->
                val v = allAdvantages.firstOrNull { it.id == id }
                if (v != null) {
                    val reqs = formatarRequisitos(v)
                    "$label$reqs" to v
                } else null
            }
        } else emptyList()

        // Cidade do Sol a Vapor: apenas os AA do cenário
        val opcoesCidadeSolVapor: List<Pair<String, Vantagem>> = if (state.compendioCidadeSolVaporAtivo) {
            val map = mapOf(
                "Magia Negra" to "aa_magia_negra",
                "Milagres" to "aa_milagres",
                "Tecnomagia" to "aa_tecnomagia"
            )
            map.mapNotNull { (label, id) ->
                val v = allAdvantages.firstOrNull { it.id == id }
                if (v != null) {
                    val reqs = formatarRequisitos(v)
                    "$label$reqs" to v
                } else null
            }
        } else emptyList()

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoAntecedente = null
                subOpcaoSelecionada = null
            },
            title = { Text("Escolha o tipo de ${vantOriginal.nome}") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (state.compendioPathfinderAtivo) {
                        opcoesPathfinder.forEach { (label, _) ->
                            ChoiceButtonRow(label, subOpcaoSelecionada == label) { subOpcaoSelecionada = label }
                        }
                    } else if (state.compendioDeadlandsAtivo) {
                        opcoesDeadlands.forEach { (label, _) ->
                            ChoiceButtonRow(label, subOpcaoSelecionada == label) { subOpcaoSelecionada = label }
                        }
                    } else if (state.compendioCidadeSolVaporAtivo) {
                        opcoesCidadeSolVapor.forEach { (label, _) ->
                            ChoiceButtonRow(label, subOpcaoSelecionada == label) { subOpcaoSelecionada = label }
                        }
                    } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
                        opcoesArcano.forEach { (label, _) ->
                            ChoiceButtonRow(label, subOpcaoSelecionada == label) { subOpcaoSelecionada = label }
                        }
                    } else {
                        vantOriginal.choiceOptions.forEach { opcao ->
                            ChoiceButtonRow(opcao, subOpcaoSelecionada == opcao) { subOpcaoSelecionada = opcao }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val handleDialogSuccess = {
                            onUserFeedback()
                            dialogMostrandoAntecedente = null
                            subOpcaoSelecionada = null
                        }

                        if (state.compendioPathfinderAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesPathfinder.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    attemptPurchase(specificEdge, handleDialogSuccess)
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioDeadlandsAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesDeadlands.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    attemptPurchase(specificEdge, handleDialogSuccess)
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioCidadeSolVaporAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesCidadeSolVapor.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    attemptPurchase(specificEdge, handleDialogSuccess)
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesArcano.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    attemptPurchase(specificEdge, handleDialogSuccess)
                                } else {
                                    // Trigger error feedback if requirements not met
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else {
                            val novaVantagem = vantOriginal.copy(
                                choice = subOpcaoSelecionada
                            )

                            if (state.podeSelecionar(novaVantagem)) {
                                attemptPurchase(novaVantagem, handleDialogSuccess)
                            }
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoAntecedente = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (dialogMostrandoPoderFavorito != null) {
        val vantOriginal = dialogMostrandoPoderFavorito!!

        // Retrieve owned power IDs
        val ownedIds by remember {
            derivedStateOf { state.poderesSelecionados.distinct().filterNotNull() }
        }

        // Retrieve already selected "Favored Powers" to exclude them
        val alreadyFavored by remember {
            derivedStateOf {
                state.vantagensSelecionadas
                    .filter { it.id == "poder_favorito" && !it.choice.isNullOrBlank() }
                    .mapNotNull { it.choice }
                    .toSet()
            }
        }

        // Resolve names and filter
        val options = remember(ownedIds, powerCache, alreadyFavored) {
            ownedIds.mapNotNull { id ->
                val p = powerCache.values.flatten().find { it.id == id }
                p?.let { it.nome.toFancyTitleCase() }
            }
            .distinct()
            .filter { it !in alreadyFavored }
            .sorted()
        }

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoPoderFavorito = null
                subOpcaoSelecionada = null
            },
            title = { Text("Poder Favorito: Escolha um Poder") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    if (isPowersLoading) {
                        com.example.swadebuilder.ui.components.LoadingState(message = "Carregando poderes...")
                    } else if (options.isEmpty()) {
                        Text("Você não possui poderes elegíveis ou já selecionou todos como favoritos.")
                    } else {
                        Text("Escolha um dos seus poderes para se tornar Favorito:")
                        Spacer(Modifier.size(8.dp))
                        options.forEach { nomePoder ->
                            ChoiceButtonRow(nomePoder, subOpcaoSelecionada == nomePoder) {
                                subOpcaoSelecionada = nomePoder
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val choice = subOpcaoSelecionada!!
                        val vantToAdd = vantOriginal.copy(choice = choice)
                        attemptPurchase(vantToAdd) {
                            dialogMostrandoPoderFavorito = null
                            subOpcaoSelecionada = null
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoPoderFavorito = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (dialogMostrandoMontaria != null) {
        val vantOriginal = dialogMostrandoMontaria!!
        val charSize = state.valorTamanho()

        val options = if (charSize < 0) {
            listOf("Javali", "Grande Felino", "Lobo", "Lobo Atroz")
        } else {
            listOf("Cavalo de Montaria", "Cavalo Élfico")
        }

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoMontaria = null
                subOpcaoSelecionada = null
            },
            title = { Text("Montaria: Escolha seu Companheiro") },
            text = {
                Column {
                    Text("Com base no seu tamanho ($charSize), escolha uma montaria:")
                    Spacer(Modifier.size(8.dp))
                    options.forEach { opcao ->
                        ChoiceButtonRow(opcao, subOpcaoSelecionada == opcao) {
                            subOpcaoSelecionada = opcao
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val choice = subOpcaoSelecionada!!
                        if (state.podeSelecionar(vantOriginal)) {
                            val vantToAdd = vantOriginal.copy(choice = choice)
                            attemptPurchase(vantToAdd) {
                                if (state.anotacoes.isNotBlank()) {
                                    state.anotacoes += "\n"
                                }
                                state.anotacoes += "• Montaria: $choice"
                                dialogMostrandoMontaria = null
                                subOpcaoSelecionada = null
                            }
                        } else {
                            // Should not happen if button was enabled
                            dialogMostrandoMontaria = null
                            subOpcaoSelecionada = null
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoMontaria = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (dialogMostrandoNovosPoderes != null) {
        val vantOriginal = dialogMostrandoNovosPoderes!!
        val activeABs = state.vantagensSelecionadas.mapNotNull { it.toArcanoKey() }.distinct()

        val options = mutableListOf<Pair<String, String>>()
        activeABs.forEach { abKey ->
            val label = "2 poderes para ${abKey.toFancyTitleCase()}"
            options.add(label to abKey)
        }
        if (activeABs.size == 2) {
            val keyCombined = "${activeABs[0]} & ${activeABs[1]}"
            val label = "1 poder para ${activeABs[0].toFancyTitleCase()} e 1 para ${activeABs[1].toFancyTitleCase()}"
            options.add(label to keyCombined)
        }

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoNovosPoderes = null
                subOpcaoSelecionada = null
            },
            title = { Text("Novos Poderes: Escolha o Destino") },
            text = {
                Column {
                    Text("Você tem múltiplos Antecedentes Arcanos. Como deseja distribuir os 2 novos poderes?")
                    Spacer(Modifier.size(8.dp))
                    options.forEach { (label, value) ->
                        ChoiceButtonRow(label, subOpcaoSelecionada == value) {
                            subOpcaoSelecionada = value
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val choice = subOpcaoSelecionada!!
                        val vantToAdd = vantOriginal.copy(choice = choice)
                        attemptPurchase(vantToAdd) {
                            dialogMostrandoNovosPoderes = null
                            subOpcaoSelecionada = null
                        }
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoNovosPoderes = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (dialogMostrandoCavaleiro != null) {
        val vantOriginal = dialogMostrandoCavaleiro!!
        AlertDialog(
            onDismissRequest = {
                dialogMostrandoCavaleiro = null
                subOpcaoSelecionada = null
            },
            title = { Text("Cavaleiro: Escolha a Armadura") },
            text = {
                Column {
                    Text("Você ganha uma armadura gratuitamente. Escolha qual:")
                    Spacer(Modifier.size(8.dp))
                    listOf("Armadura Completa", "Armadura Média").forEach { opcao ->
                        ChoiceButtonRow(opcao, subOpcaoSelecionada == opcao) {
                            subOpcaoSelecionada = opcao
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        val armor = subOpcaoSelecionada!!
                        if (state.podeSelecionar(vantOriginal)) {
                            // Custom logic for Cavaleiro bypasses attemptPurchase because it adds items
                            // But we need to spend points first
                            if (state.pontosVantagem <= 0) {
                                if (pcLivres >= 2) {
                                    state.gastarPcParaVantagem()
                                } else {
                                    viewModel.logFeedback("Pontos insuficientes.")
                                    return@TextButton
                                }
                            }

                            if (state.advantageAdvancementInProgress) {
                                viewModel.selectAdvantageForAdvancement(vantOriginal.copy(choice = armor))
                                onUserFeedback()
                            } else {
                                state.adicionarVantagemCavaleiro(vantOriginal, armor)
                                state.pontosVantagem--
                                state.rebuildAllPericiaStacks(enforcePoolLimit = true)
                                onUserFeedback()
                            }
                        }
                        dialogMostrandoCavaleiro = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogMostrandoCavaleiro = null
                        subOpcaoSelecionada = null
                    }
                ) { Text("Cancelar") }
            }
        )
    }

    if (showChoiceDialog && pendingVantagem != null) {
        state.identifyMaxedTraits()
        val vant = pendingVantagem!!
        // (Same ChoiceDialog logic as before)
        if (vant.id == "erudito") {
            val usedChoices = state.vantagensSelecionadas
                .filter { it.id == "erudito" && !it.choice.isNullOrBlank() }
                .mapNotNull { it.choice?.keyify() }
                .toSet()

            val currentLocale = LocalConfiguration.current.locales[0]
            val knowledgeOptions = state.periciasComIdiomas()
                .filter { per -> per.nome.contains("CONHECIMENTO", ignoreCase = true) }
                .map { per ->
                    val base = per.nome.substringBefore("(").trim()
                    base.lowercase(currentLocale).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(currentLocale) else it.toString()
                    }
                }
                .distinct()
                .filterNot { opt -> opt.keyify() in usedChoices }
                .sorted()

            if (knowledgeOptions.isEmpty()) {
                LaunchedEffect(vant) {
                    viewModel.logFeedback("Nenhuma perícia de Conhecimento disponível")
                    onUserFeedback()
                    showChoiceDialog = false
                    pendingVantagem = null
                }
            } else {
                    ChoiceDialog(
                        options = knowledgeOptions,
                        onConfirm = { choice ->
                            attemptPurchase(vant.copy(choice = choice)) {
                                showChoiceDialog = false
                                pendingVantagem = null
                            }
                        },
                        onDismiss = {
                            showChoiceDialog = false
                            pendingVantagem = null
                        }
                    )
            }
        } else {
            val validOptions = when {
                vant.id == "arma_predileta" -> {
                    state.periciasComIdiomas()
                        .filter { per ->
                            val nome = per.nome

                            val isAllowed =
                                nome.equals("Atirar", ignoreCase = true) ||
                                        nome.equals("Atletismo", ignoreCase = true) ||
                                        nome.equals("Lutar", ignoreCase = true)

                            val meetsMin = state.rawTotal(per) >= 8

                            isAllowed && meetsMin
                        }
                        .map { it.nome }
                }

                vant.id == "arma_predileta_aprimorada" -> {
                    state.vantagensSelecionadas
                        .mapNotNull { if (it.id == "arma_predileta") it.choice else null }
                        .distinct()
                }

                vant.id == "profissional" -> {
                    vant.choiceOptions.filter { it in state.maxedTraits }
                }

                vant.id == "especialista" -> {
                    state.vantagensSelecionadas
                        .mapNotNull { if (it.id == "profissional") it.choice else null }
                }

                vant.id == "discipulo_artes_marciais" -> {
                    state.vantagensSelecionadas
                        .filter { it.id == "estudante_artes_marciais" && !it.choice.isNullOrBlank() }
                        .mapNotNull { it.choice }
                        .ifEmpty { vant.choiceOptions }
                }

                vant.id == "mestre_artes_marciais" -> {
                    val fromDiscipulo = state.vantagensSelecionadas
                        .filter { it.id == "discipulo_artes_marciais" && !it.choice.isNullOrBlank() }
                        .mapNotNull { it.choice }
                    if (fromDiscipulo.isNotEmpty()) {
                        fromDiscipulo
                    } else {
                        state.vantagensSelecionadas
                            .filter { it.id == "estudante_artes_marciais" && !it.choice.isNullOrBlank() }
                            .mapNotNull { it.choice }
                            .ifEmpty { vant.choiceOptions }
                    }
                }

                vant.maxSelections > 0 -> {
                    val used = state.vantagensSelecionadas
                        .filter { it.id == vant.id && it.choice != null }
                        .mapNotNull { it.choice }
                    vant.choiceOptions.filter { it !in used }
                }

                else -> vant.choiceOptions
            }

            if (validOptions.isEmpty()) {
                LaunchedEffect(vant) {
                    viewModel.logFeedback("Nenhuma opção disponível para escolher")
                    onUserFeedback()
                    showChoiceDialog = false
                    pendingVantagem = null
                }
            } else {
                val displayOptions = validOptions.map { applyJutsuSkinToSkillName(it, state) }
                ChoiceDialog(
                    options = displayOptions,
                    onConfirm = { choice ->
                        val rawChoice = if (state.compendioArteDaGuerraAtivo &&
                            choice.equals("Jutsu", ignoreCase = true) &&
                            validOptions.any { it.equals("Lutar", ignoreCase = true) }
                        ) {
                            "Lutar"
                        } else {
                            choice
                        }

                        val vantToAdd = vant.copy(choice = rawChoice)
                        attemptPurchase(vantToAdd) {
                            showChoiceDialog = false
                            pendingVantagem = null
                        }
                    },
                    onDismiss = {
                        showChoiceDialog = false
                        pendingVantagem = null
                    }
                )
            }
        }
    }
}

// Extracted reusable item component
@Composable
private fun VantagemItem(
    vant: Vantagem,
    state: CriadorState,
    allEstagios: List<Estagio>,
    locked: Boolean,
    allowLongTexts: Boolean,
    showOfficialNames: Boolean,
    idParaNome: Map<String, String>,
    detalhesExpandidos: MutableMap<String, Boolean>,
    protagonistaSlotCategoria: String?,
    pcLivres: Int,
    onSelect: () -> Unit,
    onError: (String) -> Unit
) {
    val themeData = LocalAppThemeData.current

    val reqList = buildList {
        allEstagios.firstOrNull {
            it.nome.equals(vant.requisitos.estagio, true)
        }?.let { add(it.nome) }

        vant.requisitos.atributoMin.forEach { (a, m) ->
            add("$a ${m.toDiceString()}")
        }
        vant.requisitos.periciaMin.forEach { (p, m) ->
            add("${applyJutsuSkinToSkillName(p, state)} ${m.toDiceString()}")
        }

        if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
            add(
                vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                    "${it.key} ${it.value.toDiceString()}"
                }
            )
        }

        vant.requisitos.vantagensPrevias.forEach { prevId ->
            val legivel = idParaNome[prevId]
                ?: prevId.replace('_', ' ').replace('-', ' ').toFancyTitleCase()
            add("Pré-requisito: $legivel")
        }

        if (vant.requisitos.observacoes.isNotBlank()) {
            add(vant.requisitos.observacoes)
        }
        if (vant.nome.trim().removeSuffix(":").keyify() == "profissional") {
            add(
                "Traço no teto máximo: escolha entre " +
                        state.maxedTraits.joinToString()
            )
        }
    }

    val jaTem = state.vantagensSelecionadas.any { it.id == vant.id }
    val isAuto = jaTem && state.isVantagemAutomatica(vant)
    val requisitosOk = state.podeSelecionar(vant)
    // PROMPT 4: Specific logic for fantasy class validation
    val bloqueioClasse = if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant)) {
        "Requer Multiclasse"
    } else null

    val statusText = when {
        isAuto -> "Automática / Racial"
        jaTem -> "Já selecionada"
        bloqueioClasse != null -> bloqueioClasse
        requisitosOk -> "Requisitos OK"
        else -> "Requisitos pendentes"
    }
    val statusColor = when {
        isAuto -> MaterialTheme.colorScheme.onSurfaceVariant
        jaTem -> MaterialTheme.colorScheme.tertiary
        bloqueioClasse != null -> MaterialTheme.colorScheme.error
        requisitosOk -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !locked && !isAuto) {
                if (!locked && !isAuto) {
                    val conflitoMsg = state.mensagemConflitoParaVantagem(vant)

                    val isPathfinderFree = state.pathfinderSlotAvailable && state.isPathfinderEligible(vant)
                    val isProtagonistaFree = state.protagonistaSlotAvailable && state.isProtagonistaEligible(vant)
                    val isFreeAdaptavel = state.hasFreeAdaptavelSlotNow(debugSource = "VantagensSection:itemClick:${vant.id}") &&
                            (vant.requisitos.estagio.isBlank() || vant.requisitos.estagio.equals("Novato", ignoreCase = true)) &&
                            !state.isVantagemAutomatica(vant)

                    val hasBP = pcLivres >= 2
                    val canAfford = state.pontosVantagem > 0 || hasBP

                    when {
                        !state.modoLivre && !isPathfinderFree && !isProtagonistaFree && !isFreeAdaptavel && !canAfford -> onError("Sem PV disponível")
                        // PROMPT 4: Check class blocking specifically for error message
                        !state.modoLivre && state.vantagensSelecionadas.classeExclusivaBloqueada(vant) -> onError("Requer a vantagem Multiclasse para possuir duas classes")
                        !state.modoLivre && conflitoMsg != null -> onError(conflitoMsg)
                        !state.modoLivre && !state.podeSelecionar(vant) -> onError("Faltam requisitos para '${vant.nomeExibicao}'")
                        else -> onSelect()
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAuto -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                jaTem -> MaterialTheme.colorScheme.tertiaryContainer
                requisitosOk && bloqueioClasse == null -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        border = themeData.cardBorderColor?.let { androidx.compose.foundation.BorderStroke(1.dp, it) }
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    val isCustom = vant.origem.equals("CUSTOM", ignoreCase = true) || vant.id.startsWith("custom:") || vant.id.startsWith("fanmade:")
                    val customBadge = if (isCustom) " ⓒ" else ""
                    Text(
                        if (showOfficialNames && !vant.originalName.isNullOrBlank()) "${vant.originalName!!.toFancyTitleCase()}$customBadge" else "${vant.nomeExibicao.toFancyTitleCase()}$customBadge",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // PASSO 3: Origem em linha secundária se != BASICO
                }

                Text(
                    statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor
                )
            }

            Spacer(Modifier.size(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (state.vantagemAdaptavelSelecionadaId == vant.id) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Vantagem bônus de ${state.getAdaptavelLabel()}") }
                    )
                }

                if (state.pathfinderSlotAvailable && state.isPathfinderEligible(vant)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Vantagem de Classe/Profissional/Antecedente") }
                    )
                } else if (state.protagonistaSlotAvailable && state.isProtagonistaEligible(vant)) {
                    val slotSuffix = protagonistaSlotCategoria?.let { " ($it)" } ?: ""
                    AssistChip(
                        onClick = {},
                        label = { Text("Vantagem bônus de Protagonista$slotSuffix") }
                    )
                }
                if (vant.descricao.isNotBlank() && vant.vinculadoPericia &&
                    vant.id !in setOf("arma_predileta", "arma_predileta_aprimorada")
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Opções especiais") }
                    )
                }
            }

            if (reqList.isNotEmpty()) {
                Spacer(Modifier.size(4.dp))
                Text(
                    "Requisitos:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                reqList.forEach { req ->
                    Text(
                        "• $req",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (requisitosOk || jaTem) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            val canShowDetails = allowLongTexts && vant.descricao.isNotBlank()
            if (canShowDetails) {
                Spacer(Modifier.size(8.dp))
                TextButton(
                    onClick = {
                        val current = detalhesExpandidos[vant.id] ?: false
                        detalhesExpandidos[vant.id] = !current
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (detalhesExpandidos[vant.id] == true) "Ocultar detalhes" else "Ver detalhes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                AnimatedVisibility(visible = detalhesExpandidos[vant.id] == true) {
                    val rawDescription = if (showOfficialNames && !vant.originalDescription.isNullOrBlank()) {
                        vant.originalDescription!!.trim()
                    } else {
                        vant.descricao.trim()
                    }
                    Text(
                        text = applyJutsuSkinToText(rawDescription, state),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}


private val lutarWordRegex = Regex("\\bLutar\\b")

private fun applyJutsuSkinToSkillName(name: String, state: CriadorState): String {
    return if (state.compendioArteDaGuerraAtivo && name.equals("Lutar", ignoreCase = true)) {
        "Jutsu"
    } else {
        name
    }
}

private fun applyJutsuSkinToText(text: String, state: CriadorState): String {
    return if (state.compendioArteDaGuerraAtivo) {
        text.replace(lutarWordRegex, "Jutsu")
    } else {
        text
    }
}
