package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AnaoCiberTraitSelection
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
                ancestryAutomaticDisadvantages = emptyList(),
                racialAbilityIds = setOf("ARMADURA_2")
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
    fun `anoes ciber sem selecao usa mensagem padrao de escolha pendente`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "ANOES",
                descendenteElementalSelecionado = null,
                scifiVariant = "Ciber",
                ancestryOptions = listOf("Básico", "Ciber"),
                isSciFiActive = true,
                anaoCiberTracosSelecionados = emptyList(),
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )

        assertTrue(result.vantagensRaciais.any { it.equals("CIBERTOLERÂNCIA", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.contains("escolha até 2 pontos", ignoreCase = true) })
    }

    @Test
    fun `anoes ciber aplica tracos mecanicos escolhidos dentro do orcamento`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "ANOES",
                descendenteElementalSelecionado = null,
                scifiVariant = "Ciber",
                ancestryOptions = listOf("Básico", "Ciber"),
                isSciFiActive = true,
                anaoCiberTracosSelecionados = listOf(
                    AnaoCiberTraitSelection(traitId = "fragil"),
                    AnaoCiberTraitSelection(traitId = "aparar_baixo")
                ),
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )

        assertTrue(result.desvantagensRaciais.any { it.equals("Frágil", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.equals("Aparar -1", ignoreCase = true) })
        assertFalse(result.desvantagensRaciais.any { it.contains("escolha até 2 pontos", ignoreCase = true) })
    }

    @Test
    fun `anoes ciber ignora selecao que estoura o orcamento de pontos`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "ANOES",
                descendenteElementalSelecionado = null,
                scifiVariant = "Ciber",
                ancestryOptions = listOf("Básico", "Ciber"),
                isSciFiActive = true,
                anaoCiberTracosSelecionados = listOf(
                    AnaoCiberTraitSelection(traitId = "fragil"),
                    AnaoCiberTraitSelection(traitId = "aparar_baixo"),
                    AnaoCiberTraitSelection(traitId = "tamanho_menos_1")
                ),
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )

        assertFalse(result.desvantagensRaciais.any { it.equals("Frágil", ignoreCase = true) })
        assertFalse(result.desvantagensRaciais.any { it.equals("Aparar -1", ignoreCase = true) })
        assertFalse(result.desvantagensRaciais.any { it.equals("Tamanho -1", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.contains("escolha até 2 pontos", ignoreCase = true) })
    }


    @Test
    fun `centaux gazela remove tamanho grande e usa movimentacao mais quatro`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "CENTAUX",
                descendenteElementalSelecionado = null,
                scifiVariant = "Gazela",
                ancestryOptions = listOf("Padrão", "Gazela"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                ancestryAutomaticDisadvantages = listOf("GRANDE")
            )
        )

        assertTrue(result.vantagensRaciais.any { it.equals("MOVIMENTAÇÃO +4", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("TAMANHO +2", ignoreCase = true) })
        assertFalse(result.vantagensRaciais.any { it.equals("MOVIMENTAÇÃO +2", ignoreCase = true) })
        assertFalse(result.desvantagensRaciais.any { it.equals("GRANDE", ignoreCase = true) })
    }


    @Test
    fun `mineradores geneticos zero g substitui dependencia atmosferica por habitante gravidade baixa zero`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "MINERADORES GENÉTICOS",
                descendenteElementalSelecionado = null,
                scifiVariant = "Zero G",
                ancestryOptions = listOf("Padrão", "Zero G"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("FORTE"),
                ancestryAutomaticDisadvantages = listOf("DEPENDÊNCIA ATMOSFÉRICA (Maior)")
            )
        )

        assertFalse(result.desvantagensRaciais.any { it.equals("DEPENDÊNCIA ATMOSFÉRICA (Maior)", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.equals("HABITANTE DE GRAVIDADE ZERO/BAIXA (Maior)", ignoreCase = true) })
    }


    @Test
    fun `mineradores geneticos zero g adiciona adaptacao gravitacional e remove forte`() {
        val adaptacaoGravitacional = Vantagem(
            id = "adaptacao_gravitacional",
            nome = "Adaptação Gravitacional",
            categoria = Categoria.ANTECEDENTE,
            origem = "SCI_FI",
            requisitos = Requisito()
        )
        val forte = Vantagem(
            id = "forte",
            nome = "Forte",
            categoria = Categoria.ANTECEDENTE,
            origem = "SCI_FI",
            requisitos = Requisito()
        )

        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "MINERADORES GENÉTICOS",
                descendenteElementalSelecionado = null,
                scifiVariant = "Zero G",
                ancestryOptions = listOf("Padrão", "Zero G"),
                isSciFiActive = true,
                allAdvantages = listOf(adaptacaoGravitacional, forte),
                selectedAdvantages = listOf(forte),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = listOf("FORTE"),
                ancestryAutomaticDisadvantages = listOf("DEPENDÊNCIA ATMOSFÉRICA (Maior)")
            )
        )

        assertTrue(result.selectedAdvantages.any { it.id == "adaptacao_gravitacional" })
        assertFalse(result.vantagensRaciais.any { it.equals("FORTE", ignoreCase = true) })
    }


    @Test
    fun `possessores energia adiciona anotacao de compensacao de quatro pontos`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "POSSESSORES",
                descendenteElementalSelecionado = null,
                scifiVariant = "Energia",
                ancestryOptions = listOf("Padrão", "Energia"),
                isSciFiActive = true,
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = emptyList()
            )
        )
        // Nota pro mestre, não uma desvantagem de traço real — mora em
        // anotacoesToAdd (ver AncestryVariantRegistry.possessores), não em
        // desvantagensRaciais.
        assertTrue(result.anotacoesToAdd.any { it.contains("4 pontos", ignoreCase = true) })
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

    @Test
    fun `umvee gatoruja adiciona visao no escuro sem mexer em forasteiro`() {
        val result = useCase.execute(
            ResolveAncestryRacialPackageUseCase.Params(
                anc = "Umvee (Filhos da Lua)",
                descendenteElementalSelecionado = null,
                scifiVariant = "Gatoruja",
                ancestryOptions = listOf("Ápice", "Vínculo Bestial", "Pele Iluminada pela Lua", "Gatoruja", "Correnteza", "Pedregoso"),
                allAdvantages = emptyList(),
                selectedAdvantages = emptyList(),
                previousFreeAdvantageKeys = emptySet(),
                ancestryGrantedAdvantages = emptyList(),
                ancestryAutomaticDisadvantages = listOf("Forasteiro (Menor)"),
                ancestryOrigin = "ARTE_DA_GUERRA"
            )
        )

        assertTrue(result.vantagensRaciais.any { it.equals("VISÃO NO ESCURO", ignoreCase = true) })
        assertTrue(result.desvantagensRaciais.any { it.equals("Forasteiro (Menor)", ignoreCase = true) })
    }
}
