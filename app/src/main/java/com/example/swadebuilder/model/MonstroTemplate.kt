package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.keyify
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonstroTemplate(
    val id: String,
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    @SerialName("atributos_bonus")
    val atributosBonus: Map<String, Int> = emptyMap(),
    val habilidades: List<MonstroHabilidade> = emptyList(),
    val complicacoes: List<String> = emptyList(),
    // Mesmas strings de "complicacoes" (posição a posição), reescritas para a edição Lite.
    val complicacoesLite: List<String>? = null
) {
    fun exibido(): MonstroTemplate {
        if (EditionConfig.isFullEdition) return this
        val descricaoExibida = descricaoLite?.takeIf { it.isNotBlank() } ?: descricao
        val complicacoesExibidas = if (complicacoesLite != null && complicacoesLite.size == complicacoes.size) {
            complicacoes.indices.map { i -> complicacoesLite[i].takeIf { it.isNotBlank() } ?: complicacoes[i] }
        } else complicacoes
        return copy(
            descricao = descricaoExibida,
            habilidades = habilidades.map { it.exibida() },
            complicacoes = complicacoesExibidas
        )
    }

    /** Retorna vantagens grátis concedidas por traços `GRANTED_EDGE`/`racial_edge` em `habilidades`. */
    fun resolvedVantagensGratis(): List<String> {
        val list = mutableListOf<String>()
        habilidades.forEach { hab ->
            val tid = hab.resolvedTraitId().uppercase()
            if (tid == "GRANTED_EDGE" && !hab.targetRef.isNullOrBlank()) {
                if (!list.contains(hab.targetRef)) list.add(hab.targetRef)
            } else if (hab.category == "racial_edge") {
                val grant = hab.targetRef ?: hab.id ?: hab.nome
                if (!list.contains(grant)) list.add(grant)
            }
        }
        return list
    }
}

// Complicações de MonstroTemplate.complicacoes que citam, no próprio texto do livro, uma
// Complicação real do catálogo geral com severidade explícita — conferido contra
// complicacoes.json (id/severity reais) na migração pro sistema de Tropo (ver
// docs/auditoria_mecanica_racas_2026-08-31.md rodada 43): Anjo "Complicação Voto (Maior)",
// Monstro de Retalhos "Complicação Sem Noção" (catálogo só tem severidade "maior") e "Fobia
// (Maior)", Vampiro "tratado como Hábito (Maior)", Revivido "Voto (Maior)". Múmia "Lento:
// Movimentação reduzida em 1..." bate palavra por palavra com a versão Menor da Complicação
// real "Lento" do catálogo — esse caso, diferente dos outros 5 (puramente narrativos mesmo
// vinculados, igual qualquer Voto/Hábito/Fobia/Sem Noção normal), tem efeito numérico real
// (RacialTraitPointCatalog.EFEITOS["LENTO"] = PassoBonus(-1)), por isso ganha também
// traitId=PACE_CHANGE explícito — sem isso, a Complicação aparecia na lista mas nunca
// reduzia a Movimentação de verdade (igual o texto solto de complicacoes[] já fazia antes
// desta migração). Auditoria mais profunda (rodada 43, a pedido do usuário) não achou mais
// nenhum outro caso real: o catálogo geral não tem uma Complicação genérica "Fraqueza (X)"/
// "Vulnerabilidade a dano de X" equivalente às ~12 "Fraqueza (Prata)/(Fogo)/(Estaca)/..." dos
// outros templates (VULNERABILIDADE do catálogo é sobre exposição a substância — Distraído/
// Fadiga —, não sobre dano extra de um tipo de arma/elemento) — ficam narrativas mesmo, sem
// targetRef (mesmo padrão de uma Complicação de raça sem reskin, ex. Avianos "Não Sabe
// Nadar" antes do vínculo existir).
private data class MonstroComplicacaoLink(
    val monstroId: String,
    val textoContem: String,
    val targetRef: String,
    val severity: String,
    val traitId: String? = null,
    val value: Int = 1
)

private val MONSTRO_COMPLICACAO_LINKS = listOf(
    MonstroComplicacaoLink("anjo", "Servo do Paraíso", "voto", "Maior"),
    MonstroComplicacaoLink("monstro_retalhos", "Confusão", "sem_nocao", "Maior"),
    MonstroComplicacaoLink("monstro_retalhos", "Fogo Mau", "fobia", "Maior"),
    MonstroComplicacaoLink("vampiro", "Fome", "habito", "Maior"),
    MonstroComplicacaoLink("revivido", "Vingança", "voto", "Maior"),
    MonstroComplicacaoLink("mumia", "Lento", "lento", "Menor", traitId = "PACE_CHANGE", value = -1)
)

