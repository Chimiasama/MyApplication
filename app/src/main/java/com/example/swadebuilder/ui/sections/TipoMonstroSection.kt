package com.example.swadebuilder.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaMonstroTemplates
import com.example.swadebuilder.ui.components.RadioButtonRow
import com.example.swadebuilder.util.keyify

@Composable
fun TipoMonstroSection(
    state: CriadorState
) {
    if (!state.modoMonstroAtivo) return

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Tipo de Monstro",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Escolha o tipo de monstro que define seu personagem.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        listaMonstroTemplates.forEach { template ->
            val selected = state.tipoMonstroSelecionado == template.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { state.tipoMonstroSelecionado = template.id },
                colors = CardDefaults.cardColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    RadioButtonRow(
                        selected = selected,
                        label = template.nome,
                        onSelect = { state.tipoMonstroSelecionado = template.id }
                    )

                    if (selected) {
                        Text(
                            text = template.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 40.dp, end = 8.dp, bottom = 8.dp)
                        )

                        if (template.atributos_bonus.isNotEmpty()) {
                             Text(
                                text = "Bônus de Atributos: ${template.atributos_bonus.entries.joinToString { "${it.key} d${it.value}" }}", // Simplification: actually steps or die type? Json says int, assuming steps/die.
                                // Looking at json: "Forca": 2. usually means +2 die steps or +2 bonus.
                                // Standard SWADE templates usually give bonuses.
                                // Let's just display it for now.
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, end = 8.dp)
                            )
                        }

                        if (template.habilidades.isNotEmpty()) {
                             Text(
                                text = "Habilidades: ${template.habilidades.joinToString { it.nome }}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 40.dp, end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
