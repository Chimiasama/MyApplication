package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.CrystalHeart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManageCrystalHeartUseCasesTest {

    private val upsertUseCase = UpsertCrystalHeartUseCase()
    private val removeUseCase = RemoveCrystalHeartUseCase()

    @Test
    fun `upsert adiciona quando id nao existe`() {
        val current = listOf(heart("h1"))
        val updated = upsertUseCase.execute(current, heart("h2"))

        assertEquals(listOf("h1", "h2"), updated.map { it.id })
    }

    @Test
    fun `upsert substitui quando id existe`() {
        val current = listOf(heart("h1", nome = "Old"))
        val updated = upsertUseCase.execute(current, heart("h1", nome = "New"))

        assertEquals(1, updated.size)
        assertEquals("New", updated.first().nome)
    }

    @Test
    fun `remove retorna placeholder quando removido estava selecionado`() {
        val a = heart("h1")
        val placeholder = heart("starter", placeholder = true)
        val result = removeUseCase.execute(
            current = listOf(a, placeholder),
            heartIdToRemove = "h1",
            currentlySelectedId = "h1"
        )

        assertEquals(listOf("starter"), result.updated.map { it.id })
        assertEquals("starter", result.newSelected?.id)
    }

    @Test
    fun `remove preserva selecao quando id removido era outro`() {
        val selected = heart("h1")
        val other = heart("h2")
        val result = removeUseCase.execute(
            current = listOf(selected, other),
            heartIdToRemove = "h2",
            currentlySelectedId = "h1"
        )

        assertEquals(listOf("h1"), result.updated.map { it.id })
        assertEquals("h1", result.newSelected?.id)
    }

    @Test
    fun `remove sem placeholder deixa selecao nula quando remove selecionado`() {
        val selected = heart("h1")
        val result = removeUseCase.execute(
            current = listOf(selected),
            heartIdToRemove = "h1",
            currentlySelectedId = "h1"
        )

        assertEquals(emptyList<String>(), result.updated.map { it.id })
        assertNull(result.newSelected)
    }

    private fun heart(
        id: String,
        nome: String = id,
        placeholder: Boolean = false
    ) = CrystalHeart(id = id, nome = nome, estagio = "Novato", placeholder = placeholder)
}
