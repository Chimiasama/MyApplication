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
    fun `does not remove eligible edge when leaving humano ancestry (legacy logic disabled)`() {
        val racialFree = vantagem(id = "racial", nome = "Adaptável")
        val eligible = vantagem(id = "eligible", nome = "Lutador")

        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = true,
                vaiSerHumano = false,
                pontosVantagemAtuais = 3,
                vantagensSelecionadas = listOf(racialFree, eligible),
                prevFreeKeys = setOf("ADAPTAVEL")
            )
        )

        assertEquals(3, result.novosPontosVantagem)
        assertNull(result.vantagemRemovida)
    }

    @Test
    fun `does not decrement edge points when leaving humano ancestry (legacy logic disabled)`() {
        val racialFree = vantagem(id = "racial", nome = "Adaptável")

        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = true,
                vaiSerHumano = false,
                pontosVantagemAtuais = 1,
                vantagensSelecionadas = listOf(racialFree),
                prevFreeKeys = setOf("ADAPTAVEL")
            )
        )

        assertEquals(1, result.novosPontosVantagem)
        assertNull(result.vantagemRemovida)
    }

    @Test
    fun `does not increment edge points when entering humano ancestry (legacy logic disabled)`() {
        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = false,
                vaiSerHumano = true,
                pontosVantagemAtuais = 2,
                vantagensSelecionadas = emptyList(),
                prevFreeKeys = emptySet()
            )
        )

        assertEquals(2, result.novosPontosVantagem)
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

    @Test
    fun `does not force humano bonus loss for variant (legacy logic disabled)`() {
        val eligible = vantagem(id = "eligible", nome = "Lutador")

        val result = useCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = true,
                vaiSerHumano = true,
                forceLoseHumanBonus = true,
                pontosVantagemAtuais = 2,
                vantagensSelecionadas = listOf(eligible),
                prevFreeKeys = setOf("ADAPTAVEL")
            )
        )

        assertEquals(2, result.novosPontosVantagem)
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
