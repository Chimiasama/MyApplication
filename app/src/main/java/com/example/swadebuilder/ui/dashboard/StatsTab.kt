package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.sections.AtributosContent
import com.example.swadebuilder.ui.sections.PericiasContent

@Composable
fun StatsTab(
    state: CriadorState,
    viewModel: CriadorViewModel,
    isLandscape: Boolean,
    onUserFeedback: () -> Unit
) {
    if (state.mostrandoAtributosProgresso) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            AtributosContent(state, onUserFeedback)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)
            Button(
                onClick = {
                    viewModel.finishAttributeAdvancement()
                    state.mostrandoAtributosProgresso = false
                },
                enabled = state.pontosAtributo == 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Atributo e Voltar")
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
    } else if (state.mostrandoPericiasProgresso) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            PericiasContent(
                state = state,
                feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                onUserFeedback = onUserFeedback,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 3.dp)
            Button(
                onClick = {
                    viewModel.finishSkillAdvancement()
                    state.mostrandoPericiasProgresso = false
                },
                enabled = state.pontosPericia == 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar Perícias e Voltar")
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
    } else {
        if (isLandscape) {
            Row(Modifier.fillMaxSize().padding(8.dp)) {
                // Attributes Panel
                Column(Modifier.weight(0.4f).fillMaxHeight()) {
                    AtributosContent(state, onUserFeedback)
                }

                Spacer(Modifier.width(12.dp))

                // Skills Panel
                PericiasContent(
                    state = state,
                    feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                    onUserFeedback = onUserFeedback,
                    modifier = Modifier.weight(0.6f).fillMaxHeight()
                )
            }
        } else {
            Column(Modifier.fillMaxSize().padding(8.dp)) {
                // Attributes
                AtributosContent(state, onUserFeedback)

                Spacer(Modifier.height(8.dp))

                // Skills take remaining space
                PericiasContent(
                    state = state,
                    feedbackMessages = viewModel.feedbackMessages as MutableList<String>,
                    onUserFeedback = onUserFeedback,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}
