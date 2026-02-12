package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.EspecializacoesDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureDefaultSpecializationsUseCaseTest {

    private val useCase = EnsureDefaultSpecializationsUseCase()

    @Test
    fun `nao altera quando especializacoes estao desabilitadas`() {
        val pericias = listOf(Pericia("ATLETISMO", "AGILIDADE", basica = true))
        val atual = mapOf("ATLETISMO" to EspecializacoesDto(principal = null, lista = emptyList()))

        val out = useCase.execute(
            usarEspecializacoesDePericia = false,
            pericias = pericias,
            rawTotalProvider = { 4 },
            atual = atual
        )

        assertEquals(atual, out)
    }

    @Test
    fun `adiciona especializacao padrao para pericia visivel`() {
        val pericias = listOf(Pericia("ATLETISMO", "AGILIDADE", basica = true))

        val out = useCase.execute(
            usarEspecializacoesDePericia = true,
            pericias = pericias,
            rawTotalProvider = { 0 },
            atual = emptyMap()
        )

        assertTrue(out.containsKey("ATLETISMO"))
        assertEquals("Especialização 1", out["ATLETISMO"]?.principal)
    }

    @Test
    fun `nao cria especializacao para pericia nao basica invisivel`() {
        val pericias = listOf(Pericia("ATIRAR", "AGILIDADE", basica = false))

        val out = useCase.execute(
            usarEspecializacoesDePericia = true,
            pericias = pericias,
            rawTotalProvider = { 0 },
            atual = emptyMap()
        )

        assertFalse(out.containsKey("ATIRAR"))
    }
}
