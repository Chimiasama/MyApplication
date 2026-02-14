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
        val previousAncestryKey = params.previousAncestry.keyify()
        val targetAncestryKey = params.targetAncestry.keyify()

        val wasHumano = previousAncestryKey == "humanos" ||
            params.previousAncestryDef?.vantagensGratis?.any { it.keyify() == "adaptavel" } == true

        val willBeHumano = targetAncestryKey == "humanos" ||
            params.targetAncestryDef?.vantagensGratis?.any { it.keyify() == "adaptavel" } == true

        val previousFreeAdvantageKeys = (
            params.currentAutomaticAdvantages.toSet() +
                when (previousAncestryKey) {
                    "saurios" -> setOf("Sentidos Aguçados", "Prontidão")
                    "pequeninos" -> setOf("Sorte")
                    "celestiais" -> setOf("ANTECEDENTE ARCANO MILAGRES", "ANTECEDENTE ARCANO (MILAGRES)")
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
