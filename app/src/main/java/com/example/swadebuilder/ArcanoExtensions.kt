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
        ("(PSIONICOS" in n) || ("(PSIÔNICOS" in nome) -> "PSIONICOS"
        ("(CIENCIA ESTRANHA" in n) || ("(CIÊNCIA ESTRANHA" in nome) -> "CIENCIA ESTRANHA"
        "ABENCOADO" in n -> "ABENCOADO"
        "CIENTISTA LOUCO" in n -> "CIENTISTA LOUCO"
        "MESTRE DO CHI" in n -> "MESTRE DO CHI"
        "VIGARISTA" in n -> "VIGARISTA"
        "XAMA" in n -> "XAMA"
        else -> null
    }
}
