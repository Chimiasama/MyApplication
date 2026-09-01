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
    // Quantas vezes este traço foi "comprado" — os 3 livros (Básico, Fantasia,
    // Sci-Fi) marcam cada traço do catálogo de criação de ancestralidade com
    // um número entre parênteses (e "S" pra sem limite) dizendo quantas vezes
    // ele pode ser escolhido, e o efeito/custo multiplicam por esse número
    // (ex.: "Armadura (3): ... Armadura +2 cada vez que é comprada" — 2x
    // vira Armadura +4). A maioria dos traços tem vezes=1 (o padrão) porque
    // só pode ser escolhida uma vez; só os que RacialTraitPointCatalog.VEZES_MAX
    // lista como > 1 usam este campo pra valer mais de 1 — ver
    // RacialTraitEffect e ModifierEngine.aplicarEfeito.
    val vezes: Int = 1,
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
    val opcoes: List<String> = emptyList(),
    // Identificador estável do "conceito de espécie" por trás desta entrada,
    // compartilhado entre as várias cópias por livro da mesma raça (mesmo
    // padrão de vantagens.json: um id, reimpresso por livro). Existe
    // separado de `id` porque `id` já tem uma convenção divergente e mais
    // antiga (algumas raças usam sufixo de livro, ex. `anc_humano_csv`,
    // outras não têm id nenhum) — mexer nisso quebraria referências já
    // existentes. `especieId` é só para o código de regra (SummaryUtils.kt
    // etc.) checar "esta ficha é da espécie X" sem comparar texto de nome,
    // e fica `null` em raças customizadas pelo jogador (nunca preenchido na
    // criação customizada), o que já barra por construção uma raça custom
    // com nome parecido de acionar uma regra pensada para a raça oficial.
    val especieId: String? = null
)

@Serializable
data class HabilidadeCriacao(
    val nome: String,
    val custo: Int,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    // Id estável pra Traços Raciais customizados (ver SettingsDialog.kt) — o catálogo oficial
    // (basico_habilidades_raciais.json) continua identificado por `nome` em todo o app, então
    // esse campo é aditivo e não muda a regra de identidade.
    val id: String? = null,
    // Só preenchido nas ENTRADAS DO CATÁLOGO (basico_habilidades_raciais.json)
    // — quantas vezes o livro permite comprar este traço (ver
    // RacialAbility.vezes pro mesmo conceito do lado "traço já escolhido").
    // null/1 = uma vez só; -1 = "S", sem limite no livro (a UI ainda precisa
    // de um teto prático — ver SettingsDialog.kt). Vazio numa instância já
    // ESCOLHIDA (dentro de tracosAdicionados/selectedRacialTraits): ali quem
    // importa é `vezes`, não `vezesMax`.
    val vezesMax: Int? = null,
    // Só preenchido nas instâncias JÁ ESCOLHIDAS: quantas vezes este traço
    // específico foi comprado (custo/efeito já vêm multiplicados por este
    // valor em `custo` e, na hora de virar RacialAbility, aqui também).
    val vezes: Int = 1,
    // Só preenchido nas ENTRADAS DO CATÁLOGO: id compartilhado por várias
    // entradas que são, no livro, o MESMO traço "(1)" (pega uma vez só) com
    // custo/efeito variando por versão — ex.: "Ações Adicionais" custa 4, 5
    // ou 10 pontos conforme a versão (Sci-Fi condicional, Básico/Sci-Fi
    // padrão, Fantasia "Maior" — ver docs/swade_basico|fantasia|scifi).
    // Diferente de vezesMax (que soma o MESMO efeito várias vezes): aqui são
    // versões ALTERNATIVAS e mutuamente exclusivas do mesmo traço — o
    // seletor de Traços Raciais (SettingsDialog.kt) mostra as entradas com o
    // mesmo grupoEscolha como uma linha só, abrindo "Qual versão?" em vez de
    // "Quantas vezes?".
    val grupoEscolha: String? = null
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
private val racialGrantSeveritySuffixRegex = Regex("""\s*\((MAIOR|MENOR)\)\s*$""")

// Chave de dedup tolerante a diferenças de formatação puramente cosméticas entre a
// mesma vantagem/desvantagem grátis representada de duas formas — id vs nome (ex.:
// "ANTECEDENTE_ARCANO_MILAGRES" vs "Antecedente Arcano (Milagres)") ou nome cru vs
// nome com sufixo de gravidade re-anexado (ex.: "Desastrado" vs "Desastrado (Menor)").
// Reduz a string a só letras/números maiúsculos sem acento — duas grafias que só
// diferem em espaço/underscore/parênteses/pontuação caem na mesma chave.
fun String.racialGrantDedupeKey(): String =
    keyify().replace(racialGrantSeveritySuffixRegex, "").filter { it.isLetterOrDigit() }

// distinctBy(racialGrantDedupeKey) porque algumas raças do catálogo (6 de 121, ex.:
// Halfling do Pathfinder com "Sorte") têm a mesma vantagem/desvantagem registrada nas
// DUAS formas ao mesmo tempo — solta em vantagensGratis/desvantagens E de novo dentro
// de habilidades[] com category=racial_edge/racial_hindrance — em vez de só uma
// delas, que é o que o resto deste arquivo assume. Sem isso a Vantagem/Complicação
// aparecia duplicada em qualquer lugar que lesse essa lista (ex.: "Características" da
// aba Ancestralidades), mesmo a concessão mecânica de verdade só acontecendo uma vez
// (ResolveGrantedAncestryAdvantagesUseCase já tinha seu próprio distinctBy(id)).
fun vantagensGratisEfetivas(vantagensGratis: List<String>, habilidades: List<RacialAbility>): List<String> =
    (vantagensGratis + habilidades.filter { it.category == "racial_edge" }.map { it.id ?: it.nome })
        .distinctBy { it.racialGrantDedupeKey() }

fun desvantagensEfetivas(desvantagens: List<String>, habilidades: List<RacialAbility>): List<String> =
    (desvantagens + habilidades.filter { it.category == "racial_hindrance" }.map { hab ->
        val sev = hab.severity
        if (sev != null && !hab.nome.contains("($sev)", ignoreCase = true)) {
            "${hab.nome} ($sev)"
        } else {
            hab.nome
        }
    }).distinctBy { it.racialGrantDedupeKey() }

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
