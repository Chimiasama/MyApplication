package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.VariantOption
import com.example.swadebuilder.registry.AncestryVariantRegistry

/**
 * Motor único que decide QUAL pacote de traços/vantagens/desvantagens uma
 * ancestralidade produz, dado (variante escolhida pelo mestre, se houver) e
 * (respostas do jogador às Seleções disponíveis). Substitui gradualmente os
 * `if (ancKey == "X")` espalhados em `applyAncestryVariantAdjustments` e
 * `ResolveAncestrySpecificAdjustmentsUseCase` — mas não substitui o
 * ModifierEngine: este motor só decide QUAIS strings entram em
 * habilidades[]/vantagensGratis/desvantagensRaciais; a interpretação mecânica
 * dessas strings continua sendo do ModifierEngine, como já era.
 *
 * Seleções do tipo BUDGETED_CATALOG (ex.: os traços negativos do Anão Ciber)
 * não são resolvidas genericamente aqui — o catálogo de cada raça é conteúdo
 * específico dela (ver AnaoCiberTraitCatalog), então o chamador injeta o
 * resultado já resolvido via `catalogPackages`.
 */
class ResolveAncestryVariantPackageUseCase {

    fun resolve(
        ancestralidadeId: String,
        variantOptionId: String?,
        selectionAnswers: List<SelectionAnswer>,
        catalogPackages: Map<String, ResolvedTraitPackage> = emptyMap()
    ): ResolvedTraitPackage {
        val config = AncestryVariantRegistry.get(ancestralidadeId) ?: return ResolvedTraitPackage()
        val answersById = selectionAnswers.associateBy { it.selectionId }

        val variantOption: VariantOption? = variantOptionId?.let { id ->
            config.grupoVariante?.opcoes?.firstOrNull { it.id == id }
        }

        val selecoesAtivas = config.selecoes + (variantOption?.selecoes ?: emptyList())

        val resolved = mutableListOf(variantOption?.pacoteFixo ?: ResolvedTraitPackage())

        selecoesAtivas.forEach { def ->
            when (def.tipo) {
                SelectionType.BUDGETED_CATALOG -> {
                    catalogPackages[def.id]?.let { resolved.add(it) }
                }
                else -> resolveSelection(def, answersById[def.id])?.let { resolved.add(it) }
            }
        }

        return merge(resolved)
    }

    private fun resolveSelection(def: SelectionDef, answer: SelectionAnswer?): ResolvedTraitPackage? {
        return when (def.tipo) {
            SelectionType.TARGET_ATTRIBUTE_OR_SKILL -> {
                val template = def.injectionTemplate ?: return null
                val alvo = answer?.targetChoice
                    ?: def.targetOptions?.firstOrNull()
                    ?: return null
                ResolvedTraitPackage(tracosParaAdicionar = listOf(template.replace("{alvo}", alvo)))
            }
            SelectionType.FIXED_PACKAGE -> {
                val chosenId = answer?.fixedPackageChoiceId ?: def.pacotesFixos?.firstOrNull()?.id
                def.pacotesFixos?.firstOrNull { it.id == chosenId }?.pacote
            }
            SelectionType.BUDGETED_CATALOG -> null // tratado à parte em resolve()
        }
    }

    private fun merge(packages: List<ResolvedTraitPackage>): ResolvedTraitPackage = ResolvedTraitPackage(
        tracosParaAdicionar = packages.flatMap { it.tracosParaAdicionar },
        tracosParaRemoverPorId = packages.flatMap { it.tracosParaRemoverPorId },
        vantagensGratisParaAdicionar = packages.flatMap { it.vantagensGratisParaAdicionar },
        vantagensGratisIds = packages.flatMap { it.vantagensGratisIds },
        desvantagensParaAdicionar = packages.flatMap { it.desvantagensParaAdicionar },
        tracosParaRemoverPorNome = packages.flatMap { it.tracosParaRemoverPorNome },
        desvantagensParaRemover = packages.flatMap { it.desvantagensParaRemover },
        naturalArmor = packages.sumOf { it.naturalArmor },
        anotacoes = packages.flatMap { it.anotacoes }
    )
}
