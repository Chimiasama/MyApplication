package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaTecnicasChi
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.components.SectionCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn

@Composable
fun TecnicasChiSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val maxTecnicas = state.tropoSelecionado?.tecnicas_iniciais ?: 0
    val currentCount = state.tecnicasChiSelecionadas.size

    SectionCard(
        title = "Técnicas de Chi ($currentCount/$maxTecnicas)",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.FlashOn
    ) {
        if (state.tropoSelecionado == null) {
            Text("Selecione um Tropo primeiro.")
        } else if (maxTecnicas == 0) {
            Text("Seu Tropo não começa com Técnicas de Chi.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(listaTecnicasChi) { tecnica ->
                    val isSelected = state.tecnicasChiSelecionadas.any { it.id == tecnica.id }
                    val canSelect = isSelected || currentCount < maxTecnicas

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                if (isSelected) {
                                    state.tecnicasChiSelecionadas.removeIf { it.id == tecnica.id }
                                } else if (canSelect) {
                                    state.tecnicasChiSelecionadas.add(tecnica)
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null // Handled by Card click
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tecnica.nome} (Custo: ${tecnica.custo})",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Estágio: ${tecnica.estagio} | Dist: ${tecnica.distancia} | Dur: ${tecnica.duracao}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = tecnica.descricao,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
