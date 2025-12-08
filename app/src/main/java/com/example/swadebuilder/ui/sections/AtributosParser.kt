package com.example.swadebuilder.ui.sections

import com.example.swadebuilder.model.AtributoJson

data class Atributo(
    val nome: String,
    val descricao: String
)

fun parseAtributos(atributos: List<AtributoJson>): List<Atributo> {
    return atributos.map { atributoJson ->
        Atributo(
            nome = atributoJson.nome,
            descricao = atributoJson.descricao
        )
    }
}
