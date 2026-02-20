package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateMonsterTemplateTransitionTest {

    @Test
    fun `remove vantagem de template antigo e devolve ponto ao trocar tipo de monstro`() {
        val state = CriadorState().apply {
            modoMonstroAtivo = true
            pontosVantagem = 1
            aplicarTipoMonstro("demonio")
        }

        val garrasDemonio = Vantagem(
            id = "garras_demonio",
            nome = "GARRAS (Demônio)",
            categoria = Categoria.MONSTRUOSAS,
            requisitos = Requisito(estagio = "Novato", template = JsonPrimitive("demonio"))
        )

        val comprou = state.comprarVantagem(garrasDemonio)
        assertTrue(comprou)
        assertEquals(0, state.pontosVantagem)
        assertTrue(state.vantagensSelecionadas.any { it.id == "garras_demonio" })

        state.aplicarTipoMonstro("fantasma")

        assertFalse(state.vantagensSelecionadas.any { it.id == "garras_demonio" })
        assertEquals(1, state.pontosVantagem)
    }
}
