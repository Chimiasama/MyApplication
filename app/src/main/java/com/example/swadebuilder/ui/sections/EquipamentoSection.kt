package com.example.swadebuilder.ui.sections

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.R
import com.example.swadebuilder.model.EquipFilter
import com.example.swadebuilder.model.EquipSuperType
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.SAVAGE_PATHFINDER_ALLOWLIST
import com.example.swadebuilder.model.getActiveOrigins
import com.example.swadebuilder.ui.components.CollapsibleSection
import com.example.swadebuilder.ui.components.ExpandableSearchFilter
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.components.StandardEquipamentoItem
import com.example.swadebuilder.util.MoneyUtils
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
import com.example.swadebuilder.util.toSentenceCase
import kotlinx.serialization.json.JsonPrimitive

// --- Data Structures for Refactoring ---

private data class MappedCategory(
    val original: EquipamentoCategoria,
    val superType: EquipSuperType,
    val group: String,      // Major subsection (e.g., "Ataque a Distância")
    val subGroup: String    // Minor subsection (e.g., "Pólvora Negra", "Medievais")
)

private fun mapCategory(cat: EquipamentoCategoria): MappedCategory {
    val t = (cat.tipo ?: "").trim().uppercase().semAcentos()
    val st = (cat.subtipo ?: "").trim().uppercase().semAcentos()

    // 1. Identify SuperType
    val superType = when {
        t.startsWith("CIBERNETIC") -> EquipSuperType.CIBERNETICO
        t.startsWith("MECHA") -> EquipSuperType.MECHA
        t == "ROBO" || t == "VEICULOS" || t == "VEICULO" -> EquipSuperType.VEICULOS
        t == "ARMADURA ENERGIZADA" || t == "ARMADURA_ENERGIZADA" -> EquipSuperType.ARMADURA_ENERGIZADA
        t.contains("ARMADURA") -> EquipSuperType.ARMADURAS
        t == "ESCUDOS" -> EquipSuperType.ARMADURAS
        t.contains("ARMA") || t == "ARTE_DA_GUERRA" -> EquipSuperType.ARMAS
        t == "EQUIPAMENTO SUPERS" && st == "VEICULOS" -> EquipSuperType.VEICULOS
        else -> EquipSuperType.GERAL
    }

    // 2. Identify Groups and SubGroups
    var group = t.toFancyTitleCase()
    var subGroup = st.toFancyTitleCase()

    when (superType) {
        EquipSuperType.ARMAS -> {
            when {
                t == "ARMAS PESSOAIS" || t == "ARMAS CORPO A CORPO" || t == "ARMAS MEDIEVAIS" -> {
                    group = "Corpo a Corpo"
                }
                t == "ARMAS DE COMBATE À DISTÂNCIA" -> {
                    group = "Ataque a Distância"
                }
                t == "ARMAS DE PÓLVORA NEGRA" -> {
                    group = "Pólvora Negra"
                }
                t == "ARMAS DE FOGO" -> {
                    group = "Ataque a Distância"
                }
                t == "ARMAS DE AR" || t == "ARMAS A VAPOR" -> {
                    group = "Ataque a Distância"
                    subGroup = st.toFancyTitleCase()
                }
                t == "ARMAS DE ENERGIA" -> {
                    group = "Ataque a Distância"
                    subGroup = "Energia Futurista"
                }
                t == "ARMAS ESPECIAIS" -> {
                    group = "Armas Pesadas e Especiais"
                }
                st.contains("EXPLOSIV") || st.contains("GRANADA") || st.contains("MÍSS") -> {
                    group = "Explosivos e Pesadas"
                }
                else -> {
                    // Default fallback
                    if (t == "ARMAS") group = "Geral" // Crystal Heart
                }
            }
        }
        EquipSuperType.ARMADURAS -> {
             if (t == "ESCUDOS") {
                 group = "Escudos"
             } else {
                 group = "Armaduras Corporais"
             }
        }
        EquipSuperType.ARMADURA_ENERGIZADA -> {
            group = "Armaduras Energizadas"
        }
        EquipSuperType.VEICULOS -> {
            if (t == "ROBO") {
                group = "Robôs"
            } else {
                group = "Veículos"
            }
        }
        EquipSuperType.MECHA -> {
            group = "Mechas"
        }
        EquipSuperType.CIBERNETICO -> {
            group = "Implantes"
        }
        EquipSuperType.GERAL -> {
             if (t == "MUNIÇÃO") {
                 group = "Munição"
             } else if (t == "ACESSÓRIOS") {
                 group = "Acessórios"
             } else {
                 group = "Itens Gerais"
             }
        }
    }

    return MappedCategory(cat, superType, group, subGroup)
}


