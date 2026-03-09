package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateKirinSignTest {

    private fun sorte() = Vantagem(
        id = "sorte",
        nome = "Sorte",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito()
    )

    @Test
    fun `kirin trata sorte como vantagem automatica do signo`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "HUMANOS"
            signoAdgSelecionado = "Kirin"
        }

        val sorte = sorte()
        state.vantagensSelecionadas.add(sorte)

        assertTrue(state.isVantagemAutomatica(sorte))
        assertFalse(state.podeRemoverVantagem(sorte).first)
    }
}
