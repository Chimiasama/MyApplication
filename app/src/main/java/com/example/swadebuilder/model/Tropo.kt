package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tropo(
    val id: String,
    val nome: String,
    val categoria: String,
    val origem: String,
    @SerialName("tecnicas_iniciais")
    val tecnicasIniciais: Int = 0,
    val descricao: String = "",
    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),
    @SerialName("ganha_equipamentos")
    val ganhaEquipamentos: List<String> = emptyList()
)
