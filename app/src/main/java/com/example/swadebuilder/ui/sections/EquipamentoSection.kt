package com.example.swadebuilder.ui.sections

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.R
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.ui.components.PbWalletBanner
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SectionHeader
import com.example.swadebuilder.ui.components.SearchTextField
import com.example.swadebuilder.ui.components.StandardEquipamentoItem
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.swadebuilder.ui.sections.toResumo
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class EquipFilter(
    val somenteAcessiveis: Boolean = false,
    val origens: Set<String> = emptySet(),
    val tipos: Set<String> = emptySet(),
    val subtipos: Set<String> = emptySet()
) {
    fun totalSelections() =
        (if (somenteAcessiveis) 1 else 0) +
                origens.size + tipos.size + subtipos.size

    fun isEmpty() = totalSelections() == 0
}

@Composable
fun EquipFilterDialog(
    allOrigens: List<String>,
    allTipos: List<String>,
    allSubtipos: List<String>,
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
                    .heightIn(max = 300.dp)
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

                Text("Tipo", fontWeight = FontWeight.Bold)
                allTipos.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = t in current.tipos,
                            onCheckedChange = {
                                val s = current.tipos.toMutableSet()
                                if (it) s += t else s -= t
                                onChange(current.copy(tipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(t)
                    }
                }
                Spacer(Modifier.size(8.dp))

                Text("Subtipo", fontWeight = FontWeight.Bold)
                allSubtipos.forEach { st ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = st in current.subtipos,
                            onCheckedChange = {
                                val s = current.subtipos.toMutableSet()
                                if (it) s += st else s -= st
                                onChange(current.copy(subtipos = s))
                            }
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(st)
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
    dinheiro: Int,
    usaRiqueza: Boolean,
    dadoRiqueza: Int,
    pcTotal: Int,
    pcLivres: Int,
    recursosPcUsados: Int,
    emProgresso: Boolean,
    modoProgressaoAtivo: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUsarPontosBonusEmRecursos: () -> Unit,
    onDesfazerPontosBonusEmRecursos: () -> Unit,
    onEquipamentoDoubleClick: (EquipamentoItem) -> Unit,
    equipamentosComprados: List<EquipamentoItem>,
    onRemoveEquipamentoClick: (EquipamentoItem) -> Unit,
    categorias: List<EquipamentoCategoria>,
    superequipCategorias: List<EquipamentoCategoria>,
    tensaoTotal: Int,
    tensaoLimite: Int,
    isPersonagemRobotico: Boolean,
    forcaRaw: Int,
    hasMusculoso: Boolean,
    hasSoldado: Boolean,
    soldadoCargaAtivo: Boolean,
    onEditarDinheiro: (Int) -> Unit,
    onToggleSoldadoCarga: () -> Unit,
    compendioFantasiaAtivo: Boolean = false,
    compendioHorrorAtivo: Boolean = false,
    compendioSciFiAtivo: Boolean = false,
    compendioBuscatrilhaAtivo: Boolean = false,
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

    var filter by remember { mutableStateOf(EquipFilter()) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    // UI State for Search & Filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypes = remember { mutableStateListOf<String>() }

    val allowLongTexts = booleanResource(R.bool.enable_long_texts)
    val detalhesExpandidos = remember { mutableStateMapOf<String, Boolean>() }
    val showOfficialNames = EditionConfig.isFullEdition && modoOficialAtivo

    // Accordion State for browse mode
    // Map of Category Type -> Expanded
    val expandedTypeMap = remember { mutableStateMapOf<String, Boolean>() }

    SectionCard(
        title    = "Equipamento",
        expanded = expanded,
        onToggle = onToggle,
        icon     = Icons.Default.ShoppingCart,
        onToggleFeedback = onUserFeedback
    ) {
        if (!expanded) return@SectionCard

        // 1. Prepare Data
        val esconderSupers = superequipCategorias.isEmpty()
        val allCategorias = (categorias + superequipCategorias)
            .filterNot {
                esconderSupers && (
                        it.tipo.equals("Equipamento Supers", true) ||
                                it.tipo.equals("Equipamentos Supers", true)
                        )
            }
            .filter { categoria ->
                val origem = categoria.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                (origem != "ARTE_DA_GUERRA" || compendioArteDaGuerraAtivo) &&
                        (origem != "CIDADE_SOL_VAPOR" || compendioCidadeSolVaporAtivo) &&
                        (origem != "WISEGUYS" || compendioWiseguysAtivo) &&
                        (origem != "CRYSTAL_HEART" || compendioCrystalHeartAtivo) &&
                        (origem != "FANTASIA" || compendioFantasiaAtivo) &&
                        (origem != "HORROR" || compendioHorrorAtivo) &&
                        (origem != "SCI_FI" || compendioSciFiAtivo) &&
                        (origem != "FANTASIABUSCATRILHA" || compendioBuscatrilhaAtivo) &&
                        (origem != "DEADLANDS" || compendioDeadlandsAtivo)
            }

        // 2. Header (Money)
        SectionHeader(
            onHelpClick = null,
            centerText = if (usaRiqueza) "Riqueza: d$dadoRiqueza" else "Dinheiro: $dinheiro",
            onCenterClick = null,
            onListaCompletaClick = null,
            listaCompletaText = ""
        )

        if (!usaRiqueza && (emProgresso || modoProgressaoAtivo)) {
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

        if (!emProgresso) {
            PbWalletBanner(
                pcTotal = pcTotal,
                pcLivres = pcLivres,
                spendLabel = "Usar PB em Recursos",
                refundLabel = "Desfazer uso de PB",
                spendEnabled = pcLivres > 0 && recursosPcUsados == 0,
                refundEnabled = recursosPcUsados > 0,
                onSpend = onUsarPontosBonusEmRecursos,
                onRefund = onDesfazerPontosBonusEmRecursos
            )
            Spacer(Modifier.size(8.dp))
        }

        // 3. Search & Filter UI
        SearchTextField(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.size(8.dp))

        // Dynamic Categories for Chips
        val availableTypes = remember(allCategorias) {
            allCategorias.map { it.tipo }.distinct().sorted()
        }
        val availableSubtypesByType = remember(allCategorias) {
            allCategorias.groupBy { it.tipo }.mapValues { (_, cats) ->
                cats.map { it.subtipo }.distinct().sorted()
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = !filter.isEmpty(),
                    onClick = { showFilterDialog = true },
                    label = { Text("Filtros Avançados${if(!filter.isEmpty()) " (!)" else ""}") }
                )
            }

            // --- Origin Mini Cards (Chips) ---
            if (compendioFantasiaAtivo) {
                item {
                    val label = "Medievais"
                    val key = "FANTASIA"
                    FilterChip(
                        selected = key in filter.origens,
                        onClick = {
                            val newSet = if (key in filter.origens) filter.origens - key else filter.origens + key
                            filter = filter.copy(origens = newSet)
                        },
                        label = { Text(label) }
                    )
                }
            }

            if (compendioSciFiAtivo) {
                item {
                    val label = "Futuristas"
                    val key = "SCI_FI"
                    FilterChip(
                        selected = key in filter.origens,
                        onClick = {
                            val newSet = if (key in filter.origens) filter.origens - key else filter.origens + key
                            filter = filter.copy(origens = newSet)
                        },
                        label = { Text(label) }
                    )
                }
            }

            // Add "Modernas" (Basic) if any other compendium is active to allow filtering it out
            if (compendioFantasiaAtivo || compendioSciFiAtivo || compendioHorrorAtivo || compendioDeadlandsAtivo) {
                item {
                    val label = "Modernas"
                    val key = "BASICO"
                    FilterChip(
                        selected = key in filter.origens,
                        onClick = {
                            val newSet = if (key in filter.origens) filter.origens - key else filter.origens + key
                            filter = filter.copy(origens = newSet)
                        },
                        label = { Text(label) }
                    )
                }
            }

            items(availableTypes) { type ->
                FilterChip(
                    selected = type in selectedTypes,
                    onClick = {
                        if (type in selectedTypes) selectedTypes.remove(type)
                        else selectedTypes.add(type)

                        if (filter.subtipos.isNotEmpty()) {
                            filter = filter.copy(subtipos = emptySet())
                        }
                    },
                    label = { Text(type) }
                )
            }
        }

        if (selectedTypes.isNotEmpty() || filter.subtipos.isNotEmpty()) {
            Spacer(Modifier.size(8.dp))
            val subtypesForSelection = if (selectedTypes.isNotEmpty()) {
                selectedTypes
                    .flatMap { type -> availableSubtypesByType[type].orEmpty() }
            } else {
                allCategorias.map { it.subtipo }
            }
                .distinct()
                .sorted()

            if (subtypesForSelection.isNotEmpty()) {
                Text(
                    "Filtrar subcategorias:",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subtypesForSelection.forEach { subtype ->
                        FilterChip(
                            selected = subtype in filter.subtipos,
                            onClick = {
                                val newSet = filter.subtipos.toMutableSet()
                                if (subtype in newSet) newSet.remove(subtype) else newSet.add(subtype)
                                filter = filter.copy(subtipos = newSet)
                            },
                            modifier = Modifier.heightIn(min = 30.dp),
                            label = { Text(subtype, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
                Spacer(Modifier.size(8.dp))
            }
        }

        if (showFilterDialog) {
            val allTipos = allCategorias.map { it.tipo }.distinct()
            val allSubtipos = allCategorias.map { it.subtipo }.distinct()
            val allOrigens = allCategorias
                .map { it.origem?.ifBlank { "BASICO" } ?: "BASICO" }
                .map { it.uppercase() }
                .distinct()

            EquipFilterDialog(
                allOrigens = allOrigens,
                allTipos = allTipos,
                allSubtipos = allSubtipos,
                current = filter,
                onChange = { filter = it },
                onDismiss = { showFilterDialog = false }
            )
        }

        Spacer(Modifier.padding(vertical = 4.dp))

        // 4. Purchased Items
        if (equipamentosComprados.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                equipamentosComprados.forEach { eq ->
                    AssistChip(
                        onClick = { onRemoveEquipamentoClick(eq) },
                        label = { Text(eq.nome) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remover"
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }

        // 5. Weight Calculation
        val totalWeight = equipamentosComprados
            .mapNotNull {
                (it.peso as? JsonPrimitive)?.content?.replace(",", ".")?.toFloatOrNull()
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
        val tensaoLabel = if (isPersonagemRobotico) "Mods" else "Tensão"
        val tensaoColor = if (tensaoExcedida) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Peso total: ${"%.1f".format(totalWeight)} / ${"%.1f".format(limit)}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                if (compendioSciFiAtivo) {
                    Text(
                        "$tensaoLabel: $tensaoTotal/$tensaoLimite",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tensaoColor
                    )
                }
                if (hasSoldado) {
                    AssistChip(
                        onClick = onToggleSoldadoCarga,
                        label = {
                            Text(
                                if (soldadoCargaAtivo) "Bônus Soldado ativo" else "Bônus Soldado inativo"
                            )
                        }
                    )
                }
            }
        }

        Spacer(Modifier.size(4.dp))

        if (compendioSciFiAtivo && tensaoExcedida) {
            val excess = tensaoTotal - tensaoLimite
            Text(
                "Sobrecarga Cibernética: Personagem recebe o estado Fatigado (ou Exausto se X > Y+2).",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                "Estado atual: ${if (excess > 2) "Exausto" else "Fatigado"}.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
            if (excess > 2) {
                Text(
                    "Personagem incapacitado enquanto o excesso persistir.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // 6. List Content
        val isSearching = searchQuery.isNotBlank()

        if (isSearching) {
            // Flat List View
            // Collect all items first
             val finalFlatList = allCategorias.filter { cat ->
                 val catOrigem = cat.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                 if (filter.origens.isNotEmpty() && catOrigem !in filter.origens) return@filter false
                 if (filter.tipos.isNotEmpty() && cat.tipo !in filter.tipos) return@filter false
                 if (filter.subtipos.isNotEmpty() && cat.subtipo !in filter.subtipos) return@filter false
                 true
             }.flatMap { cat ->
                 cat.itens.filter { item ->
                     if (filter.somenteAcessiveis) {
                        val c = (item.custo as? JsonPrimitive)?.content?.toIntOrNull() ?: Int.MAX_VALUE
                        if (!usaRiqueza && c > dinheiro) return@filter false
                     }
                     if (selectedTypes.isNotEmpty() && cat.tipo !in selectedTypes) return@filter false

                     if (isSearching) {
                         val q = searchQuery.semAcentos().lowercase()
                         val n = item.nome.semAcentos().lowercase()
                         n.contains(q)
                     } else true
                 }
             }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(horizontal = 8.dp)
            ) {
                 if (finalFlatList.isEmpty()) {
                     item { Text("Nenhum equipamento encontrado.", modifier = Modifier.padding(8.dp)) }
                 } else {
                     items(finalFlatList, key = { it.nome + it.hashCode() }) { item ->
                         StandardEquipamentoItem(
                             equipamento = item,
                             onClick = { onEquipamentoDoubleClick(item) },
                             allowLongTexts = allowLongTexts,
                             showOriginalName = showOfficialNames,
                             showTensao = compendioSciFiAtivo
                         )
                     }
                 }
            }

        } else {
            // Browse Mode (Accordions)
            // Group by Type
            val categoriesByType = allCategorias.groupBy { it.tipo }

            Column(Modifier.padding(horizontal = 4.dp)) {
                categoriesByType.keys.sorted().forEach { type ->
                    if (selectedTypes.isNotEmpty() && type !in selectedTypes) return@forEach
                    if (filter.tipos.isNotEmpty() && type !in filter.tipos) return@forEach
                    val isExpanded = expandedTypeMap[type] ?: false

                    CollapsibleSection(
                        title = type,
                        expanded = isExpanded,
                        onToggle = { expandedTypeMap[type] = !isExpanded },
                        onToggleFeedback = onUserFeedback
                    ) {
                         // Inside the type, we have subtypes (categories with same type but different subtype)
                         val cats = categoriesByType[type] ?: emptyList()

                         // Apply filters to categories here too!
                         val filteredCats = cats.filter { cat ->
                             val catOrigem = cat.origem?.ifBlank { "BASICO" }?.uppercase() ?: "BASICO"
                             if (filter.origens.isNotEmpty() && catOrigem !in filter.origens) return@filter false
                             if (filter.subtipos.isNotEmpty() && cat.subtipo !in filter.subtipos) return@filter false
                             true
                         }

                        val catsBySubtype = filteredCats.groupBy { it.subtipo }
                        val subtypeEntries = catsBySubtype.entries.sortedBy { it.key }

                        LazyColumn(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            subtypeEntries.forEach { (subtype, subtypeCats) ->
                                item(key = "header_$subtype") {
                                    Text(
                                        text = subtype,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                val subtypeItems = subtypeCats.flatMap { it.itens }.filter { eq ->
                                     if (filter.somenteAcessiveis) {
                                         val c = (eq.custo as? JsonPrimitive)?.content?.toIntOrNull() ?: Int.MAX_VALUE
                                         if (!usaRiqueza && c > dinheiro) return@filter false
                                     }
                                     true
                                }

                                if (subtypeItems.isEmpty()) {
                                    item(key = "empty_$subtype") {
                                        Text("- Nenhum item -", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    items(subtypeItems, key = { item -> item.nome + item.hashCode() }) { item ->
                                        StandardEquipamentoItem(
                                            equipamento = item,
                                            onClick = { onEquipamentoDoubleClick(item) },
                                            allowLongTexts = allowLongTexts,
                                            showOriginalName = showOfficialNames,
                                            showTensao = compendioSciFiAtivo
                                        )
                                    }
                                }

                                item(key = "spacer_$subtype") { Spacer(Modifier.height(8.dp)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        if (showMoneyDialog) {
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
}