/**
 * Converte um MonstroTemplate (Horror) pro tipo Tropo unificado (ver rodada 43): mesmo
 * formato `habilidades: List<RacialAbility>` que raça/Tropo de Arte da Guerra usam.
 * `atributosBonus` vira ATTRIBUTE_BOOST/SKILL_BOOST (Fé é perícia, o resto é atributo) —
 * mesma conversão que `paraCaracteristicas()` já fazia, só que agora vira dado real em vez
 * de sintético-na-hora; `habilidades[]` passa direto (já usa o mesmo formato); cada string de
 * `complicacoes` vira uma RacialAbility `category="racial_hindrance"`, com `targetRef`
 * explícito só nos 5 casos verificados contra o catálogo (ver MONSTRO_COMPLICACAO_LINKS
 * acima) — preserva o comportamento atual (ModifierEngine já lê o texto antes de ":" como
 * nome da Complicação) e, nesses 5 casos, além disso vincula com o id real.
 */
fun MonstroTemplate.paraTropo(): Tropo {
    val atributoHabilidades = atributosBonus.entries
        .filterNot { it.key.keyify() == "FE" }
        .map { (atributo, passos) ->
            RacialAbility(nome = atributo, descricao = "", traitId = "ATTRIBUTE_BOOST", targetRef = atributo, value = passos, invisivel = true)
        }
    val feEntry = atributosBonus.entries.firstOrNull { it.key.keyify() == "FE" }
    val periciaHabilidade = feEntry?.let {
        RacialAbility(nome = "Fé", descricao = "", traitId = "SKILL_BOOST", targetRef = "Fé", value = it.value, invisivel = true)
    }
    val habilidadesConvertidas = habilidades.map {
        RacialAbility(
            nome = it.nome, descricao = it.descricao, descricaoLite = it.descricaoLite,
            id = it.id, category = it.category, traitId = it.traitId, targetRef = it.targetRef,
            armasNaturais = it.armasNaturais
        )
    }
    val complicacaoHabilidades = complicacoes.mapIndexed { i, texto ->
        val label = texto.substringBefore(":").trim()
        val link = MONSTRO_COMPLICACAO_LINKS.firstOrNull { it.monstroId == id && label.contains(it.textoContem, ignoreCase = true) }
        RacialAbility(
            nome = label,
            descricao = texto,
            descricaoLite = complicacoesLite?.getOrNull(i),
            id = "MONSTRO_${id}_COMPLICACAO_$i".keyify(),
            category = "racial_hindrance",
            traitId = link?.traitId,
            targetRef = link?.targetRef,
            value = link?.value ?: 1,
            severity = link?.severity
        )
    }
    return Tropo(
        id = id,
        nome = nome,
        categoria = "MONSTRO",
        origem = "HORROR",
        descricao = descricao,
        descricaoLite = descricaoLite,
        habilidades = atributoHabilidades + listOfNotNull(periciaHabilidade) + habilidadesConvertidas + complicacaoHabilidades
    )
}

@Serializable
data class MonstroHabilidade(
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    // Id estável de traço, no mesmo namespace de RacialTraitPointCatalog quando
    // a habilidade reaproveita um traço já usado por alguma Ancestralidade
    // (ex.: "MORTO_VIVO"). Opcional — nem toda habilidade tem efeito mecânico
    // modelado; várias aqui são só narrativas (ex.: Embelezar, Não Envelhece).
    val id: String? = null,
    // Mesma "CAMADA MECÂNICA / ENGINE PARAMETRIZADA" de RacialAbility (ver
    // RacialModifier.kt): category="racial_edge" (+ opcionalmente
    // traitId="GRANTED_EDGE" e targetRef quando `nome` é só skin do livro,
    // ex.: "Fúria" do Monstro de Retalhos concedendo a Vantagem Furioso)
    // é como um template concede uma Vantagem de graça — nunca mais como
    // string solta numa lista à parte.
    val category: String? = null,
    val traitId: String? = null,
    val targetRef: String? = null,
    // Ataque(s) natural(is) concedido(s) por esta habilidade, já como dado
    // estruturado (dano/PA prontos) em vez de precisar ser extraído do texto
    // de `descricao` por regex. Uma única habilidade pode gerar mais de uma
    // arma (ex.: "Mordida/Garras" do Lobisomem vira duas entradas de arma).
    val armasNaturais: List<ArmaNatural> = emptyList()
) {
    fun exibida(): MonstroHabilidade =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this

    /** Retorna o ID mecânico principal — priorizando o novo `traitId` parametrizado, ou o `id` legado. */
    fun resolvedTraitId(): String = traitId ?: id ?: ""
}

