package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ModuleSnapshotCacheTest {

    @Test
    fun `evict oldest entry when capacity exceeded`() {
        val cache = ModuleSnapshotCache(maxSize = 2)

        cache.put("A", fixtureSnapshot())
        cache.put("B", fixtureSnapshot())
        cache.put("C", fixtureSnapshot())

        assertNull(cache.get("A"))
        assertEquals(1, cache.get("B")?.listaPericias?.size)
        assertEquals(1, cache.get("C")?.listaPericias?.size)
    }

    @Test
    fun `access updates recency for lru policy`() {
        val cache = ModuleSnapshotCache(maxSize = 2)

        cache.put("A", fixtureSnapshot())
        cache.put("B", fixtureSnapshot())
        cache.get("A")
        cache.put("C", fixtureSnapshot())

        assertEquals(1, cache.get("A")?.listaPericias?.size)
        assertNull(cache.get("B"))
        assertEquals(1, cache.get("C")?.listaPericias?.size)
    }

    private fun fixtureSnapshot() = GameDataSnapshot(
        listaComplicacoes = emptyList(),
        listaCoracoesCrystal = emptyList(),
        listaAncestralidadesJson = emptyList(),
        listaMonstroTemplates = emptyList(),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
        listaAtributos = emptyList(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = listOf(Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true)),
        mapaPericias = mapOf("ATIRAR" to Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true)),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList(),
        listaPoderes = emptyList(),
        listaTropos = emptyList(),
        listaEquipamentos = emptyList(),
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList(),
        arcanoInfo = emptyList()
    )
}
