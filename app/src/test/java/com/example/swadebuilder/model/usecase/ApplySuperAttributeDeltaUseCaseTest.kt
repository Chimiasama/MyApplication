package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ApplySuperAttributeDeltaUseCaseTest {

    private val useCase = ApplySuperAttributeDeltaUseCase()

    @Test
    fun `increase aplica mais dois abaixo de d12`() {
        val out = useCase.execute(
            ApplySuperAttributeDeltaUseCase.Input(
                currentRaw = 8,
                steps = 1,
                direction = ApplySuperAttributeDeltaUseCase.Direction.INCREASE
            )
        )

        assertEquals(10, out)
    }

    @Test
    fun `increase cruza d12 e passa a aplicar mais um`() {
        val out = useCase.execute(
            ApplySuperAttributeDeltaUseCase.Input(
                currentRaw = 10,
                steps = 2,
                direction = ApplySuperAttributeDeltaUseCase.Direction.INCREASE
            )
        )

        assertEquals(13, out)
    }

    @Test
    fun `decrease aplica menos um acima de d12 e menos dois em d12 ou menos`() {
        val out = useCase.execute(
            ApplySuperAttributeDeltaUseCase.Input(
                currentRaw = 13,
                steps = 2,
                direction = ApplySuperAttributeDeltaUseCase.Direction.DECREASE
            )
        )

        assertEquals(10, out)
    }

    @Test
    fun `respeita limites minimo e maximo`() {
        val up = useCase.execute(
            ApplySuperAttributeDeltaUseCase.Input(
                currentRaw = 30,
                steps = 5,
                direction = ApplySuperAttributeDeltaUseCase.Direction.INCREASE
            )
        )
        val down = useCase.execute(
            ApplySuperAttributeDeltaUseCase.Input(
                currentRaw = 4,
                steps = 5,
                direction = ApplySuperAttributeDeltaUseCase.Direction.DECREASE
            )
        )

        assertEquals(30, up)
        assertEquals(4, down)
    }
}
