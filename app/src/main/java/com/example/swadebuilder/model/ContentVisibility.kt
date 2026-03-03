package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.rules.RulesResolver

/**
 * Centralizes logic for determining content visibility based on active compendiums.
 */


private fun CriadorState.resolveScenarioRules() = RulesResolver().resolve(
    compendioPathfinderAtivo = compendioPathfinderAtivo,
    compendioSciFiAtivo = compendioSciFiAtivo,
    compendioDeadlandsAtivo = compendioDeadlandsAtivo,
    compendioFantasiaAtivo = compendioFantasiaAtivo,
    compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
    compendioHorrorAtivo = compendioHorrorAtivo,
    compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
    compendioCidadeSolVaporAtivo = compendioCidadeSolVaporAtivo,
    compendioWiseguysAtivo = compendioWiseguysAtivo
)

fun CriadorState.getActiveOrigins(): Set<String> = buildSet {
    // 1. Add active compendiums to the set
    if (compendioFantasiaAtivo) add("FANTASIA")
    if (compendioSciFiAtivo) add("SCI_FI")
    if (modoSupers) add("SUPER")
    if (compendioHorrorAtivo) add("HORROR")
    if (compendioPathfinderAtivo) add("PATHFINDER")
    if (compendioDeadlandsAtivo) add("DEADLANDS")
    if (compendioArteDaGuerraAtivo) add("ARTE_DA_GUERRA")
    if (compendioCidadeSolVaporAtivo) {
        add("CIDADE_SOL_VAPOR")
    }
    if (compendioWiseguysAtivo) add("WISEGUYS")
    if (compendioCrystalHeartAtivo) add("CRYSTAL_HEART")

    // 2. Determine if "BASICO" should be included
    // "Replacement Settings" are those that provide their own dataset for section content.
    // When any compendium book is active, BASICO is excluded from origin visibility
    // and each section should be built from active book JSON files.
    val replacementSettingsActive =
        compendioFantasiaAtivo ||
        compendioHorrorAtivo ||
        compendioSciFiAtivo ||
        compendioPathfinderAtivo ||
        compendioDeadlandsAtivo ||
        compendioCrystalHeartAtivo ||
        compendioArteDaGuerraAtivo ||
        compendioCidadeSolVaporAtivo ||
        compendioWiseguysAtivo

    if (!replacementSettingsActive) {
        add("BASICO")
    }
}

fun CriadorState.isComplicacaoVisible(
    comp: Complicacao,
    activeOrigins: Set<String> = getActiveOrigins()
): Boolean {
    val origemSafe = canonicalOriginKey(comp.origem)
    return origemSafe in activeOrigins
}

fun CriadorState.isVantagemVisible(
    vant: Vantagem,
    multiplosAAHabilitados: Boolean
): Boolean {
    return evaluateVantagemVisibility(vant, multiplosAAHabilitados).visible
}

fun CriadorState.explainVantagemVisibility(
    vant: Vantagem,
    multiplosAAHabilitados: Boolean
): String {
    return evaluateVantagemVisibility(vant, multiplosAAHabilitados).reason
}

private data class VantagemVisibilityDecision(
    val visible: Boolean,
    val reason: String
)

private fun CriadorState.evaluateVantagemVisibility(
    vant: Vantagem,
    multiplosAAHabilitados: Boolean
): VantagemVisibilityDecision {
    val activeOrigins = getActiveOrigins()
    val selectedRules = resolveScenarioRules()
    val origemNorm = canonicalOriginKey(vant.origem)

    val isGenericAB = vant.id == "antecedente_arcano"
    val isSpecificAB = (vant.id.startsWith("antecedente_arcano_") || vant.id.startsWith("aa_"))

    // 1. Basic Origin Check
    // If the advantage's origin is not in the active set, hide it.
    if (origemNorm !in activeOrigins) {
        // Exception: cenários que usam o seletor genérico de AA (ex.: Pathfinder, Cidade do Sol a Vapor)
        if (isGenericAB && selectedRules.allowsGenericArcaneSelector()) {
            // Allow it
        } else {
            return VantagemVisibilityDecision(false, "blocked_origin:$origemNorm")
        }
    }

    // 2. Specific Item Logic (Forbidden items within an active setting)

    // Monstrous Advantages (Horror) - Require Monster Rule
    if (vant.categoria == Categoria.MONSTRUOSAS && !modoMonstroAtivo) {
        return VantagemVisibilityDecision(false, "blocked_monstruosa_without_monster_mode")
    }

    // Scenario strategy policies (Fase 4)
    if (selectedRules.blocksArcaneBackgrounds()) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return VantagemVisibilityDecision(false, "blocked_arcane_backgrounds_by_rules")
        }
    }

    if (selectedRules.hidePowerCategoryAdvantagesExceptMysticPowers()) {
        if (vant.categoria == Categoria.PODER && vant.id != "poderes_misticos") {
            return VantagemVisibilityDecision(false, "blocked_power_category_by_rules")
        }
    }

    if (vant.id in selectedRules.forbiddenAdvantageIds()) {
        return VantagemVisibilityDecision(false, "blocked_forbidden_id_by_rules:${vant.id}")
    }


    // Supers Logic
    if (modoSupers) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return VantagemVisibilityDecision(false, "blocked_supers_arcane")
        }
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
            return VantagemVisibilityDecision(false, "blocked_supers_resistencia_arcana")
        }
        if (vant.categoria == Categoria.PODER) {
            return VantagemVisibilityDecision(false, "blocked_supers_power_category")
        }
        if (vant.requisitos.vantagensPrevias.contains("antecedente_arcano") ||
            vant.id == "superpoderes"
        ) {
            return VantagemVisibilityDecision(false, "blocked_supers_arcane_requirement")
        }
    }

    // 3. Arcane Background UI Logic


    // Rule-driven generic selector policy
    // Em cenários que usam seletor genérico, ele deve ser forçado apenas quando múltiplos AA estão desabilitados.
    if (selectedRules.allowsGenericArcaneSelector() && !multiplosAAHabilitados) {
        val isPathfinderArcaneSelectorOption =
            compendioPathfinderAtivo &&
                (vant.id == "antecedente_arcano_magia_pf" || vant.id == "antecedente_arcano_milagres_pf")

        if (isSpecificAB && !isPathfinderArcaneSelectorOption) {
            return VantagemVisibilityDecision(false, "blocked_specific_ab_generic_selector_rule")
        }
        if (isGenericAB || isPathfinderArcaneSelectorOption) {
            return VantagemVisibilityDecision(true, "visible_generic_ab_generic_selector_rule")
        }
    }

    if (!multiplosAAHabilitados) {
        // If Multiple ABs DISABLED: Show ONLY the Generic AB (Hide specific ones)
        if (isSpecificAB) return VantagemVisibilityDecision(false, "blocked_specific_ab_when_multi_disabled")
    } else {
        // If Multiple ABs ENABLED: Show Specific ABs (Hide the Generic one)
        if (isGenericAB) return VantagemVisibilityDecision(false, "blocked_generic_ab_when_multi_enabled")
    }

    return VantagemVisibilityDecision(true, "visible")
}
