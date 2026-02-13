package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.GameDataStore

/**
 * Recalculates skill costs and stacks based on attributes and points.
 */
class RebuildSkillsUseCase {

    data class Input(
        val pericias: List<Pericia>,
        val atributosRaw: Map<String, Int>,
        val periciaStartRaw: (Pericia) -> Int, // Callback to get racial base
        val periciaCapRaw: (Pericia) -> Int,
        val totalSpPool: Int,
        val periciasDesiredRaw: Map<String, Int>,
        val periciasFreeSteps: Map<String, Int>,
        val atributoBaseParaPericia: (Pericia) -> String
    )

    data class Output(
        val spCostStacks: Map<String, List<Int>>,
        val baseIncs: Map<String, Int>,
        val feedbackMessages: List<String>
    )

    fun execute(input: Input): Output {
        val newStacks = mutableMapOf<String, MutableList<Int>>()
        val newIncs = mutableMapOf<String, Int>()
        val messages = mutableListOf<String>()
        var cumulativeCost = 0

        input.pericias.forEach { per ->
            val desiredRaw = input.periciasDesiredRaw[per.nome] ?: 0
            val startRaw = input.periciaStartRaw(per)
            val cap = input.periciaCapRaw(per)

            // Re-simulation logic...
            // Ideally we copy the logic from rebuildAllPericiaStacks here.
        }

        return Output(newStacks, newIncs, messages)
    }
}
