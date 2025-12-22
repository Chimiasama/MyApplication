package com.example.swadebuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.ui.sections.toResumo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun StandardEquipamentoItem(
    equipamento: EquipamentoItem,
    onClick: () -> Unit,
    allowLongTexts: Boolean,
    origemLabel: String? = null,
    showOriginalName: Boolean = false,
    showTensao: Boolean = false
) {
    val resumo = equipamento.toResumo()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (showOriginalName && !equipamento.originalName.isNullOrBlank()) equipamento.originalName else equipamento.nomeExibicao,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        origemLabel?.takeIf { it.isNotBlank() }?.let { origem ->
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = origem,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                resumo.custo?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Main Stats Row
            val stats = listOfNotNull(
                resumo.linhaArma,
                resumo.linhaGeral,
                resumo.linhaVeiculo
            )

            stats.forEach { statLine ->
                 Text(
                    text = statLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                 )
            }

            resumo.observacao?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Details
            val detalhes = buildList {
                // Fields already in summary (linhaArma, linhaGeral, linhaVeiculo, observacao) are excluded here to avoid duplication.
                if (showTensao) {
                    equipamento.tensao?.let { add("Tensão: $it") }
                }
                equipamento.mods_slots?.let { add("Slots de Mods: $it") }
            }

            if (allowLongTexts && detalhes.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Column(Modifier.padding(bottom = 4.dp)) {
                    detalhes.forEach { linha ->
                        Text(
                            text = "• $linha",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun JsonElement?.contentString(): String? = this
    ?.jsonPrimitive
    ?.contentOrNull
    ?.takeIf { it.isNotBlank() }
