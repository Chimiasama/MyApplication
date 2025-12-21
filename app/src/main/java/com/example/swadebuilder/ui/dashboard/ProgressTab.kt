package com.example.swadebuilder.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.ui.dialogs.ProgressosDialog
import com.example.swadebuilder.ui.sections.XpContent

@Composable
fun ProgressTab(
    state: CriadorState,
    viewModel: CriadorViewModel
) {
    var showAllocDialog by rememberSaveable { mutableStateOf(false) }
    var currentSlotIndex by rememberSaveable { mutableIntStateOf(-1) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        XpContent(
            state = state,
            onUseProgress = { index ->
                currentSlotIndex = index
                showAllocDialog = true
            },
            onUndo = {
                viewModel.revertLastAdvancement()
            }
        )
    }

    if (showAllocDialog) {
        ProgressosDialog(
            state = state,
            slotIndex = currentSlotIndex,
            onDismiss = { showAllocDialog = false },
            onStartSkillAdvancement = { slotIndex, stage ->
                viewModel.startSkillAdvancement(slotIndex, stage)
            },
            onStartAdvantageAdvancement = { slotIndex, est ->
                viewModel.startAdvantageAdvancement(slotIndex, est)
            },
            onStartAttributeAdvancement = { slotIndex, stage, consumeReservation ->
                viewModel.startAttributeAdvancement(slotIndex, stage, consumeReservation)
            },
            onReserveLegendaryAttribute = { slotIndex, stage ->
                viewModel.reserveLegendaryAttribute(slotIndex, stage)
            }
        )
    }
}
