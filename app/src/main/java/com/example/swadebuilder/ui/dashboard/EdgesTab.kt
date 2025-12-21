package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.sections.ComplicacoesContent
import com.example.swadebuilder.ui.sections.VantagensContent

@Composable
fun EdgesTab(
    state: CriadorState,
    viewModel: CriadorViewModel,
    onUserFeedback: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.mostrandoVantagensProgresso) {
        if (state.mostrandoVantagensProgresso) tabIndex = 0
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            Tab(
                selected = tabIndex == 0,
                onClick = { tabIndex = 0 },
                text = { Text("Vantagens") }
            )
            Tab(
                selected = tabIndex == 1,
                onClick = { tabIndex = 1 },
                text = { Text("Complicações") }
            )
        }

        // Content Area
        Column(Modifier.weight(1f)) {
            when (tabIndex) {
                0 -> VantagensContent(
                    state = state,
                    multiplosAAHabilitados = state.permiteMultiAntecedenteArcano,
                    viewModel = viewModel,
                    onUserFeedback = onUserFeedback
                )
                1 -> ComplicacoesContent(
                    state = state,
                    feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                    onUserFeedback = onUserFeedback
                )
            }
        }

        // Progression Buttons
        if (state.mostrandoVantagensProgresso) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(thickness = 3.dp)
            Button(
                onClick = {
                    viewModel.finishAdvantageAdvancement()
                    state.mostrandoVantagensProgresso = false
                },
                enabled = state.pontosVantagem == 0 && !state.arcanoCompraPendente(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Vantagem e Voltar")
            }
            TextButton(
                onClick = {
                    viewModel.cancelAdvancementInProgress()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
