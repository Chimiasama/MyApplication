package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = cardColors,
        onClick = { if (isClickable) onClick(vantagem) }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = vantagem.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = vantagem.descricao,
                style = MaterialTheme.typography.bodyMedium
            )
            if (vantagem.hasRequirements()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Requisitos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        SuggestionChip(onClick = {}, label = { Text("$attr $value") })
                    }
                    vantagem.requisitos.periciaMin.forEach { (skill, value) ->
                        SuggestionChip(onClick = {}, label = { Text("$skill $value") })
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
