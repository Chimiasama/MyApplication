package com.example.swadebuilder.model.usecase

class ResolveDependentPowerRemovalUseCase {

    data class Input(
        val skillKey: String,
        val skillRawAfterRevert: Int
    )

    data class Output(
        val dependentDisplayNameKey: String,
        val feedbackMessage: String
    )

    fun execute(input: Input): Output? {
        if (input.skillRawAfterRevert >= 10) return null

        return when (input.skillKey) {
            "OCULTISMO" -> Output(
                dependentDisplayNameKey = "SUPERFEITICARIA",
                feedbackMessage = "Superfeitiçaria removida por falta de requisito (Ocultismo < d10)."
            )

            "CIENCIA" -> Output(
                dependentDisplayNameKey = "SUPERCIENCIA",
                feedbackMessage = "Superciência removida por falta de requisito (Ciência < d10)."
            )

            else -> null
        }
    }
}
