package com.example.swadebuilder

import com.example.swadebuilder.model.AdvancementAction
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateArcaneSkillGrantTest {

    private fun aaMago() = Vantagem(
        id = "antecedente_arcano_mago_fantasia",
        nome = "ANTECEDENTE ARCANO (Mago)",
        categoria = Categoria.ANTECEDENTE,
        origem = "FANTASIA",
        requisitos = Requisito(),
        subtipoArcano = "MAGO"
    )

    private fun aaElementalista() = Vantagem(
        id = "antecedente_arcano_elementalista",
        nome = "ANTECEDENTE ARCANO (Elementalista)",
        categoria = Categoria.ANTECEDENTE,
        origem = "FANTASIA",
        requisitos = Requisito(),
        subtipoArcano = "ELEMENTALISTA"
    )

    private fun fantasiaState() = CriadorState().apply {
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        listaPericias = listOf(
            Pericia(nome = "Conjurar", atributo = "ASTUCIA", basica = false, origem = "FANTASIA")
        )
        ensureAllAtributosRegistered()
        ensureAllPericiasRegistered()
        compendioFantasiaAtivo = true
        arcanoInfo = mapOf(
            "MAGO" to Triple(6, 15, "Conjurar"),
            "ELEMENTALISTA" to Triple(5, 10, "Conjurar")
        )
    }

    @Test
    fun `primeiro antecedente arcano de fantasia concede d4 na pericia arcana durante a criacao`() {
        val state = fantasiaState().apply { pontosVantagem = 1 }
        val conjurar = state.periciasComIdiomas().first { it.nome == "Conjurar" }

        state.comprarVantagem(aaMago())

        assertEquals(4, state.rawTotal(conjurar))
        assertEquals(0, state.baseIncsPorPericia[conjurar])
    }

    @Test
    fun `antecedente arcano de fantasia recalcula pontos sem elevar pericia ja comprada`() {
        val state = fantasiaState().apply { pontosVantagem = 1 }
        val conjurar = state.periciasComIdiomas().first { it.nome == "Conjurar" }
        state.increasePericiaFromAdvancement(conjurar, cost = 1)
        val pontosAntes = state.pontosPericia

        state.comprarVantagem(aaMago())

        assertEquals(4, state.rawTotal(conjurar))
        assertEquals(0, state.baseIncsPorPericia[conjurar])
        assertEquals(pontosAntes + 1, state.pontosPericia)
    }

    @Test
    fun `segundo antecedente arcano com a mesma pericia mantem apenas o d4 inicial`() {
        val state = fantasiaState().apply { pontosVantagem = 2 }
        val conjurar = state.periciasComIdiomas().first { it.nome == "Conjurar" }

        state.comprarVantagem(aaMago())
        state.comprarVantagem(aaElementalista())

        assertEquals(4, state.rawTotal(conjurar))
        assertEquals(0, state.baseIncsPorPericia[conjurar])
    }

    @Test
    fun `antecedente arcano comprado com xp nao soma d4 a pericia existente`() {
        val state = fantasiaState().apply {
            modoProgressaoAtivo = true
            pontosVantagem = 1
        }
        val conjurar = state.periciasComIdiomas().first { it.nome == "Conjurar" }
        state.increasePericiaFromAdvancement(conjurar, cost = 1)
        state.snapshotFrozenSkillIncrements()

        state.vantagensSelecionadas.add(aaMago())
        state.advancementHistory.add(
            AdvancementAction.SpendOnAdvantage(
                advantageId = "antecedente_arcano_mago_fantasia",
                stageName = "Novato",
                arcanoKey = "MAGO"
            )
        )

        assertEquals(4, state.rawTotal(conjurar))
        assertEquals(1, state.baseIncsPorPericia[conjurar])
    }
}
