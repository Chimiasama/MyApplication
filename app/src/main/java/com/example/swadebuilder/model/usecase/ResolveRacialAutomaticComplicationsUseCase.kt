package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.util.keyify

class ResolveRacialAutomaticComplicationsUseCase {

    data class Params(
        val previousAutomaticDisadvantages: List<String>,
        val currentAutomaticDisadvantages: List<String>,
        val availableComplications: List<Complicacao>,
        val selectedComplications: Map<Complicacao, String>,
        val originPriorityResolver: (String?) -> Int
    )

    data class Result(
        val selectedComplications: Map<Complicacao, String>
    )

    fun execute(params: Params): Result {
        val oldAutoKeys = params.previousAutomaticDisadvantages
            .map { normalizeToken(it.substringBefore("(").trim()) }
            .toSet()

        val withoutOld = params.selectedComplications
            .filterKeys { complication ->
                val complicationTokens = setOf(
                    normalizeToken(complication.id),
                    normalizeToken(complication.name)
                )
                complicationTokens.none { it in oldAutoKeys }
            }
            .toMutableMap()

        val autoBaseKeys = params.currentAutomaticDisadvantages
            .map { normalizeToken(it.substringBefore("(").trim()) }
            .toSet()

        params.availableComplications
            .filter { complication ->
                val complicationTokens = setOf(
                    normalizeToken(complication.id),
                    normalizeToken(complication.name)
                )
                complicationTokens.any { it in autoBaseKeys }
            }
            .groupBy { normalizeToken(it.id) }
            .forEach { (_, variants) ->
                val selected = variants.maxByOrNull { params.originPriorityResolver(it.origem) }
                    ?: variants.first()

                val severity = when {
                    hasSeverity(params.currentAutomaticDisadvantages, selected, "Maior") -> "Maior"
                    hasSeverity(params.currentAutomaticDisadvantages, selected, "Menor") -> "Menor"
                    else -> "Menor"
                }

                withoutOld[selected] = severity
            }

        return Result(selectedComplications = withoutOld)
    }

    private fun hasSeverity(
        automaticDisadvantages: List<String>,
        complication: Complicacao,
        severity: String
    ): Boolean {
        val tokens = setOf(
            normalizeToken(complication.id),
            normalizeToken(complication.name)
        )
        return automaticDisadvantages.any {
            normalizeToken(it.substringBefore("(").trim()) in tokens &&
                it.contains(severity, ignoreCase = true)
        }
    }

    private fun normalizeToken(value: String): String =
        value.keyify()
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")
}
