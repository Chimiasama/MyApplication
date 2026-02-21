package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PbWalletBanner(
    pcTotal: Int,
    pcLivres: Int,
    spendLabel: String,
    refundLabel: String,
    spendEnabled: Boolean,
    refundEnabled: Boolean,
    onSpend: () -> Unit,
    onRefund: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasBonus = pcTotal > 0
    val showActions = hasBonus && (spendEnabled || refundEnabled)

    if (!hasBonus) return

    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pontos Bônus",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$pcLivres / $pcTotal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (showActions) {
                    Box(contentAlignment = Alignment.CenterEnd) {
                        AssistChip(
                            onClick = { menuExpanded = true },
                            label = { Text("Ações de PB") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Ações de Pontos Bônus"
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(spendLabel) },
                                onClick = {
                                    if (!spendEnabled) return@DropdownMenuItem
                                    menuExpanded = false
                                    onSpend()
                                },
                                enabled = spendEnabled,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = spendLabel
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(refundLabel) },
                                onClick = {
                                    if (!refundEnabled) return@DropdownMenuItem
                                    menuExpanded = false
                                    onRefund()
                                },
                                enabled = refundEnabled,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Undo,
                                        contentDescription = refundLabel
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
