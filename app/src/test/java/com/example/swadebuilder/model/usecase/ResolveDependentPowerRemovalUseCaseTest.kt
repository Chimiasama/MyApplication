package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResolveDependentPowerRemovalUseCaseTest {

    private val useCase = ResolveDependentPowerRemovalUseCase()

    @Test
    fun `retorna superfeiticaria quando ocultismo cai abaixo de d10`() {
        val out = useCase.execute(
            ResolveDependentPowerRemovalUseCase.Input(
                skillKey = "OCULTISMO",
                skillRawAfterRevert = 8
            )
        )

        requireNotNull(out)
        assertEquals("SUPERFEITICARIA", out.dependentDisplayNameKey)
    }

    @Test
    fun `retorna superciencia quando ciencia cai abaixo de d10`() {
        val out = useCase.execute(
            ResolveDependentPowerRemovalUseCase.Input(
                skillKey = "CIENCIA",
                skillRawAfterRevert = 9
            )
        )

        requireNotNull(out)
        assertEquals("SUPERCIENCIA", out.dependentDisplayNameKey)
    }

    @Test
    fun `retorna nulo quando pericia nao possui dependente`() {
        val out = useCase.execute(
            ResolveDependentPowerRemovalUseCase.Input(
                skillKey = "ATLETISMO",
                skillRawAfterRevert = 8
            )
        )

        assertNull(out)
    }

    @Test
    fun `retorna nulo quando raw permanece em d10 ou mais`() {
        val out = useCase.execute(
            ResolveDependentPowerRemovalUseCase.Input(
                skillKey = "OCULTISMO",
                skillRawAfterRevert = 10
            )
        )

        assertNull(out)
    }
}
