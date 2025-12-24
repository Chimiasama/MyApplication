package com.example.swadebuilder.util

import java.text.Normalizer
import java.util.Locale

/**
 * Remove acentos de uma string, normalizando para Form NFD e filtrando marcas de combinação.
 */
fun String.semAcentos(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "")

/**
 * Transforma texto em chave: trim, uppercase e sem acentos.
 */
fun String.keyify(): String =
    trim()
        .uppercase()
        .semAcentos()

fun String.titleCase(): String {
    return this.lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}

/**
 * Transforma texto para "Sentence case": primeira letra maiúscula, restante minúsculo.
 * Ex: "UM BRAÇO SÓ" -> "Um braço só".
 */
fun String.toSentenceCase(): String {
    if (this.isBlank()) return this
    val lowered = this.trim().lowercase()
    return lowered.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
