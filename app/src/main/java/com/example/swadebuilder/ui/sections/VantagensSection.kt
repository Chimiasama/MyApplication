package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.mapaPericias
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.model.isVantagemVisible
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.ui.components.CollapsibleSection
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.PbLegacyActions
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.ui.theme.LocalAppThemeData
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VantagensContent(
    state: CriadorState,
    multiplosAAHabilitados: Boolean,
    viewModel: CriadorViewModel = viewModel(),
    onUserFeedback: () -> Unit = {}
) {
    val context = LocalContext.current
    val powerCache: Map<String, List<Poder>> by androidx.compose.runtime.produceState(initialValue = emptyMap()) {
        withContext(Dispatchers.IO) {
            val origins = listOf("basico", "fantasia", "scifi", "horror", "deadlands", "pathfinder", "crystal", "sol_vapor", "wiseguys", "adg")
            val map = mutableMapOf<String, List<Poder>>()

            origins.forEach { org ->
                val list = runCatching { context.loadJsonAsset<List<Poder>>("${org}_poderes.json") }.getOrElse { emptyList() }
                map[org.uppercase()] = list
            }

            val superBaseList = runCatching { context.loadJsonAsset<List<Poder>>("super_poderes_base.json") }.getOrElse { emptyList() }
            map["SUPER"] = superBaseList

            val adgChiList = runCatching { context.loadJsonAsset<List<Poder>>("adg_tecnicas_chi.json") }.getOrElse { emptyList() }
            val adgStandardList = map["ADG"] ?: emptyList()
            val combinedAdg = (adgStandardList + adgChiList).distinctBy { it.id }

            map["ADG"] = combinedAdg
            map["ARTE DA GUERRA"] = combinedAdg

            value = map
        }
    }

    val listaVantagensGlobal = listaVantagens
    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo

    val listaVantagens: List<Vantagem> =
        remember(multiplosAAHabilitados, listaVantagensGlobal) {
            listaVantagensGlobal
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
            .sortedWith(compareBy({ it.categoria }, { it.nomeExibicao }))
    }

    val idParaNome = remember(listaVantagens) {
        listaVantagens.associate { it.id to it.nomeExibicao.toSentenceCase() }
    }

    // --- Search & Filter State ---
    val searchQuery = state.vantSearchQuery
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
    val usePbWalletRedesign = booleanResource(com.example.swadebuilder.R.bool.enable_pb_wallet_redesign)
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

    val filteredListGlobal = remember(listaVantagensAtivas, state.modoSupers, hasProfissional, filter, multiplosAAHabilitados) {
        listaVantagensAtivas.filter { vant ->
            // Professional/Specialist Dependency
            if (vant.id == "especialista" && !hasProfissional) return@filter false

            // Advanced Filters
            if (!filter.isEmpty()) {
                val vantOrigem = vant.origem.ifBlank { "BASICO" }.uppercase()
                if (filter.origens.isNotEmpty() && vantOrigem !in filter.origens) return@filter false
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

    val protagonistaSlotCategoria = when (state.protagonistaRollVantagem) {
        1 -> "Chi"
        2 -> "Estranhas"
        3 -> "Antecedente"
        4 -> "Combate"
        5 -> "Sociais"
        else -> null
    }

    // Reusable Header Content
    val headerContent: @Composable () -> Unit = {
        Column {
            SectionHeader(
                onHelpClick = null,
                centerText = "Pontos de Vantagem: ${state.pontosVantagem}",
                onListaCompletaClick = null,
                listaCompletaText = ""
            )

            Spacer(Modifier.size(4.dp))

            if (!state.emProgresso) {
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

                if (usePbWalletRedesign) {
                    PbWalletBanner(
                        pcTotal = pcTotal,
                        pcLivres = pcLivres,
                        spendLabel = "Usar PB em Vantagens",
                        refundLabel = "Desfazer uso de PB",
                        spendEnabled = !locked && pcLivres >= 2,
                        refundEnabled = !locked && pvUsados > 0,
                        onSpend = { state.gastarPcParaVantagem() },
                        onRefund = { state.devolverPcDeVantagem() }
                    )
                } else {
                    PbLegacyActions(
                        spendLabel = "Usar PB em Vantagens",
                        refundLabel = "Desfazer uso de PB",
                        spendEnabled = !locked && pcLivres >= 2,
                        refundEnabled = !locked && pvUsados > 0,
                        onSpend = { state.gastarPcParaVantagem() },
                        onRefund = { state.devolverPcDeVantagem() }
                    )
                }
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
                                val label = if (cat.name == "LIDERANCA") "Liderança" else cat.name.toSentenceCase()
                                Text(label)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.size(8.dp))

            // Selected Advantages Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                if (state.regraMultiplosIdiomas) {
                    AssistChip(
                        onClick = { },
                        enabled = false,
                        label = { Text("LINGUISTA (Regra de Ambientação)") }
                    )
                }

                state.vantagensSelecionadas.forEachIndexed { index, vant ->
                    val isRacialFree =
                        vant.nomeExibicao.keyify() in state.vantagensAutomaticas.map { it.keyify() } ||
                                vant.nome.keyify() in state.vantagensAutomaticas.map { it.keyify() }
                    val isTropoAutomatic = state.vantagensAutomaticasDoTropo.contains(vant.id)
                    val requiredByAnother = state.vantagensSelecionadas.any { other ->
                        other != vant && other.requisitos.vantagensPrevias.any { reqId ->
                            reqId == vant.id
                        }
                    }

                    val isFromSuperPoder = state.vantagensDePoder.contains(vant.id)
                    val isSuperpoderesLocked = state.modoSupers && vant.id == "superpoderes"
                    val isCrystalHeartLocked = state.compendioCrystalHeartAtivo && vant.id == "aa_agente_syn"
                    val isCelestialAAMilagres = state.ancestralidade == "CELESTIAIS" &&
                            vant.id == "antecedente_arcano_milagres"
                    val isProtagonistaAutomatic = state.vantagensAutomaticasDoProtagonista.contains(vant.id)

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
                            !isRacialFree &&
                            !isTropoAutomatic &&
                            !isProtagonistaAutomatic &&
                            !requiredByAnother &&
                            !isFromSuperPoder &&
                            !isSuperpoderesLocked &&
                            !isCrystalHeartLocked

                    val canRemove =
                        baseRemovable && !(state.emProgresso && vant.id == "novos_poderes")
                                && !isCelestialAAMilagres

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

                            if (arcKey != null && arcKey == state.arcanoEmCompraViaXpKey) {
                                state.limparCompraArcanoViaXp(restaurarSnapshot = true)
                                if (state.advantageForCurrentAdvancement == vant.id) {
                                    state.advantageForCurrentAdvancement = null
                                }
                            }
                        },
                        enabled = canRemove,
                        label = {
                            val labelText = when {
                                vant.id == "arma_predileta" &&
                                    state.compendioArteDaGuerraAtivo &&
                                    state.tropoSelecionado?.id == "tropo_youxia" &&
                                    !state.youxiaJutsuSelecionado.isNullOrBlank() ->
                                    "${vant.nomeExibicao} (${state.youxiaJutsuSelecionado})"
                                else -> vant.choice?.let { "${vant.nomeExibicao} ($it)" } ?: vant.nomeExibicao
                            }
                            val finalText = if (isCelestialAAMilagresDesabilitado) {
                                "$labelText (DESABILITADO)"
                            } else {
                                labelText
                            }
                            Text(finalText)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = if (canRemove) "Remover" else ""
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.size(16.dp))
        }
    }

    if (browsingMode) {
        // Browse Mode (Accordions) - NOW OPTIMIZED WITH LAZYCOLUMN
        val categoriasBy = remember(filteredListGlobal) {
            filteredListGlobal.groupBy { it.categoria }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item { headerContent() }

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
                            title = if (cat.name == "LIDERANCA") "Liderança" else cat.name.toSentenceCase(),
                            expanded = expanded,
                            onToggle = { expandedMap[cat] = !expanded },
                            onToggleFeedback = onUserFeedback
                        ) {} // Empty content, we use CollapsibleSection just for header rendering
                        // Note: CollapsibleSection has a Column wrapper. If we pass empty content, it just renders the header row.

                        // We need a Spacer if not expanded to match original look?
                        // Original had `Spacer(Modifier.size(8.dp))` AFTER CollapsibleSection.
                        // We can add it as padding or separate item.
                        // Let's add it to the item.
                        Spacer(Modifier.size(8.dp))
                    }
                }

                // Category Content (Only if expanded)
                if (expanded) {
                    items(lista, key = { it.id }, contentType = { "vantagem_item" }) { vant ->
                         // We need a wrapper to provide the padding used in the original Column
                         // Original: padding(start = 8.dp, bottom = 8.dp)
                         Column(modifier = Modifier.padding(start = 8.dp, bottom = 0.dp)) {
                             VantagemItem(
                                 vant = vant,
                                 state = state,
                                 locked = locked,
                                 allowLongTexts = allowLongTexts,
                                 showOfficialNames = showOfficialNames,
                                 idParaNome = idParaNome,
                                 detalhesExpandidos = detalhesExpandidos,
                                 protagonistaSlotCategoria = protagonistaSlotCategoria,
                                 onSelect = {
                                    if (vant.vinculadoPericia) {
                                        pendingVantagem = vant
                                        showChoiceDialog = true
                                    } else if (vant.id == "antecedente_arcano") {
                                        dialogMostrandoAntecedente = vant
                                    } else if (vant.id == "poderes_misticos") {
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
                                            // Single AB: proceed normally (likely assigning default if blank or single choice)
                                            // If standard purchase flow doesn't auto-assign, we might need to set choice.
                                            // But for now, let's assume standard behavior works for single AB.
                                            // Actually, if single AB, let's explicitly set it if not set?
                                            // Legacy behavior handled blank as "The AB".
                                            // Let's stick to standard flow.
                                            if (state.advantageAdvancementInProgress) {
                                                viewModel.selectAdvantageForAdvancement(vant)
                                                onUserFeedback()
                                                viewModel.logFeedback("Vantagem ${vant.nome} adicionada.")
                                            } else {
                                                state.comprarVantagem(vant) { msg ->
                                                    viewModel.logFeedback(msg)
                                                    onUserFeedback()
                                                }
                                            }
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
                                        if (state.advantageAdvancementInProgress) {
                                            viewModel.selectAdvantageForAdvancement(vant)
                                            onUserFeedback()
                                            viewModel.logFeedback("Vantagem ${vant.nome} adicionada.")
                                        } else {
                                            state.comprarVantagem(vant) { msg ->
                                                viewModel.logFeedback(msg)
                                                onUserFeedback()
                                            }
                                        }
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
        // Search Mode (Flat List) - Preserving Fixed Header
        Column(modifier = Modifier.fillMaxWidth()) {
            headerContent()

            val flatList = remember(filteredListGlobal, selectedCategories, searchQuery) {
                filteredListGlobal.filter { vant ->
                    if (selectedCategories.isNotEmpty() && vant.categoria !in selectedCategories) return@filter false
                    if (isSearching) {
                        val q = searchQuery.semAcentos().lowercase()
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
                     item(key = "empty_list", contentType = "message") { Text("Nenhuma vantagem encontrada.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(flatList, key = { it.id }, contentType = { "vantagem_item" }) { vant ->
                         VantagemItem(
                             vant = vant,
                             state = state,
                             locked = locked,
                             allowLongTexts = allowLongTexts,
                             showOfficialNames = showOfficialNames,
                             idParaNome = idParaNome,
                             detalhesExpandidos = detalhesExpandidos,
                             protagonistaSlotCategoria = protagonistaSlotCategoria,
                             onSelect = {
                                if (vant.vinculadoPericia) {
                                    pendingVantagem = vant
                                    showChoiceDialog = true
                                } else if (vant.id == "antecedente_arcano") {
                                    dialogMostrandoAntecedente = vant
                                } else if (vant.id == "poderes_misticos") {
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
                                        if (state.advantageAdvancementInProgress) {
                                            viewModel.selectAdvantageForAdvancement(vant)
                                            onUserFeedback()
                                            viewModel.logFeedback("Vantagem ${vant.nome} adicionada.")
                                        } else {
                                            state.comprarVantagem(vant) { msg ->
                                                viewModel.logFeedback(msg)
                                                onUserFeedback()
                                            }
                                        }
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
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(vant)
                                        onUserFeedback()
                                        viewModel.logFeedback("Vantagem ${vant.nome} adicionada.")
                                    } else {
                                        state.comprarVantagem(vant) { msg ->
                                            viewModel.logFeedback(msg)
                                            onUserFeedback()
                                        }
                                    }
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
        val allEstagios = listaDeEstagios.map { it.nome }
        val allAtributos = mapaAtributosDisplay.values.toList()
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

        val allPericias = listaPericias
            .map { it.nome }
            .filter { it in requiredPericias && it in visibleSkills }
            .map { applyJutsuSkinToSkillName(it, state) }
            .distinct()

        VantFilterDialog(
            allEstagios = allEstagios,
            allAtributos = allAtributos,
            allPericias = allPericias,
            current = filter,
            onChange = { state.vantFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (dialogMostrandoPoderesMisticos != null) {
        val vantOriginal = dialogMostrandoPoderesMisticos!!
        val options = listOf(
            "Bárbaro" to "Força d8+",
            "Guerreiro" to "${applyJutsuSkinToSkillName("Lutar", state)} d8+",
            "Ladrão" to "Ladinagem d8+",
            "Monge" to "Atletismo d8+",
            "Paladino" to "Espírito d8+",
            "Patrulheiro" to "Sobrevivência d8+"
        )

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoPoderesMisticos = null
                subOpcaoSelecionada = null
            },
            title = { Text("Poderes Místicos: Escolha a Classe") },
            text = {
                Column {
                    Text("Escolha a classe para definir seus poderes e requisitos:")
                    Spacer(Modifier.size(8.dp))
                    options.forEach { (opcao, requisito) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { subOpcaoSelecionada = opcao }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (subOpcaoSelecionada == opcao),
                                onClick = { subOpcaoSelecionada = opcao }
                            )
                            Spacer(Modifier.size(8.dp))
                            Column {
                                Text(opcao, fontWeight = FontWeight.Bold)
                                Text(requisito, style = MaterialTheme.typography.bodySmall)
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

                        // Validate specific requirements
                        var reqMet = false
                        var failMsg = ""

                        // Helper to find the active instance of a skill to ensure we check the correct point pool
                        fun getSkillTotal(nameKey: String): Int {
                            val activePer = state.periciasComIdiomas().firstOrNull { it.nome.keyify() == nameKey }
                                ?: mapaPericias[nameKey]
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
                            else -> reqMet = true
                        }

                        if (!reqMet) {
                            viewModel.logFeedback(failMsg)
                            onUserFeedback()
                            return@TextButton
                        }

                        val vantToAdd = vantOriginal.copy(choice = choice)

                        if (state.advantageAdvancementInProgress) {
                            viewModel.selectAdvantageForAdvancement(vantToAdd)
                            onUserFeedback()
                            viewModel.logFeedback("Vantagem ${vantToAdd.nome} adicionada ($choice).")
                        } else {
                            state.comprarVantagem(vantToAdd) { msg ->
                                viewModel.logFeedback(msg)
                                onUserFeedback()
                            }
                        }

                        dialogMostrandoPoderesMisticos = null
                        subOpcaoSelecionada = null
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
            val attrs = vant.requisitos.atributoMin.entries.map { "${it.key} d${it.value}" }
            val skills = vant.requisitos.periciaMin.entries.map {
                "${applyJutsuSkinToSkillName(it.key, state)} d${it.value}"
            }
            val all = attrs + skills
            if (all.isNotEmpty()) " (${all.joinToString(", ")})" else ""
        }

        // Fantasy Override: Show list of Fantasy + Basic ABs with requirements
        val opcoesArcano: List<Pair<String, Vantagem>> = if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
            listaVantagens
                .filter {
                    val isAb = it.id.startsWith("antecedente_arcano_")
                    val isSrc = (state.compendioFantasiaAtivo && (it.origem.equals("FANTASIA", ignoreCase = true) || it.origem.equals("BASICO", ignoreCase = true))) ||
                            (state.compendioHorrorAtivo && it.origem.equals("HORROR", ignoreCase = true))
                    isAb && isSrc
                }
                .map { vant ->
                    val nameInParens = Regex("\\((.*?)\\)").find(vant.nome)?.groupValues?.get(1)
                    val baseName = nameInParens?.toSentenceCase()
                        ?: vant.subtipoArcano?.toSentenceCase()
                        ?: vant.nome.removePrefix("ANTECEDENTE ARCANO ").replace("(", "").replace(")", "").trim().toSentenceCase()

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
                val v = listaVantagens.firstOrNull { it.id == id }
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
                val v = listaVantagens.firstOrNull { it.id == id }
                if (v != null) {
                    val reqs = formatarRequisitos(v)
                    "$label$reqs" to v
                } else null
            }
        } else emptyList()

        // Cidade do Sol a Vapor: apenas os AA do cenário
        val opcoesCidadeSolVapor: List<Pair<String, Vantagem>> = if (state.compendioCidadeSolVaporAtivo) {
            val map = mapOf(
                "Demônio" to "aa_demonio",
                "Anjo" to "aa_anjo",
                "Magia das Trevas" to "aa_magia_das_trevas",
                "Magia Negra" to "aa_magia_negra",
                "Milagres" to "aa_milagres",
                "Tecnomagia" to "aa_tecnomagia"
            )
            map.mapNotNull { (label, id) ->
                val v = listaVantagens.firstOrNull { it.id == id }
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
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = label }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == label),
                                    onClick = { subOpcaoSelecionada = label }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(label)
                            }
                        }
                    } else if (state.compendioDeadlandsAtivo) {
                        opcoesDeadlands.forEach { (label, _) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = label }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == label),
                                    onClick = { subOpcaoSelecionada = label }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(label)
                            }
                        }
                    } else if (state.compendioCidadeSolVaporAtivo) {
                        opcoesCidadeSolVapor.forEach { (label, _) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = label }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == label),
                                    onClick = { subOpcaoSelecionada = label }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(label)
                            }
                        }
                    } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
                        opcoesArcano.forEach { (label, _) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = label }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == label),
                                    onClick = { subOpcaoSelecionada = label }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(label)
                            }
                        }
                    } else {
                        vantOriginal.choiceOptions.forEach { opcao ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = opcao }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == opcao),
                                    onClick = { subOpcaoSelecionada = opcao }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(opcao)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (subOpcaoSelecionada != null),
                    onClick = {
                        if (state.compendioPathfinderAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesPathfinder.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(specificEdge)
                                    } else {
                                        state.comprarVantagem(specificEdge) { msg ->
                                            viewModel.logFeedback(msg)
                                            onUserFeedback()
                                        }
                                    }
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioDeadlandsAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesDeadlands.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(specificEdge)
                                    } else {
                                        state.comprarVantagem(specificEdge) { msg ->
                                            viewModel.logFeedback(msg)
                                            onUserFeedback()
                                        }
                                    }
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioCidadeSolVaporAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesCidadeSolVapor.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(specificEdge)
                                    } else {
                                        state.comprarVantagem(specificEdge) { msg ->
                                            viewModel.logFeedback(msg)
                                            onUserFeedback()
                                        }
                                    }
                                } else {
                                    viewModel.logFeedback("Requisitos não atendidos para ${specificEdge.nome}")
                                }
                            }
                        } else if (state.compendioFantasiaAtivo || state.compendioHorrorAtivo) {
                            val choiceLabel = subOpcaoSelecionada!!
                            val specificEdge = opcoesArcano.firstOrNull { it.first == choiceLabel }?.second

                            if (specificEdge != null) {
                                if (state.podeSelecionar(specificEdge)) {
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(specificEdge)
                                    } else {
                                        state.comprarVantagem(specificEdge) { msg ->
                                            viewModel.logFeedback(msg)
                                            onUserFeedback()
                                        }
                                    }
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
                                if (state.advantageAdvancementInProgress) {
                                    viewModel.selectAdvantageForAdvancement(novaVantagem)
                                } else {
                                    state.comprarVantagem(novaVantagem) { msg ->
                                        viewModel.logFeedback(msg)
                                        onUserFeedback()
                                    }
                                }
                            }
                        }
                        onUserFeedback()
                        dialogMostrandoAntecedente = null
                        subOpcaoSelecionada = null
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
                p?.let { it.nome.toSentenceCase() }
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
                    if (options.isEmpty()) {
                        Text("Você não possui poderes elegíveis ou já selecionou todos como favoritos.")
                    } else {
                        Text("Escolha um dos seus poderes para se tornar Favorito:")
                        Spacer(Modifier.size(8.dp))
                        options.forEach { nomePoder ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { subOpcaoSelecionada = nomePoder }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (subOpcaoSelecionada == nomePoder),
                                    onClick = { subOpcaoSelecionada = nomePoder }
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(nomePoder)
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

                        if (state.advantageAdvancementInProgress) {
                            viewModel.selectAdvantageForAdvancement(vantToAdd)
                            onUserFeedback()
                            viewModel.logFeedback("Vantagem ${vantToAdd.nome} adicionada ($choice).")
                        } else {
                            state.comprarVantagem(vantToAdd) { msg ->
                                viewModel.logFeedback(msg)
                                onUserFeedback()
                            }
                        }

                        dialogMostrandoPoderFavorito = null
                        subOpcaoSelecionada = null
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
                        Row(
                            Modifier
                            .fillMaxWidth()
                            .clickable { subOpcaoSelecionada = opcao }
                            .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (subOpcaoSelecionada == opcao),
                                onClick = { subOpcaoSelecionada = opcao }
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(opcao)
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

                            if (state.advantageAdvancementInProgress) {
                                viewModel.selectAdvantageForAdvancement(vantToAdd)
                                onUserFeedback()
                            } else {
                                state.comprarVantagem(vantToAdd) { msg ->
                                    viewModel.logFeedback(msg)
                                    onUserFeedback()
                                }
                            }

                            if (state.anotacoes.isNotBlank()) {
                                state.anotacoes += "\n"
                            }
                            state.anotacoes += "• Montaria: $choice"

                            onUserFeedback()
                        }
                        dialogMostrandoMontaria = null
                        subOpcaoSelecionada = null
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
            val label = "2 poderes para ${abKey.toSentenceCase()}"
            options.add(label to abKey)
        }
        if (activeABs.size == 2) {
            val keyCombined = "${activeABs[0]} & ${activeABs[1]}"
            val label = "1 poder para ${activeABs[0].toSentenceCase()} e 1 para ${activeABs[1].toSentenceCase()}"
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
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { subOpcaoSelecionada = value }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (subOpcaoSelecionada == value),
                                onClick = { subOpcaoSelecionada = value }
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
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

                        if (state.advantageAdvancementInProgress) {
                            viewModel.selectAdvantageForAdvancement(vantToAdd)
                            onUserFeedback()
                            viewModel.logFeedback("Vantagem ${vantToAdd.nome} adicionada ($choice).")
                        } else {
                            state.comprarVantagem(vantToAdd) { msg ->
                                viewModel.logFeedback(msg)
                                onUserFeedback()
                            }
                        }

                        dialogMostrandoNovosPoderes = null
                        subOpcaoSelecionada = null
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
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { subOpcaoSelecionada = opcao }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (subOpcaoSelecionada == opcao),
                                onClick = { subOpcaoSelecionada = opcao }
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(opcao)
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
                            if (state.advantageAdvancementInProgress) {
                                // Pass choice to ViewModel
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

            val knowledgeOptions = state.periciasComIdiomas()
                .filter { per -> per.nome.contains("CONHECIMENTO", ignoreCase = true) }
                .map { per ->
                    val base = per.nome.substringBefore("(").trim()
                    base.lowercase(Locale.getDefault()).replaceFirstChar {
                        it.titlecase(Locale.getDefault())
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
                        state.comprarVantagem(vant.copy(choice = choice)) { msg ->
                            viewModel.logFeedback(msg)
                            onUserFeedback()
                        }
                        showChoiceDialog = false
                        pendingVantagem = null
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
                        .filter { it.id == "arma_predileta" && it.choice != null }
                        .mapNotNull { it.choice }
                        .distinct()
                }

                vant.id == "profissional" -> {
                    vant.choiceOptions.filter { it in state.maxedTraits }
                }

                vant.id == "especialista" -> {
                    state.vantagensSelecionadas
                        .filter { it.id == "profissional" && it.choice != null }
                        .mapNotNull { it.choice }
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

                        if (state.advantageAdvancementInProgress) {
                            viewModel.selectAdvantageForAdvancement(vantToAdd)
                            onUserFeedback()
                            viewModel.logFeedback("Vantagem ${vantToAdd.nome} adicionada ($rawChoice).")
                        } else {
                            state.comprarVantagem(vantToAdd) { msg ->
                                viewModel.logFeedback(msg)
                                onUserFeedback()
                            }
                        }
                        showChoiceDialog = false
                        pendingVantagem = null
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
    locked: Boolean,
    allowLongTexts: Boolean,
    showOfficialNames: Boolean,
    idParaNome: Map<String, String>,
    detalhesExpandidos: MutableMap<String, Boolean>,
    protagonistaSlotCategoria: String?,
    onSelect: () -> Unit,
    onError: (String) -> Unit
) {
    val themeData = LocalAppThemeData.current

    val reqList = buildList {
        listaDeEstagios.firstOrNull {
            it.nome.equals(vant.requisitos.estagio, true)
        }?.let { add(it.nome) }

        vant.requisitos.atributoMin.forEach { (a, m) ->
            add("$a d$m")
        }
        vant.requisitos.periciaMin.forEach { (p, m) ->
            add("${applyJutsuSkinToSkillName(p, state)} d$m")
        }

        if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
            add(
                vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                    "${applyJutsuSkinToSkillName(it.key, state)} d${it.value}"
                }
            )
        }

        vant.requisitos.vantagensPrevias.forEach { prevId ->
            val legivel = idParaNome[prevId]
                ?: prevId.replace('_', ' ').replace('-', ' ').toSentenceCase()
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
    val requisitosOk = state.podeSelecionar(vant)
    // PROMPT 4: Specific logic for fantasy class validation
    val bloqueioClasse = if (state.vantagensSelecionadas.classeExclusivaBloqueada(vant)) {
        "Requer Multiclasse"
    } else null

    val statusText = when {
        jaTem -> "Já selecionada"
        bloqueioClasse != null -> bloqueioClasse
        requisitosOk -> "Requisitos OK"
        else -> "Requisitos pendentes"
    }
    val statusColor = when {
        jaTem -> MaterialTheme.colorScheme.tertiary
        bloqueioClasse != null -> MaterialTheme.colorScheme.error
        requisitosOk -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !locked) {
                if (!locked) {
                    val conflitoMsg = state.mensagemConflitoParaVantagem(vant)

                    val isPathfinderFree = state.pathfinderSlotAvailable && state.isPathfinderEligible(vant)
                    val isProtagonistaFree = state.protagonistaSlotAvailable && state.isProtagonistaEligible(vant)

                    when {
                        !isPathfinderFree && !isProtagonistaFree && state.pontosVantagem <= 0 -> onError("Sem PV disponível")
                        // PROMPT 4: Check class blocking specifically for error message
                        state.vantagensSelecionadas.classeExclusivaBloqueada(vant) -> onError("Requer a vantagem Multiclasse para possuir duas classes")
                        conflitoMsg != null -> onError(conflitoMsg)
                            !state.podeSelecionar(vant) -> onError("Faltam requisitos para '${vant.nomeExibicao}'")
                        else -> onSelect()
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
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
                    Text(
                        if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName!!.toSentenceCase() else vant.nomeExibicao.toSentenceCase(),
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
                if (state.pathfinderSlotAvailable && state.isPathfinderEligible(vant)) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Slot de Classe/Profissional/Antecedente") }
                    )
                } else if (state.protagonistaSlotAvailable && state.isProtagonistaEligible(vant)) {
                    val slotSuffix = protagonistaSlotCategoria?.let { " ($it)" } ?: ""
                    AssistChip(
                        onClick = {},
                        label = { Text("Slot de Protagonista$slotSuffix") }
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
                        vant.originalDescription.trim()
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

private fun Vantagem.isBrutamontes(): Boolean {
    val idKey = id.keyify()
    val nameKey = nome.keyify()
    return idKey == "BRUTAMONTES" || idKey == "BRAWNY" || nameKey == "BRUTAMONTES" || nameKey == "BRAWNY"
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
