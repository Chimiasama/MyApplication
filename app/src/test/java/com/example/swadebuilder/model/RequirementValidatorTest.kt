package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RequirementValidatorTest {

    private fun ameacador(): Vantagem = Vantagem(
        id = "ameacador",
        nome = "AMEAÇADOR",
        categoria = Categoria.SOCIAIS,
        origem = "BASICO",
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

    private fun vantagem(id: String): Vantagem = Vantagem(
        id = id,
        nome = id,
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito(estagio = "Novato")
    )

    @Test
    fun `ameacador exige ao menos uma complicacao valida`() {
        val state = CriadorState()

        assertFalse(RequirementValidator.canSelect(ameacador(), state))

        state.complicacoesSelecionadas[complicacao("feio")] = "Menor"
        assertTrue(RequirementValidator.canSelect(ameacador(), state))
    }

    @Test
    fun `ameacador aceita liberadores extras de outros livros`() {
        val state = CriadorState()
        state.complicacoesSelecionadas[complicacao("sinistro")] = "Menor"

        assertTrue(RequirementValidator.canSelect(ameacador(), state))
    }

    @Test
    fun `vantagens normais continuam exigindo todos prerequisitos`() {
        val vantagemComum = Vantagem(
            id = "teste_and",
            nome = "TESTE AND",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(
                estagio = "Novato",
                vantagensPrevias = listOf("prev_a", "prev_b")
            )
        )

        val state = CriadorState()
        state.vantagensSelecionadas += vantagem("prev_a")
        assertFalse(RequirementValidator.canSelect(vantagemComum, state))

        state.vantagensSelecionadas += vantagem("prev_b")
        assertTrue(RequirementValidator.canSelect(vantagemComum, state))
    }
}
