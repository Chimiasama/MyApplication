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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp

@Composable
fun XpSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    onUseProgress: () -> Unit
) {
    SectionCard(
        title = "XP",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Star
    ) {
        Column(Modifier.padding(8.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxWidth().height(224.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(20) { index ->
                    val slotUsed = state.xpSlots[index]
                    val isEnabled = if (index == 0) !slotUsed else state.xpSlots[index - 1] && !slotUsed

                    Button(
                        onClick = {
                            state.xpSlots[index] = true
                            state.progressosDisponiveis++
                            onUseProgress()
                        },
                        enabled = isEnabled,
                        modifier = Modifier.size(50.dp)
                    ) {
                        Text(
                            text = (index + 1).toString(),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}
