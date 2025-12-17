package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.EquipamentoCategoria

@Composable
fun RenderCategoryList(
    categories: List<EquipamentoCategoria>,
    state: CriadorState,
    expStates: Map<String, Boolean> = emptyMap(),
    onToggleMap: Map<String, () -> Unit> = emptyMap(),
    forceExpandAll: Boolean = false
) {
    Column {
        categories.forEach { cat ->
            val expanded = forceExpandAll || (expStates[cat.categoria] ?: false)
            val onToggle = onToggleMap[cat.categoria] ?: {}

            // If forced expand, just show header and content.
            // If toggleable, use CollapsibleSection logic or simplified header.
            // Using simplified logic here to match typical usage:

            if (forceExpandAll) {
                androidx.compose.material3.Text(
                    text = cat.categoria,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Column {
                    cat.itens.forEach { item ->
                        EquipamentoListItem(
                            item = item,
                            state = state,
                            onAdd = {
                                state.equipamentosComprados.add(item)
                                // Deduct money logic simplified
                                val custo = (item.custo as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull() ?: 0
                                state.dinheiro = (state.dinheiro - custo).coerceAtLeast(0)
                            },
                            onRemove = {
                                state.equipamentosComprados.remove(item)
                                val custo = (item.custo as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull() ?: 0
                                state.dinheiro += custo
                            }
                        )
                    }
                }
            } else {
                CollapsibleSection(
                    title = cat.categoria,
                    expanded = expanded,
                    onToggle = onToggle
                ) {
                    Column {
                        cat.itens.forEach { item ->
                            EquipamentoListItem(
                                item = item,
                                state = state,
                                onAdd = {
                                    state.equipamentosComprados.add(item)
                                    val custo = (item.custo as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull() ?: 0
                                    state.dinheiro = (state.dinheiro - custo).coerceAtLeast(0)
                                },
                                onRemove = {
                                    state.equipamentosComprados.remove(item)
                                    val custo = (item.custo as? kotlinx.serialization.json.JsonPrimitive)?.content?.toIntOrNull() ?: 0
                                    state.dinheiro += custo
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
