package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class AdjustNonNegativeBonusUseCaseTest {

    private val useCase = AdjustNonNegativeBonusUseCase()

    @Test
    fun `soma delta positivo`() {
        val out = useCase.execute(
            AdjustNonNegativeBonusUseCase.Input(
                currentValue = 2,
                delta = 3
            )
        )

        assertEquals(5, out)
    }

    @Test
    fun `aplica clamp em zero quando delta negativo excede valor atual`() {
        val out = useCase.execute(
            AdjustNonNegativeBonusUseCase.Input(
                currentValue = 2,
                delta = -5
            )
        )

        assertEquals(0, out)
    }
}
