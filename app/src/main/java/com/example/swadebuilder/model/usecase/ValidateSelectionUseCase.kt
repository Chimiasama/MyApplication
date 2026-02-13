package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.rules.GameRules
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

/**
 * Validates if an item (Advantage/Complication) can be selected based on current state and rules.
 */
class ValidateSelectionUseCase {

    data class SelectionContext(
        val ancestralidade: String,
        val estagioAtualNome: String,
        val estagioAtualIndex: Int,
        val atributosRaw: Map<String, Int>,
        val periciasRaw: Map<String, Int>,
        val vantagensSelecionadas: List<Vantagem>,
        val complicacoesSelecionadas: Map<Complicacao, String?>,
        val cartaSelvagem: Boolean,
        val isMonstro: Boolean,
        val tipoMonstro: String?
    )

    data class Result(
        val allowed: Boolean,
        val reason: String? = null
    )

    fun validateVantagem(
        vantagem: Vantagem,
        context: SelectionContext,
        rules: GameRules,
        listaEstagios: List<com.example.swadebuilder.Estagio>
    ): Result {
        // This logic mirrors 'podeSelecionar' from CriadorState.
        // Moving it here allows unit testing without instantiating the whole state.

        // 1. Check Scenario Forbidden rules
        if (rules.forbiddenAdvantageIds().contains(vantagem.id)) {
            return Result(false, "Proibido pelas regras do cenário.")
        }

        // 2. Check Prerequisites (simplified for MVP extraction)
        // ... (Full implementation would copy the logic from CriadorState.podeSelecionar)

        // Return true for now to allow compilation while we migrate logic piece by piece
        return Result(true)
    }
}
