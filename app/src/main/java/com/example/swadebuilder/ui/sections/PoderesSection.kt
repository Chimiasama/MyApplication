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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.util.semAcentos

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

            // HEADER
            item(key = "header_$arcKey") {
                Column {
                    SectionHeader(
                        onHelpClick = null,
                        centerText = centerText,
                        onCenterClick = null,
                        onListaCompletaClick = null,
                        listaCompletaText = ""
                    )
                    HorizontalDivider(thickness = 1.dp)
                }
            }

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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Slots: $slotsCount", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            slots.forEachIndexed { idx, poderId ->
                                val label = poderId ?: "— vazio —"
                                val isSlotLocked = locked || idx < lockedCount
                                AssistChip(
                                    onClick = {
                                        if (!isSlotLocked && poderId != null) {
                                            slots[idx] = null
                                            state.syncPoderesSelecionadosFromSlots()
                                            state.manifestacoesPoderes.remove(poderId)
                                        }
                                    },
                                    label = { Text("${idx + 1}: $label") },
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
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .alpha(if (selecionado) 0.45f else 1f)
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
                    Column(Modifier.padding(12.dp)) {
                        Text(poder.nome, fontWeight = FontWeight.Bold)
                        Text("Custo: ${poder.pontosDePoder}")
                        if (state.usarSemPontosDePoder) {
                            Text("Penalidade base: ${custoParaPenalidadeTexto(poder.pontosDePoder)}")
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
                            Spacer(Modifier.height(6.dp))
                            TextButton(onClick = { expanded = !expanded }) {
                                Text(
                                    if (expanded) "Ocultar detalhes" else "Ver detalhes",
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(Modifier.padding(top = 4.dp)) {
                                    if (poder.descricao.isNotBlank()) {
                                        Text(poder.descricao)
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    Text("Distância: ${poder.distancia}")
                                    Text("Duração: ${poder.duracao}")

                                    if (manifestacoesDisponiveis.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Manifestações:", fontWeight = FontWeight.SemiBold)
                                        manifestacoesDisponiveis.forEach { man ->
                                            Text("• $man", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }

                                    if (modificadoresDisponiveis.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text("Modificadores:", fontWeight = FontWeight.SemiBold)
                                        modificadoresDisponiveis.forEach { mod ->
                                            Text(
                                                "${mod.nome} (${mod.custo}): ${mod.descricao}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (selecionado) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.manifestacoesPoderes[poder.id].orEmpty(),
                                onValueChange = { value ->
                                    if (value.isBlank()) {
                                        state.manifestacoesPoderes.remove(poder.id)
                                    } else {
                                        state.manifestacoesPoderes[poder.id] = value
                                    }
                                },
                                label = { Text("Manifestação/Aparência") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Divider between AB sections (except after last)
            if (arcKeyRaw != displayKeys.last()) {
                item {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
