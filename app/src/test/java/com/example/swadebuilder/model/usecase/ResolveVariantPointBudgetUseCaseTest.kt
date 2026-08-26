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
    fun `nenhuma mudanca fica dentro do orcamento com saldo zero`() {
        val result = useCase.resolve(emptyList(), emptyList())
        assertEquals(0, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `remover um traco positivo devolve pontos (saldo negativo)`() {
        val forte = VariantBudgetItem(label = "Forte", custo = 2, habilidadeId = "FORTE")
        val result = useCase.resolve(itensRemovidos = listOf(forte), itensAdicionados = emptyList())
        assertEquals(-2, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `remover uma desvantagem custa pontos (saldo positivo)`() {
        val fragil = VariantBudgetItem(label = "Frágil", custo = -1, habilidadeId = "FRAGIL")
        val result = useCase.resolve(itensRemovidos = listOf(fragil), itensAdicionados = emptyList())
        assertEquals(1, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `adicionar vantagem novato e compensar com complicacao maior fecha dentro do orcamento`() {
        val vantagem = vantagemNovato("dom", "Dom")
        val complicacaoMaior = complicacao("obrigacao_maior", "Obrigação", "maior")

        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagem),
            complicacaoComoItemAdicionado(complicacaoMaior, comoMaior = true)
        )
        val result = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = adicionados)

        // +2 (Vantagem Novato) + (-2) (Complicação Maior) = 0
        assertEquals(0, result.saldo)
        assertTrue(result.dentroDoOrcamento)
    }

    @Test
    fun `adicionar duas vantagens sem compensar estoura o orcamento padrao`() {
        val adicionados = listOf(
            vantagemComoItemAdicionado(vantagemNovato("dom", "Dom")),
            vantagemComoItemAdicionado(vantagemNovato("poder", "Novos Poderes"))
        )
        val result = useCase.resolve(itensRemovidos = emptyList(), itensAdicionados = adicionados)

        assertEquals(4, result.saldo)
        assertFalse(result.dentroDoOrcamento)
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
