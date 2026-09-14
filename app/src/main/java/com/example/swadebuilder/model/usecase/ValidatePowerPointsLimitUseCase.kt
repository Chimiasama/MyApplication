package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

class ValidatePowerPointsLimitUseCase {

    data class Input(
        val vantagem: Vantagem,
        val ppPurchasesThisRank: Int, // total cumulative PP purchases so far (all stages), NOT just the current stage — must match the cumulative cap in maxPpPurchasesAllowed
        val maxPpPurchasesAllowed: Int,
        val currentSelectionCount: Int // For general maxSelections
    )

    fun execute(input: Input): Boolean {
        // 2) Pontos de Poder por estágio
        if (input.vantagem.nome.contains("Pontos de Poder", ignoreCase = true)) {
            if (input.ppPurchasesThisRank >= input.maxPpPurchasesAllowed) return false
        }
        // 7) Limite de Compra (maxSelections)
        else if (input.vantagem.limiteCompra != "infinito" && input.vantagem.maxSelections > 0) {
            if (input.currentSelectionCount >= input.vantagem.maxSelections) return false
        }
        return true
    }
}
