package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O app tinha vários bônus de atributo/perícia iniciais decididos comparando o
 * NOME da raça em código Kotlin, mesmo quando o traço já existia (ou já deveria
 * existir) em `habilidades[]` da raça. Isso gerava divergência entre o que a
 * ficha mostra como traço e o número realmente calculado (ex.: Feral escolhendo
 * "Gatoruja" ganhava o texto "Perceber d6" mas não o bônus, porque o código só
 * reconhecia esse traço para a raça "Umvee"). Estes testes fixam o comportamento
 * novo: o bônus deve seguir a presença do traço (por `id`), não o nome da raça.
 */
class CriadorStateRacialTraitDrivenAttributesTest {

    private fun snapshotWith(racas: List<RacialModifier>): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = listOf(
            Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true),
            Pericia(nome = "Ocultismo", atributo = "ASTUCIA", basica = false),
            Pericia(nome = "Furtividade", atributo = "AGILIDADE", basica = true)
        ),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList<Vantagem>(),
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    private fun racaComTraco(nome: String, traitId: String, origem: String = "BASICO"): RacialModifier =
        RacialModifier(
            nome = nome,
            habilidades = listOf(
                RacialAbility(nome = traitId, descricao = "traço de teste", id = traitId)
            ),
            origem = origem
        )

    @Test
    fun `forte concede Forca d6 pelo traco, nao pelo nome da raca`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "FORTE"))))
        state.ancestralidade = "RACA_QUALQUER"

        assertEquals(6, state.atributoMinRaw("Força"))
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `espirituoso concede Espirito d6 pelo traco`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "ESPIRITUOSO"))))
        state.ancestralidade = "RACA_QUALQUER"

        assertEquals(6, state.atributoMinRaw("Espírito"))
    }

    @Test
    fun `astucia concede Astucia d6 pelo efeito estruturado do catalogo, sem if dedicado`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "ASTUCIA"))))
        state.ancestralidade = "RACA_QUALQUER"

        assertEquals(6, state.atributoMinRaw("Astúcia"))
    }

    @Test
    fun `muito forte concede Forca d8 (dois passos) pelo efeito estruturado do catalogo`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "MUITO_FORTE"))))
        state.ancestralidade = "RACA_QUALQUER"

        assertEquals(8, state.atributoMinRaw("Força"))
    }

    @Test
    fun `endurecido aplica escolha entre Forca e Vigor conforme humanoMineradorAtributo`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("MEIO-ORCS", "ENDURECIDO"))))
        state.ancestralidade = "MEIO-ORCS"

        // Sem escolha explícita, o default preserva o comportamento de antes desta
        // raça migrar pro mesmo mecanismo de escolha de atributo de Feral/Minerador.
        state.humanoMineradorAtributo = null
        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))

        state.selecionarHumanoMineradorAtributo("Força")
        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertEquals(6, state.atributoMinRaw("Força"))
    }

    @Test
    fun `mente primitiva trava teto de Astucia em d6 na criacao pelo traco`() {
        val state = CriadorState()
        state.updateGameData(
            snapshotWith(
                listOf(
                    RacialModifier(
                        nome = "FERAL",
                        habilidades = listOf(
                            RacialAbility(nome = "Mente Primitiva", descricao = "teste", id = "MENTE_PRIMITIVA")
                        ),
                        origem = "ARTE_DA_GUERRA"
                    )
                )
            )
        )
        state.ancestralidade = "FERAL"

        assertTrue(state.atributoMaxRawNaCriacao("Astúcia") <= 6)
    }

    @Test
    fun `sem mente primitiva o teto de Astucia nao fica travado em d6`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("OUTRA_RACA", "QUALQUER_OUTRO_ID"))))
        state.ancestralidade = "OUTRA_RACA"

        assertTrue(state.atributoMaxRawNaCriacao("Astúcia") > 6)
    }

    @Test
    fun `heranca concede Adaptavel quando meioElfoAgil esta desligado`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("MEIO-ELFOS", "HERANCA"))))
        state.ancestralidade = "MEIO-ELFOS"
        state.meioElfoAgil = false

        assertTrue(state.temAdaptavel())
    }

    // Nenhum teste existente cobria o ramo meioElfoAgil=true de verdade (só
    // o Adaptável acima) — fechado agora que a troca Herança/Ágil passou a
    // ler de AncestryVariantRegistry.meioElfoHeranca() em vez de ser
    // construída na mão em applyAncestryVariantAdjustments.
    @Test
    fun `heranca concede Agilidade d6 quando meioElfoAgil esta ligado`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("MEIO-ELFOS", "HERANCA"))))
        state.ancestralidade = "MEIO-ELFOS"
        state.meioElfoAgil = true

        assertEquals(6, state.atributoMinRaw("Agilidade"))
        assertFalse(state.temAdaptavel())
    }

    @Test
    fun `meio-elfo pathfinder com flexibilidade (sem heranca) nao deve ser tratado como adaptavel`() {
        // Antes desse fix, ancestralidade.contains("MEIO-ELFO") por si só já
        // retornava true aqui, mesmo para a variante Pathfinder que na verdade
        // tem "Flexibilidade" (troca de atributo), não "Herança"/Adaptável.
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("MEIO-ELFO", "FLEXIBILIDADE"))))
        state.ancestralidade = "MEIO-ELFO"
        state.meioElfoAgil = false

        assertFalse(state.temAdaptavel())
    }

    @Test
    fun `obsessivos concede d4 na pericia escolhida pelo traco`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("GNOMO_TESTE", "OBSESSIVOS"))))
        state.ancestralidade = "GNOMO_TESTE"
        state.gnomoPericiaEscolhida = "Furtividade"

        val furtividade = Pericia(nome = "Furtividade", atributo = "AGILIDADE", basica = true)
        assertEquals(4, state.periciaStartRaw("GNOMO_TESTE", furtividade))
    }

    @Test
    fun `perceber d6 (Gatoruja) funciona para qualquer raca com o traco, corrigindo o bug do Feral`() {
        // Bug encontrado na auditoria: o código só reconhecia esse bônus quando o
        // NOME da raça continha "UMVEE" — Feral, que compartilha o mesmo Dom da
        // Natureza "Gatoruja", nunca recebia o bônus mesmo com o traço certo na
        // ficha. Ler pelo `id` do traço corrige os dois casos com o mesmo código.
        // (Ocultismo d4 não faz parte de Gatoruja — é NATURALMENTE_SOBRENATURAL,
        // traço base de todo Umvee, sempre concedido independente do dom.)
        val state = CriadorState()
        state.updateGameData(
            snapshotWith(
                listOf(
                    RacialModifier(
                        nome = "FERAL",
                        habilidades = listOf(
                            RacialAbility(nome = "Perceber d6", descricao = "teste", id = "PERCEBER_D6")
                        ),
                        origem = "ARTE_DA_GUERRA"
                    )
                )
            )
        )
        state.ancestralidade = "FERAL"
        state.compendioArteDaGuerraAtivo = true

        val perceber = Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)

        assertEquals(6, state.periciaStartRaw("FERAL", perceber))
    }

    // Bug real achado na auditoria de migração de Atributo/Perícia Aumentada:
    // faltava `passos = 0` no efeito de BRINCALHAO (RacialTraitPointCatalog.EFEITOS)
    // — sem isso o loop genérico (4 + passos*2, default passos=1) calculava
    // Provocar d6, não d4, apesar do texto do livro embutido em
    // ancestralidades.json ("O Araiguma recebe Provocar d4 (1)") e do custo já
    // cadastrado (1pt = tier pericia_racial_d4, nunca bateu com d6/2pt).
    @Test
    fun `brincalhao (Araiguma) concede Provocar d4, nao d6`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("ARAIGUMA (GUAXINIM)", "BRINCALHAO"))))
        state.ancestralidade = "ARAIGUMA (GUAXINIM)"
        state.compendioArteDaGuerraAtivo = true

        val provocar = Pericia(nome = "Provocar", atributo = "ESPIRITO", basica = false)

        assertEquals(4, state.periciaStartRaw("ARAIGUMA (GUAXINIM)", provocar))
    }

    // Sistema de Tropo genérico (rodada 43 do audit doc): o receio original do usuário era
    // raça dar d6 numa perícia/atributo, Tropo bonificar a MESMA perícia/atributo, e o
    // recálculo errar (fica em d10 fantasma, ou trava em d6/zera). Os 4 testes abaixo provam
    // que o loop genérico de tropoSelecionado?.habilidades em atributoBaseRacial()/
    // periciaStartRawInternal() resolve isso corretamente pros dois formatos de bônus
    // (relativo, que soma ACIMA do que a raça já deu, e fixo, que só serve de piso).
    @Test
    fun `tropo com AtributoStep relativo soma um passo ACIMA do que a raca ja deu, nao trava nem zera`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "FORTE")))) // raça: Força d6
        state.ancestralidade = "RACA_QUALQUER"
        state.tropoSelecionado = Tropo(
            id = "tropo_teste", nome = "Tropo Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA",
            descricao = "",
            habilidades = listOf(RacialAbility(nome = "Bônus Teste", descricao = "", traitId = "ATTRIBUTE_STEP_UP", targetRef = "Força", value = 1))
        )

        assertEquals(8, state.atributoMinRaw("Força")) // d6 (raça) + 1 passo (tropo relativo) = d8
    }

    @Test
    fun `tropo com AtributoStep relativo sem bonus de raca vira d6, nao trava em d4 nem falha`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "QUALQUER_OUTRO_ID")))) // raça sem bônus em Força
        state.ancestralidade = "RACA_QUALQUER"
        state.tropoSelecionado = Tropo(
            id = "tropo_teste", nome = "Tropo Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA",
            descricao = "",
            habilidades = listOf(RacialAbility(nome = "Bônus Teste", descricao = "", traitId = "ATTRIBUTE_STEP_UP", targetRef = "Força", value = 1))
        )

        assertEquals(6, state.atributoMinRaw("Força")) // d4 (base) + 1 passo = d6
    }

    @Test
    fun `tropo com PericiaStep relativo soma um passo ACIMA do que a raca ja deu`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "SENTIDOS_AGUCADOS")))) // raça: Perceber d6
        state.ancestralidade = "RACA_QUALQUER"
        state.tropoSelecionado = Tropo(
            id = "tropo_teste", nome = "Tropo Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA",
            descricao = "",
            habilidades = listOf(RacialAbility(nome = "Bônus Teste", descricao = "", traitId = "SKILL_STEP_UP", targetRef = "Perceber", value = 1))
        )
        val perceber = Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)

        assertEquals(8, state.periciaStartRaw("RACA_QUALQUER", perceber)) // d6 (raça) + 1 passo (tropo) = d8
    }

    @Test
    fun `tropo com PericiaStep fixo (nao relativo) so serve de piso, nao esconde bonus maior da raca`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "SENTIDOS_AGUCADOS")))) // raça: Perceber d6
        state.ancestralidade = "RACA_QUALQUER"
        state.tropoSelecionado = Tropo(
            id = "tropo_teste", nome = "Tropo Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA",
            descricao = "",
            // piso fixo d4 (value=0 passos) — não deveria REDUZIR o d6 que a raça já deu
            habilidades = listOf(RacialAbility(nome = "Bônus Teste", descricao = "", traitId = "SKILL_BOOST", targetRef = "Perceber", value = 0))
        )
        val perceber = Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)

        assertEquals(6, state.periciaStartRaw("RACA_QUALQUER", perceber))
    }

    @Test
    fun `sem tropo selecionado, atributoMinRaw ignora habilidades de tropo (nao ha tropo)`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("RACA_QUALQUER", "FORTE"))))
        state.ancestralidade = "RACA_QUALQUER"
        state.tropoSelecionado = null

        assertEquals(6, state.atributoMinRaw("Força"))
    }
}
