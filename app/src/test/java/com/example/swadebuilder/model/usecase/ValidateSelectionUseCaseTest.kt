package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.rules.BaseRules
import com.example.swadebuilder.model.rules.CrystalHeartRules
import com.example.swadebuilder.model.rules.GameRules
import com.example.swadebuilder.model.Requisito
import org.junit.Assert.*
import org.junit.Test

class ValidateSelectionUseCaseTest {

    private val useCase = ValidateSelectionUseCase()

    @Test
    fun `should block forbidden advantages by scenario`() {
        val rules = CrystalHeartRules
        val forbiddenId = "rico" // Crystal Heart forbids 'rico'
        // Mock requisites to satisfy Vantagem constructor
        val dummyRequisito = Requisito(estagio = "Novato")
        val vant = Vantagem(id = forbiddenId, nome = "Rico", categoria = com.example.swadebuilder.model.Categoria.SOCIAIS, requisitos = dummyRequisito)

        val context = ValidateSelectionUseCase.SelectionContext(
            ancestralidade = "Humanos",
            estagioAtualNome = "Novato",
            estagioAtualIndex = 0,
            atributosRaw = emptyMap(),
            periciasRaw = emptyMap(),
            vantagensSelecionadas = emptyList(),
            complicacoesSelecionadas = emptyMap(),
            cartaSelvagem = true,
            isMonstro = false,
            tipoMonstro = null
        )

        // Mock Rules that strictly forbid it
        val mockRules = object : GameRules {
            override fun startingResources() = BaseRules.startingResources()
            override fun defaultAncestralidade() = "Human"
            override fun forbiddenAdvantageIds() = setOf("rico")
        }

        val result = useCase.validateVantagem(vant, context, mockRules, emptyList())
        assertFalse("Should be forbidden", result.allowed)
        assertEquals("Proibido pelas regras do cenário.", result.reason)
    }
}
