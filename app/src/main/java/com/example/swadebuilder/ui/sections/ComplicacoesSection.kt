@file:Suppress("DEPRECATION")
@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.model.isComplicacaoVisible
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.dialogs.ChoiceDialog
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ComplicacoesSection(
    state: CriadorState,
    feedbackMessages: MutableList<String>,
    onUserFeedback: () -> Unit,
    onLogFeedback: (String) -> Unit = {}
) {
    val locked = state.criacaoBasicaCongelada

    var showPcInUseDialog by rememberSaveable { mutableStateOf(false) }
    var complicationToRemove by remember { mutableStateOf<Complicacao?>(null) }

    // PROMPT 3: State for adding disorders
    var showAddTranstornoDialog by rememberSaveable { mutableStateOf(false) }

    val autoBaseKeys = state.desvantagensAutomaticas
        .map { it.uppercase().semAcentos().substringBefore("(").trim() }
        .toSet()

    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }
    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo
    val scope = rememberCoroutineScope()
    var tempErrorMsg by remember { mutableStateOf("") }
    var showTempError by remember { mutableStateOf(false) }

    val origensAtivas = state.getActiveOrigins()

    // Filter states
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSeverity by rememberSaveable { mutableStateOf("Todos") }

    val complicacoesFiltradas = state.listaComplicacoes.filter { comp ->
        if (!state.isComplicacaoVisible(comp, origensAtivas)) return@filter false

        // Filter Logic
        val matchesSeverity = when (selectedSeverity) {
            "Todos" -> true
            "Menor" -> comp.severity.lowercase().contains("menor")
            "Maior" -> comp.severity.lowercase().contains("maior")
            else -> true
        }

        if (!matchesSeverity) return@filter false

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.semAcentos().lowercase()
            val n = comp.name.semAcentos().lowercase()
            val d = comp.description.semAcentos().lowercase()
            n.contains(q) || d.contains(q)
        } else {
            true
        }
    }

    val groupedComplications = remember(complicacoesFiltradas) {
        complicacoesFiltradas.groupBy { it.name.trim().lowercase().semAcentos() }
    }

    val uniqueComplications = remember(groupedComplications) {
        groupedComplications.values.mapNotNull { group ->
            group.sortedBy { if (it.origem == "BASICO") 0 else 1 }.firstOrNull()
        }.sortedBy { it.name }
    }

    SectionCard(
        title    = "Complicações",
        icon     = Icons.Default.Warning,
        showHeader = false
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val totalPc = state.pontosComplicacao
            val usadosPc = state.pontosComplicacaoGastos
            val livresPc = (totalPc - usadosPc).coerceAtLeast(0)

            // --- FIXED HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SectionHeader(
                    onHelpClick          = null,
                    centerText           = "Pontos Complicação: livres $livresPc / $totalPc",
                    onCenterClick        = null,
                    onListaCompletaClick = null,
                    listaCompletaText    = ""
                )

                if (showTempError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        tempErrorMsg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // --- STICKY SELECTED COMPLICATIONS ---
                if (state.complicacoesSelecionadas.isNotEmpty() || state.transtornos.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement   = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(8.dp)
                            .alpha(if (locked && !state.modoProgressaoAtivo) 0.6f else 1f)
                    ) {
                        // Standard Complications
                        state.complicacoesSelecionadas
                            .filterValues { it != null }
                            .forEach { (comp, tipo) ->
                                val isAuto = comp.id.keyify() in autoBaseKeys
                                val isYoungAuto = comp.id == "pequeno" && state.jovemAutoPequeno
                                // val cost = if (tipo == "Maior") 2 else 1
                                val isClickable = !locked && !isAuto && !isYoungAuto

                                AssistChip(
                                    onClick = {
                                        if (!isClickable) return@AssistChip

                                        val (pode, msg) = state.podeRemoverComplicacao(comp, tipo)
                                        if (!pode) {
                                            if (msg != null && msg.contains("Pontos em uso")) {
                                                complicationToRemove = comp
                                                showPcInUseDialog = true
                                            } else {
                                                tempErrorMsg = msg ?: "Ação bloqueada."
                                                showTempError = true
                                                scope.launch {
                                                    delay(3000)
                                                    showTempError = false
                                                }
                                            }
                                            return@AssistChip
                                        }

                                        onUserFeedback()
                                        performRemoval(state, comp, feedbackMessages, onLogFeedback, complicacoesFiltradas)
                                    },
                                    enabled = isClickable,
                                    label = { Text("${comp.name} ($tipo)", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remover ${comp.name}",
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

                        // PROMPT 3: Display Transtornos
                        state.transtornos.forEach { transtorno ->
                            AssistChip(
                                onClick = {
                                    state.transtornos.remove(transtorno)
                                    onUserFeedback()
                                },
                                label = { Text("${transtorno.name} (T.)", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remover Transtorno",
                                        modifier = Modifier.size(12.dp)
                                    )
                                },
                                modifier = Modifier.height(24.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                ExpandableSearchFilter(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    isExpanded = isSearchExpanded,
                    onExpandedChange = { isSearchExpanded = it },
                    onClear = {
                        searchQuery = ""
                        selectedSeverity = "Todos"
                    },
                    placeholder = "Pesquisar Complicações..."
                ) {
                    // Filters UI inside search expansion
                    if (!locked) {
                        Spacer(Modifier.height(8.dp))

                        Column {
                            // Severity Filter
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                item {
                                    Text(
                                        "Gravidade:",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                items(listOf("Todos", "Menor", "Maior")) { type ->
                                    FilterChip(
                                        selected = selectedSeverity == type,
                                        onClick = { selectedSeverity = type },
                                        label = { Text(type) }
                                    )
                                }
                            }
                        }
                    }
                }
            } // End Fixed Header

            Spacer(Modifier.height(4.dp))

            // --- SCROLLABLE CONTENT ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Dynamic Text: Only show if no complications selected
                if (state.complicacoesSelecionadas.isEmpty()) {
                    item {
                        Text(
                            "Escolha Complicações para ganhar Pontos Bônus de Criação.\nUse-os em Atributos, Perícias, Vantagens ou Equipamento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    // PROMPT 3: Add Disorder Button
                    if (state.modoProgressaoAtivo && state.compendioHorrorAtivo) {
                        TextButton(
                            onClick = { showAddTranstornoDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Adicionar Transtorno (Gratuito)")
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                item {
                    // Dialogs Logic
                    if (showPcInUseDialog && complicationToRemove != null) {
                        val comp = complicationToRemove!!
                        val tipo = state.complicacoesSelecionadas[comp]
                        val costToRemove = if (tipo == "Maior") 2 else 1

                        val paCount = state.cpPaStack.size
                        val spCount = state.cpSpStack.size
                        val pvCount = state.cpPvStack.size
                        val recCount = state.cpRecursosStack.size

                        // We need to free up 'costToRemove' points.
                        // Currently free: (totalPc - usadosPc) but totalPc will decrease by costToRemove upon removal.
                        // Actually, totalPc is calculated FROM complications.
                        // If I remove comp, totalPc decreases.
                        // I need (newTotalPc - usadosPc) >= 0.
                        // So I need to reduce 'usadosPc' by at least (usadosPc - newTotalPc).
                        // Deficit = usadosPc - (currentTotalPc - costToRemove).
                        // Since I can't remove yet, I need to reduce 'usadosPc' by Deficit.
                        // Deficit is basically 'costToRemove' (assuming 0 free currently).
                        // If I have 1 free, and remove Major (2), I have deficit of 1.

                        val deficit = (state.pontosComplicacaoGastos - (state.pontosComplicacao - costToRemove)).coerceAtLeast(0)

                        AlertDialog(
                            onDismissRequest = {
                                showPcInUseDialog = false
                                complicationToRemove = null
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPcInUseDialog = false
                                    complicationToRemove = null
                                }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("Pontos em uso: Devolva $deficit ponto(s)") },
                            text = {
                                Column {
                                    Text("Para remover ${comp.name} ($tipo), você precisa devolver recursos comprados com Pontos Bônus.")
                                    Spacer(Modifier.height(8.dp))

                                    if (paCount > 0) {
                                        val label = if (costToRemove <= 2) "Devolver e Remover" else "Devolver 1"
                                        // Attribute gives 2 BP. Always enough for Minor or Major.
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Atributos (Gasto: $paCount)")
                                            TextButton(onClick = {
                                                state.devolverPcDeAtributo()
                                                // Check if deficit covered
                                                val newDeficit = (state.pontosComplicacaoGastos - (state.pontosComplicacao - costToRemove)).coerceAtLeast(0)
                                                if (newDeficit == 0) {
                                                    performRemoval(state, comp, feedbackMessages, onLogFeedback, complicacoesFiltradas)
                                                    showPcInUseDialog = false
                                                    complicationToRemove = null
                                                }
                                            }) {
                                                Text("Devolver (+2 BP)")
                                            }
                                        }
                                    }
                                    if (spCount > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Perícias (Gasto: $spCount)")
                                            TextButton(onClick = {
                                                state.devolverPcDePericia()
                                                val newDeficit = (state.pontosComplicacaoGastos - (state.pontosComplicacao - costToRemove)).coerceAtLeast(0)
                                                if (newDeficit == 0) {
                                                    performRemoval(state, comp, feedbackMessages, onLogFeedback, complicacoesFiltradas)
                                                    showPcInUseDialog = false
                                                    complicationToRemove = null
                                                }
                                            }) {
                                                Text("Devolver (+1 BP)")
                                            }
                                        }
                                    }
                                    if (pvCount > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Vantagens (Gasto: $pvCount)")
                                            TextButton(onClick = {
                                                state.devolverPcDeVantagem()
                                                val newDeficit = (state.pontosComplicacaoGastos - (state.pontosComplicacao - costToRemove)).coerceAtLeast(0)
                                                if (newDeficit == 0) {
                                                    performRemoval(state, comp, feedbackMessages, onLogFeedback, complicacoesFiltradas)
                                                    showPcInUseDialog = false
                                                    complicationToRemove = null
                                                }
                                            }) {
                                                Text("Devolver (+2 BP)")
                                            }
                                        }
                                    }
                                    if (recCount > 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Recursos (Gasto: $recCount)")
                                            TextButton(onClick = {
                                                state.devolverPcDeRecursos()
                                                val newDeficit = (state.pontosComplicacaoGastos - (state.pontosComplicacao - costToRemove)).coerceAtLeast(0)
                                                if (newDeficit == 0) {
                                                    performRemoval(state, comp, feedbackMessages, onLogFeedback, complicacoesFiltradas)
                                                    showPcInUseDialog = false
                                                    complicationToRemove = null
                                                }
                                            }) {
                                                Text("Devolver (+1 BP)")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    // PROMPT 3: Add Disorder Dialog
                    if (showAddTranstornoDialog) {
                        val available = uniqueComplications.filter {
                            !state.complicacoesSelecionadas.containsKey(it) && !state.transtornos.contains(
                                it
                            )
                        }.map { it.name.toSentenceCase() }.sorted()

                        ChoiceDialog(
                            options = available,
                            onConfirm = { choice ->
                                val selected =
                                    uniqueComplications.firstOrNull { it.name.toSentenceCase() == choice }
                                if (selected != null) {
                                    state.transtornos.add(selected)
                                    onUserFeedback()
                                    onLogFeedback("Transtorno ${selected.name} adicionado.")
                                }
                                showAddTranstornoDialog = false
                            },
                            onDismiss = { showAddTranstornoDialog = false }
                        )
                    }
                }

                if (!locked) {
                    val pequComp = uniqueComplications.firstOrNull { it.id == "pequeno" }
                    val listaParaMostrar = uniqueComplications
                        .filter { comp ->
                            comp.id.keyify() !in autoBaseKeys
                        }

                    items(
                        items = listaParaMostrar,
                        key = { it.id }
                    ) { comp ->
                        // Using key so state is preserved
                        ComplicacaoItem(
                            comp = comp,
                            state = state,
                            locked = locked,
                            allowLongTexts = allowLongTexts,
                            showOfficialNames = showOfficialNames,
                            groupedComplications = groupedComplications,
                            detalhesExpandidos = detalhesExpandidos,
                            onUserFeedback = onUserFeedback,
                            onLogFeedback = onLogFeedback,
                            onError = { msg ->
                                if (msg.contains("Pontos em uso")) {
                                    complicationToRemove = comp
                                    showPcInUseDialog = true
                                } else {
                                    tempErrorMsg = msg
                                    showTempError = true
                                    scope.launch {
                                        delay(2_000)
                                        showTempError = false
                                    }
                                }
                            },
                            peqComp = pequComp
                        )
                    }
                }
            } // End LazyColumn
        }
    }
}

private fun performRemoval(
    state: CriadorState,
    comp: Complicacao,
    feedbackMessages: MutableList<String>,
    onLogFeedback: (String) -> Unit,
    allComps: List<Complicacao>
) {
    when (comp.id) {
        "idoso" -> {
            state.complicacoesSelecionadas.remove(comp)
            state.idosoBonusSp = 0
            state.syncFromCPRefund(
                sp = true,
                feedbackMessages = feedbackMessages
            )
        }

        "jovem" -> {
            val pequComp = allComps.firstOrNull { it.id == "pequeno" }
            if (pequComp != null) {
                state.removeYoung(pequComp)
            }
            state.complicacoesSelecionadas.remove(comp)
        }

        "pobreza" -> {
            state.complicacoesSelecionadas.remove(comp)
            if (state.compendioPathfinderAtivo) {
                state.dinheiro += 15000
            } else if (state.compendioFantasiaAtivo) {
                state.dinheiro += 150
            } else {
                state.dinheiro += 250
            }
            state.checkAndRefundResourcePb()
        }

        "obeso" -> {
            state.complicacoesSelecionadas.remove(comp)
            state.obesoBonusSize = 0
            state.obesoMalusMov = 0
        }

        else -> {
            state.complicacoesSelecionadas.remove(comp)
        }
    }
    onLogFeedback("Complicação ${comp.name} removida.")
}

@Composable
private fun ComplicacaoItem(
    comp: Complicacao,
    state: CriadorState,
    locked: Boolean,
    allowLongTexts: Boolean,
    showOfficialNames: Boolean,
    groupedComplications: Map<String, List<Complicacao>>,
    detalhesExpandidos: MutableMap<String, Boolean>,
    onUserFeedback: () -> Unit,
    onLogFeedback: (String) -> Unit,
    onError: (String) -> Unit,
    peqComp: Complicacao?
) {
    // MERGED DESCRIPTION LOGIC
    val mergedDescription = remember(comp, groupedComplications, allowLongTexts) {
        if (allowLongTexts) {
            val group = groupedComplications[comp.name.trim().lowercase().semAcentos()] ?: listOf(comp)
            val showMerged = group.size > 1 && EditionConfig.isFullEdition

            if (showMerged) {
                group.sortedBy { if (it.origem == "BASICO") 0 else 1 }
                    .joinToString("\n\n") { v ->
                        val tag = when(v.origem) {
                            "BASICO" -> "BÁSICO"
                            "SUPER" -> "SUPERS"
                            "FANTASIA" -> "FANTASIA"
                            "HORROR" -> "HORROR"
                            "PATHFINDER" -> "BUSCATRILHA"
                            else -> v.origem.toEditionDisplayName().uppercase()
                        }
                        val txt = if (showOfficialNames && !v.originalDescription.isNullOrBlank())
                            v.originalDescription.trim()
                        else
                            v.description.trim()
                        "[$tag] $txt"
                    }
            } else {
                if (showOfficialNames && !comp.originalDescription.isNullOrBlank())
                    comp.originalDescription.trim()
                else
                    comp.description.trim()
            }
        } else ""
    }

    val cur    = state.complicacoesSelecionadas[comp]
    val sevRaw = comp.severity.lowercase().trim()
    val menorOnly = sevRaw.contains("menor") && !sevRaw.contains("maior")
    val maiorOnly = sevRaw.contains("maior") && !sevRaw.contains("menor")
    val ambos     = sevRaw.contains("menor") && sevRaw.contains("maior")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (showOfficialNames && !comp.originalName.isNullOrBlank()) comp.originalName.toSentenceCase() else comp.name.toSentenceCase(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (menorOnly || ambos) {
                    val enabledMenor = !locked && cur == null
                    TextButton(
                        onClick = {
                            if (!enabledMenor) return@TextButton
                            val (pode, msg) = state.podeSelecionarComplicacao(comp)
                            if (!pode) {
                                onError(msg ?: "Requisito não atendido.")
                                return@TextButton
                            }

                            val conflitoMsg = state.mensagemConflitoParaComplicacao(comp)
                            if (conflitoMsg != null) {
                                onError(conflitoMsg)
                                return@TextButton
                            }
                            onUserFeedback()
                            state.adicionarComplicacao(comp, "Menor")
                            onLogFeedback("Complicação ${comp.name} (Menor) adicionada.")
                        },
                        enabled = enabledMenor
                    ) {
                        Text("Menor")
                    }
                }

                if (maiorOnly || ambos) {
                    val enabledMaior = !locked && (
                            (maiorOnly && cur == null) ||
                                    (ambos && cur == "Menor")
                            )
                    TextButton(
                        onClick = {
                            if (!enabledMaior) return@TextButton
                            val (pode, msg) = state.podeSelecionarComplicacao(comp)
                            if (!pode) {
                                onError(msg ?: "Requisito não atendido.")
                                return@TextButton
                            }

                            val conflitoMsg = state.mensagemConflitoParaComplicacao(comp)
                            if (conflitoMsg != null) {
                                onError(conflitoMsg)
                                return@TextButton
                            }
                            onUserFeedback()
                            state.adicionarComplicacao(comp, "Maior")
                            onLogFeedback("Complicação ${comp.name} (Maior) adicionada.")
                        },
                        enabled = enabledMaior
                    ) {
                        Text("Maior")
                    }
                }
            }
        }

        if (allowLongTexts && mergedDescription.isNotBlank()) {
            TextButton(
                onClick = {
                    onUserFeedback()
                    val current = detalhesExpandidos[comp.id] ?: false
                    detalhesExpandidos[comp.id] = !current
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    if (detalhesExpandidos[comp.id] == true) "Ocultar detalhes" else "Ver detalhes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(visible = detalhesExpandidos[comp.id] == true) {
                Text(
                    text = mergedDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun TransparentOutlinedReadOnlyField(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    BasicTextField(
        value         = text,
        onValueChange = {},
        enabled       = enabled,
        readOnly      = true,
        textStyle     = LocalTextStyle.current.copy(color = colors.onSurface),
        singleLine    = true,
        modifier      = modifier,
        decorationBox = { inner ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Transparent, shape = MaterialTheme.shapes.small)
                    .border(
                        width = 1.dp,
                        color = if (enabled)
                            colors.outline.copy(alpha = 0.8f)
                        else
                            colors.outline.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clickable(enabled = enabled, onClick = onClick)
            ) {
                inner()
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector        = Icons.Default.ArrowDropDown,
                    contentDescription = "",
                    tint = if (enabled)
                        colors.onSurface
                    else
                        colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    )
}