private data class EquipamentoListEntry(
    val item: EquipamentoItem,
    val origemKey: String
)

@Composable
fun EquipFilterDialog(
    availableSuperTypes: List<EquipSuperType>,
    availableSubSections: Map<EquipSuperType, Set<String>>,
    current: EquipFilter,
    onChange: (EquipFilter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtros Avançados") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = current.somenteAcessiveis,
                        onCheckedChange = {
                            onChange(current.copy(somenteAcessiveis = it))
                        }
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Somente acessíveis")
                }
                Spacer(Modifier.size(8.dp))

                Text("Categorias e Subseções", fontWeight = FontWeight.Bold)
                availableSuperTypes.forEach { t ->
                    // SuperType Header/Checkbox
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = t in current.superTipos,
                            onCheckedChange = {
                                val s = current.superTipos.toMutableSet()
                                if (it) s += t else s -= t
                                onChange(current.copy(superTipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(t.label, fontWeight = FontWeight.SemiBold)
                    }

                    // SubSections List (only if SuperType has subsections)
                    val subSecs = availableSubSections[t]?.sorted() ?: emptyList()
                    if (subSecs.isNotEmpty()) {
                        Column(modifier = Modifier.padding(start = 32.dp)) {
                            subSecs.forEach { sub ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = sub in current.subSections,
                                        onCheckedChange = {
                                            val s = current.subSections.toMutableSet()
                                            if (it) s += sub else s -= sub
                                            onChange(current.copy(subSections = s))
                                        }
                                    )
                                    Spacer(Modifier.size(4.dp))
                                    Text(sub, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipamentoSection(
    state: CriadorState, // Changed signature to include state
    dinheiro: Int,
    requisicao: Int,
    usaRiqueza: Boolean,
    usaRequisicao: Boolean,
    dadoRiqueza: Int,
    pcTotal: Int,
    pcLivres: Int,
    recursosPcUsados: Int,
    emProgresso: Boolean,
    modoProgressaoAtivo: Boolean,
    onUsarPontosBonusEmRecursos: () -> Unit,
    onDesfazerPontosBonusEmRecursos: () -> Unit,
    onEquipamentoDoubleClick: (EquipamentoItem) -> Unit,
    equipamentosComprados: List<EquipamentoItem>,
    onRemoveEquipamentoClick: (EquipamentoItem) -> Unit,
    categorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    tensaoTotal: Int,
    tensaoLimite: Int,
    mechaSlotsTotal: Int,
    isPersonagemRobotico: Boolean,
    forcaRaw: Int,
    hasMusculoso: Boolean,
    hasSoldado: Boolean,
    soldadoCargaAtivo: Boolean,
    onEditarDinheiro: (Int) -> Unit,
    onEditarRequisicao: (Int) -> Unit,
    onToggleSoldadoCarga: () -> Unit,
    compendioFantasiaAtivo: Boolean = false,
    compendioHorrorAtivo: Boolean = false,
    compendioSciFiAtivo: Boolean = false,
    compendioPathfinderAtivo: Boolean = false,
    compendioDeadlandsAtivo: Boolean = false,
    compendioArteDaGuerraAtivo: Boolean = false,
    compendioCidadeSolVaporAtivo: Boolean = false,
    compendioWiseguysAtivo: Boolean = false,
    compendioCrystalHeartAtivo: Boolean = false,
    modoOficialAtivo: Boolean = false,
    onUserFeedback: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showMoneyDialog by rememberSaveable { mutableStateOf(false) }
    var dinheiroInput by rememberSaveable { mutableStateOf(dinheiro.toString()) }
    var showRequisicaoDialog by rememberSaveable { mutableStateOf(false) }
    var requisicaoInput by rememberSaveable { mutableStateOf(requisicao.toString()) }

    // Replaced local state with CriadorState properties
    val filter = state.equipFilter
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    // UI State for Search & Filters
    val searchQuery = state.equipSearchQuery
    // We now filter by SuperType primarily in the horizontal scroll
    val selectedSuperTypes = state.equipSelectedSuperTypes

    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val usePbWalletRedesign = booleanResource(R.bool.enable_pb_wallet_redesign)
    val showOfficialNames = EditionConfig.isFullEdition && modoOficialAtivo
    val isSearching = searchQuery.isNotBlank()

    // Accordion State for browse mode (keyed by SuperType label)
    val expandedTypeMap = state.equipExpandedTypes

    // Pre-calculate allowed keys set for performance
    val pathfinderAllowedKeys = remember(compendioPathfinderAtivo) {
        if (compendioPathfinderAtivo) {
            SAVAGE_PATHFINDER_ALLOWLIST.map { it.keyify() }.toSet()
        } else {
            emptySet()
        }
    }

    SectionCard(
        title    = "Equipamento",
        icon     = Icons.Default.ShoppingCart,
        showHeader = false
    ) {
        val containerModifier = if (isSearching) {
            Modifier
        } else {
            Modifier.verticalScroll(rememberScrollState())
        }

        Column(modifier = containerModifier) {
            // 1. Prepare Data
            val activeOrigins = state.getActiveOrigins()
            val esconderSupers = superequipCategorias.isEmpty()
            val rawCategories = (categorias + superequipCategorias)
                .filterNot {
                    esconderSupers && (
                            it.tipo.equals("Equipamento Supers", true) ||
                                    it.tipo.equals("Equipamentos Supers", true)
                            )
                }
                .filter { categoria ->
                    val origem = categoria.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                    if (origem == "SUPLEMENTO") false
                    else if (compendioFantasiaAtivo && origem == "BASICO") false
                    else if (state.compendioScifiMechasCiberneticosAtivo && origem == "SCI_FI") true
                    else origem in activeOrigins
                }

            // Mapped Data
            val mappedCategories = remember(rawCategories) {
                rawCategories.map { mapCategory(it) }
            }


            // Helper function for filtering logic
            fun isItemAllowedByPathfinderRule(item: EquipamentoItem, origemKey: String): Boolean {
                if (!compendioPathfinderAtivo) return true
                // If the item is explicitly from Pathfinder module, allow it
                if (origemKey == "PATHFINDER" || origemKey == "PATHFINDER") return true

                // Allow Sci-Fi items if the Mecha rule is active (Explicit exception)
                // Includes "SUPER" because some sci-fi equipment files might have mixed origins
                if (state.compendioScifiMechasCiberneticosAtivo && (origemKey == "SCI_FI" || origemKey == "SUPER")) return true

                // If item is from BASE, strictly enforce AllowList
                if (origemKey == "BASICO") {
                    // Check if name is in AllowList (normalized)
                    val nameKey = item.nome.keyify()
                    return nameKey in pathfinderAllowedKeys
                }

                // Default DENY for everything else (Sci-Fi, Horror, WW2, etc.)
                // This ensures strict mode really hides tanks, lasers, etc.
                return false
            }

            // Available SuperTypes for Chips (Filtered by Pathfinder rules if active)
            val availableSuperTypes = remember(mappedCategories, compendioPathfinderAtivo) {
                mappedCategories
                    .filter { mapped ->
                        // Filter categories that contain at least one valid item for the current mode
                        mapped.original.itens.any { item ->
                             val origemKey = (item.origem?.ifBlank { mapped.original.origem ?: "BASICO" } ?: (mapped.original.origem ?: "BASICO")).uppercase()
                             isItemAllowedByPathfinderRule(item, origemKey)
                        }
                    }
                    .map { it.superType }
                    .distinct()
                    .sortedBy { it.order }
            }

            // Available SubSections Map for Dialog
            val availableSubSectionsMap = remember(mappedCategories) {
                mappedCategories
                    .groupBy { it.superType }
                    .mapValues { (_, categories) ->
                        categories.map { it.subGroup }.filter { it.isNotBlank() }.toSet()
                    }
            }


            // 2. Header (Money)
            val moneyDisplayText = if (usaRequisicao) {
                "Requisição: $requisicao"
            } else if (usaRiqueza) {
                "Riqueza: d$dadoRiqueza"
            } else if (compendioPathfinderAtivo) {
                "Dinheiro: ${state.carteiraPathfinder["PL"] ?: 0} pl  ${state.carteiraPathfinder["PO"] ?: 0} po  ${state.carteiraPathfinder["PP"] ?: 0} pp  ${state.carteiraPathfinder["PC"] ?: 0} pc"
            } else {
                "Dinheiro: ${MoneyUtils.formatCurrency(dinheiro, false)}"
            }

            SectionHeader(
                onHelpClick = null,
                centerText = moneyDisplayText,
                onCenterClick = null,
                onListaCompletaClick = null,
                listaCompletaText = ""
            )

            if (usaRequisicao && (emProgresso || modoProgressaoAtivo)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        requisicaoInput = requisicao.toString()
                        showRequisicaoDialog = true
                    }) {
                        Text("Editar requisição")
                    }
                }
            } else if (!usaRiqueza && (emProgresso || modoProgressaoAtivo)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        dinheiroInput = dinheiro.toString()
                        showMoneyDialog = true
                    }) {
                        Text("Editar dinheiro")
                    }
                }
            }

            Spacer(modifier = Modifier.size(4.dp))

            if (usaRequisicao) {
                Text(
                    text = "A Requisição é apenas uma referência. Combine com o mestre quais itens estão disponíveis.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Legacy PB Buttons Removed Here

            // 3. Search & Filter UI
            var isSearchExpanded by rememberSaveable { mutableStateOf(false) }

            ExpandableSearchFilter(
                query = searchQuery,
                onQueryChange = { state.equipSearchQuery = it },
                isExpanded = isSearchExpanded,
                onExpandedChange = { isSearchExpanded = it },
                onClear = {
                    state.equipSearchQuery = ""
                    state.equipFilter = EquipFilter()
                    state.equipSelectedSuperTypes.clear()
                    state.equipSectionFilters.clear()
                },
                placeholder = "Pesquisar Equipamentos..."
            ) {
                Spacer(Modifier.size(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item(key = "advanced_filters", contentType = "filter_chip") {
                        FilterChip(
                            selected = !filter.isEmpty(),
                            onClick = { showFilterDialog = true },
                            label = { Text("Filtros${if(!filter.isEmpty()) " (!)" else ""}") }
                        )
                    }

                    items(availableSuperTypes, key = { it.name }, contentType = { "type_chip" }) { type ->
                        FilterChip(
                            selected = type in selectedSuperTypes,
                            onClick = {
                                if (type in selectedSuperTypes) selectedSuperTypes.remove(type)
                                else selectedSuperTypes.add(type)
                            },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            Spacer(Modifier.padding(vertical = 4.dp))

            // 4. Purchased Items / Weight Banner (Unchanged Logic)
            if (equipamentosComprados.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    equipamentosComprados.forEach { eq ->
                        val isLocked = eq.origemGrant != null && !modoProgressaoAtivo
                        val chipColors = if (isLocked) {
                            androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                leadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        } else {
                            androidx.compose.material3.AssistChipDefaults.assistChipColors()
                        }

                        AssistChip(
                            onClick = { onRemoveEquipamentoClick(eq) },
                            label = { Text(eq.nome.toFancyTitleCase()) },
                            leadingIcon = {
                                Icon(
                                    if (isLocked) Icons.Default.Close else Icons.Default.Close,
                                    contentDescription = "Remover"
                                )
                            },
                            colors = chipColors
                        )
                    }
                }
                Spacer(Modifier.padding(vertical = 4.dp))
            }

            val totalWeight = equipamentosComprados
            .mapNotNull { item ->
                (item.peso as? JsonPrimitive)?.content?.replace(",", ".")?.toFloatOrNull()
                }
                .sum()
            val effectiveStrength = if (hasSoldado && soldadoCargaAtivo) {
                if (forcaRaw < 12) forcaRaw + 2 else forcaRaw + 1
            } else {
                forcaRaw
            }
            val baseLimit = ((effectiveStrength - 2) / 2) * 10f
            val limit = baseLimit + if (hasMusculoso) 10f else 0f

            val tensaoExcedida = tensaoTotal > tensaoLimite
            val tensaoLabel = if (isPersonagemRobotico) "Mods (Robô)" else "Tensão (Ciber)"
            val tensaoColor = if (tensaoExcedida) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            // Determine visibility of Tensão/Mecha Slots
            val showSciFiStats = state.compendioScifiMechasCiberneticosAtivo || isPersonagemRobotico

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Peso: ${"%.1f".format(totalWeight)} / ${"%.1f".format(limit)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Column(horizontalAlignment = Alignment.End) {
                    if (showSciFiStats) {
                        Text(
                            "$tensaoLabel: $tensaoTotal/$tensaoLimite",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tensaoColor
                        )
                    }
                    if (compendioSciFiAtivo && mechaSlotsTotal > 0) {
                        Text(
                            "Slots Mecha/Veículo usados: $mechaSlotsTotal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (hasSoldado) {
                        AssistChip(
                            onClick = onToggleSoldadoCarga,
                            label = {
                                Text(
                                    if (soldadoCargaAtivo) "Soldado (+1)" else "Soldado (Off)"
                                )
                            }
                        )
                    }
                }
            }

            if (showSciFiStats && tensaoExcedida) {
                // ... Error Text ...
                Text(
                    "Sobrecarga Cibernética: Personagem recebe o estado Fatigado (ou Exausto se X > Y+2).",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.padding(vertical = 4.dp))

            // 5. List Content
            if (isSearching) {
                // Flat List Mode
                val finalFlatList = remember(mappedCategories, filter, selectedSuperTypes, searchQuery, dinheiro, usaRiqueza, usaRequisicao, compendioPathfinderAtivo) {
                    mappedCategories.filter { mapped ->
                        // Filter Check (removed Origin logic)
                        if (filter.superTipos.isNotEmpty() && mapped.superType !in filter.superTipos) return@filter false
                        if (selectedSuperTypes.isNotEmpty() && mapped.superType !in selectedSuperTypes) return@filter false

                        // SubSection Filter
                        if (filter.subSections.isNotEmpty() && mapped.subGroup !in filter.subSections) return@filter false

                        true
                    }.flatMap { mapped ->
                        mapped.original.itens.filter { item ->
                            if (filter.somenteAcessiveis) {
                                val c = MoneyUtils.parseCostInBaseUnit(item.custo, compendioPathfinderAtivo)
                                if (!usaRiqueza && !usaRequisicao && c > dinheiro) return@filter false
                            }
                            val q = searchQuery.semAcentos().lowercase()
                            val n = item.nomeExibicao.semAcentos().lowercase()
                            val original = item.nome.semAcentos().lowercase()
                            (n.contains(q) || original.contains(q))
                        }.map { item ->
                            val origemKey = (item.origem?.ifBlank { mapped.original.origem ?: "BASICO" } ?: (mapped.original.origem ?: "BASICO")).uppercase()
                            EquipamentoListEntry(item, origemKey)
                        }
                    }.filter { entry ->
                        // Strict Pathfinder Filter
                        if (!isItemAllowedByPathfinderRule(entry.item, entry.origemKey)) return@filter false
                        true
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    if (finalFlatList.isEmpty()) {
                        item(key = "empty_list", contentType = "message") { Text("Nenhum equipamento encontrado.", modifier = Modifier.padding(8.dp)) }
                    } else {
                        items(finalFlatList, key = { it.item.nome + it.hashCode() }, contentType = { "equip_item" }) { entry ->
                            StandardEquipamentoItem(
                                equipamento = entry.item,
                                onClick = { onEquipamentoDoubleClick(entry.item) },
                                allowLongTexts = allowLongTexts,
                                showOriginalName = showOfficialNames,
                                showTensao = compendioSciFiAtivo
                            )
                        }
                    }
                }

            } else {
                // Browse Mode (Categorized)
                // 1. Group by SuperType
                val groupsBySuperType = remember(mappedCategories) {
                    mappedCategories.groupBy { it.superType }
                }

                // --- SOLUÇÃO DEFINITIVA: Pré-calcular os dados filtrados ---
                // Added state.compendioScifiMechasCiberneticosAtivo to keys to ensure refresh when rule is toggled
                val visibleContentData = remember(groupsBySuperType, filter, usaRiqueza, usaRequisicao, dinheiro, compendioPathfinderAtivo, state.compendioScifiMechasCiberneticosAtivo) {
                    // Mapeia cada SuperType para seus dados filtrados
                    groupsBySuperType.mapValues { (_, categoriesInSuper) ->
                        // Apply SubSection Filter at Category Level
                        val filteredCategories = if (filter.subSections.isNotEmpty()) {
                            categoriesInSuper.filter { it.subGroup in filter.subSections }
                        } else {
                            categoriesInSuper
                        }

                        // Process the subgroups directly to check for content
                        val groups = filteredCategories.groupBy { it.group }.mapValues { (_, catsInGroup) ->
                            catsInGroup.groupBy { it.subGroup }.mapValues { (_, catsInSub) ->
                                // Filtra os itens dentro do subgrupo
                                catsInSub.flatMap { cat ->
                                    cat.original.itens.map { item ->
                                        val origemKey = (item.origem?.ifBlank { cat.original.origem ?: "BASICO" }
                                            ?: (cat.original.origem ?: "BASICO")).uppercase()
                                        EquipamentoListEntry(item, origemKey)
                                    }
                                    }
                                    .groupBy { it.item.nome.keyify() }
                                    .map { (_, duplicates) ->
                                        duplicates.maxByOrNull { CriadorState.getOriginPriority(it.origemKey) }!!
                                    }
                                    .filter { entry ->
                                    val isAcessivel = if (filter.somenteAcessiveis) {
                                        val c = MoneyUtils.parseCostInBaseUnit(entry.item.custo, compendioPathfinderAtivo)
                                        usaRiqueza || usaRequisicao || c <= dinheiro
                                    } else {
                                        true
                                    }

                                    // Strict Pathfinder Filter
                                    val isAllowedByPathfinder = isItemAllowedByPathfinderRule(entry.item, entry.origemKey)

                                    isAcessivel && isAllowedByPathfinder
                                }.sortedBy { it.item.nome }
                            }.filter { it.value.isNotEmpty() } // Remove subgrupos vazios
                        }.filter { it.value.isNotEmpty() } // Remove grupos vazios

                        if (groups.isEmpty()) null else groups
                    }
                }

                Column(Modifier.padding(horizontal = 4.dp)) {
                    // Itera sobre os SuperTypes na ordem definida
                    EquipSuperType.entries.sortedBy { it.order }.forEach { superType ->
                        // Filter out MECHA, CIBERNETICO and ARMADURA_ENERGIZADA if rule is not active
                        if (superType == EquipSuperType.MECHA || superType == EquipSuperType.CIBERNETICO || superType == EquipSuperType.ARMADURA_ENERGIZADA) {
                            if (!state.compendioScifiMechasCiberneticosAtivo) return@forEach
                        }

                        // Pula se o supertipo não estiver selecionado ou não tiver conteúdo visível
                        if (selectedSuperTypes.isNotEmpty() && superType !in selectedSuperTypes) return@forEach
                        if (filter.superTipos.isNotEmpty() && superType !in filter.superTipos) return@forEach

                        val groupData = visibleContentData[superType] ?: return@forEach
                        if (groupData.isEmpty()) return@forEach // Double check empty groups

                        val isExpanded = expandedTypeMap[superType.label] ?: false
                        CollapsibleSection(
                            title = superType.label,
                            expanded = isExpanded,
                            onToggle = { expandedTypeMap[superType.label] = !isExpanded },
                            onToggleFeedback = onUserFeedback
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 8.dp).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val availableGroups = groupData.keys.sorted()
                                val activeFilters = state.equipSectionFilters[superType] ?: emptySet()

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        FilterChip(
                                            selected = activeFilters.isEmpty(),
                                            onClick = {
                                                state.equipSectionFilters[superType] = emptySet()
                                            },
                                            label = { Text("Todos") }
                                        )
                                    }
                                    items(availableGroups) { gName ->
                                        val isSelected = gName in activeFilters
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                val newSet = activeFilters.toMutableSet()
                                                if (isSelected) newSet.remove(gName) else newSet.add(gName)
                                                state.equipSectionFilters[superType] = newSet
                                            },
                                            label = { Text(gName) }
                                        )
                                    }
                                }

                                val filteredGroupKeys = if (activeFilters.isEmpty()) availableGroups else availableGroups.filter { it in activeFilters }

                                // Itera sobre os dados pré-calculados
                                filteredGroupKeys.forEach { groupName ->
                                    val subGroups = groupData[groupName]!!
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                    )

                                    subGroups.keys.sorted().forEach { subGroupName ->
                                        if (subGroupName != groupName && subGroupName.isNotBlank()) {
                                            Text(
                                                text = subGroupName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp, start = 4.dp)
                                            )
                                        }

                                        val itemsInSub = subGroups[subGroupName]!!
                                        itemsInSub.forEach { entry ->
                                            StandardEquipamentoItem(
                                                equipamento = entry.item,
                                                onClick = { onEquipamentoDoubleClick(entry.item) },
                                                allowLongTexts = allowLongTexts,
                                                showOriginalName = showOfficialNames,
                                                showTensao = compendioSciFiAtivo
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            if (showMoneyDialog) {
                if (compendioPathfinderAtivo) {
                    // Pathfinder Multi-Currency Dialog
                    var plInput by remember { mutableStateOf((state.carteiraPathfinder["PL"] ?: 0).toString()) }
                    var poInput by remember { mutableStateOf((state.carteiraPathfinder["PO"] ?: 0).toString()) }
                    var ppInput by remember { mutableStateOf((state.carteiraPathfinder["PP"] ?: 0).toString()) }
                    var pcInput by remember { mutableStateOf((state.carteiraPathfinder["PC"] ?: 0).toString()) }
                    var otimizar by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { showMoneyDialog = false },
                        title = { Text("Editar moedas (Pathfinder)") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = plInput,
                                        onValueChange = { plInput = it.filter { c -> c.isDigit() } },
                                        label = { Text("Platina") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = poInput,
                                        onValueChange = { poInput = it.filter { c -> c.isDigit() } },
                                        label = { Text("Ouro") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = ppInput,
                                        onValueChange = { ppInput = it.filter { c -> c.isDigit() } },
                                        label = { Text("Prata") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pcInput,
                                        onValueChange = { pcInput = it.filter { c -> c.isDigit() } },
                                        label = { Text("Cobre") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = otimizar,
                                        onCheckedChange = { otimizar = it }
                                    )
                                    Spacer(Modifier.size(8.dp))
                                    Text("Otimizar moedas")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                state.carteiraPathfinder["PL"] = plInput.toIntOrNull() ?: 0
                                state.carteiraPathfinder["PO"] = poInput.toIntOrNull() ?: 0
                                state.carteiraPathfinder["PP"] = ppInput.toIntOrNull() ?: 0
                                state.carteiraPathfinder["PC"] = pcInput.toIntOrNull() ?: 0

                                if (otimizar) {
                                    state.compactPathfinderMoney()
                                } else {
                                    state.updateTotalPathfinderMoney()
                                }
                                showMoneyDialog = false
                            }) {
                                Text("Salvar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showMoneyDialog = false }) { Text("Cancelar") }
                        }
                    )
                } else {
                    // Standard Dialog
                    AlertDialog(
                        onDismissRequest = { showMoneyDialog = false },
                        title = { Text("Editar dinheiro") },
                        text = {
                            OutlinedTextField(
                                value = dinheiroInput,
                                onValueChange = { novo ->
                                    dinheiroInput = novo.filter { it.isDigit() || it == '-' }
                                },
                                label = { Text("Valor em dinheiro") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val novoValor = dinheiroInput.toIntOrNull()
                                if (novoValor != null) {
                                    onEditarDinheiro(novoValor)
                                }
                                showMoneyDialog = false
                            }) {
                                Text("Salvar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showMoneyDialog = false }) { Text("Cancelar") }
                        }
                    )
                }
            }
            if (showRequisicaoDialog) {
                AlertDialog(
                    onDismissRequest = { showRequisicaoDialog = false },
                    title = { Text("Editar requisição") },
                    text = {
                        OutlinedTextField(
                            value = requisicaoInput,
                            onValueChange = { novo ->
                                requisicaoInput = novo.filter { it.isDigit() || it == '-' }
                            },
                            label = { Text("Valor de requisição") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val novoValor = requisicaoInput.toIntOrNull()
                            if (novoValor != null) {
                                onEditarRequisicao(novoValor)
                            }
                            showRequisicaoDialog = false
                        }) {
                            Text("Salvar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRequisicaoDialog = false }) { Text("Cancelar") }
                    }
                )
            }
            if (showFilterDialog) {
                EquipFilterDialog(
                    availableSuperTypes = availableSuperTypes,
                    availableSubSections = availableSubSectionsMap,
                    current = filter,
                    onChange = { state.equipFilter = it },
                    onDismiss = { showFilterDialog = false }
                )
            }
        }
    }
}
