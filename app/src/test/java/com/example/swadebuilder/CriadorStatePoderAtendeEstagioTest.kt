package com.example.swadebuilder

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStatePoderAtendeEstagioTest {

    @Test
    fun `sem Nasce um Heroi, personagem Novato so ve poderes de Novato`() {
        val state = CriadorState()

        assertTrue(state.poderAtendeEstagio("Novato"))
        assertFalse(state.poderAtendeEstagio("Experiente"))
        assertFalse(state.poderAtendeEstagio("Lendário"))
    }

    @Test
    fun `Nasce um Heroi libera poderes de Estagio acima na criacao, exceto Lendario`() {
        val state = CriadorState()
        state.nasceUmHeroi = true

        assertTrue(state.poderAtendeEstagio("Experiente"))
        assertTrue(state.poderAtendeEstagio("Veterano"))
        assertTrue(state.poderAtendeEstagio("Heroico"))
        assertFalse(state.poderAtendeEstagio("Lendário"))
    }

    @Test
    fun `Nasce um Heroi nao libera Estagio fora da criacao`() {
        val state = CriadorState()
        state.nasceUmHeroi = true

        state.advantageAdvancementInProgress = true
        state.updateEmProgressoFlag()
        assertTrue(state.emProgresso)

        assertFalse(state.poderAtendeEstagio("Experiente"))
    }

    @Test
    fun `Nasce um Heroi nao libera Estagio com PV de XP pendente`() {
        val state = CriadorState()
        state.nasceUmHeroi = true
        state.pvFromXpOutstanding = 1

        assertFalse(state.poderAtendeEstagio("Experiente"))
    }
}
