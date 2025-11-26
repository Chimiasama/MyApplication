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

    @Test
    fun pequeninos_deveAdicionarDesvantagensRaciais() {
        viewModel.state.aplicarAncestralidade("PEQUENINOS")
        assertTrue(
            "Pequeninos devem ter 'Tamanho -1' em desvantagensRaciais",
            viewModel.state.desvantagensRaciais.contains("Tamanho -1")
        )
        assertTrue(
            "Pequeninos devem ter 'Movimentação Reduzida' em desvantagensRaciais",
            viewModel.state.desvantagensRaciais.contains("Movimentação Reduzida")
        )
    }

    @Test
    fun celestial_deveDesabilitarAAMilagres_noModoSupers() {
        viewModel.resetStateParaNovoPersonagem(
            cartaSelvagem = true,
            maisPontosPericias = true,
            modoSupers = true
        )
        viewModel.state.aplicarAncestralidade("CELESTIAIS")
        assertTrue(
            "AA(Milagres) de Celestiais deve ser desabilitado no modo Supers",
            viewModel.state.celestialAAMilagresDesabilitado
        )
    }

    @Test
    fun meioElfo_deveAumentarAgilidadeMaxima_comHerancaAgil() {
        viewModel.state.aplicarAncestralidade("MEIO-ELFOS")
        viewModel.state.meioElfoAgil = true
        val maxAgilidade = viewModel.state.atributoMaxRaw("AGILIDADE")
        assertTrue("Meio-elfo ágil deve ter Agilidade máxima aumentada", maxAgilidade > 12)
    }

    @Test
    fun meioElfo_naoDeveAumentarAgilidadeMaxima_comHerancaHumana() {
        viewModel.state.aplicarAncestralidade("MEIO-ELFOS")
        viewModel.state.meioElfoAgil = false
        val maxAgilidade = viewModel.state.atributoMaxRaw("AGILIDADE")
        assertTrue("Meio-elfo humano não deve ter Agilidade máxima aumentada", maxAgilidade == 12)
    }
}
