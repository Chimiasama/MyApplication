package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class RebuildSkillStacksUseCaseTest {

    private val useCase = RebuildSkillStacksUseCase()

    @Test
    fun `executa a acao de rebuild exatamente uma vez`() {
        var calls = 0

        useCase.execute { calls++ }

        assertEquals(1, calls)
    }
}
