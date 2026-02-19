package com.example.swadebuilder

import com.example.swadebuilder.model.Pericia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PericiaRulesResilienceTest {

    private fun createStateWithIdiomas(): CriadorState {
        val state = CriadorState()
        state.listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        state.listaPericias = listOf(
            Pericia(nome = "Idiomas", atributo = "ASTUCIA", basica = false, origem = "BASICO"),
            Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true, origem = "BASICO")
        )
        state.ensureAllAtributosRegistered()
        state.ensureAllPericiasRegistered()
        return state
    }

    @Test
    fun `rawTotal and pericia rules do not crash when pericia map entries are missing`() {
        val state = createStateWithIdiomas()
        val idioma = state.periciasComIdiomas().firstOrNull { it.nome.equals("Idiomas", ignoreCase = true) }
            ?: run {
                fail("Perícia 'Idiomas' deveria estar disponível no cenário básico de teste")
                return
            }

        state.baseIncsPorPericia.remove(idioma)

        val compIncsField = CriadorState::class.java.getDeclaredField("compIncsPorPericia").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val compIncs = compIncsField.get(state) as MutableMap<Pericia, Int>
        compIncs.remove(idioma)

        val compCostField = CriadorState::class.java.getDeclaredField("compCostStackPorPericia").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val compCost = compCostField.get(state) as MutableMap<Pericia, MutableList<Int>>
        compCost.remove(idioma)

        state.spCostStackPorPericia.remove(idioma)

        try {
            val raw = state.rawTotal(idioma)
            val rules = state.calcularPericiaRules(
                pericia = idioma,
                idosoActive = false,
                locked = false
            )
            assertTrue(raw >= 0)
            assertTrue(rules.currentRaw >= 0)
        } catch (t: Throwable) {
            fail("Não deveria lançar exceção ao calcular perícia sem entradas nos mapas: ${t.message}")
        }
    }

    @Test
    fun `xp flow with idiomas extras remains stable after removing first slot`() {
        val state = createStateWithIdiomas()
        state.skillAdvancementInProgress = true

        val idiomaBase = state.periciasComIdiomas().firstOrNull { it.nome.equals("Idiomas", ignoreCase = true) }
            ?: run {
                fail("Perícia 'Idiomas' deveria existir para o fluxo de XP")
                return
            }

        state.increasePericiaFromAdvancement(idiomaBase, 1)

        val idioma2 = state.periciasComIdiomas().firstOrNull { it.nome.equals("Idiomas 2", ignoreCase = true) }
        assertNotNull("Era esperado criar o slot 'Idiomas 2' após investir em Idiomas base", idioma2)

        state.increasePericiaFromAdvancement(idioma2!!, 1)
        state.decreasePericia(idiomaBase)

        try {
            val remaining = state.periciasComIdiomas().filter { state.isIdiomaPericia(it) }
            remaining.forEach {
                state.calcularPericiaRules(it, idosoActive = false, locked = false)
            }

            val emptySlots = remaining.count { state.rawTotal(it) == 0 }
            assertEquals("Deve haver apenas um slot de idiomas vazio no final", 1, emptySlots)
            assertTrue(remaining.isNotEmpty())
        } catch (t: Throwable) {
            fail("Fluxo de XP/Idiomas não deveria quebrar após remover o primeiro slot: ${t.message}")
        }
    }
}
