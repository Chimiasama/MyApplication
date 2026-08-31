package com.example.swadebuilder.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.paraCaracteristicas
import com.example.swadebuilder.ui.components.SelectableItemRow
import com.example.swadebuilder.ui.components.SectionCard
import com.example.swadebuilder.ui.components.SelectionMode

@Composable
fun TipoMonstroSection(
    state: CriadorState,
    onUserFeedback: () -> Unit,
    onLogFeedback: (String) -> Unit
) {
    if (!state.modoMonstroAtivo) return

    SectionCard(
        title = "Tipo de Monstro",
        icon = Icons.Default.BugReport,
        showHeader = false
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Escolha o tipo de monstro que define seu personagem.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            state.listaMonstroTemplates.forEach { template ->
                val selected = state.tipoMonstroSelecionado == template.id

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    SelectableItemRow(
                        title = template.nome,
                        selected = selected,
                        mode = SelectionMode.UNICA,
                        onClick = {
                            onUserFeedback()
                            state.aplicarTipoMonstro(template.id).forEach(onLogFeedback)
                        }
                    )

                    if (selected) {
                        Text(
                            text = template.descricao,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp)
                        )

                        val caracteristicas = template.paraCaracteristicas()
                        if (caracteristicas.isNotEmpty()) {
                            Text(
                                text = "Características:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp)
                            )
                            caracteristicas.forEach { linha ->
                                Text(
                                    text = "• $linha",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 20.dp, end = 8.dp, bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
