package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.ui.dialogs.MultipleSelectionDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VantagensContent(
    state: CriadorState,
    multiplosAAHabilitados: Boolean,
    onOpenVantagensDetail: (String) -> Unit,
    viewModel: CriadorViewModel = viewModel()
) {
    val context = LocalContext.current

    val listaVantagensRaw: List<Vantagem> = remember {
        val jsonString = context.assets.open("Vantagens.json")
            .bufferedReader()
            .use { it.readText() }
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            isLenient = true
            coerceInputValues = true
        }
        json.decodeFromString(jsonString)
    }

    val listaVantagens: List<Vantagem> =
        remember(multiplosAAHabilitados, listaVantagensRaw) {
            if (multiplosAAHabilitados) {
                listaVantagensRaw.filterNot { it.id == "antecedente_arcano" }
            } else {
                listaVantagensRaw.filterNot { it.id.startsWith("antecedente_arcano_") }
            }
        }

    remember(state.modoSupers) {
        buildSet {
            add("BASICO")
            if (state.modoSupers) add("SUPER")
        }
    }

    val listaVantagensAtivas: List<Vantagem> = remember(listaVantagens, state.modoSupers) {
        listaVantagens.filter { vant ->
            val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase()
            val isBasico = origemNorm == "BASICO"
            val isSuper = origemNorm == "SUPER"

            isBasico || (isSuper && state.modoSupers)
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
    var selectedReqs by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingVantagem by remember { mutableStateOf<Vantagem?>(null) }
    var showChoiceDialog by rememberSaveable { mutableStateOf(false) }
    var pendingNovosPoderes by rememberSaveable { mutableStateOf<Vantagem?>(null) }
    var showNovosPoderesDialog by rememberSaveable { mutableStateOf(false) }
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
    val showLista = booleanResource(com.example.swadebuilder.R.bool.show_lista_completa)

    val pcTotal = state.pontosComplicacao
    val pcGastos = state.pontosComplicacaoGastos
    val pcLivres = (pcTotal - pcGastos).coerceAtLeast(0)
    val pvUsados = state.cpPvStack.size

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            onHelpClick = null,
            centerText = "Pontos de Vantagem: ${state.pontosVantagem}",
            onListaCompletaClick = if (showLista) ({ onOpenVantagensDetail("") }) else null,
            listaCompletaText = "Lista Completa"
        )

        Spacer(Modifier.size(4.dp))

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
            val allOrigens = listaVantagensAtivas.map { it.origem.uppercase() }.distinct()
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
                        !isSuperpoderesLocked

                val canRemove =
                    baseRemovable && !(state.emProgresso && vant.id == "novos_poderes")
                            && !isCelestialAAMilagres

                val isCelestialAAMilagresDesabilitado = state.celestialAAMilagresDesabilitado &&
                        vant.id == "antecedente_arcano_milagres"

                AssistChip(
                        onClick = {
                            if (!canRemove) return@AssistChip

                            if (vant.id == "novos_poderes") {
                                val escolhidoArcano = state.vantagensSelecionadas
                                    .firstOrNull { it.id == "antecedente_arcano" }
                                    ?.choice
                                    ?.uppercase()
                                    ?.semAcentos()
                                    ?.trim()
                                    ?: ""

                                val initialSlots = arcanoInfo[escolhidoArcano]?.first ?: 0

                                state.desfazerUltimosNovosPoderes(
                                    versionKey = escolhidoArcano,
                                    initialSlots = initialSlots
                                )

                                state.vantagensSelecionadas.remove(vant)
                                state.pontosVantagem++
                                state.rebuildAllPericiaStacks()
                            } else if (vant.nome.contains("Pontos de Poder", true)) {
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

        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(8.dp)) {
                when {
                    showTempError ->
                        Text(tempErrorMsg, color = MaterialTheme.colorScheme.error)

                    selectedReqs.isEmpty() -> Text(
                        "Selecione uma vantagem para ver requisitos",
                        style = MaterialTheme.typography.bodySmall
                    )

                    else -> selectedReqs.forEach { req ->
                        Text("• $req", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.size(16.dp))

        categoriasBy.forEach { (cat, lista) ->
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
                        if (filter.origens.isNotEmpty() &&
                            vant.origem.uppercase() !in filter.origens
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
                            }?.let { add("Estágio ≥ ${it.nome}") }

                            vant.requisitos.atributoMin.forEach { (a, m) ->
                                add("$a ≥ $m")
                            }
                            vant.requisitos.periciaMin.forEach { (p, m) ->
                                add("$p ≥ $m")
                            }

                            if (vant.requisitos.periciaMinOpcional.isNotEmpty()) {
                                add(
                                    vant.requisitos.periciaMinOpcional.entries.joinToString(" ou ") {
                                        "${it.key} d${it.value}+"
                                    }
                                )
                            }

                            vant.requisitos.vantagensPrevias.forEach { prevId ->
                                val legivel = idParaNome[prevId]
                                    ?: prevId.replace('_', ' ').uppercase()
                                add("Pré-requisito: $legivel")
                            }

                            if (vant.requisitos.exigeCS) add("Requer Carta Selvagem")
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

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .clickable(enabled = !locked) {
                                    selectedReqs = reqList
                                    if (!locked) {
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

                                            vant.id == "novos_poderes" -> {
                                                pendingNovosPoderes = vant
                                                showNovosPoderesDialog = true
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
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    jaTem -> MaterialTheme.colorScheme.tertiaryContainer
                                    requisitosOk -> MaterialTheme.colorScheme.surfaceVariant
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
                                        vant.nome,
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
                                    val origem = vant.origem.ifBlank { "BÁSICO" }.uppercase(Locale.ROOT)
                                    AssistChip(onClick = {}, label = { Text(origem) })

                                    listaDeEstagios.firstOrNull {
                                        it.nome.equals(vant.requisitos.estagio, true)
                                    }?.let { est ->
                                        AssistChip(onClick = {}, label = { Text("Estágio ${est.nome}") })
                                    }

                                    if (showLista) {
                                        AssistChip(
                                            onClick = { onOpenVantagensDetail(vant.nome) },
                                            label = { Text("Ver detalhes") }
                                        )
                                    }

                                    if (vant.descricao.isNotBlank() && vant.vinculadoPericia) {
                                        AssistChip(
                                            onClick = {},
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                )
                                            },
                                            label = { Text("Opções especiais") }
                                        )
                                    }
                                }

                                vant.descricao.takeIf { it.isNotBlank() }?.let { desc ->
                                    Spacer(Modifier.size(8.dp))
                                    Text(
                                        desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
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
                                state.vantagensSelecionadas += novaVantagem
                                state.pontosVantagem--
                                state.rebuildAllPericiaStacks()
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

        if (showNovosPoderesDialog && pendingNovosPoderes != null) {
            val vant = pendingNovosPoderes!!

            val allPoderes: List<Poder> =
                LocalContext.current.loadJsonAsset("poderes.json")

            val curEstIndex = listaDeEstagios.indexOfFirst {
                it.nome == state.estagioAtual().nome
            }

            val disponiveis = allPoderes
                .filter { poder ->
                    val poderIndex = listaDeEstagios.indexOfFirst { it.nome == poder.estagio }
                    if (curEstIndex == -1 || poderIndex == -1) {
                        true
                    } else {
                        poderIndex <= curEstIndex
                    }
                }
                .map { it.nome }
                .filter { nome -> nome !in state.poderesSelecionados }

            MultipleSelectionDialog(
                title = "Escolha 2 novos poderes",
                options = disponiveis,
                maxSelections = 2,
                onConfirm = { escolhas ->
                    val escolhidoArcano = state.vantagensSelecionadas
                        .firstOrNull { it.id == "antecedente_arcano" }
                        ?.choice
                        ?: ""

                    val versionKey = escolhidoArcano
                        .uppercase()
                        .semAcentos()
                        .trim()

                    val initialSlots = arcanoInfo[versionKey]?.first ?: 0

                    val slots = state.poderSlotsPorArcano.getOrPut(versionKey) {
                        mutableStateListOf<String?>().apply {
                            repeat(initialSlots) { add(null) }
                        }
                    }

                    escolhas.forEach { poder ->
                        val firstEmpty = slots.indexOfFirst { it == null }
                        if (firstEmpty >= 0) {
                            slots[firstEmpty] = poder
                        } else {
                            slots.add(poder)
                        }
                    }

                    state.poderSlotsPorArcano[versionKey] = slots
                    state.poderesSelecionados.clear()
                    state.poderesSelecionados.addAll(slots.filterNotNull())
                    state.registrarNovosPoderes(versionKey, escolhas)

                    state.vantagensSelecionadas += vant
                    state.pontosVantagem--
                    state.rebuildAllPericiaStacks()

                    showNovosPoderesDialog = false
                    pendingNovosPoderes = null
                },
                onDismiss = {
                    showNovosPoderesDialog = false
                    pendingNovosPoderes = null
                }
            )
        }
    }
}


