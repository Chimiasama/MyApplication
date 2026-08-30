package com.example.swadebuilder.model

import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import com.example.swadebuilder.util.toFancyTitleCase
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

/**
 * Uma raça pode registrar uma Vantagem/Complicação grátis de duas formas:
 * numa lista solta (`vantagensGratis`/`desvantagens`) ou embutida numa
 * habilidade (`category == "racial_edge"`/`"racial_hindrance"`). As duas
 * formas coexistem nos dados (histórico de quando cada raça foi cadastrada),
 * então qualquer código que precise da lista completa — cálculo de pontos em
 * CriadorState, ou a lista de Características da aba Ancestralidades — usa
 * estas duas funções em vez de ler só um dos dois lugares.
 */
fun vantagensGratisEfetivas(vantagensGratis: List<String>, habilidades: List<RacialAbility>): List<String> =
    vantagensGratis + habilidades.filter { it.category == "racial_edge" }.map { it.id ?: it.nome }

fun desvantagensEfetivas(desvantagens: List<String>, habilidades: List<RacialAbility>): List<String> =
    desvantagens + habilidades.filter { it.category == "racial_hindrance" }.map { hab ->
        val sev = hab.severity
        if (sev != null && !hab.nome.contains("($sev)", ignoreCase = true)) {
            "${hab.nome} ($sev)"
        } else {
            hab.nome
        }
    }

/**
 * Monta a lista "Características" da aba Ancestralidades inteiramente a
 * partir de dado estruturado — nunca de `RacialAbility.descricao`. A raça não
 * "diz" o que tem em texto livre; ela só carrega atributos/perícias
 * (mapas numéricos já existentes), vantagens/complicações grátis (ids/nomes
 * já existentes) e um id por habilidade solta, e é só esse conjunto que essa
 * função lê. Rótulos de exibição vêm de `RacialTraitPointCatalog.LABEL` por
 * id — se um id não tem entrada lá, cai no `nome` cru da habilidade (nunca na
 * descrição longa), como ponte até o catálogo de rótulos cobrir mais ids.
 *
 * Ex. Elfo (Básico): atributos={Agilidade:2}, desvantagens=[Desastrado
 * (Menor)], habilidades=[Ágil(id=AGIL), Desastrado(id=DESASTRADO,
 * category=racial_hindrance), Visão no Escuro(id=VISAO_NO_ESCURO)] produz:
 * ["Atributo aumentado d6: Agilidade", "Complicação racial menor:
 * Desastrado", "Visão no Escuro"] — Ágil não vira linha própria porque seu id
 * já resolve pra AtributoStep(Agilidade), a mesma informação da primeira
 * linha; Desastrado não vira linha própria porque sua categoria já virou a
 * segunda linha via `desvantagens`.
 */
object RacialCaracteristicasResolver {

    fun resolver(
        atributos: Map<String, Int>,
        pericias: Map<String, Int>,
        vantagensGratis: List<String>,
        desvantagens: List<String>,
        habilidades: List<RacialAbility>
    ): List<String> {
        val linhas = mutableListOf<String>()

        atributos.filterValues { it != 0 }.forEach { (atributo, delta) ->
            val dado = (4 + delta).toDiceString()
            val verbo = if (delta > 0) "aumentado" else "reduzido"
            linhas += "Atributo $verbo $dado: ${atributo.toFancyTitleCase()}"
        }

        // Mesma convenção de tier usada na exibição atual: 0 = d4-2, N = d4 +
        // (N-1) passos de dado.
        pericias.filterValues { it > 0 }.forEach { (pericia, tier) ->
            val dado = (4 + (tier - 1) * 2).toDiceString()
            linhas += "Perícia inicial $dado: ${pericia.toFancyTitleCase()}"
        }

        // vantagensGratisEfetivas/desvantagensEfetivas somam a lista solta com
        // o que só existe embutido numa habilidade (category=racial_edge/
        // racial_hindrance) — várias raças (Elfo incluso) só têm a Complicação
        // registrada assim, com a lista solta vazia.
        vantagensGratisEfetivas(vantagensGratis, habilidades)
            .filterNot { it.keyify() == Constants.ID_AA_AGENT_SYN.keyify() }
            .forEach { linhas += "Vantagem racial: ${it.toFancyTitleCase()}" }

        desvantagensEfetivas(desvantagens, habilidades).forEach { entrada ->
            // Só "(Maior)"/"(Menor)" é severidade — outros parênteses no nome
            // da complicação (ex.: "Sentidos Aguçados (Olhos de Águia)") são
            // parte do nome, não devem virar "Complicação racial olhos de
            // águia: ...".
            val match = Regex("""^(.*?)\s*\((Maior|Menor)\)$""").find(entrada)
            if (match != null) {
                val (nome, severidade) = match.destructured
                linhas += "Complicação racial ${severidade.lowercase()}: ${nome.toFancyTitleCase()}"
            } else {
                linhas += "Complicação racial: ${entrada.toFancyTitleCase()}"
            }
        }

        habilidades.forEach { hab ->
            // Já virou linha de Vantagem/Complicação acima — não duplica.
            if (hab.category == "racial_hindrance" || hab.category == "racial_edge") return@forEach
            val id = hab.id?.keyify()
            val efeito = RacialTraitPointCatalog.efeitoDe(id)
            // Já virou a linha de Atributo/Perícia acima — não duplica.
            if (efeito is RacialTraitEffect.AtributoStep || efeito is RacialTraitEffect.PericiaStep) return@forEach
            val rotulo = id?.let { RacialTraitPointCatalog.LABEL[it] } ?: hab.nome.toFancyTitleCase()
            linhas += rotulo
        }

        return linhas
    }
}
