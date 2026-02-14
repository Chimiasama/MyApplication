package com.example.swadebuilder.model.usecase

class CalculateSuperSkillRawAfterRevertUseCase {
    data class Input(
        val baseRaw: Int,
        val currentSuperSteps: Int,
        val revertingSteps: Int,
        val applySteps: (Int, Int) -> Int
    )

    fun execute(input: Input): Int {
        val stepsAfterRevert = (input.currentSuperSteps - input.revertingSteps).coerceAtLeast(0)
        return input.applySteps(input.baseRaw, stepsAfterRevert)
    }
}
