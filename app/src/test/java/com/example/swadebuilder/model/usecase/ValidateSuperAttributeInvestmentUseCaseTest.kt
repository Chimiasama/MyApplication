package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateSuperAttributeInvestmentUseCaseTest {

    private val useCase = ValidateSuperAttributeInvestmentUseCase(technicalCap = 30)

    @Test
    fun `retorna null quando nao ha steps`() {
        val erro = useCase.execute(
            ValidateSuperAttributeInvestmentUseCase.Input(
                currentRaw = 12,
                steps = 0,
                applySteps = { raw, delta -> raw + delta }
            )
        )

        assertNull(erro)
    }

    @Test
    fun `retorna erro quando alvo ultrapassa teto tecnico`() {
        val erro = useCase.execute(
            ValidateSuperAttributeInvestmentUseCase.Input(
                currentRaw = 29,
                steps = 1,
                applySteps = { raw, delta -> raw + delta * 2 }
            )
        )

        assertEquals("Limite técnico de atributo excedido (30).", erro)
    }

    @Test
    fun `retorna null quando alvo fica dentro do teto`() {
        val erro = useCase.execute(
            ValidateSuperAttributeInvestmentUseCase.Input(
                currentRaw = 20,
                steps = 2,
                applySteps = { raw, delta -> raw + delta }
            )
        )

        assertNull(erro)
    }
}
