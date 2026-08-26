package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AnaoCiberTraitCatalog
import com.example.swadebuilder.model.AnaoCiberTraitSelection
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import com.example.swadebuilder.model.SelectionDef
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

    enum class ElementalAction {
        NONE,
        SELECT_DEFAULT,
        REAPPLY_CURRENT
    }

    data class Result(
        val naturalArmorFromRace: Int,
        val forceArmorZero: Boolean,
        val ensureAdvantageNames: List<String>,
        val ensureAdvantageIds: List<String>,
        val ensureAutomaticAdvantages: List<String>,
        val automaticAdvantagesToRemove: List<String> = emptyList(),
        val ensureRacialDisadvantages: List<String>,
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
        ancestryOrigin: String = "BASICO"
    ): Result {
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
                    ensureAutomaticAdvantages = listOf("CALCULISTA"),
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
                        listOf("Anões Ciber: escolha até 2 pontos de traços raciais negativos (nenhum maior que -2) na ficha.")
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
                        ensureAdvantageNames = resolved.vantagensGratisParaAdicionar,
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

            if (ancKey == "RAKASHANOS") {
                return if (effectiveVariant == "Brincalhão") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("CURIOSO (Maior)"),
                        elementalAction = ElementalAction.NONE,
                        racialDisadvantagesToRemove = listOf("SANGUINÁRIO", "SANGUINÁRIO (Maior)")
                    )
                } else {
                    // Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("SANGUINÁRIO (Maior)"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "SAURIOS") {
                return if (effectiveVariant == "Cuspidor") {
                    Result(
                        naturalArmorFromRace = 2, // Keeps Armor
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("TOQUE VENENOSO (Cuspidor)"), // Text only trait
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Básico
                    Result(
                        naturalArmorFromRace = 2, // Keeps Armor
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("PRONTIDÃO"),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("MORDIDA"), // Natural Weapon
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "AQUARIANOS") {
                return if (effectiveVariant == "Semi-aquáticos") {
                    Result(
                        naturalArmorFromRace = 0, // Assume no Natural Armor change unless stated
                        forceArmorZero = true, // To avoid stacking issues if any
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("SEMIAQUÁTICO", "TOQUE VENENOSO"),
                        automaticAdvantagesToRemove = listOf("AQUÁTICO", "RESISTÊNCIA"),
                        ensureRacialDisadvantages = emptyList(), // Dependency is in JSON
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Básico
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

            if (ancKey == "AVIANOS") {
                return if (effectiveVariant.equals("Ave de rapina", ignoreCase = true)) {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        automaticAdvantagesToRemove = listOf("FRÁGIL", "FRAGIL", "NÃO SABE NADAR", "NAO SABE NADAR"),
                        ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE ZERO/BAIXA", "FORMA ALIENÍGENA", "SENTIDOS AGUÇADOS (Olhos de Águia)"),
                        elementalAction = ElementalAction.NONE,
                        racialDisadvantagesToRemove = listOf("NÃO SABE NADAR", "NÃO SABE NADAR (Menor)", "FRÁGIL")
                    )
                } else {
                    // Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("FRÁGIL", "NÃO SABE NADAR"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "ELFOS") {
                return if (effectiveVariant == "Comunitário") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("COMUNITÁRIO"),
                        automaticAdvantagesToRemove = listOf("DESASTRADO"),
                        ensureRacialDisadvantages = listOf("TRANSTORNO DE SEPARAÇÃO"),
                        elementalAction = ElementalAction.NONE,
                        racialDisadvantagesToRemove = listOf("DESASTRADO", "DESASTRADO (Menor)")
                    )
                } else {
                    // Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("DESASTRADO (Menor)"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey.contains("HUMANO")) {
                // Check if it's the specific SciFi Human entry or generic.
                // Assuming "HUMANOS" key.
                if (ancKey == "HUMANOS") {
                    return when (effectiveVariant) {
                        "Baixa Gravidade" -> Result(
                            naturalArmorFromRace = 0,
                            forceArmorZero = true,
                            ensureAdvantageNames = emptyList(),
                            ensureAdvantageIds = emptyList(),
                            ensureAutomaticAdvantages = emptyList(), // Agility d6 handled in Attribute Logic
                            automaticAdvantagesToRemove = listOf("ADAPTÁVEL", "ADAPTAVEL"),
                            ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE BAIXA"),
                            elementalAction = ElementalAction.NONE
                        )
                        "Minerador" -> {
                            Result(
                                naturalArmorFromRace = 0,
                                forceArmorZero = true,
                                ensureAdvantageNames = emptyList(),
                                ensureAdvantageIds = emptyList(),
                                ensureAutomaticAdvantages = emptyList(), // Attribute handled logic
                                ensureRacialDisadvantages = listOf("DEPENDÊNCIA ATMOSFÉRICA (Maior)"),
                                elementalAction = ElementalAction.NONE
                            )
                        }
                        else -> { // Padrão
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
                }
            }

            if (ancKey == "CENTAUX") {
                return if (effectiveVariant == "Gazela") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("MOVIMENTAÇÃO +4"),
                        automaticAdvantagesToRemove = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                        ensureRacialDisadvantages = emptyList(),
                        racialDisadvantagesToRemove = listOf("GRANDE"),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "DRAKENS") {
                return if (effectiveVariant == "Dragão") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("ARMA DE SOPRO (Fogo)"), // Text trait
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORTE", "RESISTÊNCIA +2"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
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

            if (ancKey == "FERAIS") {
                return if (effectiveVariant == "Menor") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("DIMINUTO (Tamanho -4)"),
                        ensureRacialDisadvantages = listOf("TRANSTORNO DE SEPARAÇÃO"),
                        elementalAction = ElementalAction.NONE,
                        automaticAdvantagesToRemove = listOf("ESPIRITUOSO"),
                        racialDisadvantagesToRemove = listOf("ALTA/BAIXA TECNOLOGIA", "ALTA/BAIXA TECNOLOGIA (Maior)")
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("DIMINUTO (Tamanho -3)"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "FLORANS") {
                return if (effectiveVariant == "Defensivo") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("TOQUE VENENOSO (Paralisante)"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("ROBUSTO"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "GELATINOIDES") {
                return if (effectiveVariant == "Ameba") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("CAMUFLAGEM"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("REGENERAÇÃO"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "INSETOIDES") {
                return if (effectiveVariant == "Vespa") {
                    Result(
                        naturalArmorFromRace = 0, // No Armor +2
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FERRÃO (Mordida For+d4)", "VOO (Movimentação 6)", "TOQUE VENENOSO (Moderado)"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 2,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("ARMADURA +2", "GARRAS"), // For+d4 will be handled in extrairArmasNaturais logic update or default if needed
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "MIMICOS" || ancKey == "MÍMICOS") {
                return if (effectiveVariant == "Resistente") {
                    Result(
                        naturalArmorFromRace = 0, // No specific Armor trait mentioned, but Resistance +1. Usually handled via TOUGHNESS modifier or manually.
                        // "aumentando sua Resistência em +1". If not via Armor, maybe via Toughness bonus logic in State or just a trait "RESISTENTE".
                        // Assuming "RESISTÊNCIA +1" trait string handles it via ModifierEngine if mapped, or just textual.
                        // Standard Mimics have nothing special? They have "Mudança de Forma".
                        // Variant text: "MUDANÇA DE FORMA: ... sem variação de Tamanho."
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("RESISTÊNCIA +1", "MUDANÇA DE FORMA (Sem variação de tamanho)"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("MUDANÇA DE FORMA"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "MINERADORES GENETICOS" || ancKey == "MINERADORES GENÉTICOS") {
                return if (effectiveVariant == "Zero G") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("ADAPTAÇÃO GRAVITACIONAL"),
                        ensureAdvantageIds = listOf("adaptacao_gravitacional"),
                        ensureAutomaticAdvantages = listOf("ADAPTAÇÃO GRAVITACIONAL"),
                        automaticAdvantagesToRemove = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA"),
                        ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE ZERO/BAIXA (Maior)"),
                        racialDisadvantagesToRemove = listOf("DEPENDÊNCIA ATMOSFÉRICA", "DEPENDÊNCIA ATMOSFÉRICA (Maior)"),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA"),
                        ensureRacialDisadvantages = emptyList(), // Removed from complications
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "ORACULOS" || ancKey == "ORÁCULOS") {
                return if (effectiveVariant == "Aterrorizado") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = listOf("poderes_misticos"), // Handled in CriadorState to set choice to Telepata
                        ensureAutomaticAdvantages = listOf("PODERES MÍSTICOS (TELEPATA)"),
                        automaticAdvantagesToRemove = listOf("NOÇÃO DO PERIGO", "NOCAO_DO_PERIGO"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("NOÇÃO DO PERIGO"),
                        ensureAdvantageIds = listOf("nocao_do_perigo"),
                        ensureAutomaticAdvantages = listOf("NOÇÃO DO PERIGO"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "POSSESSORES") {
                return if (effectiveVariant == "Energia") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORMA DE ENERGIA"),
                        automaticAdvantagesToRemove = listOf("NOÇÃO DO PERIGO", "NOCAO DO PERIGO"),
                        ensureRacialDisadvantages = listOf("Combine com o mestre de jogo para equilibrar com 4 pontos de habilidades negativas que façam sentido\nno cenário."),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        automaticAdvantagesToRemove = listOf("NOÇÃO DO PERIGO", "NOCAO DO PERIGO"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "QUADROIDES") {
                return if (effectiveVariant == "Habilidoso") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("AÇÃO ADICIONAL (Ignora 2 pontos de penalidade por Ações Múltiplas)"),
                        ensureRacialDisadvantages = listOf(
                            "SENSÍVEL (Maior)",
                            "Combine com o mestre de jogo para equilibrar com 1 ponto de habilidade negativa que faça sentido ao cenário."
                        ),
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = emptyList()
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("AÇÃO ADICIONAL (Física)"),
                        ensureRacialDisadvantages = listOf("SENSÍVEL (Maior)"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "ROBOS" || ancKey == "ROBÔS") {
                return when (effectiveVariant) {
                    "Guerreiro" -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("SEM ESCRÚPULOS (Maior)", "PROGRAMADO (Maior)"),
                        elementalAction = ElementalAction.NONE
                    )
                    "Limitado" -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("PACIFISTA (Maior)", "PROGRAMADO (Maior)"),
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = listOf("Robôs Limitado: Combine com o mestre compensação de Perícias Reduzidas.")
                    )
                    else -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("PACIFISTA (Maior)", "PROGRAMADO (Maior)"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "SERES SINTETICOS" || ancKey == "SERES SINTÉTICOS") {
                return when (effectiveVariant) {
                    "Máquina (Procurado)" -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("PROCURADO (Maior)"),
                        elementalAction = ElementalAction.NONE,
                        racialDisadvantagesToRemove = listOf("PROGRAMADO (Maior)")
                    )
                    "Máquina (Forasteiro)" -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("FORASTEIRO (Maior)"),
                        elementalAction = ElementalAction.NONE,
                        racialDisadvantagesToRemove = listOf("PROGRAMADO (Maior)")
                    )
                    else -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("PROGRAMADO"),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "SOLDADOS GENETICOS" || ancKey == "SOLDADOS GENÉTICOS") {
                return if (effectiveVariant == "Fuzileiro Zero G") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("ADAPTAÇÃO GRAVITACIONAL", "REFLEXOS DE COMBATE"),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("ADAPTAÇÃO GRAVITACIONAL", "REFLEXOS DE COMBATE"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("NERVOS DE AÇO", "REFLEXOS DE COMBATE"),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("NERVOS DE AÇO", "REFLEXOS DE COMBATE"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "YETIS") {
                return if (effectiveVariant == "Sopro") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("ARMA DE SOPRO (Frio)"),
                        ensureRacialDisadvantages = listOf("DEPENDÊNCIA"),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
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
                ensureAdvantageNames = resolved.vantagensGratisParaAdicionar,
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
                ensureAutomaticAdvantages = listOf("FURIOSO", "GARRAS"),
                ensureRacialDisadvantages = listOf("SANGUINÁRIO"),
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
                ensureRacialDisadvantages = listOf("PECULIARIDADE"),
                elementalAction = ElementalAction.NONE
            )
        }

        return when (ancKey) {
            "SAURIOS" -> Result(
                naturalArmorFromRace = 2,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf("PRONTIDÃO"),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "GOLENS" -> Result(
                naturalArmorFromRace = 2,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DRACONIANOS" -> Result(
                naturalArmorFromRace = 2,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "INSETOIDES" -> Result(
                naturalArmorFromRace = 2,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = listOf("GARRAS"),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "PEQUENINOS" -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = listOf("Sorte", "Espirituoso"),
                ensureAdvantageIds = emptyList(),
                ensureAutomaticAdvantages = emptyList(),
                ensureRacialDisadvantages = listOf("Tamanho -1", "Movimentação Reduzida"),
                elementalAction = ElementalAction.NONE
            )

            "CELESTIAIS" -> {
                if (canonicalOriginKey(ancestryOrigin) == "BASICO") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = listOf("antecedente_arcano_milagres"),
                        ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (MILAGRES)"),
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
                ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (DOM)"),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

            "DEMÔNIO (ABISMO)".keyify() -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("aa_demonio"),
                ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (DEMÔNIO)"),
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
