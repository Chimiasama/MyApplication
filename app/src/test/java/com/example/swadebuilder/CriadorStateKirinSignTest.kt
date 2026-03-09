package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
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

    private fun elevarOMoral() = Vantagem(
        id = "elevar_o_moral",
        nome = "Elevar o Moral",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito()
    )

    @Test
    fun `raposa usa id correto elevar o moral`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "HUMANOS"
            listaVantagens.add(elevarOMoral())
        }

        state.selecionarSigno("Raposa")

        assertTrue(state.vantagensSelecionadas.any { it.id == "elevar_o_moral" })
        assertTrue("elevar_o_moral" in state.vantagensAutomaticasDoSigno)
    }


    @Test
    fun `adg nao ignora cap de pericia por valor inicial racial`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Akaimimi (Panda Vermelho)"
            racialSkillStartMap = mapOf(
                "AKAIMIMI (PANDA VERMELHO)" to mapOf(
                    "CONVENCAO" to 2,
                    "CONHECIMENTO GERAL" to 4
                )
            )
        }

        val convencao = com.example.swadebuilder.model.Pericia("Convenção", "ASTUCIA", true)
        val conhecimento = com.example.swadebuilder.model.Pericia("Conhecimento Geral", "ASTUCIA", true)

        assertEquals(13, state.periciaCapRaw(convencao))
        assertEquals(14, state.periciaCapRaw(conhecimento))
    }

}
