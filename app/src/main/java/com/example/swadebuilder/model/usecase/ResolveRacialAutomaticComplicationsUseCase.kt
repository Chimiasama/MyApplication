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
        // Build map of ID/Key -> Severity from the current list strings
        // Strings might be "Forasteiro (Maior)" or "FORASTEIRO|Maior" or just name
        // We need a robust parser for the new enriched strings coming from CriadorState
        val currentSpecs = parseDisadvantages(params.currentAutomaticDisadvantages)
        val oldSpecs = parseDisadvantages(params.previousAutomaticDisadvantages)

        val oldAutoKeys = oldSpecs.keys

        val withoutOld = params.selectedComplications
            .filterKeys { complication ->
                val complicationTokens = setOf(
                    normalizeToken(complication.id),
                    normalizeToken(complication.name)
                )
                complicationTokens.none { it in oldAutoKeys }
            }
            .toMutableMap()

        val autoBaseKeys = currentSpecs.keys

        params.availableComplications
            .filter { complication ->
                val complicationTokens = setOf(
                    normalizeToken(complication.id),
                    normalizeToken(complication.name)
                )
                complicationTokens.any { it in autoBaseKeys }
            }
            .groupBy { normalizeToken(it.id) }
            .forEach { (normId, variants) ->
                val selected = variants.maxByOrNull { params.originPriorityResolver(it.origem) }
                    ?: variants.first()

                // Determine severity from the parsed spec, defaulting to "Menor"
                val specSeverity = currentSpecs[normId] ?: "Menor"
                withoutOld[selected] = specSeverity
            }

        return Result(selectedComplications = withoutOld)
    }

    private fun parseDisadvantages(list: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        list.forEach { item ->
            // item format might be "Name (Severity)" or "Name|Severity" if we change upstream
            // For now assume standard "Name (Severity)" or just "Name"
            // But we need to handle the new explicit severity if we passed it down?
            // Actually, CriadorState passes strings. We need CriadorState to pass "Name|Severity" or use the (Parens) convention.
            // Our script updated names to include (Maior)/(Menor)? No, script added `severity` field to JSON.
            // We need CriadorState to construct the string properly from that JSON field.

            val normKey = normalizeToken(item.substringBefore("(").trim())
            val severity = when {
                item.contains("(Maior)", ignoreCase = true) || item.contains("|Maior", ignoreCase = true) -> "Maior"
                item.contains("(Menor)", ignoreCase = true) || item.contains("|Menor", ignoreCase = true) -> "Menor"
                else -> "Menor" // Default if unspecified
            }
            map[normKey] = severity
        }
        return map
    }

    private fun normalizeToken(value: String): String =
        value.keyify()
            .replace("_", "")
            .replace("-", "")
            .replace(" ", "")
}
