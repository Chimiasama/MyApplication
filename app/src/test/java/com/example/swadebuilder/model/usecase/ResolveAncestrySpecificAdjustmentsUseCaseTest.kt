package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestrySpecificAdjustmentsUseCaseTest {

    private val useCase = ResolveAncestrySpecificAdjustmentsUseCase()

    @Test
    fun `returns saurios adjustments`() {
        val result = useCase.execute("SAURIOS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf("PRONTIDÃO"), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns golens adjustments`() {
        val result = useCase.execute("GOLENS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns draconianos adjustments`() {
        val result = useCase.execute("DRACONIANOS", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns insetoides adjustments`() {
        val result = useCase.execute("INSETOIDES", null)

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf("GARRAS"), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns pequeninos adjustments`() {
        val result = useCase.execute("PEQUENINOS", null)

        assertEquals(listOf("Sorte", "Espirituoso"), result.ensureAdvantageNames)
        assertEquals(listOf("Tamanho -1", "Movimentação Reduzida"), result.ensureRacialDisadvantages)
        assertTrue(result.forceArmorZero)
    }

    @Test
    fun `returns descendente elemental action based on current selection`() {
        val withoutCurrent = useCase.execute("DESCENDENTE ELEMENTAL", null)
        val withCurrent = useCase.execute("DESCENDENTE ELEMENTAL", "Fogo")

        assertEquals(
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.SELECT_DEFAULT,
            withoutCurrent.elementalAction
        )
        assertEquals(
            ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.REAPPLY_CURRENT,
            withCurrent.elementalAction
        )
    }



    @Test
    fun `returns avianos ave de rapina adjustments for scifi`() {
        val result = useCase.execute(
            anc = "AVIANOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Ave de rapina",
            isSciFiActive = true,
            ancestryOptions = listOf("Básico", "Ave de rapina")
        )

        assertEquals(
            listOf("HABITANTE DE GRAVIDADE ZERO/BAIXA", "FORMA ALIENÍGENA", "SENTIDOS AGUÇADOS (Olhos de Águia)"),
            result.ensureRacialDisadvantages
        )
        assertEquals(listOf("NÃO SABE NADAR"), result.racialDisadvantagesToRemove)
        assertTrue(result.anotacoesToAdd.isEmpty())
    }

    @Test
    fun `returns avianos basico adjustments for scifi`() {
        val result = useCase.execute(
            anc = "AVIANOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Básico",
            isSciFiActive = true
        )

        assertEquals(listOf("FRÁGIL", "NÃO SABE NADAR"), result.ensureRacialDisadvantages)
    }


    @Test
    fun `normalizes basico to padrao for scifi ancestries that define padrao`() {
        val result = useCase.execute(
            anc = "DRAKENS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Básico",
            ancestryOptions = listOf("Padrão", "Dragão"),
            isSciFiActive = true
        )

        assertEquals(listOf("FORTE"), result.ensureAutomaticAdvantages)
    }


    @Test
    fun `aquarianos basico nao injeta resistencia por hardcode`() {
        val result = useCase.execute(
            anc = "AQUARIANOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Básico",
            ancestryOptions = listOf("Básico", "Semi-aquáticos"),
            isSciFiActive = true
        )

        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
    }

    @Test
    fun `aquarianos semi aquaticos remove tracos substituidos`() {
        val result = useCase.execute(
            anc = "AQUARIANOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Semi-aquáticos",
            ancestryOptions = listOf("Básico", "Semi-aquáticos"),
            isSciFiActive = true
        )

        assertEquals(listOf("AQUÁTICO", "RESISTÊNCIA"), result.automaticAdvantagesToRemove)
    }

    @Test
    fun `humanos baixa gravidade remove adaptavel`() {
        val result = useCase.execute(
            anc = "HUMANOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Baixa Gravidade",
            ancestryOptions = listOf("Básico", "Baixa Gravidade", "Minerador"),
            isSciFiActive = true
        )

        assertEquals(listOf("ADAPTÁVEL", "ADAPTAVEL"), result.automaticAdvantagesToRemove)
    }

    @Test
    fun `returns fallback for unknown ancestry`() {
        val result = useCase.execute("QUALQUER", null)

        assertEquals(0, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }
}
