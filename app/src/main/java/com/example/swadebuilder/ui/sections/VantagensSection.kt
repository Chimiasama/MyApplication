package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.mutableStateListOf
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
import java.util.Locale
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.components.StandardListItem
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.ui.dialogs.MultipleSelectionDialog
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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
        title = { Text("Filtros de Vantagens") },
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
    viewModel: CriadorViewModel = viewModel()
) {
    // We use the global `listaVantagens` loaded in MainActivity (which includes compendium content)
    // instead of reloading just the base `Vantagens.json`.
    val listaVantagensGlobal = listaVantagens

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

    val listaVantagensAtivas: List<Vantagem> = remember(listaVantagens, state.modoSupers, state.compendioFantasiaAtivo, state.compendioHorrorAtivo, state.compendioTrilhadorAtivo, state.compendioDeadlandsAtivo, state.compendioCrystalHeartAtivo, state.compendioArteDaGuerraAtivo, state.compendioCidadeSolVaporAtivo, state.compendioWiseguysAtivo) {
        listaVantagens.filter { vant ->
            val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase()
            val isBasico = origemNorm == "BASICO"
            val isSuper = origemNorm == "SUPER"
            val isFantasia = origemNorm == "FANTASIA"
            val isHorror = origemNorm == "HORROR"
            val isTrilhador = origemNorm == "FANTASIA_TRILHADOR"
            val isDeadlands = origemNorm == "DEADLANDS"
            val isAdg = origemNorm == "ARTE_DA_GUERRA"
            val isCidadeSolVapor = origemNorm == "CIDADE_SOL_VAPOR"
            val isWiseguys = origemNorm == "WISEGUYS"

            if (state.compendioCrystalHeartAtivo) {
                if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
                     return@filter false
                }
            }

            isBasico || (isAdg && state.compendioArteDaGuerraAtivo) || (isSuper && state.modoSupers) || (isFantasia && state.compendioFantasiaAtivo) || (isHorror && state.compendioHorrorAtivo) || (isTrilhador && state.compendioTrilhadorAtivo) || (isDeadlands && state.compendioDeadlandsAtivo) || (isCidadeSolVapor && state.compendioCidadeSolVaporAtivo) || (isWiseguys && state.compendioWiseguysAtivo)
        }
    }

    val idParaNome = remember(listaVantagens) {
        listaVantagens.associate { it.id to it.nome }
    }

    val categoriasBy = remember(listaVantagensAtivas) {
        listaVantagensAtivas.groupBy { it.categoria }
    }

    var filter by remember { mutableStateOf(VantFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
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

        Spacer(Modifier.size(8.dp))

        Text(
            text = if (filter.isEmpty()) "Filtrar vantagens"
            else "Filtros (${filter.totalSelections()})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { showFilterDialog = true }
        )

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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                            if (vant.nome.contains("Pontos de Poder", true)) {
                                state.removerPontosDePoder(vant)
                                state.pontosVantagem++
                                state.rebuildAllPericiaStacks()
                            } else {
                                state.removeVantagemDinheiro(vant)
                                state.vantagensSelecionadas.remove(vant)
                                state.pontosVantagem++
                                state.rebuildAllPericiaStacks()

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

        // Iterate through Categoria enum to ensure consistent order
        Categoria.entries.forEach { cat ->
            val lista = categoriasBy[cat] ?: return@forEach
            if (state.modoSupers && cat == Categoria.PODER) return@forEach

            val expanded = expandedMap[cat] ?: false

            CollapsibleSection(
                title = cat.name,
                expanded = expanded,
                onToggle = { expandedMap[cat] = !expanded }
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
                        val statusText = when {
                            jaTem -> "Já selecionada"
                            requisitosOk -> "Requisitos OK"
                            else -> "Requisitos pendentes"
                        }
                        val statusColor = when {
                            jaTem -> MaterialTheme.colorScheme.tertiary
                            requisitosOk -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.error
                        }

                        // Status Color Mapping
                        val containerColor = when {
                            jaTem -> MaterialTheme.colorScheme.tertiaryContainer
                            requisitosOk -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.errorContainer
                        }

                        StandardListItem(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                            title = {
                                Text(
                                    if (state.modoOficialAtivo && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            statusText = statusText,
                            statusColor = statusColor,
                            backgroundColor = containerColor,
                            onClick = if (!locked) {
                                {
                                    when {
                                        state.pontosVantagem <= 0 -> {
                                            tempErrorMsg = "Sem PV disponível"
                                            showTempError = true
                                        }

                                        !state.podeSelecionar(vant) -> {
                                            tempErrorMsg =
                                                "Faltam requisitos para '${vant.nome}'"
                                            showTempError = true
                                        }

                                        vant.vinculadoPericia -> {
                                            pendingVantagem = vant
                                            showChoiceDialog = true
                                        }

                                        vant.id == "antecedente_arcano" -> {
                                            dialogMostrandoAntecedente = vant
                                        }

                                        else -> {
                                            if (state.advantageAdvancementInProgress) {
                                                viewModel.selectAdvantageForAdvancement(vant)
                                            } else {
                                                if (vant.nome.contains(
                                                        "Pontos de Poder",
                                                        true
                                                    )
                                                ) {
                                                    state.comprarPontoDePoder(vant)
                                                } else {
                                                    state.applyVantagemDinheiro(vant)
                                                    state.vantagensSelecionadas += vant
                                                }
                                                state.pontosVantagem--
                                                state.rebuildAllPericiaStacks()
                                            }
                                        }
                                    }

                                    scope.launch {
                                        delay(2_000)
                                        showTempError = false
                                    }
                                }
                            } else null,
                            bottomContent = {
                                Column {
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
                                }
                            },
                            detailsContent = if (allowLongTexts && vant.descricao.isNotBlank()) {
                                {
                                    Text(
                                        text = if (state.modoOficialAtivo && !vant.originalDescription.isNullOrBlank()) vant.originalDescription.trim() else vant.descricao.trim(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
            Spacer(Modifier.size(8.dp))
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
                                    state.vantagensSelecionadas += novaVantagem
                                    state.pontosVantagem--
                                    state.rebuildAllPericiaStacks()
                                }
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
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            state.pontosVantagem--
                            state.rebuildAllPericiaStacks()
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
                            state.vantagensSelecionadas += vant.copy(choice = choice)
                            state.pontosVantagem--
                            state.rebuildAllPericiaStacks()
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
}
