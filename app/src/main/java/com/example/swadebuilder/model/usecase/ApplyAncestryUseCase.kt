package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.GameDataStore
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.rules.GameRules
import com.example.swadebuilder.util.keyify

/**
 * Handles the logic of applying an ancestry change.
 */
class ApplyAncestryUseCase {

    data class Input(
        val newAncestryName: String,
        val previousAncestryName: String,
        val availableAncestries: List<RacialModifier>,
        val currentAttributesRaw: Map<String, Int>,
        val attributeStacks: Map<String, List<Int>>,
        val isHuman: Boolean // Helper flag
    )

    data class Output(
        val newAttributesRaw: Map<String, Int>,
        val newAttributeStacks: Map<String, List<Int>>,
        val messages: List<String>,
        val autoAdvantages: List<String>,
        val autoHindrances: List<String>
    )

    fun execute(input: Input): Output {
        val messages = mutableListOf<String>()

        // 1. Determine new racial bases
        // 2. Re-apply attribute purchases (stacks) respecting limits
        // 3. Determine new racial traits

        // Placeholder return
        return Output(
            input.currentAttributesRaw,
            input.attributeStacks,
            messages,
            emptyList(),
            emptyList()
        )
    }
}
