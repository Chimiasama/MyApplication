package com.example.swadebuilder.util

import java.text.Normalizer
import java.util.Locale

private val DIACRITICS_REGEX = "\\p{M}".toRegex()

/**
 * Remove acentos de uma string, normalizando para Form NFD e filtrando marcas de combinação.
 */
fun String.semAcentos(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(DIACRITICS_REGEX, "")

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
 * Substitui underscores por espaços.
 * Ex: "UM BRAÇO SÓ" -> "Um braço só".
 * Ex: "armadura_energizada" -> "Armadura energizada".
 */
fun String.toSentenceCase(): String {
    if (this.isBlank()) return this
    // Replace underscores with spaces, then normalize
    val cleaned = this.replace('_', ' ').trim().lowercase()
    return cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}
