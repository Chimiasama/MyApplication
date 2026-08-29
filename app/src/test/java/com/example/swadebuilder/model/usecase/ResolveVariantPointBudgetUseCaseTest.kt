package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.complicacaoComoItemAdicionado
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.custoDeAdicionarComplicacao
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.custoDeAdicionarVantagem
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.itensRemoviveisDe
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.vantagemComoItemAdicionado
import com.example.swadebuilder.model.usecase.ResolveVariantPointBudgetUseCase.Companion.valorTotalDe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveVariantPointBudgetUseCaseTest {

    private val useCase = ResolveVariantPointBudgetUseCase()

    private fun vantagemNovato(id: String, nome: String) = Vantagem(
        id = id,
        nome = nome,
        categoria = Categoria.COMBATE,
        requisitos = Requisito(estagio = "Novato")
    )

    private fun complicacao(id: String, nome: String, severity: String) = Complicacao(
        id = id,
        name = nome,
        severity = severity,
        description = "",
        origem = "BASICO"
    )

    @Test
    fun `raca de livro sem nenhuma mudanca ja fecha o orcamento (valor de livro ja e 2)`() {
        // Toda raça oficial já fecha em 2 pontos por conta própria (ex.: Humanos
        // = só Adaptável = 2). Sem mexer em nada, a Variante já nasce fechada.
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = emptyList(), itensAdicionados = emptyList())
        assertEquals(2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `remover sozinho o unico traco de uma raca de valor 2 nao fecha o orcamento`() {
        // Humanos (valor de livro 2, só Adaptável) removendo Adaptável sem
        // adicionar nada: 2 - 2 + 0 = 0, não fecha em 2 — precisa compensar.
        val adaptavel = VariantBudgetItem(label = "Adaptável", custo = 2, habilidadeId = "ADAPTAVEL")
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = listOf(adaptavel), itensAdicionados = emptyList())
        assertEquals(0, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `remover o unico traco e adicionar uma vantagem novato equivalente fecha o orcamento`() {
        val adaptavel = VariantBudgetItem(label = "Adaptável", custo = 2, habilidadeId = "ADAPTAVEL")
        val adicionados = listOf(vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")))
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = listOf(adaptavel), itensAdicionados = adicionados)
        assertEquals(2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `remover sozinha uma desvantagem de custo -1 (comprar de volta) nao fecha o orcamento`() {
        // Anões (valor de livro 2: -1 Movimentação Reduzida + 2 Resistente + 1
        // Visão no Escuro) removendo só a Movimentação Reduzida: 2 - (-1) + 0 = 3.
        val movRed = VariantBudgetItem(label = "Movimentação Reduzida", custo = -1, habilidadeId = "MOVIMENTACAO_REDUZIDA")
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = listOf(movRed), itensAdicionados = emptyList())
        assertEquals(3, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `duas vantagens novato compensadas por uma complicacao maior fecham o orcamento de uma raca sem valor de livro`() {
        // Isolando só a matemática de adicionados (valorBaseRaca = 0): +2 +2
        // (duas Vantagens Novato) + (-2) (uma Complicação Maior) = +2 exato.
        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")),
            vantagemComoItemAdicionado(vantagemNovato("poder", "Novos Poderes")),
            complicacaoComoItemAdicionado(complicacao("obrigacao_maior", "Obrigação", "maior"), comoMaior = true)
        )
        val result = useCase.resolve(valorBaseRaca = 0, itensRemovidos = emptyList(), itensAdicionados = adicionados)

        assertEquals(2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `adicionar duas vantagens sem compensar nao fecha o orcamento padrao`() {
        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")),
            vantagemComoItemAdicionado(vantagemNovato("poder", "Novos Poderes"))
        )
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = emptyList(), itensAdicionados = adicionados)

        assertEquals(6, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `sem limite aceita qualquer saldo, incluindo o que nao fecharia no orcamento padrao`() {
        val adicionados = listOf(vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")))
        val result = useCase.resolve(valorBaseRaca = 2, itensRemovidos = emptyList(), itensAdicionados = adicionados, semLimite = true)

        assertEquals(4, result.saldo)
        assertTrue(result.dentroDoOrcamento)

        val vazio = useCase.resolve(valorBaseRaca = 0, itensRemovidos = emptyList(), itensAdicionados = emptyList(), semLimite = true)
        assertTrue(vazio.dentroDoOrcamento)
    }

    @Test
    fun `custo de vantagem escala pelo estagio`() {
        assertEquals(2, custoDeAdicionarVantagem(vantagemNovato("a", "A")))
        assertEquals(3, custoDeAdicionarVantagem(vantagemNovato("a", "A").copy(requisitos = Requisito(estagio = "Experiente"))))
        assertEquals(4, custoDeAdicionarVantagem(vantagemNovato("a", "A").copy(requisitos = Requisito(estagio = "Veterano"))))
        assertEquals(5, custoDeAdicionarVantagem(vantagemNovato("a", "A").copy(requisitos = Requisito(estagio = "Heroico"))))
    }

    @Test
    fun `custo de complicacao menor ou maior depende da escolha do criador`() {
        val flexivel = complicacao("voto", "Voto", "menor ou maior")
        assertEquals(-1, custoDeAdicionarComplicacao(flexivel, comoMaior = false))
        assertEquals(-2, custoDeAdicionarComplicacao(flexivel, comoMaior = true))
    }

    @Test
    fun `itens removiveis de uma raca base cobrem habilidades vantagensGratis e desvantagens`() {
        val raca = RacialModifier(
            nome = "RAÇA_TESTE",
            atributos = emptyMap(),
            pericias = emptyMap(),
            vantagensGratis = listOf("PRONTIDÃO"),
            desvantagens = listOf("DESASTRADO"),
            habilidades = listOf(
                RacialAbility(nome = "Forte", descricao = "teste", id = "FORTE", category = "racial_trait_positive")
            )
        )

        val itens = itensRemoviveisDe(raca)

        assertEquals(3, itens.size)
        assertTrue(itens.any { it.habilidadeId == "FORTE" && it.custo == 2 })
        assertTrue(itens.any { it.vantagemId == "prontidao" && it.custo == 2 })
        assertTrue(itens.any { it.complicacaoId == "desastrado" && it.custo == -1 })
    }

    @Test
    fun `valorTotalDe soma o custo de todos os itens removiveis da raca`() {
        // Anões (Básico): -1 Movimentação Reduzida + 2 Resistente + 1 Visão no
        // Escuro = 2 — o valor de livro que toda raça oficial deve fechar.
        val anoes = RacialModifier(
            nome = "ANÕES",
            atributos = mapOf("Vigor" to 2),
            pericias = emptyMap(),
            habilidades = listOf(
                RacialAbility(nome = "Movimentação Reduzida", descricao = "", id = "MOVIMENTACAO_REDUZIDA", category = "racial_trait_negative"),
                RacialAbility(nome = "Resistente", descricao = "", id = "RESISTENTE", category = "racial_trait_positive"),
                RacialAbility(nome = "Visão no Escuro", descricao = "", id = "VISAO_NO_ESCURO", category = "racial_trait_positive")
            )
        )

        assertEquals(2, valorTotalDe(anoes))
    }
}
