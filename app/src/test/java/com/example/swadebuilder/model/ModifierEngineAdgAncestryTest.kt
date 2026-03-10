package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierEngineAdgAncestryTest {

    private fun stateWithAncestry(ancestralidade: String, modifier: RacialModifier): CriadorState {
        return CriadorState().apply {
            this.ancestralidade = ancestralidade
            updateGameData(
                GameDataSnapshot(
                    listaComplicacoes = emptyList(),
                    listaCoracoesCrystal = emptyList(),
                    listaAncestralidadesJson = listOf(modifier),
                    listaMonstroTemplates = emptyList(),
                    racialAttrMinMap = emptyMap(),
                    racialSkillStartMap = emptyMap(),
                    listaAtributos = emptyList(),
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
            )
        }
    }

    @Test
    fun `generic aparar minus from racial disadvantage applies parry penalty`() {
        val state = stateWithAncestry(
            ancestralidade = "Tanukimimi (Tanuki)",
            modifier = RacialModifier(
                nome = "Tanukimimi (Tanuki)",
                origem = "ARTE_DA_GUERRA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = listOf("APARAR -1"),
                habilidades = emptyList()
            )
        )

        val modifiers = ModifierEngine.collect(state)

        assertTrue(modifiers.any { it.id == "racial_parry_generic" && it.value == -1 })
    }

    @Test
    fun `explicit racial movement bonus applies to pace`() {
        val state = stateWithAncestry(
            ancestralidade = "Usagimimi (Coelho)",
            modifier = RacialModifier(
                nome = "Usagimimi (Coelho)",
                origem = "ARTE_DA_GUERRA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = emptyList(),
                habilidades = emptyList(),
                movimentacao = 2
            )
        )

        val modifiers = ModifierEngine.collect(state)

        assertTrue(modifiers.any { it.id == "racial_pace_explicit" && it.value == 2 })
    }
}
