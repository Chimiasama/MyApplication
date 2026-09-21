package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Categoria
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
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Arma de Sopro" (Draconianos, livro Fantasia): "causa 2d6 de dano em um Modelo de Cone
 * ou em uma linha de 12 quadros" — sem exibição nenhuma antes desta rodada. Agora entra
 * como item de Armas à Distância (via CriadorState.extrairArmasNaturais(), mesma lista
 * que alimenta Resumo e PDF). "Queimar" (Vantagem, mesmo livro): "o dano... aumenta em um
 * tipo de dado" — 2d6 vira 2d8.
 */
class CriadorStateArmaDeSoproTest {

    private fun snapshotWith(racas: List<RacialModifier>, vantagens: List<Vantagem>): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR").associateWith { it },
        listaPericias = listOf(Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = vantagens,
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private val draconianos = RacialModifier(
        nome = "DRACONIANOS",
        habilidades = listOf(
            RacialAbility(nome = "ARMA DE SOPRO", descricao = "", id = "ARMA_DE_SOPRO", category = "racial_trait_positive")
        ),
        tags = listOf("arma_de_sopro"),
        especieId = "draconianos"
    )

    private val queimar = Vantagem(
        id = "queimar",
        nome = "QUEIMAR",
        categoria = Categoria.COMBATE,
        origem = "FANTASIA",
        requisitos = Requisito(atributoMin = mapOf("Vigor" to 8), tags = listOf("arma_de_sopro"))
    )

    @Test
    fun `Draconiano sem Queimar mostra Ataque de Sopro com 2d6`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(draconianos), listOf(queimar)))
        state.ancestralidade = "DRACONIANOS"

        val sopro = state.extrairArmasNaturais().firstOrNull { it.nome == "Ataque de Sopro" }
        assertTrue("Esperava um item 'Ataque de Sopro'", sopro != null)
        assertEquals("2d6", (sopro!!.dano as JsonPrimitive).content)
        assertEquals("Cone ou Linha (12)", (sopro.distancia as JsonPrimitive).content)
    }

    @Test
    fun `Draconiano com Queimar mostra Ataque de Sopro com 2d8`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(draconianos), listOf(queimar)))
        state.ancestralidade = "DRACONIANOS"
        state.vantagensSelecionadas.add(queimar)

        val sopro = state.extrairArmasNaturais().firstOrNull { it.nome == "Ataque de Sopro" }
        assertTrue("Esperava um item 'Ataque de Sopro'", sopro != null)
        assertEquals("2d8", (sopro!!.dano as JsonPrimitive).content)
    }

    @Test
    fun `Raca sem Arma de Sopro nao ganha o item Ataque de Sopro`() {
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), especieId = "humano")
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(humanos), listOf(queimar)))
        state.ancestralidade = "HUMANOS"

        assertNull(state.extrairArmasNaturais().firstOrNull { it.nome == "Ataque de Sopro" })
    }
}
