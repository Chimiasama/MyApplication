package com.example.swadebuilder

import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Pericia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `buildSummaryLines uses avianos ave de rapina traits in racial characteristics`() {
        val listaPericias = listOf(Pericia("Perceber", "ASTUCIA", true))
        val listaAncestralidades = listOf(
            com.example.swadebuilder.model.RacialModifier(
                nome = "AVIANOS",
                atributos = emptyMap(),
                pericias = mapOf("Perceber" to 2),
                desvantagens = emptyList(),
                habilidades = listOf(
                    com.example.swadebuilder.model.RacialAbility("Frágil", ""),
                    com.example.swadebuilder.model.RacialAbility("Movimentação Reduzida", ""),
                    com.example.swadebuilder.model.RacialAbility("Não Sabe Nadar", ""),
                    com.example.swadebuilder.model.RacialAbility("Sentidos Aguçados", ""),
                    com.example.swadebuilder.model.RacialAbility("Voo", "")
                ),
                origem = "FC"
            )
        )
        val listaMonstros = emptyList<com.example.swadebuilder.model.MonstroTemplate>()
        val listaComplicacoes = listOf(
            com.example.swadebuilder.model.Complicacao(
                id = "habitante_de_gravidade_baixa",
                name = "HABITANTE DE GRAVIDADE ZERO/BAIXA",
                severity = "menor",
                description = "",
                origem = "SCIFI"
            )
        )
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        val mapaAtributosDisplay = listaAtributos.associateWith { it }
        val listaPoderes = emptyList<com.example.swadebuilder.model.Poder>()
        val arcanoInfo = emptyMap<String, Triple<Int, Int, String>>()

        val personagem = MeuPersonagem(
            nome = "Ave",
            atributos = emptyMap(),
            pericias = mapOf("Perceber" to 6),
            ancestralidade = "AVIANOS",
            celestialAAMilagresDesabilitado = false,
            vantagens = emptyList(),
            complicacoes = emptyList(),
            desvantagensRaciais = listOf(
                "HABITANTE DE GRAVIDADE ZERO/BAIXA",
                "FORMA ALIENÍGENA",
                "SENTIDOS AGUÇADOS (Olhos de Águia)"
            ),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 0,
            pontosRestantes = 0,
            compendioSciFiAtivo = true
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

        val racialLine = lines.firstOrNull { it.startsWith("Características Raciais:") }
        assertNotNull(racialLine)
        assertTrue(racialLine!!.contains("Habitante de Gravidade Zero/Baixa"))
        assertTrue(racialLine.contains("Forma Alienígena"))
        assertFalse(racialLine.contains("Frágil"))
        assertFalse(racialLine.contains("Não Sabe Nadar"))
        assertFalse(lines.any { it.startsWith("Anotações Raciais:") && it.contains("FORMA ALIEN") })
    }

    @Test
    fun `buildSummaryLines preserves raw casing for narrative racial notes`() {
        val lines = buildSummaryLines(
            personagem = MeuPersonagem(
                nome = "Dwarf",
                atributos = emptyMap(),
                pericias = emptyMap(),
                ancestralidade = "ANÕES",
                celestialAAMilagresDesabilitado = false,
                vantagens = emptyList(),
                complicacoes = emptyList(),
                desvantagensRaciais = listOf("Anões Cyber: Combinar com o Mestre 2 pontos em habilidades negativas apropriadas ao cenário."),
                equipamentos = emptyList(),
                poderes = emptyMap(),
                dinheiro = 0,
                pontosRestantes = 0,
                compendioSciFiAtivo = true
            ),
            allAdvantages = emptyList(),
            listaAncestralidades = emptyList(),
            listaMonstros = emptyList(),
            listaComplicacoes = emptyList(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = mapOf(
                "AGILIDADE" to "Agilidade",
                "ASTUCIA" to "Astúcia",
                "ESPIRITO" to "Espírito",
                "FORCA" to "Força",
                "VIGOR" to "Vigor"
            ),
            listaPericias = emptyList(),
            listaPoderes = emptyList(),
            arcanoInfo = emptyMap()
        )

        val annotations = lines.firstOrNull { it.startsWith("Anotações Raciais:") }
        assertEquals(
            "Anotações Raciais: Anões Cyber: Combinar com o Mestre 2 pontos em habilidades negativas apropriadas ao cenário.",
            annotations
        )
    }

}
