package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateMysticGhostTest {

    private fun vantagem(choice: String): Vantagem =
        Vantagem(
            id = "poderes_misticos_fantasma",
            nome = "PODERES MÍSTICOS (Fantasma)",
            categoria = Categoria.MONSTRUOSAS,
            requisitos = Requisito(estagio = "Experiente")
        ).apply { this.choice = choice }

    @Test
    fun `pacote aparicao aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Aparição"))

        assertEquals(
            listOf("atordoar", "cegar", "iluminar_obscurecer", "morosidade_velocidade"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }

    @Test
    fun `pacote poltergeist aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Poltergeist"))

        assertEquals(
            listOf("devastacao", "horrores_ilusorios", "som_silencio", "telecinese"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }

    @Test
    fun `pacote sombra aplica poderes fixos`() {
        val state = CriadorState()
        state.adicionarVantagem(vantagem("Sombra"))

        assertEquals(
            listOf("confusao", "fantoche", "manipulacao_elemental", "medo"),
            state.poderSlotsPorArcano["MISTICO"]?.filterNotNull()
        )
    }
}
