package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OriginKeysTest {

    @Test
    fun `normalizes cidade do sol a vapor aliases to canonical key`() {
        val aliases = listOf(
            "SOL_VAPOR",
            "sol e vapor",
            "Cidade do Sol a Vapor",
            "CIDADE_DO_SOL_A_VAPOR"
        )

        aliases.forEach { raw ->
            assertEquals("CIDADE_SOL_VAPOR", canonicalOriginKey(raw))
        }
    }
}
