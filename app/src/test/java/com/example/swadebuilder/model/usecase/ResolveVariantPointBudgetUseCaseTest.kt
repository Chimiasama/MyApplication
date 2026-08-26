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
    fun `nenhuma mudanca (saldo zero) nao fecha o orcamento padrao - precisa ser exato em 2`() {
        val result = useCase.resolve(emptyList(), emptyList())
        assertEquals(0, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `remover sozinho um traco positivo de custo 2 fecha exato no orcamento`() {
        val forte = VariantBudgetItem(label = "Forte", custo = 2, habilidadeId = "FORTE")
        val result = useCase.resolve(itensRemovidos = listOf(forte), itensAdicionados = emptyList())
        assertEquals(-2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `remover uma unica desvantagem de custo -1 nao fecha o orcamento padrao`() {
        // saldo vira +1 (devolve 1 ponto ao "comprar de volta" a Complicação) —
        // não bate nos 2 pontos exatos, então não fecha sozinho.
        val fragil = VariantBudgetItem(label = "Frágil", custo = -1, habilidadeId = "FRAGIL")
        val result = useCase.resolve(itensRemovidos = listOf(fragil), itensAdicionados = emptyList())
        assertEquals(1, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `duas vantagens novato compensadas por uma complicacao maior fecham exato em 2`() {
        // +2 +2 (duas Vantagens Novato) + (-2) (uma Complicação Maior) = +2 exato
        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")),
            vantagemComoItemAdicionado(vantagemNovato("poder", "Novos Poderes")),
            complicacaoComoItemAdicionado(complicacao("obrigacao_maior", "Obrigação", "maior"), comoMaior = true)
        )
        val result = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = adicionados)

        assertEquals(2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `adicionar duas vantagens sem compensar nao fecha o orcamento padrao`() {
        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")),
            vantagemComoItemAdicionado(vantagemNovato("poder", "Novos Poderes"))
        )
        val result = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = adicionados)

        assertEquals(4, result.saldo)
        assertFalse(result.dentroDoOrcamento)
    }

    @Test
    fun `sem limite aceita qualquer saldo, incluindo o que nao fecharia no orcamento padrao`() {
        val adicionados = listOf(vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")))
        val result = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = adicionados, semLimite = true)

        assertEquals(2, result.saldo)
        assertTrue(result.dentroDoOrcamento)

        val vazio = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = emptyList(), semLimite = true)
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
}
