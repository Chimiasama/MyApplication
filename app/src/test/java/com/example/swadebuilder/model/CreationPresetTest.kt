package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreationPresetTest {

    @Test
    fun `all presets list contains all expected setting books`() {
        val presets = CreationPreset.ALL_PRESETS
        assertTrue(presets.isNotEmpty())
        val ids = presets.map { it.id }
        assertTrue(ids.contains("basico"))
        assertTrue(ids.contains("fantasia"))
        assertTrue(ids.contains("scifi"))
        assertTrue(ids.contains("horror"))
        assertTrue(ids.contains("supers"))
        assertTrue(ids.contains("pathfinder"))
        assertTrue(ids.contains("deadlands"))
        assertTrue(ids.contains("crystal_heart"))
        assertTrue(ids.contains("arte_da_guerra"))
        assertTrue(ids.contains("cidade_sol_vapor"))
        assertTrue(ids.contains("wiseguys"))
    }

    @Test
    fun `getById retrieves correct preset and falls back to basico for unknown id`() {
        val wiseguys = CreationPreset.getById("wiseguys")
        assertEquals("wiseguys", wiseguys.id)
        assertTrue(wiseguys.defaultCompendioWiseguys)
        assertTrue(wiseguys.defaultRegraRiqueza)
        assertTrue(wiseguys.defaultRegraCosaNostra)

        val unknown = CreationPreset.getById("non_existent_preset")
        assertEquals("basico", unknown.id)
    }

    @Test
    fun `preset defaults match setting requirements`() {
        val arteDaGuerra = CreationPreset.getById("arte_da_guerra")
        assertTrue(arteDaGuerra.defaultNasceUmHeroi)
        assertFalse(arteDaGuerra.defaultMaisPontosPericias)
        assertTrue(arteDaGuerra.defaultCompendioArteDaGuerra)

        val pathfinder = CreationPreset.getById("pathfinder")
        assertFalse(pathfinder.defaultMaisPontosPericias)
        assertTrue(pathfinder.defaultCompendioPathfinder)

        val basico = CreationPreset.getById("basico")
        assertTrue(basico.defaultMaisPontosPericias)
        assertFalse(basico.defaultCompendioWiseguys)
    }
}
