package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.toDiceString

@Composable
fun SuperAtributosPickerDialog(
    state: CriadorState,
    poolInicial: Int,
    onConfirmDistribuicao: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // alocações por atributo (em "steps" de superatributo)
    val alocacoes = remember {
        mutableStateMapOf<String, Int>().apply {
            state.listaAtributos.forEach { put(it, 0) }
        }
    }

    val totalAlocado by remember {
        derivedStateOf { alocacoes.values.sum() }
    }
    val restante by remember {
        derivedStateOf { poolInicial - totalAlocado }
    }
    val podeConfirmar by remember {
        derivedStateOf { totalAlocado in 0..poolInicial }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Distribuir Superatributos",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Pool: $poolInicial   •   Restante: $restante")
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    state.listaAtributos.forEach { attr ->
                        val currentSteps = alocacoes[attr] ?: 0
                        val baseRaw = state.atributoRawComSupers(attr)
                        val attrDisplayName = state.mapaAtributosDisplay[attr] ?: attr

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = attrDisplayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val maxStepsPossible = minOf(poolInicial, currentSteps + restante)
                                val stepOptions = (0..maxStepsPossible).toList()

                                stepOptions.forEach { stepOpt ->
                                    val projectedRaw = state.applySuperStepsFrom(baseRaw, stepOpt)
                                    val spCost = stepOpt * 2
                                    val isSelected = stepOpt == currentSteps
                                    val canAfford = stepOpt <= currentSteps || (stepOpt - currentSteps) <= restante

                                    val containerColor = when {
                                        !canAfford -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    }

                                    androidx.compose.material3.OutlinedCard(
                                        onClick = {
                                            if (canAfford) alocacoes[attr] = stepOpt
                                        },
                                        enabled = canAfford,
                                        modifier = Modifier.weight(1f),
                                        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                                            containerColor = containerColor,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f)
                                        ),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.material3.CardDefaults.outlinedCardBorder()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                                        ) {
                                            Text(
                                                text = projectedRaw.toDiceString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = if (canAfford) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                            )
                                            Text(
                                                text = if (spCost == 0) "base" else "$spCost SP",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "Cada ponto aqui é 1 step de Superatributo para aquele atributo. " +
                            "O valor mostrado já é o dado final considerando os supers existentes + estes steps.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeConfirmar,
                onClick = {
                    // só envia os > 0
                    val nonZero = alocacoes.filterValues { it > 0 }
                    onConfirmDistribuicao(nonZero)
                }
            ) {
                Text("Aplicar ($totalAlocado/$poolInicial)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
