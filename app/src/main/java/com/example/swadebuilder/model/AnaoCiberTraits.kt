package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify
import kotlinx.serialization.Serializable

/**
 * Catálogo curado de traços raciais negativos que os Anões (variante Ciber, Sci-Fi)
 * podem escolher na criação de personagem.
 *
 * NÃO é uma cópia direta de `basico_habilidades_raciais.json`: cada entrada aqui foi
 * verificada manualmente contra `ModifierEngine` para garantir que produz o efeito
 * mecânico esperado (ou, quando o traço é puramente narrativo/situacional no livro,
 * que fica registrado como desvantagem visível sem fingir um efeito numérico que
 * o motor não calcula). Dois traços do catálogo original ("Movimentação Reduzida"
 * -1 e -3) foram deliberadamente excluídos: os Anões Sci-Fi já têm "Movimentação
 * Reduzida" na ancestralidade base, então escolhê-los de novo não somaria nada
 * (o próprio ModifierEngine ignora a duplicata) — o jogador gastaria pontos à toa.
 * "Complicação Racial (Menor/Maior)" também ficaram de fora por exigirem escolher
 * uma Complicação de verdade do catálogo, um fluxo diferente deste seletor.
 */
@Serializable
data class AnaoCiberTraitSelection(
    val traitId: String,
    val escolhaAtributo: String? = null,
    val escolhaPericia: String? = null
)

data class AnaoCiberNegativeTrait(
    val id: String,
    val nome: String,
    val custo: Int,
    val descricao: String,
    val injecaoMecanica: String? = null,
    // Id mecânico real do traço (RacialTraitPointCatalog.EFEITOS), pareado
    // à mão com `injecaoMecanica` — nunca derivado do texto de exibição em
    // tempo de execução. Só os traços do Grupo 1 (efeito numérico
    // verificado) têm um aqui.
    val injecaoId: String? = null,
    val exigeEscolhaAtributo: Boolean = false,
    val exigeEscolhaPericia: Boolean = false
)

object AnaoCiberTraitCatalog {

    const val MAX_PONTOS = 2

