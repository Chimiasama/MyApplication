package com.example.swadebuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Vantagem
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

class VantagensTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CriadorViewModel

    @Before
    fun setup() {
        viewModel = CriadorViewModel()
        viewModel.state.modoSupers = true
    }

    @Test
    fun testOMelhorQueHa() {
        val superPoder = PurchasedPower(nome = "SuperPoder", custo = 10, baseCost = 10, poderId = "sp_superpoder")
        viewModel.state.superPoderesComprados.add(superPoder)
        viewModel.definirPoderFavorecido(superPoder.poderId)

        assertEquals(superPoder.poderId, viewModel.state.poderFavoritoId)
    }

    @Test
    fun testOMelhorQueHaProgressionRestriction() {
        val oMelhorQueHa = Vantagem(id = "o_melhor_que_ha", nome = "O Melhor Que Há", categoria = com.example.swadebuilder.model.Categoria.SUPER, requisitos = com.example.swadebuilder.model.Requisitos(estagio = "Novato"), origem = "SUPER")
        viewModel.state.emProgresso = true
        assertFalse(viewModel.state.podeSelecionar(oMelhorQueHa))
    }
}