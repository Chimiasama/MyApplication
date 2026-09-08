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
 * reaproveitar o resolver:
 *
 * - `atributos_bonus` guarda PASSOS (ex.: Anjo Força:2 = 2 passos de dado,
 *   RacialTraitEffect.AtributoStep(passos=2)), não o delta bruto que
 *   RacialModifier.atributos usa (onde Elfo Agilidade:2 já É "+2" pronto pra
 *   somar a 4). Multiplica por 2 pra entrar no mesmo formato. "Fe" (perícia
 *   Fé, não atributo) sai à parte, convertida pro "tier" que o resolver
 *   espera pra perícias (passos+1: 1 passo = tier 2 = d6).
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
    val atributosConvertidos = atributosBonus
        .filterKeys { it.keyify() != "FE" }
        .mapValues { (_, passos) -> passos * 2 }

    val feEntry = atributosBonus.entries.firstOrNull { it.key.keyify() == "FE" }
    val periciasConvertidas = feEntry?.let { mapOf("Fé" to it.value + 1) } ?: emptyMap()

    val habilidadesConvertidas = habilidades.map {
        RacialAbility(nome = it.nome, descricao = "", id = it.id, category = it.category, traitId = it.traitId, targetRef = it.targetRef)
    }

    val linhas = RacialCaracteristicasResolver.resolver(
        atributos = atributosConvertidos,
        pericias = periciasConvertidas,
        habilidades = habilidadesConvertidas
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
