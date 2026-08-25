package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
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
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),
    @SerialName("pericias_gratuitas")
    val periciasGratuitas: Map<String, Int> = emptyMap()
) {
    fun exibido(): Tropo =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}
