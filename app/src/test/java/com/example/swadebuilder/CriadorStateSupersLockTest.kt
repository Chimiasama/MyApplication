package com.example.swadebuilder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateSupersLockTest {

    @Test
    fun `congela criacao basica durante fase de supers na criacao`() {
        val state = CriadorState().apply {
            modoProgressaoAtivo = false
            faseSupersAtiva = true
            superNivelCampanha = 2
            superPontosTotais = 30
            superPontosDisponiveis = 30
        }

        assertTrue(state.criacaoBasicaCongelada)
    }

    @Test
    fun `descongela criacao basica ao sair da fase de supers`() {
        val state = CriadorState().apply {
            modoProgressaoAtivo = false
            faseSupersAtiva = false
            superNivelCampanha = null
            superPontosTotais = 0
            superPontosDisponiveis = 0
        }

        assertFalse(state.criacaoBasicaCongelada)
    }
}
