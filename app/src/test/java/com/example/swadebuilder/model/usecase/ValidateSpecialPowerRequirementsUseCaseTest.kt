package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateSpecialPowerRequirementsUseCaseTest {

    private val useCase = ValidateSpecialPowerRequirementsUseCase()

    @Test
    fun `retorna erro para superfeiticaria quando ocultismo menor que d10`() {
        val erro = useCase.execute(
            ValidateSpecialPowerRequirementsUseCase.Input(
                effectNameKey = "SUPERFEITICARIA",
                ocultismoRaw = 8,
                cienciaRaw = null
            )
        )

        assertEquals("Requer Ocultismo d10+ (atual: d8).", erro)
    }

    @Test
    fun `retorna erro para superciencia quando ciencia menor que d10`() {
        val erro = useCase.execute(
            ValidateSpecialPowerRequirementsUseCase.Input(
                effectNameKey = "SUPERCIENCIA",
                ocultismoRaw = null,
                cienciaRaw = 6
            )
        )

        assertEquals("Requer Ciência d10+ (atual: d6).", erro)
    }

    @Test
    fun `retorna null para superfeiticaria quando ocultismo atende requisito`() {
        val erro = useCase.execute(
            ValidateSpecialPowerRequirementsUseCase.Input(
                effectNameKey = "SUPERFEITICARIA",
                ocultismoRaw = 10,
                cienciaRaw = null
            )
        )

        assertNull(erro)
    }

    @Test
    fun `retorna null para efeito generico sem regra especial`() {
        val erro = useCase.execute(
            ValidateSpecialPowerRequirementsUseCase.Input(
                effectNameKey = "OUTRO_PODER",
                ocultismoRaw = 0,
                cienciaRaw = 0
            )
        )

        assertNull(erro)
    }
}
