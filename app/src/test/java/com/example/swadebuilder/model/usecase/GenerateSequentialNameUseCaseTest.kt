package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerateSequentialNameUseCaseTest {

    private val useCase = GenerateSequentialNameUseCase(defaultName = "Nome")

    @Test
    fun `retorna nome base quando ainda nao existe`() {
        val out = useCase.execute(
            baseName = "Aventureiro",
            existingNames = listOf("Nome", "Outro"),
            usarParenteses = false
        )

        assertEquals("Aventureiro", out)
    }

    @Test
    fun `gera sufixo numerico sem parenteses`() {
        val out = useCase.execute(
            baseName = "Nome",
            existingNames = listOf("Nome", "Nome 2", "Nome 3"),
            usarParenteses = false
        )

        assertEquals("Nome 4", out)
    }

    @Test
    fun `gera sufixo com parenteses`() {
        val out = useCase.execute(
            baseName = "Heroi",
            existingNames = listOf("Heroi", "Heroi (2)"),
            usarParenteses = true
        )

        assertEquals("Heroi (3)", out)
    }

    @Test
    fun `quando base vazia usa default`() {
        val out = useCase.execute(
            baseName = "",
            existingNames = emptyList(),
            usarParenteses = false
        )

        assertEquals("Nome", out)
    }
}
