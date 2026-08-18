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
        "Fantasma" to "F.",
        "Crystal Heart" to "Coração de Cristal",
        "Syn" to "Organização",
        "Cronista Desbravador" to "Cronista Buscatrilha",
        "Desbravador" to "Buscatrilha",
        "Aeronave (Dirigível)" to "Aeronave",
        " (Dirigível)" to "",
        "(Dirigível)" to "",
        "Dragão Alquímico" to "D. Alquímico",
        "Veterano do Oeste Estranho" to "Veterano do Oeste",
        "Atormentando" to "Revivido",
        "Atormentado" to "Revivido"
    )

    fun map(input: String): String {
        // 1. First apply edition-level display replacements (e.g., setting books/modules)
        var output = input.toEditionDisplayName()

        // 2. Direct match for exact names (e.g. Vantagem ID/Name)
        // Check if the input *contains* the key as a significant part or is the key.
        // For "Made Man", it appears as "BOM COMPANHEIRO (Made Man)".
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
