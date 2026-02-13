package com.example.swadebuilder.model.usecase

class AdjustNonNegativeBonusUseCase {
    data class Input(
        val currentValue: Int,
        val delta: Int
    )

    fun execute(input: Input): Int {
        return (input.currentValue + input.delta).coerceAtLeast(0)
    }
}
