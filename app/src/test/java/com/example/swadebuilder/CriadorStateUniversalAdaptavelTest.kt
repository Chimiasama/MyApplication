package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateUniversalAdaptavelTest {

    private fun vantagem(id: String, nome: String, categoria: Categoria, estagio: String = "Novato"): Vantagem = Vantagem(
        id = id,
        nome = nome,
        categoria = categoria,
        requisitos = Requisito(estagio = estagio)
    )

    private fun ancestryWithAbility(nome: String, origem: String): RacialModifier = RacialModifier(
        nome = nome,
        origem = origem,
        descricao = "",
        atributos = emptyMap(),
        pericias = emptyMap(),
        vantagensGratis = emptyList(),
        desvantagens = emptyList(),
        habilidades = listOf(
            RacialAbility(
                nome = "Adaptável",
                descricao = "...",
                id = "ADAPTAVEL",
                category = "racial_trait_positive"
            )
        )
    )

    private fun ancestryWithFreeEdge(nome: String, origem: String): RacialModifier = RacialModifier(
        nome = nome,
        origem = origem,
        descricao = "",
        atributos = emptyMap(),
        pericias = emptyMap(),
        vantagensGratis = listOf("ADAPTÁVEL"),
        desvantagens = emptyList(),
        habilidades = emptyList()
    )

    @Test
    fun `adaptable works for Basic Humans (Ability ID)`() {
        val state = CriadorState()
        state.compendioPathfinderAtivo = false
        state.listaAncestralidadesJson = listOf(ancestryWithAbility("HUMANOS", "BASICO"))
        state.ancestralidade = "HUMANOS"

        assertTrue(state.temAdaptavel())
        assertTrue(state.adaptavelSlotAvailable)

        val v = vantagem("atento", "Atento", Categoria.ANTECEDENTE)
        state.comprarVantagem(v)

        assertEquals("atento", state.vantagemAdaptavelSelecionadaId)
        assertFalse(state.adaptavelSlotAvailable)
        assertEquals(0, state.pontosVantagem)
    }

    @Test
    fun `adaptable works for Fantasy Humans (Ability ID)`() {
        val state = CriadorState()
        state.compendioFantasiaAtivo = true
        state.listaAncestralidadesJson = listOf(ancestryWithAbility("HUMANOS", "FANTASIA"))
        state.ancestralidade = "HUMANOS"

        assertTrue(state.temAdaptavel())
        assertTrue(state.adaptavelSlotAvailable)

        val v = vantagem("atento", "Atento", Categoria.ANTECEDENTE)
        state.comprarVantagem(v)

        assertEquals("atento", state.vantagemAdaptavelSelecionadaId)
    }

    @Test
    fun `adaptable works for Horror Humans (Free Edge String)`() {
        val state = CriadorState()
        state.compendioHorrorAtivo = true
        // Horror often uses the String list method in JSON
        state.listaAncestralidadesJson = listOf(ancestryWithFreeEdge("HUMANOS", "HORROR"))
        state.ancestralidade = "HUMANOS"

        assertTrue(state.temAdaptavel())
        assertTrue(state.adaptavelSlotAvailable)

        val v = vantagem("atento", "Atento", Categoria.ANTECEDENTE)
        state.comprarVantagem(v)

        assertEquals("atento", state.vantagemAdaptavelSelecionadaId)
    }

    @Test
    fun `adaptable works for SciFi Humans (Ability ID)`() {
        val state = CriadorState()
        state.compendioSciFiAtivo = true
        state.listaAncestralidadesJson = listOf(ancestryWithAbility("HUMANOS", "SCIFI"))
        state.ancestralidade = "HUMANOS"

        assertTrue(state.temAdaptavel())

        val v = vantagem("atento", "Atento", Categoria.ANTECEDENTE)
        state.comprarVantagem(v)

        assertEquals("atento", state.vantagemAdaptavelSelecionadaId)
    }

    @Test
    fun `pathfinder priority works correctly`() {
        val state = CriadorState()
        state.compendioPathfinderAtivo = true
        // Pathfinder Humans have both Adaptable (String) AND implicit Pathfinder Slot logic
        state.listaAncestralidadesJson = listOf(ancestryWithFreeEdge("Humano (Pathfinder)", "PATHFINDER"))
        state.ancestralidade = "Humano (Pathfinder)"

        val classEdge = vantagem("clerigo", "Clérigo", Categoria.CLASSE)
        val genericEdge = vantagem("atento", "Atento", Categoria.ANTECEDENTE)

        // 1. Buy Class Edge -> Should take Pathfinder Slot
        state.comprarVantagem(classEdge)

        assertNull("Adaptable slot should NOT be used for Class Edge", state.vantagemAdaptavelSelecionadaId)
        assertTrue("Adaptable slot should still be available", state.adaptavelSlotAvailable)

        // 2. Buy Generic Edge -> Should take Adaptable Slot
        state.comprarVantagem(genericEdge)

        assertEquals("Adaptable slot should be used for Generic Edge", "atento", state.vantagemAdaptavelSelecionadaId)
        assertFalse("Adaptable slot should be exhausted", state.adaptavelSlotAvailable)
    }
}
