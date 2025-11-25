package com.example.swadebuilder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swadebuilder.model.CriadorViewModel
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VantagensTest {

    private lateinit var viewModel: CriadorViewModel

    @Before
    fun setup() {
        viewModel = CriadorViewModel()
        viewModel.resetStateParaNovoPersonagem(
            cartaSelvagem = true,
            maisPontosPericias = true,
            modoSupers = false
        )
    }

    private fun getAAMilagres(): Vantagem {
        return listaVantagens.first { it.id == "antecedente_arcano_milagres" }
    }

    @Test
    fun celestial_deveReceberAAMilagres_aoTrocarParaCelestial() {
        viewModel.state.aplicarAncestralidade("CELESTIAIS")
        assertTrue(
            "Celestiais devem receber AA(Milagres) automaticamente",
            viewModel.state.vantagensSelecionadas.any { it.id == "antecedente_arcano_milagres" }
        )
    }

    @Test
    fun celestial_deveRemoverAAMilagres_aoTrocarParaOutraRaca() {
        viewModel.state.aplicarAncestralidade("CELESTIAIS")
        viewModel.state.aplicarAncestralidade("HUMANOS")
        assertFalse(
            "AA(Milagres) deve ser removido ao trocar de Celestial para outra raça",
            viewModel.state.vantagensSelecionadas.any { it.id == "antecedente_arcano_milagres" }
        )
    }

}
