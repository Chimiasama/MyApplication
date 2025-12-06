package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

fun JsonElement?.asText(): String? = when (this) {
    is JsonPrimitive -> this.content
    else -> this?.toString()
}?.takeIf { it.isNotBlank() && it != "\"\"" }

@Composable
fun VantagemListItem(
    vantagem: Vantagem,
    isRequirementMet: Boolean,
    isAlreadyAcquired: Boolean,
    idToNameMap: Map<String, String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAlreadyAcquired -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                !isRequirementMet -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = vantagem.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                val statusText = when {
                    isAlreadyAcquired -> "Adquirida"
                    !isRequirementMet -> "Requisitos pendentes"
                    else -> ""
                }
                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isAlreadyAcquired -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = vantagem.descricao,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            val reqs = mutableListOf<String>()
            if (vantagem.requisitos.estagio.isNotBlank() && vantagem.requisitos.estagio != "Noviço") {
                reqs.add("Estágio: ${vantagem.requisitos.estagio}")
            }
            vantagem.requisitos.atributoMin.forEach { (attr, value) ->
                reqs.add("$attr $value")
            }
            vantagem.requisitos.periciaMin.forEach { (skill, value) ->
                reqs.add("$skill $value")
            }
            if (vantagem.requisitos.periciaMinOpcional.isNotEmpty()) {
                val optionalSkills = vantagem.requisitos.periciaMinOpcional.map { (skill, value) ->
                    "$skill $value"
                }.joinToString(" ou ")
                reqs.add(optionalSkills)
            }
            vantagem.requisitos.vantagensPrevias.forEach { vantId ->
                val vantName = idToNameMap[vantId] ?: vantId.replaceFirstChar { it.uppercase() }
                reqs.add("Vantagem: $vantName")
            }
            if (vantagem.requisitos.exigeCS) {
                reqs.add("Requer Carta Selvagem")
            }
            if(vantagem.requisitos.observacoes.isNotBlank()){
                reqs.add("Obs: ${vantagem.requisitos.observacoes}")
            }


            if (reqs.isNotEmpty()) {
                Text(
                    text = "Requisitos: ${reqs.joinToString(" • ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EquipamentoListItem(
    equipamento: com.example.swadebuilder.model.EquipamentoItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = equipamento.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                equipamento.custo.asText()?.let {
                    Text(
                        text = "$ $it",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            equipamento.observacoes.asText()?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Weapon Stats
            val weaponStats = listOfNotNull(
                equipamento.dano.asText()?.let { "Dano: $it" },
                equipamento.pa.asText()?.let { "PA: $it" },
                equipamento.cdt.asText()?.let { "CdT: $it" },
                equipamento.distancia.asText()?.let { "Dist: $it" },
                equipamento.tiros.asText()?.let { "Tiros: $it" },
            ).joinToString(" • ")

            if (weaponStats.isNotBlank()) {
                Text(
                    text = weaponStats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // General Stats
            val generalStats = listOfNotNull(
                equipamento.peso.asText()?.let { "Peso: $it" },
                equipamento.forcaMin.asText()?.let { "Força Mín: $it" },
                equipamento.armadura.asText()?.let { "Armadura: +$it" },
                equipamento.aparar.asText()?.let { "Aparar: +$it" },
            ).joinToString(" • ")

            if (generalStats.isNotBlank()) {
                Text(
                    text = generalStats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Vehicle Stats
            val vehicleStats = listOfNotNull(
                equipamento.velMaxima.asText()?.let { "Vel. Máx: $it" },
                equipamento.manobrabilidade.asText()?.let { "Manob: $it" },
                equipamento.tamanho.asText()?.let { "Tamanho: $it" },
                equipamento.resistencia.asText()?.let { "Resistência/Blindagem: $it" },
                equipamento.tripulacao.asText()?.let { "Tripulação/Passageiros: $it" },
            ).joinToString(" • ")

            if (vehicleStats.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = vehicleStats,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
