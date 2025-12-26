package com.example.swadebuilder

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.semAcentos

fun String.normAAKey(): String =
    this.uppercase().semAcentos().trim()

fun Vantagem.toArcanoKey(): String? {
    if (!subtipoArcano.isNullOrBlank()) return subtipoArcano.normAAKey()
    if (!choice.isNullOrBlank()) return choice!!.normAAKey()
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
        "AGENTE DA SYN" in n -> "AGENTE DA SYN"
        "ALQUIMIA" in n -> "ALQUIMIA"
        "BARDO" in n -> "BARDO"
        "BRUXO" in n -> "BRUXO"
        "BRUXA" in n -> "BRUXA"
        "CLERIGO" in n -> "CLERIGO"
        "DIABOLISTA" in n -> "DIABOLISTA"
        "DRUIDA" in n -> "DRUIDA"
        "ELEMENTALISTA" in n -> "ELEMENTALISTA"
        "FEITICEIRO" in n -> "FEITICEIRO"
        "MAGO" in n -> "MAGO"
        "NECROMANTE" in n -> "NECROMANTE"
        "RITUALISTA" in n -> "RITUALISTA"
        "INVOCADOR DE DEMONIOS" in n -> "INVOCADOR DE DEMONIOS"
        "VIDENTE" in n -> "VIDENTE"
        "MISTICO" in n -> "MISTICO"
        "VODUISTA" in n -> "VODUISTA"
        else -> null
    }
}
