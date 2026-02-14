package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyAncestryChangeCoordinatorUseCaseTest {

    private val useCase = ApplyAncestryChangeCoordinatorUseCase()

    @Test
    fun `selects signo none for humano in arte da guerra when no signo selected`() {
        val params = baseParams(
            targetAncestry = "HUMANOS",
            compendioArteDaGuerraAtivo = true,
            signoAdgSelecionado = null
        )

        val result = useCase.execute(params)

        assertEquals(ApplyAncestryChangeCoordinatorUseCase.SignoAction.SELECT_NONE, result.signoAction)
        assertTrue(result.clearDescendenteElemental)
        assertTrue(result.clearPericiaGnomo)
    }

    @Test
    fun `keeps signo action when not required and resolves invalid advantages`() {
        val params = baseParams(
            previousAncestry = "HUMANOS",
            targetAncestry = "ELFOS",
            compendioArteDaGuerraAtivo = false,
            signoAdgSelecionado = null,
            meetsRequirements = { false }
        )

        val result = useCase.execute(params)

        assertEquals(ApplyAncestryChangeCoordinatorUseCase.SignoAction.KEEP, result.signoAction)
        assertFalse(result.invalidAdvantagesResolution.removedAdvantages.isEmpty())
        // Points refund is handled by the caller based on removedAdvantages size
        assertEquals(1, result.invalidAdvantagesResolution.removedAdvantages.size)
    }

    private fun baseParams(
        previousAncestry: String = "ELFOS",
        targetAncestry: String = "HUMANOS",
        compendioArteDaGuerraAtivo: Boolean = false,
        signoAdgSelecionado: String? = null,
        meetsRequirements: (Vantagem) -> Boolean = { true }
    ): ApplyAncestryChangeCoordinatorUseCase.Params {
        val previousDef = RacialModifier(
            nome = previousAncestry,
            vantagensGratis = emptyList(),
            desvantagens = emptyList(),
            atributos = emptyMap(),
            pericias = emptyMap(),
            habilidades = emptyList(),
            origem = "BASICO"
        )
        val targetDef = RacialModifier(
            nome = targetAncestry,
            vantagensGratis = listOf("Sorte"),
            desvantagens = emptyList(),
            atributos = emptyMap(),
            pericias = emptyMap(),
            habilidades = emptyList(),
            origem = "BASICO"
        )
        val complicacao = Complicacao(
            id = "medo",
            name = "Medo",
            severity = "menor",
            description = "desc",
            origem = "BASICO"
        )

        return ApplyAncestryChangeCoordinatorUseCase.Params(
            previousAncestry = previousAncestry,
            targetAncestry = targetAncestry,
            previousAncestryDef = previousDef,
            targetAncestryDef = targetDef,
            currentAutomaticAdvantages = emptyList(),
            pontosVantagemAtuais = 2,
            vantagensSelecionadas = ResolveAncestryRacialPackageUseCaseTestFixtures.sampleAdvantages(),
            attributeNames = listOf("Força"),
            attributeCaps = mapOf(
                "Força" to AdjustAttributesForAncestryChangeUseCase.AttributeCap(minRaw = 4, maxRaw = 12)
            ),
            paCostStacks = mapOf("Força" to listOf(1, 1)),
            descendenteElementalSelecionado = null,
            allAdvantages = ResolveAncestryRacialPackageUseCaseTestFixtures.sampleAdvantages(),
            availableComplications = listOf(complicacao),
            selectedComplications = mapOf(complicacao to "Menor"),
            automaticTropoAdvantageIds = emptySet(),
            meetsRequirements = meetsRequirements,
            originPriorityResolver = { 0 },
            compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
            signoAdgSelecionado = signoAdgSelecionado,
            modoSupers = false
        )
    }
}

private object ResolveAncestryRacialPackageUseCaseTestFixtures {
    fun sampleAdvantages() = listOf(
        Vantagem(
            id = "sorte",
            nome = "Sorte",
            categoria = Categoria.SOCIAIS,
            origem = "BASICO",
            requisitos = Requisito()
        )
    )
}
