package com.example.swadebuilder.util

object GenericNameMapper {
    private val directReplacements = mapOf(
        "Made Man" to "Iniciado da Máfia",
        "Wiseguys" to "Máfia",
        "Syn Agent" to "Agente da Organização"
    )

    private val termReplacements = listOf(
        "Pedra Fantasma" to "Carvão Espectral",
        "Ghost Rock" to "Carvão Espectral",
        "Crystal Heart" to "Coração de Cristal",
        "Syn" to "Organização",
        "Cronista Desbravador" to "Cronista Buscatrilha",
        "Desbravador" to "Buscatrilha"
    )

    fun map(input: String): String {
        // 1. Direct match for exact names (e.g. Vantagem ID/Name)
        // Check if the input *contains* the key as a significant part or is the key.
        // For "Made Man", it appears as "BOM COMPANHEIRO (Made Man)".

        var output = input

        // Handle specific cases where English name is in parens
        directReplacements.forEach { (term, replacement) ->
             if (output.contains("($term)", ignoreCase = true)) {
                 output = output.replace("($term)", "($replacement)", ignoreCase = true)
             } else if (output.equals(term, ignoreCase = true)) {
                 output = replacement
             }
        }

        // 2. Term replacement for substrings
        termReplacements.forEach { (term, replacement) ->
            if (output.contains(term, ignoreCase = true)) {
                output = output.replace(term, replacement, ignoreCase = true)
            }
        }

        return output
    }
}
