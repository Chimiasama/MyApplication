package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryTransitionBootstrapUseCaseTest {

    private val useCase = ResolveAncestryTransitionBootstrapUseCase()

    @Test
    fun `increments vantagem points when transitioning into humanos`() {
        val result = useCase.execute(
            ResolveAncestryTransitionBootstrapUseCase.Params(
                previousAncestry = "ELFOS",
                targetAncestry = "HUMANOS",
                previousAncestryDef = null,
                targetAncestryDef = null,
                currentAutomaticAdvantages = emptyList(),
                pontosVantagemAtuais = 1,
                vantagensSelecionadas = emptyList()
            )
        )

        assertFalse(result.ancestryTransitionContext.wasHumano)
        assertTrue(result.ancestryTransitionContext.willBeHumano)
        assertEquals(2, result.humanTransition.novosPontosVantagem)
    }

    @Test
    fun `uses transition context free keys when leaving humano via adaptavel ancestry`() {
        val previousDef = RacialModifier(
            nome = "Povo Adaptado",
            vantagensGratis = listOf("Adaptável"),
            desvantagens = emptyList(),
            atributos = emptyMap(),
            pericias = emptyMap(),
            habilidades = emptyList(),
            origem = "BASICO"
        )

        val result = useCase.execute(
            ResolveAncestryTransitionBootstrapUseCase.Params(
                previousAncestry = "PEQUENINOS",
                targetAncestry = "ELFOS",
                previousAncestryDef = previousDef,
                targetAncestryDef = null,
                currentAutomaticAdvantages = listOf("Sorte"),
                pontosVantagemAtuais = 2,
                vantagensSelecionadas = emptyList()
            )
        )

        assertTrue(result.ancestryTransitionContext.wasHumano)
        assertFalse(result.ancestryTransitionContext.willBeHumano)
        assertTrue("sorte" in result.ancestryTransitionContext.previousFreeAdvantageKeys)
        assertEquals(1, result.humanTransition.novosPontosVantagem)
    }
}
