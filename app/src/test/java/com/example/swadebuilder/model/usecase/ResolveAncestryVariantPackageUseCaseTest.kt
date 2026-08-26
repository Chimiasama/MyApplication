package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryVariantPackageUseCaseTest {

    private val useCase = ResolveAncestryVariantPackageUseCase()

    @Test
    fun `ancestralidade desconhecida retorna pacote vazio`() {
        val result = useCase.resolve(
            ancestralidadeId = "RACA_QUE_NAO_EXISTE",
            variantOptionId = null,
            selectionAnswers = emptyList()
        )

        assertEquals(ResolvedTraitPackage(), result)
    }

    @Test
    fun `terracota escolhe voto`() {
        val result = useCase.resolve(
            ancestralidadeId = "TERRACOTA",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "terracota_complicacao", fixedPackageChoiceId = "voto")
            )
        )

        assertEquals(listOf("VOTO (Maior)"), result.desvantagensParaAdicionar)
    }

    @Test
    fun `terracota escolhe obrigacao`() {
        val result = useCase.resolve(
            ancestralidadeId = "TERRACOTA",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "terracota_complicacao", fixedPackageChoiceId = "obrigacao")
            )
        )

        assertEquals(listOf("OBRIGAÇÃO (Maior)"), result.desvantagensParaAdicionar)
    }

    @Test
    fun `terracota sem resposta cai no primeiro pacote (voto)`() {
        val result = useCase.resolve(
            ancestralidadeId = "TERRACOTA",
            variantOptionId = null,
            selectionAnswers = emptyList()
        )

        assertEquals(listOf("VOTO (Maior)"), result.desvantagensParaAdicionar)
    }

    @Test
    fun `umvee vinculo bestial concede vantagem de verdade`() {
        val result = useCase.resolve(
            ancestralidadeId = "UMVEE (FILHOS DA LUA)",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "umvee_dom_da_natureza", fixedPackageChoiceId = "vinculo_bestial")
            )
        )

        assertEquals(listOf("SENHOR DAS FERAS"), result.vantagensGratisParaAdicionar)
        assertTrue(result.tracosParaAdicionar.isEmpty())
    }

    @Test
    fun `umvee pedregoso concede traco de resistencia`() {
        val result = useCase.resolve(
            ancestralidadeId = "UMVEE (FILHOS DA LUA)",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "umvee_dom_da_natureza", fixedPackageChoiceId = "pedregoso")
            )
        )

        assertEquals(listOf("RESISTÊNCIA +1"), result.tracosParaAdicionar)
    }

    @Test
    fun `umvee sem resposta cai no primeiro dom (apice)`() {
        val result = useCase.resolve(
            ancestralidadeId = "UMVEE (FILHOS DA LUA)",
            variantOptionId = null,
            selectionAnswers = emptyList()
        )

        assertEquals(listOf("GARRAS"), result.tracosParaAdicionar)
    }

    @Test
    fun `elementais padrao mantem forte e resistencia`() {
        val result = useCase.resolve(
            ancestralidadeId = "ELEMENTAIS",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "elementais_scifi_elemento", fixedPackageChoiceId = "padrao")
            )
        )

        assertEquals(listOf("FORTE", "RESISTÊNCIA +2"), result.tracosParaAdicionar)
    }

    @Test
    fun `elementais ar fogo ou agua troca por forma de energia`() {
        val result = useCase.resolve(
            ancestralidadeId = "ELEMENTAIS",
            variantOptionId = null,
            selectionAnswers = listOf(
                SelectionAnswer(selectionId = "elementais_scifi_elemento", fixedPackageChoiceId = "ar_fogo_ou_agua")
            )
        )

        assertEquals(listOf("FORMA DE ENERGIA"), result.tracosParaAdicionar)
    }

    @Test
    fun `anoes ciber combina pacote fixo da variante com o catalogo de tracos negativos`() {
        val result = useCase.resolve(
            ancestralidadeId = "ANOES",
            variantOptionId = "ciber",
            selectionAnswers = emptyList(),
            catalogPackages = mapOf(
                "anao_ciber_tracos_negativos" to ResolvedTraitPackage(
                    desvantagensParaAdicionar = listOf("Frágil", "Aparar -1")
                )
            )
        )

        assertEquals(listOf("CIBERTOLERÂNCIA"), result.vantagensGratisParaAdicionar)
        assertEquals(listOf("Frágil", "Aparar -1"), result.desvantagensParaAdicionar)
    }

    @Test
    fun `anoes sem variante (basico) nao concede nada`() {
        val result = useCase.resolve(
            ancestralidadeId = "ANOES",
            variantOptionId = null,
            selectionAnswers = emptyList()
        )

        assertEquals(ResolvedTraitPackage(), result)
    }

    @Test
    fun `anoes com id de variante desconhecido se comporta como basico`() {
        val result = useCase.resolve(
            ancestralidadeId = "ANOES",
            variantOptionId = "variante_que_nao_existe",
            selectionAnswers = emptyList()
        )

        assertEquals(ResolvedTraitPackage(), result)
    }
}
