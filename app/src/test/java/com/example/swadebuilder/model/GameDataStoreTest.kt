package com.example.swadebuilder.model

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SuperPoder
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDataStoreTest {

    @Test
    fun `usa fallback quando snapshot ainda nao foi carregado`() {
        val store = GameDataStore()
        val fallbackPericias = listOf(Pericia("ATLETISMO", "AGILIDADE", basica = true, origem = "BASICO"))

        assertEquals(fallbackPericias, store.pericias(fallbackPericias))
        assertEquals(emptyList<Vantagem>(), store.vantagens(emptyList()))
    }

    @Test
    fun `apos updateSnapshot prefere dados do snapshot`() {
        val store = GameDataStore()
        val periciaSnapshot = Pericia("HACKEAR", "ASTUCIA", basica = false, origem = "SCI_FI")
        val snapshot = snapshotBase(pericias = listOf(periciaSnapshot))

        store.updateSnapshot(snapshot)

        val fallbackPericias = listOf(Pericia("ATLETISMO", "AGILIDADE", basica = true, origem = "BASICO"))
        assertEquals(listOf(periciaSnapshot), store.pericias(fallbackPericias))
        assertEquals(mapOf("HACKEAR" to periciaSnapshot), store.periciasMap(emptyMap()))
    }

    @Test
    fun `withUpdatedCoracoesCrystal atualiza lista no snapshot em memoria`() {
        val store = GameDataStore()
        val heartA = CrystalHeart(id = "heart_a", nome = "Heart A", estagio = "Novato")
        val heartB = CrystalHeart(id = "heart_b", nome = "Heart B", estagio = "Novato")
        store.updateSnapshot(snapshotBase(coracoes = listOf(heartA)))

        store.withUpdatedCoracoesCrystal(listOf(heartB))

        assertEquals(listOf(heartB), store.coracoesCrystal(emptyList()))
    }

    private fun snapshotBase(
        pericias: List<Pericia> = emptyList(),
        coracoes: List<CrystalHeart> = emptyList()
    ): GameDataSnapshot {
        return GameDataSnapshot(
            listaComplicacoes = emptyList(),
            listaCoracoesCrystal = coracoes,
            listaAncestralidadesJson = emptyList(),
            listaMonstroTemplates = emptyList(),
            racialAttrMinMap = emptyMap(),
            racialSkillStartMap = emptyMap(),
            listaAtributos = emptyList(),
            mapaAtributosDisplay = emptyMap(),
            listaPericias = pericias,
            mapaPericias = pericias.associateBy { it.nome },
            mapaAtributosDescricao = emptyMap(),
            listaVantagens = emptyList(),
            listaPoderes = emptyList(),
            listaTropos = emptyList(),
            listaEquipamentos = emptyList(),
            equipamentoCategorias = emptyList(),
            superequipCategorias = emptyList(),
            listaSuperPoderes = emptyList<SuperPoder>(),
            arcanoInfo = emptyList<ArcanoInfo>()
        )
    }
}
