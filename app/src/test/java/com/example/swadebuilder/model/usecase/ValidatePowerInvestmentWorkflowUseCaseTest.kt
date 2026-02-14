package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatePowerInvestmentWorkflowUseCaseTest {

    private val useCase = ValidatePowerInvestmentWorkflowUseCase()

    @Test
    fun `retorna erro basico quando sem saldo`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentWorkflowUseCase.Input(
                poderId = "sp_test",
                custo = 3,
                superPontosDisponiveis = 1,
                gastosPorPoder = emptyMap(),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 5,
                effect = ValidatePowerInvestmentWorkflowUseCase.EffectInput.Other
            )
        )

        assertEquals("Sem saldo: precisa de 3, tem 1.", erro)
    }

    @Test
    fun `retorna erro de validacao especial para poder generico`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentWorkflowUseCase.Input(
                poderId = "sp_generico",
                custo = 1,
                superPontosDisponiveis = 10,
                gastosPorPoder = emptyMap(),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 5,
                effect = ValidatePowerInvestmentWorkflowUseCase.EffectInput.Generico(
                    effectNameKey = "SUPERCIENCIA",
                    ocultismoRaw = null,
                    cienciaRaw = 8
                )
            )
        )

        assertEquals("Requer Ciência d10+ (atual: d8).", erro)
    }

    @Test
    fun `retorna null quando validacoes passam`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentWorkflowUseCase.Input(
                poderId = "sp_ok",
                custo = 1,
                superPontosDisponiveis = 10,
                gastosPorPoder = emptyMap(),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 5,
                effect = ValidatePowerInvestmentWorkflowUseCase.EffectInput.Other
            )
        )

        assertNull(erro)
    }
}
