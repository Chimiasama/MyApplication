package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CriadorViewModelProgressUndoTest {

    @Test
    fun `undoLastProgressAction cancela pendencia antes de desfazer historico`() {
        val vm = CriadorViewModel()

        vm.state.xpSlots[0] = true
        vm.state.progresso = 1
        vm.state.stageNameForCurrentAdvancement = "Experiente"
        vm.state.stageXpSpent["Experiente"] = 1
        vm.state.advantageAdvancementInProgress = true
        vm.state.updateEmProgressoFlag()

        vm.undoLastProgressAction()

        assertFalse(vm.state.xpSlots[0])
        assertEquals(0, vm.state.progresso)
        assertEquals(0, vm.state.stageXpSpent.getValue("Experiente"))
        assertFalse(vm.state.advantageAdvancementInProgress)
        assertNull(vm.state.stageNameForCurrentAdvancement)
        assertFalse(vm.state.emProgresso)
    }
}
