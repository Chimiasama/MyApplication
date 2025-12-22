package com.example.swadebuilder.util

import com.example.swadebuilder.EditionConfig

private val liteDisplayReplacements = listOf(
    "Deadlands: O Oeste Estranho" to "Faroeste Estranho",
    "Arte da Guerra: Nova Era" to "Arte de Combate Novo Tempo",
    "Arte da Guerra Nova Era" to "Arte de Combate Novo Tempo",
    "Arte da Guerra" to "Arte de Combate Novo Tempo",
    "A Cidade do Sol a Vapor" to "Steampunk do Sol",
    "Cidade do Sol a Vapor" to "Steampunk do Sol",
    "Cidade Sol V'apor" to "Steampunk do Sol",
    "Crystal Heart" to "Coração de Pedra",
    "Deadlands" to "Faroeste Estranho",
    "Wiseguys" to "Malandros",
    "ARTE_DA_GUERRA" to "ARTE DE COMBATE NOVO TEMPO",
    "CIDADE_SOL_VAPOR" to "STEAMPUNK DO SOL",
    "CRYSTAL_HEART" to "CORAÇÃO DE PEDRA",
    "DEADLANDS" to "FAROESTE ESTRANHO",
    "WISEGUYS" to "MALANDROS"
)

fun String.toEditionDisplayName(): String {
    if (EditionConfig.isFullEdition) {
        return this
    }

    return liteDisplayReplacements.fold(this) { acc, (from, to) ->
        acc.replace(from, to)
    }
}
