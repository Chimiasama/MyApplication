package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OriginKeysTest {

    @Test
    fun `cidade do sol e vapor aponta para asset de sol vapor`() {
        assertEquals("SOL_VAPOR", powerAssetOriginKey("CIDADE_SOL_VAPOR"))
        assertEquals("SOL_VAPOR", powerAssetOriginKey("CIDADE DO SOL A VAPOR"))
    }

    @Test
    fun `origens legadas de sol vapor continuam normalizadas`() {
        assertEquals("SOL_VAPOR", powerAssetOriginKey("SOL_VAPOR"))
        assertEquals("SOL_VAPOR", powerAssetOriginKey("SOL E VAPOR"))
    }
}
