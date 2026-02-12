package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatePowerInvestmentUseCaseTest {

    private val useCase = ValidatePowerInvestmentUseCase()

    @Test
    fun `retorna erro quando custo eh invalido`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = "sp_test",
                custo = 0,
                superPontosDisponiveis = 10,
                gastosPorPoder = emptyMap(),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 5
            )
        )

        assertEquals("Custo inválido.", erro)
    }

    @Test
    fun `retorna erro quando nao ha saldo de super pontos`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = "sp_test",
                custo = 3,
                superPontosDisponiveis = 2,
                gastosPorPoder = emptyMap(),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 5
            )
        )

        assertEquals("Sem saldo: precisa de 3, tem 2.", erro)
    }

    @Test
    fun `retorna erro quando excede limite individual por poder`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = "sp_test",
                custo = 2,
                superPontosDisponiveis = 10,
                gastosPorPoder = mapOf("sp_test" to 4),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 8
            )
        )

        assertEquals("Limite de gasto neste poder excedido em 1 (limite: 5).", erro)
    }

    @Test
    fun `retorna erro quando excede limite compartilhado de armadura e resistencia`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = "sp_armor",
                custo = 2,
                superPontosDisponiveis = 10,
                gastosPorPoder = mapOf("sp_armor" to 2, "sp_res" to 3),
                limitePorPoder = 10,
                limiteCompartilhadoArmaduraResistencia = 6
            )
        )

        assertEquals(
            "Limite compartilhado de Armadura+Resistência excedido em 1 (gasto previsto: 7 / limite 6).",
            erro
        )
    }

    @Test
    fun `retorna null quando investimento basico eh valido`() {
        val erro = useCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = "sp_generic",
                custo = 2,
                superPontosDisponiveis = 10,
                gastosPorPoder = mapOf("sp_generic" to 1),
                limitePorPoder = 5,
                limiteCompartilhadoArmaduraResistencia = 6
            )
        )

        assertNull(erro)
    }
}
