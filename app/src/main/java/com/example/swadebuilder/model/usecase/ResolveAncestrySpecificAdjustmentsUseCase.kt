package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AnaoCiberTraitCatalog
import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.RacialTraitEffect
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.canonicalOriginKey
import com.example.swadebuilder.registry.AncestryVariantRegistry
import com.example.swadebuilder.util.keyify

class ResolveAncestrySpecificAdjustmentsUseCase(
    private val resolveAncestryVariantUseCase: ResolveAncestryVariantUseCase = ResolveAncestryVariantUseCase(),
    private val resolveAncestryVariantPackageUseCase: ResolveAncestryVariantPackageUseCase = ResolveAncestryVariantPackageUseCase()
) {

    /**
     * Ponte temporária entre o texto de variante hoje armazenado em
     * `scifiVariant` (ex.: "Voto (Maior)") e o id estável do pacote fixo no
     * AncestryVariantRegistry (ex.: "voto") — casa pelo `nome` cadastrado no
     * registro, sem precisar de uma tabela de tradução separada. Isso é só
     * enquanto a Seleção ainda usa o mesmo armazenamento/UI da Variante
     * (unificar isso de vez é um passo à parte, ainda não feito).
     */
    private fun fixedPackageAnswerFrom(def: SelectionDef, displayValue: String?): SelectionAnswer {
        val matchId = def.pacotesFixos?.firstOrNull { it.nome.equals(displayValue, ignoreCase = true) }?.id
        return SelectionAnswer(selectionId = def.id, fixedPackageChoiceId = matchId)
    }

    /**
     * Casa a Variante efetiva (texto já normalizado por
     * ResolveAncestryVariantUseCase) com o id estável da VariantOption
     * correspondente no registro. Sem opção correspondente (ex.:
     * `effectiveVariant` nulo por falta de `ancestryOptions`), cai na opção
     * "Básico"/"Padrão" cadastrada — mesmo comportamento de fallback que o
     * "when" fixo tinha via `else`.
     */
    private fun variantOptionIdFrom(config: AncestryVariantConfig, effectiveVariant: String?): String? {
        val opcoes = config.grupoVariante?.opcoes ?: return null
        return opcoes.firstOrNull { it.nome.equals(effectiveVariant, ignoreCase = true) }?.id
            ?: opcoes.firstOrNull { it.nome.keyify() == "BASICO" || it.nome.keyify() == "PADRAO" }?.id
    }

    /**
     * Constrói o Result a partir do pacote resolvido no registro — só
     * traduz os campos genéricos (traços/vantagens/desvantagens a
     * adicionar/remover). Armadura Natural quando a Variante muda o valor
     * (ex.: Insetoides) já vem embutida em `naturalArmor`; `forceArmorZero`
     * é sempre true nesse grupo de raças (nenhuma delas mantém Armadura sem
     * forçar o reset primeiro), igual ao "when" fixo que este substitui.
     */
    private fun buildResultFromVariantRegistry(ancestralidadeId: String, effectiveVariant: String?): Result? {
        val config = AncestryVariantRegistry.get(ancestralidadeId) ?: return null
        val variantOptionId = variantOptionIdFrom(config, effectiveVariant) ?: return null
        val resolved = resolveAncestryVariantPackageUseCase.resolve(
            ancestralidadeId = ancestralidadeId,
            variantOptionId = variantOptionId,
            selectionAnswers = emptyList()
        )
        return Result(
            naturalArmorFromRace = resolved.naturalArmor,
            forceArmorZero = true,
            ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
            ensureAdvantageIds = resolved.vantagensGratisIds,
            ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar,
            automaticAdvantagesToRemove = resolved.tracosParaRemoverPorNome,
            ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
            racialDisadvantagesToRemove = resolved.desvantagensParaRemover,
            elementalAction = ElementalAction.NONE,
            anotacoesToAdd = resolved.anotacoes
        )
    }

    enum class ElementalAction {
        NONE,
        SELECT_DEFAULT,
        REAPPLY_CURRENT
    }

    data class Result(
        val naturalArmorFromRace: Int,
        val forceArmorZero: Boolean,
        // Nomes de Vantagem (vantagens.json) casados por nome pra conceder a
        // Vantagem de verdade — ver ResolveAncestryRacialPackageUseCase.
        val ensureAdvantageNames: List<String>,
        val ensureAdvantageIds: List<String>,
        // Traços/vantagens automáticas que só entram como bookkeeping
        // (vantagensRaciais/ModifierEngine) — cada um já carrega seu id
        // mecânico explícito (ver TraitAddition), nunca derivado do texto
        // em tempo de execução.
        val ensureAutomaticAdvantages: List<TraitAddition>,
        val automaticAdvantagesToRemove: List<String> = emptyList(),
        val ensureRacialDisadvantages: List<TraitAddition>,
        val elementalAction: ElementalAction,
        val anotacoesToAdd: List<String> = emptyList(),
        val racialDisadvantagesToRemove: List<String> = emptyList()
    )

    fun execute(
        anc: String,
        descendenteElementalSelecionado: String?,
        anoesScifiSelecionado: String? = null,
        scifiVariant: String? = null,
        humanoMineradorAtributo: String? = null,
        anaoCiberTracosSelecionados: List<AnaoCiberTraitSelection> = emptyList(),
        ancestryOptions: List<String> = emptyList(),
        isSciFiActive: Boolean = false,
        isSciFiMechasActive: Boolean = false,
        ancestryOrigin: String = "BASICO",
        // Ids de habilidade[] da raça já resolvida (ver
        // ApplyAncestryChangeCoordinatorUseCase) — usado pra decidir Armadura
        // Natural por id de traço (ARMADURA), não pelo nome da raça no
        // "when" abaixo. Vazio quando o chamador não tem essa lista (ex.:
        // testes isolados deste use case). Sem contagem de "vezes" aqui de
        // propósito: nenhuma raça oficial hoje tem Armadura Natural além de
        // +2 (1 compra) — se um dia alguma precisar de +4/+6, este cálculo
        // (e o `racialAbilityIds` que o alimenta) precisa virar Map<String,
        // Int> igual a `racialTraitIdsFromVariants` do ModifierEngine.
        racialAbilityIds: Set<String> = emptySet()
    ): Result {
        val naturalArmorFromAbilityId = RacialTraitPointCatalog.EFEITOS
            .filterValues { it is RacialTraitEffect.ArmaduraBonus }
            .entries
            .firstOrNull { (id, _) -> id in racialAbilityIds }
            ?.let { (_, efeito) -> (efeito as RacialTraitEffect.ArmaduraBonus).valor }
            ?: 0
        val ancKey = anc.keyify()
        val effectiveVariant = if (ancestryOptions.isNotEmpty()) {
            resolveAncestryVariantUseCase.execute(
                ResolveAncestryVariantUseCase.Input(
                    selectedVariant = scifiVariant,
                    legacySelectedVariant = anoesScifiSelecionado,
                    availableOptions = ancestryOptions
                )
            ).normalizedSelection
        } else {
            null
        }

        if (isSciFiActive) {
            if (ancKey == "DEADERS (PARASTEEN)" || ancKey == "DEADERS") {
                return Result(
                    naturalArmorFromRace = 0,
                    forceArmorZero = true,
                    ensureAdvantageNames = listOf("CALCULISTA"),
                    ensureAdvantageIds = emptyList(),
                    ensureAutomaticAdvantages = listOf(TraitAddition("CALCULISTA", "CALCULISTA")),
                    ensureRacialDisadvantages = emptyList(),
                    elementalAction = ElementalAction.NONE
                )
            }

            if (ancKey == "ANOES") {
                // Anões "Ciber" é Variante de verdade (o mestre reconfigura a
                // raça pro cenário) com Seleção aninhada (até 2 pontos de
                // traços negativos, catálogo em AnaoCiberTraitCatalog).
                // Resolvido via AncestryVariantRegistry em vez do "when" fixo
                // que existia aqui antes.
                return if (effectiveVariant == "Ciber") {
                    val pontosUsados = AnaoCiberTraitCatalog.pontosUsados(anaoCiberTracosSelecionados)
                    val tracosValidos = if (pontosUsados <= AnaoCiberTraitCatalog.MAX_PONTOS) {
                        anaoCiberTracosSelecionados
                    } else {
                        // Segurança: nunca aplicar uma seleção que estoure o orçamento de
                        // pontos, mesmo que algo upstream falhe em validar antes de chegar aqui.
                        emptyList()
                    }
                    val racialDisadvantages = AnaoCiberTraitCatalog.buildDesvantagens(tracosValidos).ifEmpty {
                        listOf(
                            TraitAddition(
                                "Anões Ciber: escolha até 2 pontos de traços raciais negativos (nenhum maior que -2) na ficha.",
                                "ANAO_CIBER_TRACOS_PENDENTES"
                            )
                        )
                    }
                    val catalogSelection = AncestryVariantRegistry.get("ANOES")
                        ?.grupoVariante?.opcoes?.firstOrNull { it.id == "ciber" }
                        ?.selecoes?.firstOrNull { it.id == "anao_ciber_tracos_negativos" }
                    val resolved = if (catalogSelection != null) {
                        resolveAncestryVariantPackageUseCase.resolve(
                            ancestralidadeId = "ANOES",
                            variantOptionId = "ciber",
                            selectionAnswers = emptyList(),
                            catalogPackages = mapOf(
                                catalogSelection.id to ResolvedTraitPackage(desvantagensParaAdicionar = racialDisadvantages)
                            )
                        )
                    } else {
                        ResolvedTraitPackage()
                    }
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar,
                        ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = emptyList()
                    )
                } else {
                    // Default / Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        automaticAdvantagesToRemove = listOf("CIBERTOLERÂNCIA", "CIBERTOLERANCIA"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey in AncestryVariantRegistry.scifiVariantDrivenKeys) {
                buildResultFromVariantRegistry(ancKey, effectiveVariant)?.let { return it }
            }

            if (ancKey == "ELEMENTAIS") {
                // Elementais não tem Variante — é Seleção de elemento (todo
                // elemental é de algum elemento). Resolvido via
                // AncestryVariantRegistry em vez do "when" fixo que existia
                // aqui antes.
                val def = AncestryVariantRegistry.get("ELEMENTAIS")
                    ?.selecoes?.firstOrNull { it.id == "elementais_scifi_elemento" }
                val resolved = if (def != null) {
                    resolveAncestryVariantPackageUseCase.resolve(
                        ancestralidadeId = "ELEMENTAIS",
                        variantOptionId = null,
                        selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                    )
                } else {
                    ResolvedTraitPackage()
                }
                return Result(
                    naturalArmorFromRace = 0,
                    forceArmorZero = true,
                    ensureAdvantageNames = emptyList(),
                    ensureAdvantageIds = emptyList(),
                    ensureAutomaticAdvantages = resolved.tracosParaAdicionar,
                    ensureRacialDisadvantages = emptyList(),
                    elementalAction = ElementalAction.NONE
                )
            }

        }

        if (canonicalOriginKey(ancestryOrigin) == "ARTE_DA_GUERRA" && ancKey.contains("UMVEE")) {
            // Umvee não tem Variante nenhuma — "Dom da Natureza" é Seleção de
            // pacote fixo (o jogador escolhe 1 de 6, não o mestre reconfigura
            // a raça). Resolvido via AncestryVariantRegistry em vez do "when"
            // fixo que existia aqui antes.
            val def = AncestryVariantRegistry.get("UMVEE (FILHOS DA LUA)")
                ?.selecoes?.firstOrNull { it.id == "umvee_dom_da_natureza" }
            val resolved = if (def != null) {
                resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "UMVEE (FILHOS DA LUA)",
                    variantOptionId = null,
                    selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                )
            } else {
                ResolvedTraitPackage()
            }
            return Result(
                naturalArmorFromRace = if (effectiveVariant == "Pedregoso") 2 else 0,
                forceArmorZero = true,
                ensureAdvantageNames = resolved.vantagensGratisParaAdicionar.map { it.nome },
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = resolved.vantagensGratisParaAdicionar + resolved.tracosParaAdicionar,
                ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                elementalAction = ElementalAction.NONE
            )
        }

        if (canonicalOriginKey(ancestryOrigin) == "ARTE_DA_GUERRA" && ancKey == "FERAL") {
            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = listOf("FURIOSO"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf(
                    TraitAddition("FURIOSO", "FURIOSO"),
                    TraitAddition("GARRAS", "GARRAS")
                ),
                ensureRacialDisadvantages = listOf(TraitAddition("SANGUINÁRIO", "SANGUINARIO")),
                elementalAction = ElementalAction.NONE,
                anotacoesToAdd = listOf("Feral: não pode canalizar Técnicas de Chi.")
            )
        }


        if (ancKey.contains("TERRACOTA")) {
            // Terracota não tem Variante — é Seleção de pacote fixo: todo
            // Terracota nasce com Voto OU Obrigação (Maior), o jogador só
            // escolhe qual das duas. Resolvido via AncestryVariantRegistry.
            val def = AncestryVariantRegistry.get("TERRACOTA")
                ?.selecoes?.firstOrNull { it.id == "terracota_complicacao" }
            val resolved = if (def != null) {
                resolveAncestryVariantPackageUseCase.resolve(
                    ancestralidadeId = "TERRACOTA",
                    variantOptionId = null,
                    selectionAnswers = listOf(fixedPackageAnswerFrom(def, effectiveVariant))
                )
            } else {
                ResolvedTraitPackage()
            }

            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = false,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = resolved.desvantagensParaAdicionar,
                racialDisadvantagesToRemove = listOf("Voto ou Obrigação", "VOTO_OU_OBRIGACAO", "VOTO OU OBRIGACAO"),
                elementalAction = ElementalAction.NONE
            )
        }

        if (ancKey.contains("AKAIMIMI")) {
            return Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = listOf(TraitAddition("PECULIARIDADE", "PECULIARIDADE")),
                elementalAction = ElementalAction.NONE
            )
        }

        return when (ancKey) {
            // Armadura Natural lida pelo id do traço (ARMADURA, ver
            // ancestralidades.json), não mais fixa por nome de raça — o
            // valor só é diferente de 0 quando a raça resolvida realmente
            // carrega esse traço em habilidades[].
            "SAURIOS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf(TraitAddition("PRONTIDÃO", "PRONTIDAO")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "GOLENS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DRACONIANOS" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "INSETOIDES" -> Result(
                naturalArmorFromRace = naturalArmorFromAbilityId,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf(TraitAddition("GARRAS", "GARRAS")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "PEQUENINOS" -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = listOf("Sorte", "Espirituoso"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = listOf(
                    TraitAddition("Tamanho -1", "TAMANHO_MENOS_1"),
                    TraitAddition("Movimentação Reduzida", "MOVIMENTACAO_REDUZIDA")
                ),
                elementalAction = ElementalAction.NONE
            )

            "CELESTIAIS" -> {
                if (canonicalOriginKey(ancestryOrigin) == "BASICO") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = listOf("antecedente_arcano_milagres"),
                        ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (MILAGRES)", "ANTECEDENTE_ARCANO_MILAGRES")),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            "HUMANO (WISEGUYS)".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = false,
                ensureAdvantageNames = listOf("Conexões (Máfia)"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DESCENDENTE ELEMENTAL".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                automaticAdvantagesToRemove = if (descendenteElementalSelecionado?.equals("Fogo", ignoreCase = true) == true) {
                    emptyList()
                } else {
                    listOf("RAPIDO", "RÁPIDO", "rapido")
                },
                ensureRacialDisadvantages = emptyList(),
                elementalAction = if (descendenteElementalSelecionado == null) {
                    ElementalAction.SELECT_DEFAULT
                } else {
                    ElementalAction.REAPPLY_CURRENT
                }
            )

            "TRANSMORFOS".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("antecedente_arcano_dom"),
                ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (DOM)", "ANTECEDENTE_ARCANO_DOM")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DEMÔNIO (ABISMO)".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("aa_demonio"),
                ensureAutomaticAdvantages = listOf(TraitAddition("ANTECEDENTE ARCANO (DEMÔNIO)", "ANTECEDENTE_ARCANO_DEMONIO")),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            else -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )
        }
    }
}
