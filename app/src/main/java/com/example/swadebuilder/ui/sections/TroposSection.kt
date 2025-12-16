package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.components.SectionCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star

@Composable
fun TroposSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    SectionCard(
        title = "Tropos (Arte da Guerra)",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Star
    ) {
        Column {
            Text(
                "Escolha um Tropo (obrigatório). Tropos definem técnicas iniciais e perícias.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                items(listaTropos) { tropo ->
                    val selected = state.tropoSelecionado?.id == tropo.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selecionarTropo(tropo) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = null // Handled by Card click
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = tropo.nome,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tropo.descricao,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (tropo.tecnicas_iniciais > 0) {
                                    Text(
                                        text = "Técnicas Iniciais: ${tropo.tecnicas_iniciais}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
