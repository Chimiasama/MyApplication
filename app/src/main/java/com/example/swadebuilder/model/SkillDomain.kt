package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.util.toDiceString
import kotlin.math.max

object SkillDomain {

    fun calculateCost(state: CriadorState, per: Pericia): SkillCostResult {
        val currentRaw = state.rawTotal(per)
        val attrKey    = state.atributoBaseParaPericia(per)
        val atrRaw     = state.valoresAtributos[attrKey]?.intValue ?: 4
        val capRaw     = state.periciaCapRaw(per)

        val nextRaw = when {
            currentRaw == 0 && per.basica -> 4
            currentRaw < 12               -> currentRaw + 2
            else                          -> currentRaw + 1
        }
        val cost = if (nextRaw <= atrRaw) 1 else 2

        val displayRaw = state.rawTotalComSupers(per)
        val displayDice = when {
            displayRaw == 0 && per.basica -> "d4"
            displayRaw == 0 -> "-"
            else -> displayRaw.toDiceString()
        }

        return SkillCostResult(
            currentRaw = currentRaw,
            nextRaw = nextRaw,
            cost = cost,
            capRaw = capRaw,
            attrRaw = atrRaw,
            displayDice = displayDice
        )
    }

    fun canIncrease(state: CriadorState, per: Pericia, costResult: SkillCostResult): Boolean {
        if (state.criacaoBasicaCongelada && !state.skillAdvancementInProgress) return false

        val idosoBonus = state.idosoBonusSp
        val astuciaSpent = state.spCostStackPorPericia
            .filterKeys { p -> p.atributo == Constants.ATTR_SMARTS }
            .values
            .sumOf { costs -> costs.sum() }

        // Logic:
        // 1. Must have enough points
        // 2. Must not exceed cap
        // 3. If Elderly (Idoso) active, first 5 points MUST be in Smarts-linked skills
        val pointsAvailable = state.pontosPericia >= costResult.cost
        val notCapped = costResult.nextRaw <= costResult.capRaw
        val elderlyCheck = if (idosoBonus > 0 && astuciaSpent < 5) {
            per.atributo == Constants.ATTR_SMARTS
        } else {
            true
        }

        return pointsAvailable && notCapped && elderlyCheck
    }

    fun canDecrease(state: CriadorState, per: Pericia): Boolean {
        if (state.modoProgressaoAtivo) {
            val frozenIncs = state.frozenSkillIncrements[per.nome] ?: 0
            return state.baseIncsPorPericia.getValue(per) > frozenIncs
        }

        if (state.criacaoBasicaCongelada) return false

        val currentRaw = state.rawTotal(per)
        val compStack = state.compCostStackPorPericia.getValue(per)
        val spStack   = state.spCostStackPorPericia.getValue(per)

        val minimoBasico: Int = state.minPericiaPorVantagem[per] ?: 0

        val opcionalList: List<Int> = state.vantagensSelecionadas.flatMap { vant ->
            val mapaOpc = vant.requisitos.periciaMinOpcional ?: emptyMap()
            mapaOpc.entries
                .filter { it.key.equals(per.nome, ignoreCase = true) }
                .map { it.value }
        }
        val minimoOpcional: Int = opcionalList.maxOrNull() ?: 0
        val minimoTotal = max(minimoBasico, minimoOpcional)

        val hasInvested = compStack.isNotEmpty() || spStack.any { it > 0 }
        val aboveMin = currentRaw - 2 >= minimoTotal

        return hasInvested && aboveMin
    }
}

data class SkillCostResult(
    val currentRaw: Int,
    val nextRaw: Int,
    val cost: Int,
    val capRaw: Int,
    val attrRaw: Int,
    val displayDice: String
)
