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
    "Humano (Império San)" to "Humano",
    "Humano (Império do Sol)" to "Humano",
    "Humano (Império Oriental)" to "Humano",
    "Akaimimi (Panda Vermelho)" to "Panda Vermelho",
    "Araiguma (Guaxinim)" to "Guaxinim",
    "Inumimi (Cão)" to "Cão",
    "Kitsunemimi (Raposa)" to "Raposa",
    "Nekomimi (Gato)" to "Gato",
    "Onigem (Sangue Oni)" to "Sangue Oni",
    "Onigem (Sangue de Oni)" to "Sangue Oni",
    "Tanukimimi (Tanuki)" to "Tanuki",
    "Umvee (Filhos da Lua)" to "Filhos da Lua",
    "Usagimimi (Coelho)" to "Coelho",
    "Terracota" to "Povo de Barro"
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
