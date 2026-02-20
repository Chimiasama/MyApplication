package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestrySpecificAdjustmentsUseCaseTest {

    private val useCase = ResolveAncestrySpecificAdjustmentsUseCase()

    @Test
    fun `returns saurios adjustments`() {
        val result = useCase.execute("SAURIOS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf("PRONTIDÃO"), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns golens adjustments`() {
        val result = useCase.execute("GOLENS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns draconianos adjustments`() {
        val result = useCase.execute("DRACONIANOS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns insetoides adjustments`() {
        val result = useCase.execute("INSETOIDES", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf("GARRAS"), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns pequeninos adjustments`() {
        val result = useCase.execute("PEQUENINOS", null)

        assertEquals(listOf("Sorte", "Espirituoso"), result.ensureAdvantageNames)
        assertEquals(listOf("Tamanho -1", "Movimentação Reduzida"), result.ensureRacialDisadvantages)
        assertTrue(result.forceArmorZero)
    }

    @Test
    fun `returns descendente elemental action based on current selection`() {
        val withoutCurrent = useCase.execute("DESCENDENTE ELEMENTAL", null)
        val withCurrent = useCase.execute("DESCENDENTE ELEMENTAL", "Fogo")

        assertEquals(
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.SELECT_DEFAULT,
            withoutCurrent.elementalAction
        )
        assertEquals(
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.REAPPLY_CURRENT,
            withCurrent.elementalAction
        )
    }

    @Test
    fun `returns fallback for unknown ancestry`() {
        val result = useCase.execute("QUALQUER", null)

        assertEquals(0, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }
}
