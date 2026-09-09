package com.example.swadebuilder.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Enum que representa as categorias de Vantagem.
 * Deve corresponder exatamente aos valores que aparecem no JSON em "categoria".
 */
@Serializable
enum class Categoria {
    @SerialName("ANTECEDENTE")   ANTECEDENTE,
    @SerialName("COMBATE")       COMBATE,
    @SerialName("ESTILO_MARCIAL") ESTILO_MARCIAL,
    @SerialName("LIDERANCA")     LIDERANCA,
    @SerialName("PODER")         PODER,
    @SerialName("PROFISSIONAL")  PROFISSIONAL,
    @SerialName("SOCIAIS")       SOCIAIS,
    @SerialName("ESTRANHAS")     ESTRANHAS,
    @SerialName("ATORMENTADO")  ATORMENTADO,
    @SerialName("LENDARIAS")     LENDARIAS,
    @SerialName("SUPER")         SUPER,
    @SerialName("MONSTRUOSAS")   MONSTRUOSAS,
    @SerialName("CHI")           CHI,
    @SerialName("CLASSE")        CLASSE,
    @SerialName("VANTAGEM_DE_CLASSE") VANTAGEM_DE_CLASSE,
    @SerialName("PRESTIGIO")     PRESTIGIO,
    @SerialName("TROPO")         TROPO,
    @SerialName("ANCESTRALIDADE") ANCESTRALIDADE,
    // Vantagem cuja categoria "de verdade" é uma CategoriaCustomizada criada pelo
    // Mestre (ver Vantagem.categoriaCustomizadaId) — não corresponde a nenhum valor
    // real do JSON oficial, só existe pra vantagens customizadas. Como não é uma
    // categoria de regra conhecida (Classe/Profissional/Antecedente etc.), cai
    // corretamente no `else` de qualquer regra que dependa de categorias específicas
    // (ex.: CriadorState.isPathfinderEligible).
    @SerialName("CUSTOMIZADA")   CUSTOMIZADA
}

fun Categoria.getDisplayName(): String = when (this) {
    Categoria.ANTECEDENTE -> "Antecedente"
    Categoria.COMBATE -> "Combate"
    Categoria.ESTILO_MARCIAL -> "Estilo Martial"
    Categoria.LIDERANCA -> "Liderança"
    Categoria.PODER -> "Poder"
    Categoria.PROFISSIONAL -> "Profissional"
    Categoria.SOCIAIS -> "Sociais"
    Categoria.ESTRANHAS -> "Estranhas"
    Categoria.ATORMENTADO -> "Atormentado"
    Categoria.LENDARIAS -> "Lendárias"
    Categoria.SUPER -> "Super"
    Categoria.MONSTRUOSAS -> "Monstruosas"
    Categoria.CHI -> "Chi"
    Categoria.CLASSE -> "Classe"
    Categoria.VANTAGEM_DE_CLASSE -> "Vantagem de Classe"
    Categoria.PRESTIGIO -> "Prestígio"
    Categoria.TROPO -> "Tropo"
    Categoria.ANCESTRALIDADE -> "Ancestralidade"
    // Fallback genérico: quem quiser o nome real da categoria customizada precisa
    // resolver Vantagem.categoriaCustomizadaId contra a lista de CategoriaCustomizada
    // ativa (ver Vantagem.categoriaExibicao()) — este enum não carrega esse nome.
    Categoria.CUSTOMIZADA -> "Personalizada"
}
