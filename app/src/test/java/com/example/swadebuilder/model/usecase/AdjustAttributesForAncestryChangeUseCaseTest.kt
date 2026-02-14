package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class AdjustAttributesForAncestryChangeUseCaseTest {

    private val useCase = AdjustAttributesForAncestryChangeUseCase()

    @Test
    fun `keeps all steps when new max allows full progression`() {
        val result = useCase.execute(
            AdjustAttributesForAncestryChangeUseCase.Params(
                attributeNames = listOf("FORCA"),
                attributeCaps = mapOf(
                    "FORCA" to AdjustAttributesForAncestryChangeUseCase.AttributeCap(minRaw = 4, maxRaw = 10)
                ),
                paCostStacks = mapOf("FORCA" to listOf(2, 2, 2))
            )
        )

        val adjustment = result.adjustmentsByAttribute.getValue("FORCA")
        assertEquals(10, adjustment.newRaw)
        assertEquals(listOf(2, 2, 2), adjustment.adjustedStack)
        assertEquals(0, adjustment.refundedPoints)
    }

    @Test
    fun `refunds exceeding steps when new max is lower`() {
        val result = useCase.execute(
            AdjustAttributesForAncestryChangeUseCase.Params(
                attributeNames = listOf("AGILIDADE"),
                attributeCaps = mapOf(
                    "AGILIDADE" to AdjustAttributesForAncestryChangeUseCase.AttributeCap(minRaw = 4, maxRaw = 8)
                ),
                paCostStacks = mapOf("AGILIDADE" to listOf(2, 2, 2))
            )
        )

        val adjustment = result.adjustmentsByAttribute.getValue("AGILIDADE")
        assertEquals(8, adjustment.newRaw)
        assertEquals(listOf(2, 2), adjustment.adjustedStack)
        assertEquals(1, adjustment.refundedPoints)
    }

    @Test
    fun `uses d12 plus progression after reaching raw 12`() {
        val result = useCase.execute(
            AdjustAttributesForAncestryChangeUseCase.Params(
                attributeNames = listOf("VIGOR"),
                attributeCaps = mapOf(
                    "VIGOR" to AdjustAttributesForAncestryChangeUseCase.AttributeCap(minRaw = 12, maxRaw = 14)
                ),
                paCostStacks = mapOf("VIGOR" to listOf(2, 2, 2))
            )
        )

        val adjustment = result.adjustmentsByAttribute.getValue("VIGOR")
        assertEquals(14, adjustment.newRaw)
        assertEquals(listOf(2, 2), adjustment.adjustedStack)
        assertEquals(1, adjustment.refundedPoints)
    }
}
