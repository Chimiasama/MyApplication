package com.example.swadebuilder

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.semAcentos

fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()

fun Vantagem.toArcanoKey(): String? {
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()

    // Fix for "Poderes Místicos": force MISTICO key even if choice (class) is present
    // to ensure fixed power lookup works correctly in CriadorState.
    if ("PODERES MISTICOS" in nome.normAAKey()) return "MISTICO"

    if (!choice.isNullOrBlank()) {
        val c = choice!!.normAAKey()
        // Map new (Básico) keys to standard keys
        return when {
            "DOM BASICO" in c -> "DOM"
            "MAGIA BASICO" in c -> "MAGIA"
            "MILAGRES BASICO" in c -> "MILAGRES"
            "PSIONICOS BASICO" in c -> "PSIONICOS"
            "CIENCIA ESTRANHA BASICO" in c -> "CIENCIA ESTRANHA"
            "VUDUISMO" in c -> "VODUISTA"
            else -> c
        }
    }
    val n = nome.normAAKey()
    return when {
        "(DOM" in n -> "DOM"
        "(MAGIA" in n -> "MAGIA"
        "(MILAGRES" in n -> "MILAGRES"
        ("(PSIONICOS" in n) -> "PSIONICOS"
        ("(CIENCIA ESTRANHA" in n) -> "CIENCIA ESTRANHA"
        "ABENCOADO" in n -> "ABENCOADO"
        "CIENTISTA LOUCO" in n -> "CIENTISTA LOUCO"
        "MESTRE DO CHI" in n -> "MESTRE DO CHI"
        "MASCATE" in n || "HUCKSTER" in n || "VIGARISTA" in n -> "MASCATE"
        "XAMA" in n -> "XAMA"
        "CANALIZAR CRISTAL" in n -> "CANALIZAR CRISTAL"
        "ALQUIMIA" in n -> "ALQUIMIA"
        "BARDO" in n -> "BARDO"
        "BRUXO" in n -> "BRUXO"
        "BRUXA" in n -> "BRUXA"
        "BRUXARIA" in n -> "FEITICARIA"
        "CLERIGO" in n -> "CLERIGO"
        "DIABOLISTA" in n -> "DIABOLISTA"
        "DRUIDA" in n -> "DRUIDA"
        "ELEMENTALISTA" in n -> "ELEMENTALISTA"
        "FEITICEIRO" in n -> "FEITICEIRO"
        "MAGO" in n -> "MAGO"
        "NECROMANTE" in n -> "NECROMANTE"
        "RITUALISTA" in n -> "RITUALISTA"
        "CLERIGO_PF" in n -> "CLERIGO_PF"
        "MILAGRES_PF" in n -> "MILAGRES_PF"
        "BARDO_PF" in n -> "BARDO_PF"
        "DRUIDA_PF" in n -> "DRUIDA_PF"
        "FEITICEIRO_PF" in n -> "FEITICEIRO_PF"
        "MAGO_PF" in n -> "MAGO_PF"
        "INVOCADOR DE DEMONIOS" in n -> "INVOCADOR DE DEMONIOS"
        "VIDENTE" in n -> "VIDENTE"
        "MISTICO" in n -> "MISTICO"
        "VODUISTA" in n || "VUDUISMO" in n -> "VODUISTA"
        "DEMONIO" in n -> "DEMONIO"
        else -> null
    }
}
