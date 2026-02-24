package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestryRacialPackageUseCaseTest {

    private val useCase = ResolveAncestryRacialPackageUseCase()

    @Test
    fun `removes previous free advantages and adds granted by ancestry`() {
        val sorte = Vantagem(
            id = "sorte",
            nome = "Sorte",
            categoria = Categoria.SOCIAIS,
            origem = "BASICO",
            requisitos = Requisito()
        )
        val lutador = Vantagem(
            id = "lutador",
            nome = "Lutador",
            categoria = Categoria.COMBATE,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "HUMANOS",
                descendenteElementalSelecionado = null,
                allAdvantages = listOf(sorte, lutador),
                selectedAdvantages = listOf(sorte),
                previousFreeAdvantageKeys = setOf("SORTE", "sorte"),
                ancestryGrantedAdvantages = listOf("Lutador"),
                ancestryAutomaticDisadvantages = listOf("Curioso")
            )
        )

        assertFalse(result.selectedAdvantages.any { it.id == "sorte" })
        assertTrue(result.selectedAdvantages.any { it.id == "lutador" })
        assertEquals(listOf("Lutador"), result.vantagensAutomaticas)
        assertEquals(listOf("Lutador"), result.vantagensRaciais)
        assertEquals(listOf("Curioso"), result.desvantagensRaciais)
    }

    @Test
    fun `applies ancestry specific automatic adjustments for saurios`() {
        val blindado = Vantagem(
            id = "blindado",
            nome = "Blindado",
            categoria = Categoria.COMBATE,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "SAURIOS",
                descendenteElementalSelecionado = null,
                allAdvantages = listOf(blindado),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.selectedAdvantages.isEmpty())
    }

    @Test
    fun `removes replaced automatic traits for aquarianos semi aquaticos`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "AQUARIANOS",
                descendenteElementalSelecionado = null,
                scifiVariant = "Semi-aquáticos",
                ancestryOptions = listOf("Básico", "Semi-aquáticos"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("Dependência", "Visão no Escuro", "Aquático", "Resistência"),
                ancestryAutomaticDisadvantages = listOf("Dependência")
            )
        )

        assertTrue(result.vantagensRaciais.any { it.equals("Semiaquático", ignoreCase = true) })
        assertTrue(result.vantagensRaciais.any { it.equals("Toque Venenoso", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("Aquático", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("Resistência", ignoreCase = true) })
    }

    @Test
    fun `removes replaced automatic traits for avianos ave de rapina`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "AVIANOS",
                descendenteElementalSelecionado = null,
                scifiVariant = "Ave de rapina",
                ancestryOptions = listOf("Básico", "Ave de rapina"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("Frágil", "Movimentação Reduzida", "Não Sabe Nadar", "Sentidos Aguçados", "Voo"),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )

        assertFalse(result.vantagensRaciais.any { it.equals("Frágil", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("Não Sabe Nadar", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.contains("FORMA ALIEN", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.contains("HABITANTE DE GRAVIDADE", ignoreCase = true) })
    }

    @Test
    fun `elfos comunitario substitui desastrado por transtorno de separacao`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "ELFOS",
                descendenteElementalSelecionado = null,
                scifiVariant = "Comunitário",
                ancestryOptions = listOf("Básico", "Comunitário"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("Desastrado", "Visão no Escuro"),
                ancestryAutomaticDisadvantages = listOf("DESASTRADO")
            )
        )

        assertFalse(result.desvantagensRaciais.any { it.equals("DESASTRADO", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("DESASTRADO", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.equals("TRANSTORNO DE SEPARAÇÃO", ignoreCase = true) })
        assertTrue(result.vantagensRaciais.any { it.equals("COMUNITÁRIO", ignoreCase = true) })
    }
}
