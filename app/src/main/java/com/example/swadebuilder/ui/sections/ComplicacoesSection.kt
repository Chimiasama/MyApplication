@file:Suppress("DEPRECATION")
@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.example.swadebuilder.ui.sections

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.model.Complicacao
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

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ComplicacoesSection(
    state: CriadorState,
    feedbackMessages: MutableList<String>,
    onUserFeedback: () -> Unit
) {
    val locked = state.criacaoBasicaCongelada

    var showPcInUseDialog by rememberSaveable { mutableStateOf(false) }
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

    val origensAtivas: Set<String> = buildSet {
        add("BASICO")
        if (state.modoSupers) add("SUPER")
        if (state.compendioFantasiaAtivo) add("FANTASIA")
        if (state.compendioHorrorAtivo) add("HORROR")
        if (state.compendioBuscatrilhaAtivo) add("FANTASIABUSCATRILHA")
        if (state.compendioDeadlandsAtivo) add("OESTE_ESTRANHO")
        if (state.compendioArteDaGuerraAtivo) add("ARTE_DA_GUERRA")
        if (state.compendioCidadeSolVaporAtivo) add("CIDADE_SOL_VAPOR")
        if (state.compendioWiseguysAtivo) add("WISEGUYS")
        if (state.compendioCrystalHeartAtivo) add("CRYSTAL_HEART")
    }

    // Filter states
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSeverity by rememberSaveable { mutableStateOf("Todos") }
    var selectedOrigin by rememberSaveable { mutableStateOf("Todos") }

    val complicacoesFiltradas = listaComplicacoes.filter { comp ->
        val origemSafe = if (comp.origem.isBlank()) "BASICO" else comp.origem.uppercase().semAcentos().trim()
        val matchesActiveOrigin = origemSafe in origensAtivas

        if (!matchesActiveOrigin) return@filter false

        // Filter Logic
        val matchesSeverity = when (selectedSeverity) {
            "Todos" -> true
            "Menor" -> comp.severity.lowercase().contains("menor")
            "Maior" -> comp.severity.lowercase().contains("maior")
            else -> true
        }

        val matchesOriginFilter = if (selectedOrigin == "Todos") true else origemSafe == selectedOrigin

        if (!matchesSeverity || !matchesOriginFilter) return@filter false

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
        val totalPc = state.pontosComplicacao
        val usadosPc = state.pontosComplicacaoGastos
        val livresPc = (totalPc - usadosPc).coerceAtLeast(0)

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

        Spacer(Modifier.height(4.dp))

        ExpandableSearchFilter(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            isExpanded = isSearchExpanded,
            onExpandedChange = { isSearchExpanded = it },
            placeholder = "Pesquisar Complicações..."
        ) {
            // Filters UI inside search expansion
            if (!locked) {
                Spacer(Modifier.height(8.dp))

                Column {
                    // Severity Filter
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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

                    // Origin Filter (if multiple active)
                    if (origensAtivas.size > 1) {
                        Spacer(Modifier.height(4.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                Text(
                                    "Livro:",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedOrigin == "Todos",
                                    onClick = { selectedOrigin = "Todos" },
                                    label = { Text("Todos") }
                                )
                            }
                            items(origensAtivas.toList().sorted()) { origin ->
                                val label = when(origin) {
                                    "BASICO" -> "Básico"
                                    "SUPER" -> "Supers"
                                    "FANTASIA" -> "Fantasia"
                                    "HORROR" -> "Horror"
                                    "FANTASIABUSCATRILHA" -> androidx.compose.ui.res.stringResource(R.string.sw_pathfinder_label)
                                    else -> origin.toEditionDisplayName()
                                }
                                FilterChip(
                                    selected = selectedOrigin == origin,
                                    onClick = { selectedOrigin = origin },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (totalPc == 0) {
            Text(
                "Escolha Complicações para ganhar Pontos Bônus de Criação.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "Use seus Pontos Bônus de Complicação nas seções de Atributos, Perícias, Vantagens ou Equipamento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp),
            modifier = Modifier.alpha(if (locked && !state.modoProgressaoAtivo) 0.3f else 1f)
        ) {
            // Standard Complications
            state.complicacoesSelecionadas
                .filterValues { it != null }
                .forEach { (comp, tipo) ->
                    val isAuto = comp.id.keyify() in autoBaseKeys
                    val isYoungAuto = comp.id == "pequeno" && state.jovemAutoPequeno
                    val cost = if (tipo == "Maior") 2 else 1
                    val canRemove = !locked &&
                            !isAuto &&
                            !isYoungAuto &&
                            state.pontosComplicacaoGastos <= state.pontosComplicacao - cost

                    val isClickable = !locked && !isAuto && !isYoungAuto

                    AssistChip(
                        onClick = {
                            if (!isClickable) return@AssistChip
                            onUserFeedback()

                            if (canRemove) {
                                when (comp.id) {
                                    "idoso" -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.idosoBonusSp = 0
                                        state.syncFromCPRefund(sp = true, feedbackMessages = feedbackMessages)
                                    }
                                    "jovem" -> {
                                        val pequComp = complicacoesFiltradas.firstOrNull { it.id == "pequeno" }
                                        if (pequComp != null) {
                                            state.removeYoung(pequComp)
                                        }
                                        state.complicacoesSelecionadas.remove(comp)
                                    }
                                    "pobreza" -> {
                                        state.complicacoesSelecionadas.remove(comp)
                                        state.dinheiro += 250
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
                            } else {
                                showPcInUseDialog = true
                            }
                        },
                        enabled     = isClickable,
                        label       = { Text("${comp.name} ($tipo)") },
                        leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover ${comp.name}") }
                    )
                }

            // PROMPT 3: Display Transtornos
            state.transtornos.forEach { transtorno ->
                AssistChip(
                    onClick = {
                        state.transtornos.remove(transtorno)
                        onUserFeedback()
                    },
                    label = { Text("${transtorno.name} (Transtorno)") },
                    leadingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover Transtorno") },
                    colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (showPcInUseDialog) {
            val paCount  = state.cpPaStack.size
            val spCount  = state.cpSpStack.size
            val pvCount  = state.cpPvStack.size
            val recCount = state.cpRecursosStack.size
            val totalUsados = paCount + spCount + pvCount + recCount

            AlertDialog(
                onDismissRequest = { showPcInUseDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPcInUseDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Não é possível remover esta Complicação") },
                text = {
                    Column {
                        Text("Você já converteu Pontos Bônus de Criação vindos de Complicações em:")
                        Spacer(Modifier.height(8.dp))

                        if (totalUsados == 0) {
                            Text("Nenhum Ponto Bônus está em uso no momento.")
                        } else {
                            if (paCount > 0) Text("• $paCount em Atributos adicionais")
                            if (spCount > 0) Text("• $spCount em passos extras de Perícia")
                            if (pvCount > 0) Text("• $pvCount em Vantagens adicionais")
                            if (recCount > 0) Text("• $recCount em Recursos extras (500$ cada)")

                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Para remover esta Complicação, primeiro desfaça alguns " +
                                        "desses Pontos Bônus nas seções de Atributos, Perícias, " +
                                        "Vantagens ou Equipamento.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
        }

        // PROMPT 3: Add Disorder Dialog
        if (showAddTranstornoDialog) {
            val available = uniqueComplications.filter {
                !state.complicacoesSelecionadas.containsKey(it) && !state.transtornos.contains(it)
            }.map { it.name.toSentenceCase() }.sorted()

            ChoiceDialog(
                options = available,
                onConfirm = { choice ->
                    val selected = uniqueComplications.firstOrNull { it.name.toSentenceCase() == choice }
                    if (selected != null) {
                        state.transtornos.add(selected)
                        onUserFeedback()
                    }
                    showAddTranstornoDialog = false
                },
                onDismiss = { showAddTranstornoDialog = false }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Only show list if not locked (Creation phase)
        if (!locked) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val pequComp = uniqueComplications.firstOrNull { it.id == "pequeno" }

                val listaParaMostrar = uniqueComplications
                    .filter { comp ->
                        comp.id.keyify() !in autoBaseKeys
                    }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(listaParaMostrar) { comp ->
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
                                                  "FANTASIABUSCATRILHA" -> "BUSCATRILHA"
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
                                                val conflitoMsg = state.mensagemConflitoParaComplicacao(comp)
                                                if (conflitoMsg != null) {
                                                    tempErrorMsg = conflitoMsg
                                                    showTempError = true
                                                    scope.launch {
                                                        delay(2_000)
                                                        showTempError = false
                                                    }
                                                    return@TextButton
                                                }
                                                onUserFeedback()
                                                when (comp.id) {
                                                    "jovem" -> {
                                                        state.complicacoesSelecionadas[comp] = "Menor"
                                                        state.applyYoungMinor()
                                                    }
                                                    "obeso" -> {
                                                        state.complicacoesSelecionadas[comp] = "Menor"
                                                        state.obesoBonusSize = 1
                                                        state.obesoMalusMov = 1
                                                    }
                                                    else -> {
                                                        state.complicacoesSelecionadas[comp] = "Menor"
                                                    }
                                                }
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
                                                val conflitoMsg = state.mensagemConflitoParaComplicacao(comp)
                                                if (conflitoMsg != null) {
                                                    tempErrorMsg = conflitoMsg
                                                    showTempError = true
                                                    scope.launch {
                                                        delay(2_000)
                                                        showTempError = false
                                                    }
                                                    return@TextButton
                                                }
                                                onUserFeedback()
                                                when (comp.id) {
                                                    "idoso" -> {
                                                        state.complicacoesSelecionadas[comp] = "Maior"
                                                        state.idosoBonusSp = 5
                                                        state.rebuildAllPericiaStacks()
                                                    }
                                                    "jovem" -> {
                                                        if (pequComp != null) {
                                                            state.complicacoesSelecionadas[comp] = "Maior"
                                                            state.applyYoungMajor(pequComp)
                                                        }
                                                    }
                                                    "obeso" -> {
                                                        state.complicacoesSelecionadas[comp] = "Maior"
                                                        state.obesoBonusSize = 1
                                                        state.obesoMalusMov = 1
                                                    }
                                                    else -> {
                                                        state.complicacoesSelecionadas[comp] = "Maior"
                                                    }
                                                }
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
                }
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
