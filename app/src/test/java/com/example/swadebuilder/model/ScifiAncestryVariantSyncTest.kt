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
    fun `aquarianos semi aquaticos resolve selecao de variante sem fallback`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            scifiVariant = "Semi-aquáticos"
        }

        val varianteResolvida = state.resolveSciFiVariantSelectionFor(
            ancestryName = "AQUARIANOS",
            availableOptions = listOf("Básico", "Semi-aquáticos")
        )

        assertEquals("Semi-aquáticos", varianteResolvida)
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


    @Test
    fun `drakens aplicam lento e resistencia mais dois sem armadura racial`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "DRAKENS"
            naturalArmorFromRace = 0
        }

        val mods = ModifierEngine.collect(state)

        assertTrue(mods.any { it.id == "racial_pace_lento" && it.value == -1 })
        assertTrue(mods.any { it.id == "racial_res_generic" && it.value == 2 })
        assertFalse(mods.any { it.id == "racial_armor_generic" })
    }

    @Test
    fun `drakens expoem ataque natural cabeca dura`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "DRAKENS"
        }

        val armas = state.extrairArmasNaturais()

        assertTrue(armas.any { it.nome.equals("Cabeça Dura", ignoreCase = true) })
    }


    @Test
    fun `elementais scifi comecam com forca d8`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "ELEMENTAIS"
        }

        assertEquals(8, state.atributoMinRaw("FORCA"))
    }

    @Test
    fun `elementais scifi aplicam resistencia mais dois via traco racial`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "ELEMENTAIS"
            vantagensRaciais.clear()
            vantagensRaciais.add("RESISTÊNCIA +2")
        }

        val mods = ModifierEngine.collect(state)
        assertTrue(mods.any { it.id == "racial_res_generic" && it.value == 2 })
    }

    @Test
    fun `avianos ave de rapina nao aplica penalidade de fragil`() {
        val state = CriadorState().apply {
            compendioSciFiAtivo = true
            ancestralidade = "AVIANOS"
            desvantagensRaciais.clear()
            desvantagensRaciais.add("FORMA ALIENÍGENA")
            desvantagensRaciais.add("HABITANTE DE GRAVIDADE ZERO/BAIXA")
        }

        val mods = ModifierEngine.collect(state)
        assertFalse(mods.any { it.id == "racial_fragil" })
    }

}
