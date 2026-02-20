package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateMysticAngelTest {

    @Test
    fun `adiciona pacote arauto com poderes fixos de anjo`() {
        val state = CriadorState()
        val vantagem = Vantagem(
            id = "poderes_misticos_anjo",
            nome = "PODERES MÍSTICOS (Anjo)",
            categoria = Categoria.MONSTRUOSAS,
            requisitos = Requisito(estagio = "Experiente")
        ).apply { choice = "Arauto" }

        state.adicionarVantagem(vantagem)

        val poderes = state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        assertEquals(
            listOf("adivinhacao", "aumentar_reduzir_caracteristica", "cura", "visao_distante"),
            poderes
        )
    }

    @Test
    fun `adiciona pacote morte com poderes fixos de anjo`() {
        val state = CriadorState()
        val vantagem = Vantagem(
            id = "poderes_misticos_anjo",
            nome = "PODERES MÍSTICOS (Anjo)",
            categoria = Categoria.MONSTRUOSAS,
            requisitos = Requisito(estagio = "Experiente")
        ).apply { choice = "Morte" }

        state.adicionarVantagem(vantagem)

        val poderes = state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        assertEquals(
            listOf("aumentar_reduzir_caracteristica", "deflexao", "ferir", "protecao"),
            poderes
        )
    }
}
