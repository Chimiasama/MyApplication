package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.toEditionDisplayName

/**
 * Dialog para distribuir "slots" de SUPERVANTAGEM.
 *
 * Regra:
 * - poolInicial = número máximo de vantagens que podem ser compradas
 *   (já é baseCost / 2 lá na SuperPoderesSection).
 * - Cada Vantagem escolhida aqui consumirá 2 PP quando confirmada.
 */
@Composable
fun SuperVantagensPickerDialog(
    poolInicial: Int,
    vantagensDisponiveis: List<Vantagem>,
    onConfirm: (List<Vantagem>) -> Unit,
    onDismiss: () -> Unit
) {
    val maxSlots = poolInicial.coerceAtLeast(0)

    // lista local de vantagens selecionadas
    val selecionadas = remember { mutableStateListOf<Vantagem>() }

    val totalSelecionadas by remember {
        derivedStateOf { selecionadas.size }
    }

    val restante by remember {
        derivedStateOf { (maxSlots - totalSelecionadas).coerceAtLeast(0) }
    }

    val podeConfirmar by remember {
        derivedStateOf { maxSlots > 0 && totalSelecionadas in 1..maxSlots }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Escolher Supervantagens",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
            ) {
                Text(
                    "Slots disponíveis: $maxSlots   •   Restante: $restante",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                if (maxSlots == 0) {
                    Text(
                        "Sem slots disponíveis para Supervantagem (limite já atingido).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    if (selecionadas.isNotEmpty()) {
                        Text(
                            "Selecionadas:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        selecionadas.forEach { v ->
                            Text("• ${v.nome}", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        "Toque para adicionar/remover vantagens (exceto Lendárias).",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(vantagensDisponiveis, key = { it.id }) { v ->
                            val jaSelecionada = selecionadas.any { it.id == v.id }
                            val podeAdicionar = !jaSelecionada && restante > 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable(enabled = jaSelecionada || podeAdicionar) {
                                        if (jaSelecionada) {
                                            selecionadas.removeAll { it.id == v.id }
                                        } else if (podeAdicionar) {
                                            selecionadas.add(v)
                                        }
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(v.nome, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${v.categoriaRotulo()} • ${v.origem.toEditionDisplayName()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Text(
                                    when {
                                        jaSelecionada -> "Remover"
                                        podeAdicionar -> "Adicionar"
                                        else          -> "Cheio"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cada Vantagem escolhida aqui custará 2 Pontos de Superpoder ao confirmar.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = podeConfirmar,
                onClick = { onConfirm(selecionadas.toList()) }
            ) {
                Text("Aplicar ($totalSelecionadas/$maxSlots)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Pequeno helper para mostrar o rótulo da categoria sem precisar mexer em Vantagem.kt
 */
private fun Vantagem.categoriaRotulo(): String {
    // categoria é não-nula em Vantagem.kt, não precisa de safe call
    return categoria.name
}
