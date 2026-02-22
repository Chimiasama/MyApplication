package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveAncestryVariantUseCaseTest {

    private val useCase = ResolveAncestryVariantUseCase()

    @Test
    fun `defaults to first option when selection is null`() {
        val result = useCase.execute(
            ResolveAncestryVariantUseCase.Input(
                selectedVariant = null,
                availableOptions = listOf("Padrão", "Gazela")
            )
        )

        assertEquals("Padrão", result.normalizedSelection)
        assertEquals("Padrão", result.defaultOption)
    }

    @Test
    fun `maps basico alias to padrao when only padrao exists`() {
        val result = useCase.execute(
            ResolveAncestryVariantUseCase.Input(
                selectedVariant = "Básico",
                availableOptions = listOf("Padrão", "Dragão")
            )
        )

        assertEquals("Padrão", result.normalizedSelection)
    }

    @Test
    fun `ignores invalid free text and falls back to default option`() {
        val result = useCase.execute(
            ResolveAncestryVariantUseCase.Input(
                selectedVariant = "<script>alert(1)</script>",
                availableOptions = listOf("Básico", "Ave de rapina")
            )
        )

        assertEquals("Básico", result.normalizedSelection)
    }

    @Test
    fun `returns null when ancestry has no options`() {
        val result = useCase.execute(
            ResolveAncestryVariantUseCase.Input(
                selectedVariant = "Básico",
                availableOptions = emptyList()
            )
        )

        assertNull(result.normalizedSelection)
        assertNull(result.defaultOption)
    }
}
