package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ids.ArcaneBackgroundIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NormalizeArcaneBackgroundChoiceUseCaseTest {

    private val useCase = NormalizeArcaneBackgroundChoiceUseCase()

    @Test
    fun `normaliza escolhas de antecedente arcano para ids canônicos`() {
        assertEquals(ArcaneBackgroundIds.DOM, useCase.execute("dom"))
        assertEquals(ArcaneBackgroundIds.MAGIA, useCase.execute(" MAGIA "))
        assertEquals(ArcaneBackgroundIds.MILAGRES, useCase.execute("milagres"))
        assertEquals(ArcaneBackgroundIds.PSIONICOS, useCase.execute("PSIÔNICOS"))
        assertEquals(ArcaneBackgroundIds.CIENCIA_ESTRANHA, useCase.execute("ciência estranha"))
    }

    @Test
    fun `retorna nulo para escolhas desconhecidas`() {
        assertNull(useCase.execute("tecnomancia"))
        assertNull(useCase.execute(null))
    }
}
