package com.example.swadebuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
        val superPoder = PurchasedPower(
            nome = "SuperPoder",
            custo = 10,
            baseCost = 10,
            poderId = "sp_superpoder"
        )
        viewModel.state.superPoderesComprados.add(superPoder)
        viewModel.definirPoderFavorecido(superPoder.poderId)

        assertEquals(superPoder.poderId, viewModel.state.poderFavoritoId)
    }

    @Test
    fun testOMelhorQueHaProgressionRestriction() {
        val oMelhorQueHa = Vantagem(
            id = "o_melhor_que_ha",
            nome = "O Melhor Que Há",
            categoria = Categoria.SUPER,
            origem = "SUPER",
            requisitos = Requisito(
                estagio = "Novato" // demais campos usam os defaults do data class
            )
        )

        viewModel.state.emProgresso = true

        assertFalse(viewModel.state.podeSelecionar(oMelhorQueHa))
    }
}
