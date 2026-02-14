package com.example.swadebuilder.model.usecase

class ValidateSuperAttributeInvestmentUseCase(
    private val technicalCap: Int = 30
) {
    data class Input(
        val currentRaw: Int,
        val steps: Int,
        val applySteps: (Int, Int) -> Int
    )

    fun execute(input: Input): String? {
        if (input.steps == 0) return null

        val targetRaw = input.applySteps(input.currentRaw, input.steps)
        if (targetRaw > technicalCap) {
            return "Limite técnico de atributo excedido ($technicalCap)."
        }

        return null
    }
}
