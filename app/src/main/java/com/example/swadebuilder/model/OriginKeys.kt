package com.example.swadebuilder.model

import com.example.swadebuilder.util.semAcentos

fun canonicalOriginKey(raw: String?): String {
    val normalized = raw
        .orEmpty()
        .ifBlank { "BASICO" }
        .uppercase()
        .semAcentos()
        .trim()
        .replace('-', '_')
        .replace(' ', '_')

    return when (normalized) {
        "SOL_VAPOR",
        "SOL_E_VAPOR",
        "CIDADE_DO_SOL_A_VAPOR",
        "CIDADE_SOL_A_VAPOR" -> "CIDADE_SOL_VAPOR"
        else -> normalized
    }
}

fun powerAssetOriginKey(raw: String?): String {
    val canonical = canonicalOriginKey(raw)
    return when (canonical) {
        "CIDADE_SOL_VAPOR" -> "SOL_VAPOR"
        else -> canonical
    }
}
