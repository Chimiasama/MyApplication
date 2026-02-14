package com.example.swadebuilder.model.usecase

class ApplySuperAttributeDeltaUseCase(
    private val minRaw: Int = 4,
    private val thresholdRaw: Int = 12,
    private val maxRaw: Int = 30
) {
    enum class Direction { INCREASE, DECREASE }

    data class Input(
        val currentRaw: Int,
        val steps: Int,
        val direction: Direction
    )

    fun execute(input: Input): Int {
        var value = input.currentRaw
        repeat(input.steps.coerceAtLeast(0)) {
            value = when (input.direction) {
                Direction.INCREASE -> {
                    if (value < thresholdRaw) (value + 2).coerceAtMost(maxRaw)
                    else (value + 1).coerceAtMost(maxRaw)
                }
                Direction.DECREASE -> {
                    if (value > thresholdRaw) (value - 1).coerceAtLeast(minRaw)
                    else (value - 2).coerceAtLeast(minRaw)
                }
            }
        }
        return value
    }
}
