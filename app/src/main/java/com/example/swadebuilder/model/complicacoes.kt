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
    val vantagensPrevias: List<String> = emptyList()
) {
    val nomeExibicao: String
        get() = if (EditionConfig.isFullEdition) {
            name
        } else {
            GenericNameMapper.map(name)
        }
}
