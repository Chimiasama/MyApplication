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

class CriadorStateHumanoBasicoTest {

    private fun vantagem(id: String, nome: String, categoria: Categoria, estagio: String = "Novato"): Vantagem = Vantagem(
        id = id,
        nome = nome,
        categoria = categoria,
        requisitos = Requisito(estagio = estagio)
    )

    private fun humanoBasico(): RacialModifier = RacialModifier(
        nome = "HUMANOS",
        origem = "BASICO",
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

    @Test
    fun `adaptable trait allows buying novice edge without points`() {
        val state = CriadorState()
        // Ensure no other module is active
        state.compendioPathfinderAtivo = false
        state.compendioArteDaGuerraAtivo = false

        // Mock Ancestry
        state.listaAncestralidadesJson = listOf(humanoBasico())
        state.ancestralidade = "HUMANOS"

        // Mock Advantages
        val atraente = vantagem("atraente", "Atraente", Categoria.SOCIAIS, "Novato")
        val corajoso = vantagem("corajoso", "Corajoso", Categoria.SOCIAIS, "Novato")
        val experiente = vantagem("experiente", "Experiente", Categoria.SOCIAIS, "Experiente")
        state.listaVantagens = listOf(atraente, corajoso, experiente)

        // Verify setup
        assertTrue("Should have Adaptable trait", state.temAdaptavel())
        assertTrue("Adaptable slot should be available", state.adaptavelSlotAvailable)
        assertEquals("Should have 0 points initially", 0, state.pontosVantagem)

        // 1. Buy Novice Edge (Atraente) - Should use Adaptable Slot
        val boughtAtraente = state.comprarVantagem(atraente)
        assertTrue("Should successfully buy Atraente", boughtAtraente)
        assertEquals("Adaptable slot should be used", "atraente", state.vantagemAdaptavelSelecionadaId)
        assertFalse("Adaptable slot should no longer be available", state.adaptavelSlotAvailable)
        assertEquals("Points should remain 0", 0, state.pontosVantagem)

        // 2. Buy another Novice Edge (Corajoso) - Should Fail (No points, slot used)
        val boughtCorajoso = state.comprarVantagem(corajoso)
        assertFalse("Should fail to buy Corajoso (no points)", boughtCorajoso)

        // 3. Sell Atraente
        state.venderVantagem(atraente)
        assertNull("Adaptable slot should be free", state.vantagemAdaptavelSelecionadaId)
        assertTrue("Adaptable slot should be available", state.adaptavelSlotAvailable)

        // 4. Try to buy Seasoned Edge (Experiente) - Should Fail (Rank check)
        val boughtExperiente = state.comprarVantagem(experiente)
        assertFalse("Should fail to buy Experiente with Adaptable slot (Wrong Rank)", boughtExperiente)
    }
}
