package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
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
        // We need an extra advantage so that when one is removed by Human transition (Frenesi),
        // another one remains (Corajoso) to be checked for validity.
        // "Corajoso" requires Spirit d6 (default 4), so with meetsRequirements={false} it should be invalid.
        val extraAdvantage = Vantagem(
            id = "corajoso",
            nome = "Corajoso",
            categoria = Categoria.SOCIAIS,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val params = baseParams(
            previousAncestry = "HUMANOS",
            targetAncestry = "ELFOS",
            compendioArteDaGuerraAtivo = false,
            signoAdgSelecionado = null,
            meetsRequirements = { false }, // Force verification failure
            extraAdvantages = listOf(extraAdvantage)
        )

        val result = useCase.execute(params)

        assertEquals(ApplyAncestryChangeCoordinatorUseCase.SignoAction.KEEP, result.signoAction)
        // One advantage removed by Human transition (Frenesi), one remains (Corajoso).
        // Corajoso fails validation, so it should be in removedAdvantages.
        assertFalse(result.invalidAdvantagesResolution.removedAdvantages.isEmpty())
        assertEquals(1, result.invalidAdvantagesResolution.removedAdvantages.size)
        assertEquals("corajoso", result.invalidAdvantagesResolution.removedAdvantages[0].id)
    }



    @Test
    fun `uses previous automatic disadvantages to remove stale racial complications`() {
        val naoSabeNadar = Complicacao(
            id = "nao_sabe_nadar",
            name = "NÃO SABE NADAR",
            severity = "menor",
            description = "desc",
            origem = "BASICO"
        )
        val habitante = Complicacao(
            id = "habitante_de_gravidade_baixa",
            name = "HABITANTE DE GRAVIDADE ZERO/BAIXA",
            severity = "menor",
            description = "desc",
            origem = "SCIFI"
        )

        val params = baseParams(
            previousAncestry = "AVIANOS",
            targetAncestry = "AVIANOS"
        ).copy(
            availableComplications = listOf(naoSabeNadar, habitante),
            selectedComplications = mapOf(naoSabeNadar to "Menor"),
            previousAutomaticDisadvantages = listOf("NÃO SABE NADAR"),
            compendioSciFiAtivo = true,
            scifiVariant = "Ave de rapina",
            previousAncestryDef = RacialModifier(
                nome = "AVIANOS",
                vantagensGratis = emptyList(),
                desvantagens = listOf("NÃO SABE NADAR"),
                atributos = emptyMap(),
                pericias = emptyMap(),
                habilidades = emptyList(),
                origem = "SCIFI"
            ),
            targetAncestryDef = RacialModifier(
                nome = "AVIANOS",
                vantagensGratis = emptyList(),
                desvantagens = emptyList(),
                atributos = emptyMap(),
                pericias = emptyMap(),
                habilidades = emptyList(),
                origem = "SCIFI",
                opcoes = listOf("Básico", "Ave de rapina")
            )
        )

        val result = useCase.execute(params)
        val selectedNames = result.complicationsSnapshot.selectedComplications.keys.map { it.name }

        assertFalse(selectedNames.contains("NÃO SABE NADAR"))
        assertTrue(selectedNames.contains("HABITANTE DE GRAVIDADE ZERO/BAIXA"))
    }

    @Test
    fun `forces loss of human free edge on scifi baixa gravidade variant`() {
        val adaptavel = Vantagem(
            id = "adaptavel",
            nome = "Adaptável",
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

        val params = baseParams(
            previousAncestry = "HUMANOS",
            targetAncestry = "HUMANOS"
        ).copy(
            currentAutomaticAdvantages = listOf("ADAPTÁVEL"),
            compendioSciFiAtivo = true,
            scifiVariant = "Baixa Gravidade",
            previousAncestryDef = RacialModifier(
                nome = "HUMANOS",
                vantagensGratis = listOf("ADAPTÁVEL"),
                desvantagens = emptyList(),
                atributos = emptyMap(),
                pericias = emptyMap(),
                habilidades = emptyList(),
                origem = "SCIFI",
                opcoes = listOf("Básico", "Baixa Gravidade")
            ),
            targetAncestryDef = RacialModifier(
                nome = "HUMANOS",
                vantagensGratis = listOf("ADAPTÁVEL"),
                desvantagens = emptyList(),
                atributos = emptyMap(),
                pericias = emptyMap(),
                habilidades = emptyList(),
                origem = "SCIFI",
                opcoes = listOf("Básico", "Baixa Gravidade")
            ),
            vantagensSelecionadas = listOf(adaptavel, lutador),
            allAdvantages = listOf(adaptavel, lutador)
        )

        val result = useCase.execute(params)

        assertEquals("lutador", result.humanTransition.vantagemRemovida?.id)
    }

    private fun baseParams(
        previousAncestry: String = "ELFOS",
        targetAncestry: String = "HUMANOS",
        compendioArteDaGuerraAtivo: Boolean = false,
        signoAdgSelecionado: String? = null,
        meetsRequirements: (Vantagem) -> Boolean = { true },
        extraAdvantages: List<Vantagem> = emptyList()
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
            vantagensGratis = listOf("Sorte"), // Target grants Sorte
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

        // Use "Frenesi" as the selected advantage, which is NOT granted by Humanos.
        // This ensures the validation logic runs on it.
        val selectedAdvantages = extraAdvantages + listOf(
            Vantagem(
                id = "frenesi",
                nome = "Frenesi",
                categoria = Categoria.COMBATE,
                origem = "BASICO",
                requisitos = Requisito()
            )
        )

        return ApplyAncestryChangeCoordinatorUseCase.Params(
            previousAncestry = previousAncestry,
            targetAncestry = targetAncestry,
            previousAncestryDef = previousDef,
            targetAncestryDef = targetDef,
            currentAutomaticAdvantages = emptyList(),
            previousAutomaticDisadvantages = emptyList(),
            pontosVantagemAtuais = 2,
            vantagensSelecionadas = selectedAdvantages,
            attributeNames = listOf("Força"),
            attributeCaps = mapOf(
                "Força" to AdjustAttributesForAncestryChangeUseCase.AttributeCap(minRaw = 4, maxRaw = 12)
            ),
            paCostStacks = mapOf("Força" to listOf(1, 1)),
            descendenteElementalSelecionado = null,
            anoesScifiSelecionado = null,
            scifiVariant = null,
            humanoMineradorAtributo = null,
            allAdvantages = selectedAdvantages + ResolveAncestryRacialPackageUseCaseTestFixtures.sampleAdvantages(), // Ensure Sorte is also known if needed
            availableComplications = listOf(complicacao),
            selectedComplications = mapOf(complicacao to "Menor"),
            automaticTropoAdvantageIds = emptySet(),
            meetsRequirements = meetsRequirements,
            originPriorityResolver = { 0 },
            compendioArteDaGuerraAtivo = compendioArteDaGuerraAtivo,
            compendioSciFiAtivo = false,
            signoAdgSelecionado = signoAdgSelecionado,
            modoSupers = false,
            meioElfoAgil = false
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
