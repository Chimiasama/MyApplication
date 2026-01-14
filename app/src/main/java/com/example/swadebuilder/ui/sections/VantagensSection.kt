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
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.VantFilter
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
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
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
        state.compendioBuscatrilhaAtivo,
        state.compendioDeadlandsAtivo,
        state.compendioCrystalHeartAtivo,
        state.compendioArteDaGuerraAtivo,
        state.compendioCidadeSolVaporAtivo,
        state.compendioWiseguysAtivo
    ) {
        listaVantagens.filter { vant ->
            val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase()
            val isBasico = origemNorm == "BASICO"
            val isSuper = origemNorm == "SUPER"
            val isFantasia = origemNorm == "FANTASIA"
            val isHorror = origemNorm == "HORROR"
            val isBuscatrilha = origemNorm == "FANTASIABUSCATRILHA"
            val isDeadlands = origemNorm == "OESTE_ESTRANHO"
            val isAdg = origemNorm == "ARTE_DA_GUERRA"
            val isCidadeSolVapor = origemNorm == "CIDADE_SOL_VAPOR"
            val isWiseguys = origemNorm == "WISEGUYS"
            val isCrystalHeart = origemNorm == "CRYSTAL_HEART"

            if (state.compendioCrystalHeartAtivo) {
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
                    return@filter false
                }
            }

            if (state.compendioBuscatrilhaAtivo) {
                val forbiddenIds = setOf(
                    "antecedente_arcano_ciencia_estranha",
                    "antecedente_arcano_psionicos",
                    "antecedente_arcano_dom",
                    "rico",
                    "podre_de_rico"
                )
                if (vant.id in forbiddenIds) return@filter false
            }

            if (state.compendioArteDaGuerraAtivo) {
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
                    return@filter false
                }
                if (vant.categoria == Categoria.PODER) {
                    return@filter false
                }
                if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
                    return@filter false
                }
            }

            if (state.compendioWiseguysAtivo) {
                if (vant.categoria == Categoria.PODER) {
                    return@filter false
                }
                if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
                    return@filter false
                }
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
                    return@filter false
                }
                val forbiddenIds = setOf("aristocrata", "chi", "campeao", "matador_de_gigantes", "corajoso")
                if (vant.id in forbiddenIds) {
                    return@filter false
                }
            }

            (isBasico && !state.compendioFantasiaAtivo) || (isAdg && state.compendioArteDaGuerraAtivo) || (isSuper && state.modoSupers) || (isFantasia && state.compendioFantasiaAtivo) || (isHorror && state.compendioHorrorAtivo) || (isBuscatrilha && state.compendioBuscatrilhaAtivo) || (isDeadlands && state.compendioDeadlandsAtivo) || (isCidadeSolVapor && state.compendioCidadeSolVaporAtivo) || (isWiseguys && state.compendioWiseguysAtivo) || (isCrystalHeart && state.compendioCrystalHeartAtivo)
        }.sortedWith(compareBy({ it.categoria }, { it.nomeExibicao }))
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
    var tempErrorMsg by remember { mutableStateOf("") }
    var showTempError by remember { mutableStateOf(false) }
    var pendingVantagem by remember { mutableStateOf<Vantagem?>(null) }
    var showChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var dialogMostrandoAntecedente by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoCavaleiro by remember { mutableStateOf<Vantagem?>(null) }
    var dialogMostrandoMontaria by remember { mutableStateOf<Vantagem?>(null) }
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

    val filteredListGlobal = remember(listaVantagensAtivas, state.modoSupers, hasProfissional, filter) {
        listaVantagensAtivas.filter { vant ->
            // Supers Logic
            if (state.modoSupers) {
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) return@filter false
                if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") return@filter false
                if (vant.categoria == Categoria.PODER) return@filter false
                if (vant.requisitos.vantagensPrevias.contains("antecedente_arcano") ||
                    vant.id == "superpoderes") return@filter false
            }

            // Professional/Specialist Dependency
            if (vant.id == "especialista" && !hasProfissional) return@filter false

            // Advanced Filters
            if (!filter.isEmpty()) {
                val vantOrigem = vant.origem.ifBlank { "BASICO" }.uppercase()
                if (filter.origens.isNotEmpty() && vantOrigem !in filter.origens) return@filter false
                if (filter.estagios.isNotEmpty() && vant.requisitos.estagio !in filter.estagios) return@filter false
                if (filter.atributos.isNotEmpty() && filter.atributos.intersect(vant.requisitos.atributoMin.keys).isEmpty()) return@filter false
                if (filter.pericias.isNotEmpty()) {
                    val reqMin = vant.requisitos.periciaMin.keys
                    val reqOpt = vant.requisitos.periciaMinOpcional.keys
                    val vinc = if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
                    if (filter.pericias.intersect(reqMin + reqOpt + vinc).isEmpty()) return@filter false
                }
            }
            true
        }
    }

    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

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
                if (state.compendioBuscatrilhaAtivo) {
                    val slotAvailable = state.pathfinderSlotAvailable
                    val (color, text) = if (slotAvailable) {
                        MaterialTheme.colorScheme.primaryContainer to "Vantagem de Classe Gratuita DISPONÍVEL"
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant to "Vantagem de Classe Gratuita UTILIZADA"
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
                placeholder = "Pesquisar Vantagens..."
            ) {
                Spacer(Modifier.size(8.dp))

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

                    // Category Chips
                    items(
                        items = Categoria.entries.toTypedArray(),
                        key = { it.name },
                        contentType = { "category_chip" }
                    ) { cat ->
                        if (state.modoSupers && cat == Categoria.PODER) return@items

                        // --- NEW FILTERING LOGIC ---
                        if ((cat == Categoria.CLASSE || cat == Categoria.PRESTIGIO) && !state.compendioBuscatrilhaAtivo) return@items
                        if (cat == Categoria.RESSUSCITADO && !state.compendioDeadlandsAtivo) return@items
                        if (cat == Categoria.TROPO && !state.compendioArteDaGuerraAtivo) return@items
                        if (cat == Categoria.SUPER && !state.modoSupers) return@items
                        if (cat == Categoria.MONSTRUOSAS && !state.compendioHorrorAtivo) return@items
                        if (cat == Categoria.CHI && !state.compendioArteDaGuerraAtivo) return@items

                        FilterChip(
                            selected = cat in selectedCategories,
                            onClick = {
                                if (cat in selectedCategories) selectedCategories.remove(cat)
                                else selectedCategories.add(cat)
                            },
                            label = { Text(cat.name.toSentenceCase()) }
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
                                tempErrorMsg = msg ?: "Não é possível remover."
                                showTempError = true
                                scope.launch {
                                    delay(2_000)
                                    showTempError = false
                                }
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
                            val labelText = vant.choice?.let { "${vant.nomeExibicao} ($it)" } ?: vant.nomeExibicao
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

            if (showTempError) {
                Text(
                    tempErrorMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Spacer(Modifier.size(8.dp))
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
                if ((cat == Categoria.CLASSE || cat == Categoria.PRESTIGIO) && !state.compendioBuscatrilhaAtivo) return@forEach
                if (cat == Categoria.RESSUSCITADO && !state.compendioDeadlandsAtivo) return@forEach
                if (cat == Categoria.TROPO && !state.compendioArteDaGuerraAtivo) return@forEach

                val expanded = expandedMap[cat] ?: false

                // Category Header
                item(key = "header_${cat.name}") {
                    Column {
                        CollapsibleSection(
                            title = cat.name.toSentenceCase(),
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
                                 onSelect = {
                                    if (vant.vinculadoPericia) {
                                        pendingVantagem = vant
                                        showChoiceDialog = true
                                    } else if (vant.id == "antecedente_arcano") {
                                        dialogMostrandoAntecedente = vant
                                    } else if (vant.nome.keyify() == "CAVALEIRO") {
                                        dialogMostrandoCavaleiro = vant
                                    } else if (vant.nome.keyify() == "MONTARIA") {
                                        dialogMostrandoMontaria = vant
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
                                     tempErrorMsg = msg
                                     showTempError = true
                                     scope.launch {
                                        delay(2_000)
                                        showTempError = false
                                     }
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
                             onSelect = {
                                if (vant.vinculadoPericia) {
                                    pendingVantagem = vant
                                    showChoiceDialog = true
                                } else if (vant.id == "antecedente_arcano") {
                                    dialogMostrandoAntecedente = vant
                                } else if (vant.nome.keyify() == "CAVALEIRO") {
                                    dialogMostrandoCavaleiro = vant
                                } else if (vant.nome.keyify() == "MONTARIA") {
                                    dialogMostrandoMontaria = vant
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
                                 tempErrorMsg = msg
                                 showTempError = true
                                 scope.launch {
                                    delay(2_000)
                                    showTempError = false
                                 }
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
            } else if (state.compendioBuscatrilhaAtivo) {
                val n = per.nome.keyify()
                n != "FOCO" && n !in com.example.swadebuilder.model.SAVAGE_PATHFINDER_BLOCKED_SKILLS
            } else {
                true
            }
        }.map { it.nome }

        val allPericias = listaPericias
            .map { it.nome }
            .filter { it in requiredPericias && it in visibleSkills }

        VantFilterDialog(
            allEstagios = allEstagios,
            allAtributos = allAtributos,
            allPericias = allPericias,
            current = filter,
            onChange = { state.vantFilter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (dialogMostrandoAntecedente != null) {
        val vantOriginal = dialogMostrandoAntecedente!!
        val opcoesArcano: List<String> = vantOriginal.choiceOptions

        AlertDialog(
            onDismissRequest = {
                dialogMostrandoAntecedente = null
                subOpcaoSelecionada = null
            },
            title = { Text("Escolha o tipo de ${vantOriginal.nome}") },
            text = {
                Column {
                    opcoesArcano.forEach { opcao ->
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
                            onUserFeedback()
                        }
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

            val knowledgeOptions = listaPericias
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
                    tempErrorMsg = "Nenhuma perícia de Conhecimento disponível"
                    showTempError = true
                    delay(2_000)
                    showTempError = false
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
                    listaPericias
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
                    tempErrorMsg = "Nenhuma opção disponível para escolher"
                    showTempError = true
                    delay(2_000)
                    showTempError = false
                    showChoiceDialog = false
                    pendingVantagem = null
                }
            } else {
                ChoiceDialog(
                    options = validOptions,
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
            add("$p d$m")
        }

        if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
            add(
                vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                    "${it.key} d${it.value}"
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

                    when {
                        !isPathfinderFree && state.pontosVantagem <= 0 -> onError("Sem PV disponível")
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
                    val origemLabel = vant.origem
                    if (origemLabel.isNotBlank() && origemLabel != "BASICO") {
                         Text(
                             text = origemLabel.toEditionDisplayName(),
                             style = MaterialTheme.typography.labelSmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant
                         )
                    }
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
                if (vant.descricao.isNotBlank() && vant.vinculadoPericia) {
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
                    Text(
                        text = if (showOfficialNames && !vant.originalDescription.isNullOrBlank()) vant.originalDescription.trim() else vant.descricao.trim(),
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
