package com.example.swadebuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class VantagensTest {

    @get:Rule
    val instantExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CriadorViewModel

    @Before
    fun setup() {
        viewModel = CriadorViewModel()
        viewModel.state.modoSupers = true
    }

    @Test
    fun testOMelhorQueHa() {
        val oMelhorQueHa = Vantagem(
            id = "o_melhor_que_ha",
            nome = "O Melhor Que Há",
            categoria = Categoria.SUPER,
            requisitos = Requisito(estagio = "Novato"),
            origem = "SUPER"
        )

        val superPoder = PurchasedPower(
            nome = "SuperPoder",
            custo = 10,
            baseCost = 10,
            poderId = "sp_superpoder"
        )

        viewModel.state.superPoderesComprados.add(superPoder)
        viewModel.definirPoderFavorecido(oMelhorQueHa, superPoder.poderId)

        assertEquals(superPoder.poderId, viewModel.state.idPoderFavorecido)
        assertTrue(viewModel.state.vantagensSelecionadas.contains(oMelhorQueHa))
    }

    @Test
    fun testOMelhorQueHaProgressionRestriction() {
        val oMelhorQueHa = Vantagem(
            id = "o_melhor_que_ha",
            nome = "O Melhor Que Há",
            categoria = Categoria.SUPER,
            requisitos = Requisito(estagio = "Novato"),
            origem = "SUPER"
        )
        viewModel.state.emProgresso = true
        assertFalse(viewModel.state.podeSelecionar(oMelhorQueHa))
    }
}