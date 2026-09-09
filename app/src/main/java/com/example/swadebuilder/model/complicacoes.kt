package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.GenericNameMapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Complicacao(
    val id: String,
    val name: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    val severity: String,
    val description: String,
    val origem: String,
    val observacoes: String = "",
    @SerialName("vantagens_previas")
    val vantagensPrevias: List<String> = emptyList(),
    // Id de CategoriaCustomizada (ver model/CategoriaCustomizada.kt) — Complicação não
    // tem categoria oficial fixa, só se aplica a Complicações customizadas do Mestre.
    val categoriaCustomizadaId: String? = null
) {
    val nomeExibicao: String
        get() = if (EditionConfig.isFullEdition) {
            name
        } else {
            GenericNameMapper.map(name)
        }
}
