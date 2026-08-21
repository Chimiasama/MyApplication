package com.example.swadebuilder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchetypeTemplateManagerTest {

    private val manager = ArchetypeTemplateManager()

    @Test
    fun testDefaultArchetypesLoaded() {
        val archetypes = manager.defaultArchetypes
        assertTrue(archetypes.isNotEmpty())
        assertTrue(archetypes.any { it.id == "guerreiro_corpo_a_corpo" })
        assertTrue(archetypes.any { it.id == "atirador_pistoleiro" })
    }

    @Test
    fun testGetArchetypesForBasicSetting() {
        val basicArchetypes = manager.getArchetypesForSetting("BASICO")
        assertEquals(3, basicArchetypes.size)
        assertFalse(basicArchetypes.any { it.id == "conjurador_basico" })
    }

    @Test
    fun testGetArchetypesForFantasySetting() {
        val fantasyArchetypes = manager.getArchetypesForSetting("FANTASIA")
        assertEquals(4, fantasyArchetypes.size)
        assertTrue(fantasyArchetypes.any { it.id == "conjurador_basico" })
    }

    @Test
    fun testGenerateReportWithoutWarnings() {
        val guerreiro = manager.defaultArchetypes.first { it.id == "guerreiro_corpo_a_corpo" }
        val report = manager.generateReport(guerreiro)

        assertEquals("guerreiro_corpo_a_corpo", report.archetypeId)
        assertEquals(2, report.appliedAttributes.size)
        assertEquals(2, report.appliedSkills.size)
        assertEquals(1, report.appliedEdges.size)
        assertTrue(report.warnings.isEmpty())
    }

    @Test
    fun testGenerateReportWithPowerWarning() {
        val conjurador = manager.defaultArchetypes.first { it.id == "conjurador_basico" }
        val conjuradorSemArcano = conjurador.copy(edges = emptyList())
        val report = manager.generateReport(conjuradorSemArcano)

        assertTrue(report.warnings.isNotEmpty())
        assertTrue(report.warnings.first().contains("Antecedente Arcano"))
    }
}
