package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScifiAncestryVariantSyncTest {

    @Test
    fun `trocar de ancestralidade limpa variante e voltar usa basico`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "ANÕES"
        }

        state.selecionarScifiVariant("Cyber")
        assertEquals("Cyber", state.scifiVariant)

        state.aplicarAncestralidade("HUMANOS", mutableListOf())
        assertNull(state.scifiVariant)

        state.aplicarAncestralidade("ANÕES", mutableListOf())
        assertEquals("Básico", state.scifiVariant)
    }

    @Test
    fun `resolve variante usa basico ou padrao como fallback preferencial`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            scifiVariant = null
        }

        val selecionada = state.resolveSciFiVariantSelectionFor(
            ancestryName = "ANÕES",
            availableOptions = listOf("Cyber", "Básico")
        )

        assertEquals("Básico", selecionada)
    }
}
