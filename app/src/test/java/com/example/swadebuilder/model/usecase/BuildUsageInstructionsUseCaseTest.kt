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
                compendioSciFiAtivo = true,
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
        assertTrue(text.contains("Compêndio Ficção Científica"))
        assertTrue(text.contains("Supers"))
        assertTrue(text.contains("Dica: para ativar/desativar compêndios"))

        assertTrue(text.contains("### Como usar o app"))
        assertTrue(text.contains("Navegue pelas abas"))
        assertTrue(text.contains("As Classes de Prestígio funcionam como Vantagens"))
        assertTrue(text.contains("A instalação de Corações e Cristais afeta"))
        assertTrue(text.contains("Ajuste o Nível de Campanha"))
        assertTrue(text.contains("A instalação de Implantes Cibernéticos é limitada"))
    }

    @Test
    fun `inclui livro de monstros quando modo monstro ativo`() {
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
                modoMonstroAtivo = true,
                pathfinderLabel = "Pathfinder",
                supersBookLabel = "Livro Supers",
                monsterBookLabel = "Livro de Monstros"
            )
        )

        assertTrue(text.contains("Livro de Monstros"))
        assertFalse(text.contains("Livro Supers"))
        assertTrue(text.contains("Os limites normais de pontos são ignorados"))
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
        assertFalse(text.contains("Monstros"))

        assertTrue(text.contains("### Como usar o app"))
        assertFalse(text.contains("### Instruções específicas dos compêndios ativos"))
    }
}
