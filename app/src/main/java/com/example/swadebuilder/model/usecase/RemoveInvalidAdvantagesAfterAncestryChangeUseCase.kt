package com.example.swadebuilder.model.usecase

import android.util.Log
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

        Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - AutoKeys: $autoKeys")
        Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - AutoIds: $autoIds")

        val removed = mutableListOf<Vantagem>()
        var changed = true

        while (changed) {
            changed = false
            val iterator = params.selectedAdvantages.iterator()
            while (iterator.hasNext()) {
                val vantagem = iterator.next()

                val nomeKey = vantagem.nome.substringBefore("(").trim().keyify()

                if (nomeKey in autoKeys) {
                    Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - Retaining (AutoKey match): ${vantagem.nome}")
                    continue
                }
                if (vantagem.id in autoIds) {
                    Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - Retaining (AutoId match): ${vantagem.nome}")
                    continue
                }
                if (vantagem.id in params.automaticTropoAdvantageIds) {
                    Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - Retaining (Tropo match): ${vantagem.nome}")
                    continue
                }
                if (vantagem.id == "conexoes" && vantagem.choice?.equals("Máfia", ignoreCase = true) == true) continue

                val meetsReqs = params.meetsRequirements(vantagem)
                if (!meetsReqs) {
                    Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - Removing (Requirements not met): ${vantagem.nome}")
                    iterator.remove()
                    removed.add(vantagem)
                    changed = true
                } else {
                    Log.d("SWADE_DEBUG", "RemoveInvalidAdvantages - Retaining (Requirements met): ${vantagem.nome}")
                }
            }
        }

        return Result(removedAdvantages = removed)
    }
}
