package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

class ApplyHumanAncestryTransitionUseCase {

    data class Params(
        val wasHumano: Boolean,
        val vaiSerHumano: Boolean,
        val forceLoseHumanBonus: Boolean = false,
        val pontosVantagemAtuais: Int,
        val vantagensSelecionadas: List<Vantagem>,
        val prevFreeKeys: Set<String>
    )

    data class Result(
        val novosPontosVantagem: Int,
        val vantagemRemovida: Vantagem? = null
    )

    fun execute(params: Params): Result {
        // Legacy +1 PV logic for Humans disabled in favor of Adaptable Slot logic in CriadorState.
        return Result(novosPontosVantagem = params.pontosVantagemAtuais)
    }
}
