package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.util.keyify

class ResolveAncestryTransitionContextUseCase {

    data class Params(
        val previousAncestry: String,
        val targetAncestry: String,
        val previousAncestryDef: RacialModifier?,
        val targetAncestryDef: RacialModifier?,
        val currentAutomaticAdvantages: List<String>
    )

    data class Result(
        val wasHumano: Boolean,
        val willBeHumano: Boolean,
        val previousFreeAdvantageKeys: Set<String>
    )

    fun execute(params: Params): Result {
        val wasHumano = params.previousAncestry == "HUMANOS" ||
            params.previousAncestryDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true

        val willBeHumano = params.targetAncestry == "HUMANOS" ||
            params.targetAncestryDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true

        val previousFreeAdvantageKeys = (
            params.currentAutomaticAdvantages.toSet() +
                when (params.previousAncestry) {
                    "SAURIOS" -> setOf("Sentidos Aguçados", "Prontidão")
                    "PEQUENINOS" -> setOf("Sorte")
                    "CELESTIAIS" -> setOf("ANTECEDENTE ARCANO MILAGRES", "ANTECEDENTE ARCANO (MILAGRES)")
                    else -> emptySet()
                }
            ).map { it.keyify() }
            .toSet()

        return Result(
            wasHumano = wasHumano,
            willBeHumano = willBeHumano,
            previousFreeAdvantageKeys = previousFreeAdvantageKeys
        )
    }
}
