package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Test

class NormalizeModuleKeysTest {

    @Test
    fun `normalizeModuleKeys remove blanks uppercase and dedup`() {
        val input = setOf(" fantasia ", "FANTASIA", "", "   ", "super", " SUPER ")

        val normalized = normalizeModuleKeys(input)

        assertEquals(setOf("FANTASIA", "SUPER"), normalized)
    }
}
