package com.example.swadebuilder.model

import kotlinx.serialization.Serializable

/**
 * A quais tipos de conteúdo customizado uma [CategoriaCustomizada] pode se aplicar.
 * Cada valor corresponde a uma das listas de conteúdo customizado do Mestre
 * (ver BookCustomContent, CustomStorageManager.kt).
 */
@Serializable
enum class TipoEntidadeCategoria {
    VANTAGEM, EQUIPAMENTO, PODER, SUPER_PODER, COMPLICACAO
}

fun TipoEntidadeCategoria.getDisplayName(): String = when (this) {
    TipoEntidadeCategoria.VANTAGEM -> "Vantagem"
    TipoEntidadeCategoria.EQUIPAMENTO -> "Equipamento"
    TipoEntidadeCategoria.PODER -> "Poder"
    TipoEntidadeCategoria.SUPER_PODER -> "Super Poder"
    TipoEntidadeCategoria.COMPLICACAO -> "Complicação"
}

/**
 * Categoria criada pelo Mestre pra organizar Vantagens/Equipamentos/Poderes/Super
 * Poderes/Complicações customizados além das categorias oficiais fixas (ex.: o enum
 * `Categoria` de Vantagem). Salva por "livro" junto do resto do conteúdo customizado
 * (ver BookCustomContent), então uma categoria criada numa campanha própria não
 * aparece nas demais.
 *
 * Vantagens referenciam isto via `Vantagem.categoriaCustomizadaId` (com
 * `categoria == Categoria.CUSTOMIZADA`); Equipamento/Poder/Super Poder/Complicação
 * via seus próprios campos `categoriaCustomizadaId`, já que essas entidades não têm
 * um enum de categoria fixo pra começar.
 */
@Serializable
data class CategoriaCustomizada(
    val id: String,
    val nome: String,
    val tipoEntidade: TipoEntidadeCategoria,
    val descricao: String = ""
)
