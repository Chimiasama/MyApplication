package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatePerPowerLimitUseCaseTest {

    private val useCase = CalculatePerPowerLimitUseCase()

    @Test
    fun `retorna limite favorecido quando poder alvo eh o favorito`() {
        val out = useCase.execute(
            CalculatePerPowerLimitUseCase.Input(
                favoritePowerId = "sp_fire",
                targetPowerId = "sp_fire",
                favoriteLimit = 6,
                defaultLimit = 3
            )
        )

        assertEquals(6, out)
    }

    @Test
    fun `retorna limite padrao quando poder alvo nao eh favorito`() {
        val out = useCase.execute(
            CalculatePerPowerLimitUseCase.Input(
                favoritePowerId = "sp_fire",
                targetPowerId = "sp_ice",
                favoriteLimit = 6,
                defaultLimit = 3
            )
        )

        assertEquals(3, out)
    }

    @Test
    fun `retorna limite padrao quando nao ha favorito`() {
        val out = useCase.execute(
            CalculatePerPowerLimitUseCase.Input(
                favoritePowerId = null,
                targetPowerId = "sp_fire",
                favoriteLimit = 6,
                defaultLimit = 3
            )
        )

        assertEquals(3, out)
    }
}
