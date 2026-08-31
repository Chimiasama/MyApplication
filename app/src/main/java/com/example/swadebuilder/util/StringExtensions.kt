package com.example.swadebuilder.util

import java.text.Collator
import java.text.Normalizer
import java.util.Collections
import java.util.Locale

private val DIACRITICS_REGEX = "\\p{M}".toRegex()
private val WHITESPACE_REGEX = "\\s+".toRegex()

private const val MAX_CACHE_SIZE = 2000

private val semAcentosCache: MutableMap<String, String> = Collections.synchronizedMap(
    object : LinkedHashMap<String, String>(MAX_CACHE_SIZE + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }
)

private val keyifyCache: MutableMap<String, String> = Collections.synchronizedMap(
    object : LinkedHashMap<String, String>(MAX_CACHE_SIZE + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }
)

private val fancyTitleCaseCache: MutableMap<String, String> = Collections.synchronizedMap(
    object : LinkedHashMap<String, String>(MAX_CACHE_SIZE + 1, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }
)

private val fancyLowerCaseWords = setOf(
    "de", "da", "do", "das", "dos",
    "e", "em", "no", "na", "nos", "nas",
    "por", "para", "com", "sem", "sob", "sobre",
    "a", "o", "as", "os", "à", "às", "ou", "ao", "aos"
)

private val fancyUpperCaseWords = setOf(
    "XP", "PA", "PB", "PP", "PV", "PC", "PE", "SP", "GM", "MJ", "CD", "ME", "VE", "NV"
)
private val fancyUpperCaseMap = fancyUpperCaseWords.associateBy { it.lowercase(Locale.ROOT) }

private val fancyRomanNumerals = setOf(
    "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
    "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
)
private val fancyRomanNumeralsMap = fancyRomanNumerals.associateBy { it.lowercase(Locale.ROOT) }

private val fancySpecialPrefixes = listOf("d'", "l'")

/**
 * Remove acentos de uma string, normalizando para Form NFD e filtrando marcas de combinação.
 * Optimized: Memoized with bounded LRU cache.
 */
fun String.semAcentos(): String {
    if (this.isEmpty()) return ""
    // Note: getOrPut is not atomic on synchronizedMap for computation, but map structure is safe.
    // Re-computation is acceptable for pure functions.
    return semAcentosCache.getOrPut(this) {
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(DIACRITICS_REGEX, "")
    }
}

/**
 * Transforma texto em chave: trim, uppercase e sem acentos.
 * Optimized: Memoized with bounded LRU cache.
 */
fun String.keyify(): String {
    if (this.isBlank()) return ""
    return keyifyCache.getOrPut(this) {
        trim()
            .uppercase()
            .semAcentos()
    }
}

/**
 * Normaliza texto pra uso como sufixo de id estável: sem acentos, minúsculo, com espaços e
 * qualquer outra pontuação virando "_" (ex.: "Fôgo do Inferno!" -> "fogo_do_inferno"). Usado
 * pra gerar ids de conteúdo customizado (ver SettingsDialog.kt) de forma consistente com
 * keyify() (mesma normalização de acentos), evitando colisões silenciosas por causa de acento
 * ou espaçamento diferentes entre dois nomes que na prática são o mesmo texto.
 */
fun String.toIdSlug(): String {
    if (this.isBlank()) return ""
    return trim().semAcentos().lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
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
    val collator = Collator.getInstance(Locale.Builder().setLanguage("pt").setRegion("BR").build())
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

    return fancyTitleCaseCache.getOrPut(this) {
        this.toFancyTitleCaseUncached()
    }
}

private fun String.toFancyTitleCaseUncached(): String {
    if (this.isBlank()) return this

    val normalized = this.replace('_', ' ').trim()
    val words = normalized.split(WHITESPACE_REGEX)

    fun isWordChar(c: Char): Boolean = c.isLetterOrDigit() || c == '\''

    // Recursive helper to process individual segments (handles slashes/hyphens if they appear inside a token)
    fun processSegment(segment: String, isFirstWordOfSentence: Boolean): String {
        // If segment contains '/', split and process parts
        if (segment.contains('/')) {
            return segment.split('/').joinToString("/") { part ->
                processSegment(part, isFirstWordOfSentence && part == segment.substringBefore('/')) // Only first part considers sentence start if applicable
            }
        }

        val lowerSegment = segment.lowercase()
        val lowerRoot = lowerSegment.lowercase(Locale.ROOT)

        return when {
            // 1. Acronyms & Roman Numerals
            fancyUpperCaseMap.containsKey(lowerRoot) -> {
                fancyUpperCaseMap.getValue(lowerRoot)
            }
            fancyRomanNumeralsMap.containsKey(lowerRoot) -> {
                fancyRomanNumeralsMap.getValue(lowerRoot)
            }
            // 2. Special Prefixes (d'Arc)
            fancySpecialPrefixes.any { lowerSegment.startsWith(it) } -> {
                val p = fancySpecialPrefixes.first { lowerSegment.startsWith(it) }
                if (lowerSegment.length > p.length) {
                    val rest = lowerSegment.substring(p.length)
                    p + rest.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                } else {
                    if (isFirstWordOfSentence) lowerSegment.replaceFirstChar { it.titlecase(Locale.getDefault()) } else lowerSegment
                }
            }
            // 3. General Rules
            isFirstWordOfSentence -> {
                lowerSegment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
            fancyLowerCaseWords.contains(lowerSegment) -> {
                lowerSegment
            }
            else -> {
                lowerSegment.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }
    }

    return words.mapIndexed { index, rawToken ->
        // Separate punctuation wrapper
        val prefix = rawToken.takeWhile { !isWordChar(it) }
        val suffix = rawToken.takeLastWhile { !isWordChar(it) }

        // Handle case where token is just punctuation (e.g. "-")
        if (prefix.length + suffix.length >= rawToken.length) {
             return@mapIndexed rawToken
        }

        val core = rawToken.substring(prefix.length, rawToken.length - suffix.length)

        // Process the core part
        val transformedCore = processSegment(core, index == 0)

        prefix + transformedCore + suffix
    }.joinToString(" ")
}
