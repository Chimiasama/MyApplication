package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

class ValidatePowerPointsLimitUseCase {

    data class Input(
        val vantagem: Vantagem,
        // Compras feitas NO ESTÁGIO ATUAL apenas — SEM acumular Estágios pulados ("pega
        // agora ou já era"; a única exceção do livro pra guardar Progressos é Complicação).
        // maxPpPurchasesAllowed é 1 em todo Estágio, exceto Lendário, que é ilimitado (mas
        // só vale 2 PP em vez de 5 a partir da 2ª compra lá — ver
        // CriadorState.maxComprasPpNesteEstagio()/selecionarPontosDePoder()).
        val ppPurchasesThisRank: Int,
        val maxPpPurchasesAllowed: Int,
        val currentSelectionCount: Int, // For general maxSelections
        // "uma vez por Estágio", SEM compensar Estágios pulados — pra qualquer Vantagem com
        // limite_compra == "uma_vez_por_estagio" que não seja Pontos de Poder (Pontos de Chi,
        // Presa, Poder do Sangue, Vontade Sombria, e qualquer futura Vantagem marcada assim).
        // Sem a exceção de teto ilimitado no Lendário que só Pontos de Poder tem — o teto
        // genérico é sempre 1, olhando só o Estágio atual.
        val estagioPurchasesGenerico: Int = 0,
        val maxEstagioPurchasesGenericoAllowed: Int = 0
    )

    fun execute(input: Input): Boolean {
        val v = input.vantagem
        return when {
            // 2) Pontos de Poder por estágio (com teto ilimitado no Lendário — ver
            // CriadorState.maxComprasPpNesteEstagio()).
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
