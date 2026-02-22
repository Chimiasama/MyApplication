package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.util.keyify

class ResolveAncestryVariantUseCase {

    data class Input(
        val selectedVariant: String?,
        val availableOptions: List<String>,
        val legacySelectedVariant: String? = null
    )

    data class Result(
        val normalizedSelection: String?,
        val defaultOption: String?
    )

    fun execute(input: Input): Result {
        val options = input.availableOptions
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (options.isEmpty()) {
            return Result(normalizedSelection = null, defaultOption = null)
        }

        val defaultOption = options.first()
        val aliasesByOption = options.associateWith { option -> optionAliases(option) }

        val selected = normalizeCandidate(
            candidate = input.selectedVariant,
            options = options,
            aliasesByOption = aliasesByOption
        )

        val legacy = normalizeCandidate(
            candidate = input.legacySelectedVariant,
            options = options,
            aliasesByOption = aliasesByOption
        )

        return Result(
            normalizedSelection = selected ?: legacy ?: defaultOption,
            defaultOption = defaultOption
        )
    }

    private fun normalizeCandidate(
        candidate: String?,
        options: List<String>,
        aliasesByOption: Map<String, Set<String>>
    ): String? {
        if (candidate.isNullOrBlank()) return null
        val key = candidate.keyify()
        val direct = options.firstOrNull { it.keyify() == key }
        if (direct != null) return direct

        return aliasesByOption.entries.firstOrNull { (_, aliases) -> key in aliases }?.key
    }

    private fun optionAliases(option: String): Set<String> {
        val optionKey = option.keyify()
        val aliases = mutableSetOf(optionKey)

        if (optionKey == "BASICO" || optionKey == "PADRAO") {
            aliases += setOf("BASICO", "PADRAO")
        }

        return aliases
    }
}
