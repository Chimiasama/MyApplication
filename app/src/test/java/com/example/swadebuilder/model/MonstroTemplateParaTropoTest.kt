package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `MonstroTemplate.paraTropo()` — conversão do Monstro Heroico (Horror) pro tipo `Tropo`
 * unificado (rodada 43 do audit doc): `atributosBonus` (mapa numérico solto) vira
 * `habilidades[]` real (ATTRIBUTE_BOOST/SKILL_BOOST), `habilidades[]` passa direto, e cada
 * string de `complicacoes` vira uma `RacialAbility` `category="racial_hindrance"` — com
 * `targetRef` só nos casos verificados contra o catálogo real.
 */
class MonstroTemplateParaTropoTest {

    @Test
    fun `atributosBonus vira ATTRIBUTE_BOOST real, Fe vira SKILL_BOOST`() {
        val monstro = MonstroTemplate(
            id = "vampiro", nome = "Vampiro", descricao = "",
            atributosBonus = mapOf("Forca" to 2, "Vigor" to 2, "Fe" to 1)
        )
        val tropo = monstro.paraTropo()

        val forca = tropo.habilidades.first { it.targetRef == "Forca" }
        assertEquals("ATTRIBUTE_BOOST", forca.traitId)
        assertEquals(2, forca.value)

        val fe = tropo.habilidades.first { it.targetRef == "Fé" }
        assertEquals("SKILL_BOOST", fe.traitId)
        assertEquals(1, fe.value)
    }

    @Test
    fun `habilidades passam direto, incluindo armasNaturais`() {
        val arma = ArmaNatural(nome = "Mordida", dano = "For+d4", pa = 0)
        val monstro = MonstroTemplate(
            id = "vampiro", nome = "Vampiro", descricao = "",
            habilidades = listOf(
                MonstroHabilidade(nome = "Mordida", descricao = "", id = "MORDIDA_VAMPIRO", armasNaturais = listOf(arma)),
                MonstroHabilidade(nome = "Fúria", descricao = "", category = "racial_edge", traitId = "GRANTED_EDGE", targetRef = "furioso")
            )
        )
        val tropo = monstro.paraTropo()

        val mordida = tropo.habilidades.first { it.id == "MORDIDA_VAMPIRO" }
        assertEquals(listOf(arma), mordida.armasNaturais)

        val furia = tropo.habilidades.first { it.targetRef == "furioso" }
        assertEquals("GRANTED_EDGE", furia.traitId)
        assertEquals("racial_edge", furia.category)
    }

    @Test
    fun `complicacao com Vantagem-Complicacao real citada no texto ganha targetRef vinculado`() {
        val monstro = MonstroTemplate(
            id = "anjo", nome = "Anjo", descricao = "",
            complicacoes = listOf("Servo do Paraíso: Anjos devem obedecer sua divindade e possuem a Complicação Voto (Maior).")
        )
        val tropo = monstro.paraTropo()

        val comp = tropo.habilidades.first { it.category == "racial_hindrance" }
        assertEquals("voto", comp.targetRef)
        assertEquals("Maior", comp.severity)
        assertEquals("Servo do Paraíso", comp.nome)
    }

    @Test
    fun `complicacao Lento da Mumia ganha traitId PACE_CHANGE, alem do targetRef, pra reduzir Movimentacao de verdade`() {
        val monstro = MonstroTemplate(
            id = "mumia", nome = "Múmia", descricao = "",
            complicacoes = listOf(
                "Fraqueza (Fogo): Sofrem +4 de dano de fogo.",
                "Lento: Movimentação reduzida em 1 e dado de corrida é d4."
            )
        )
        val tropo = monstro.paraTropo()

        val lento = tropo.habilidades.first { it.nome == "Lento" }
        assertEquals("lento", lento.targetRef)
        assertEquals("Menor", lento.severity)
        assertEquals("PACE_CHANGE", lento.traitId)
        assertEquals(-1, lento.value)
        val efeito = RacialTraitPointCatalog.efeitoDe(lento.resolvedTraitId(), lento.targetRef, lento.value)
        assertEquals(RacialTraitEffect.PassoBonus(-1), efeito)
    }

    @Test
    fun `complicacao sem Complicacao real citada fica narrativa, sem targetRef`() {
        val monstro = MonstroTemplate(
            id = "mumia", nome = "Múmia", descricao = "",
            complicacoes = listOf("Fraqueza (Fogo): Sofrem +4 de dano de fogo.")
        )
        val tropo = monstro.paraTropo()

        val comp = tropo.habilidades.first { it.category == "racial_hindrance" }
        assertNull(comp.targetRef)
        assertNull(comp.severity)
        assertEquals("Fraqueza (Fogo)", comp.nome)
    }

    @Test
    fun `categoria e origem do Tropo convertido sao MONSTRO e HORROR`() {
        val monstro = MonstroTemplate(id = "lobisomem", nome = "Lobisomem", descricao = "descrição qualquer")
        val tropo = monstro.paraTropo()

        assertEquals("MONSTRO", tropo.categoria)
        assertEquals("HORROR", tropo.origem)
        assertEquals("lobisomem", tropo.id)
        assertTrue(tropo.nome == "Lobisomem")
    }
}
