package com.example.swadebuilder.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.ArchetypeApplicationReport
import com.example.swadebuilder.model.CreationArchetype
import com.example.swadebuilder.util.ArchetypeTemplateManager

@Composable
fun ArchetypeSelectionDialog(
    settingKey: String = "BASICO",
    onDismiss: () -> Unit,
    onApplyArchetype: (CreationArchetype) -> ArchetypeApplicationReport
) {
    val manager = remember { ArchetypeTemplateManager() }
    val archetypes = remember(settingKey) { manager.getArchetypesForSetting(settingKey) }
    var selectedArchetype by remember { mutableStateOf<CreationArchetype?>(null) }
    var applicationReport by remember { mutableStateOf<ArchetypeApplicationReport?>(null) }

    if (applicationReport != null) {
        val report = applicationReport!!
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Relatório de Arquétipo Aplicado") },
            text = {
                Column {
                    Text(
                        text = "Arquétipo: ${report.archetypeName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    if (report.appliedAttributes.isNotEmpty()) {
                        Text("• Atributos: ${report.appliedAttributes.joinToString(", ")}")
                    }
                    if (report.appliedSkills.isNotEmpty()) {
                        Text("• Perícias: ${report.appliedSkills.joinToString(", ")}")
                    }
                    if (report.appliedEdges.isNotEmpty()) {
                        Text("• Vantagens: ${report.appliedEdges.joinToString(", ")}")
                    }
                    if (report.appliedHindrances.isNotEmpty()) {
                        Text("• Complicações: ${report.appliedHindrances.joinToString(", ")}")
                    }
                    if (report.warnings.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = report.warnings.joinToString("\n"),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("OK") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Arquétipos & Templates") },
            text = {
                Column {
                    Text(
                        text = "Selecione um arquétipo para aplicar escolhas rápidas de atributos, perícias, vantagens e complicações:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(archetypes) { archetype ->
                            val isSelected = selectedArchetype?.id == archetype.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedArchetype = archetype },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        text = archetype.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = archetype.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedArchetype?.let { archetype ->
                            applicationReport = onApplyArchetype(archetype)
                        }
                    },
                    enabled = selectedArchetype != null
                ) {
                    Text("Aplicar Arquétipo")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
            }
        )
    }
}
