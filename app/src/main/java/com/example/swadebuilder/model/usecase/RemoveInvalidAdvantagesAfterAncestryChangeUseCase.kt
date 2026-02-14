package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class RemoveInvalidAdvantagesAfterAncestryChangeUseCase {

    data class Params(
        val selectedAdvantages: MutableList<Vantagem>,
        val automaticAdvantages: List<String>,
        val automaticRacialAdvantages: List<String>,
        val automaticTropoAdvantageIds: Set<String>,
        val meetsRequirements: (Vantagem) -> Boolean
    )

    data class Result(
        val removedAdvantages: List<Vantagem>
    )

    fun execute(params: Params): Result {
        val autoValues = params.automaticAdvantages + params.automaticRacialAdvantages
        val autoKeys = autoValues
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()
        val autoIds = autoValues.toSet()

        val removed = mutableListOf<Vantagem>()
        var changed = true

        while (changed) {
            changed = false
            val iterator = params.selectedAdvantages.iterator()
            while (iterator.hasNext()) {
                val vantagem = iterator.next()

                if (vantagem.nome.substringBefore("(").trim().keyify() in autoKeys) continue
                if (vantagem.id in autoIds) continue
                if (vantagem.id in params.automaticTropoAdvantageIds) continue
                if (vantagem.id == "conexoes" && vantagem.choice?.equals("Máfia", ignoreCase = true) == true) continue

                if (!params.meetsRequirements(vantagem)) {
                    iterator.remove()
                    removed.add(vantagem)
                    changed = true
                }
            }
        }

        return Result(removedAdvantages = removed)
    }
}
