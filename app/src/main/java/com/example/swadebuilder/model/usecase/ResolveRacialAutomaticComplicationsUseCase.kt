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
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        val withoutOld = params.selectedComplications
            .filterKeys { it.id.keyify() !in oldAutoKeys }
            .toMutableMap()

        val autoBaseKeys = params.currentAutomaticDisadvantages
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()

        params.availableComplications
            .filter { it.id.keyify() in autoBaseKeys }
            .groupBy { it.id.keyify() }
            .forEach { (_, variants) ->
                val selected = variants.maxByOrNull { params.originPriorityResolver(it.origem) }
                    ?: variants.first()

                val severity = when {
                    hasSeverity(params.currentAutomaticDisadvantages, selected.id, "Maior") -> "Maior"
                    hasSeverity(params.currentAutomaticDisadvantages, selected.id, "Menor") -> "Menor"
                    else -> "Menor"
                }

                withoutOld[selected] = severity
            }

        return Result(selectedComplications = withoutOld)
    }

    private fun hasSeverity(automaticDisadvantages: List<String>, complicationId: String, severity: String): Boolean {
        val key = complicationId.keyify()
        return automaticDisadvantages.any {
            it.substringBefore("(").trim().keyify() == key &&
                it.contains(severity, ignoreCase = true)
        }
    }
}
