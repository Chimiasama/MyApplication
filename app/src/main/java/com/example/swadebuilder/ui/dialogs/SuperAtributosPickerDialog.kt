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
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaAtributos

@Composable
fun SuperAtributosPickerDialog(
    state: CriadorState,                // ainda não usamos, mas deixei aqui para futuro
    poolInicial: Int,
    onConfirmDistribuicao: (Map<String, Int>) -> Unit,
    onDismiss: () -> Unit
) {
    // alocações por atributo (em "steps" de superatributo)
    val alocacoes = remember {
        mutableStateMapOf<String, Int>().apply {
            listaAtributos.forEach { put(it, 0) }
        }
    }

    // >>> corrigido: todos os derivedStateOf agora estão dentro de remember
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
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Diminuir $attr"
                            )
                        }

                        Text(
                            "$atual",
                            modifier = Modifier.width(24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                if (restante > 0) alocacoes[attr] = atual + 1
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
            ) {
                Text("Aplicar ($totalAlocado/$poolInicial)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
