package com.example.swadebuilder

import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RuleConstants
import com.example.swadebuilder.util.keyify
import kotlin.math.max

/**
 * Representa um retrato calculado das regras de investimento em uma perícia, sem efeitos colaterais na UI.
 */
data class PericiaRuleSnapshot(
    val attrKey: String,
    val currentRaw: Int,
    val displayRaw: Int,
    val nextRaw: Int,
    val cost: Int,
    val capRaw: Int,
    val minimoTotal: Int,
    val canIncrease: Boolean,
    val canDecrease: Boolean
)

fun CriadorState.calcularPericiaRules(
    pericia: Pericia,
    idosoActive: Boolean,
    locked: Boolean
): PericiaRuleSnapshot {
    val currentRaw = rawTotal(pericia)
    val attrKey = atributoBaseParaPericia(pericia)
    val atrRaw = valoresAtributos[attrKey]?.intValue ?: 0
    val capRaw = periciaCapRaw(pericia)

    val displayRaw = rawTotalComSupers(pericia)
    val nextRaw = when {
        currentRaw == 0 && pericia.basica -> 4
        currentRaw < 12 -> currentRaw + 2
        else -> currentRaw + 1
    }
    val costNormal = if (modoLivre) 0 else (if (nextRaw <= atrRaw) 1 else 2)

    val compStack = compCostStackPorPericia[pericia] ?: emptyList()
    val spStack = spCostStackPorPericia[pericia] ?: emptyList()

    val minimoBasico: Int = minPericiaPorVantagem[pericia] ?: 0
    val opcionalList: List<Int> = vantagensSelecionadas.flatMap { vant ->
        vant.requisitos.periciaMinOpcional.entries
            .filter { it.key.equals(pericia.nome, ignoreCase = true) }
            .map { it.value }
    }
    val minimoOpcional: Int = opcionalList.maxOrNull() ?: 0
    val linguistaMin = linguistaMinRawFor(pericia)
    val minimoTotal = max(max(minimoBasico, minimoOpcional), linguistaMin)

    val canDecrease = when {
        modoLivre -> currentRaw > 0
        modoProgressaoAtivo -> {
            val frozenIncs = frozenSkillIncrements[pericia.nome] ?: 0
            (baseIncsPorPericia[pericia] ?: 0) > frozenIncs
        }
        else -> {
            !locked &&
                (compStack.isNotEmpty() || spStack.any { it > 0 }) &&
                (currentRaw - 2 >= minimoTotal)
        }
    }

    val astuciaSpent = spCostStackPorPericia
        .filterKeys { p -> p.atributo == RuleConstants.ATRIBUTO_ASTUCIA }
        .values
        .sumOf { costs -> costs.sum() }

    val canIncrease = if (modoLivre) {
        nextRaw <= 20
    } else {
        !locked &&
            pontosPericia >= costNormal &&
            nextRaw <= capRaw &&
            (if (idosoActive && astuciaSpent < 5) {
                pericia.atributo == RuleConstants.ATRIBUTO_ASTUCIA
            } else {
                true
            })
    }

    return PericiaRuleSnapshot(
        attrKey = attrKey,
        currentRaw = currentRaw,
        displayRaw = displayRaw,
        nextRaw = nextRaw,
        cost = costNormal,
        capRaw = capRaw,
        minimoTotal = minimoTotal,
        canIncrease = canIncrease,
        canDecrease = canDecrease
    )
}

fun CriadorState.atributoBaseParaPericia(per: Pericia): String {
    val isAtletismo = per.nome.keyify() == "ATLETISMO"
    val hasBrutamontes = vantagensSelecionadas.any { it.id == Constants.ID_BRUTAMONTES }

    return if (isAtletismo && hasBrutamontes) {
        "FORCA"
    } else {
        per.atributo
    }
}
