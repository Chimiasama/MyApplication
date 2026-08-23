package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreationProgressAggregatorTest {

    @Test
    fun `aggregateIssues returns empty list for fully configured character`() {
        val issues = CreationProgressAggregator.aggregateIssues(
            nome = "Valeros",
            pontosAtributosRestantes = 0,
            pontosPericiasRestantes = 0,
            temPoderesSemEscolha = false,
            ancestryChoicePending = false
        )
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `aggregateIssues returns errors for unspent attribute and skill points`() {
        val issues = CreationProgressAggregator.aggregateIssues(
            nome = "Valeros",
            pontosAtributosRestantes = 2,
            pontosPericiasRestantes = 3,
            temPoderesSemEscolha = false,
            ancestryChoicePending = false
        )
        assertEquals(2, issues.size)
        val attributeIssue = issues.first { it.id == "pontos_atributos_restantes" }
        assertEquals(IssueSeverity.ERRO, attributeIssue.severidade)
        assertTrue(attributeIssue.bloqueiaExportacao)

        val skillIssue = issues.first { it.id == "pontos_pericias_restantes" }
        assertEquals(IssueSeverity.ERRO, skillIssue.severidade)
        assertTrue(skillIssue.bloqueiaExportacao)
    }

    @Test
    fun `aggregateIssues returns warnings for blank name and pending choices`() {
        val issues = CreationProgressAggregator.aggregateIssues(
            nome = "",
            pontosAtributosRestantes = 0,
            pontosPericiasRestantes = 0,
            temPoderesSemEscolha = true,
            ancestryChoicePending = true
        )
        assertEquals(3, issues.size)
        assertTrue(issues.any { it.id == "nome_em_branco" && it.severidade == IssueSeverity.AVISO })
        assertTrue(issues.any { it.id == "poderes_pendentes" && it.severidade == IssueSeverity.AVISO })
        assertTrue(issues.any { it.id == "ancestralidade_opcao_pendente" && it.severidade == IssueSeverity.AVISO })
    }
}
