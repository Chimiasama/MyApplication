// Vantagem.kt
package com.example.myapplication.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vantagem(
    val id: String,
    val nome: String,

    val categoria: Categoria,

    val origem: String = "",

    @SerialName("nivel")
    val nivel: String = "N",

    val requisitos: Requisito,

    @SerialName("limite_compra")
    val limiteCompra: String = "",

    @SerialName("vinculado_pericia")
    val vinculadoPericia: Boolean = false,

    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),

    val descricao: String = "",

    // 1) Lê diretamente do JSON a lista de opções, sem marcar @Transient:
    @SerialName("choiceOptions")
    val choiceOptions: List<String> = emptyList(),

    // 2) Se quiser controlar quantas escolhas o usuário pode fazer (padrão = 1), também pode vir do JSON:
    @SerialName("maxSelections")
    val maxSelections: Int = 1,

    // 3) Sempre que existir alguma opção em choiceOptions, entende que precisa forçar escolha
    val requiresChoice: Boolean = choiceOptions.isNotEmpty(),

    // 4) “choice” (a própria escolha que o usuário fez) permanece mutável, mas não é serializado:
    @kotlinx.serialization.Transient
    var choice: String? = null
)
