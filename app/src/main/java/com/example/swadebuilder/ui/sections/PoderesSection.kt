package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.R
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.criacaoBasicaCongeladaComXp
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.loadJsonAsset
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.normAAKey
import com.example.swadebuilder.toArcanoKey
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.titleCase
import com.example.swadebuilder.util.toSentenceCase

private fun custoParaPenalidadeTexto(custo: String): String {
    val clean = custo.trim()
    clean.toIntOrNull()?.let { base -> return "-${(base + 1) / 2}" }
    if (clean.contains("/")) {
        return clean.split("/").joinToString("/") { p ->
            p.replace("+", "").trim().toIntOrNull()?.let { "-${(it + 1) / 2}" } ?: "—"
        }
    }
    if (clean.endsWith("+")) clean.removeSuffix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}+" }
    if (clean.startsWith("+")) clean.removePrefix("+").toIntOrNull()?.let { return "-${(it + 1) / 2}" }
    return "—"
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PoderesSection(
    state: CriadorState
) {
    val context = LocalContext.current
    val allowLongTexts = booleanResource(R.bool.enable_long_texts)

    val locked = state.criacaoBasicaCongeladaComXp

    // Identify active Arcane Backgrounds
    val arcanosAtivos = remember(state.vantagensSelecionadas) {
        state.vantagensSelecionadas.mapNotNull { it.toArcanoKey() }.distinct()
    }

    if (arcanosAtivos.isEmpty()) return

    // Ensure slot lists are initialized and resized correctly for all active backgrounds
    LaunchedEffect(arcanosAtivos, state.vantagensSelecionadas.size, state.tropoSelecionado) {
        arcanosAtivos.forEach { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()
            val slotsCount = state.getSlotsCountForArcano(arcKey)
            val existente = state.poderSlotsPorArcano[arcKey]

            if (existente == null) {
                val nova = mutableStateListOf<String?>().apply { repeat(slotsCount) { add(null) } }
                state.poderSlotsPorArcano[arcKey] = nova
            } else {
                if (existente.size < slotsCount) {
                    while (existente.size < slotsCount) { existente.add(null) }
                } else if (existente.size > slotsCount) {
                    while (existente.size > slotsCount) { existente.removeLast() }
                }
            }
        }
    }

    val allPoderes: List<Poder> by androidx.compose.runtime.produceState(initialValue = emptyList()) {
        val poderesBase = runCatching { context.loadJsonAsset<List<Poder>>("poderes.json") }.getOrElse { emptyList() }
        val tecnicasChi = runCatching { context.loadJsonAsset<List<Poder>>("tecnicas_chi.json") }.getOrElse { emptyList() }
        value = poderesBase + tecnicasChi
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedRank by rememberSaveable { mutableStateOf("Todos") }

    // Track expanded state for each AB section. Default to true (expanded).
    // Using remember instead of rememberSaveable to avoid crash with Map serialization.
    val sectionStates = remember { mutableStateMapOf<String, Boolean>() }

    val poderesElegiveis = remember(allPoderes, searchQuery, selectedRank) {
        allPoderes.filter { power ->
            val matchSearch = if (searchQuery.isBlank()) true else {
                power.nome.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true) ||
                power.descricao.semAcentos().contains(searchQuery.semAcentos(), ignoreCase = true)
            }

            val matchRank = if (selectedRank == "Todos") true else {
                power.estagio.semAcentos().equals(selectedRank.semAcentos(), ignoreCase = true)
            }

            matchSearch && matchRank
        }
    }

    // Determine which ABs to display
    val displayKeys = if (!state.permiteMultiAntecedenteArcano) {
        listOf(arcanosAtivos.first())
    } else {
        arcanosAtivos
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- FILTERS ---
        item {
            ExpandableSearchFilter(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                isExpanded = isSearchExpanded,
                onExpandedChange = { isSearchExpanded = it },
                placeholder = "Pesquisar Poderes..."
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedRank == "Todos",
                        onClick = { selectedRank = "Todos" },
                        label = { Text("Todos") }
                    )
                }
                items(listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendario")) { rank ->
                    FilterChip(
                        selected = selectedRank == rank,
                        onClick = { selectedRank = rank },
                        label = { Text(rank) }
                    )
                }
            }
        }

        // --- ARCANE BACKGROUND SECTIONS ---
        displayKeys.forEach { arcKeyRaw ->
            val arcKey = arcKeyRaw.normAAKey()
            val baseInfo = arcanoInfo[arcKey] ?: Triple(0, 0, "—")
            val ppTotal = baseInfo.second
            val foco = baseInfo.third
            val slotsCount = state.getSlotsCountForArcano(arcKey)

            val centerText = if (state.usarSemPontosDePoder) {
                "Teste $foco = -(custo/2)"
            } else {
                "PP: $ppTotal  •  $foco"
            }

            // Treat null as true (default expanded)
            val isExpanded = sectionStates[arcKey] ?: true

            // HEADER (Custom Collapsible)
            item(key = "header_$arcKey") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Toggle state. If missing (true), become false. If present, negate.
                                sectionStates[arcKey] = !isExpanded
                            }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = centerText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(thickness = 1.dp)
                }
            }

            if (isExpanded) {
                // SLOTS PANEL
                item(key = "slots_$arcKey") {
                    val slots = state.poderSlotsPorArcano[arcKey] ?: remember { mutableStateListOf() }
                    // Lock logic
                    val lockedCount = if (state.mostrandoPoderesProgresso && state.arcanoEmCompraViaXpKey == arcKey)
                        state.arcanoSnapshotAntesDaCompra?.size ?: 0
                    else 0

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Column(Modifier.padding(8.dp)) {
                            Text("Slots: $slotsCount", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                slots.forEachIndexed { idx, poderId ->
                                    val label = poderId?.toSentenceCase() ?: "— vazio —"
                                    val isSlotLocked = locked || idx < lockedCount
                                    AssistChip(
                                        onClick = {
                                            if (!isSlotLocked && poderId != null) {
                                                slots[idx] = null
                                                state.syncPoderesSelecionadosFromSlots()
                                                state.manifestacoesPoderes.remove(poderId)
                                            }
                                        },
                                        label = { Text("${idx + 1}: $label", style = MaterialTheme.typography.bodySmall) },
                                        enabled = !isSlotLocked && poderId != null
                                    )
                                }
                            }
                        }
                    }
                }

                // POWERS LIST
                items(
                    items = poderesElegiveis,
                    key = { "${arcKey}_${it.id}" } // Unique key per AB + Power
                ) { poder ->
                    val slots = state.poderSlotsPorArcano[arcKey] ?: remember { mutableStateListOf() }
                    val selecionado = slots.any { it?.equals(poder.id, ignoreCase = true) == true }
                    val lockedCount = if (state.mostrandoPoderesProgresso && state.arcanoEmCompraViaXpKey == arcKey)
                        state.arcanoSnapshotAntesDaCompra?.size ?: 0
                    else 0

                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp) // Reduced padding
                            .alpha(if (selecionado) 0.6f else 1f) // Increased opacity for selected for better visibility
                            .clickable(enabled = !locked) {
                                if (selecionado) {
                                    val idx = slots.indexOfFirst { it?.equals(poder.id, ignoreCase = true) == true }
                                    if (idx >= 0 && idx >= lockedCount) {
                                        slots[idx] = null
                                        state.syncPoderesSelecionadosFromSlots()
                                        state.manifestacoesPoderes.remove(poder.id)
                                    }
                                } else {
                                    val firstEmpty = slots.indexOfFirst { it == null }
                                    if (firstEmpty >= 0 && firstEmpty >= lockedCount) {
                                        slots[firstEmpty] = poder.id
                                        state.syncPoderesSelecionadosFromSlots()
                                    }
                                }
                            }
                    ) {
                        Column(Modifier.padding(8.dp)) { // Compact internal padding
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(poder.nome.toSentenceCase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("PP: ${poder.pontosDePoder}", style = MaterialTheme.typography.bodySmall)
                            }

                            if (state.usarSemPontosDePoder) {
                                Text("Penalidade base: ${custoParaPenalidadeTexto(poder.pontosDePoder)}", style = MaterialTheme.typography.bodySmall)
                            }

                            val manifestacoesDisponiveis = poder.manifestacoes.filter { it.isNotBlank() }
                            val modificadoresDisponiveis = poder.modificadores.filter { mod ->
                                mod.nome.isNotBlank() || mod.descricao.isNotBlank()
                            }

                            val detalhesDisponiveis = allowLongTexts && (
                                poder.descricao.isNotBlank() ||
                                    manifestacoesDisponiveis.isNotEmpty() ||
                                    modificadoresDisponiveis.isNotEmpty()
                            )

                            if (detalhesDisponiveis) {
                                Spacer(Modifier.height(4.dp))
                                TextButton(
                                    onClick = { expanded = !expanded },
                                    modifier = Modifier.height(24.dp), // Reduce button height
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text(
                                        if (expanded) "Ocultar detalhes" else "Ver detalhes",
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                AnimatedVisibility(visible = expanded) {
                                    Column(Modifier.padding(top = 4.dp)) {
                                        if (poder.descricao.isNotBlank()) {
                                            Text(poder.descricao, style = MaterialTheme.typography.bodySmall)
                                            Spacer(Modifier.height(4.dp))
                                        }

                                        Text("Distância: ${poder.distancia}", style = MaterialTheme.typography.labelSmall)
                                        Text("Duração: ${poder.duracao}", style = MaterialTheme.typography.labelSmall)

                                        if (manifestacoesDisponiveis.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text("Manifestações:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                                            manifestacoesDisponiveis.forEach { man ->
                                                Text("• $man", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        if (modificadoresDisponiveis.isNotEmpty()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text("Modificadores:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall)
                                            modificadoresDisponiveis.forEach { mod ->
                                                Text(
                                                    "${mod.nome} (${mod.custo}): ${mod.descricao}",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (selecionado) {
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = state.manifestacoesPoderes[poder.id].orEmpty(),
                                    onValueChange = { value ->
                                        if (value.isBlank()) {
                                            state.manifestacoesPoderes.remove(poder.id)
                                        } else {
                                            state.manifestacoesPoderes[poder.id] = value
                                        }
                                    },
                                    label = { Text("Manifestação/Aparência", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Divider between AB sections (except after last)
            if (arcKeyRaw != displayKeys.last()) {
                item {
                    Spacer(Modifier.height(16.dp)) // Slightly reduced spacer
                }
            }
        }
    }
}
