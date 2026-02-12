package com.example.swadebuilder.model.usecase

class CalculatePerPowerLimitUseCase {
    data class Input(
        val favoritePowerId: String?,
        val targetPowerId: String,
        val favoriteLimit: Int,
        val defaultLimit: Int
    )

    fun execute(input: Input): Int {
        return if (input.favoritePowerId != null && input.favoritePowerId == input.targetPowerId) {
            input.favoriteLimit
        } else {
            input.defaultLimit
        }
    }
}
