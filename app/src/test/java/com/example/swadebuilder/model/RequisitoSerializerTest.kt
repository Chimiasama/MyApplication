package com.example.swadebuilder.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RequisitoSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes requisitos as object`() {
        val input = """
            {
              "estagio": "Novato",
              "pericias": { "Fé": 6 },
              "observacoes": "Carta Selvagem"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<Requisito>(input)

        assertEquals("Novato", decoded.estagio)
        assertEquals(6, decoded.periciaMin["Fé"] ?: 0)
        assertTrue(decoded.exigeCS)
    }

    @Test
    fun `decodes requisitos as legacy string preserving observacoes and stage`() {
        val input = "\"Novato, Demônio ou Meio-Demônio\""

        val decoded = json.decodeFromString<Requisito>(input)

        assertEquals("Novato", decoded.estagio)
        assertEquals("Novato, Demônio ou Meio-Demônio", decoded.observacoes)
        assertTrue(decoded.atributoMin.isEmpty())
        assertTrue(decoded.periciaMin.isEmpty())
    }
}
