// SuperPericiasPickerDialog.kt
package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.listaPericias
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
    poolInicial: Int,
    onConfirmDistribuicao: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // Mapa local: periciaKey -> steps alocados
    val alocacoes = remember {
        mutableStateMapOf<String, Int>().apply {
            listaPericias.forEach { per ->
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
        derivedStateOf { totalAlocado in 0..poolInicial }
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

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(listaPericias, key = { it.nome }) { per ->
                        val key = per.nome.keyify()
                        val atual = alocacoes[key] ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(per.nome, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        if (atual > 0) {
                                            alocacoes[key] = atual - 1
                                        }
                                    },
                                    enabled = atual > 0
                                ) {
                                    Text("−")
                                }

                                Text(
                                    atual.toString(),
                                    modifier = Modifier.widthIn(min = 24.dp)
                                )

                                TextButton(
                                    onClick = {
                                        if (restante > 0) {
                                            alocacoes[key] = atual + 1
                                        }
                                    },
                                    enabled = restante > 0
                                ) {
                                    Text("+")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Cada ponto aqui é 1 nível de Superperícia naquela perícia. " +
                            "Isso ignora o atributo ligado para custo e pode ultrapassar o limite racial, " +
                            "mas ainda conta para derivados (ex.: Lutar aumenta Aparar).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeConfirmar,
                onClick = {
                    val resultado = alocacoes
                        .filterValues { it > 0 }
                        .toMap()

                    onConfirmDistribuicao(resultado)
                }
            ) {
                Text("Aplicar ($totalAlocado/$poolInicial)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
