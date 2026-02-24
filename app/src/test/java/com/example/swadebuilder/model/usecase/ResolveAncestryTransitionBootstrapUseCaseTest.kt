package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryTransitionBootstrapUseCaseTest {

    private val useCase = ResolveAncestryTransitionBootstrapUseCase()

    @Test
    fun `does not increment vantagem points when transitioning into humanos (legacy logic disabled)`() {
        val result = useCase.execute(
            ResolveAncestryTransitionBootstrapUseCase.Params(
                previousAncestry = "ELFOS",
                targetAncestry = "HUMANOS",
                previousAncestryDef = null,
                targetAncestryDef = null,
                currentAutomaticAdvantages = emptyList(),
                pontosVantagemAtuais = 1,
                vantagensSelecionadas = emptyList(),
                meioElfoAgil = false
            )
        )

        assertFalse(result.ancestryTransitionContext.wasHumano)
        assertTrue(result.ancestryTransitionContext.willBeHumano)
        assertEquals(1, result.humanTransition.novosPontosVantagem)
    }

    @Test
    fun `uses transition context free keys but does not change points when leaving humano via adaptavel ancestry (legacy logic disabled)`() {
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
                vantagensSelecionadas = emptyList(),
                meioElfoAgil = false
            )
        )

        assertTrue(result.ancestryTransitionContext.wasHumano)
        assertFalse(result.ancestryTransitionContext.willBeHumano)
        assertTrue("sorte" in result.ancestryTransitionContext.previousFreeAdvantageKeys)
        assertEquals(2, result.humanTransition.novosPontosVantagem)
    }
}
