package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

class ValidatePowerPointsLimitUseCase {

    data class Input(
        val vantagem: Vantagem,
        val ppPurchasesThisRank: Int, // total cumulative PP purchases so far (all stages), NOT just the current stage — must match the cumulative cap in maxPpPurchasesAllowed
        val maxPpPurchasesAllowed: Int,
        val currentSelectionCount: Int, // For general maxSelections
        // "uma vez por Estágio, com direito a compensar Estágios pulados" GENÉRICO — pra
        // qualquer Vantagem com limite_compra == "uma_vez_por_estagio" que não seja Pontos de
        // Poder (Pontos de Chi, Presa, Poder do Sangue, Vontade Sombria, e qualquer futura
        // Vantagem marcada assim). Sem a exceção de teto ilimitado no Lendário que só Pontos
        // de Poder tem.
        val estagioPurchasesGenerico: Int = 0,
        val maxEstagioPurchasesGenericoAllowed: Int = 0
    )

    fun execute(input: Input): Boolean {
        val v = input.vantagem
        return when {
            // 2) Pontos de Poder por estágio (com teto ilimitado no Lendário — ver
            // CriadorState.maxComprasPpAteAgora()).
            v.nome.contains("Pontos de Poder", ignoreCase = true) ->
                input.ppPurchasesThisRank < input.maxPpPurchasesAllowed

            // 2b) Demais Vantagens repetíveis "uma vez por Estágio" (sem exceção no Lendário).
            v.limiteCompra == "uma_vez_por_estagio" ->
                input.estagioPurchasesGenerico < input.maxEstagioPurchasesGenericoAllowed

            // 7) Limite de Compra (maxSelections)
            v.limiteCompra != "infinito" && v.maxSelections > 0 ->
                input.currentSelectionCount < v.maxSelections

            else -> true
        }
    }
}
