package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.toMeuPersonagem

@Composable
fun SummaryContent(state: CriadorState) {
    val personagem = state.toMeuPersonagem()

    val flagsTemplate = remember(state) {
        listOfNotNull(
            "Carta Selvagem".takeIf { state.cartaSelvagem },
            "Heróis Sem Armadura".takeIf { state.heroisSemArmadura },
            "Sem Pontos de Poder".takeIf { state.usarSemPontosDePoder },
            "Mais Pontos de Perícias".takeIf { state.maisPontosPericias },
            "Especializações".takeIf { state.usarEspecializacoesDePericia }
        )
    }

    LaunchedEffect(flagsTemplate) {
        if (state.anotacoes.isBlank() && flagsTemplate.isNotEmpty()) {
            state.anotacoes = flagsTemplate.joinToString("\n") { "• $it" }
        }
    }

    // Main layout
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Identity and Derived Attributes
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Identity
                Text(
                    text = personagem.nome.ifBlank { "(Sem Nome)" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Raça: ${personagem.ancestralidade}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Derived Attributes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val resistenciaTexto = if (personagem.armadura > 0) {
                        "${personagem.resistencia}(${personagem.armadura})"
                    } else {
                        personagem.resistencia.toString()
                    }

                    DerivedAttribute("Aparar", personagem.aparar.toString())
                    DerivedAttribute("Resistência", resistenciaTexto)
                    DerivedAttribute("Mov.", personagem.movimento.toString())
                    DerivedAttribute("Tamanho", personagem.tamanho.toString())
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Main info grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Left Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Attributes
                SummaryCard("Atributos") {
                    personagem.atributos.forEach { (attr, dado) ->
                        AttributeChip(attr, "d$dado")
                    }
                }

                // Skills
                SummaryCard("Perícias") {
                    val pericias = personagem.pericias
                        .toList()
                        .sortedBy { it.first }
                    if (pericias.isEmpty()) {
                        Text("– Nenhuma")
                    } else {
                        pericias.forEach { (nome, dado) ->
                            Text("• $nome: d$dado")
                        }
                    }
                }

                // Arcane Powers
                if (personagem.poderes.isNotEmpty()) {
                    SummaryCard("Poderes Arcanos") {
                        personagem.poderes.forEach { (arcano, lista) ->
                            val poderes = if (lista.isEmpty()) {
                                "– nenhum"
                            } else {
                                lista.joinToString(", ")
                            }
                            Text("• $arcano: $poderes")
                        }
                    }
                }
            }

            // Right Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Advantages
                SummaryCard("Vantagens") {
                    if (personagem.vantagens.isEmpty()) {
                        Text("– Nenhuma")
                    } else {
                        Text(personagem.vantagens.joinToString(", "))
                    }
                }

                // Hindrances
                SummaryCard(
                    "Complicações",
                    icon = Icons.Default.Warning
                ) {
                    val allComps = personagem.complicacoes + personagem.desvantagensRaciais
                    if (allComps.isEmpty()) {
                        Text("– Nenhuma")
                    } else {
                        Text(allComps.joinToString(", "))
                    }
                }

                // Super Powers
                if (personagem.modoSupers) {
                    SummaryCard("Superpoderes") {
                        if (personagem.gastosPorPoder.isEmpty()) {
                            Text("– Nenhum")
                        } else {
                            personagem.gastosPorPoder.forEach { (poder, custo) ->
                                Text("• $poder: $custo SP")
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Notes
        OutlinedTextField(
            value = state.anotacoes,
            onValueChange = { state.anotacoes = it },
            label = { Text("Anotações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

@Composable
fun AttributeChip(
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = "$title Icon",
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            SectionTitle(title, icon)
            Spacer(modifier = Modifier.height(4.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                content()
            }
        }
    }
}

@Composable
fun DerivedAttribute(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
