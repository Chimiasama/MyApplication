// SuperPericiasPickerDialog.kt
package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.keyify

/**
 * Dialog genérico para distribuir "steps" de SUPERPERÍCIA entre várias perícias.
 *
 * - poolInicial: quantidade de pontos de Superperícia que o jogador comprou (baseCost).
 * - onConfirmDistribuicao: mapa periciaKey -> steps (cada step = 1 nível de Superperícia).
 *
 * IMPORTANTE:
 * Esses steps NÃO gastam pontos de perícia normais. A aplicação real é feita
 * na SuperPoderesSection via PowerEffect.SuperPericia.
 */
@Composable
fun SuperPericiasPickerDialog(
    state: CriadorState,
    poolInicial: Int,
    onConfirmDistribuicao: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // Filter and prepare skill list
    val skillsParaExibir = remember(state.listaPericias) {
        val blacklist = setOf(
            "ALQUIMIA", "ACROBACIA", "LEI", "TRANSICAO", "CONVENCAO", "OFICIO"
        )
        state.listaPericias
            .filter { it.nome.keyify() !in blacklist }
            .distinctBy { it.nome.keyify() } // Deduplicate "Foco" and others
            .sortedBy { it.nome }
    }

    // Mapa local: periciaKey -> steps alocados
    val alocacoes = remember {
        mutableStateMapOf<String, Int>().apply {
            skillsParaExibir.forEach { per ->
                put(per.nome.keyify(), 0)
            }
        }
    }

    // Se poolInicial mudar por algum motivo, zera alocações
    LaunchedEffect(poolInicial) {
        alocacoes.keys.forEach { k -> alocacoes[k] = 0 }
    }

    val totalAlocado by remember {
        derivedStateOf { alocacoes.values.sum() }
    }

    val restante by remember {
        derivedStateOf { (poolInicial - totalAlocado).coerceAtLeast(0) }
    }

    val podeConfirmar by remember {
        derivedStateOf { poolInicial > 0 && totalAlocado in 1..poolInicial }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Distribuir Superperícias", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                Text(
                    "Pool disponível: $poolInicial   •   Restante: $restante",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                if (poolInicial <= 0) {
                    Text(
                        "Sem pontos disponíveis para Superperícia (limite já atingido).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(skillsParaExibir, key = { it.nome }) { per ->
                            val key = per.nome.keyify()
                            val currentSteps = alocacoes[key] ?: 0
                            val baseRaw = state.rawTotal(per)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = per.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))

                                val maxStepsPossible = currentSteps + restante
                                val stepOptions = (0..maxStepsPossible).toList()

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp)
                                ) {
                                    items(stepOptions) { stepOpt ->
                                        val projectedRaw = state.applySuperStepsFrom(baseRaw, stepOpt)
                                        val isSelected = stepOpt == currentSteps
                                        val canAfford = stepOpt <= currentSteps || (stepOpt - currentSteps) <= restante

                                        val textoValor = when {
                                            projectedRaw == 0 && per.basica -> "d4"
                                            projectedRaw == 0 -> "—"
                                            else -> projectedRaw.toDiceString()
                                        }

                                        val containerColor = when {
                                            !canAfford -> MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.4f)
                                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                        }

                                        androidx.compose.material3.OutlinedCard(
                                            onClick = {
                                                if (canAfford) alocacoes[key] = stepOpt
                                            },
                                            enabled = canAfford,
                                            modifier = Modifier.width(68.dp),
                                            colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                                                containerColor = containerColor,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.3f)
                                            ),
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.material3.CardDefaults.outlinedCardBorder()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = textoValor,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = if (canAfford) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                )
                                                Text(
                                                    text = if (stepOpt == 0) "base" else "$stepOpt SP",
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

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cada ponto aqui é 1 nível de Superperícia naquela perícia.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeConfirmar,
                onClick = {
                    val resultado = alocacoes.filterValues { it > 0 }.toMap()
                    onConfirmDistribuicao(resultado)
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
