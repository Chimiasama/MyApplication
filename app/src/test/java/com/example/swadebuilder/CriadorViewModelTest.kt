package com.example.swadebuilder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.swadebuilder.model.CriadorViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CriadorViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CriadorViewModel

    @Before
    fun setup() {
        viewModel = CriadorViewModel()
    }

    @Test
    fun `resetStateParaNovoPersonagem should reset character state`() {
        // Given
        viewModel.state.nomePersonagem = "Old Name"
        viewModel.state.pontosAtributo = 0
        viewModel.state.pontosPericia = 0

        // When
        viewModel.resetStateParaNovoPersonagem(
            cartaSelvagem = true,
            maisPontosPericias = true,
            modoSupers = false
        )

        // Then
        assertEquals("", viewModel.state.nomePersonagem)
        assertEquals(5, viewModel.state.pontosAtributo)
        assertEquals(15, viewModel.state.pontosPericia)
    }
}
