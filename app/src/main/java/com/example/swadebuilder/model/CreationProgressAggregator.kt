package com.example.swadebuilder.model

/**
 * Agregador de validação que analisa o estado atual do personagem
 * e calcula a lista de pendências/avisos de criação.
 */
object CreationProgressAggregator {

    fun aggregateIssues(
        nome: String,
        pontosAtributosRestantes: Int,
        pontosPericiasRestantes: Int,
        temPoderesSemEscolha: Boolean = false,
        ancestryChoicePending: Boolean = false
    ): List<CreationPendingIssue> {
        val issues = mutableListOf<CreationPendingIssue>()

        if (nome.isBlank()) {
            issues.add(
                CreationPendingIssue(
                    id = "nome_em_branco",
                    severidade = IssueSeverity.AVISO,
                    secao = "Identidade",
                    mensagem = "Personagem está sem nome definido.",
                    acaoSugerida = "Preencha o nome do personagem na aba Identidade."
                )
            )
        }

        if (pontosAtributosRestantes > 0) {
            issues.add(
                CreationPendingIssue(
                    id = "pontos_atributos_restantes",
                    severidade = IssueSeverity.ERRO,
                    secao = "Atributos",
                    mensagem = "Existem $pontosAtributosRestantes ponto(s) de Atributo não distribuído(s).",
                    acaoSugerida = "Aumente seus atributos na aba Atributos.",
                    bloqueiaExportacao = true
                )
            )
        }

        if (pontosPericiasRestantes > 0) {
            issues.add(
                CreationPendingIssue(
                    id = "pontos_pericias_restantes",
                    severidade = IssueSeverity.ERRO,
                    secao = "Perícias",
                    mensagem = "Existem $pontosPericiasRestantes ponto(s) de Perícia não distribuído(s).",
                    acaoSugerida = "Aloque os pontos em perícias na aba Perícias.",
                    bloqueiaExportacao = true
                )
            )
        }

        if (temPoderesSemEscolha) {
            issues.add(
                CreationPendingIssue(
                    id = "poderes_pendentes",
                    severidade = IssueSeverity.AVISO,
                    secao = "Poderes",
                    mensagem = "Existem Antecedentes Arcanos sem poderes selecionados.",
                    acaoSugerida = "Escolha seus poderes na aba Poderes."
                )
            )
        }

        if (ancestryChoicePending) {
            issues.add(
                CreationPendingIssue(
                    id = "ancestralidade_opcao_pendente",
                    severidade = IssueSeverity.AVISO,
                    secao = "Ancestralidades",
                    mensagem = "Sua ancestralidade exige uma escolha pendente (ex: elemento ou perícia).",
                    acaoSugerida = "Complete as opções da ancestralidade na aba Ancestralidades."
                )
            )
        }

        return issues
    }
}
