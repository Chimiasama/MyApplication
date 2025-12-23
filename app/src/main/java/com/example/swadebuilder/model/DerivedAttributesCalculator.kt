package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.keyify

/**
 * Calculates derived attributes like Parry, Size (generic), etc.
 * Keeping Toughness calculation in CriadorState as requested for specific hardcoded logic.
 */
object DerivedAttributesCalculator {

    fun valorAparar(state: CriadorState): Int {
        val perLutar = com.example.swadebuilder.mapaPericias[Constants.SKILL_FIGHTING]
        val perJutsu = com.example.swadebuilder.mapaPericias[Constants.SKILL_JUTSU]
        val lutarRaw = perLutar?.let { state.rawTotalComSupers(it) } ?: 0
        val jutsuRaw = perJutsu?.let { state.rawTotalComSupers(it) } ?: 0
        val melhorLuta = maxOf(lutarRaw, jutsuRaw)
        val base     = 2 + (melhorLuta / 2)

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
            com.example.swadebuilder.listaAncestralidadesJson
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
