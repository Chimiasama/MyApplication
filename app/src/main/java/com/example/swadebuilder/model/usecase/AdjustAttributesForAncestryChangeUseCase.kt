package com.example.swadebuilder.model.usecase

class AdjustAttributesForAncestryChangeUseCase {

    data class Params(
        val attributeNames: List<String>,
        val attributeCaps: Map<String, AttributeCap>,
        val paCostStacks: Map<String, List<Int>>
    )

    data class AttributeCap(
        val minRaw: Int,
        val maxRaw: Int
    )

    data class AttributeAdjustment(
        val newRaw: Int,
        val adjustedStack: List<Int>,
        val refundedPoints: Int
    )

    data class Result(
        val adjustmentsByAttribute: Map<String, AttributeAdjustment>
    )

    fun execute(params: Params): Result {
        val out = linkedMapOf<String, AttributeAdjustment>()

        params.attributeNames.forEach { name ->
            val cap = params.attributeCaps.getValue(name)
            val originalStack = params.paCostStacks[name].orEmpty()

            var raw = cap.minRaw.coerceAtMost(cap.maxRaw)
            var appliedSteps = 0

            repeat(originalStack.size) {
                val candidate = if (raw < 12) raw + 2 else raw + 1
                if (candidate > cap.maxRaw) {
                    return@repeat
                }
                raw = candidate
                appliedSteps++
            }

            val adjustedStack = originalStack.take(appliedSteps)
            val refunded = originalStack.size - appliedSteps

            out[name] = AttributeAdjustment(
                newRaw = raw,
                adjustedStack = adjustedStack,
                refundedPoints = refunded
            )
        }

        return Result(adjustmentsByAttribute = out)
    }
}
