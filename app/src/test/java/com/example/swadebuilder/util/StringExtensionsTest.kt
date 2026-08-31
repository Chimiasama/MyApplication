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
        assertEquals("Junto ao Chão", "Junto Ao Chão".toFancyTitleCase())
        assertEquals("Dano aos Inimigos", "dano aos inimigos".toFancyTitleCase())
    }

    @Test
    fun toFancyTitleCase_handlesAcronymsRomanNumeralsAndSpecialPrefixes() {
        assertEquals("Cura XP", "cura xp".toFancyTitleCase())
        assertEquals("Capitulo IV", "capitulo iv".toFancyTitleCase())
        assertEquals("d'Arc", "d'arc".toFancyTitleCase())
        assertEquals("\"A Lenda de XP\"", "\"a lenda de xp\"".toFancyTitleCase())
    }

    @Test
    fun toIdSlug_worksCorrectly() {
        assertEquals("fogo_do_inferno", "Fogo do Inferno".toIdSlug())
        assertEquals("acao_adicional", "Ação Adicional".toIdSlug())
        assertEquals("", "".toIdSlug())
        assertEquals("", "   ".toIdSlug())
    }

    @Test
    fun toIdSlug_treatsAccentAndPunctuationVariantsAsTheSameId() {
        // Duas grafias diferentes do "mesmo" nome (acento, espaçamento duplo, pontuação)
        // precisam colapsar pro mesmo slug — é essa propriedade que a checagem de colisão
        // de conteúdo customizado em SettingsDialog.kt depende pra nunca deixar dois itens
        // com nomes "iguais na prática" coexistirem sob ids diferentes.
        val a = "Fogo do Inferno".toIdSlug()
        val b = "Fôgo do Inferno".toIdSlug()
        val c = "  Fogo   do Inferno!!  ".toIdSlug()
        assertEquals(a, b)
        assertEquals(a, c)
    }
}
