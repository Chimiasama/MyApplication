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
fun MonstroTemplate.paraCaracteristicas(): List<String> {
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
        habilidades = atributosSinteticos + listOfNotNull(periciaSintetica) + habilidadesConvertidas
    ).toMutableList()

    complicacoes.forEach { linhas += it.substringBefore(":").trim() }

    return linhas
}

@Serializable
data class ArmaNatural(
    val nome: String,
    val dano: String,
    val pa: Int = 0,
    // Se Artista Marcial/Brigão aumenta o tipo de dado desta arma (regra do
    // livro: só armas de "impacto" tipo garras escalam, mordida não).
    val escalavel: Boolean = false
)
