package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.CategoriaCustomizada
import com.example.swadebuilder.model.TipoEntidadeCategoria

/**
 * Linha de chips pra escolher (ou criar) a Categoria Customizada de uma Vantagem/
 * Equipamento/Poder/Super Poder/Complicação, dentro do formulário de Conteúdo
 * Customizado (ver SettingsDialog.kt). `categorias` já deve vir filtrada pelo
 * `tipoEntidade` correspondente.
 */
@Composable
fun CategoriaCustomizadaChipRow(
    label: String,
    categorias: List<CategoriaCustomizada>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
    onCreate: (nome: String) -> CategoriaCustomizada,
    onRename: (id: String, novoNome: String) -> Unit,
    onDelete: (id: String) -> Unit,
    allowNone: Boolean = true,
    noneLabel: String = "Nenhuma"
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showManageDialog by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LabeledChipGroup(label) {
            if (allowNone) {
                FilterChip(
                    selected = selectedId == null,
                    onClick = { onSelect(null) },
                    label = { Text(noneLabel, style = MaterialTheme.typography.labelSmall) }
                )
            }
            categorias.forEach { cat ->
                FilterChip(
                    selected = selectedId == cat.id,
                    onClick = { onSelect(cat.id) },
                    label = { Text(cat.nome, style = MaterialTheme.typography.labelSmall) }
                )
            }
            FilterChip(
                selected = false,
                onClick = { showCreateDialog = true },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(2.dp))
                        Text("Nova categoria", style = MaterialTheme.typography.labelSmall)
                    }
                }
            )
        }
        if (categorias.isNotEmpty()) {
            TextButton(onClick = { showManageDialog = true }) {
                Text("Gerenciar categorias", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showCreateDialog) {
        var nome by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Nova categoria") },
            text = {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (nome.isNotBlank()) {
                            val created = onCreate(nome.trim())
                            onSelect(created.id)
                            showCreateDialog = false
                        }
                    }
                ) { Text("Criar") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showManageDialog) {
        var renamingId by remember { mutableStateOf<String?>(null) }
        var renamingValue by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            title = { Text("Gerenciar categorias") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(categorias, key = { it.id }) { cat ->
                        if (renamingId == cat.id) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = renamingValue,
                                    onValueChange = { renamingValue = it },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = {
                                    if (renamingValue.isNotBlank()) onRename(cat.id, renamingValue.trim())
                                    renamingId = null
                                }) { Text("OK") }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cat.nome, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                TextButton(onClick = { renamingId = cat.id; renamingValue = cat.nome }) {
                                    Text("Renomear", style = MaterialTheme.typography.labelSmall)
                                }
                                TextButton(onClick = { onDelete(cat.id) }) {
                                    Text("Excluir", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showManageDialog = false }) { Text("Fechar") }
            }
        )
    }
}

/**
 * Chip-picker compacto usado na lista "Itens Customizados" pra reatribuir a
 * categoria de um item já criado ("mover entre categorias") sem reabrir o
 * formulário inteiro.
 */
@Composable
fun MoverCategoriaDialog(
    itemNome: String,
    categorias: List<CategoriaCustomizada>,
    categoriaAtualId: String?,
    onConfirm: (novaCategoriaId: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selecionada by remember { mutableStateOf(categoriaAtualId) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover categoria: $itemNome") },
        text = {
            LabeledChipGroup("Categoria:") {
                FilterChip(
                    selected = selecionada == null,
                    onClick = { selecionada = null },
                    label = { Text("Nenhuma", style = MaterialTheme.typography.labelSmall) }
                )
                categorias.forEach { cat ->
                    FilterChip(
                        selected = selecionada == cat.id,
                        onClick = { selecionada = cat.id },
                        label = { Text(cat.nome, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selecionada) }) { Text("Mover") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
