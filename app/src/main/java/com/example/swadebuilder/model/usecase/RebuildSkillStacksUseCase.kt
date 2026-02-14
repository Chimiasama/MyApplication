package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Pericia

class RebuildSkillStacksUseCase {

    data class Input(
        val pericias: List<Pericia>,
        val totalSpPool: Int,
        val currentRawValues: Map<String, Int>,
        val startRawValues: Map<String, Int>,
        val capRawValues: Map<String, Int>,
        val minRawValues: Map<String, Int>,
        val freeStepsMap: Map<String, Int>,
        val effectiveAttributeValues: Map<String, Int>,
        val skillAttributeMap: Map<String, String>,
        val enforcePoolLimit: Boolean
    )

    data class Output(
        val spCostStacks: Map<String, List<Int>>,
        val baseIncs: Map<String, Int>,
        val feedbackMessages: List<String>
    )

    fun execute(input: Input): Output {
        val spCostStacks = mutableMapOf<String, MutableList<Int>>()
        val baseIncs = mutableMapOf<String, Int>()
        val feedbackMessages = mutableListOf<String>()

        var cumulativeCost = 0
        val pool = input.totalSpPool

        input.pericias.forEach { per ->
            val perName = per.nome

            // Initialize outputs for this skill
            spCostStacks[perName] = mutableListOf()
            baseIncs[perName] = 0

            val desiredRaw = input.currentRawValues[perName] ?: 0
            val cap = input.capRawValues[perName] ?: 12
            val minRaw = input.minRawValues[perName] ?: 0
            val startRaw = input.startRawValues[perName] ?: 0
            val initialFreeSteps = input.freeStepsMap[perName] ?: 0
            val attrKey = input.skillAttributeMap[perName] ?: per.atributo
            val attrValue = input.effectiveAttributeValues[attrKey] ?: 4

            var target = desiredRaw.coerceIn(minRaw, cap)

            // Helper function to calculate cost to reach a target
            fun costFor(tgt: Int): Int {
                var curr = startRaw
                var freeSteps = initialFreeSteps
                var sum = 0

                while (curr < tgt) {
                    val next = if (curr == 0) 4 else curr + 2

                    val stepCost = if (next <= attrValue) 1 else 2

                    if (freeSteps > 0) {
                        freeSteps -= 1
                    } else {
                        sum += stepCost
                    }
                    curr = next
                }
                return sum
            }

            var cost = costFor(target)

            if (input.enforcePoolLimit && cost > 0 && cumulativeCost + cost > pool) {
                feedbackMessages.add("Perícia $perName reduzida para d$target para compensar pontos.")
            }

            while (input.enforcePoolLimit && cumulativeCost + cost > pool) {
                // Reduce target by one step (usually 2, but handle d4 start carefully if needed)
                // Logic in original code: target = (target - 2).coerceAtLeast(minRaw)
                // If target is 4 and we reduce, it goes to 2 (which is invalid usually, so likely 0 or minRaw)
                // The original code does `(target - 2)`.
                target = (target - 2).coerceAtLeast(minRaw)
                cost = costFor(target)

                // Safety break if we can't reduce further (target == minRaw)
                if (target <= minRaw) break
            }

            // Now rebuild the stack for the final target
            val currentStack = spCostStacks[perName]!!

            // Re-run the loop to populate the stack
            var currRaw = startRaw
            var freeSteps = initialFreeSteps

            while (currRaw < target) {
                val next = if (currRaw == 0) 4 else currRaw + 2

                val stepCost = if (next <= attrValue) 1 else 2

                if (freeSteps > 0) {
                    freeSteps -= 1
                } else {
                    currentStack.add(stepCost)
                    baseIncs[perName] = (baseIncs[perName] ?: 0) + 1
                }
                currRaw = next
            }

            cumulativeCost += cost
        }

        return Output(
            spCostStacks = spCostStacks,
            baseIncs = baseIncs,
            feedbackMessages = feedbackMessages
        )
    }
}
