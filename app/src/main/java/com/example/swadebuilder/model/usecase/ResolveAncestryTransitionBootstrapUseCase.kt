package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem

class ResolveAncestryTransitionBootstrapUseCase(
    private val resolveAncestryTransitionContextUseCase: ResolveAncestryTransitionContextUseCase = ResolveAncestryTransitionContextUseCase(),
    private val applyHumanAncestryTransitionUseCase: ApplyHumanAncestryTransitionUseCase = ApplyHumanAncestryTransitionUseCase()
) {

    data class Params(
        val previousAncestry: String,
        val targetAncestry: String,
        val forceLoseHumanBonus: Boolean = false,
        val previousAncestryDef: RacialModifier?,
        val targetAncestryDef: RacialModifier?,
        val currentAutomaticAdvantages: List<String>,
        val pontosVantagemAtuais: Int,
        val vantagensSelecionadas: List<Vantagem>,
        val meioElfoAgil: Boolean // Pass flag
    )

    data class Result(
        val ancestryTransitionContext: ResolveAncestryTransitionContextUseCase.Result,
        val humanTransition: ApplyHumanAncestryTransitionUseCase.Result
    )

    fun execute(params: Params): Result {
        val transitionContext = resolveAncestryTransitionContextUseCase.execute(
            ResolveAncestryTransitionContextUseCase.Params(
                previousAncestry = params.previousAncestry,
                targetAncestry = params.targetAncestry,
                previousAncestryDef = params.previousAncestryDef,
                targetAncestryDef = params.targetAncestryDef,
                currentAutomaticAdvantages = params.currentAutomaticAdvantages,
                meioElfoAgil = params.meioElfoAgil
            )
        )

        val humanTransition = applyHumanAncestryTransitionUseCase.execute(
            ApplyHumanAncestryTransitionUseCase.Params(
                wasHumano = transitionContext.wasHumano,
                vaiSerHumano = transitionContext.willBeHumano,
                forceLoseHumanBonus = params.forceLoseHumanBonus,
                pontosVantagemAtuais = params.pontosVantagemAtuais,
                vantagensSelecionadas = params.vantagensSelecionadas,
                prevFreeKeys = transitionContext.previousFreeAdvantageKeys
            )
        )

        return Result(
            ancestryTransitionContext = transitionContext,
            humanTransition = humanTransition
        )
    }
}
