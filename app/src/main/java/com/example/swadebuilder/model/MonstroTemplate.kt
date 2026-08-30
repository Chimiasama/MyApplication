package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import com.example.swadebuilder.util.keyify
import kotlinx.serialization.Serializable

@Serializable
data class MonstroTemplate(
    val id: String,
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val atributos_bonus: Map<String, Int> = emptyMap(),
    val habilidades: List<MonstroHabilidade> = emptyList(),
    // Ids/nomes de Vantagem que o template concede de graça (não custam
    // escolha de Vantagem do jogador) — mesmo campo/mecanismo que
    // RacialModifier.vantagensGratis usa pra Ancestralidade, aplicado aqui a
    // uma habilidade do monstro (ex.: "Fúria" do Monstro de Retalhos concede
    // Furioso; "Ciência!" concede Resistência Arcana).
    val vantagensGratis: List<String> = emptyList(),
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
    // Ataque(s) natural(is) concedido(s) por esta habilidade, já como dado
    // estruturado (dano/PA prontos) em vez de precisar ser extraído do texto
    // de `descricao` por regex. Uma única habilidade pode gerar mais de uma
    // arma (ex.: "Mordida/Garras" do Lobisomem vira duas entradas de arma).
    val armasNaturais: List<ArmaNatural> = emptyList()
) {
    fun exibida(): MonstroHabilidade =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
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
 * - `complicacoes` são frases completas ("Fraqueza (Estaca no Coração):
 *   Ataque Localizado..."), não o par "Nome (Severidade)" que
 *   RacialModifier.desvantagens usa — o resolver tentaria ler errado o que
 *   vem entre parênteses como severidade. Em vez de forçar no mesmo
 *   parâmetro, cada complicação vira uma linha própria só com o rótulo antes
 *   dos ":" (mesmo corte que ModifierEngine já faz pra aplicar a mecânica).
 */
fun MonstroTemplate.paraCaracteristicas(): List<String> {
    val atributosConvertidos = atributos_bonus
        .filterKeys { it.keyify() != "FE" }
        .mapValues { (_, passos) -> passos * 2 }

    val feEntry = atributos_bonus.entries.firstOrNull { it.key.keyify() == "FE" }
    val periciasConvertidas = feEntry?.let { mapOf("Fé" to it.value + 1) } ?: emptyMap()

    val habilidadesConvertidas = habilidades.map {
        RacialAbility(nome = it.nome, descricao = "", id = it.id)
    }

    val linhas = RacialCaracteristicasResolver.resolver(
        atributos = atributosConvertidos,
        pericias = periciasConvertidas,
        vantagensGratis = vantagensGratis,
        desvantagens = emptyList(),
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
