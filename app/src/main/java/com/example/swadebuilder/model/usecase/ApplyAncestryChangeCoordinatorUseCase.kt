package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ApplyAncestryChangeCoordinatorUseCase(
    private val resolveAncestryTransitionBootstrapUseCase: ResolveAncestryTransitionBootstrapUseCase = ResolveAncestryTransitionBootstrapUseCase(),
    private val adjustAttributesForAncestryChangeUseCase: AdjustAttributesForAncestryChangeUseCase = AdjustAttributesForAncestryChangeUseCase(),
    private val resolveAncestryRacialPackageUseCase: ResolveAncestryRacialPackageUseCase = ResolveAncestryRacialPackageUseCase(),
    private val resolveAncestryComplicationsSnapshotUseCase: ResolveAncestryComplicationsSnapshotUseCase = ResolveAncestryComplicationsSnapshotUseCase(),
    private val removeInvalidAdvantagesAfterAncestryChangeUseCase: RemoveInvalidAdvantagesAfterAncestryChangeUseCase = RemoveInvalidAdvantagesAfterAncestryChangeUseCase()
) {

    enum class SignoAction { KEEP, SELECT_NONE, CLEAR }

    data class Params(
        val previousAncestry: String,
        val targetAncestry: String,
        val previousAncestryDef: RacialModifier?,
        val targetAncestryDef: RacialModifier?,
        val currentAutomaticAdvantages: List<String>,
        val previousAutomaticDisadvantages: List<String>,
        val pontosVantagemAtuais: Int,
        val vantagensSelecionadas: List<Vantagem>,
        val attributeNames: List<String>,
        val attributeCaps: Map<String, AdjustAttributesForAncestryChangeUseCase.AttributeCap>,
        val paCostStacks: Map<String, List<Int>>,
        val descendenteElementalSelecionado: String?,
        val anoesScifiSelecionado: String? = null,
        val scifiVariant: String? = null,
        val humanoMineradorAtributo: String? = null,
        val allAdvantages: List<Vantagem>,
        val availableComplications: List<Complicacao>,
        val selectedComplications: Map<Complicacao, String?>,
        val automaticTropoAdvantageIds: Set<String>,
        val meetsRequirements: (Vantagem) -> Boolean,
        val originPriorityResolver: (String?) -> Int,
        val compendioArteDaGuerraAtivo: Boolean,
        val compendioSciFiAtivo: Boolean = false,
        val signoAdgSelecionado: String?,
        val modoSupers: Boolean,
        val meioElfoAgil: Boolean // Pass current flag
    )

    data class Result(
        val humanTransition: ApplyHumanAncestryTransitionUseCase.Result,
        val previousFreeAdvantageKeys: Set<String>,
        val attributeAdjustmentResult: AdjustAttributesForAncestryChangeUseCase.Result,
        val signoAction: SignoAction,
        val celestialAAMilagresDesabilitado: Boolean,
        val resetMeioElfoAgil: Boolean,
        val resetMeioOrcForca: Boolean,
        val clearDescendenteElemental: Boolean,
        val resetAnoesScifi: Boolean,
        val resetScifiVariant: Boolean,
        val clearPericiaGnomo: Boolean,
        val racialPackage: ResolveAncestryRacialPackageUseCase.Result,
        val complicationsSnapshot: ResolveAncestryComplicationsSnapshotUseCase.Result,
        val invalidAdvantagesResolution: RemoveInvalidAdvantagesAfterAncestryChangeUseCase.Result
    )

    fun execute(params: Params): Result {
        val transitionBootstrap = resolveAncestryTransitionBootstrapUseCase.execute(
            ResolveAncestryTransitionBootstrapUseCase.Params(
                previousAncestry = params.previousAncestry,
                targetAncestry = params.targetAncestry,
                previousAncestryDef = params.previousAncestryDef,
                targetAncestryDef = params.targetAncestryDef,
                currentAutomaticAdvantages = params.currentAutomaticAdvantages,
                pontosVantagemAtuais = params.pontosVantagemAtuais,
                vantagensSelecionadas = params.vantagensSelecionadas,
                meioElfoAgil = params.meioElfoAgil
            )
        )

        val attributeAdjustmentResult = adjustAttributesForAncestryChangeUseCase.execute(
            AdjustAttributesForAncestryChangeUseCase.Params(
                attributeNames = params.attributeNames,
                attributeCaps = params.attributeCaps,
                paCostStacks = params.paCostStacks
            )
        )

        val targetKey = params.targetAncestry.keyify()
        val signoAction = when {
            params.compendioArteDaGuerraAtivo && targetKey.contains("HUMANO") && params.signoAdgSelecionado == null -> SignoAction.SELECT_NONE
            (!params.compendioArteDaGuerraAtivo || !targetKey.contains("HUMANO")) && params.signoAdgSelecionado != null -> SignoAction.CLEAR
            else -> SignoAction.KEEP
        }

        val filteredAdvantages = if (transitionBootstrap.humanTransition.vantagemRemovida != null) {
            params.vantagensSelecionadas.filter { it.id != transitionBootstrap.humanTransition.vantagemRemovida.id }
        } else {
            params.vantagensSelecionadas
        }

        val racialPackage = resolveAncestryRacialPackageUseCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = params.targetAncestry,
                descendenteElementalSelecionado = params.descendenteElementalSelecionado,
                anoesScifiSelecionado = params.anoesScifiSelecionado,
                scifiVariant = params.scifiVariant,
                humanoMineradorAtributo = params.humanoMineradorAtributo,
                ancestryOptions = params.targetAncestryDef?.opcoes ?: emptyList(),
                isSciFiActive = params.compendioSciFiAtivo,
                allAdvantages = params.allAdvantages,
                selectedAdvantages = filteredAdvantages,
                previousFreeAdvantageKeys = transitionBootstrap.ancestryTransitionContext.previousFreeAdvantageKeys,
                ancestryGrantedAdvantages = params.targetAncestryDef?.vantagensGratis ?: emptyList(),
                ancestryAutomaticDisadvantages = params.targetAncestryDef?.desvantagens ?: emptyList()
            )
        )

        val complicationsSnapshot = resolveAncestryComplicationsSnapshotUseCase.execute(
            ResolveAncestryComplicationsSnapshotUseCase.Params(
                previousAutomaticDisadvantages = params.previousAutomaticDisadvantages,
                currentAutomaticDisadvantages = racialPackage.desvantagensRaciais,
                availableComplications = params.availableComplications,
                selectedComplications = params.selectedComplications,
                originPriorityResolver = params.originPriorityResolver
            )
        )

        val invalidAdvantagesResolution = removeInvalidAdvantagesAfterAncestryChangeUseCase.execute(
            RemoveInvalidAdvantagesAfterAncestryChangeUseCase.Params(
                selectedAdvantages = racialPackage.selectedAdvantages.toMutableList(),
                automaticAdvantages = racialPackage.vantagensAutomaticas,
                automaticRacialAdvantages = racialPackage.vantagensRaciais,
                automaticTropoAdvantageIds = params.automaticTropoAdvantageIds,
                meetsRequirements = params.meetsRequirements
            )
        )

        return Result(
            humanTransition = transitionBootstrap.humanTransition,
            previousFreeAdvantageKeys = transitionBootstrap.ancestryTransitionContext.previousFreeAdvantageKeys,
            attributeAdjustmentResult = attributeAdjustmentResult,
            signoAction = signoAction,
            celestialAAMilagresDesabilitado = (params.targetAncestry == "CELESTIAIS" && params.modoSupers),
            resetMeioElfoAgil = params.targetAncestry != "MEIO-ELFOS",
            resetMeioOrcForca = params.targetAncestry != "MEIO-ORCS",
            clearDescendenteElemental = targetKey != "DESCENDENTE ELEMENTAL" && targetKey != "DESC_ELEMENTAL",
            resetAnoesScifi = targetKey != "ANOES",
            resetScifiVariant = !params.compendioSciFiAtivo || params.previousAncestry != params.targetAncestry, // Simple reset logic: if ancestry changes or SciFi disabled
            clearPericiaGnomo = !targetKey.contains("GNOMO"),
            racialPackage = racialPackage,
            complicationsSnapshot = complicationsSnapshot,
            invalidAdvantagesResolution = invalidAdvantagesResolution
        )
    }
}
