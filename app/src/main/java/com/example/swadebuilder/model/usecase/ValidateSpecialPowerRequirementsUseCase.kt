package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.model.ids.PowerIds

class ValidateSpecialPowerRequirementsUseCase {

    data class Input(
        val effectNameKey: String,
        val ocultismoRaw: Int?,
        val cienciaRaw: Int?
    )

    fun execute(input: Input): String? {
        return when (input.effectNameKey) {
            PowerIds.SUPER_FEITICARIA -> {
                val raw = input.ocultismoRaw ?: return null
                if (raw < 10) "Requer Ocultismo d10+ (atual: ${raw.toDiceString()})." else null
            }

            PowerIds.SUPER_CIENCIA -> {
                val raw = input.cienciaRaw ?: return null
                if (raw < 10) "Requer Ciência d10+ (atual: ${raw.toDiceString()})." else null
            }

            else -> null
        }
    }
}
