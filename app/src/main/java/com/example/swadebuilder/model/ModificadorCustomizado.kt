package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

/**
 * Modificador universal criado pelo Mestre e vinculado a um Super Poder já
 * existente (oficial do catálogo ou customizado) — cobre o modificador
 * "Especial" do livro de Supers (negociado à mesa, sem texto fixo) e
 * qualquer outro modificador de casa que o Mestre queira adicionar a um
 * poder específico.
 *
 * Salvo por "livro" junto do resto do conteúdo customizado (ver
 * BookCustomContent). O carregador (model/DataLoader) injeta `paraTexto()` na lista
 * `modificadores` (e, se presente, `modificadoresLite`) do poder alvo em
 * tempo de carregamento — nunca no catálogo oficial em disco —, no mesmo
 * formato usado pelos modificadores oficiais ("Nome (+custo): descrição"),
 * pra reaproveitar o parsing de custo já existente em SuperPoderesSection.kt.
 * `poderAlvoNome` é comparado por keyify() (ver DataLoader/CustomStorageManager),
 * não por igualdade exata, pra resistir a acento/caixa.
 */
@Serializable
data class ModificadorCustomizado(
    val id: String,
    val poderAlvoNome: String,
    val nome: String,
    val custo: String,
    val descricao: String = ""
) {
    fun paraTexto(): String = "$nome ($custo): $descricao"
}
