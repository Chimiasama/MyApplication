package com.example.swadebuilder.ui.sections

import org.junit.Assert.assertEquals
import org.junit.Test

class PoderesSectionOriginTest {

    @Test
    fun `cidade do sol e vapor usa cache de poderes sol vapor`() {
        assertEquals("SOL_VAPOR", normalizePowerOriginKey("CIDADE_SOL_VAPOR"))
        assertEquals("SOL_VAPOR", normalizePowerOriginKey("CIDADE DO SOL A VAPOR"))
    }

    @Test
    fun `sol vapor legado continua apontando para cache sol vapor`() {
        assertEquals("SOL_VAPOR", normalizePowerOriginKey("SOL_VAPOR"))
        assertEquals("SOL_VAPOR", normalizePowerOriginKey("SOL E VAPOR"))
    }
}
