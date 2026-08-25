package com.example.swadebuilder.model

import com.example.swadebuilder.util.semAcentos

fun canonicalOriginKey(raw: String?): String {
    val normalized = raw
        .orEmpty()
        .ifBlank { "BASICO" }
        .uppercase()
        .semAcentos()
        .trim()
        .replace('-', '_')
        .replace(' ', '_')

    return when (normalized) {
        "SOL_VAPOR",
        "SOL_E_VAPOR",
        "CIDADE_DO_SOL_A_VAPOR",
        "CIDADE_SOL_A_VAPOR" -> "CIDADE_SOL_VAPOR"
        else -> normalized
    }
}

fun powerAssetOriginKey(raw: String?): String {
    return when (val canonical = canonicalOriginKey(raw)) {
        "CIDADE_SOL_VAPOR" -> "SOL_VAPOR"
        else -> canonical
    }
}

// Usado para decidir, quando um mesmo id/nome existe em mais de um livro ativo ao mesmo
// tempo (ex.: Modo Livre, ou um livro companheiro somado ao Básico), qual versão prevalece
// ao colapsar para uma única entrada. Livros de cenário/companheiros têm prioridade sobre o
// Básico, pois normalmente são versões mais específicas/ajustadas ao cenário.
fun originPriority(origin: String?): Int {
    val o = canonicalOriginKey(origin)
    return when {
        o == "HORROR" -> 1000
        o == "FANTASIA" -> 900
        o == "ARTE_DA_GUERRA" -> 800
        o == "DEADLANDS" -> 800
        o == "WISEGUYS" -> 800
        o == "CIDADE_SOL_VAPOR" -> 800
        o.contains("TRILHADOR") || o.contains("PATHFINDER") -> 800
        o == "FC" || o == "SCIFI" || o == "SCI_FI" -> 800
        o == "CRYSTAL_HEART" -> 800
        o == "BASICO" -> 0
        else -> 100
    }
}

// Colapsa uma lista para uma única entrada por chave, preferindo a de maior originPriority()
// quando a mesma chave aparece em mais de um livro ativo — em vez de manter simplesmente a
// primeira que aparecer no arquivo (o que descartava silenciosamente conteúdo de outros
// livros sempre que dois livros ativos compartilhavam o mesmo id/nome).
fun <T> List<T>.distinctByOriginPriority(origin: (T) -> String?, key: (T) -> Any): List<T> =
    groupBy(key).values.map { group ->
        if (group.size == 1) group.first() else group.maxBy { originPriority(origin(it)) }
    }
