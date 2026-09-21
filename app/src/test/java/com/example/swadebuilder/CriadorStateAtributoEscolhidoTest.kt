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
 * Escolha de atributo (TARGET_ATTRIBUTE_OR_SKILL) migrada pro sistema de
 * Seleção — mesmo padrão de Herança/Signo (ver CriadorStateSignoDeNascencaTest),
 * mas o "efeito mecânico" é o próprio atributo escolhido, não um pacote de
 * traços fixo por opção (ver AncestryVariantRegistry.meioOrc()/feral() e a
 * Seleção aninhada de humanos() Sci-Fi). Estes testes fixam que o traço real
 * (traitId=ATTRIBUTE_BOOST + targetRef=o atributo escolhido) reflete
 * `humanoMineradorAtributo` corretamente pras 3 raças que compartilham esse
 * campo, e que o default de cada raça (quando nada foi escolhido ainda)
 * corresponde ao que existia antes desta migração.
 */
class CriadorStateAtributoEscolhidoTest {

    private fun atributosPadrao() = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")

    private fun snapshotCom(vararg racas: RacialModifier): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas.toList(),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = atributosPadrao(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList<Pericia>(),
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

    private fun meioOrc() = RacialModifier(
        nome = "MEIO-ORCS",
        habilidades = listOf(RacialAbility(nome = "Endurecido", descricao = "", id = "ENDURECIDO")),
        origem = "FANTASIA"
    )

    private fun feral() = RacialModifier(
        nome = "Feral",
        habilidades = listOf(RacialAbility(nome = "Primitivo", descricao = "", id = "PRIMITIVO")),
        origem = "ARTE_DA_GUERRA"
    )

    private fun humanosSciFi() = RacialModifier(
        nome = "HUMANOS",
        habilidades = emptyList(),
        origem = "SCI_FI",
        opcoes = listOf("Padrão", "Baixa Gravidade", "Minerador")
    )

    @Test
    fun `meio-orc sem escolha usa o default Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))
    }

    @Test
    fun `meio-orc com Forca escolhida sobe Forca e nao Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.selecionarHumanoMineradorAtributo("Força")

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `feral sem escolha usa o default Forca`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(feral()))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "Feral"
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `feral com Agilidade escolhida sobe Agilidade`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(feral()))
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "Feral"
        state.selecionarHumanoMineradorAtributo("Agilidade")

        assertEquals(6, state.atributoMinRaw("Agilidade"))
        assertEquals(4, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `humano sci-fi minerador sem escolha usa o default Forca`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Minerador")
        state.humanoMineradorAtributo = null

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `humano sci-fi minerador com Vigor escolhido sobe Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Minerador")
        state.selecionarHumanoMineradorAtributo("Vigor")

        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))
    }

    @Test
    fun `humano sci-fi baixa gravidade nao ganha bonus de forca ou vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(humanosSciFi()))
        state.compendioSciFiAtivo = true
        state.ancestralidade = "HUMANOS"
        state.selecionarScifiVariant("Baixa Gravidade")

        assertEquals(4, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(6, state.atributoMinRaw("Agilidade"))
    }

    @Test
    fun `trocar de meio-orc Vigor para Forca nao deixa Vigor vazando`() {
        val state = CriadorState()
        state.updateGameData(snapshotCom(meioOrc()))
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "MEIO-ORCS"
        state.selecionarHumanoMineradorAtributo("Vigor")
        assertEquals(6, state.atributoMinRaw("Vigor"))

        state.selecionarHumanoMineradorAtributo("Força")

        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(6, state.atributoMinRaw("Força"))
    }
}
