package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `reserveProgressSlot usa estágio do slot e não estagio atual`() {
        val vm = CriadorViewModel()

        vm.state.progresso = 3 // limiar entre Novato e Experiente

        val reservado = vm.reserveProgressSlot(slotIndex = 3) // E1

        assertTrue(reservado)
        assertEquals("Experiente", vm.state.stageNameForCurrentAdvancement)
    }

}
