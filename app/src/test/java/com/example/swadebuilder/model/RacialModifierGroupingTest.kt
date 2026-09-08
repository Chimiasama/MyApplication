package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RacialModifierGroupingTest {

    private fun raca(
        nome: String,
        origem: String,
        habilidades: List<RacialAbility> = emptyList(),
        opcoes: List<String> = emptyList()
    ) = RacialModifier(
        nome = nome,
        origem = origem,
        habilidades = habilidades,
        opcoes = opcoes
    )

    @Test
    fun `raca unica gera um unico grupo`() {
        val itens = listOf(raca("Elfos", "SCI_FI"))
        val grupos = groupAncestralidadesForDisplay(itens)
        assertEquals(1, grupos.size)
        assertEquals("Elfos", grupos.first().first().nome)
    }

    @Test
    fun `com o mesmo nome em dois livros ativos, vence o livro de maior prioridade`() {
        // Regressão do bug real: com Sci-Fi ativo junto do Básico, a tela de Ancestralidades
        // deve oferecer a variante Ciber de Anões — ela só existe na versão Sci-Fi do registro.
        val basico = raca("Anões", "BASICO", opcoes = emptyList())
        val sciFi = raca("Anões", "SCI_FI", opcoes = listOf("Básico", "Ciber"))

        for (entrada in listOf(listOf(basico, sciFi), listOf(sciFi, basico))) {
            val grupos = groupAncestralidadesForDisplay(entrada)
            assertEquals(1, grupos.size)
            val representante = grupos.first().first()
            assertEquals("SCI_FI", representante.origem)
            assertEquals(listOf("Básico", "Ciber"), representante.opcoes)
        }
    }

    @Test
    fun `racas diferentes com mecanica identica NAO sao fundidas`() {
        // Regressão: duas raças distintas com o mesmo bloco de habilidades por coincidência
        // (um livro reaproveitando o bloco entre duas entradas com nomes diferentes) devem
        // aparecer como duas linhas separadas e selecionáveis, não fundidas numa só.
        val habilidadesCompartilhadas = listOf(
            RacialAbility(nome = "Ação Adicional", descricao = "Ignora 2 pontos de penalidade por Ações Múltiplas."),
            RacialAbility(nome = "Frágil", descricao = "Reduz a Resistência em 1.")
        )
        val quadroides = raca(
            "Quadroides", "SCI_FI",
            habilidades = habilidadesCompartilhadas,
            opcoes = listOf("Padrão", "Habilidoso")
        )
        val outraRaca = raca("Outra Raça", "SCI_FI", habilidades = habilidadesCompartilhadas)

        val grupos = groupAncestralidadesForDisplay(listOf(quadroides, outraRaca))

        assertEquals(2, grupos.size)
        val nomes = grupos.map { it.first().nome }.toSet()
        assertEquals(setOf("Quadroides", "Outra Raça"), nomes)
    }

    @Test
    fun `variantes de nome da mesma raca entre livros continuam se fundindo`() {
        // "Humano" e "Humano (Buscatrilha)" representam a mesma raça em livros diferentes;
        // quando a mecânica é idêntica, devem continuar sendo tratados como uma única entrada
        // (permitindo remover o sufixo de cenário na exibição).
        val habilidadesHumano = listOf(RacialAbility(nome = "Astuto", descricao = "", id = "ASTUCIA"))
        val humanoBasico = raca("Humano", "BASICO", habilidades = habilidadesHumano)
        val humanoBuscatrilha = raca("Humano (Buscatrilha)", "PATHFINDER", habilidades = habilidadesHumano)

        val grupos = groupAncestralidadesForDisplay(listOf(humanoBasico, humanoBuscatrilha))

        assertEquals(1, grupos.size)
        assertEquals(2, grupos.first().size)
        assertTrue(grupos.first().map { it.origem }.containsAll(listOf("BASICO", "PATHFINDER")))
    }
}
