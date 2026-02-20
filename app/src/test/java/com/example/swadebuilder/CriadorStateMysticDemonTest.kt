package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateMysticDemonTest {

    private fun vantagem(choice: String): Vantagem =
        Vantagem(
            id = "poderes_misticos_demonio",
            nome = "PODERES MÍSTICOS (Demônio)",
            categoria = Categoria.MONSTRUOSAS,
            requisitos = Requisito(estagio = "Experiente")
        ).apply { this.choice = choice }

    @Test
    fun `pacote invocador aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Invocador"))

        assertEquals(
            listOf("conjurar_aliado", "conjurar_demonio", "protecao", "zumbi"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }

    @Test
    fun `pacote possessor aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Possessor"))

        assertEquals(
            listOf("aumentar_reduzir_caracteristica", "fantoche", "maldicao", "pesadelos"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }

    @Test
    fun `pacote sedutor aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Sedutor"))

        assertEquals(
            listOf("aumentar_reduzir_caracteristica", "disfarce", "empatia", "leitura_mental"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }

    @Test
    fun `pacote trapaceiro aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Trapaceiro"))

        assertEquals(
            listOf("disfarce", "deflexao", "horrores_ilusorios", "medo"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }
}
