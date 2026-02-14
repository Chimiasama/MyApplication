package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentVisibilityTest {

    @Test
    fun `cidade do sol a vapor mantém seletor generico de antecedente arcano visivel`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val genericArcane = Vantagem(
            id = "antecedente_arcano",
            nome = "ANTECEDENTE ARCANO",
            categoria = Categoria.ANTECEDENTE,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = genericArcane,
            multiplosAAHabilitados = false
        )

        assertTrue(visible)
    }

    @Test
    fun `cidade do sol a vapor mostra AA especifico quando multiplos antecedentes estão habilitados`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val specificArcane = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.ANTECEDENTE,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = specificArcane,
            multiplosAAHabilitados = true
        )

        assertTrue(visible)
    }

    @Test
    fun `cidade do sol a vapor esconde AA especifico quando multiplos antecedentes estão desabilitados`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val specificArcane = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.ANTECEDENTE,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = specificArcane,
            multiplosAAHabilitados = false
        )

        assertFalse(visible)
    }

    @Test
    fun `cidade do sol a vapor aceita alias de origem sol e vapor`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val steamAdvantage = Vantagem(
            id = "engenhoca_1",
            nome = "Engenhoca",
            categoria = Categoria.PROFISSIONAL,
            origem = "SOL E VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = steamAdvantage,
            multiplosAAHabilitados = true
        )

        assertTrue(visible)
    }

}
