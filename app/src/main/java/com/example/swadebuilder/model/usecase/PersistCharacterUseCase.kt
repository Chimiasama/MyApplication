package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.GameDataStore
import com.example.swadebuilder.model.PersonagemSnapshot
import com.example.swadebuilder.model.rules.GameRules
import com.example.swadebuilder.model.rules.RulesResolver

/**
 * Use case to persist the character state into a snapshot.
 */
class PersistCharacterUseCase {

    data class Input(
        val state: com.example.swadebuilder.CriadorState,
        val activeModules: Set<String>,
        val gameRules: GameRules
    )

    fun execute(input: Input): PersonagemSnapshot {
        // Delegate to the existing logic in state for now, but this is where we would
        // move the mapping logic to decouple it from the state class.
        // For Phase 2 extraction, we just wrap the call to show intent.
        return input.state.toSnapshot()
    }
}
