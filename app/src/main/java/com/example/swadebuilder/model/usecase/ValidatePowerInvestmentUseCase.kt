package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ids.PowerIds
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

        if (input.poderId == PowerIds.ARMOR || input.poderId == PowerIds.RESISTANCE) {
            val gastosArmor = input.gastosPorPoder[PowerIds.ARMOR] ?: 0
            val gastosRes = input.gastosPorPoder[PowerIds.RESISTANCE] ?: 0
            val shareDepois = gastosArmor + gastosRes + input.custo

            if (shareDepois > input.limiteCompartilhadoArmaduraResistencia) {
                val excedeu = shareDepois - input.limiteCompartilhadoArmaduraResistencia
                return "Limite compartilhado de Armadura+Resistência excedido em $excedeu (gasto previsto: $shareDepois / limite ${input.limiteCompartilhadoArmaduraResistencia})."
            }
        }

        return null
    }
}
