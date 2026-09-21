package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.TraitTargetKind
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
        livro: String,
        variantOptionId: String?,
        selectionAnswers: List<SelectionAnswer>,
        catalogPackages: Map<String, ResolvedTraitPackage> = emptyMap()
    ): ResolvedTraitPackage {
        val config = AncestryVariantRegistry.get(ancestralidadeId, livro) ?: return ResolvedTraitPackage()
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
            SelectionType.TARGET_ATTRIBUTE_OR_SKILL -> resolveTargetAttributeOrSkill(def, answer)
            SelectionType.FIXED_PACKAGE -> {
                val chosenId = answer?.fixedPackageChoiceId ?: def.pacotesFixos?.firstOrNull()?.id
                def.pacotesFixos?.firstOrNull { it.id == chosenId }?.pacote
            }
            SelectionType.BUDGETED_CATALOG -> null // tratado à parte em resolve()
        }
    }

    // O jogador escolhe QUAL atributo/perícia recebe o bônus (ex.: Meio-Orc
    // Força-ou-Vigor, Feral Força/Vigor/Agilidade) — o traço injetado usa o
    // mesmo mecanismo parametrizado (traitId=ATTRIBUTE_BOOST/SKILL_BOOST +
    // targetRef) que MonstroTemplate.kt já usa pra atributo de monstro, em
    // vez de um id fixo por combinação (ex.: um id só pra "Força escolhida"
    // e outro pra "Vigor escolhido" seria hardcode por valor final, o mesmo
    // problema que RacialTraitPointCatalog já evita com AtributoStep/
    // PericiaStep). `pontos` vai explícito (não confia em CUSTOS[id]), mas
    // computado via RacialTraitPointCatalog.custoDe() — a MESMA fórmula que
    // já calibra o catálogo oficial pra ATTRIBUTE_BOOST/SKILL_BOOST (ver
    // custoDe) — em vez de duplicado aqui, porque o custo de ATTRIBUTE_BOOST/
    // SKILL_BOOST no orçamento (Resolve/ValidateAncestryOptionBudgetsUseCase)
    // lê `TraitAddition.id` cru, não `traitId` — ver o comentário de
    // TraitAddition.traitId.
    private fun resolveTargetAttributeOrSkill(def: SelectionDef, answer: SelectionAnswer?): ResolvedTraitPackage {
        val opcoes = def.targetOptions.orEmpty()
        val alvo = answer?.targetChoice?.takeIf { escolhido -> opcoes.any { it.equals(escolhido, ignoreCase = true) } }
            ?: def.defaultTargetChoice?.takeIf { padrao -> opcoes.any { it.equals(padrao, ignoreCase = true) } }
            ?: opcoes.firstOrNull()
            ?: return ResolvedTraitPackage()
        val nomeExibicao = def.injectionTemplate?.replace("{alvo}", alvo) ?: alvo
        val traitIdMecanico = if (def.targetKind == TraitTargetKind.SKILL) "SKILL_BOOST" else "ATTRIBUTE_BOOST"
        val pontos = com.example.swadebuilder.model.RacialTraitPointCatalog.custoDe(traitIdMecanico, value = def.passos)
        return ResolvedTraitPackage(
            tracosParaAdicionar = listOf(
                TraitAddition(
                    nome = nomeExibicao,
                    id = "${def.id}_escolha".uppercase(),
                    traitId = traitIdMecanico,
                    targetRef = alvo,
                    value = def.passos,
                    pontos = pontos
                )
            )
        )
    }

    private fun merge(packages: List<ResolvedTraitPackage>): ResolvedTraitPackage = ResolvedTraitPackage(
        tracosParaAdicionar = packages.flatMap { it.tracosParaAdicionar },
        tracosNegativosParaAdicionar = packages.flatMap { it.tracosNegativosParaAdicionar },
        tracosParaRemoverPorId = packages.flatMap { it.tracosParaRemoverPorId },
        vantagensGratisParaAdicionar = packages.flatMap { it.vantagensGratisParaAdicionar },
        vantagensGratisIds = packages.flatMap { it.vantagensGratisIds },
        desvantagensParaAdicionar = packages.flatMap { it.desvantagensParaAdicionar },
        tracosParaRemoverPorNome = packages.flatMap { it.tracosParaRemoverPorNome },
        desvantagensParaRemover = packages.flatMap { it.desvantagensParaRemover },
        naturalArmor = packages.sumOf { it.naturalArmor },
        armasNaturaisParaAdicionar = packages.flatMap { it.armasNaturaisParaAdicionar },
        anotacoes = packages.flatMap { it.anotacoes }
    )
}
