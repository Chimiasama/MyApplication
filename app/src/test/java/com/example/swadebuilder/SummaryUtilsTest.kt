package com.example.swadebuilder

import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Pericia
import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryUtilsTest {

    @Test
    fun `buildSummaryLines deduplicates skills`() {
        val listaPericias = listOf(
            Pericia("Atletismo", "AGILIDADE", true),
            Pericia("Atletismo", "AGILIDADE", true), // Duplicate
            Pericia("Lutar", "AGILIDADE", true)
        )
        val listaAncestralidades = emptyList<com.example.swadebuilder.model.RacialModifier>()
        val listaMonstros = emptyList<com.example.swadebuilder.model.MonstroTemplate>()
        val listaComplicacoes = emptyList<com.example.swadebuilder.model.Complicacao>()
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        val mapaAtributosDisplay = listaAtributos.associateWith { it }
        val listaPoderes = emptyList<com.example.swadebuilder.model.Poder>()
        val arcanoInfo = emptyMap<String, Triple<Int, Int, String>>()

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

        val lines = buildSummaryLines(
            personagem = personagem,
            allAdvantages = emptyList(),
            listaAncestralidades = listaAncestralidades,
            listaMonstros = listaMonstros,
            listaComplicacoes = listaComplicacoes,
            listaAtributos = listaAtributos,
            mapaAtributosDisplay = mapaAtributosDisplay,
            listaPericias = listaPericias,
            listaPoderes = listaPoderes,
            arcanoInfo = arcanoInfo
        )

        // Find lines starting with "Atletismo:"
        val atletismoLines = lines.filter { it.startsWith("Atletismo:") }

        // Should be exactly 1
        assertEquals("Should have exactly one line for Atletismo", 1, atletismoLines.size)
        assertEquals("Atletismo: d4", atletismoLines.first())
    }
}
