package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.util.keyify

class ResolveAncestrySpecificAdjustmentsUseCase {

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
        val ensureRacialDisadvantages: List<String>,
        val elementalAction: ElementalAction,
        val anotacoesToAdd: List<String> = emptyList()
    )

    fun execute(
        anc: String,
        descendenteElementalSelecionado: String?,
        anoesScifiSelecionado: String? = null,
        scifiVariant: String? = null,
        humanoMineradorAtributo: String? = null,
        isSciFiActive: Boolean = false
    ): Result {
        val ancKey = anc.keyify()

        if (isSciFiActive) {
            // Anões Logic (using unified scifiVariant or fallback to anoesScifiSelecionado for compatibility)
            // Ideally anoesScifiSelecionado should be migrated to scifiVariant in State, but handling both here for now or just checking variant.
            // Assuming State manages to set scifiVariant for new selections.
            val effectiveVariant = scifiVariant ?: anoesScifiSelecionado

            if (ancKey == "ANOES") {
                return if (effectiveVariant == "Cyber") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("CIBERTOLERÂNCIA"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = listOf("Anões Cyber: Combinar com o Mestre 2 pontos em habilidades negativas apropriadas ao cenário.")
                    )
                } else {
                    // Default / Básico
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("GANANCIOSO"),
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
                        ensureAutomaticAdvantages = listOf("CURIOSO"), // Injected as Edge/Ability name (text only if no definition)
                        ensureRacialDisadvantages = emptyList(),
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
                        ensureRacialDisadvantages = listOf("SANGUINÁRIO"),
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
                        ensureAutomaticAdvantages = listOf("AQUÁTICO", "RESISTÊNCIA"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "AVIANOS") {
                return if (effectiveVariant == "Ave de rapina") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("SENTIDOS AGUÇADOS (Olhos de Águia)"),
                        ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE BAIXA", "FORMA ALIENÍGENA"),
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
                        ensureRacialDisadvantages = listOf("TRANSTORNO DE SEPARAÇÃO"),
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
                        ensureRacialDisadvantages = listOf("DESASTRADO"),
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
                            ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE BAIXA"),
                            elementalAction = ElementalAction.NONE
                        )
                        "Minerador" -> {
                            val choice = humanoMineradorAtributo ?: "Força"
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
                                ensureAutomaticAdvantages = listOf("ADAPTÁVEL"),
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
                        ensureAutomaticAdvantages = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "DRAKENS") {
                return if (effectiveVariant == "Dragão") {
                    Result(
                        naturalArmorFromRace = 2, // Default armor
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
                        naturalArmorFromRace = 2,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORTE"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                }
            }

            if (ancKey == "ELEMENTAIS") {
                return if (effectiveVariant == "Ar, Fogo ou Água") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORMA DE ENERGIA"),
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

            if (ancKey == "FERAIS") {
                return if (effectiveVariant == "Menor") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("DIMINUTO (Tamanho -4)"),
                        ensureRacialDisadvantages = listOf("TRANSTORNO DE SEPARAÇÃO"),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("DIMINUTO (Tamanho -3)", "ESPIRITUOSO"),
                        ensureRacialDisadvantages = listOf("ALTA TECNOLOGIA (Maior)"),
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
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("HABITANTE DE GRAVIDADE BAIXA/ZERO"),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("FORTE"),
                        ensureRacialDisadvantages = listOf("DEPENDÊNCIA ATMOSFÉRICA"),
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
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = listOf("NOÇÃO DO PERIGO"),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
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
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = listOf("Possessores Energia: Combinar com o Mestre e equilibrar com 4 pontos de habilidades negativas.")
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

            if (ancKey == "QUADROIDES") {
                return if (effectiveVariant == "Habilidoso") {
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("AÇÃO ADICIONAL (Ignora 2 pontos de penalidade por Ações Múltiplas)"),
                        ensureRacialDisadvantages = emptyList(),
                        elementalAction = ElementalAction.NONE,
                        anotacoesToAdd = listOf("Quadroides Habilidoso: Equilibre com uma habilidade -1, combine com o mestre de jogo.")
                    )
                } else {
                    // Padrão
                    Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = listOf("AÇÃO ADICIONAL (Física)"),
                        ensureRacialDisadvantages = emptyList(),
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
                        elementalAction = ElementalAction.NONE
                    )
                    "Máquina (Forasteiro)" -> Result(
                        naturalArmorFromRace = 0,
                        forceArmorZero = true,
                        ensureAdvantageNames = emptyList(),
                        ensureAdvantageIds = emptyList(),
                        ensureAutomaticAdvantages = emptyList(),
                        ensureRacialDisadvantages = listOf("FORASTEIRO (Maior)"),
                        elementalAction = ElementalAction.NONE
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
                        ensureAutomaticAdvantages = emptyList(),
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
                        ensureAutomaticAdvantages = emptyList(),
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
                        ensureRacialDisadvantages = listOf("DEPENDÊNCIA (deve estar em temperaturas abaixo de zero por pelo menos uma hora por dia)"),
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

            "CELESTIAIS" -> Result(
                naturalArmorFromRace = 0,
                forceArmorZero = true,
                ensureAdvantageNames = emptyList(),
                ensureAdvantageIds = listOf("antecedente_arcano_milagres"),
                ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (MILAGRES)"),
                ensureRacialDisadvantages = emptyList(),
                elementalAction = ElementalAction.NONE
            )

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
