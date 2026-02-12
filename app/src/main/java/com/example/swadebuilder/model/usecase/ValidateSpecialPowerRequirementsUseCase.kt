package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.toDiceString

class ValidateSpecialPowerRequirementsUseCase {

    data class Input(
        val effectNameKey: String,
        val ocultismoRaw: Int?,
        val cienciaRaw: Int?
    )

    fun execute(input: Input): String? {
        return when (input.effectNameKey) {
            "SUPERFEITICARIA" -> {
                val raw = input.ocultismoRaw ?: return null
                if (raw < 10) "Requer Ocultismo d10+ (atual: ${raw.toDiceString()})." else null
            }

            "SUPERCIENCIA" -> {
                val raw = input.cienciaRaw ?: return null
                if (raw < 10) "Requer Ciência d10+ (atual: ${raw.toDiceString()})." else null
            }

            else -> null
        }
    }
}
