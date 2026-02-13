package com.example.swadebuilder.model.usecase

class CalculateCurrentSuperSkillStepsUseCase {

    data class Investment(
        val skillKey: String,
        val steps: Int
    )

    data class Input(
        val targetSkillName: String,
        val investments: List<Investment>
    )

    fun execute(input: Input): Int {
        return input.investments
            .filter { it.skillKey.equals(input.targetSkillName, ignoreCase = true) }
            .sumOf { it.steps }
    }
}
