package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveAdvantageByIdUseCaseTest {

    private val useCase = ResolveAdvantageByIdUseCase()

    @Test
    fun `resolve vantagem por id ignorando case`() {
        val vantagens = listOf(
            Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE, requisitos = Requisito())
        )

        val out = useCase.execute(vantagens, "ALERTA")

        assertEquals("Alerta", out?.nome)
    }

    @Test
    fun `retorna nulo quando id nao existe`() {
        val vantagens = listOf(
            Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE, requisitos = Requisito())
        )

        val out = useCase.execute(vantagens, "nao_existe")

        assertNull(out)
    }
}
