package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApplyHumanAncestryTransitionUseCaseTest {

    private val useCase = ApplyHumanAncestryTransitionUseCase()

    @Test
    fun `removes last eligible edge when leaving humano ancestry`() {
        val racialFree = vantagem(id = "racial", nome = "Adaptável")
        val keptByPrereq = vantagem(id = "base")
        val dependsOnBase = vantagem(id = "dep", requisitos = Requisito(vantagensPrevias = listOf("base")))
        val eligible = vantagem(id = "eligible", nome = "Lutador")

        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = true,
                vaiSerHumano = false,
                pontosVantagemAtuais = 3,
                vantagensSelecionadas = listOf(racialFree, keptByPrereq, dependsOnBase, eligible),
                prevFreeKeys = setOf("ADAPTAVEL")
            )
        )

        assertEquals(3, result.novosPontosVantagem)
        assertEquals("eligible", result.vantagemRemovida?.id)
    }

    @Test
    fun `decrements edge points when leaving humano ancestry and no eligible edge exists`() {
        val racialFree = vantagem(id = "racial", nome = "Adaptável")
        val powerEdge = vantagem(id = "power", categoria = Categoria.PODER)
        val scenarioEdge = vantagem(id = "superpoderes")

        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = true,
                vaiSerHumano = false,
                pontosVantagemAtuais = 1,
                vantagensSelecionadas = listOf(racialFree, powerEdge, scenarioEdge),
                prevFreeKeys = setOf("ADAPTAVEL")
            )
        )

        assertEquals(0, result.novosPontosVantagem)
        assertNull(result.vantagemRemovida)
    }

    @Test
    fun `increments edge points when entering humano ancestry`() {
        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = false,
                vaiSerHumano = true,
                pontosVantagemAtuais = 2,
                vantagensSelecionadas = emptyList(),
                prevFreeKeys = emptySet()
            )
        )

        assertEquals(3, result.novosPontosVantagem)
        assertNull(result.vantagemRemovida)
    }

    @Test
    fun `keeps state unchanged when ancestry transition is unrelated to humano bonus`() {
        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = false,
                vaiSerHumano = false,
                pontosVantagemAtuais = 5,
                vantagensSelecionadas = emptyList(),
                prevFreeKeys = emptySet()
            )
        )

        assertEquals(5, result.novosPontosVantagem)
        assertNull(result.vantagemRemovida)
    }

    private fun vantagem(
        id: String,
        nome: String = id,
        categoria: Categoria = Categoria.SOCIAIS,
        requisitos: Requisito = Requisito()
    ): Vantagem = Vantagem(
        id = id,
        nome = nome,
        categoria = categoria,
        requisitos = requisitos
    )
}
