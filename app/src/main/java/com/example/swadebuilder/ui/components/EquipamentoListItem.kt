package com.example.swadebuilder.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.R
import com.example.swadebuilder.model.EquipamentoItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipamentoListItem(
    equipamento: EquipamentoItem,
    isClickable: Boolean = false,
    onClick: (EquipamentoItem) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val showFull = booleanResource(R.bool.show_full_descriptions)

    fun JsonElement?.asText(): String? = when (this) {
        is JsonPrimitive -> this.content
        else -> this?.toString()
    }?.takeIf { it.isNotBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        onClick = { if (isClickable) onClick(equipamento) }
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = equipamento.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                equipamento.custo.asText()?.let {
                    Text(
                        text = "$$it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                equipamento.dano.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Dano: $it") }) }
                equipamento.pa.asText()?.let { SuggestionChip(onClick = {}, label = { Text("PA: $it") }) }
                equipamento.cdt.asText()?.let { SuggestionChip(onClick = {}, label = { Text("CdT: $it") }) }
                equipamento.distancia.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Dist.: $it") }) }
                equipamento.peso.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Peso: $it") }) }
                equipamento.forcaMin.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Força: $it") }) }
                equipamento.armadura.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Armadura: $it") }) }
                equipamento.aparar.asText()?.let { SuggestionChip(onClick = {}, label = { Text("Aparar: $it") }) }
            }
            if (showFull && equipamento.observacoes.asText() != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Ocultar" else "Detalhes")
                    }
                }
                AnimatedVisibility(visible = expanded) {
                    Column {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        equipamento.observacoes.asText()?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}