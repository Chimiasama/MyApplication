package com.example.swadebuilder

import com.example.swadebuilder.model.Vantagem

/**
 * AppData mantém em escopo global o conteúdo “completo” das
 * supervantagens e supercomplicações, para exibir na Lista Completa.
 */
object AppData {
    var basicasVantagens: List<Vantagem> = emptyList()
    var superVantagens: List<Vantagem> = emptyList()
    var horrorVantagens: List<Vantagem> = emptyList()

    /** Texto completo de cada supervantagem (acrescentado à lista normal). */
    var superVantagensParaDetalhe: List<Vantagem> = emptyList()

}
