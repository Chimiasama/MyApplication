package com.example.swadebuilder

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Pericia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IdosoSystemMessageTest {

    @Test
    fun testIdosoRestrictionTriggersSystemFeedbackMessage() {
        val state = CriadorState()
        state.listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        state.ensureAllAtributosRegistered()

        val atletismo = Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = false, id = "atletismo")
        val conhecimentoGeral = Pericia(nome = "Conhecimento Geral", atributo = "ASTUCIA", basica = false, id = "conhecimento_geral")
        state.updateGameData(
            com.example.swadebuilder.model.GameDataSnapshot(
                listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
                listaPericias = listOf(atletismo, conhecimentoGeral),
                listaVantagens = emptyList(),
                listaComplicacoes = emptyList(),
                listaTropos = emptyList(),
                listaEquipamentos = emptyList(),
                listaPoderes = emptyList(),
                listaSuperPoderes = emptyList(),
                listaAncestralidadesJson = emptyList(),
                listaVariantesRaciaisCustom = emptyList(),
                listaCategoriasCustomizadas = emptyList(),
                listaMonstroTemplates = emptyList(),
                listaCoracoesCrystal = emptyList(),
                equipamentoCategorias = emptyList(),
                superequipCategorias = emptyList(),
                mapaAtributosDisplay = mapOf("AGILIDADE" to "Agilidade", "ASTUCIA" to "Astúcia"),
                mapaAtributosDescricao = emptyMap(),
                mapaPericias = mapOf("atletismo" to atletismo, "conhecimento_geral" to conhecimentoGeral),
                arcanoInfo = emptyList()
            )
        )

        // Enable Idoso hindrance
        val idosoComp = Complicacao(id = "idoso", name = "Idoso", severity = "Maior", description = "", origem = "BASICO")
        state.complicacoesSelecionadas[idosoComp] = "Maior"
        state.idosoBonusSp = 5

        val feedback = mutableListOf<String>()

        // Attempt to increase non-Astúcia skill (Atletismo) before spending 5 points in Astúcia skills
        state.increasePericiaFromAdvancement(atletismo, cost = 1, feedbackMessages = feedback)

        // Assert system message generated and skill NOT increased
        assertTrue("Expected Idoso system message in feedback", feedback.contains("Gaste ao menos 5 pontos em perícias baseadas em Astúcia"))
        assertEquals(0, state.rawTotal(atletismo))

        // Now spend 5 points in Astúcia skill (Conhecimento Geral)
        repeat(5) {
            state.increasePericiaFromAdvancement(conhecimentoGeral, cost = 1)
        }

        feedback.clear()
        // Attempt to increase Atletismo again
        state.increasePericiaFromAdvancement(atletismo, cost = 1, feedbackMessages = feedback)

        // Assert skill IS increased and no Idoso restriction message is produced
        assertFalse("Expected no Idoso warning after spending 5 pts in Astucia", feedback.contains("Gaste ao menos 5 pontos em perícias baseadas em Astúcia"))
        assertEquals(4, state.rawTotal(atletismo))
    }
}
