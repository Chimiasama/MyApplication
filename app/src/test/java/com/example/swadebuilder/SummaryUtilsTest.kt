package com.example.swadebuilder

import com.example.swadebuilder.model.MeuPersonagem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SummaryUtilsTest {

    @Before
    fun setup() {
        // Initialize globals used by SummaryUtils
        listaPericias = listOf(
            Pericia("Atletismo", "AGILIDADE", true),
            Pericia("Atletismo", "AGILIDADE", true), // Duplicate
            Pericia("Lutar", "AGILIDADE", true)
        )
        listaAncestralidadesJson = emptyList()
        listaVantagens = emptyList()
        listaComplicacoes = emptyList()
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        mapaAtributosDisplay = listaAtributos.associateWith { it }
        listaMonstroTemplates = emptyList()
    }

    @Test
    fun `buildSummaryLines deduplicates skills`() {
        val personagem = MeuPersonagem(
            nome = "Test Char",
            atributos = emptyMap(),
            pericias = mapOf("Atletismo" to 4),
            ancestralidade = "Humano",
            celestialAAMilagresDesabilitado = false,
            vantagens = emptyList(),
            complicacoes = emptyList(),
            desvantagensRaciais = emptyList(),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 500,
            pontosRestantes = 0
        )

        val lines = buildSummaryLines(personagem)

        // Find lines starting with "Atletismo:"
        val atletismoLines = lines.filter { it.startsWith("Atletismo:") }

        // Should be exactly 1
        assertEquals("Should have exactly one line for Atletismo", 1, atletismoLines.size)
        assertEquals("Atletismo: d4", atletismoLines.first())
    }
}