    val TRACOS: List<AnaoCiberNegativeTrait> = listOf(
        // Grupo 1 — efeito mecânico automático, verificado contra ModifierEngine
        AnaoCiberNegativeTrait(
            id = "fragil",
            nome = "Frágil",
            custo = -1,
            descricao = "Reduz a Resistência em 1.",
            injecaoMecanica = "Frágil",
            // FRAGIL só é reconhecido por id de habilidade de verdade (ver
            // ModifierEngine.idsSoPorHabilidade) — Anão Ciber não tem uma
            // habilidade própria com este id, então escolher este traço
            // registra a desvantagem mas não soma a penalidade de
            // Resistência automaticamente (mesma limitação de antes desta
            // refatoração; documentado, não uma regressão nova).
            injecaoId = "FRAGIL"
        ),
        AnaoCiberNegativeTrait(
            id = "tamanho_menos_1",
            nome = "Tamanho -1",
            custo = -1,
            descricao = "Reduz o Tamanho e a Resistência em 1.",
            injecaoMecanica = "Tamanho -1",
            injecaoId = "TAMANHO_MENOS_1"
        ),
        AnaoCiberNegativeTrait(
            id = "aparar_baixo",
            nome = "Aparar Baixo",
            custo = -1,
            descricao = "Aparar -1.",
            injecaoMecanica = "Aparar -1",
            // Id próprio (não "APARAR_BAIXO", que no catálogo vale -2) — este
            // traço do Anão Ciber é -1, não -2.
            injecaoId = "APARAR_MENOS_1"
        ),
        // Grupo 2 — traços narrativos/situacionais (o livro não define um valor de
        // ficha calculável; ficam anotados como desvantagem, sem modifier numérico,
        // igual a qualquer outra raça com esse mesmo traço)
        AnaoCiberNegativeTrait(
            id = "dependencia",
            nome = "Dependência",
            custo = -2,
            descricao = "Precisa ter contato com uma substância por 1h a cada 24h ou sofre Fadiga."
        ),
        AnaoCiberNegativeTrait(
            id = "dependencia_atmosferica_1",
            nome = "Dependência Atmosférica (Horas)",
            custo = -1,
            descricao = "Necessita de atmosfera rara; teste de Vigor a cada hora em outra atmosfera ou sofre Fadiga."
        ),
        AnaoCiberNegativeTrait(
            id = "dependencia_atmosferica_2",
            nome = "Dependência Atmosférica (Minutos)",
            custo = -2,
            descricao = "Necessita de atmosfera rara; teste de Vigor a cada minuto em outra atmosfera ou sofre Fadiga."
        ),
        AnaoCiberNegativeTrait(
            id = "doente_1",
            nome = "Doente",
            custo = -1,
            descricao = "Sofre -2 em rolagens de Vigor para resistir ou se recuperar de doenças."
        ),
        AnaoCiberNegativeTrait(
            id = "doente_2",
            nome = "Doente (Infeccioso ao Ferir)",
            custo = -2,
            descricao = "-2 em Vigor contra doenças; ao sofrer Ferimento, rola Vigor ou é infectado por Doença Debilitante."
        ),
        AnaoCiberNegativeTrait(
            id = "forma_alienigena",
            nome = "Forma Alienígena",
            custo = -1,
            // Evita a palavra "Tamanho" de propósito: essa string cai em
            // `desvantagensRaciais` e o ModifierEngine trata qualquer ocorrência de
            // "TAMANHO" como fonte de Tamanho racial. Se o jogador também tivesse
            // escolhido o traço "Tamanho -1" nesta mesma seleção, o motor pegaria
            // apenas a primeira ocorrência da palavra na lista, potencialmente
            // ignorando a escolha real de Tamanho.
            descricao = "Corpo ou forma incomum: requer equipamentos personalizados (+100% custo) ou sofre -1 em rolagens de Característica."
        ),
        AnaoCiberNegativeTrait(
            id = "fraqueza_ambiental",
            nome = "Fraqueza Ambiental",
            custo = -1,
            descricao = "-4 para resistir a um efeito ambiental e sofre +4 de dano dele."
        ),
        AnaoCiberNegativeTrait(
            id = "inimigo_racial",
            nome = "Inimigo Racial",
            custo = -1,
            descricao = "-2 em Persuadir ao lidar com a espécie rival."
        ),
        AnaoCiberNegativeTrait(
            id = "nao_fala",
            nome = "Não Fala",
            custo = -1,
            descricao = "Não pode formar sons comuns. Comunica-se por gestos, música ou linguagem própria."
        ),
        AnaoCiberNegativeTrait(
            id = "nao_pode_curar",
            nome = "Não Pode Curar",
            custo = -1,
            descricao = "Sem capacidade de cura natural ou autorreparo (exige cura ativa ou conserto)."
        ),
        AnaoCiberNegativeTrait(
            id = "repugnante",
            nome = "Repugnante",
            custo = -1,
            descricao = "Aparência ou odor repulsivo; reações de terceiros começam sempre Hostis (ou 1d6 na Tabela de Reação)."
        ),
        AnaoCiberNegativeTrait(
            id = "transtorno_separacao",
            nome = "Transtorno de Separação",
            custo = -2,
            descricao = "Subtrai 2 das rolagens de Espírito quando nenhum outro membro da própria espécie estiver na linha de visão."
        ),
        AnaoCiberNegativeTrait(
            id = "volumoso",
            nome = "Volumoso",
            custo = -2,
            descricao = "-2 em Características ao usar equipamento comum; custos de itens dobrados."
        ),
        // Grupo 3 — traços paramétricos: exigem que o jogador escolha um alvo
        // (atributo ou perícia). Ficam registrados como anotação/desvantagem
        // (o app não modela penalidade de teste por atributo/perícia como um
        // Modifier calculável, então isso é aplicado manualmente na mesa, como
        // já ocorre com qualquer outra Complicação equivalente do app).
        AnaoCiberNegativeTrait(
            id = "penalidade_atributo_1",
            nome = "Penalidade em Atributo (-1)",
            custo = -2,
            descricao = "Um Atributo sofre penalidade de -1 em seus testes.",
            exigeEscolhaAtributo = true
        ),
        AnaoCiberNegativeTrait(
            id = "penalidade_pericia_1",
            nome = "Penalidade em Perícia (-1)",
            custo = -1,
            descricao = "Sofre -1 em uma perícia comum (ou -2 se incomum).",
            exigeEscolhaPericia = true
        ),
        AnaoCiberNegativeTrait(
            id = "penalidade_pericia_2",
            nome = "Penalidade em Perícia (-2)",
            custo = -2,
            descricao = "Sofre -2 em uma perícia comum (ou -4 se incomum).",
            exigeEscolhaPericia = true
        )
    )

    fun byId(id: String): AnaoCiberNegativeTrait? = TRACOS.firstOrNull { it.id == id }

    fun pontosUsados(selecoes: List<AnaoCiberTraitSelection>): Int =
        selecoes.sumOf { sel -> byId(sel.traitId)?.custo?.let { -it } ?: 0 }

    /**
     * Monta os traços a inserir em `desvantagensRaciais`. Traços do Grupo 1
     * carregam `injecaoId` (id mecânico real, verificado contra o
     * catálogo — ver `AnaoCiberNegativeTrait.injecaoId`). Os demais não têm
     * efeito numérico modelado, então recebem o próprio id do catálogo
     * (`trait.id`, já estável e escrito à mão nesta classe) — nunca um id
     * derivado do texto de exibição.
     */
    fun buildDesvantagens(selecoes: List<AnaoCiberTraitSelection>): List<TraitAddition> {
        return selecoes.mapNotNull { sel ->
            val trait = byId(sel.traitId) ?: return@mapNotNull null
            when {
                trait.injecaoMecanica != null ->
                    TraitAddition(trait.injecaoMecanica, trait.injecaoId ?: trait.id.keyify())
                trait.exigeEscolhaAtributo -> {
                    val atributo = sel.escolhaAtributo ?: return@mapNotNull null
                    TraitAddition(
                        "${trait.nome}: testes de $atributo sofrem -1 (escolha do jogador).",
                        "${trait.id.keyify()}_${atributo.keyify()}"
                    )
                }
                trait.exigeEscolhaPericia -> {
                    val pericia = sel.escolhaPericia ?: return@mapNotNull null
                    TraitAddition(
                        "${trait.nome}: testes de $pericia sofrem penalidade (escolha do jogador).",
                        "${trait.id.keyify()}_${pericia.keyify()}"
                    )
                }
                else -> TraitAddition("${trait.nome}: ${trait.descricao}", trait.id.keyify())
            }
        }
    }
}
