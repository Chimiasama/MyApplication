package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ResolveGrantedAncestryAdvantagesUseCase {

    data class Params(
        val grantedAdvantageNamesOrIds: List<String>,
        val allAdvantages: List<Vantagem>,
        val selectedAdvantages: List<Vantagem>
    )

    data class Result(
        val advantagesToAdd: List<Vantagem>
    )

    fun execute(params: Params): Result {
        val selectedIds = params.selectedAdvantages.map { it.id }.toSet()

        val toAdd = params.grantedAdvantageNamesOrIds.mapNotNull { featString ->
            val featKey = featString.keyify()
            params.allAdvantages.firstOrNull { advantage ->
                advantage.nome.keyify() == featKey ||
                    advantage.id == featString ||
                    advantage.id.keyify() == featKey
            }
        }.filter { it.id !in selectedIds }

        return Result(advantagesToAdd = toAdd.distinctBy { it.id })
    }
}
