package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.TraitAddition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveAncestrySpecificAdjustmentsUseCaseTest {

    private val useCase = ResolveAncestrySpecificAdjustmentsUseCase()

    @Test
    fun `returns saurios adjustments`() {
        val result = useCase.execute("SAURIOS", null, racialAbilityIds = setOf("ARMADURA_2"))

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf(TraitAddition("PRONTIDÃO", "PRONTIDAO")), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `saurios without the armadura_2 trait id gets no natural armor`() {
        val result = useCase.execute("SAURIOS", null)

        assertEquals(0, result.naturalArmorFromRace)
    }

    @Test
    fun `returns golens adjustments`() {
        val result = useCase.execute("GOLENS", null, racialAbilityIds = setOf("ARMADURA_2"))

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns draconianos adjustments`() {
        val result = useCase.execute("DRACONIANOS", null, racialAbilityIds = setOf("ARMADURA_2"))

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns insetoides adjustments`() {
        val result = useCase.execute("INSETOIDES", null, racialAbilityIds = setOf("ARMADURA_2"))

        assertEquals(2, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(listOf(TraitAddition("GARRAS", "GARRAS")), result.ensureAutomaticAdvantages)
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `returns pequeninos adjustments`() {
        val result = useCase.execute("PEQUENINOS", null)

        assertEquals(listOf("Sorte", "Espirituoso"), result.ensureAdvantageNames)
        assertEquals(
            listOf(
                TraitAddition("Tamanho -1", "TAMANHO_MENOS_1"),
                TraitAddition("Movimentação Reduzida", "MOVIMENTACAO_REDUZIDA")
            ),
            result.ensureRacialDisadvantages
        )
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
    fun `demonio abismo recebe aa demonio automaticamente`() {
        val result = useCase.execute("Demônio (Abismo)", null)

        assertEquals(listOf("aa_demonio"), result.ensureAdvantageIds)
        assertEquals(
            listOf(TraitAddition("ANTECEDENTE ARCANO (DEMÔNIO)", "ANTECEDENTE_ARCANO_DEMONIO")),
            result.ensureAutomaticAdvantages
        )
        assertTrue(result.forceArmorZero)
    }

    @Test
    fun `umvee pedregoso aplica resistencia e armadura raciais`() {
        val result = useCase.execute(
            anc = "Umvee (Filhos da Lua)",
            descendenteElementalSelecionado = null,
            scifiVariant = "Pedregoso",
            ancestryOptions = listOf("Ápice", "Vínculo Bestial", "Pele Iluminada pela Lua", "Gatoruja", "Correnteza", "Pedregoso"),
            ancestryOrigin = "ARTE_DA_GUERRA"
        )

        assertEquals(2, result.naturalArmorFromRace)
        assertEquals(listOf(TraitAddition("RESISTÊNCIA +1", "RESISTENCIA")), result.ensureAutomaticAdvantages)
    }

    @Test
    fun `umvee vinculo bestial concede senhor das feras`() {
        val result = useCase.execute(
            anc = "Umvee (Filhos da Lua)",
            descendenteElementalSelecionado = null,
            scifiVariant = "Vínculo Bestial",
            ancestryOptions = listOf("Ápice", "Vínculo Bestial", "Pele Iluminada pela Lua", "Gatoruja", "Correnteza", "Pedregoso"),
            ancestryOrigin = "ARTE_DA_GUERRA"
        )

        assertEquals(listOf("SENHOR DAS FERAS"), result.ensureAdvantageNames)
        assertEquals(listOf(TraitAddition("SENHOR DAS FERAS", "SENHOR_DAS_FERAS")), result.ensureAutomaticAdvantages)
    }

    @Test
    fun `feral recebe furioso garras sanguinario e bloqueio de chi sem nenhuma opcao de variante`() {
        // Feral não tem Variante nem Seleção de dom da natureza (diferente do
        // Umvee) — é uma raça própria com traços fixos, por isso o teste não
        // passa nenhuma ancestryOptions.
        val result = useCase.execute(
            anc = "Feral",
            descendenteElementalSelecionado = null,
            scifiVariant = null,
            ancestryOptions = emptyList(),
            ancestryOrigin = "ARTE_DA_GUERRA"
        )

        assertTrue(result.ensureAdvantageNames.contains("FURIOSO"))
        assertTrue(result.ensureAutomaticAdvantages.any { it.nome == "GARRAS" })
        assertEquals(listOf(TraitAddition("SANGUINÁRIO", "SANGUINARIO")), result.ensureRacialDisadvantages)
        assertTrue(result.anotacoesToAdd.any { it.contains("Técnicas de Chi") })
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
            listOf(
                TraitAddition("HABITANTE DE GRAVIDADE ZERO/BAIXA", "HABITANTE_DE_GRAVIDADE_ZERO_BAIXA"),
                TraitAddition("FORMA ALIENÍGENA", "FORMA_ALIENIGENA"),
                TraitAddition("SENTIDOS AGUÇADOS (Olhos de Águia)", "SENTIDOS_AGUCADOS_OLHOS_DE_AGUIA")
            ),
            result.ensureRacialDisadvantages
        )
        assertEquals(listOf("NÃO SABE NADAR", "NÃO SABE NADAR (Menor)", "FRÁGIL"), result.racialDisadvantagesToRemove)
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

        assertEquals(
            listOf(TraitAddition("FRÁGIL", "FRAGIL"), TraitAddition("NÃO SABE NADAR", "NAO_SABE_NADAR")),
            result.ensureRacialDisadvantages
        )
    }

    @Test
    fun `elfos comunitario remove desastrado e adiciona transtorno`() {
        val result = useCase.execute(
            anc = "ELFOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Comunitário",
            ancestryOptions = listOf("Básico", "Comunitário"),
            isSciFiActive = true
        )

        assertEquals(listOf(TraitAddition("COMUNITÁRIO", "COMUNITARIO")), result.ensureAutomaticAdvantages)
        assertEquals(listOf("DESASTRADO"), result.automaticAdvantagesToRemove)
        assertEquals(
            listOf(TraitAddition("TRANSTORNO DE SEPARAÇÃO", "TRANSTORNO_DE_SEPARACAO")),
            result.ensureRacialDisadvantages
        )
        assertEquals(listOf("DESASTRADO", "DESASTRADO (Menor)"), result.racialDisadvantagesToRemove)
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

        assertEquals(
            listOf(TraitAddition("FORTE", "FORTE"), TraitAddition("RESISTÊNCIA +2", "RESISTENCIA_2")),
            result.ensureAutomaticAdvantages
        )
    }



    @Test
    fun `drakens nao recebem armadura racial e mantem resistencia dois`() {
        val result = useCase.execute(
            anc = "DRAKENS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Padrão",
            ancestryOptions = listOf("Padrão", "Dragão"),
            isSciFiActive = true
        )

        assertEquals(0, result.naturalArmorFromRace)
        assertEquals(
            listOf(TraitAddition("FORTE", "FORTE"), TraitAddition("RESISTÊNCIA +2", "RESISTENCIA_2")),
            result.ensureAutomaticAdvantages
        )
    }


    @Test
    fun `elementais scifi padrao mantem forte e resistencia mais dois`() {
        val result = useCase.execute(
            anc = "ELEMENTAIS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Padrão",
            ancestryOptions = listOf("Padrão", "Ar, Fogo ou Água"),
            isSciFiActive = true
        )

        assertEquals(
            listOf(TraitAddition("FORTE", "FORTE"), TraitAddition("RESISTÊNCIA +2", "RESISTENCIA_2")),
            result.ensureAutomaticAdvantages
        )
        assertEquals(0, result.naturalArmorFromRace)
    }

    @Test
    fun `elementais scifi ar fogo ou agua troca forte por forma de energia`() {
        val result = useCase.execute(
            anc = "ELEMENTAIS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Ar, Fogo ou Água",
            ancestryOptions = listOf("Padrão", "Ar, Fogo ou Água"),
            isSciFiActive = true
        )

        assertEquals(listOf(TraitAddition("FORMA DE ENERGIA", "FORMA_DE_ENERGIA")), result.ensureAutomaticAdvantages)
        assertEquals(0, result.naturalArmorFromRace)
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
    fun `centaux gazela troca bonus de tamanho e movimento no scifi`() {
        val result = useCase.execute(
            anc = "CENTAUX",
            descendenteElementalSelecionado = null,
            scifiVariant = "Gazela",
            ancestryOptions = listOf("Padrão", "Gazela"),
            isSciFiActive = true
        )

        assertEquals(listOf(TraitAddition("MOVIMENTAÇÃO +4", "MOVIMENTACAO_4")), result.ensureAutomaticAdvantages)
        assertEquals(listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"), result.automaticAdvantagesToRemove)
        assertEquals(listOf("GRANDE"), result.racialDisadvantagesToRemove)
    }



    @Test
    fun `oraculos variante aterrorizado usa poderes misticos sem nocao do perigo`() {
        val result = useCase.execute(
            anc = "ORÁCULOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Aterrorizado",
            ancestryOptions = listOf("Padrão", "Aterrorizado"),
            isSciFiActive = true
        )

        assertTrue(result.ensureAdvantageIds.contains("poderes_misticos"))
        assertTrue(result.ensureAdvantageNames.isEmpty())
    }


    @Test
    fun `possessores nao devem injetar nocao do perigo em nenhuma variante`() {
        val padrao = useCase.execute(
            anc = "POSSESSORES",
            descendenteElementalSelecionado = null,
            scifiVariant = "Padrão",
            ancestryOptions = listOf("Padrão", "Energia"),
            isSciFiActive = true
        )
        val energia = useCase.execute(
            anc = "POSSESSORES",
            descendenteElementalSelecionado = null,
            scifiVariant = "Energia",
            ancestryOptions = listOf("Padrão", "Energia"),
            isSciFiActive = true
        )

        assertTrue(padrao.automaticAdvantagesToRemove.any { it.contains("NOÇÃO", ignoreCase = true) })
        assertTrue(energia.automaticAdvantagesToRemove.any { it.contains("NOÇÃO", ignoreCase = true) })
        // A nota pro mestre é anotação, não uma desvantagem de traço real —
        // mora em anotacoesToAdd (ver AncestryVariantRegistry.possessores).
        assertTrue(energia.ensureRacialDisadvantages.isEmpty())
        assertTrue(
            energia.anotacoesToAdd.any {
                it.contains("Combine com o mestre de jogo para equilibrar com 4 pontos")
            }
        )
    }


    @Test
    fun `quadroides padrao inclui sensivel maior`() {
        val result = useCase.execute(
            anc = "QUADROIDES",
            descendenteElementalSelecionado = null,
            scifiVariant = "Padrão",
            ancestryOptions = listOf("Padrão", "Habilidoso"),
            isSciFiActive = true
        )

        assertTrue(result.ensureRacialDisadvantages.any { it.nome == "SENSÍVEL (Maior)" })
    }

    @Test
    fun `quadroides habilidoso inclui anotacao racial e sensivel maior`() {
        val result = useCase.execute(
            anc = "QUADROIDES",
            descendenteElementalSelecionado = null,
            scifiVariant = "Habilidoso",
            ancestryOptions = listOf("Padrão", "Habilidoso"),
            isSciFiActive = true
        )

        assertTrue(result.ensureRacialDisadvantages.any { it.nome == "SENSÍVEL (Maior)" })
        // A nota pro mestre é anotação, não uma desvantagem de traço real —
        // mora em anotacoesToAdd (ver AncestryVariantRegistry.quadroides).
        assertTrue(
            result.anotacoesToAdd.any {
                it.contains("Combine com o mestre de jogo para equilibrar com 1 ponto")
            }
        )
    }

    @Test
    fun `mineradores geneticos padrao usa dependencia atmosferica maior`() {
        val result = useCase.execute(
            anc = "MINERADORES GENÉTICOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Padrão",
            ancestryOptions = listOf("Padrão", "Zero G"),
            isSciFiActive = true
        )

        assertTrue(result.ensureAutomaticAdvantages.any { it.nome == "DEPENDÊNCIA ATMOSFÉRICA" })
    }

    @Test
    fun `mineradores geneticos zero g remove dependencia atmosferica`() {
        val result = useCase.execute(
            anc = "MINERADORES GENÉTICOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Zero G",
            ancestryOptions = listOf("Padrão", "Zero G"),
            isSciFiActive = true
        )

        assertTrue(result.ensureRacialDisadvantages.any { it.nome == "HABITANTE DE GRAVIDADE ZERO/BAIXA (Maior)" })
        assertTrue(result.racialDisadvantagesToRemove.contains("DEPENDÊNCIA ATMOSFÉRICA (Maior)"))
        assertTrue(result.ensureAdvantageIds.contains("adaptacao_gravitacional"))
        assertTrue(result.automaticAdvantagesToRemove.contains("FORTE"))
        assertTrue(result.automaticAdvantagesToRemove.contains("DEPENDÊNCIA ATMOSFÉRICA"))
    }

    @Test
    fun `terracota voto concede complicacao voto maior`() {
        val result = useCase.execute(
            anc = "TERRACOTA",
            descendenteElementalSelecionado = null,
            scifiVariant = "Voto (Maior)",
            ancestryOptions = listOf("Voto (Maior)", "Obrigação (Maior)")
        )

        assertEquals(listOf(TraitAddition("VOTO (Maior)", "VOTO_MAIOR")), result.ensureRacialDisadvantages)
    }

    @Test
    fun `terracota obrigacao concede complicacao obrigacao maior`() {
        val result = useCase.execute(
            anc = "TERRACOTA",
            descendenteElementalSelecionado = null,
            scifiVariant = "Obrigação (Maior)",
            ancestryOptions = listOf("Voto (Maior)", "Obrigação (Maior)")
        )

        assertEquals(listOf(TraitAddition("OBRIGAÇÃO (Maior)", "OBRIGACAO_MAIOR")), result.ensureRacialDisadvantages)
    }

    @Test
    fun `returns fallback for unknown ancestry`() {
        val result = useCase.execute("QUALQUER", null)

        assertEquals(0, result.naturalArmorFromRace)
        assertTrue(result.forceArmorZero)
        assertTrue(result.ensureAdvantageNames.isEmpty())
        assertEquals(ResolveAncestrySpecificAdjustmentsUseCase.ElementalAction.NONE, result.elementalAction)
    }

    @Test
    fun `akaimimi recebe peculiaridade como desvantagem racial`() {
        val result = useCase.execute("Akaimimi (Panda Vermelho)", null)

        assertEquals(listOf(TraitAddition("PECULIARIDADE", "PECULIARIDADE")), result.ensureRacialDisadvantages)
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
    }

}
