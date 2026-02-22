package com.example.swadebuilder.util

import java.text.Collator
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

/**
 * Transforma texto para Title Case simples: cada palavra começa com maiúscula.
 */
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

/**
 * Colator para ordenação em Português do Brasil (sensível a acentos).
 */
val ptBrCollator: Collator by lazy {
    val collator = Collator.getInstance(Locale("pt", "BR"))
    collator.strength = Collator.TERTIARY // Diferencia 'a', 'á', 'à', 'A'
    collator
}

/**
 * Transforma texto para "Fancy Title Case" (padrão bibliográfico brasileiro/PT-BR).
 * - Primeira letra da string sempre maiúscula.
 * - Preposições (de, da, do...) minúsculas.
 * - Siglas (XP, PA, PB...) maiúsculas.
 * - Numerais Romanos (I..XX) maiúsculos.
 * - Prefixos especiais (d', l') minúsculos seguidos de maiúscula.
 * - Preserva pontuação envolvente ((XP), "Texto", etc).
 */
fun String.toFancyTitleCase(): String {
    if (this.isBlank()) return this

    val normalized = this.replace('_', ' ').trim()
    val words = normalized.split("\\s+".toRegex())

    // Lista de preposições/artigos que devem ficar em minúsculo (exceto se for a 1ª palavra)
    val lowerCaseWords = setOf(
        "de", "da", "do", "das", "dos",
        "e", "em", "no", "na", "nos", "nas",
        "por", "para", "com", "sem", "sob", "sobre",
        "a", "o", "as", "os", "à", "às"
    )

    // Lista de siglas que devem ficar em maiúsculo
    val upperCaseWords = setOf(
        "XP", "PA", "PB", "PP", "PV", "PC", "PE", "SP", "GM", "MJ", "CD", "ME", "VE", "NV"
    )

    // Lista de numerais romanos comuns (até 20)
    val romanNumerals = setOf(
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
    )

    // Prefixos especiais que ficam minúsculos seguidos de maiúscula (ex: d'Arc)
    val specialPrefixes = listOf("d'", "l'")

    fun isWordChar(c: Char): Boolean = c.isLetterOrDigit() || c == '\''

    return words.mapIndexed { index, rawToken ->
        // Separate punctuation wrapper
        val prefix = rawToken.takeWhile { !isWordChar(it) }
        val suffix = rawToken.takeLastWhile { !isWordChar(it) }

        // Handle case where token is just punctuation (e.g. "-")
        if (prefix.length + suffix.length >= rawToken.length) {
             // If token is all punctuation but not empty (e.g. "..."),
             // takeWhile/takeLastWhile might overlap.
             // If prefix covers whole string, suffix is basically same or empty.
             return@mapIndexed rawToken
        }

        val core = rawToken.substring(prefix.length, rawToken.length - suffix.length)
        val lowerCore = core.lowercase()

        val transformedCore = when {
            // 1. Acronyms & Roman Numerals (Check case-insensitive)
            upperCaseWords.any { it.equals(core, ignoreCase = true) } -> {
                upperCaseWords.find { it.equals(core, ignoreCase = true) }!!
            }
            romanNumerals.any { it.equals(core, ignoreCase = true) } -> {
                romanNumerals.find { it.equals(core, ignoreCase = true) }!!
            }
            // 2. Special Prefixes (d'Arc)
            specialPrefixes.any { lowerCore.startsWith(it) } -> {
                val p = specialPrefixes.find { lowerCore.startsWith(it) }!!
                if (lowerCore.length > p.length) {
                     val rest = lowerCore.substring(p.length)
                     p + rest.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                } else {
                    // Just the prefix itself? e.g. "D'"
                    if (index == 0) lowerCore.replaceFirstChar { it.titlecase(Locale.getDefault()) } else lowerCore
                }
            }
            // 3. General Rules
            index == 0 -> {
                lowerCore.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            lowerCaseWords.contains(lowerCore) -> {
                lowerCore
            }
            else -> {
                lowerCore.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }

        prefix + transformedCore + suffix
    }.joinToString(" ")
}
