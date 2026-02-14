package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryComplicationsSnapshotUseCaseTest {

    private val useCase = ResolveAncestryComplicationsSnapshotUseCase()

    @Test
    fun `normalizes nullable selected complications before resolving`() {
        val medo = Complicacao(id = "medo", name = "Medo", severity = "menor", description = "desc", origem = "BASICO")
        val arrogante = Complicacao(id = "arrogante", name = "Arrogante", severity = "menor", description = "desc", origem = "BASICO")

        val result = useCase.execute(
            ResolveAncestryComplicationsSnapshotUseCase.Params(
                previousAutomaticDisadvantages = emptyList(),
                currentAutomaticDisadvantages = emptyList(),
                availableComplications = listOf(medo, arrogante),
                selectedComplications = mapOf(
                    medo to "Menor",
                    arrogante to null
                ),
                originPriorityResolver = { 0 }
            )
        )

        assertEquals(1, result.selectedComplications.size)
        assertTrue(result.selectedComplications.containsKey(medo))
    }
}
