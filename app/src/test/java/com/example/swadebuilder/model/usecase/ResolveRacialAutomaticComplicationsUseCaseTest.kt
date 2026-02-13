package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveRacialAutomaticComplicationsUseCaseTest {

    private val useCase = ResolveRacialAutomaticComplicationsUseCase()

    @Test
    fun `removes old automatic complications and adds new ones with severity`() {
        val oldComp = complicacao(id = "small", origem = "BASICO")
        val selectedManual = complicacao(id = "manual_keep", origem = "BASICO")
        val newComp = complicacao(id = "new_auto", origem = "BASICO")

        val result = useCase.execute(
            ResolveRacialAutomaticComplicationsUseCase.Params(
                previousAutomaticDisadvantages = listOf("Small (Menor)"),
                currentAutomaticDisadvantages = listOf("New Auto (Maior)"),
                availableComplications = listOf(newComp),
                selectedComplications = linkedMapOf(oldComp to "Menor", selectedManual to "Maior"),
                originPriorityResolver = { 0 }
            )
        )

        assertTrue(result.selectedComplications.keys.none { it.id == "small" })
        assertEquals("Maior", result.selectedComplications[newComp])
        assertEquals("Maior", result.selectedComplications[selectedManual])
    }

    @Test
    fun `selects highest priority variant when same complication id exists in multiple origins`() {
        val lowPriority = complicacao(id = "fear", origem = "BASICO", name = "Fear Basic")
        val highPriority = complicacao(id = "fear", origem = "HORROR", name = "Fear Horror")

        val result = useCase.execute(
            ResolveRacialAutomaticComplicationsUseCase.Params(
                previousAutomaticDisadvantages = emptyList(),
                currentAutomaticDisadvantages = listOf("Fear (Menor)"),
                availableComplications = listOf(lowPriority, highPriority),
                selectedComplications = emptyMap(),
                originPriorityResolver = { origin -> if (origin == "HORROR") 1000 else 0 }
            )
        )

        val selected = result.selectedComplications.keys.single()
        assertEquals("Fear Horror", selected.name)
        assertEquals("Menor", result.selectedComplications[selected])
    }

    @Test
    fun `defaults severity to Menor when not specified`() {
        val comp = complicacao(id = "stigma", origem = "BASICO")

        val result = useCase.execute(
            ResolveRacialAutomaticComplicationsUseCase.Params(
                previousAutomaticDisadvantages = emptyList(),
                currentAutomaticDisadvantages = listOf("Stigma"),
                availableComplications = listOf(comp),
                selectedComplications = emptyMap(),
                originPriorityResolver = { 0 }
            )
        )

        assertEquals("Menor", result.selectedComplications[comp])
    }

    private fun complicacao(id: String, origem: String, name: String = id): Complicacao = Complicacao(
        id = id,
        name = name,
        severity = "Menor",
        description = "d",
        origem = origem
    )
}
