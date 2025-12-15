package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaCoracoesCrystal
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun CrystalHeartSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    SectionCard(
        title = "Coração de Cristal",
        expanded = expanded,
        onToggle = onToggle,
        icon = Icons.Default.Favorite
    ) {
        val temAgenteSyn = state.vantagensSelecionadas.any { it.id == "aa_agente_syn" }

        if (!temAgenteSyn) {
            Text(
                text = "Você precisa da vantagem 'Antecedente Arcano (Agente da Syn)' para equipar um Coração de Cristal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            val selectedHeart = state.coracaoCrystalSelecionado

            if (selectedHeart != null) {
                 Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Coração Equipado: ${selectedHeart.nome}", fontWeight = FontWeight.Bold)
                        Text("Estágio: ${selectedHeart.estagio}")
                        Text("PP: ${selectedHeart.pontos_poder}")
                        Text("Slots: ${selectedHeart.slots}")
                        if (selectedHeart.habilidade_passiva != null) {
                            Text("Passiva: ${selectedHeart.habilidade_passiva}")
                        }
                        if (selectedHeart.complicacao_inerente != null) {
                            Text("Complicação: ${selectedHeart.complicacao_inerente}", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Poderes: ${selectedHeart.poderes.joinToString(", ")}")

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Trocar Coração",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { viewModel.desequiparCrystalHeart() }
                        )
                    }
                }
            } else {
                Text(
                    text = "Selecione um Coração de Cristal:",
                    modifier = Modifier.padding(8.dp)
                )

                Column {
                    listaCoracoesCrystal.forEach { heart ->
                        CrystalHeartItem(heart) {
                            viewModel.selecionarCrystalHeart(heart)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CrystalHeartItem(heart: CrystalHeart, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = heart.nome, fontWeight = FontWeight.Bold)
                Text(text = "PP: ${heart.pontos_poder} | Slots: ${heart.slots}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Estágio: ${heart.estagio}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
