package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

/**
 * Variante custom de uma raça, criada pelo mestre no conteúdo customizado:
 * pega uma raça existente (oficial ou custom) como base, remove alguns dos
 * traços/vantagens/desvantagens dela e/ou adiciona traços novos, Vantagens
 * e Complicações do catálogo geral — dentro do orçamento de pontos de
 * `ResolveVariantPointBudgetUseCase`.
 *
 * É uma Variante de verdade (o mestre reconfigura a raça pro cenário/mesa),
 * não uma Seleção — some da lista quando a regra de livro "Variantes de
 * Raça" está desligada, igual às Variantes oficiais (ver
 * AncestryVariantRegistry, VariantOption.oficial).
 */
@Serializable
data class CustomAncestryVariant(
    val id: String,
    val ancestralidadeId: String,
    val nome: String,
    val descricao: String = "",
    /** Ids de habilidades[] da raça base removidos nesta Variante. */
    val tracosRemovidosIds: List<String> = emptyList(),
    /** Strings de vantagensGratis da raça base removidas nesta Variante. */
    val vantagensGratisRemovidas: List<String> = emptyList(),
    /** Strings de desvantagens da raça base removidas nesta Variante. */
    val desvantagensRemovidas: List<String> = emptyList(),
    /** Traços bespoke adicionados, escolhidos do catálogo geral (basico_habilidades_raciais.json). */
    val tracosAdicionados: List<HabilidadeCriacao> = emptyList(),
    /** Ids de Vantagem (vantagens.json) adicionadas de graça nesta Variante. */
    val vantagensAdicionadasIds: List<String> = emptyList(),
    /** Complicações (complicacoes.json) adicionadas, com a severidade escolhida quando a Complicação permite Menor ou Maior. */
    val complicacoesAdicionadas: List<CustomVariantComplicacaoEscolhida> = emptyList(),
    /** Quando true, ignora a exigência de fechar exato em ResolveVariantPointBudgetUseCase.DEFAULT_ORCAMENTO. */
    val semLimiteDePontos: Boolean = false
)

@Serializable
data class CustomVariantComplicacaoEscolhida(
    val complicacaoId: String,
    val comoMaior: Boolean
)
