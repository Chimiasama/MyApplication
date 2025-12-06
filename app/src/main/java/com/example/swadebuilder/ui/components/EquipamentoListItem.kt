package com.example.swadebuilder.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            equipamento.observacoes.asText()?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
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
        }
    }
}
