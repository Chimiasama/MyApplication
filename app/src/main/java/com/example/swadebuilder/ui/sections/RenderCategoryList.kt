package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CollapsibleSection
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.ui.components.StandardEquipamentoItem
import kotlinx.serialization.json.JsonPrimitive

@Composable
fun RenderCategoryList(
    categories: List<EquipamentoCategoria>,
    filter: EquipFilter,
    dinheiro: Int,
    usaRiqueza: Boolean,
    allowLongTexts: Boolean,
    detalhesExpandidos: SnapshotStateMap<String, Boolean>,
    onEquipamentoDoubleClick: (EquipamentoItem) -> Unit,
    showOriginalName: Boolean = false,
    onUserFeedback: () -> Unit
) {
    val tipos = categories.map { it.tipo }.distinct()
    val expandedTipoMap = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.padding(start = 8.dp)) {
        tipos.forEach { tipo ->
            if (filter.tipos.isNotEmpty() && tipo !in filter.tipos) return@forEach

            val isTipoExpanded = expandedTipoMap[tipo] ?: false

            CollapsibleSection(
                title = tipo,
                expanded = isTipoExpanded,
                onToggle = { expandedTipoMap[tipo] = !isTipoExpanded },
                onToggleFeedback = onUserFeedback
            ) {
                val catsPorTipo = categories
                    .filter { it.tipo == tipo }
                    .let { list ->
                        if (filter.origens.isNotEmpty())
                            list.filter {
                                val o = it.origem?.ifBlank { "BASICO" } ?: "BASICO"
                                o.uppercase() in filter.origens
                            }
                        else list
                    }

                if (catsPorTipo.isNotEmpty()) {
                    val subtipos = catsPorTipo.map { it.subtipo }.distinct()
                    val expandedSubtipoMap = remember(tipo) {
                        subtipos.associateWith { mutableStateOf(false) }
                    }

                    subtipos.forEach { subtipo ->
                        if (filter.subtipos.isNotEmpty() && subtipo !in filter.subtipos) return@forEach

                        val isSubExpanded = expandedSubtipoMap.getValue(subtipo).value
                        CollapsibleSection(
                            title = subtipo,
                            expanded = isSubExpanded,
                            onToggle = { expandedSubtipoMap.getValue(subtipo).value = !isSubExpanded },
                            onToggleFeedback = onUserFeedback
                        ) {
                            val scroll = rememberScrollState()
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(scroll)
                                    .padding(start = 8.dp, bottom = 8.dp)
                            ) {
                                val catsPorSub = catsPorTipo.filter { it.subtipo == subtipo }
                                val subsub = catsPorSub.mapNotNull { it.subsubtipo }.distinct()

                                if (subsub.isEmpty()) {
                                    catsPorSub.flatMap { it.itens }
                                        .filter { eq ->
                                            if (filter.somenteAcessiveis) {
                                                val c = (eq.custo as? JsonPrimitive)?.content?.toIntOrNull()
                                                    ?: Int.MAX_VALUE
                                                if (!usaRiqueza && c > dinheiro) return@filter false
                                            }
                                            true
                                        }
                                        .forEach { equipamento ->
                                            StandardEquipamentoItem(
                                                equipamento = equipamento,
                                                onClick = { onEquipamentoDoubleClick(equipamento) },
                                                allowLongTexts = allowLongTexts,
                                                showOriginalName = showOriginalName
                                            )
                                        }
                                } else {
                                    val expandedSubsub = remember(tipo, subtipo) {
                                        subsub.associateWith { mutableStateOf(false) }
                                    }
                                    subsub.forEach { ss ->
                                        val isSsExpanded = expandedSubsub.getValue(ss).value
                                        CollapsibleSection(
                                            title = ss,
                                            expanded = isSsExpanded,
                                            onToggle = { expandedSubsub.getValue(ss).value = !isSsExpanded }
                                        ) {
                                            catsPorSub.filter { it.subsubtipo == ss }
                                                .flatMap { it.itens }
                                                .filter { eq ->
                                                    if (filter.somenteAcessiveis) {
                                                        val c =
                                                            (eq.custo as? JsonPrimitive)?.content?.toIntOrNull()
                                                                ?: Int.MAX_VALUE
                                                        if (!usaRiqueza && c > dinheiro) return@filter false
                                                    }
                                                    true
                                                }
                                                .forEach { equipamento ->
                                                    StandardEquipamentoItem(
                                                        equipamento = equipamento,
                                                        onClick = { onEquipamentoDoubleClick(equipamento) },
                                                        allowLongTexts = allowLongTexts,
                                                        showOriginalName = showOriginalName
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
        }
    }
}
