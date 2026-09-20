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
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateDiminutoRaceSwitchTest {

    private fun snapshotWith(racas: List<RacialModifier>): GameDataSnapshot = GameDataSnapshot(
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
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private fun diminutoHab(traitId: String, valor: Int, pontos: Int) = RacialAbility(
        nome = "Diminuto",
        descricao = "Tamanho $valor, Diminuto.",
        traitId = traitId,
        value = valor,
        pontos = pontos
    )

    @Test
    fun `trocar de raca Minuscula pra Humano recalcula peso e custo pro valor cheio automaticamente`() {
        val fada = RacialModifier(nome = "FADA", habilidades = listOf(diminutoHab("DIMINUTO_TAMANHO_4", -4, -6)), origem = "FANTASIA")
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), origem = "BASICO")

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(fada, humanos)))
        state.ancestralidade = "FADA"

        val armadura = EquipamentoItem(nome = "Armadura de Placas", peso = JsonPrimitive("40"), custo = JsonPrimitive("300"))
        state.equipamentosComprados.add(armadura)

        assertEquals(30, state.custoEquipamentoEfetivo(armadura))
        assertEquals(4f, state.pesoEquipamentoEfetivo(armadura)!!, 0.001f)

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("HUMANOS", feedback)

        // Trocou de tamanho (Minúsculo -> normal): o item foi devolvido, mochila vazia.
        assertEquals(0, state.equipamentosComprados.size)
        assertTrue(
            "Esperava mensagem de devolução de equipamento, mensagens: $feedback",
            feedback.any { it.contains("devolvido") && it.contains("Diminuto") }
        )
    }

    @Test
    fun `trocar entre duas racas de tamanho normal nao mexe na mochila`() {
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), origem = "BASICO")
        val elfos = RacialModifier(nome = "ELFOS", habilidades = emptyList(), origem = "BASICO")

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(humanos, elfos)))
        state.ancestralidade = "HUMANOS"

        val espada = EquipamentoItem(nome = "Espada Longa", peso = JsonPrimitive("4"), custo = JsonPrimitive("300"))
        state.equipamentosComprados.add(espada)

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("ELFOS", feedback)

        assertEquals(1, state.equipamentosComprados.size)
        assertTrue(feedback.none { it.contains("Diminuto") })
    }

    @Test
    fun `trocar entre dois tiers diferentes de Diminuto tambem devolve o equipamento`() {
        val fadaMinuscula = RacialModifier(nome = "FADA", habilidades = listOf(diminutoHab("DIMINUTO_TAMANHO_4", -4, -6)), origem = "FANTASIA")
        val povoRatoPequeno = RacialModifier(nome = "POVO RATAZANA", habilidades = listOf(diminutoHab("DIMINUTO_TAMANHO_2", -2, -2)), origem = "FANTASIA")

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(fadaMinuscula, povoRatoPequeno)))
        state.ancestralidade = "FADA"

        val faca = EquipamentoItem(nome = "Faca", peso = JsonPrimitive("1"), custo = JsonPrimitive("20"))
        state.equipamentosComprados.add(faca)

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("POVO RATAZANA", feedback)

        assertEquals(0, state.equipamentosComprados.size)
        assertTrue(feedback.any { it.contains("devolvido") })
    }
}
