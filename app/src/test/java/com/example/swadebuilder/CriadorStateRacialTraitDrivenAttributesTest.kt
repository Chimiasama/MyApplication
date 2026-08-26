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
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
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
            atributos = emptyMap(),
            pericias = emptyMap(),
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
    fun `endurecido aplica escolha entre Forca e Vigor conforme meioOrcForca`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaComTraco("MEIO-ORCS", "ENDURECIDO"))))
        state.ancestralidade = "MEIO-ORCS"

        state.meioOrcForca = false
        assertEquals(6, state.atributoMinRaw("Vigor"))
        assertEquals(4, state.atributoMinRaw("Força"))

        state.meioOrcForca = true
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
                        atributos = emptyMap(),
                        pericias = emptyMap(),
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
    fun `perceber d6 e ocultismo d4 (Gatoruja) funcionam para qualquer raca com o traco, corrigindo o bug do Feral`() {
        // Bug encontrado na auditoria: o código só reconhecia esse bônus quando o
        // NOME da raça continha "UMVEE" — Feral, que compartilha o mesmo Dom da
        // Natureza "Gatoruja", nunca recebia o bônus mesmo com o traço certo na
        // ficha. Ler pelo `id` do traço corrige os dois casos com o mesmo código.
        val state = CriadorState()
        state.updateGameData(
            snapshotWith(
                listOf(
                    RacialModifier(
                        nome = "FERAL",
                        atributos = emptyMap(),
                        pericias = emptyMap(),
                        habilidades = listOf(
                            RacialAbility(nome = "Perceber d6", descricao = "teste", id = "PERCEBER_D6"),
                            RacialAbility(nome = "Ocultismo d4", descricao = "teste", id = "OCULTISMO_D4")
                        ),
                        origem = "ARTE_DA_GUERRA"
                    )
                )
            )
        )
        state.ancestralidade = "FERAL"
        state.compendioArteDaGuerraAtivo = true

        val perceber = Pericia(nome = "Perceber", atributo = "ASTUCIA", basica = true)
        val ocultismo = Pericia(nome = "Ocultismo", atributo = "ASTUCIA", basica = false)

        assertEquals(6, state.periciaStartRaw("FERAL", perceber))
        assertEquals(4, state.periciaStartRaw("FERAL", ocultismo))
    }
}
