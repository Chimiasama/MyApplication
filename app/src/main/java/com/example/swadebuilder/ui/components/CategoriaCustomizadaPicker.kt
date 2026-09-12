package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CategoriaCustomizada
import com.example.swadebuilder.model.TipoEntidadeCategoria
import com.example.swadebuilder.model.getDisplayName

/**
 * Controle único de "Categoria": dropdown com as opções oficiais (quando existem — ex.:
 * Vantagem) seguidas das Categorias Customizadas do Mestre e, por fim, "Personalizada…",
 * que revela um campo de texto abaixo pra criar uma nova. Antes disso existiam dois
 * controles concorrentes fazendo a mesma coisa (dropdown de categoria oficial + chip row
 * de categoria customizada, ver Vantagem no formulário de Conteúdo Customizado) — este
 * composable substitui os dois em todo lugar (Vantagem, Poder, Super Poder, Equipamento).
 *
 * Exatamente um de [selectedOfficial]/[selectedCustomId] deve estar preenchido por vez;
 * escolher um lado limpa o outro através dos callbacks `onSelectOfficial`/`onSelectCustomId`.
 */
@Composable
fun CategorySelector(
    officialOptions: List<Categoria>,
    selectedOfficial: Categoria?,
    customCategorias: List<CategoriaCustomizada>,
    selectedCustomId: String?,
    onSelectOfficial: (Categoria) -> Unit,
    onSelectCustomId: (String?) -> Unit,
    onCreateCustom: (nome: String) -> CategoriaCustomizada,
    onRenameCustom: (id: String, novoNome: String) -> Unit,
    onDeleteCustom: (id: String) -> Unit,
    label: String = "Categoria:",
    noneLabel: String = "Nenhuma"
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreateField by remember { mutableStateOf(false) }
    var novoNome by remember { mutableStateOf("") }
    var showManageDialog by remember { mutableStateOf(false) }

    val currentLabel = when {
        selectedCustomId != null -> customCategorias.firstOrNull { it.id == selectedCustomId }?.nome ?: noneLabel
        selectedOfficial != null -> selectedOfficial.getDisplayName()
        else -> noneLabel
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Text("▾", style = MaterialTheme.typography.bodyMedium)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (selectedOfficial == null && selectedCustomId == null) {
                    DropdownMenuItem(text = { Text(noneLabel) }, onClick = { expanded = false })
                }
                officialOptions.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.getDisplayName()) },
                        onClick = {
                            onSelectOfficial(cat)
                            expanded = false
                        }
                    )
                }
                if (officialOptions.isNotEmpty() && customCategorias.isNotEmpty()) {
                    HorizontalDivider()
                }
                customCategorias.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat.nome) },
                        onClick = {
                            onSelectCustomId(cat.id)
                            expanded = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Personalizada…")
                        }
                    },
                    onClick = {
                        showCreateField = true
                        expanded = false
                    }
                )
            }
        }

        if (showCreateField) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = novoNome,
                    onValueChange = { novoNome = it },
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (novoNome.isNotBlank()) {
                        val created = onCreateCustom(novoNome.trim())
                        onSelectCustomId(created.id)
                        novoNome = ""
                        showCreateField = false
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Criar categoria")
                }
            }
        }

        if (customCategorias.isNotEmpty()) {
            TextButton(onClick = { showManageDialog = true }) {
                Text("Gerenciar categorias", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showManageDialog) {
        var renamingId by remember { mutableStateOf<String?>(null) }
        var renamingValue by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManageDialog = false },
            title = { Text("Gerenciar categorias") },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(customCategorias, key = { it.id }) { cat ->
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
                                    if (renamingValue.isNotBlank()) onRenameCustom(cat.id, renamingValue.trim())
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
                                TextButton(onClick = {
                                    onDeleteCustom(cat.id)
                                    if (selectedCustomId == cat.id) onSelectCustomId(null)
                                }) {
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
            ChipRow("Categoria:") {
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
