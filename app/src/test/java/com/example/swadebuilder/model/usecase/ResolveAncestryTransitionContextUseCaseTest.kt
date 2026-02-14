package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryTransitionContextUseCaseTest {

    private val useCase = ResolveAncestryTransitionContextUseCase()

    @Test
    fun `detects human transitions through adaptavel and computes previous free keys`() {
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
            ResolveAncestryTransitionContextUseCase.Params(
                previousAncestry = "PEQUENINOS",
                targetAncestry = "ELFOS",
                previousAncestryDef = previousDef,
                targetAncestryDef = null,
                currentAutomaticAdvantages = listOf("Sorte")
            )
        )

        assertTrue(result.wasHumano)
        assertFalse(result.willBeHumano)
        assertTrue("sorte" in result.previousFreeAdvantageKeys)
    }

    @Test
    fun `includes celestial and saurios legacy aliases in previous free keys`() {
        val celestial = useCase.execute(
            ResolveAncestryTransitionContextUseCase.Params(
                previousAncestry = "CELESTIAIS",
                targetAncestry = "HUMANOS",
                previousAncestryDef = null,
                targetAncestryDef = null,
                currentAutomaticAdvantages = emptyList()
            )
        )

        val saurios = useCase.execute(
            ResolveAncestryTransitionContextUseCase.Params(
                previousAncestry = "SAURIOS",
                targetAncestry = "HUMANOS",
                previousAncestryDef = null,
                targetAncestryDef = null,
                currentAutomaticAdvantages = emptyList()
            )
        )

        assertTrue("antecedente_arcano_milagres" in celestial.previousFreeAdvantageKeys)
        assertTrue("sentidos_agucados" in saurios.previousFreeAdvantageKeys)
        assertTrue("prontidao" in saurios.previousFreeAdvantageKeys)
    }
}
