package com.example.swadebuilder

import com.example.swadebuilder.ui.MainSection
import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveSectionResolverTest {
    @Test
    fun keepsRequestedSectionWhenAvailable() {
        val available = listOf(MainSection.RESUMO, MainSection.PERICIAS)

        val resolved = resolveActiveSection(MainSection.PERICIAS, available)

        assertEquals(MainSection.PERICIAS, resolved)
    }

    @Test
    fun fallsBackToResumoWhenRequestedMissing() {
        val available = listOf(MainSection.RESUMO, MainSection.VANTAGENS)

        val resolved = resolveActiveSection(MainSection.PERICIAS, available)

        assertEquals(MainSection.RESUMO, resolved)
    }

    @Test
    fun fallsBackToResumoWhenNoSectionsAvailable() {
        val resolved = resolveActiveSection(MainSection.VANTAGENS, emptyList())

        assertEquals(MainSection.RESUMO, resolved)
    }

    @Test
    fun fallsBackToFirstAvailableWhenResumoMissing() {
        val available = listOf(MainSection.PERICIAS, MainSection.VANTAGENS)

        val resolved = resolveActiveSection(MainSection.RESUMO, available)

        // When RESUMO is not in the available sections, fallback should be the first available entry.
        assertEquals(MainSection.PERICIAS, resolved)
    }
}
