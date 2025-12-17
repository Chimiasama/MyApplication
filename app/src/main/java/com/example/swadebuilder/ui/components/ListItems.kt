package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.sections.TransparentOutlinedReadOnlyField

@Composable
fun EquipamentoListItem(
    item: EquipamentoItem,
    state: CriadorState,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val name = if (state.modoOficialAtivo && !item.originalName.isNullOrBlank()) {
                    item.originalName
                } else {
                    item.nome
                }
                Text(text = name, fontWeight = FontWeight.Bold)

                val costStr = item.custo.toString() // Simplified
                val weightStr = item.peso.toString()

                Text(
                    text = "Custo: $$costStr | Peso: $weightStr",
                    style = MaterialTheme.typography.bodySmall
                )
                if (!item.descricao.isNullOrBlank()) {
                    Text(
                        text = item.descricao,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remover")
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
            }
        }
    }
}

@Composable
fun VantagemListItem(
    vantagem: Vantagem,
    state: CriadorState,
    canSelect: Boolean,
    onSelect: (Vantagem) -> Unit,
    onRemove: (Vantagem) -> Unit
) {
    val isSelected = state.vantagensSelecionadas.any { it.id == vantagem.id }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val name = if (state.modoOficialAtivo && !vantagem.originalName.isNullOrBlank()) {
                        vantagem.originalName
                    } else {
                        vantagem.nome
                    }
                    Text(text = name, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Estágio: ${vantagem.requisitos.estagio}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (isSelected) {
                    IconButton(onClick = { onRemove(vantagem) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover")
                    }
                } else {
                    IconButton(
                        onClick = { onSelect(vantagem) },
                        enabled = canSelect
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    }
                }
            }
            if (!vantagem.descricao.isNullOrBlank()) {
                Text(
                    text = vantagem.descricao,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ComplicacaoListItem(
    complicacao: Complicacao,
    nivelSelecionado: String?, // "Menor", "Maior", or null
    readOnly: Boolean,
    onSelect: (String) -> Unit,
    onRemove: () -> Unit
) {
    val isSelected = nivelSelecionado != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            val name = if (false) { // TODO access state.modoOficialAtivo if passed
                complicacao.originalName ?: complicacao.name
            } else {
                complicacao.name
            }

            Text(text = name, fontWeight = FontWeight.Bold)

            if (!isSelected) {
                Row {
                    if (complicacao.severity == "Menor" || complicacao.severity == "Ambos") {
                        androidx.compose.material3.Button(
                            onClick = { onSelect("Menor") },
                            enabled = !readOnly,
                            modifier = Modifier.padding(end = 8.dp)
                        ) { Text("Menor") }
                    }
                    if (complicacao.severity == "Maior" || complicacao.severity == "Ambos") {
                        androidx.compose.material3.Button(
                            onClick = { onSelect("Maior") },
                            enabled = !readOnly
                        ) { Text("Maior") }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selecionado: $nivelSelecionado")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onRemove, enabled = !readOnly) {
                        Icon(Icons.Default.Delete, contentDescription = "Remover")
                    }
                }
            }

            if (!complicacao.description.isNullOrBlank()) {
                Text(
                    text = complicacao.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
