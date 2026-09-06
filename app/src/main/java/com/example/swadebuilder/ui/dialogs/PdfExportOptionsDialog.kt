package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.swadebuilder.FichaPdfSecao
import com.example.swadebuilder.ui.theme.Spacing

/**
 * Antes de gerar o PDF, deixa o jogador escolher quais páginas extras (separadas por
 * tópico: Antecedente Arcano, Mechas, Equipamentos, Cibernéticos) entram na ficha — útil
 * pra quem vai imprimir de verdade e quer reduzir a quantidade de páginas. A página
 * principal nunca é opcional.
 */
@Composable
fun PdfExportOptionsDialog(
    availableSections: Set<FichaPdfSecao>,
    onConfirm: (Set<FichaPdfSecao>) -> Unit,
    onDismiss: () -> Unit
) {
    val selection = remember(availableSections) {
        mutableStateMapOf<FichaPdfSecao, Boolean>().apply {
            availableSections.forEach { put(it, true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exportar Ficha em PDF") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "A página principal (atributos, perícias, complicações, vantagens e armas) sempre é incluída.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (availableSections.isEmpty()) {
                    Text(
                        "Nenhuma página extra disponível para este personagem.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "Páginas extras (separadas por tópico):",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    availableSections.forEach { secao ->
                        val checked = selection[secao] ?: true
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selection[secao] = !checked },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { selection[secao] = it })
                            Text(secao.titulo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(availableSections.filterTo(mutableSetOf()) { selection[it] == true })
            }) {
                Text("Gerar PDF")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
