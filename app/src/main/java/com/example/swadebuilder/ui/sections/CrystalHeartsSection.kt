package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CoracaoCristal
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun CrystalHeartsSection(
    state: CriadorState,
    expanded: Boolean,
    onToggle: () -> Unit,
    coracoes: List<CoracaoCristal>
) {
    if (!state.compendioCrystalHeartAtivo) return

    SectionCard(
        title = "Corações de Cristal",
        expanded = expanded,
        onToggle = onToggle,
        icon = androidx.compose.material.icons.Icons.Default.FlashOn
    ) {
        if (coracoes.isEmpty()) {
            Text(
                text = "Nenhum coração cadastrado.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
            return@SectionCard
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(coracoes) { heart ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(heart.nome, style = MaterialTheme.typography.titleMedium)
                                Text("Estágio: ${heart.estagio}", style = MaterialTheme.typography.bodyMedium)
                            }
                            RadioButton(
                                selected = state.coracaoCristalSelecionadoId == heart.id,
                                onClick = { state.coracaoCristalSelecionadoId = heart.id }
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        Text("PP: ${heart.pontosPoder} | Slots: ${heart.slots}", fontWeight = FontWeight.SemiBold)
                        if (heart.habilidadePassiva.isNotBlank()) {
                            Text("Habilidade Passiva: ${heart.habilidadePassiva}")
                        }
                        if (heart.poderes.isNotEmpty()) {
                            Text("Poderes: ${heart.poderes.joinToString(", ")}")
                        }
                        heart.complicacaoInerente?.let {
                            Text("Complicação Inerente: $it")
                        }
                        heart.descricao?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
