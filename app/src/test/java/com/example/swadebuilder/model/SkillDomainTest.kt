package com.example.swadebuilder.model

import androidx.compose.runtime.mutableStateMapOf
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SkillDomainTest {

    private lateinit var state: CriadorState
    private lateinit var fighting: Pericia
    private lateinit var shooting: Pericia

    @Before
    fun setup() {
        // We mock CriadorState because it has complex initialization
        state = mock()
        fighting = Pericia("Lutar", "AGILIDADE", true)
        shooting = Pericia("Atirar", "AGILIDADE", true)
    }

    @Test
    fun `calculateCost returns correct values for base skill below attribute`() {
        // Setup: Agility d8, Fighting d4
        val attrKey = "AGILIDADE"
        val attrValue = 8 // d8
        val currentSkill = 4 // d4

        whenever(state.rawTotal(fighting)).thenReturn(currentSkill)
        whenever(state.atributoBaseParaPericia(fighting)).thenReturn(attrKey)
        whenever(state.valoresAtributos).thenReturn(mapOf(attrKey to mutableStateInt(attrValue)))
        whenever(state.periciaCapRaw(fighting)).thenReturn(12)
        whenever(state.rawTotalComSupers(fighting)).thenReturn(currentSkill)

        val result = SkillDomain.calculateCost(state, fighting)

        // Next step is d6 (4 -> 6)
        // Cost is 1 because 6 <= 8
        assertEquals(6, result.nextRaw)
        assertEquals(1, result.cost)
        assertEquals("d4", result.displayDice)
    }

    @Test
    fun `calculateCost returns double cost for skill above attribute`() {
        // Setup: Agility d6, Fighting d6
        val attrKey = "AGILIDADE"
        val attrValue = 6 // d6
        val currentSkill = 6 // d6

        whenever(state.rawTotal(fighting)).thenReturn(currentSkill)
        whenever(state.atributoBaseParaPericia(fighting)).thenReturn(attrKey)
        whenever(state.valoresAtributos).thenReturn(mapOf(attrKey to mutableStateInt(attrValue)))
        whenever(state.periciaCapRaw(fighting)).thenReturn(12)
        whenever(state.rawTotalComSupers(fighting)).thenReturn(currentSkill)

        val result = SkillDomain.calculateCost(state, fighting)

        // Next step is d8 (6 -> 8)
        // Cost is 2 because 8 > 6
        assertEquals(8, result.nextRaw)
        assertEquals(2, result.cost)
        assertEquals("d6", result.displayDice)
    }

    @Test
    fun `canIncrease returns false if not enough points`() {
        val costResult = SkillCostResult(
            currentRaw = 4, nextRaw = 6, cost = 2, capRaw = 12, attrRaw = 6, displayDice = "d4"
        )

        whenever(state.pontosPericia).thenReturn(1) // Only 1 point
        whenever(state.criacaoBasicaCongelada).thenReturn(false)
        whenever(state.skillAdvancementInProgress).thenReturn(false)
        whenever(state.idosoBonusSp).thenReturn(0)

        assertFalse(SkillDomain.canIncrease(state, fighting, costResult))
    }

    @Test
    fun `canIncrease returns true if points sufficient`() {
        val costResult = SkillCostResult(
            currentRaw = 4, nextRaw = 6, cost = 1, capRaw = 12, attrRaw = 8, displayDice = "d4"
        )

        whenever(state.pontosPericia).thenReturn(1)
        whenever(state.criacaoBasicaCongelada).thenReturn(false)
        whenever(state.skillAdvancementInProgress).thenReturn(false)
        whenever(state.idosoBonusSp).thenReturn(0)

        assertTrue(SkillDomain.canIncrease(state, fighting, costResult))
    }

    // Helper to mock MutableState<Int> behavior if needed,
    // but here we just need the map to return something that has .intValue
    private fun mutableStateInt(value: Int): androidx.compose.runtime.MutableState<Int> {
        val m = mock<androidx.compose.runtime.MutableState<Int>>()
        whenever(m.intValue).thenReturn(value)
        return m
    }
}
