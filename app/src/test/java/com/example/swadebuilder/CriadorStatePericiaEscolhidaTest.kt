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
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kitsunemimi (Preparado) e Gnomo (Obsessivos) migrados pro sistema de
 * Seleção (TARGET_ATTRIBUTE_OR_SKILL/SKILL) — mesmo padrão de Meio-Orc/
 * Feral, mas escolhendo uma PERÍCIA em vez de um atributo. Antes eram só
 * `if` avulsos em CriadorState lendo gnomoPericiaEscolhida/
 * kitsunemimiPericiaEscolhida; a mecânica em si já funcionava, migrada
 * agora pro registro pra ficar consistente com o resto do app.
 */
class CriadorStatePericiaEscolhidaTest {

    private fun atributosPadrao() = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")

    private fun snapshotCom(raca: RacialModifier): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = listOf(raca),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = atributosPadrao(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = listOf(
            Pericia(nome = "Conhecimento Acadêmico", atributo = "ASTUCIA", basica = false),
            Pericia(nome = "Provocar", atributo = "ESPIRITO", basica = false),
            Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)
        ),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList<Vantagem>(),
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private fun kitsunemimi() = RacialModifier(
        nome = "Kitsunemimi (Raposa)",
        habilidades = listOf(
            RacialAbility(nome = "Visão no Escuro", descricao = "", id = "VISAO_NO_ESCURO"),
            RacialAbility(nome = "Preparado", descricao = "", id = "PREPARADO")
        ),
        origem = "ARTE_DA_GUERRA"
    )

    private fun gnomo() = RacialModifier(
        nome = "Gnomo",
        habilidades = listOf(
            RacialAbility(nome = "Magia Gnômica", descricao = "", id = "MAGIA_GNOMICA"),
            RacialAbility(nome = "Obsessivos", descricao = "", id = "OBSESSIVOS")
        ),
        origem = "PATHFINDER"
    )

    @Test
    fun `kitsunemimi com Provocar escolhido comeca com Provocar d4`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(kitsunemimi()))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "Kitsunemimi (Raposa)"
        state.selecionarPericiaKitsunemimi("Provocar")

        val provocar = Pericia(nome = "Provocar", atributo = "ESPIRITO", basica = false)
        assertEquals(4, state.periciaStartRaw(state.ancestralidade, provocar))
    }

    @Test
    fun `gnomo com Conhecimento Academico escolhido comeca com Conhecimento Academico d4`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(gnomo()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Gnomo"
        state.selecionarPericiaGnomo("Conhecimento Acadêmico")

        val conhecimento = Pericia(nome = "Conhecimento Acadêmico", atributo = "ASTUCIA", basica = false)
        assertEquals(4, state.periciaStartRaw(state.ancestralidade, conhecimento))
    }

    @Test
    fun `trocar de pericia escolhida no gnomo nao deixa a anterior vazando`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(gnomo()))
        state.compendioPathfinderAtivo = true
        state.ancestralidade = "Gnomo"
        state.selecionarPericiaGnomo("Conhecimento Acadêmico")

        state.selecionarPericiaGnomo("Perceber")

        val conhecimento = Pericia(nome = "Conhecimento Acadêmico", atributo = "ASTUCIA", basica = false)
        val perceber = Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)
        assertEquals(0, state.periciaStartRaw(state.ancestralidade, conhecimento))
        assertEquals(4, state.periciaStartRaw(state.ancestralidade, perceber))
    }
}
