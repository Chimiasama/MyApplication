package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCurrentSuperSkillStepsUseCaseTest {

    private val useCase = CalculateCurrentSuperSkillStepsUseCase()

    @Test
    fun `soma steps da pericia alvo ignorando case`() {
        val out = useCase.execute(
            CalculateCurrentSuperSkillStepsUseCase.Input(
                targetSkillName = "Ocultismo",
                investments = listOf(
                    CalculateCurrentSuperSkillStepsUseCase.Investment("OCULTISMO", 2),
                    CalculateCurrentSuperSkillStepsUseCase.Investment("ocultismo", 1),
                    CalculateCurrentSuperSkillStepsUseCase.Investment("CIENCIA", 3)
                )
            )
        )

        assertEquals(3, out)
    }

    @Test
    fun `retorna zero quando nao ha investimentos para a pericia alvo`() {
        val out = useCase.execute(
            CalculateCurrentSuperSkillStepsUseCase.Input(
                targetSkillName = "ATLETISMO",
                investments = listOf(
                    CalculateCurrentSuperSkillStepsUseCase.Investment("OCULTISMO", 2)
                )
            )
        )

        assertEquals(0, out)
    }
}
