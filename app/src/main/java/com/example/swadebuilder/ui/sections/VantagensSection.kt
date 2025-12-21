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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.classeExclusivaBloqueada
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.ui.components.PbLegacyActions
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SearchTextField
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class VantFilter(
    val origens: Set<String> = emptySet(),
    val estagios: Set<String> = emptySet(),
    val atributos: Set<String> = emptySet(),
    val pericias: Set<String> = emptySet()
) {
    fun isEmpty() =
        origens.isEmpty() && estagios.isEmpty() && atributos.isEmpty() && pericias.isEmpty()

    fun totalSelections() =
        origens.size + estagios.size + atributos.size + pericias.size
}

@Composable
fun VantFilterDialog(
    allOrigens: List<String>,
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
                Text("Origem", fontWeight = FontWeight.Bold)
                allOrigens.forEach { o ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = o in current.origens,
                            onCheckedChange = {
                                val s = current.origens.toMutableSet()
                                if (it) s += o else s -= o
                                onChange(current.copy(origens = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(o)
                    }
                }
                Spacer(Modifier.size(8.dp))

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
            if (multiplosAAHabilitados) {
                listaVantagensGlobal.filterNot { it.id == "antecedente_arcano" }
            } else {
                listaVantagensGlobal.filterNot { it.id.startsWith("antecedente_arcano_") }
            }
        }

    remember(state.modoSupers) {
        buildSet {
            add("BASICO")
            if (state.modoSupers) add("SUPER")
        }
    }

    val listaVantagensAtivas: List<Vantagem> = remember(listaVantagens, state.modoSupers, state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioBuscatrilhaAtivo, state.compendioDeadlandsAtivo, state.compendioCrystalHeartAtivo, state.compendioArteDaGuerraAtivo, state.compendioCidadeSolVaporAtivo, state.compendioWiseguysAtivo) {
        listaVantagens.filter { vant ->
            val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase()
            val isBasico = origemNorm == "BASICO"
            val isSuper = origemNorm == "SUPER"
            val isFantasia = origemNorm == "FANTASIA"
            val isHorror = origemNorm == "HORROR"
            val isBuscatrilha = origemNorm == "FANTASIABUSCATRILHA"
            val isDeadlands = origemNorm == "DEADLANDS"
            val isAdg = origemNorm == "ARTE_DA_GUERRA"
            val isCidadeSolVapor = origemNorm == "CIDADE_SOL_VAPOR"
            val isWiseguys = origemNorm == "WISEGUYS"

            if (state.compendioCrystalHeartAtivo) {
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
                     return@filter false
                }
            }

            isBasico || (isAdg && state.compendioArteDaGuerraAtivo) || (isSuper && state.modoSupers) || (isFantasia && state.compendioFantasiaAtivo) || (isHorror && state.compendioHorrorAtivo) || (isBuscatrilha && state.compendioBuscatrilhaAtivo) || (isDeadlands && state.compendioDeadlandsAtivo) || (isCidadeSolVapor && state.compendioCidadeSolVaporAtivo) || (isWiseguys && state.compendioWiseguysAtivo)
        }
    }

    val idParaNome = remember(listaVantagens) {
        listaVantagens.associate { it.id to it.nome }
    }

    // --- Search & Filter State ---
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories = remember { mutableStateListOf<Categoria>() }
    var filter by remember { mutableStateOf(VantFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    // --- Interaction State ---
    var tempErrorMsg by remember { mutableStateOf("") }
    var showTempError by remember { mutableStateOf(false) }
    var pendingVantagem by remember { mutableStateOf<Vantagem?>(null) }
    var showChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var dialogMostrandoAntecedente by remember { mutableStateOf<Vantagem?>(null) }
    var subOpcaoSelecionada by rememberSaveable { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val expandedMap = state.categoriasVantagensExpandidas
    val vantagemEmFoco = state.vantagemEmFoco

    LaunchedEffect(vantagemEmFoco) {
        if (!vantagemEmFoco.isNullOrBlank()) {
            val v = listaVantagensAtivas.firstOrNull { it.nome == vantagemEmFoco }
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

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Vantagem: ${state.pontosVantagem}",
            onListaCompletaClick = null,
            listaCompletaText = ""
        )

        Spacer(Modifier.size(4.dp))

        if (!state.emProgresso) {
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
        SearchTextField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.size(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Advanced Filters Chip
             item {
                 FilterChip(
                     selected = !filter.isEmpty(),
                     onClick = { showFilterDialog = true },
                     label = { Text("Filtros Avançados${if(!filter.isEmpty()) " (!)" else ""}") }
                 )
             }

            // Category Chips
            items(Categoria.entries.toTypedArray()) { cat ->
                if (state.modoSupers && cat == Categoria.PODER) return@items

                // --- NEW FILTERING LOGIC ---
                // Hide specific categories if their compendium is not active
                if ((cat == Categoria.CLASSE || cat == Categoria.PRESTIGIO) && !state.compendioBuscatrilhaAtivo) return@items
                if (cat == Categoria.ESTRANHAS && !state.compendioDeadlandsAtivo) return@items
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
                    label = { Text(cat.name) }
                )
            }
        }

        if (showFilterDialog) {
            val allOrigens = listaVantagensAtivas.map { it.origem.ifBlank { "BASICO" }.uppercase() }.distinct()
            val allEstagios = listaDeEstagios.map { it.nome }
            val allAtributos = mapaAtributosDisplay.values.toList()
            val requiredPericias = listaVantagensAtivas.flatMap { vant ->
                vant.requisitos.periciaMin.keys +
                        vant.requisitos.periciaMinOpcional.keys +
                        if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
            }.distinct()
            val allPericias = listaPericias
                .map { it.nome }
                .filter { it in requiredPericias }

            VantFilterDialog(
                allOrigens = allOrigens,
                allEstagios = allEstagios,
                allAtributos = allAtributos,
                allPericias = allPericias,
                current = filter,
                onChange = { filter = it },
                onDismiss = { showFilterDialog = false }
            )
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

                            val enforcePoolLimit = !vant.isBrutamontes()

                            if (vant.nome.contains("Pontos de Poder", true)) {
                                state.removerPontosDePoder(vant)
                                state.pontosVantagem++
                                state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
                            } else {
                                state.removeVantagemDinheiro(vant)
                                state.vantagensSelecionadas.remove(vant)
                                state.pontosVantagem++
                                state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
                                onUserFeedback()

                                if (vant.id == "o_melhor_que_ha") {
                                    state.poderFavoritoId = null
                                }
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
                        val labelText = vant.choice?.let { "${vant.nome} ($it)" } ?: vant.nome
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

        // --- List Content ---
        // Logic: If search OR category selection is active, use Flat List. Otherwise, usage Accordions (Browse Mode).
        val isSearching = searchQuery.isNotBlank()
        val isFilteringCategories = selectedCategories.isNotEmpty()

        if (isSearching || isFilteringCategories) {
            // Flat List View
            val flatList = listaVantagensAtivas.filter { vant ->
                // Basic Filter Logic
                 if (!state.modoSupers) {
                    true
                } else {
                    vant.id != "antecedente_arcano" &&
                            !vant.requisitos.vantagensPrevias.contains("antecedente_arcano")
                }
            }.filter { vant ->
                if (state.modoSupers) vant.id != "superpoderes" else true
            }.filter { vant ->
                 vant.id != "especialista" || state.vantagensSelecionadas.any { it.id == "profissional" }
            }.filter { vant ->
                // Category Filter
                if (selectedCategories.isNotEmpty() && vant.categoria !in selectedCategories) return@filter false

                // Search Query
                if (isSearching) {
                    val q = searchQuery.semAcentos().lowercase()
                    val n = vant.nome.semAcentos().lowercase()
                    val d = vant.descricao.semAcentos().lowercase()
                    if (!n.contains(q) && !d.contains(q)) return@filter false
                }

                // Advanced Filters
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
                true
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Taller list for flat view
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                 if (flatList.isEmpty()) {
                     item { Text("Nenhuma vantagem encontrada.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(flatList, key = { it.id }) { vant ->
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
                                } else {
                                    if (state.advantageAdvancementInProgress) {
                                        viewModel.selectAdvantageForAdvancement(vant)
                                        onUserFeedback()
                                    } else {
                                        val enforcePoolLimit = !vant.isBrutamontes()
                                        if (vant.nome.contains("Pontos de Poder", true)) {
                                            state.comprarPontoDePoder(vant)
                                            onUserFeedback()
                                        } else {
                                            state.applyVantagemDinheiro(vant)
                                            state.vantagensSelecionadas += vant
                                            state.pontosVantagem--
                                            state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
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

        } else {
            // Browse Mode (Accordions)
            val categoriasBy = remember(listaVantagensAtivas) {
                listaVantagensAtivas.groupBy { it.categoria }
            }

            Categoria.entries.forEach { cat ->
                val lista = categoriasBy[cat] ?: return@forEach
                if (state.modoSupers && cat == Categoria.PODER) return@forEach

                // Also hide in accordion view if filtered out by active mods (consistency)
                if ((cat == Categoria.CLASSE || cat == Categoria.PRESTIGIO) && !state.compendioBuscatrilhaAtivo) return@forEach
                if (cat == Categoria.ESTRANHAS && !state.compendioDeadlandsAtivo) return@forEach
                if (cat == Categoria.RESSUSCITADO && !state.compendioDeadlandsAtivo) return@forEach
                if (cat == Categoria.TROPO && !state.compendioArteDaGuerraAtivo) return@forEach

                val expanded = expandedMap[cat] ?: false

                CollapsibleSection(
                    title = cat.name,
                    expanded = expanded,
                    onToggle = { expandedMap[cat] = !expanded },
                    onToggleFeedback = onUserFeedback
                ) {
                    val listaFiltrada = lista
                        .filter { vant ->
                            if (!state.modoSupers) {
                                true
                            } else {
                                vant.id != "antecedente_arcano" &&
                                        !vant.requisitos.vantagensPrevias.contains("antecedente_arcano")
                            }
                        }
                        .filter { vant ->
                            if (state.modoSupers) vant.id != "superpoderes" else true
                        }
                        .filter { vant ->
                            vant.categoria == cat &&
                                    (vant.id != "especialista" ||
                                            state.vantagensSelecionadas.any { it.id == "profissional" })
                        }
                        .filter { vant ->
                            val vantOrigem = vant.origem.ifBlank { "BASICO" }.uppercase()
                            if (filter.origens.isNotEmpty() &&
                                vantOrigem !in filter.origens
                            ) return@filter false

                            if (filter.estagios.isNotEmpty() &&
                                vant.requisitos.estagio !in filter.estagios
                            ) return@filter false

                            if (filter.atributos.isNotEmpty() &&
                                filter.atributos.intersect(vant.requisitos.atributoMin.keys)
                                    .isEmpty()
                            ) return@filter false

                            if (filter.pericias.isNotEmpty()) {
                                val reqMin = vant.requisitos.periciaMin.keys
                                val reqOpt = vant.requisitos.periciaMinOpcional.keys
                                val vinc =
                                    if (vant.vinculadoPericia) vant.choiceOptions else emptyList()
                                if (filter.pericias.intersect(reqMin + reqOpt + vinc).isEmpty())
                                    return@filter false
                            }
                            true
                        }

                    val listState = rememberLazyListState()

                    LaunchedEffect(vantagemEmFoco, expanded, listaFiltrada) {
                        if (expanded && !vantagemEmFoco.isNullOrBlank()) {
                            val targetIndex = listaFiltrada.indexOfFirst { it.nome == vantagemEmFoco }
                            if (targetIndex >= 0) {
                                listState.animateScrollToItem(targetIndex)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .padding(start = 8.dp, bottom = 8.dp),
                        state = listState
                    ) {
                        items(listaFiltrada, key = { it.id }) { vant ->
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
                                    } else {
                                        if (state.advantageAdvancementInProgress) {
                                            viewModel.selectAdvantageForAdvancement(vant)
                                            onUserFeedback()
                                        } else {
                                            val enforcePoolLimit = !vant.isBrutamontes()
                                            if (vant.nome.contains("Pontos de Poder", true)) {
                                                state.comprarPontoDePoder(vant)
                                                onUserFeedback()
                                            } else {
                                                state.applyVantagemDinheiro(vant)
                                                state.vantagensSelecionadas += vant
                                                state.pontosVantagem--
                                                state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
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
                Spacer(Modifier.size(8.dp))
            }
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
                                val enforcePoolLimit = !novaVantagem.isBrutamontes()

                                if (state.advantageAdvancementInProgress) {
                                    viewModel.selectAdvantageForAdvancement(novaVantagem)
                                } else {
                                    state.vantagensSelecionadas += novaVantagem
                                    state.pontosVantagem--
                                    state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
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
                            val enforcePoolLimit = !vant.isBrutamontes()
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            state.pontosVantagem--
                            state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
                            showChoiceDialog = false
                            pendingVantagem = null
                            onUserFeedback()
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
                            val enforcePoolLimit = !vant.isBrutamontes()
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            state.pontosVantagem--
                            state.rebuildAllPericiaStacks(enforcePoolLimit = enforcePoolLimit)
                            showChoiceDialog = false
                            pendingVantagem = null
                            onUserFeedback()
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
                ?: prevId.replace('_', ' ').uppercase()
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
    // PROMPT 4: Specific logic for Pathfinder Class validation
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
                    when {
                        state.pontosVantagem <= 0 -> onError("Sem PV disponível")
                        // PROMPT 4: Check class blocking specifically for error message
                        state.vantagensSelecionadas.classeExclusivaBloqueada(vant) -> onError("Requer a vantagem Multiclasse para possuir duas classes")
                        conflitoMsg != null -> onError(conflitoMsg)
                        !state.podeSelecionar(vant) -> onError("Faltam requisitos para '${vant.nome}'")
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
        )
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )

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
                        style = MaterialTheme.typography.labelMedium
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
