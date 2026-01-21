package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.semAcentos

/**
 * Centralizes logic for determining content visibility based on active compendiums.
 */

fun CriadorState.getActiveOrigins(): Set<String> = buildSet {
    val basicReplaced = compendioFantasiaAtivo || compendioSciFiAtivo
    if (compendioFantasiaAtivo) {
        add("FANTASIA")
    }
    if (compendioSciFiAtivo) {
        add("SCI_FI")
    }
    if (!basicReplaced) {
        add("BASICO")
    }
    if (modoSupers) add("SUPER")
    if (compendioHorrorAtivo) add("HORROR")
    if (compendioPathfinderAtivo) add("PATHFINDER")
    if (compendioDeadlandsAtivo) add("DEADLANDS")
    if (compendioArteDaGuerraAtivo) add("ARTE_DA_GUERRA")
    if (compendioCidadeSolVaporAtivo) add("CIDADE_SOL_VAPOR")
    if (compendioWiseguysAtivo) add("WISEGUYS")
    if (compendioCrystalHeartAtivo) add("CRYSTAL_HEART")
}

fun CriadorState.isComplicacaoVisible(
    comp: Complicacao,
    activeOrigins: Set<String> = getActiveOrigins()
): Boolean {
    val origemSafe = if (comp.origem.isBlank()) "BASICO" else comp.origem.uppercase().semAcentos().trim()
    return origemSafe in activeOrigins
}

fun CriadorState.isVantagemVisible(
    vant: Vantagem,
    multiplosAAHabilitados: Boolean
): Boolean {
    val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase().semAcentos()
    val isBasico = origemNorm == "BASICO"
    val isSuper = origemNorm == "SUPER"
    val isFantasia = origemNorm == "FANTASIA"
    val isHorror = origemNorm == "HORROR"
    val isBuscatrilha = origemNorm == "PATHFINDER"
    val isDeadlands = origemNorm == "DEADLANDS"
    val isAdg = origemNorm == "ARTE_DA_GUERRA"
    val isCidadeSolVapor = origemNorm == "CIDADE_SOL_VAPOR"
    val isWiseguys = origemNorm == "WISEGUYS"
    val isCrystalHeart = origemNorm == "CRYSTAL_HEART"
    val isSciFi = origemNorm == "SCI_FI"

    // 1. Check Compendium Activation Logic
    if (compendioCrystalHeartAtivo) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return false
        }
    }

    if (compendioPathfinderAtivo) {
        val forbiddenIds = setOf(
            "antecedente_arcano_ciencia_estranha",
            "antecedente_arcano_psionicos",
            "antecedente_arcano_dom",
            "rico",
            "podre_de_rico"
        )
        if (vant.id in forbiddenIds) return false
    }

    if (compendioArteDaGuerraAtivo) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return false
        }
        if (vant.categoria == Categoria.PODER) {
            return false
        }
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
            return false
        }
    }

    if (compendioWiseguysAtivo) {
        if (vant.categoria == Categoria.PODER) {
            return false
        }
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
            return false
        }
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return false
        }
        val forbiddenIds = setOf("aristocrata", "chi", "campeao", "matador_de_gigantes", "corajoso")
        if (vant.id in forbiddenIds) {
            return false
        }
    }

    val isActive = (isBasico && !compendioFantasiaAtivo && !compendioDeadlandsAtivo) ||
            (isAdg && compendioArteDaGuerraAtivo) ||
            (isSuper && modoSupers) ||
            (isFantasia && compendioFantasiaAtivo) ||
            (isHorror && compendioHorrorAtivo) ||
            (isBuscatrilha && compendioPathfinderAtivo) ||
            (isDeadlands && compendioDeadlandsAtivo) ||
            (isCidadeSolVapor && compendioCidadeSolVaporAtivo) ||
            (isWiseguys && compendioWiseguysAtivo) ||
            (isCrystalHeart && compendioCrystalHeartAtivo) ||
            (isSciFi && compendioSciFiAtivo)

    if (!isActive) return false

    // 2. Supers Logic
    if (modoSupers) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) return false
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") return false
        if (vant.categoria == Categoria.PODER) return false
        if (vant.requisitos.vantagensPrevias.contains("antecedente_arcano") ||
            vant.id == "superpoderes") return false
    }

    // 3. Arcane Background Visibility
    val isGenericAB = vant.id == "antecedente_arcano"
    val isSpecificAB = (vant.id.startsWith("antecedente_arcano_") || vant.id.startsWith("aa_"))

    if (!multiplosAAHabilitados) {
        // If Multiple ABs DISABLED: Show ONLY the Generic AB (Hide specific ones)
        if (isSpecificAB) return false
    } else {
        // If Multiple ABs ENABLED: Show Specific ABs (Hide the Generic one)
        if (isGenericAB) return false
    }

    return true
}
