package com.example.swadebuilder

import com.example.swadebuilder.model.listaDeEstagios
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionSlotRulesTest {

    @Test
    fun `respeita limites inferior e superior de índice de estágio`() {
        assertEquals(0, stageIndexForSlot(-1))
        assertEquals(0, stageIndexForSlot(0))

        val high = stageIndexForSlot(9_999)
        assertTrue(high in 0..listaDeEstagios.lastIndex)
    }

    @Test
    fun `retorna estágio correspondente ao slot`() {
        val firstStage = listaDeEstagios.first()
        assertEquals(firstStage, stageForSlot(0))
        assertEquals(firstStage, stageForSlot(2))

        val expected = listaDeEstagios[1.coerceAtMost(listaDeEstagios.lastIndex)]
        assertEquals(expected, stageForSlot(3))
    }
}
