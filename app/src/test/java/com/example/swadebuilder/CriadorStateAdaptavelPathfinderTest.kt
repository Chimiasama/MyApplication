package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateAdaptavelPathfinderTest {

    private fun vantagem(id: String, nome: String, categoria: Categoria, estagio: String = "Novato"): Vantagem = Vantagem(
        id = id,
        nome = nome,
        categoria = categoria,
        requisitos = Requisito(estagio = estagio)
    )

    private fun racialModifier(nome: String, gratis: List<String>): RacialModifier = RacialModifier(
        nome = nome,
        origem = "PATHFINDER",
        descricao = "",
        atributos = emptyMap(),
        pericias = emptyMap(),
        vantagensGratis = gratis,
        desvantagens = emptyList(),
        habilidades = emptyList()
    )

    @Test
    fun `prioritizes pathfinder slot over adaptable slot`() {
        val state = CriadorState()
        state.compendioPathfinderAtivo = true

        // Mock Ancestry
        val ancestryName = "Humano (Pathfinder)"
        val humanPathfinder = racialModifier(ancestryName, listOf("ADAPTÁVEL"))
        state.listaAncestralidadesJson = listOf(humanPathfinder)
        state.ancestralidade = ancestryName

        // Mock Advantages
        val clerigo = vantagem("clerigo", "Clérigo", Categoria.CLASSE)
        val atraente = vantagem("atraente", "Atraente", Categoria.SOCIAIS)
        state.listaVantagens = listOf(clerigo, atraente)

        // Verify setup
        assertTrue("Should have Adaptable trait", state.temAdaptavel())
        assertTrue("Adaptable slot should be available initially", state.adaptavelSlotAvailable)
        assertTrue("Pathfinder slot should be available initially", state.pathfinderSlotAvailable)

        // 1. Buy Cleric (Class Edge) - Should use Pathfinder Slot
        val boughtCleric = state.comprarVantagem(clerigo)
        assertTrue("Should successfully buy Cleric", boughtCleric)

        // Assertions for CORRECT logic (prioritizing Pathfinder slot)
        // With current buggy logic, these assertions might fail if run against existing code.
        // We write the test for the DESIRED behavior.

        // If Pathfinder slot is prioritized:
        // adaptavelSlotAvailable should still be true (Adaptable slot UNUSED)
        // pathfinderSlotAvailable should be false (Pathfinder slot USED)
        // vantagemAdaptavelSelecionadaId should be null

        assertNull("Adaptable slot should NOT be used for Class Edge", state.vantagemAdaptavelSelecionadaId)
        assertTrue("Adaptable slot should remain available", state.adaptavelSlotAvailable)

        // With current logic, pathfinderSlotAvailable is flawed (it sees 1 eligible edge and says false).
        // But since we want it to be false (used), this assertion passes even with current logic IF we ignore priority.
        // But wait! If priority is wrong (uses Adaptable), then pathfinderSlotAvailable might be TRUE (because Adaptable hides it? No, current logic doesn't hide it).
        // Current logic: Uses Adaptable. pathfinderSlotAvailable sees eligible edge -> returns false.
        // So both slots appear used.

        // Desired logic: Uses Pathfinder. pathfinderSlotAvailable sees eligible edge -> returns false.
        assertFalse("Pathfinder slot should be used", state.pathfinderSlotAvailable)

        assertEquals("Points should be 0", 0, state.pontosVantagem)

        // 2. Buy Attractive (Social Edge) - Should use Adaptable Slot
        val boughtAttractive = state.comprarVantagem(atraente)
        assertTrue("Should successfully buy Attractive", boughtAttractive)

        assertEquals("Adaptable slot should be used for Social Edge", "atraente", state.vantagemAdaptavelSelecionadaId)
        assertFalse("Adaptable slot should be used", state.adaptavelSlotAvailable)
        assertEquals("Points should still be 0", 0, state.pontosVantagem)
    }
}
