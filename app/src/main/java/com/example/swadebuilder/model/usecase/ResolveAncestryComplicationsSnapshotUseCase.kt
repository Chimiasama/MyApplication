package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao

class ResolveAncestryComplicationsSnapshotUseCase(
    private val resolveRacialAutomaticComplicationsUseCase: ResolveRacialAutomaticComplicationsUseCase = ResolveRacialAutomaticComplicationsUseCase()
) {

    data class Params(
        val previousAutomaticDisadvantages: List<String>,
        val currentAutomaticDisadvantages: List<String>,
        val availableComplications: List<Complicacao>,
        val selectedComplications: Map<Complicacao, String?>,
        val originPriorityResolver: (Complicacao) -> Int
    )

    data class Result(
        val selectedComplications: Map<Complicacao, String?>
    )

    fun execute(params: Params): Result {
        val normalizedSelectedComplications = params.selectedComplications
            .mapValues { (_, v) -> if (v == "Maior") 2 else 1 }
            .mapNotNull { (complicacao, severidade) -> severidade.let { complicacao to it } }
            .toMap()

        val resolvedAutomaticComplications = resolveRacialAutomaticComplicationsUseCase.execute(
            ResolveRacialAutomaticComplicationsUseCase.Params(
                previousAutomaticDisadvantages = params.previousAutomaticDisadvantages,
                currentAutomaticDisadvantages = params.currentAutomaticDisadvantages,
                availableComplications = params.availableComplications,
                selectedComplications = normalizedSelectedComplications,
                originPriorityResolver = params.originPriorityResolver
            )
        )

        return Result(
            selectedComplications = resolvedAutomaticComplications.selectedComplications.mapValues { (_, v) ->
                if (v == 2) "Maior" else "Menor"
            }
        )
    }
}
