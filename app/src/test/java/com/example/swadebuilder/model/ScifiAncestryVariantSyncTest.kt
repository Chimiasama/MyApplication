package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertEquals
import org.junit.Test

class ScifiAncestryVariantSyncTest {

    @Test
    fun `ao reaplicar anoes mantém variante legacy sincronizada no estado generico`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "ANÕES"
            anoesScifiSelecionado = "Cyber"
            scifiVariant = "Básico"
        }

        state.aplicarAncestralidade("ANÕES", mutableListOf())

        assertEquals("Cyber", state.anoesScifiSelecionado)
        assertEquals("Cyber", state.scifiVariant)
    }
}
