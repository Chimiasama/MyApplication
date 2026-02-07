package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaCoracoesCrystal
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.ui.components.SectionCard

@Composable
fun CrystalHeartSection(
    state: CriadorState,
    viewModel: CriadorViewModel,
) {
    SectionCard(
        title = "Coração de Cristal",
        icon = Icons.Default.Favorite,
        showHeader = false
    ) {
        val temAgenteSyn = state.vantagensSelecionadas.any { it.id == "aa_agente_syn" }

        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!temAgenteSyn) {
                Text(
                    text = "Você precisa da vantagem 'Antecedente Arcano: Canalizar Cristal' para equipar um Coração de Cristal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                val selectedHeart = state.coracaoCrystalSelecionado
                var previewHeart by remember { mutableStateOf<CrystalHeart?>(null) }

                if (selectedHeart != null) {
                    Text(
                        text = "Coração Equipado:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(selectedHeart.nome, fontWeight = FontWeight.Bold)
                            Text("Estágio: ${selectedHeart.estagio}")
                            if (selectedHeart.habilidade_passiva != null) {
                                Text("Passiva: ${selectedHeart.habilidade_passiva}")
                            }
                            if (selectedHeart.complicacao_inerente != null) {
                                Text("Complicação: ${selectedHeart.complicacao_inerente}", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Poderes: ${selectedHeart.poderes.joinToString(", ")}")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Text(
                    text = "Corações Disponíveis:",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val stageOrder = listOf("Novato", "Experiente", "Veterano", "Heroico", "Lendário")
                val heartsByStage = listaCoracoesCrystal
                    .groupBy { it.estagio.ifBlank { "Outros" } }
                    .toSortedMap(compareBy<String> { stageOrder.indexOf(it).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }
                        .thenBy { it })

                Column {
                    heartsByStage.forEach { (stage, hearts) ->
                        Text(
                            text = stage,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        hearts.forEach { heart ->
                            CrystalHeartItem(heart) {
                                previewHeart = heart
                            }
                        }
                    }
                }

                if (previewHeart != null) {
                    Dialog(onDismissRequest = { previewHeart = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = previewHeart!!.nome,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text("Estágio: ${previewHeart!!.estagio}")
                                Spacer(Modifier.height(8.dp))
                                if (previewHeart!!.habilidade_passiva != null) {
                                    Text("Passiva: ${previewHeart!!.habilidade_passiva}")
                                }
                                if (previewHeart!!.complicacao_inerente != null) {
                                    Text("Complicação: ${previewHeart!!.complicacao_inerente}", color = MaterialTheme.colorScheme.error)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Poderes: ${previewHeart!!.poderes.joinToString(", ")}")

                                if (previewHeart!!.descricao != null) {
                                    Spacer(Modifier.height(8.dp))
                                    Text("Descrição: ${previewHeart!!.descricao}", style = MaterialTheme.typography.bodySmall)
                                }

                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                                ) {
                                    TextButton(onClick = { previewHeart = null }) {
                                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        viewModel.selecionarCrystalHeart(previewHeart!!)
                                        previewHeart = null
                                    }) {
                                        Text("Selecionar")
                                    }
                                }
                            }
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
                Text(text = "Estágio: ${heart.estagio}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
