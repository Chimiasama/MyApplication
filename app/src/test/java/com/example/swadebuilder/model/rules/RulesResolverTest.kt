package com.example.swadebuilder.model.rules

import com.example.swadebuilder.model.ids.AdvantageIds
import com.example.swadebuilder.model.ids.CrystalHeartIds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesResolverTest {

    private val resolver = RulesResolver()

    @Test
    fun `prioriza pathfinder sobre outros modulos`() {
        val rules = resolver.resolve(
            compendioPathfinderAtivo = true,
            compendioSciFiAtivo = true,
            compendioDeadlandsAtivo = true,
            compendioFantasiaAtivo = true,
            compendioCrystalHeartAtivo = true,
            compendioHorrorAtivo = true,
            compendioArteDaGuerraAtivo = true,
            compendioCidadeSolVaporAtivo = true,
            compendioWiseguysAtivo = true
        )

        assertEquals(30000, rules.startingResources().dinheiro)
        assertEquals("Humano (Pathfinder)", rules.defaultAncestralidade())
    }

    @Test
    fun `resolve valores de dinheiro por modulo`() {
        assertEquals(1000, resolver.resolve(false, true, false, false, false, false, false, false, false).startingResources().dinheiro)
        assertEquals(250, resolver.resolve(false, false, true, false, false, false, false, false, false).startingResources().dinheiro)
        assertEquals(300, resolver.resolve(false, false, false, true, false, false, false, false, false).startingResources().dinheiro)
        assertEquals(500, resolver.resolve(false, false, false, false, false, false, false, false, false).startingResources().dinheiro)
    }

    @Test
    fun `crystal heart define defaults de cenário`() {
        val rules = resolver.resolve(
            compendioPathfinderAtivo = false,
            compendioSciFiAtivo = false,
            compendioDeadlandsAtivo = false,
            compendioFantasiaAtivo = false,
            compendioCrystalHeartAtivo = true,
            compendioHorrorAtivo = false,
            compendioArteDaGuerraAtivo = false,
            compendioCidadeSolVaporAtivo = false,
            compendioWiseguysAtivo = false
        )

        assertEquals("As Ilhas", rules.defaultAncestralidade())
        assertTrue(rules.mandatoryAdvantageIds().contains(AdvantageIds.AA_AGENTE_SYN))
        assertEquals(CrystalHeartIds.HEART_STARTER, rules.defaultCrystalHeartId())
    }

    @Test
    fun `wiseguys e arte da guerra aplicam políticas de vantagem`() {
        val wiseguys = resolver.resolve(false, false, false, false, false, false, false, false, true)
        assertTrue(wiseguys.blocksArcaneBackgrounds())
        assertTrue(wiseguys.hidePowerCategoryAdvantagesExceptMysticPowers())
        assertTrue(wiseguys.forbiddenAdvantageIds().contains("aristocrata"))

        val adg = resolver.resolve(false, false, false, false, false, false, true, false, false)
        assertTrue(adg.blocksArcaneBackgrounds())
        assertTrue(adg.forbiddenAdvantageIds().contains("resistencia_arcana"))
    }
}
