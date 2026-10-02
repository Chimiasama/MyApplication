package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.ids.PathfinderCurrencyIds
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PobrezaMoneyTest {

    private lateinit var state: CriadorState

    @Before
    fun setup() {
        state = CriadorState()
    }

    @Test
    fun `livro basico - sem pobreza inicia com 500 e com pobreza fica com 250`() {
        state.dinheiro = 500
        assertEquals(500, state.dinheiro)

        val pobreza = Complicacao(
            id = "pobreza",
            name = "POBREZA",
            severity = "menor",
            description = "Dizem que um tolo e seu dinheiro logo se separam...",
            origem = "BASICO"
        )

        state.adicionarComplicacao(pobreza, "Menor")
        assertEquals(250, state.dinheiro)

        state.removerComplicacao(pobreza)
        assertEquals(500, state.dinheiro)
    }

    @Test
    fun `compendio fantasia - sem pobreza inicia com 300 e com pobreza fica com 150`() {
        state.compendioFantasiaAtivo = true
        state.dinheiro = 300
        assertEquals(300, state.dinheiro)

        val pobreza = Complicacao(
            id = "pobreza",
            name = "POBREZA",
            severity = "menor",
            description = "Dizem que um tolo e seu dinheiro logo se separam...",
            origem = "FANTASIA"
        )

        state.adicionarComplicacao(pobreza, "Menor")
        assertEquals(150, state.dinheiro)

        state.removerComplicacao(pobreza)
        assertEquals(300, state.dinheiro)
    }

    @Test
    fun `compendio pathfinder - sem pobreza inicia com 30000 e com pobreza fica com 15000 e atualiza carteira`() {
        state.compendioPathfinderAtivo = true
        state.dinheiro = 30000
        state.carteiraPathfinder[PathfinderCurrencyIds.PO] = 300
        state.updateTotalPathfinderMoney()
        assertEquals(30000, state.dinheiro)

        val pobreza = Complicacao(
            id = "pobreza",
            name = "POBREZA",
            severity = "menor",
            description = "Dizem que um tolo e seu dinheiro logo se separam...",
            origem = "PATHFINDER"
        )

        state.adicionarComplicacao(pobreza, "Menor")
        assertEquals(15000, state.dinheiro)

        // Verify total money calculated from wallet matches 15000 CP
        state.updateTotalPathfinderMoney()
        assertEquals(15000, state.dinheiro)

        state.removerComplicacao(pobreza)
        assertEquals(30000, state.dinheiro)

        // Verify total money calculated from wallet matches 30000 CP
        state.updateTotalPathfinderMoney()
        assertEquals(30000, state.dinheiro)
    }
}
