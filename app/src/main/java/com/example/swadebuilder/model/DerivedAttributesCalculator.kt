package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.keyify

/**
 * Calculates derived attributes like Parry, Size (generic), etc.
 * Keeping Toughness calculation in CriadorState as requested for specific hardcoded logic.
 */
object DerivedAttributesCalculator {

    fun valorAparar(state: CriadorState): Int {
        // PROMPT 5: Jutsu logic
        // Find max Fighting value among all "Lutar" variants (Base + Jutsu extras)
        // If Arte da Guerra is active, isJutsuPericia covers Lutar + Jutsu X.
        // If not, it just finds Lutar.

        // Use periciasExpandidas (periciasComIdiomas) to iterate over all skills including dynamic ones
        // But periciasComIdiomas returns a List<Pericia>. We just need to check all of them.

        // Wait, periciasComIdiomas constructs the list dynamically.
        // state.periciasComIdiomas() will contain "Lutar", "Jutsu 2", etc. if active.

        val fightingSkills = state.periciasComIdiomas().filter {
            state.isJutsuPericia(it) || it.nome.keyify() == Constants.SKILL_FIGHTING
        }

        val maxFightingRaw = fightingSkills.maxOfOrNull { state.rawTotalComSupers(it) } ?: 0

        val base = 2 + (maxFightingRaw / 2)

        val bloquearBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_BLOCK.keyify() }) 1 else 0
        val bloquearAprimoradoBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_IMPROVED_BLOCK.keyify() }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + state.bonusApararFromPower
    }

    fun valorTamanho(state: CriadorState): Int {
        return ModifierEngine.sizeDisplay(state)
    }

    fun valorMovimentacao(state: CriadorState): Int {
        val base = 6

        val racialPenalty =
            state.listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == state.ancestralidade }
                ?.desvantagens
                ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                .takeIf { it == true }
                ?.let { 1 }
                ?: 0

        val idosoPenalty =
            state.complicacoesSelecionadas
                .filterKeys { it.name.keyify() == Constants.EDGE_ELDERLY.keyify() || it.id.keyify().endsWith(Constants.EDGE_ELDERLY.keyify()) }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val lentoPenalty = state.complicacoesSelecionadas
            .entries
            .firstOrNull {
                it.key.name.keyify() == Constants.EDGE_SLOW.keyify() || it.key.id.keyify().endsWith(Constants.EDGE_SLOW.keyify())
            }
            ?.let { (_, grau) ->
                when (grau) {
                    "Menor" -> 1
                    "Maior" -> 2
                    else    -> 0
                }
            }
            ?: 0

        val obesoPenalty =
            state.complicacoesSelecionadas
                .filterKeys { it.name.keyify() == Constants.EDGE_OBESE.keyify() || it.id.keyify().endsWith(Constants.EDGE_OBESE.keyify()) }
                .isNotEmpty()
                .takeIf { it }
                ?.let { 1 }
                ?: 0

        val ligeiroBonus =
            if (state.vantagensSelecionadas.any { it.nome.keyify() == Constants.EDGE_FLEET_FOOTED.keyify() })
                2
            else
                0

        return (base
                - racialPenalty
                - idosoPenalty
                - lentoPenalty
                - obesoPenalty
                + ligeiroBonus
                + state.bonusMovimentacaoFromPower)
            .coerceAtLeast(0)
    }
}
