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
                        ensureAutomaticAdvantages = listOf("AQUÁTICO"),
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
        }

        return when (ancKey) {
            "SAURIOS" -> Result(
                naturalArmorFromRace = 2,
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
