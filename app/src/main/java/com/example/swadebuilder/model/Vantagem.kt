package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Vantagem(
    val id: String,
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,

    val categoria: Categoria,

    val origem: String = "",

    val requisitos: Requisito,

    @SerialName("limite_compra")
    val limiteCompra: String = "",

    @SerialName("vinculado_pericia")
    val vinculadoPericia: Boolean = false,

    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),

    val descricao: String = "",


    // Identificação de "grupo" e variantes do Antecedente Arcano
    @SerialName("grupoId")
    val grupoId: String? = null,           // ex.: "antecedente_arcano" (para os 5 subtipos)

    @SerialName("subtipoArcano")
    val subtipoArcano: String? = null,     // ex.: "DOM", "MAGIA", "MILAGRES", "PSIÔNICOS", "CIÊNCIA ESTRANHA"

    @SerialName("isGrupoSelector")

    val isGrupoSelector: Boolean = false,  // true apenas para a entrada-base "antecedente_arcano" (com choiceOptions)
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
