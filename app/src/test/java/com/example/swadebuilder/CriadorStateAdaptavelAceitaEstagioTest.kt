package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateAdaptavelAceitaEstagioTest {

    private fun vantagem(estagio: String): Vantagem = Vantagem(
        id = "v_$estagio",
        nome = "V $estagio",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito(estagio = estagio)
    )

    @Test
    fun `sem Nasce um Heroi, Adaptavel so aceita Vantagens de Novato`() {
        val state = CriadorState()

        assertTrue(state.adaptavelAceitaEstagio(vantagem("Novato")))
        assertFalse(state.adaptavelAceitaEstagio(vantagem("Experiente")))
        assertFalse(state.adaptavelAceitaEstagio(vantagem("Lendário")))
    }

    @Test
    fun `Nasce um Heroi libera Adaptavel pra Vantagens de Estagio acima, exceto Lendario`() {
        val state = CriadorState()
        state.nasceUmHeroi = true

        assertTrue(state.adaptavelAceitaEstagio(vantagem("Experiente")))
        assertTrue(state.adaptavelAceitaEstagio(vantagem("Veterano")))
        assertTrue(state.adaptavelAceitaEstagio(vantagem("Heroico")))
        assertFalse(state.adaptavelAceitaEstagio(vantagem("Lendário")))
    }

    @Test
    fun `Nasce um Heroi nao libera Adaptavel fora da criacao`() {
        val state = CriadorState()
        state.nasceUmHeroi = true

        state.advantageAdvancementInProgress = true
        state.updateEmProgressoFlag()
        assertTrue(state.emProgresso)

        assertFalse(state.adaptavelAceitaEstagio(vantagem("Experiente")))
    }
}
