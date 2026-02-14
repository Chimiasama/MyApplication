package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ResolveAncestryRacialPackageUseCase(
    private val resolveGrantedAncestryAdvantagesUseCase: ResolveGrantedAncestryAdvantagesUseCase = ResolveGrantedAncestryAdvantagesUseCase(),
    private val resolveAncestrySpecificAdjustmentsUseCase: ResolveAncestrySpecificAdjustmentsUseCase = ResolveAncestrySpecificAdjustmentsUseCase()
) {

    data class Params(
        val anc: String,
        val descendenteElementalSelecionado: String?,
        val allAdvantages: List<Vantagem>,
        val selectedAdvantages: List<Vantagem>,
        val previousFreeAdvantageKeys: Set<String>,
        val ancestryGrantedAdvantages: List<String>,
        val ancestryAutomaticDisadvantages: List<String>
    )

    data class Result(
        val selectedAdvantages: List<Vantagem>,
        val vantagensAutomaticas: List<String>,
        val vantagensRaciais: List<String>,
        val desvantagensRaciais: List<String>,
        val naturalArmorFromRace: Int,
        val forceArmorZero: Boolean,
        val elementalAction: ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction
    )

    fun execute(params: Params): Result {
        val selected = params.selectedAdvantages
            .filterNot { it.nome.keyify() in params.previousFreeAdvantageKeys }
            .toMutableList()

        val vantagensAutomaticas = params.ancestryGrantedAdvantages.toMutableList()
        val vantagensRaciais = params.ancestryGrantedAdvantages.toMutableList()
        val desvantagensRaciais = params.ancestryAutomaticDisadvantages.toMutableList()

        val grantedAdvantagesResult = resolveGrantedAncestryAdvantagesUseCase.execute(
            ResolveGrantedAncestryAdvantagesUseCase.Params(
                grantedAdvantageNamesOrIds = params.ancestryGrantedAdvantages,
                allAdvantages = params.allAdvantages,
                selectedAdvantages = selected
            )
        )
        selected.addAll(grantedAdvantagesResult.advantagesToAdd)

        val ancestrySpecificAdjustments = resolveAncestrySpecificAdjustmentsUseCase.execute(
            anc = params.anc,
            descendenteElementalSelecionado = params.descendenteElementalSelecionado
        )

        ancestrySpecificAdjustments.ensureAdvantageNames.forEach { advantageName ->
            params.allAdvantages.firstOrNull { it.nome.equals(advantageName, ignoreCase = true) }
                ?.let { edge ->
                    if (selected.none { it.id == edge.id }) {
                        selected.add(edge)
                    }
                }
        }

        ancestrySpecificAdjustments.ensureAdvantageIds.forEach { advantageId ->
            val edge = params.allAdvantages.firstOrNull { it.id == advantageId }
            if (edge != null && selected.none { it.id == edge.id }) {
                selected.add(edge)
            }
        }

        ancestrySpecificAdjustments.ensureAutomaticAdvantages.forEach { automaticAdvantage ->
            if (vantagensAutomaticas.none { it.equals(automaticAdvantage, ignoreCase = true) }) {
                vantagensAutomaticas.add(automaticAdvantage)
            }
        }

        ancestrySpecificAdjustments.ensureRacialDisadvantages.forEach { racialDisadvantage ->
            if (desvantagensRaciais.none { it.equals(racialDisadvantage, ignoreCase = true) }) {
                desvantagensRaciais.add(racialDisadvantage)
            }
        }

        return Result(
            selectedAdvantages = selected,
            vantagensAutomaticas = vantagensAutomaticas,
            vantagensRaciais = vantagensRaciais,
            desvantagensRaciais = desvantagensRaciais,
            naturalArmorFromRace = ancestrySpecificAdjustments.naturalArmorFromRace,
            forceArmorZero = ancestrySpecificAdjustments.forceArmorZero,
            elementalAction = ancestrySpecificAdjustments.elementalAction
        )
    }
}
