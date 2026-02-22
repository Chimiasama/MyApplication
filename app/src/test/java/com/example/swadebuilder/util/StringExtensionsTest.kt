package com.example.swadebuilder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun toDisplayTitleCase_formatsConnectorsRomanNumeralsAndIds() {
        assertEquals("Amigo das Feras", "amigo_das_feras".toDisplayTitleCase())
        assertEquals("Visão no Escuro", "VISÃO NO ESCURO".toDisplayTitleCase())
        assertEquals("Nível III", "NÍVEL iii".toDisplayTitleCase())
        assertEquals("Ganha XP e PA", "ganha xp e pa".toDisplayTitleCase())
        assertEquals("Voto de Guerra nas Trevas no Abismo", "VOTO DE GUERRA NAS TREVAS NO ABISMO".toDisplayTitleCase())
        assertEquals("d'Arc", "d'ARC".toDisplayTitleCase())
    }

    @Test
    fun comparePtBrDisplay_keepsAccentedWordsNearby() {
        val ordered = listOf("Árvore", "Arbusto", "Abacate").sortedWith(::comparePtBrDisplay)
        assertEquals("Abacate", ordered.first())
        assertTrue(ordered.indexOf("Arbusto") < ordered.indexOf("Árvore"))
    }
}
