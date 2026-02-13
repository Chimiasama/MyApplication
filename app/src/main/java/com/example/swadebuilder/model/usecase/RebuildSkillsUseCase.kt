package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Pericia

/**
 * Recalculates skill costs and stacks based on attributes and points.
 * Mirrors the logic of `rebuildAllPericiaStacks` in `CriadorState`.
 */
class RebuildSkillsUseCase {

    data class Input(
        val pericias: List<Pericia>,
        val periciaStartRawProvider: (Pericia) -> Int, // Callback to get racial base
        val periciaCapRawProvider: (Pericia) -> Int,
        val periciaMinRawProvider: (Pericia) -> Int,
        val atributoBaseParaPericiaProvider: (Pericia) -> String,
        val atributoRawForCostProvider: (String) -> Int, // Should resolve if supers active or not
        val totalSpPool: Int,
        val currentBaseIncs: Map<String, Int>,
        val currentCompIncs: Map<String, Int>,
        val currentSpStacks: Map<String, List<Int>>,
        val enforcePoolLimit: Boolean,
        val isPericiaBasicaEfetiva: (Pericia) -> Boolean // Needed for minRaw calculation if not passed
    )

    data class Output(
        val spCostStacks: Map<String, List<Int>>,
        val baseIncs: Map<String, Int>,
        val feedbackMessages: List<String>
    )

    fun execute(input: Input): Output {
        val newSpStacks = mutableMapOf<String, MutableList<Int>>()
        val newBaseIncs = mutableMapOf<String, Int>()
        val feedbackMessages = mutableListOf<String>()

        var cumulativeCost = 0
        val pool = input.totalSpPool

        // Initialize maps for all skills
        input.pericias.forEach { per ->
            newSpStacks[per.nome] = mutableListOf()
            newBaseIncs[per.nome] = 0
        }

        input.pericias.forEach { per ->
            // Reconstruct logic from CriadorState.rawTotal (without using state directly)
            // But here we need to know the *desired* raw total.
            // In CriadorState, it calculates desired from current stacks before clearing them.
            // "val desiredRaw = rawTotal(per)" -> which uses current stack size.

            // So we need to calculate desiredRaw from input.currentBaseIncs + input.currentCompIncs
            val startRaw = input.periciaStartRawProvider(per)
            val currentBase = input.currentBaseIncs[per.nome] ?: 0
            val currentComp = input.currentCompIncs[per.nome] ?: 0
            val totalIncs = currentBase + currentComp

            val desiredRaw = if (startRaw == 0 && totalIncs == 0) 0 else {
                 val (startForSteps, steps) = if (startRaw == 0) {
                    4 to (totalIncs - 1).coerceAtLeast(0)
                } else {
                    startRaw to totalIncs.coerceAtLeast(0)
                }
                // Helper to apply steps (copied from CriadorState.applySuperStepsFrom logic essentially)
                var raw = startForSteps
                var rem = steps
                // Logic: d4->d6 (1 step), d6->d8 (1), d8->d10 (1), d10->d12 (1), d12->d12+1 (1), d12+1->d12+2 (1)
                // Wait, CriadorState logic:
                // raw += if (raw < 12) 2 else 1
                repeat(rem) {
                     raw += if (raw < 12) 2 else 1
                }
                raw
            }

            val cap = input.periciaCapRawProvider(per)
            val minRaw = input.periciaMinRawProvider(per)

            var target = desiredRaw.coerceIn(minRaw, cap)

            // Function to calculate cost for a specific target
            fun costFor(tgt: Int): Int {
                var curr = startRaw
                var freeSteps = input.currentCompIncs[per.nome] ?: 0
                var sum = 0
                while (curr < tgt) {
                    val next = if (curr == 0) 4 else curr + 2
                    val attrKey = input.atributoBaseParaPericiaProvider(per)
                    val attrRawForCost = input.atributoRawForCostProvider(attrKey)

                    // Standard SWADE: < Attribute = 1, >= Attribute = 1 (until max). > Attribute = 2
                    // Logic in CriadorState: if (next <= attrRawForCost) 1 else 2
                    val stepCost = if (next <= attrRawForCost) 1 else 2

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
                feedbackMessages.add("Perícia ${per.nome} reduzida para d$target para compensar pontos.")
            }

            while (input.enforcePoolLimit && cumulativeCost + cost > pool && target > minRaw) {
                // Reduce target by 1 step (dX -> d(X-2) or d4 -> 0)
                // Note: Logic in CriadorState was "target = (target - 2).coerceAtLeast(minRaw)"
                // But for d12+1 -> d12 it's -1. And 4 -> 0 is -4.
                // CriadorState simple approach: target - 2.
                // If target is 4 (d4), target-2 = 2. 2 is invalid if min is 0 or 4.
                // If minRaw is 0, 4->2 is weird.
                // But let's stick to strict reproduction of CriadorState logic for now:
                target = (target - 2).coerceAtLeast(minRaw)
                cost = costFor(target)
            }

            // Rebuild the stack for the final target
            val stack = newSpStacks[per.nome]!! // already init
            newBaseIncs[per.nome] = 0 // Reset

            var currRaw = startRaw
            var freeSteps = input.currentCompIncs[per.nome] ?: 0

            while (currRaw < target) {
                val next = if (currRaw == 0) 4 else currRaw + 2
                val attrKey = input.atributoBaseParaPericiaProvider(per)
                val attrRawForCost = input.atributoRawForCostProvider(attrKey)

                val stepCost = if (next <= attrRawForCost) 1 else 2

                if (freeSteps > 0) {
                    freeSteps -= 1
                } else {
                    stack.add(stepCost)
                    newBaseIncs[per.nome] = (newBaseIncs[per.nome] ?: 0) + 1
                }
                currRaw = next
            }

            cumulativeCost += cost
        }

        return Output(
            spCostStacks = newSpStacks,
            baseIncs = newBaseIncs,
            feedbackMessages = feedbackMessages
        )
    }
}
