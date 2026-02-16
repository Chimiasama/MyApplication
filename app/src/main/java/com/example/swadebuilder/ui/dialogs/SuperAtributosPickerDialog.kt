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

                // lista com + / - mostrando o VALOR FINAL (dado) e não mais só o número de steps
                state.listaAtributos.forEach { attr ->
                    val steps = alocacoes[attr] ?: 0

                    // valor atual (já com supers existentes)
                    val baseRaw = state.atributoRawComSupers(attr)
                    // simula aplicação dos steps desta compra
                    val projectedRaw = state.applySuperStepsFrom(baseRaw, steps)
                    val textoValor = projectedRaw.toDiceString()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = attr,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(
                            onClick = {
                                if (steps > 0) alocacoes[attr] = steps - 1
                            },
                            enabled = steps > 0
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Diminuir $attr"
                            )
                        }

                        Text(
                            textoValor,
                            modifier = Modifier.width(48.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                if (restante > 0) alocacoes[attr] = steps + 1
                            },
                            enabled = restante > 0
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Aumentar $attr"
                            )
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
