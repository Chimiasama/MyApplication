package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import com.example.swadebuilder.util.keyify

class ScifiAncestryVariantSyncTest {

    @Test
    fun `trocar de ancestralidade limpa variante e voltar usa basico`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "ANÕES"
            scifiVariant = "Cyber"
        }
        assertEquals("Cyber", state.scifiVariant)

        state.aplicarAncestralidade("HUMANOS", mutableListOf())
        assertNull(state.scifiVariant)

        state.aplicarAncestralidade("ANÕES", mutableListOf())
        val anoesOptions = state.getAncestralidadeDef("ANÕES")?.opcoes.orEmpty()
        if (anoesOptions.isNotEmpty()) {
            val fallbackKey = state.scifiVariant?.keyify()
            assertTrue(fallbackKey == "BASICO" || fallbackKey == "PADRAO")
        } else {
            assertNull(state.scifiVariant)
        }
    }

    @Test
    fun `resolve variante usa basico ou padrao como fallback preferencial`() {
        val state = CriadorState().apply {
            scifiVariant = null
        }

        val selecionada = state.resolveSciFiVariantSelectionFor(
            ancestryName = "Qualquer",
            availableOptions = listOf("Cyber", "Básico")
        )

        assertEquals("Básico", selecionada)
    }

    @Test
    fun `resolve variante usa estado atual para qualquer ancestralidade com opcoes`() {
        val state = CriadorState().apply {
            scifiVariant = "Gazela"
            anoesScifiSelecionado = "Cyber"
        }

        val selecionada = state.resolveSciFiVariantSelectionFor(
            ancestryName = "Centaux",
            availableOptions = listOf("Básico", "Gazela")
        )

        assertEquals("Gazela", selecionada)
    }


    @Test
    fun `aquarianos semi aquaticos substituem aquatico e resistencia`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "AQUARIANOS"
            scifiVariant = "Semi-aquáticos"
        }

        val habilidades = state.getAncestralidadeDef("AQUARIANOS")?.habilidades?.map { it.nome.keyify() }.orEmpty()

        assertFalse(habilidades.contains("AQUATICO"))
        assertFalse(habilidades.contains("RESISTENCIA"))
        assertTrue(habilidades.any { it.contains("SEMI") && it.contains("AQUATIC") })
        assertTrue(habilidades.contains("TOQUE VENENOSO"))
    }

    @Test
    fun `aquarianos semi aquaticos nao aplicam bonus de resistencia por traco removido`() {
        val variante = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "AQUARIANOS"
            scifiVariant = "Semi-aquáticos"
        }

        val modsVariante = ModifierEngine.collect(variante)

        assertFalse(modsVariante.any { it.id == "racial_resistencia" })
    }

    @Test
    fun `aquarianos semiaquatico nao recebe bonus mesmo com ancestralidade base`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = false
            ancestralidade = "AQUARIANOS"
            vantagensRaciais.clear()
            vantagensRaciais.add("SEMIAQUÁTICO")
            vantagensRaciais.add("TOQUE VENENOSO")
        }

        val mods = ModifierEngine.collect(state)
        assertFalse(mods.any { it.id == "racial_resistencia" })
    }

}
