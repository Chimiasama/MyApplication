package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val id: String? = null,
    val category: String? = null,
    val severity: String? = null,
    // Ataque(s) natural(is) concedido(s) por esta habilidade racial, já como
    // dado estruturado — mesmo campo/tipo que MonstroHabilidade.armasNaturais
    // usa pro Template de Monstro Heroico (ver MonstroTemplate.kt). Antes
    // disso, extrairArmasNaturais() extraía dano/PA via regex sobre
    // `descricao`, casando por palavra-chave no nome; algumas raças (ex.:
    // Insetoides) precisavam de um valor de PA hardcoded no código porque a
    // descrição não bastava. Com o dado aqui, o código só lê.
    val armasNaturais: List<ArmaNatural> = emptyList()
)

@Serializable
data class RacialModifier(
    val id: String? = null,
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    val descricao: String? = null,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val habilidades: List<RacialAbility> = emptyList(),
    val origem: String = "BASICO",
    val movimentacao: Int = 0,
    val tags: List<String> = emptyList(),
    val opcoes: List<String> = emptyList()
)

@Serializable
data class HabilidadeCriacao(
    val nome: String,
    val custo: Int,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null
) {
    fun exibida(): HabilidadeCriacao =
        if (!com.example.swadebuilder.EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}

data class RacialAbilitySignature(val nome: String, val descricao: String)

data class RacialSignature(
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String>,
    val desvantagens: List<String>,
    val habilidades: List<RacialAbilitySignature>
)

fun RacialModifier.signature(): RacialSignature {
    fun normalizeList(values: List<String>): List<String> = values.sortedBy { it.uppercase().semAcentos() }

    return RacialSignature(
        atributos = atributos,
        pericias = pericias,
        vantagensGratis = normalizeList(vantagensGratis),
        desvantagens = normalizeList(desvantagens),
        habilidades = habilidades
            .map { RacialAbilitySignature(it.nome, it.descricao) }
            .sortedWith(compareBy({ it.nome.uppercase().semAcentos() }, { it.descricao.uppercase().semAcentos() }))
    )
}

fun stripAncestralidadeScenarioSuffix(nome: String): String =
    nome.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()

/**
 * Colapsa candidatos de ancestralidade — possivelmente vindos de múltiplos livros ativos ao
 * mesmo tempo (Modo Livre, ou um livro companheiro somado ao Básico) — em grupos prontos para
 * exibição em uma lista de seleção de raça.
 *
 * Duas etapas:
 * 1. Por nome exato: quando o mesmo nome existe em mais de um livro ativo, mantém a versão do
 *    livro de maior [originPriority] (em vez da primeira do arquivo), já que livros de
 *    cenário/companheiros costumam trazer uma versão mais específica que o Básico.
 * 2. Por (nome-base sem sufixo de cenário, assinatura mecânica): funde apenas quando AMBOS
 *    coincidem, nunca só a assinatura — duas raças diferentes podem ter atributos/perícias/
 *    habilidades idênticos por coincidência (ex.: Kalianos reaproveita o mesmo bloco de
 *    habilidades de Quadroides no Sci-Fi) sem serem a mesma raça. Exigir o nome-base também
 *    preserva a fusão legítima de variantes de nome da mesma raça entre livros (ex.: "Humano"
 *    e "Humano (Buscatrilha)").
 */
fun groupAncestralidadesForDisplay(items: List<RacialModifier>): List<List<RacialModifier>> {
    val prioritized = items.groupBy { it.nome.keyify() }
        .map { (_, duplicates) ->
            if (duplicates.size == 1) duplicates.first() else duplicates.maxBy { originPriority(it.origem) }
        }

    return prioritized
        .groupBy { stripAncestralidadeScenarioSuffix(it.nome).keyify() to it.signature() }
        .values
        .toList()
}
