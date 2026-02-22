package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
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
        val anotacoesToAdd: List<String> = emptyList(),
        val racialDisadvantagesToRemove: List<String> = emptyList()
    )

    fun execute(
        anc: String,
        ancestralidadeDef: RacialModifier?,
        descendenteElementalSelecionado: String?,
        anoesScifiSelecionado: String? = null,
        scifiVariant: String? = null,
        humanoMineradorAtributo: String? = null,
        isSciFiActive: Boolean = false
    ): Result {
        val ancKey = anc.keyify()
        val variant = scifiVariant ?: anoesScifiSelecionado

        // Determine Base Armor from Definition (Data-Driven)
        var naturalArmor = ancestralidadeDef?.naturalArmorBonus ?: 0
        var forceZero = ancestralidadeDef?.forceArmorZero ?: false

        // Determine Annotations from Variant (Data-Driven)
        val activeVariant = ancestralidadeDef?.variantes?.find { it.nome == variant }
        val notes = activeVariant?.anotacoes ?: emptyList()

        // Legacy Fallback for Fantasy/Base races not yet migrated to JSON fields
        // (Golens, Draconianos, etc. in Fantasy/Base don't have naturalArmorBonus set in JSON yet)
        val origin = ancestralidadeDef?.origem?.uppercase() ?: "BASICO"
        val isLegacyOrigin = origin != "FC" && origin != "SCI_FI" && origin != "SCIFI"

        if (isLegacyOrigin) {
            when (ancKey) {
                "SAURIOS", "GOLENS", "DRACONIANOS", "INSETOIDES" -> {
                    naturalArmor = 2
                    forceZero = true
                }
            }
        }

        // Base Result
        var result = Result(
            naturalArmorFromRace = naturalArmor,
            forceArmorZero = forceZero,
            ensureAdvantageNames = emptyList(),
            ensureAdvantageIds = emptyList(),
            ensureAutomaticAdvantages = emptyList(),
            ensureRacialDisadvantages = emptyList(),
            elementalAction = ElementalAction.NONE,
            anotacoesToAdd = notes
        )

        // Legacy/Special Logic Checks
        when (ancKey) {
            "SAURIOS" -> {
                if (isLegacyOrigin) {
                    result = result.copy(ensureAdvantageNames = listOf("PRONTIDÃO"))
                }
            }
            "INSETOIDES" -> {
                if (isLegacyOrigin) {
                    result = result.copy(ensureAutomaticAdvantages = listOf("GARRAS"))
                }
            }
            "PEQUENINOS" -> {
                result = result.copy(
                    ensureAdvantageNames = listOf("Sorte", "Espirituoso"),
                    ensureRacialDisadvantages = listOf("Tamanho -1", "Movimentação Reduzida")
                )
            }
            "CELESTIAIS" -> {
                result = result.copy(
                    ensureAdvantageIds = listOf("antecedente_arcano_milagres"),
                    ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (MILAGRES)")
                )
            }
            "HUMANO (WISEGUYS)".keyify() -> {
                result = result.copy(
                    ensureAdvantageNames = listOf("Conexões (Máfia)")
                )
            }
            "DESCENDENTE ELEMENTAL".keyify() -> {
                result = result.copy(
                    elementalAction = if (descendenteElementalSelecionado == null) {
                        ElementalAction.SELECT_DEFAULT
                    } else {
                        ElementalAction.REAPPLY_CURRENT
                    }
                )
            }
            "TRANSMORFOS".keyify() -> {
                result = result.copy(
                    ensureAdvantageIds = listOf("antecedente_arcano_dom"),
                    ensureAutomaticAdvantages = listOf("ANTECEDENTE ARCANO (DOM)")
                )
            }
        }

        // Sci-Fi Default Selection Logic (Only if no variant selected)
        if (isSciFiActive && variant == null) {
             val hasOptions = ancKey == "ANOES" || ancKey == "AQUARIANOS" || ancKey == "AVIANOS" || ancKey == "ELFOS" || ancKey == "HUMANOS"
             if (hasOptions) {
                 result = result.copy(elementalAction = ElementalAction.SELECT_DEFAULT)
             }
        }

        return result
    }
}
