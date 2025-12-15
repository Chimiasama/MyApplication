package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoracaoCristal(
    val id: String,
    val nome: String,
    val origem: String = "",
    val estagio: String,
    @SerialName("pontos_poder")
    val pontosPoder: Int,
    val slots: Int,
    @SerialName("habilidade_passiva")
    val habilidadePassiva: String,
    val poderes: List<String> = emptyList(),
    @SerialName("complicacao_inerente")
    val complicacaoInerente: String? = null,
    val descricao: String? = null
)
