package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.MainSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rodada 46: trocar de Ancestralidade com um Tropo selecionado deixou de ser bloqueado (era
 * uma defesa do sistema antigo de Tropo do Arte da Guerra, que calculava atributo/perícia de
 * forma incremental — o motor atual é puro, recalcula do zero a cada chamada lendo raça+Tropo
 * juntos, então não tem mais esse risco). Cobre também a única invalidação real que a troca
 * livre abre: Usagimimi com Transição favorita só permite o Tropo Elementalista (ou nenhum) —
 * ver CriadorState.isUsagimimiTransicaoRestrictionActive/aplicarAncestralidade.
 */
class CriadorStateTrocarRacaComTropoTest {

    private fun snapshotWith(racas: List<RacialModifier>, tropos: List<Tropo>): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR").associateWith { it },
        listaPericias = listOf(Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList<Vantagem>(),
        listaPoderes = emptyList<Poder>(),
        listaTropos = tropos,
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private val elfos = RacialModifier(nome = "ELFOS", origem = "BASICO")
    private val usagimimi = RacialModifier(nome = "USAGIMIMI", origem = "ARTE_DA_GUERRA")
    private val tropoSamurai = Tropo(id = "tropo_samurai", nome = "Samurai", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "")
    private val tropoElementalista = Tropo(id = "tropo_elementalista", nome = "Elementalista", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "")

    private fun estadoBase(): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(elfos, usagimimi), listOf(tropoSamurai, tropoElementalista)))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "ELFOS"
        return state
    }

    @Test
    fun `aba Ancestralidade continua liberada com um Tropo selecionado`() {
        val state = estadoBase()
        state.tropoSelecionado = tropoSamurai

        assertTrue(state.isSectionEnabled(MainSection.ANCESTRALIDADES))
    }

    @Test
    fun `trocar de raca com Tropo selecionado preserva o Tropo quando nao ha restricao`() {
        val state = estadoBase()
        state.tropoSelecionado = tropoSamurai

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("USAGIMIMI", feedback)

        assertEquals("tropo_samurai", state.tropoSelecionado?.id)
        assertEquals("USAGIMIMI", state.ancestralidade)
    }

    @Test
    fun `virar Usagimimi com Transicao ja escolhida remove automaticamente um Tropo incompativel`() {
        val state = estadoBase()
        state.tropoSelecionado = tropoSamurai
        // Simula Transição favorita já escolhida numa sessão anterior (o campo não é
        // resetado ao trocar de raça — só passa a valer quando a raça atual é Usagimimi).
        state.usagimimiPericiaEscolhida = "Transição"

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("USAGIMIMI", feedback)

        assertNull(state.tropoSelecionado)
        assertTrue(feedback.any { it.contains("Samurai") && it.contains("Transição") })
    }

    @Test
    fun `virar Usagimimi com Transicao ja escolhida mantem Elementalista, que e permitido`() {
        val state = estadoBase()
        state.tropoSelecionado = tropoElementalista
        state.usagimimiPericiaEscolhida = "Transição"

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("USAGIMIMI", feedback)

        assertEquals("tropo_elementalista", state.tropoSelecionado?.id)
    }
}
