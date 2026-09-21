package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.FixedPackageOption
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.VariantGroup
import com.example.swadebuilder.model.VariantOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateAncestryOptionBudgetsUseCaseTest {

    private val useCase = ValidateAncestryOptionBudgetsUseCase()

    @Test
    fun `opcao com pacote vazio fecha com o valor da raca base`() {
        val base = RacialModifier(
            nome = "RACA_TESTE",
            habilidades = listOf(RacialAbility(nome = "Adaptável", descricao = "", id = "ADAPTAVEL")),
            pontosRaciaisEsperados = 2
        )
        val config = AncestryVariantConfig(
            ancestralidadeId = "RACA_TESTE",
            livro = "BASICO",
            grupoVariante = VariantGroup(
                opcoes = listOf(VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()))
            )
        )

        val results = useCase.execute(base, config)

        assertEquals(1, results.size)
        assertEquals(2, results.first().saldo)
        assertTrue(results.first().dentroDoOrcamento)
    }

    @Test
    fun `opcao que troca um traco por outro de mesmo custo continua fechando`() {
        val base = RacialModifier(
            nome = "RACA_TESTE",
            habilidades = listOf(RacialAbility(nome = "Adaptável", descricao = "", id = "ADAPTAVEL")),
            pontosRaciaisEsperados = 2
        )
        val config = AncestryVariantConfig(
            ancestralidadeId = "RACA_TESTE",
            livro = "BASICO",
            grupoVariante = VariantGroup(
                opcoes = listOf(
                    VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                    VariantOption(
                        id = "agil",
                        nome = "Ágil",
                        pacoteFixo = ResolvedTraitPackage(
                            tracosParaRemoverPorNome = listOf("Adaptável"),
                            tracosParaAdicionar = listOf(TraitAddition("Ágil", "AGIL"))
                        )
                    )
                )
            )
        )

        val results = useCase.execute(base, config)

        assertEquals(2, results.size)
        assertTrue(results.all { it.dentroDoOrcamento })
        assertTrue(results.all { it.saldo == 2 })
    }

    @Test
    fun `opcao desbalanceada e sinalizada sem afetar as outras opcoes da mesma raca`() {
        val base = RacialModifier(
            nome = "RACA_TESTE",
            habilidades = listOf(RacialAbility(nome = "Adaptável", descricao = "", id = "ADAPTAVEL")),
            pontosRaciaisEsperados = 2
        )
        val config = AncestryVariantConfig(
            ancestralidadeId = "RACA_TESTE",
            livro = "BASICO",
            grupoVariante = VariantGroup(
                opcoes = listOf(
                    VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                    // Adiciona Resistência (1 pt) sem remover nada — fica 1
                    // ponto acima do orçamento, de propósito.
                    VariantOption(
                        id = "errado",
                        nome = "Errado",
                        pacoteFixo = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Resistência", "RESISTENCIA"))
                        )
                    )
                )
            )
        )

        val results = useCase.execute(base, config)

        val padrao = results.first { it.optionId == "padrao" }
        val errado = results.first { it.optionId == "errado" }

        assertTrue(padrao.dentroDoOrcamento)
        assertEquals(2, padrao.saldo)

        assertFalse(errado.dentroDoOrcamento)
        assertEquals(3, errado.saldo)
    }

    @Test
    fun `selecao fixed package tambem e validada por opcao, mesmo padrao de terracota`() {
        val base = RacialModifier(
            nome = "RACA_TESTE",
            habilidades = emptyList(),
            pontosRaciaisEsperados = -2
        )
        val config = AncestryVariantConfig(
            ancestralidadeId = "RACA_TESTE",
            livro = "ARTE_DA_GUERRA",
            selecoes = listOf(
                SelectionDef(
                    id = "escolha",
                    rotulo = "Escolha a Complicação",
                    tipo = SelectionType.FIXED_PACKAGE,
                    pacotesFixos = listOf(
                        FixedPackageOption(
                            id = "voto",
                            nome = "Voto",
                            pacote = ResolvedTraitPackage(
                                desvantagensParaAdicionar = listOf(TraitAddition("Voto (Maior)", "VOTO_MAIOR"))
                            )
                        ),
                        FixedPackageOption(
                            id = "obrigacao",
                            nome = "Obrigação",
                            pacote = ResolvedTraitPackage(
                                desvantagensParaAdicionar = listOf(TraitAddition("Obrigação (Maior)", "OBRIGACAO_MAIOR"))
                            )
                        )
                    )
                )
            )
        )

        val results = useCase.execute(base, config)

        assertEquals(2, results.size)
        assertTrue(results.all { it.dentroDoOrcamento })
        assertTrue(results.all { it.saldo == -2 })
    }

    @Test
    fun `raca sem grupoVariante nem selecoes fixed package nao produz nenhum resultado`() {
        val base = RacialModifier(nome = "RACA_TESTE", pontosRaciaisEsperados = 2)
        val config = AncestryVariantConfig(ancestralidadeId = "RACA_TESTE", livro = "BASICO")

        val results = useCase.execute(base, config)

        assertTrue(results.isEmpty())
    }
}
