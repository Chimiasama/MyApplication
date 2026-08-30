package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierEngineCidadeSolVaporTest {

    @Test
    fun `demonio de cidade do sol a vapor aplica fragil menos dois`() {
        val demonio = RacialModifier(
            id = "anc_demonio_csv",
            nome = "Demônio (Abismo)",
            origem = "CIDADE_SOL_VAPOR",
            atributos = emptyMap(),
            pericias = emptyMap(),
            vantagensGratis = emptyList(),
            desvantagens = listOf("FRÁGIL"),
            habilidades = listOf(
                RacialAbility(
                    nome = "Frágil",
                    descricao = "-2 na Resistência devido à degeneração física secular.",
                    id = "FRAGIL_MAIOR",
                    category = "racial_trait_negative"
                )
            )
        )

        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
            ancestralidade = "Demônio (Abismo)"
            updateGameData(
                GameDataSnapshot(
                    listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
                    listaAncestralidadesJson = listOf(demonio),
                    listaPericias = emptyList(),
                    listaVantagens = emptyList(),
                    listaComplicacoes = emptyList(),
                    listaTropos = emptyList(),
                    listaEquipamentos = emptyList(),
                    listaPoderes = emptyList(),
                    listaSuperPoderes = emptyList(),
                    listaMonstroTemplates = emptyList(),
                    listaCoracoesCrystal = emptyList(),
                    equipamentoCategorias = emptyList(),
                    superequipCategorias = emptyList(),
                    mapaAtributosDisplay = emptyMap(),
                    mapaPericias = emptyMap(),
                    racialAttrMinMap = emptyMap(),
                    racialSkillStartMap = emptyMap(),
                    arcanoInfo = emptyList(),
                    mapaAtributosDescricao = emptyMap()
                )
            )
        }

        val modifiers = ModifierEngine.collect(state)

        assertTrue(
            modifiers.any { it.id == "racial_trait_FRAGIL_MAIOR_res" && it.target == ModifierTarget.TOUGHNESS_FLAT && it.value == -2 }
        )
    }
}
