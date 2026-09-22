package com.example.swadebuilder

import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.ui.MainSection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gate de ativação genérico do sistema de Tropo (rodada 43 do audit doc): `modoTroposAtivo`
 * (Arte da Guerra sempre liga; qualquer outro livro só liga pela checkbox manual) e
 * `isSectionEnabled()` — só bloqueia TODAS as outras abas até a primeira escolha quando o
 * livro OBRIGA escolher um Tropo (hoje só Arte da Guerra). A trava de Ancestralidade quando
 * um Tropo está selecionado foi removida na rodada 46 (o motor de raça+Tropo é puro/
 * recalculado do zero a cada chamada — não precisa mais dessa defesa; ver
 * CriadorState.isSectionEnabled/aplicarAncestralidade).
 */
class TropoGateTest {

    private fun estadoBase(): CriadorState = CriadorState().apply {
        listaAncestralidadesJson = listOf(RacialModifier(nome = "Humano"))
        ancestralidade = "Humano"
    }

    private val tropoQualquer = Tropo(id = "tropo_x", nome = "Tropo X", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "")

    @Test
    fun `sem nenhum livro de tropo ativo, modoTroposAtivo eh falso e nada trava`() {
        val state = estadoBase()

        assertFalse(state.modoTroposAtivo)
        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
        assertTrue(state.isSectionEnabled(MainSection.VANTAGENS))
    }

    @Test
    fun `arte da guerra ativo sem tropo escolhido trava tudo, exceto Resumo, Ancestralidade e Tropos`() {
        val state = estadoBase()
        state.compendioArteDaGuerraAtivo = true

        assertTrue(state.modoTroposAtivo)
        assertTrue(state.isSectionEnabled(MainSection.RESUMO))
        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
        assertTrue(state.isSectionEnabled(MainSection.TROPOS))
        assertFalse(state.isSectionEnabled(MainSection.VANTAGENS))
        assertFalse(state.isSectionEnabled(MainSection.COMPLICACOES))
    }

    @Test
    fun `arte da guerra ativo com tropo escolhido libera tudo, inclusive Ancestralidade`() {
        val state = estadoBase()
        state.compendioArteDaGuerraAtivo = true
        state.tropoSelecionado = tropoQualquer

        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
        assertTrue(state.isSectionEnabled(MainSection.VANTAGENS))
        assertTrue(state.isSectionEnabled(MainSection.COMPLICACOES))
        assertTrue(state.isSectionEnabled(MainSection.TROPOS))
    }

    @Test
    fun `outro livro com checkbox manual ligada e sem tropo escolhido NAO trava nada, so libera a aba Tropo`() {
        val state = estadoBase()
        state.modoTroposHabilitadoManualmente = true

        assertTrue(state.modoTroposAtivo)
        // "Nenhum Tropo escolhido" é um estado final válido pra um livro opcional — não
        // bloqueia o resto da ficha esperando o jogador visitar a aba Tropo.
        assertTrue(state.isSectionEnabled(MainSection.VANTAGENS))
        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
        assertTrue(state.isSectionEnabled(MainSection.COMPLICACOES))
    }

    @Test
    fun `outro livro com checkbox manual ligada e um tropo de verdade escolhido tambem libera Ancestralidade`() {
        val state = estadoBase()
        state.modoTroposHabilitadoManualmente = true
        state.tropoSelecionado = tropoQualquer

        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
        assertTrue(state.isSectionEnabled(MainSection.VANTAGENS))
    }

    @Test
    fun `checkbox manual nao importa quando arte da guerra ja esta ativo, o gate continua ligado`() {
        val state = estadoBase()
        state.compendioArteDaGuerraAtivo = true
        state.modoTroposHabilitadoManualmente = false

        assertTrue(state.modoTroposAtivo)
    }
}
