package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaAtributos
import kotlin.math.max

@Composable
fun SuperAtributosPickerDialog(
    state: CriadorState,
    poolInicial: Int,
    onConfirmDistribuicao: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // alocações por atributo (em "steps" de superatributo)
    val alocacoes = remember { mutableStateMapOf<String, Int>().apply {
        listaAtributos.forEach { put(it, 0) }
    } }

    val totalAlocado by derivedStateOf { alocacoes.values.sum() }
    val restante by derivedStateOf { poolInicial - totalAlocado }
    val podeConfirmar by derivedStateOf { totalAlocado in 0..poolInicial }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Distribuir Superatributos", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Pool: $poolInicial   •   Restante: $restante")
                Spacer(Modifier.height(8.dp))

                // lista com + / -
                listaAtributos.forEach { attr ->
                    val atual = alocacoes[attr] ?: 0
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
                                if (atual > 0) alocacoes[attr] = atual - 1
                            },
                            enabled = atual > 0
                        ) { Icon(Icons.Default.Remove, contentDescription = "Diminuir $attr") }

                        Text("$atual", modifier = Modifier.width(24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)

                        IconButton(
                            onClick = {
                                if (restante > 0) alocacoes[attr] = atual + 1
                            },
                            enabled = restante > 0
                        ) { Icon(Icons.Default.Add, contentDescription = "Aumentar $attr") }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    "Regra: cada ponto alocado aqui é 1 ‘step’ de superatributo para aquele atributo. " +
                            "Aplica as regras de Superatributo (pode exceder limites raciais, etc.).",
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
            ) { Text("Aplicar ($totalAlocado/$poolInicial)") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}