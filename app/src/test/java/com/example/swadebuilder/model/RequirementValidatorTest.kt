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
    fun `requisito antecedente arcano reconhece AA de fantasia por id especifico`() {
        val vantagemComRequisitoArcano = Vantagem(
            id = "teste_arcano_fantasia",
            nome = "TESTE ARCANO FANTASIA",
            categoria = Categoria.SOCIAIS,
            requisitos = Requisito(
                estagio = "Novato",
                vantagensPrevias = listOf("antecedente_arcano")
            )
        )

        val state = CriadorState().apply {
            vantagensSelecionadas += Vantagem(
                id = "antecedente_arcano_mago_fantasia",
                nome = "ANTECEDENTE ARCANO (Mago)",
                categoria = Categoria.ANTECEDENTE,
                requisitos = Requisito(estagio = "Novato")
            )
        }

        assertTrue(RequirementValidator.canSelect(vantagemComRequisitoArcano, state))
    }

    @Test
    fun `requisito antecedente arcano reconhece ids aa de outros livros`() {
        val vantagemComRequisitoArcano = Vantagem(
            id = "teste_arcano_aa",
            nome = "TESTE ARCANO AA",
            categoria = Categoria.SOCIAIS,
            requisitos = Requisito(
                estagio = "Novato",
                vantagensPrevias = listOf("Antecedente Arcano")
            )
        )

        val state = CriadorState().apply {
            vantagensSelecionadas += Vantagem(
                id = "aa_tecnomagia",
                nome = "ANTECEDENTE ARCANO (Tecnomagia)",
                categoria = Categoria.ANTECEDENTE,
                requisitos = Requisito(estagio = "Novato")
            )
        }

        assertTrue(RequirementValidator.canSelect(vantagemComRequisitoArcano, state))
    }

    @Test
    fun `cavaleiro exige Obrigacao Maior`() {
        val cavaleiro = Vantagem(
            id = "cavaleiro",
            nome = "CAVALEIRO",
            categoria = Categoria.SOCIAIS,
            requisitos = Requisito(estagio = "Novato")
        )

        val state = CriadorState()
        assertFalse(RequirementValidator.canSelect(cavaleiro, state))

        state.complicacoesSelecionadas[complicacao("obrigacao")] = "Menor"
        assertFalse(RequirementValidator.canSelect(cavaleiro, state))

        state.complicacoesSelecionadas[complicacao("obrigacao")] = "Maior"
        assertTrue(RequirementValidator.canSelect(cavaleiro, state))
    }

    @Test
    fun `tiro duplo aprimorado exige tiro duplo com escolha de pericia`() {
        val tiroDuploAprimorado = Vantagem(
            id = "tiro_duplo_aprimorado",
            nome = "TIRO DUPLO APRIMORADO",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Novato")
        )

        val state = CriadorState()
        assertFalse(RequirementValidator.canSelect(tiroDuploAprimorado, state))

        state.vantagensSelecionadas += Vantagem(
            id = "tiro_duplo",
            nome = "TIRO DUPLO",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Novato")
        )
        assertFalse(RequirementValidator.canSelect(tiroDuploAprimorado, state))
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
