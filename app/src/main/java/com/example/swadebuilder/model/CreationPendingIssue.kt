package com.example.swadebuilder.model

/**
 * Representa o nível de gravidade de uma pendência de criação.
 */
enum class IssueSeverity {
    ERRO,
    AVISO,
    INFO
}

/**
 * Modelo de dados para pendências e avisos de validação de criação de personagem.
 */
data class CreationPendingIssue(
    val id: String,
    val severidade: IssueSeverity,
    val secao: String,
    val mensagem: String,
    val explicacao: String? = null,
    val acaoSugerida: String? = null,
    val bloqueiaExportacao: Boolean = false
)
