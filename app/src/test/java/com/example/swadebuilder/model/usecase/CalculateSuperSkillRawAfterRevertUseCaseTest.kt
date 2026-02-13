package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateSuperSkillRawAfterRevertUseCaseTest {

    private val useCase = CalculateSuperSkillRawAfterRevertUseCase()

    @Test
    fun `calcula raw apos reverter parte dos steps`() {
        val rawAfter = useCase.execute(
            CalculateSuperSkillRawAfterRevertUseCase.Input(
                baseRaw = 6,
                currentSuperSteps = 3,
                revertingSteps = 1,
                applySteps = { raw, steps -> raw + steps }
            )
        )

        assertEquals(8, rawAfter)
    }

    @Test
    fun `nao permite steps negativos apos reversao`() {
        val rawAfter = useCase.execute(
            CalculateSuperSkillRawAfterRevertUseCase.Input(
                baseRaw = 6,
                currentSuperSteps = 1,
                revertingSteps = 5,
                applySteps = { raw, steps -> raw + steps }
            )
        )

        assertEquals(6, rawAfter)
    }
}
