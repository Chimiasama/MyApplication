package com.example.swadebuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.Vantagem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VantagemListItem(
    vantagem: Vantagem,
    isSelected: Boolean,
    requirementsMet: Boolean,
    isClickable: Boolean = false,
    onClick: (Vantagem) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val showFull = booleanResource(R.bool.show_full_descriptions)

    val cardColors = CardDefaults.cardColors(
        containerColor = when {
            isSelected -> MaterialTheme.colorScheme.tertiaryContainer
            !requirementsMet -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = isClickable, onClick = { onClick(vantagem) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vantagem.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = when {
                        isSelected -> "Já possui"
                        !requirementsMet -> "Não cumpre"
                        else -> "Cumpre"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vantagem.hasRequirements()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    vantagem.requisitos.estagio.let {
                        if (it.isNotBlank()) SuggestionChip(onClick = {}, label = { Text("Estágio: $it") })
                    }
                    vantagem.requisitos.vantagensPrevias.forEach {
                        SuggestionChip(onClick = {}, label = { Text(it) })
                    }
                    vantagem.requisitos.atributoMin.forEach { (attr, value) ->
                        SuggestionChip(onClick = {}, label = { Text("$attr d$value") })
                    }
                    vantagem.requisitos.periciaMin.forEach { (skill, value) ->
                        SuggestionChip(onClick = {}, label = { Text("$skill d$value") })
                    }
                }
            }

            if (showFull) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Ocultar" else "Detalhes")
                    }
                }
                AnimatedVisibility(visible = expanded) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = vantagem.descricao,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

private fun Vantagem.hasRequirements(): Boolean {
    return requisitos.estagio.isNotBlank() ||
            requisitos.vantagensPrevias.isNotEmpty() ||
            requisitos.atributoMin.isNotEmpty() ||
            requisitos.periciaMin.isNotEmpty()
}
