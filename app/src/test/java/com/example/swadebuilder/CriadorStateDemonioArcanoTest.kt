package com.example.swadebuilder

import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateDemonioArcanoTest {

    @Test
    fun `aa demonio marca disfarce demoniaco como poder fixo`() {
        val state = CriadorState()

        assertTrue(state.isFixedPower("Demônio", "disfarce_demoniaco"))
    }
}
