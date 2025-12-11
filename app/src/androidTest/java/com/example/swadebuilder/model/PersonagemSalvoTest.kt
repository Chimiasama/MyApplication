package com.example.swadebuilder.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersonagemSalvoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testSerializationWithNewFields() {
        val savedAdvantage = SavedAdvantage(id = "test_adv", choice = "Test Choice", origem = "BASICO")
        val saved = PersonagemSalvo(
            nome = "Test Character",
            atributos = mapOf("VIGOR" to 6),
            pericias = mapOf("Lutar" to 8),
            ancestralidade = "HUMANOS",
            vantagens = listOf("test_adv"), // Legacy
            savedAdvantages = listOf(savedAdvantage),
            complicacoes = emptyList(),
            equipamentos = emptyList(),
            poderes = emptyMap(),
            dinheiro = 500,
            pontosRestantes = 0,
            maisPontosPericias = true,
            cartaSelvagem = true,
            nasceUmHeroi = true // New field
        )

        val jsonString = json.encodeToString(saved)

        // Verify JSON contains new fields
        assertTrue(jsonString.contains("savedAdvantages"))
        assertTrue(jsonString.contains("nasceUmHeroi"))
        assertTrue(jsonString.contains("true")) // for nasceUmHeroi

        val loaded = json.decodeFromString<PersonagemSalvo>(jsonString)

        assertEquals("Test Character", loaded.nome)
        assertTrue(loaded.nasceUmHeroi)
        assertEquals(1, loaded.savedAdvantages.size)
        assertEquals("test_adv", loaded.savedAdvantages[0].id)
        assertEquals("Test Choice", loaded.savedAdvantages[0].choice)
    }

    @Test
    fun testLegacyDeserialization() {
        // JSON representing an old save (no savedAdvantages, no nasceUmHeroi)
        val oldJson = """
            {
                "id": "old-id",
                "nome": "Legacy Char",
                "atributos": {},
                "pericias": {},
                "ancestralidade": "HUMANOS",
                "vantagens": ["legacy_adv"],
                "vantagemChoices": { "legacy_adv": ["Legacy Choice"] },
                "complicacoes": [],
                "equipamentos": [],
                "poderes": {},
                "dinheiro": 500,
                "pontosRestantes": 0,
                "maisPontosPericias": true,
                "cartaSelvagem": true
            }
        """.trimIndent()

        val loaded = json.decodeFromString<PersonagemSalvo>(oldJson)

        assertEquals("Legacy Char", loaded.nome)
        assertTrue(loaded.savedAdvantages.isEmpty()) // Should be empty
        assertEquals(1, loaded.vantagens.size)
        assertEquals("legacy_adv", loaded.vantagens[0])
        assertEquals("Legacy Choice", loaded.vantagemChoices["legacy_adv"]?.get(0))

        // nasceUmHeroi defaults to false
        assertEquals(false, loaded.nasceUmHeroi)
    }
}
