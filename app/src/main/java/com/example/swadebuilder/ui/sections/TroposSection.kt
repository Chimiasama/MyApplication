package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.criacaoBasicaCongelada
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.ui.components.SectionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TroposSection(
    state: CriadorState,
    onUserFeedback: () -> Unit
) {
    if (!state.compendioArteDaGuerraAtivo) return

    val tropos = remember { listaTropos }
    val showOfficialNames = EditionConfig.isFullEdition && state.modoOficialAtivo
    val idParaNome = remember(showOfficialNames) {
        listaVantagens.associate { vant ->
            val nome = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome
            vant.id to nome
        }
    }

    SectionCard(
        title = "Tropos",
        icon = Icons.Default.AutoAwesome,
        showHeader = false
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Escolha um único tropo para definir o estilo marcial do personagem.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            tropos.forEach { tropo ->
                val selecionado = state.tropoSelecionado?.id == tropo.id
                val vantagensNomeadas = tropo.ganhaAoComprar.map { idParaNome[it] ?: it }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !state.criacaoBasicaCongelada) {
                            state.selecionarTropo(tropo)
                            onUserFeedback()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selecionado) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        RadioButtonRow(
                            selected = selecionado,
                            label = if (showOfficialNames && tropo.nome.isNotBlank()) tropo.nome else tropo.nome,
                            onSelect = {
                                if (state.criacaoBasicaCongelada) return@RadioButtonRow
                                state.selecionarTropo(tropo)
                                onUserFeedback()
                            }
                        )

                        if (tropo.tecnicasIniciais > 0) {
                            Text(
                                text = "Técnicas iniciais: ${tropo.tecnicasIniciais}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                            )
                        }

                        Text(
                            text = tropo.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 40.dp, top = 4.dp, end = 8.dp)
                        )

                        if (tropo.ganhaAoComprar.isNotEmpty()) {
                            Spacer(Modifier.size(6.dp))
                            FlowRow(
                                modifier = Modifier.padding(start = 36.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                vantagensNomeadas.forEach { nome ->
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text(nome) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
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
