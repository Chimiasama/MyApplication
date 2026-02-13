package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoveInvalidAdvantagesAfterAncestryChangeUseCaseTest {

    private val useCase = RemoveInvalidAdvantagesAfterAncestryChangeUseCase()

    @Test
    fun `keeps automatic and mafia advantages even when requirement callback fails`() {
        val auto = vantagem(id = "auto_id", nome = "Auto Edge")
        val mafia = vantagem(id = "conexoes").apply { choice = "Máfia" }
        val manual = vantagem(id = "manual")
        val selected = mutableListOf(auto, mafia, manual)

        val result = useCase.execute(
            RemoveInvalidAdvantagesAfterAncestryChangeUseCase.Params(
                selectedAdvantages = selected,
                automaticAdvantages = listOf("Auto Edge"),
                automaticRacialAdvantages = emptyList(),
                automaticTropoAdvantageIds = emptySet(),
                meetsRequirements = { false }
            )
        )

        assertEquals(listOf("manual"), result.removedAdvantages.map { it.id })
        assertEquals(listOf("auto_id", "conexoes"), selected.map { it.id })
    }

    @Test
    fun `removes dependent chain when callback starts failing after first removal`() {
        val base = vantagem(id = "base")
        val dependent = vantagem(id = "dep", requisitos = Requisito(vantagensPrevias = listOf("base")))
        val selected = mutableListOf(base, dependent)

        val result = useCase.execute(
            RemoveInvalidAdvantagesAfterAncestryChangeUseCase.Params(
                selectedAdvantages = selected,
                automaticAdvantages = emptyList(),
                automaticRacialAdvantages = emptyList(),
                automaticTropoAdvantageIds = emptySet(),
                meetsRequirements = { vantagem ->
                    when (vantagem.id) {
                        "base" -> false
                        "dep" -> selected.any { it.id == "base" }
                        else -> true
                    }
                }
            )
        )

        assertEquals(listOf("base", "dep"), result.removedAdvantages.map { it.id })
        assertTrue(selected.isEmpty())
    }

    private fun vantagem(
        id: String,
        nome: String = id,
        categoria: Categoria = Categoria.SOCIAIS,
        requisitos: Requisito = Requisito()
    ): Vantagem = Vantagem(
        id = id,
        nome = nome,
        categoria = categoria,
        requisitos = requisitos
    )
}
