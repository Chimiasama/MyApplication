package com.example.swadebuilder

import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EquipamentoEquipadoTest {

    private fun emptySnapshot() = GameDataSnapshot(
        listaComplicacoes = emptyList(),
        listaCoracoesCrystal = emptyList(),
        listaAncestralidadesJson = emptyList(),
        listaMonstroTemplates = emptyList(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList(),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList(),
        listaPoderes = emptyList(),
        listaTropos = emptyList(),
        listaEquipamentos = emptyList(),
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList(),
        arcanoInfo = emptyList()
    )

    @Test
    fun `equipamento nao equipado nao conta no peso total`() {
        val state = CriadorState()
        state.updateGameData(emptySnapshot())

        val itemEquipado = EquipamentoItem(
            nome = "Espada",
            peso = JsonPrimitive(4),
            equipado = true
        )
        val itemMochila = EquipamentoItem(
            nome = "Escudo Murcho",
            peso = JsonPrimitive(6),
            equipado = false
        )

        state.equipamentosComprados.addAll(listOf(itemEquipado, itemMochila))

        assertEquals(4.0f, state.totalPesoEquipamentos(), 0.01f)
    }

    @Test
    fun `armadura nao equipada nao soma na resistencia do personagem`() {
        val state = CriadorState()
        state.updateGameData(emptySnapshot())

        val armaduraTronco = EquipamentoItem(
            nome = "Corselete de Couro",
            armadura = JsonPrimitive("+2"),
            local = listOf("TRONCO"),
            equipado = false
        )

        state.equipamentosComprados.add(armaduraTronco)

        assertEquals(0, state.armadura)
        assertTrue(state.armaduraPorLocal().isEmpty())

        state.toggleEquipado(armaduraTronco)

        assertEquals(2, state.armadura)
        assertEquals(2, state.armaduraPorLocal()["TRONCO"]?.valor)
    }

    @Test
    fun `toggleEquipado altera o estado de equipado com sucesso`() {
        val state = CriadorState()
        state.updateGameData(emptySnapshot())

        val item = EquipamentoItem(
            nome = "Jaqueta",
            armadura = JsonPrimitive("+1"),
            local = listOf("TRONCO", "BRACOS"),
            peso = JsonPrimitive(2.5),
            equipado = true
        )

        state.equipamentosComprados.add(item)
        assertTrue(state.equipamentosComprados.first().equipado)
        assertEquals(2.5f, state.totalPesoEquipamentos(), 0.01f)

        state.toggleEquipado(state.equipamentosComprados.first())

        assertFalse(state.equipamentosComprados.first().equipado)
        assertEquals(0f, state.totalPesoEquipamentos(), 0.01f)
        assertEquals(0, state.armadura)
    }
}
