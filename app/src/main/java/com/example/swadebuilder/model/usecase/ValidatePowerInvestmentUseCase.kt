package com.example.swadebuilder.model.usecase

class ValidatePowerInvestmentUseCase {

    data class Input(
        val poderId: String,
        val custo: Int,
        val superPontosDisponiveis: Int,
        val gastosPorPoder: Map<String, Int>,
        val limitePorPoder: Int,
        val limiteCompartilhadoArmaduraResistencia: Int
    )

    fun execute(input: Input): String? {
        if (input.custo <= 0) return "Custo inválido."

        if (input.superPontosDisponiveis < input.custo) {
            return "Sem saldo: precisa de ${input.custo}, tem ${input.superPontosDisponiveis}."
        }

        val jaGastoNestePoder = input.gastosPorPoder[input.poderId] ?: 0
        if (jaGastoNestePoder + input.custo > input.limitePorPoder) {
            val falta = (jaGastoNestePoder + input.custo) - input.limitePorPoder
            return "Limite de gasto neste poder excedido em $falta (limite: ${input.limitePorPoder})."
        }

        if (input.poderId == "sp_armor" || input.poderId == "sp_res") {
            val gastosArmor = input.gastosPorPoder["sp_armor"] ?: 0
            val gastosRes = input.gastosPorPoder["sp_res"] ?: 0
            val shareDepois = gastosArmor + gastosRes + input.custo

            if (shareDepois > input.limiteCompartilhadoArmaduraResistencia) {
                val excedeu = shareDepois - input.limiteCompartilhadoArmaduraResistencia
                return "Limite compartilhado de Armadura+Resistência excedido em $excedeu (gasto previsto: $shareDepois / limite ${input.limiteCompartilhadoArmaduraResistencia})."
            }
        }

        return null
    }
}
