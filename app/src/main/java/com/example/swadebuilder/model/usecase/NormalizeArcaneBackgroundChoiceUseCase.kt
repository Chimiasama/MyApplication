package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ids.ArcaneBackgroundChoices
import com.example.swadebuilder.model.ids.ArcaneBackgroundIds

class NormalizeArcaneBackgroundChoiceUseCase {

    fun execute(choice: String?): String? {
        return when (choice?.trim()?.uppercase()) {
            ArcaneBackgroundChoices.DOM -> ArcaneBackgroundIds.DOM
            ArcaneBackgroundChoices.MAGIA -> ArcaneBackgroundIds.MAGIA
            ArcaneBackgroundChoices.MILAGRES -> ArcaneBackgroundIds.MILAGRES
            ArcaneBackgroundChoices.PSIONICOS,
            "PSIÔNICOS" -> ArcaneBackgroundIds.PSIONICOS
            ArcaneBackgroundChoices.CIENCIA_ESTRANHA,
            "CIÊNCIA ESTRANHA" -> ArcaneBackgroundIds.CIENCIA_ESTRANHA
            else -> null
        }
    }
}
