package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateDemonArmorTest {

    @Test
    fun `couro blindado adiciona mais quatro de armadura efetiva`() {
        val state = CriadorState()

        state.adicionarVantagem(
            Vantagem(
                id = "couro_blindado",
                nome = "COURO BLINDADO",
                categoria = Categoria.MONSTRUOSAS,
                requisitos = Requisito(estagio = "Experiente")
            )
        )

        assertEquals(4, state.valorArmaduraEfetiva())
    }

    @Test
    fun `couro blindado acumula com armadura de poder e natural`() {
        val state = CriadorState()
        state.armorFromPower = 2
        state.naturalArmorFromRace = 1

        state.adicionarVantagem(
            Vantagem(
                id = "couro_blindado",
                nome = "COURO BLINDADO",
                categoria = Categoria.MONSTRUOSAS,
                requisitos = Requisito(estagio = "Experiente")
            )
        )

        assertEquals(7, state.valorArmaduraEfetiva())
    }
}
