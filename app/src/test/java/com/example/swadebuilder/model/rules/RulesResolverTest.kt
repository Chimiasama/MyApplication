package com.example.swadebuilder.model.rules

import org.junit.Assert.assertEquals
import org.junit.Test

class RulesResolverTest {

    private val resolver = RulesResolver()

    @Test
    fun `prioriza pathfinder sobre outros modulos`() {
        val rules = resolver.resolve(
            compendioPathfinderAtivo = true,
            compendioSciFiAtivo = true,
            compendioDeadlandsAtivo = true,
            compendioFantasiaAtivo = true
        )

        assertEquals(30000, rules.startingResources().dinheiro)
    }

    @Test
    fun `resolve valores de dinheiro por modulo`() {
        assertEquals(1000, resolver.resolve(false, true, false, false).startingResources().dinheiro)
        assertEquals(250, resolver.resolve(false, false, true, false).startingResources().dinheiro)
        assertEquals(300, resolver.resolve(false, false, false, true).startingResources().dinheiro)
        assertEquals(500, resolver.resolve(false, false, false, false).startingResources().dinheiro)
    }
}
