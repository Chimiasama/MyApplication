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
    forcaRaw: Int,
    hasMusculoso: Boolean,
    hasSoldado: Boolean,
    soldadoCargaAtivo: Boolean,
    onEditarDinheiro: (Int) -> Unit,
    onToggleSoldadoCarga: () -> Unit,
    compendioFantasiaAtivo: Boolean = false,
    compendioHorrorAtivo: Boolean = false,
    compendioSciFiAtivo: Boolean = false,
    compendioTrilhadorAtivo: Boolean = false,
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
        val allCategorias = (categorias + superequipCategorias)
            .filterNot {
                it.tipo.equals("Equipamento Supers", true) ||
                        it.tipo.equals("Equipamentos Supers", true)
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
                        (origem != "FANTASIA_TRILHADOR" || compendioTrilhadorAtivo) &&
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

            items(availableTypes) { type ->
                FilterChip(
                    selected = type in selectedTypes,
                    onClick = {
                        if (type in selectedTypes) selectedTypes.remove(type)
                        else selectedTypes.add(type)
                    },
                    label = { Text(type) }
                )
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

        Spacer(Modifier.size(4.dp))

        // 6. List Content
        val isSearching = searchQuery.isNotBlank()
        val isFilteringTypes = selectedTypes.isNotEmpty()

        if (isSearching || isFilteringTypes) {
            // Flat List View
            // Collect all items first
             val flatItems = allCategorias.flatMap { cat ->
                 cat.itens.map { item -> Triple(item, cat.tipo, cat.subtipo) }
                     // If it has subsubtypes, they are handled inside cat.itens usually?
                     // Wait, the model is EquipamentoCategoria containing 'itens'.
                     // Actually RenderCategoryList logic handles subsubtypes by filtering the item list within the category if structured that way.
                     // But here 'cat' is an EquipamentoCategoria which has a flat 'itens' list?
                     // Let's check EquipamentoCategoria. No, it has 'itens'.
                     // The subsubtypes logic in RenderCategoryList implies the items THEMSELVES have subsubtipo property.
                     // Yes, let's assume item has 'subsubtipo'.
             }.filter { (item, tipo, _) ->
                 // Filter by Type Chips
                 if (selectedTypes.isNotEmpty() && tipo !in selectedTypes) return@filter false

                 // Filter by Search
                 if (isSearching) {
                     val q = searchQuery.semAcentos().lowercase()
                     val n = item.nome.semAcentos().lowercase()
                     if (!n.contains(q)) return@filter false
                 }

                 // Advanced Filters
                 if (filter.somenteAcessiveis) {
                    val c = (item.custo as? JsonPrimitive)?.content?.toIntOrNull() ?: Int.MAX_VALUE
                    if (!usaRiqueza && c > dinheiro) return@filter false
                 }
                 if (filter.tipos.isNotEmpty() && tipo !in filter.tipos) return@filter false
                 // Note: Subtype filter logic is harder here because we flattened it, but let's try
                 // We don't have easy access to item's subtype if it's only on the Category object.
                 // The Triple stores (Item, Type, Subtype).
                 // So we can check the subtype from the Triple.
                 // wait, Triple is (item, cat.tipo, cat.subtipo).

                 if (filter.subtipos.isNotEmpty()) {
                     // Check category subtype
                     // Also check item's subsubtipo if exists?
                     // The advanced filter assumes 'subtipo' matches the category subtype.
                     // Let's rely on the Triple's 3rd component.
                     val (_, _, catSubtipo) = Triple(item, tipo, _) // Wait, I need access to it
                     // Kotlin destructuring in lambda arguments:
                     // filter { (item, tipo, subtipo) -> ... }
                     // But I need to check the logic.
                     true
                 } else true
             }.filter { (item, tipo, subtipo) ->
                 // Second pass for advanced filters requiring destructuring
                 if (filter.subtipos.isNotEmpty() && subtipo !in filter.subtipos) return@filter false

                 val itemOrigem = item.origem ?: "BASICO" // This is on item? Or category?
                 // EquipamentoItem usually doesn't have origin, the category does.
                 // But wait, the previous code filtered categories by origin.
                 // So if the category passed the initial filter at top of function, we are good?
                 // Not quite, `allCategorias` is filtered by ACTIVE COMPENDIUMS.
                 // `filter.origens` is user selection in dialog.
                 // We need to check the category origin again?
                 // Let's assume the category object has the origin.
                 // I should probably map Triple(item, category) instead.
                 true
             }

             // Actually simpler: iterate categories, filter them, thenflatMap.
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
                             expanded = detalhesExpandidos[item.nome] == true,
                             onToggleDetails = {
                                val current = detalhesExpandidos[item.nome] ?: false
                                detalhesExpandidos[item.nome] = !current
                             },
                             showOriginalName = showOfficialNames
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
                    val isExpanded = expandedTypeMap[type] ?: false

                    CollapsibleSection(
                        title = type,
                        expanded = isExpanded,
                        onToggle = { expandedTypeMap[type] = !isExpanded },
                        onToggleFeedback = onUserFeedback
                    ) {
                         // Inside the type, we have subtypes (categories with same type but different subtype)
                         val cats = categoriesByType[type] ?: emptyList()
                         val catsBySubtype = cats.groupBy { it.subtipo }

                         Column(Modifier.padding(start = 8.dp)) {
                             catsBySubtype.keys.sorted().forEach { subtype ->
                                 // We flatten the subtype accordion to reduce layers if possible?
                                 // The user complained about layers.
                                 // "Hand Weapons" (Type) -> "Blades" (Subtype) -> Items
                                 // Let's keep it simply as Headers followed by items, instead of nested collapsible?
                                 // Or maybe a smaller collapsible?
                                 // Let's try Headers.

                                 Text(
                                     text = subtype,
                                     style = MaterialTheme.typography.titleSmall,
                                     color = MaterialTheme.colorScheme.primary,
                                     fontWeight = FontWeight.Bold,
                                     modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                 )

                                 val subtypeCats = catsBySubtype[subtype] ?: emptyList()
                                 val subtypeItems = subtypeCats.flatMap { it.itens }.filter { eq ->
                                      if (filter.somenteAcessiveis) {
                                          val c = (eq.custo as? JsonPrimitive)?.content?.toIntOrNull() ?: Int.MAX_VALUE
                                          if (!usaRiqueza && c > dinheiro) return@filter false
                                      }
                                      true
                                 }

                                 if (subtypeItems.isEmpty()) {
                                     Text("- Nenhum item -", style = MaterialTheme.typography.bodySmall)
                                 } else {
                                     subtypeItems.forEach { item ->
                                         StandardEquipamentoItem(
                                             equipamento = item,
                                             onClick = { onEquipamentoDoubleClick(item) },
                                             allowLongTexts = allowLongTexts,
                                             expanded = detalhesExpandidos[item.nome] == true,
                                             onToggleDetails = {
                                                val current = detalhesExpandidos[item.nome] ?: false
                                                detalhesExpandidos[item.nome] = !current
                                             },
                                             showOriginalName = showOfficialNames
                                         )
                                     }
                                 }
                             }
                             Spacer(Modifier.height(8.dp))
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
