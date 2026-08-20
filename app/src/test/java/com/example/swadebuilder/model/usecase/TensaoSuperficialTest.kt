package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.PowerEffect
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TensaoSuperficialTest {

    @Test
    fun `rejeita Tensao Superficial se base de velocidade for menor que 13`() {
        val vm = CriadorViewModel()

        vm.state.superPontosDisponiveis = 50
        vm.state.superPontosTotais = 50

        val check = vm.canInvestInPower(
            poderId = "sp_VELOCIDADE",
            custo = 14,
            efeito = PowerEffect.Generico("Velocidade"),
            baseCost = 11,
            modifiers = mapOf("Tensão superficial" to 1)
        )

        assertFalse(check.ok)
        assertTrue(check.motivoBloqueio != null && check.motivoBloqueio!!.contains("Tensão Superficial"))
    }

    @Test
    fun `aceita Tensao Superficial se base de velocidade for maior ou igual a 13`() {
        val vm = CriadorViewModel()
        vm.state.superPontosDisponiveis = 50
        vm.state.superPontosTotais = 50

        val check = vm.canInvestInPower(
            poderId = "sp_VELOCIDADE",
            custo = 14,
            efeito = PowerEffect.Generico("Velocidade"),
            baseCost = 13,
            modifiers = mapOf("Tensão superficial" to 1)
        )

        assertTrue(check.ok)
    }
}
