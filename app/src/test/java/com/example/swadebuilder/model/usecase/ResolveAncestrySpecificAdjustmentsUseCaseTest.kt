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
    fun `demonio abismo recebe aa demonio automaticamente`() {
        val result = useCase.execute("Demônio (Abismo)", null)

        assertEquals(listOf("aa_demonio"), result.ensureAdvantageIds)
        assertEquals(listOf("ANTECEDENTE ARCANO (DEMÔNIO)"), result.ensureAutomaticAdvantages)
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
        assertEquals(listOf("RESISTÊNCIA +1"), result.ensureAutomaticAdvantages)
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
        assertEquals(listOf("SENHOR DAS FERAS"), result.ensureAutomaticAdvantages)
    }

    @Test
    fun `feral recebe furioso sanguinario e bloqueio de chi`() {
        val result = useCase.execute(
            anc = "Feral",
            descendenteElementalSelecionado = null,
            scifiVariant = "Correnteza",
            ancestryOptions = listOf("Ápice", "Vínculo Bestial", "Pele Iluminada pela Lua", "Gatoruja", "Correnteza", "Pedregoso"),
            ancestryOrigin = "ARTE_DA_GUERRA"
        )

        assertTrue(result.ensureAdvantageNames.contains("FURIOSO"))
        assertTrue(result.ensureAutomaticAdvantages.contains("MOVIMENTAÇÃO +2"))
        assertEquals(listOf("SANGUINÁRIO"), result.ensureRacialDisadvantages)
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
            listOf("HABITANTE DE GRAVIDADE ZERO/BAIXA", "FORMA ALIENÍGENA", "SENTIDOS AGUÇADOS (Olhos de Águia)"),
            result.ensureRacialDisadvantages
        )
        assertEquals(listOf("NÃO SABE NADAR", "FRÁGIL"), result.racialDisadvantagesToRemove)
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
    fun `elfos comunitario remove desastrado e adiciona transtorno`() {
        val result = useCase.execute(
            anc = "ELFOS",
            descendenteElementalSelecionado = null,
            scifiVariant = "Comunitário",
            ancestryOptions = listOf("Básico", "Comunitário"),
            isSciFiActive = true
        )

        assertEquals(listOf("COMUNITÁRIO"), result.ensureAutomaticAdvantages)
        assertEquals(listOf("DESASTRADO"), result.automaticAdvantagesToRemove)
        assertEquals(listOf("TRANSTORNO DE SEPARAÇÃO"), result.ensureRacialDisadvantages)
        assertEquals(listOf("DESASTRADO"), result.racialDisadvantagesToRemove)
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

        assertEquals(listOf("FORTE", "RESISTÊNCIA +2"), result.ensureAutomaticAdvantages)
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
        assertEquals(listOf("FORTE", "RESISTÊNCIA +2"), result.ensureAutomaticAdvantages)
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

        assertEquals(listOf("FORTE", "RESISTÊNCIA +2"), result.ensureAutomaticAdvantages)
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

        assertEquals(listOf("MOVIMENTAÇÃO +4"), result.ensureAutomaticAdvantages)
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
        assertEquals(
            listOf("Combine com o mestre de jogo para equilibrar com 4 pontos de habilidades negativas que façam sentido\nno cenário."),
            energia.ensureRacialDisadvantages
        )
        assertTrue(energia.anotacoesToAdd.isEmpty())
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

        assertTrue(result.ensureRacialDisadvantages.contains("SENSÍVEL (Maior)"))
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

        assertTrue(result.ensureRacialDisadvantages.contains("SENSÍVEL (Maior)"))
        assertTrue(result.ensureRacialDisadvantages.contains("Combine com o mestre de jogo para equilibrar com 1 ponto de habilidade negativa que faça sentido ao cenário."))
        assertTrue(result.anotacoesToAdd.isEmpty())
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

        assertTrue(result.ensureRacialDisadvantages.contains("DEPENDÊNCIA ATMOSFÉRICA (Maior)"))
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

        assertTrue(result.ensureRacialDisadvantages.contains("HABITANTE DE GRAVIDADE BAIXA/ZERO"))
        assertTrue(result.racialDisadvantagesToRemove.contains("DEPENDÊNCIA ATMOSFÉRICA (Maior)"))
        assertTrue(result.ensureAdvantageIds.contains("adaptacao_gravitacional"))
        assertTrue(result.automaticAdvantagesToRemove.contains("FORTE"))
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

        assertEquals(listOf("PECULIARIDADE"), result.ensureRacialDisadvantages)
        assertTrue(result.ensureAutomaticAdvantages.isEmpty())
    }

}
