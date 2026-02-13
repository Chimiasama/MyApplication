package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveActiveAncestryCandidatesUseCaseTest {

    private val useCase = ResolveActiveAncestryCandidatesUseCase()

    private val flags = ResolveActiveAncestryCandidatesUseCase.Flags(
        compendioFantasiaAtivo = true,
        compendioHorrorAtivo = false,
        compendioArteDaGuerraAtivo = false,
        compendioDeadlandsAtivo = false,
        compendioWiseguysAtivo = false,
        compendioCidadeSolVaporAtivo = false,
        compendioCrystalHeartAtivo = false,
        compendioSciFiAtivo = true,
        compendioPathfinderAtivo = true
    )

    @Test
    fun `ativa origem de modulo habilitado`() {
        assertTrue(useCase.isOriginActive("FANTASIA", flags))
        assertFalse(useCase.isOriginActive("HORROR", flags))
    }

    @Test
    fun `suporta aliases e pathfinder por origem textual`() {
        assertTrue(useCase.isOriginActive("FC", flags))
        assertTrue(useCase.isOriginActive("trilhador", flags))
        assertTrue(useCase.isOriginActive("PATHFINDER EXPANSAO", flags))
    }
}
