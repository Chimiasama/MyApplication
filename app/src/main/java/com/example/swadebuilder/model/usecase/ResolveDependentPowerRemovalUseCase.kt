package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ids.PowerIds
import com.example.swadebuilder.model.ids.SkillIds
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
            SkillIds.OCULTISMO -> Output(
                dependentDisplayNameKey = PowerIds.SUPER_FEITICARIA,
                feedbackMessage = "Superfeitiçaria removida por falta de requisito (Ocultismo < d10)."
            )

            SkillIds.CIENCIA -> Output(
                dependentDisplayNameKey = PowerIds.SUPER_CIENCIA,
                feedbackMessage = "Superciência removida por falta de requisito (Ciência < d10)."
            )

            else -> null
        }
    }
}
