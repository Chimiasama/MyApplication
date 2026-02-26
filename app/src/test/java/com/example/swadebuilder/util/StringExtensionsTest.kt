package com.example.swadebuilder.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {

    @Test
    fun semAcentos_removesAccentsCorrectly() {
        assertEquals("Ola", "Olá".semAcentos())
        assertEquals("Coracao", "Coração".semAcentos())
        assertEquals("Avo", "Avó".semAcentos())
        assertEquals("Avo", "Avô".semAcentos())
        assertEquals("U", "Ü".semAcentos())
        assertEquals("C", "Ç".semAcentos())
    }

    @Test
    fun semAcentos_handlesMixedCase() {
        assertEquals("Erico Verissimo", "Érico Veríssimo".semAcentos())
    }

    @Test
    fun semAcentos_handlesNoAccents() {
        assertEquals("Test", "Test".semAcentos())
    }

    @Test
    fun semAcentos_handlesEmptyString() {
        assertEquals("", "".semAcentos())
    }

    @Test
    fun keyify_worksCorrectly() {
        assertEquals("CORACAO", "Coração".keyify())
        assertEquals("TESTE", "  Teste  ".keyify())
        assertEquals("AVO", "Avô".keyify())
        assertEquals("", "".keyify())
    }

    @Test
    fun toFancyTitleCase_handlesConjunctions() {
        assertEquals("Mordida ou Garra", "MORDIDA OU GARRA".toFancyTitleCase())
        assertEquals("Mordida ou Garra", "mordida ou garra".toFancyTitleCase())
        assertEquals("Um ou Outro", "UM OU OUTRO".toFancyTitleCase())
    }
}
