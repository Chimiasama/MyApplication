package com.example.swadebuilder

import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PericiaRulesResilienceTest {

    @Test
    fun `rawTotal and pericia rules do not crash when pericia map entries are missing`() {
        val state = CriadorState()
        val idioma = state.periciasComIdiomas().first { it.nome.equals("Idiomas", ignoreCase = true) }

        state.baseIncsPorPericia.remove(idioma)

        val compIncsField = CriadorState::class.java.getDeclaredField("compIncsPorPericia").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val compIncs = compIncsField.get(state) as MutableMap<com.example.swadebuilder.model.Pericia, Int>
        compIncs.remove(idioma)

        val compCostField = CriadorState::class.java.getDeclaredField("compCostStackPorPericia").apply { isAccessible = true }
        @Suppress("UNCHECKED_CAST")
        val compCost = compCostField.get(state) as MutableMap<com.example.swadebuilder.model.Pericia, MutableList<Int>>
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
        val state = CriadorState()
        state.skillAdvancementInProgress = true

        val idiomaBase = state.periciasComIdiomas().first { it.nome.equals("Idiomas", ignoreCase = true) }
        state.increasePericiaFromAdvancement(idiomaBase, 1)

        val idioma2 = state.periciasComIdiomas().firstOrNull { it.nome.equals("Idiomas 2", ignoreCase = true) }
            ?: run {
                fail("Era esperado criar o slot 'Idiomas 2' após investir em Idiomas base")
                return
            }

        state.increasePericiaFromAdvancement(idioma2, 1)
        state.decreasePericia(idiomaBase)

        try {
            val remaining = state.periciasComIdiomas().filter { state.isIdiomaPericia(it) }
            remaining.forEach {
                state.calcularPericiaRules(it, idosoActive = false, locked = false)
            }
            assertTrue(remaining.isNotEmpty())
        } catch (t: Throwable) {
            fail("Fluxo de XP/Idiomas não deveria quebrar após remover o primeiro slot: ${t.message}")
        }
    }
}
