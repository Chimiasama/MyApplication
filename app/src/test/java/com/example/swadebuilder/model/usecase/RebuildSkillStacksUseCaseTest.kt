package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Pericia
import org.junit.Assert.assertEquals
import org.junit.Test

class RebuildSkillStacksUseCaseTest {

    private val useCase = RebuildSkillStacksUseCase()

    @Test
    fun `rebuilds skill stacks correctly based on costs and pool limits`() {
        // Setup:
        // Lutar (d4 base) -> Desired d8 (raw 8)
        // Agilidade (d6 base) (raw 6)
        // Cost: d4->d6 (1pt), d6->d8 (2pts). Total 3pts.
        // Pool: 5 pts available.

        val lutar = Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)
        val atirar = Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true)

        val input = RebuildSkillStacksUseCase.Input(
            pericias = listOf(lutar, atirar),
            totalSpPool = 5,
            currentRawValues = mapOf("Lutar" to 8, "Atirar" to 4), // Lutar d8, Atirar d4
            startRawValues = mapOf("Lutar" to 4, "Atirar" to 4), // Both start at d4
            capRawValues = mapOf("Lutar" to 12, "Atirar" to 12),
            minRawValues = mapOf("Lutar" to 4, "Atirar" to 4),
            freeStepsMap = mapOf("Lutar" to 0, "Atirar" to 0),
            effectiveAttributeValues = mapOf("AGILIDADE" to 6), // d6
            skillAttributeMap = mapOf("Lutar" to "AGILIDADE", "Atirar" to "AGILIDADE"),
            enforcePoolLimit = true
        )

        val result = useCase.execute(input)

        // Verify Lutar costs:
        // d4 -> d6 (1 pt)
        // d6 -> d8 (2 pts, exceeds d6 attribute)
        // Stack should be [1, 2]
        assertEquals(listOf(1, 2), result.spCostStacks["Lutar"])
        assertEquals(2, result.baseIncs["Lutar"]) // 2 increases

        // Verify Atirar costs:
        // d4 -> d4 (0 pts)
        // Stack should be empty
        assertEquals(emptyList<Int>(), result.spCostStacks["Atirar"])
        assertEquals(0, result.baseIncs["Atirar"])

        // Total cost: 1+2 = 3. Pool 5. Should succeed fully.
        // No messages expected
        assertEquals(0, result.feedbackMessages.size)
    }

    @Test
    fun `respects pool limit and reduces skill if cost exceeds pool`() {
        // Setup:
        // Lutar (d4 base) -> Desired d8 (raw 8)
        // Agilidade (d6 base) (raw 6)
        // Cost: d4->d6 (1pt), d6->d8 (2pts). Total 3pts.
        // Pool: 2 pts available. (Should stop at d6)

        val lutar = Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)

        val input = RebuildSkillStacksUseCase.Input(
            pericias = listOf(lutar),
            totalSpPool = 2, // Only 2 points available
            currentRawValues = mapOf("Lutar" to 8), // Desired d8
            startRawValues = mapOf("Lutar" to 4),
            capRawValues = mapOf("Lutar" to 12),
            minRawValues = mapOf("Lutar" to 4),
            freeStepsMap = mapOf("Lutar" to 0),
            effectiveAttributeValues = mapOf("AGILIDADE" to 6),
            skillAttributeMap = mapOf("Lutar" to "AGILIDADE"),
            enforcePoolLimit = true
        )

        val result = useCase.execute(input)

        // Verify Lutar costs:
        // d4 -> d6 (1 pt) -> 1/2 used. OK.
        // d6 -> d8 (2 pts) -> 3/2 used. Fail.
        // Stack should be [1] (stopped at d6)
        assertEquals(listOf(1), result.spCostStacks["Lutar"])
        assertEquals(1, result.baseIncs["Lutar"]) // 1 increase only

        // Should have a feedback message
        assertEquals(1, result.feedbackMessages.size)
        assertEquals("Perícia Lutar reduzida para d6 para compensar pontos.", result.feedbackMessages[0])
    }
}
