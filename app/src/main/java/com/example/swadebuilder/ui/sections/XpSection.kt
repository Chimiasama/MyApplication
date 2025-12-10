package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.TOTAL_PROGRESS_LIMIT
import com.example.swadebuilder.dynamicStageCaps
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaDeEstagios
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.AdvancementAction
import com.example.swadebuilder.model.HindranceChangeType
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun XpSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUseProgress: (Int) -> Unit,
    onUndo: () -> Unit
) {
    SectionCard(
        title = "XP",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Star
    ) {
        LaunchedEffect(
            state.progresso,
            state.stageXpSpent.values.toList(),
            state.xpSlots.toList(),
            state.modoProgressaoAtivo
        ) {
            state.recomputeAvailableProgress()
        }

        val slotDescriptions = buildSlotDescriptions(state)
        val slotStageLabels = buildStageLabels()
        val listState = rememberLazyListState()

        LaunchedEffect(state.xpSlots.toList()) {
            val lastUsedIndex = state.xpSlots.indexOfLast { it }
            if (lastUsedIndex > 0) {
                listState.animateScrollToItem(lastUsedIndex)
            }
        }

        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TOTAL_PROGRESS_LIMIT) { index ->
                    val slotUsed = state.xpSlots[index]
                    val isEnabled = (if (index == 0) !slotUsed else state.xpSlots[index - 1] && !slotUsed) && state.pontosVantagem == 0 && state.pontosPericia == 0 && state.progressosDisponiveis > 0
                    val isLastUsed = state.xpSlots.indexOfLast { it } == index
                    val label = slotStageLabels.getOrNull(index) ?: (index + 1).toString()
                    val contentText = slotDescriptions.getOrNull(index)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.width(44.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .alpha(if (isEnabled || slotUsed) 1f else 0.5f)
                                .clickable(enabled = isEnabled) { onUseProgress(index) },
                            shape = RoundedCornerShape(8.dp),
                            tonalElevation = if (slotUsed) 4.dp else 0.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            color = if (slotUsed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = contentText ?: if (slotUsed) "Em andamento" else "Usar progresso",
                                    fontSize = 15.sp,
                                    fontWeight = if (slotUsed) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }

                        if (slotUsed && isLastUsed) {
                            TextButton(onClick = onUndo) {
                                Text("Desfazer")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildStageLabels(): List<String> {
    val labels = mutableListOf<String>()
    listaDeEstagios.forEachIndexed { idx, est ->
        val cap = dynamicStageCaps.getOrNull(idx) ?: 0
        repeat(cap) { offset ->
            labels += "${est.nome.first()}${offset + 1}"
        }
    }
    return labels
}

private fun buildSlotDescriptions(state: CriadorState): List<String?> {
    val descriptions = mutableListOf<String?>()

    state.advancementHistory.forEach { action ->
        val desc = describeAction(action, state)
        repeat(action.progressCost.coerceAtLeast(1)) {
            if (descriptions.size < TOTAL_PROGRESS_LIMIT) {
                descriptions += desc
            }
        }
    }

    if (descriptions.size < TOTAL_PROGRESS_LIMIT) {
        descriptions += List(TOTAL_PROGRESS_LIMIT - descriptions.size) { null }
    }

    return descriptions.take(TOTAL_PROGRESS_LIMIT)
}

private fun describeAction(action: AdvancementAction, state: CriadorState): String = when (action) {
    is AdvancementAction.SpendOnAdvantage -> {
        val advantageName = listaVantagens.firstOrNull { it.id == action.advantageId }?.nome
        "Vantagem: ${advantageName ?: action.advantageId}"
    }

    is AdvancementAction.IncreaseAttribute -> {
        val attrName = mapaAtributosDisplay[action.attributeName] ?: action.attributeName
        "Atributo: $attrName"
    }

    is AdvancementAction.SpendOnSkills -> {
        val skills = action.skillsIncreased.distinct().mapNotNull { skillName ->
            val per = listaPericias.firstOrNull { it.nome == skillName }
            per?.let {
                val dieValue = action.recordedSkillValues?.get(skillName) ?: state.rawTotal(it)
                val die = dieValue.toDiceString()
                "$die ${it.nome}"
            } ?: skillName
        }
        "Perícias: ${skills.joinToString(", ")}".trim()
    }

    is AdvancementAction.RemoveHindrance -> {
        val compName = listaComplicacoes.firstOrNull { it.id == action.hindranceId }
        val baseLabel = compName?.id ?: action.hindranceId
        when (action.changeType) {
            HindranceChangeType.RESERVATION -> "Reserva de Complicação: $baseLabel"
            HindranceChangeType.REDUCE_TO_MINOR -> "Reduzir Complicação: $baseLabel"
            HindranceChangeType.REMOVE -> "Remover Complicação: $baseLabel"
        }
    }

    is AdvancementAction.ReserveLegendaryAttribute -> "Reserva de atributo lendário"
}
