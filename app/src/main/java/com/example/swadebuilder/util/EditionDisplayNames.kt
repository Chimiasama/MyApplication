package com.example.swadebuilder.util

import com.example.swadebuilder.EditionConfig

// Mappings for raw IDs/Caps to proper Display Names (Full Edition defaults)
private val rawIdToDisplayName = mapOf(
    "CRYSTAL_HEART" to "Crystal Heart",
    "WISEGUYS" to "Wiseguys",
    "ARTE_DA_GUERRA" to "Arte da Guerra",
    "CIDADE_SOL_VAPOR" to "Cidade do Sol a Vapor",
    "OESTE_ESTRANHO" to "Deadlands",
    "FANTASIA" to "Fantasia",
    "HORROR" to "Horror",
    "SCI_FI" to "Sci-Fi",
    "PATHFINDER" to "Pathfinder",
    "BASICO" to "Básico"
)

private val liteDisplayReplacements = listOf(
    "Pathfinder" to "Buscatrilha",
    "Savage Pathfinder" to "Buscatrilha",
    "Deadlands: O Oeste Estranho" to "Faroeste Assombrado",
    "Arte da Guerra: Nova Era" to "Arte de Combate Novo Tempo",
    "Arte da Guerra Nova Era" to "Arte de Combate Novo Tempo",
    "Arte da Guerra" to "Arte de Combate Novo Tempo",
    "A Cidade do Sol a Vapor" to "Steampunk do Sol",
    "Cidade do Sol a Vapor" to "Steampunk do Sol",
    "Cidade Sol V'apor" to "Steampunk do Sol",
    "Crystal Heart" to "Coração de Pedra",
    "Deadlands" to "Faroeste Assombrado",
    "Wiseguys" to "Malandros",
    "Kemonomimi (Espírito Animal)" to "Povo Animal Espiritual",
    "Humano (Império do Sol)" to "Humano (Império Oriental)",
    "Terracota" to "Povo de Barro",
    "Onigem (Sangue de Oni)" to "Sangue de Demônio"
)

fun String.toEditionDisplayName(): String {
    // 1. Prettify ID if it matches a known raw ID
    val standardName = rawIdToDisplayName[this.uppercase()] ?: this

    if (EditionConfig.isFullEdition) {
        return standardName
    }

    return liteDisplayReplacements.fold(standardName) { acc, (from, to) ->
        acc.replace(from, to)
    }
}
