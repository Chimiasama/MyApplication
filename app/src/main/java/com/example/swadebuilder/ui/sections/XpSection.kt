package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.ui.components.SectionCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme

@Composable
fun XpSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUseProgress: (Int) -> Unit,
    onUndo: () -> Unit,
    getAdvantageName: (String) -> String,
    getHindranceName: (String) -> String
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

        Column(Modifier.padding(8.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(20) { index ->
                    val rankAbbreviation = getRankAbbreviationForIndex(index)
                    val advancement = state.advancementHistory.getOrNull(index)
                    val nextSlot = index == state.advancementHistory.size
                    val isLastUsed = index == state.advancementHistory.size - 1

                    val isEnabled = nextSlot && state.pontosVantagem == 0 && state.pontosPericia == 0 && state.progressosDisponiveis > 0

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                    ) {
                        Text(
                            text = rankAbbreviation,
                            modifier = Modifier.width(40.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (advancement != null) {
                                Text(
                                    text = advancement.getDisplayText(getAdvantageName, getHindranceName),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else if (nextSlot) {
                                Button(
                                    onClick = { onUseProgress(index) },
                                    enabled = isEnabled,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Gastar XP")
                                }
                            } else {
                                Spacer(modifier = Modifier.height(40.dp)) // Match button height
                            }
                        }

                        if (advancement != null && isLastUsed) {
                            TextButton(onClick = onUndo) {
                                Text("Desfazer")
                            }
                        } else {
                            // Spacer to keep alignment consistent, width is auto by IntrinsicSize
                            Spacer(modifier = Modifier.width(64.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun getRankAbbreviationForIndex(index: Int): String {
    val rankIndex = index / 4
    val slotInRank = (index % 4) + 1
    val rankChar = when (rankIndex) {
        0 -> "N" // Novato
        1 -> "E" // Experiente
        2 -> "V" // Veterano
        3 -> "H" // Heroico
        4 -> "L" // Lendário
        else -> "?"
    }
    return "$rankChar$slotInRank"
}