/**
 * Mesma lista "Características" que a aba Ancestralidades usa (ver
 * RacialCaracteristicasResolver), adaptada pro Template de Monstro Heroico —
 * que não é uma RacialModifier, então precisa converter duas coisas antes de
 * reaproveitar o resolver (que só lê `habilidades[]`, sem mapas numéricos
 * estáticos em paralelo):
 *
 * - `atributos_bonus` guarda PASSOS (ex.: Anjo Força:2 = 2 passos de dado) —
 *   a mesma unidade de `RacialTraitEffect.AtributoStep.passos`, então vira
 *   uma RacialAbility sintética por entrada (traitId="ATTRIBUTE_BOOST",
 *   value=passos) em vez de precisar converter pra delta bruto. "Fe"
 *   (perícia Fé, não atributo) sai à parte, virando uma sintética
 *   traitId="SKILL_BOOST" — mesma unidade de passos, sem a conversão de
 *   "tier" que o resolver antigo baseado em mapa exigia.
 * - Vantagem/Complicação de graça do template já vem embutida em
 *   `habilidades[]` (category=racial_edge/racial_hindrance, ver
 *   MonstroHabilidade) — o resolver lê isso direto, igual Ancestralidade.
 *   `complicacoes` é outra coisa: frases narrativas completas ("Fraqueza
 *   (Estaca no Coração): Ataque Localizado..."), sem par Nome/Severidade
 *   pra oferecer ao resolver — cada uma vira sua própria linha, só com o
 *   rótulo antes dos ":" (mesmo corte que ModifierEngine já faz pra aplicar
 *   a mecânica).
 */
fun MonstroTemplate.paraCaracteristicas(allVantagens: List<Vantagem> = emptyList()): List<String> {
    val atributosSinteticos = atributosBonus
        .filterKeys { it.keyify() != "FE" }
        .map { (atributo, passos) ->
            RacialAbility(nome = atributo, descricao = "", traitId = "ATTRIBUTE_BOOST", targetRef = atributo, value = passos, invisivel = true)
        }

    val feEntry = atributosBonus.entries.firstOrNull { it.key.keyify() == "FE" }
    val periciaSintetica = feEntry?.let {
        RacialAbility(nome = "Fé", descricao = "", traitId = "SKILL_BOOST", targetRef = "Fé", value = it.value, invisivel = true)
    }

    val habilidadesConvertidas = habilidades.map {
        RacialAbility(nome = it.nome, descricao = "", id = it.id, category = it.category, traitId = it.traitId, targetRef = it.targetRef)
    }

    val linhas = RacialCaracteristicasResolver.resolver(
        habilidades = atributosSinteticos + listOfNotNull(periciaSintetica) + habilidadesConvertidas,
        allVantagens = allVantagens
    ).toMutableList()

    complicacoes.forEach { linhas += it.substringBefore(":").trim() }

    return linhas
}

@Serializable
data class ArmaNatural(
    val nome: String,
    val dano: String,
    val pa: Int = 0,
    // Id do traço/Vantagem que concedeu esta arma (ex.: "GARRAS_SEM_PA",
    // "CHIFRES", "garras_demonio") — única fonte de verdade pra saber se ela
    // escala com Artista Marcial/Brigão (regra do livro: só armas de
    // "impacto" tipo garra escalam, mordida/chifre não — ver
    // RacialTraitPointCatalog.armaNaturalEscalavel()). Nunca mais um
    // booleano `escalavel` solto por raça/instância: sem esse campo pra
    // setar, não tem como uma raça nova "errar" o valor — o traço que já
    // concede a arma é quem decide, pelo próprio id.
    val id: String? = null
) {
    val escalavel: Boolean get() = RacialTraitPointCatalog.armaNaturalEscalavel(id)
}
