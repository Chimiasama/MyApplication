package com.example.swadebuilder.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.ui.sections.CircleStat

@Composable
fun LiveSummaryPanel(state: CriadorState) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(end = 8.dp) // padding for scrollbar area if needed
    ) {
        Text(
            text = "Resumo",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // --- Orçamento (Points Budget) ---
        PointBudgetCard(state)

        Spacer(Modifier.height(12.dp))

        // --- Atributos Derivados ---
        DerivedStatsCard(state)

        Spacer(Modifier.height(12.dp))

        // --- Status (Load, Wounds, etc if implemented later) ---
        LoadStatusCard(state)
    }
}

@Composable
fun PointBudgetCard(state: CriadorState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Pontos Disponíveis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))

            BudgetRow("Atributos", state.pontosAtributo, 0)
            BudgetRow("Perícias", state.pontosPericia, 0)
            BudgetRow("Vantagens", state.pontosVantagem, 0)

            val pcLivres = (state.pontosComplicacao - state.pontosComplicacaoGastos).coerceAtLeast(0)
            BudgetRow("Complicações (Ganhos)", pcLivres, null, positiveIsGood = true)

            HorizontalDivider(Modifier.padding(vertical = 8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dinheiro", style = MaterialTheme.typography.bodyMedium)
                Text("$${state.dinheiro}", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BudgetRow(label: String, value: Int, threshold: Int?, positiveIsGood: Boolean = false) {
    val color = if (threshold != null) {
        if (positiveIsGood) {
            if (value > threshold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        } else {
            if (value < threshold) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        }
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value.toString(), fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DerivedStatsCard(state: CriadorState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Estatísticas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CircleStat("Aparar", state.valorAparar().toString())
                CircleStat("Resist.", "${state.valorResistenciaFinal()}(${state.valorArmaduraEfetiva()})")
                CircleStat("Mov.", state.valorMovimentacao().toString())
            }
        }
    }
}

@Composable
fun LoadStatusCard(state: CriadorState) {
    val totalWeight = state.equipamentosComprados.sumOf { item ->
         (item.peso as? kotlinx.serialization.json.JsonPrimitive)
            ?.content?.replace(",", ".")?.toFloatOrNull() ?: 0f
    }
    val maxLoad = state.valorCargaMaxima()
    val isOverloaded = totalWeight > maxLoad

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Carga", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Peso Total", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${"%.1f".format(totalWeight)} / ${"%.1f".format(maxLoad)}",
                    fontWeight = FontWeight.Bold,
                    color = if (isOverloaded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }

            if (isOverloaded) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Text(
                        " Sobrecarga (-1 a -3)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
