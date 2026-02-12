package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildUsageInstructionsUseCaseTest {

    private val useCase = BuildUsageInstructionsUseCase()

    @Test
    fun `inclui livros ativos e dica final`() {
        val text = useCase.execute(
            BuildUsageInstructionsUseCase.Input(
                compendioFantasiaAtivo = true,
                compendioHorrorAtivo = false,
                compendioSciFiAtivo = false,
                compendioPathfinderAtivo = true,
                compendioDeadlandsAtivo = false,
                compendioCrystalHeartAtivo = true,
                compendioArteDaGuerraAtivo = false,
                compendioCidadeSolVaporAtivo = false,
                compendioWiseguysAtivo = false,
                modoSupers = true,
                modoMonstroAtivo = false,
                pathfinderLabel = "Pathfinder",
                supersBookLabel = "Supers",
                monsterBookLabel = "Monstros"
            )
        )

        assertTrue(text.contains("Básico (sempre ativo)"))
        assertTrue(text.contains("Compêndio Fantasia"))
        assertTrue(text.contains("Compêndio Pathfinder"))
        assertTrue(text.contains("Compêndio Crystal Heart"))
        assertTrue(text.contains("Supers"))
        assertTrue(text.contains("Dica: para ativar/desativar compêndios"))
    }

    @Test
    fun `nao inclui livros desativados`() {
        val text = useCase.execute(
            BuildUsageInstructionsUseCase.Input(
                compendioFantasiaAtivo = false,
                compendioHorrorAtivo = false,
                compendioSciFiAtivo = false,
                compendioPathfinderAtivo = false,
                compendioDeadlandsAtivo = false,
                compendioCrystalHeartAtivo = false,
                compendioArteDaGuerraAtivo = false,
                compendioCidadeSolVaporAtivo = false,
                compendioWiseguysAtivo = false,
                modoSupers = false,
                modoMonstroAtivo = false,
                pathfinderLabel = "Pathfinder",
                supersBookLabel = "Supers",
                monsterBookLabel = "Monstros"
            )
        )

        assertFalse(text.contains("Compêndio Fantasia"))
        assertFalse(text.contains("Compêndio Crystal Heart"))
        assertFalse(text.contains("Supers"))
    }
}
