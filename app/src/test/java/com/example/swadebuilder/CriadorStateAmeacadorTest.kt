package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateAmeacadorTest {

    private fun ameacador(): Vantagem = Vantagem(
        id = "ameacador",
        nome = "AMEAÇADOR",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito(
            estagio = "Novato",
            vantagensPrevias = listOf("sanguinario", "desagradavel", "sem_escrupulos", "feio")
        )
    )

    private fun complicacao(id: String): Complicacao = Complicacao(
        id = id,
        name = id,
        severity = "Menor",
        description = "",
        origem = "BASICO"
    )

    @Test
    fun `pode selecionar ameacador com desagradavel`() {
        val state = CriadorState()
        assertFalse(state.podeSelecionar(ameacador()))

        state.complicacoesSelecionadas[complicacao("desagradavel")] = "Menor"
        assertTrue(state.podeSelecionar(ameacador()))
    }
}
