package com.example.swadebuilder.util

import java.text.Normalizer
import java.text.Collator
import java.util.Locale

private val DIACRITICS_REGEX = "\\p{M}".toRegex()
private val ROMAN_NUMERAL_REGEX = "^[IVXLCDM]+$".toRegex()
private val APOSTROPHE_WORD_REGEX = "^([A-Za-zÀ-ÿ])'([A-Za-zÀ-ÿ].*)$".toRegex()

private val LOWERCASE_CONNECTORS = setOf(
    "a", "à", "ao", "aos", "as", "às",
    "com", "da", "das", "de", "do", "dos",
    "e", "em", "entre", "na", "nas", "no", "nos",
    "para", "per", "pela", "pelas", "pelo", "pelos", "por",
    "sem", "sob", "sobre"
)

private val KNOWN_ACRONYMS = setOf(
    "XP", "PA", "PB", "PP", "PV", "SP", "PC", "AA"
)

private val ptBrCollator: Collator by lazy {
    Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
        decomposition = Collator.CANONICAL_DECOMPOSITION
    }
}

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
    return toDisplayTitleCase()
}

/**
 * Transforma texto para "Sentence case": primeira letra maiúscula, restante minúsculo.
 * Substitui underscores por espaços.
 * Ex: "UM BRAÇO SÓ" -> "Um braço só".
 * Ex: "armadura_energizada" -> "Armadura energizada".
 */
fun String.toSentenceCase(): String {
    return toDisplayTitleCase()
}

fun String.toDisplayTitleCase(): String {
    if (isBlank()) return this

    val cleaned = replace('_', ' ').replace(Regex("\\s+"), " ").trim()
    val locale = Locale("pt", "BR")

    return cleaned.split(" ").mapIndexed { index, word ->
        formatDisplayWord(word, index == 0, locale)
    }.joinToString(" ")
}

fun comparePtBrDisplay(a: String, b: String): Int = ptBrCollator.compare(a, b)

private fun formatDisplayWord(word: String, isFirst: Boolean, locale: Locale): String {
    if (word.isBlank()) return word
    val uppercaseWord = word.uppercase(locale)
    if (uppercaseWord in KNOWN_ACRONYMS) return uppercaseWord
    if (ROMAN_NUMERAL_REGEX.matches(uppercaseWord)) return uppercaseWord

    val lower = word.lowercase(locale)
    if (!isFirst && lower in LOWERCASE_CONNECTORS) return lower

    val apostropheMatch = APOSTROPHE_WORD_REGEX.matchEntire(lower)
    if (apostropheMatch != null) {
        val prefix = apostropheMatch.groupValues[1]
        val remainder = apostropheMatch.groupValues[2]
        val transformedPrefix = prefix.lowercase(locale)
        return "$transformedPrefix'${remainder.replaceFirstChar { it.titlecase(locale) }}"
    }

    return lower.replaceFirstChar { it.titlecase(locale) }
}
