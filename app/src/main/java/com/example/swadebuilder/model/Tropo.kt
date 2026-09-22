package com.example.swadebuilder.model

import com.example.swadebuilder.EditionConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tropo(
    val id: String,
    val nome: String,
    val categoria: String,
    val origem: String,
    @SerialName("tecnicas_iniciais")
    val tecnicasIniciais: Int = 0,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    @SerialName("ganha_ao_comprar")
    val ganhaAoComprar: List<String> = emptyList(),
    @SerialName("pericias_gratuitas")
    val periciasGratuitas: Map<String, Int> = emptyMap(),
    // CAMADA NOVA (sistema de Tropo genérico, ver docs/auditoria_mecanica_racas_2026-08-31.md
    // rodada 43): mesmo formato "habilidades[] com category/traitId/targetRef" que
    // RacialModifier/MonstroTemplate já usam — Vantagem/Complicação/perícia/atributo
    // concedidos por um Tropo entram aqui, não em `ganhaAoComprar`/`periciasGratuitas`
    // (campos antigos, mantidos só pros 9 Tropos oficiais que ainda não foram migrados;
    // ver Fase 4). Um Tropo que só usa `habilidades` não precisa preencher os campos
    // antigos.
    val habilidades: List<RacialAbility> = emptyList(),
    // Escolha obrigatória do jogador que MUDA o alvo de uma ou mais `habilidades[]` deste
    // Tropo — ex.: Kensai (Youxia, Arte da Guerra) escolhe se a Arma Predileta bonifica
    // Lutar, Atirar ou Atletismo; essa MESMA escolha decide tanto o alvo do bônus de perícia
    // quanto o rótulo da Vantagem concedida (ver TropoEscolha, CriadorState.tropoEscolhaAtual/
    // habilidadesDoTropoResolvidas). Uma `habilidade` que depende de uma escolha marca isso
    // no próprio `targetRef`, com o valor especial "$ESCOLHA:<id>" — nunca um targetRef de
    // verdade, sempre resolvido antes de qualquer cálculo (ver
    // CriadorState.habilidadesDoTropoResolvidas). Vazio = Tropo sem escolha nenhuma
    // (a maioria).
    val escolhas: List<TropoEscolha> = emptyList()
) {
    fun exibido(): Tropo =
        if (!EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}

@Serializable
data class TropoEscolha(
    val id: String,
    val rotulo: String,
    val opcoes: List<String>,
    // Escolha inicial (sempre uma das `opcoes`) — todo Tropo com `escolhas` PRECISA vir com
    // uma escolha já indicada por padrão, pro jogador nunca conseguir fechar a criação do
    // personagem sem nenhuma escolha feita (ver CriadorState.tropoEscolhaAtual).
    val padrao: String
)

// Prefixo reservado que marca um `RacialAbility.targetRef` como "resolver pela escolha do
// jogador", não um alvo fixo — ver `RacialAbility.armaEscolhaTropo()` e
// `CriadorState.habilidadesDoTropoResolvidas`.
private const val PREFIXO_ESCOLHA_TROPO = "\$ESCOLHA:"

/** Id da TropoEscolha referenciada por este targetRef, ou null se for um alvo fixo normal. */
fun String?.escolhaTropoReferenciada(): String? =
    this?.takeIf { it.startsWith(PREFIXO_ESCOLHA_TROPO) }?.removePrefix(PREFIXO_ESCOLHA_TROPO)

/** Constrói o valor de targetRef que marca "resolver pela escolha de id `escolhaId`". */
fun targetRefPorEscolhaTropo(escolhaId: String): String = "$PREFIXO_ESCOLHA_TROPO$escolhaId"
