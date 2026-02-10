package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.semAcentos

/**
 * Centralizes logic for determining content visibility based on active compendiums.
 */

fun CriadorState.getActiveOrigins(): Set<String> = buildSet {
    // 1. Add active compendiums to the set
    if (compendioFantasiaAtivo) add("FANTASIA")
    if (compendioSciFiAtivo) add("SCI_FI")
    if (modoSupers) add("SUPER")
    if (compendioHorrorAtivo) add("HORROR")
    if (compendioPathfinderAtivo) add("PATHFINDER")
    if (compendioDeadlandsAtivo) add("DEADLANDS")
    if (compendioArteDaGuerraAtivo) add("ARTE_DA_GUERRA")
    if (compendioCidadeSolVaporAtivo) add("CIDADE_SOL_VAPOR")
    if (compendioWiseguysAtivo) add("WISEGUYS")
    if (compendioCrystalHeartAtivo) add("CRYSTAL_HEART")

    // 2. Determine if "BASICO" should be included
    // "Replacement Settings" are those that provide their own full set of core rules/content,
    // intending to replace the Basic book rather than just add to it.
    // Sci-Fi and Horror are technically Companions (Add-ons), so we keep BASICO active for them.
    // Priority logic in CriadorState handles deduplication if the add-on provides specific versions.
    val replacementSettingsActive =
        compendioPathfinderAtivo ||
        compendioDeadlandsAtivo ||
        compendioArteDaGuerraAtivo ||
        compendioHorrorAtivo ||
        compendioCidadeSolVaporAtivo

    if (!replacementSettingsActive) {
        add("BASICO")
    }
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
    val activeOrigins = getActiveOrigins()
    val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase().semAcentos().trim()

    // 1. Basic Origin Check
    // If the advantage's origin is not in the active set, hide it.
    if (origemNorm !in activeOrigins) {
        // Exception: Pathfinder uses the Generic "Antecedente Arcano" (from Basic) as a selector.
        if (compendioPathfinderAtivo && vant.id == "antecedente_arcano") {
            // Allow it
        } else {
            return false
        }
    }

    // 2. Specific Item Logic (Forbidden items within an active setting)

    // Monstrous Advantages (Horror) - Require Monster Rule
    if (vant.categoria == Categoria.MONSTRUOSAS && !modoMonstroAtivo) {
        return false
    }

    // Crystal Heart Logic
    if (compendioCrystalHeartAtivo) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            // Only specific Crystal Heart ABs allowed (handled by dataset, but hiding generics here)
            // If Generic AB is BASICO and BASICO is active (it is, for CH), we might want to hide it if CH forbids it.
            return false
        }
    }

    // Pathfinder Logic (Redundant if BASICO is excluded, but kept for safety/specific exclusions within Pathfinder set)
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

    // Arte da Guerra Logic
    if (compendioArteDaGuerraAtivo) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) {
            return false
        }
        if (vant.categoria == Categoria.PODER && vant.id != "poderes_misticos") {
            return false
        }
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") {
            return false
        }
    }

    // Wiseguys Logic
    if (compendioWiseguysAtivo) {
        if (vant.categoria == Categoria.PODER && vant.id != "poderes_misticos") {
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

    // Supers Logic
    if (modoSupers) {
        if (vant.id.startsWith("antecedente_arcano") || vant.id.startsWith("aa_")) return false
        if (vant.id == "resistencia_arcana" || vant.id == "resistencia_arcana_aprimorada") return false
        if (vant.categoria == Categoria.PODER) return false
        if (vant.requisitos.vantagensPrevias.contains("antecedente_arcano") ||
            vant.id == "superpoderes") return false
    }

    // 3. Arcane Background UI Logic

    // Cidade do Sol a Vapor: bloqueios específicos do cenário
    if (compendioCidadeSolVaporAtivo) {
        val blockedIdsCsv = setOf(
            "artifice",
            "senhor_das_feras",
            "campeao",
            "engenhoqueiro",
            "guerreiro_sagrado_profano",
            "mentalista",
            "pontos_de_poder",
            "surto_de_poder",
            "recarga_rapida",
            "recarga_rapida_aprimorada",
            "drenar_a_alma",
            "mago"
        )
        if (vant.id in blockedIdsCsv) return false

        // "Novos Poderes" só para Tecnomagia.
        if (vant.id == "novos_poderes") {
            val temTecnomagia = vantagensSelecionadas.any { it.id == "aa_tecnomagia" }
            if (!temTecnomagia) return false
        }

        // Bloqueia vantagens com pré-requisito de AA que não sejam as do livro Cidade do Sol a Vapor.
        val hasArcanePrereq = vant.requisitos.vantagensPrevias.any { prevId ->
            val p = prevId.uppercase().semAcentos().trim()
            p == "ANTECEDENTE_ARCANO" ||
                p == "ANTECEDENTE_ARCANO:*" ||
                p.startsWith("ANTECEDENTE_ARCANO_") ||
                p.startsWith("AA_")
        }
        val origemNorm = (vant.origem.ifBlank { "BASICO" }).uppercase().semAcentos().trim()
        if (hasArcanePrereq && origemNorm != "CIDADE_SOL_VAPOR" && origemNorm != "SOL_VAPOR") {
            return false
        }
    }

    val isGenericAB = vant.id == "antecedente_arcano"
    val isSpecificAB = (vant.id.startsWith("antecedente_arcano_") || vant.id.startsWith("aa_"))

    // Fantasy Logic: Keep Generic visible, Hide Specifics (Use Generic as selector)
    if (compendioFantasiaAtivo) {
        if (isSpecificAB) return false
        if (isGenericAB) return true
    }

    // Horror Logic: Keep Generic visible, Hide Specifics (Use Generic as selector)
    if (compendioHorrorAtivo) {
        if (isSpecificAB) return false
        if (isGenericAB) return true
    }

    // Pathfinder Exception: Uses Generic AB as selector for Magia/Milagres
    if (compendioPathfinderAtivo) {
        // Show Generic, Hide Specific (unless owned?)
        if (isSpecificAB) return false
        if (isGenericAB) return true
    }

    // Deadlands Exception: Uses Generic AB as selector
    if (compendioDeadlandsAtivo) {
        if (isSpecificAB) return false
        if (isGenericAB) return true
    }

    if (!multiplosAAHabilitados) {
        // If Multiple ABs DISABLED: Show ONLY the Generic AB (Hide specific ones)
        if (isSpecificAB) return false
    } else {
        // If Multiple ABs ENABLED: Show Specific ABs (Hide the Generic one)
        if (isGenericAB) return false
    }

    return true
}
