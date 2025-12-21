package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.ui.sections.PoderesSection
import com.example.swadebuilder.ui.sections.SuperPoderesSection

@Composable
fun PowersTab(
    state: CriadorState,
    viewModel: CriadorViewModel,
    listaSuperPoderes: List<SuperPoder>,
    onUserFeedback: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val temArcano = state.vantagensSelecionadas.any { it.id.startsWith("antecedente_arcano") || it.nome.startsWith("Antecedente Arcano") }

        if (temArcano) {
            Text("Poderes Arcanos", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            PoderesSection(state = state)
        }

        if (state.modoSupers) {
            if (temArcano) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }
            Text("Superpoderes", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            SuperPoderesSection(
                state = state,
                listaSuperPoderes = listaSuperPoderes,
                expanded = true,
                viewModel = viewModel
            )
        }
    }
}
