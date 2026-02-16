package com.example.swadebuilder.phase0

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.GameDataSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Phase0CriticalFlowsTest {

    private lateinit var atletismo: Pericia
    private lateinit var atirar: Pericia
    private lateinit var testSnapshot: GameDataSnapshot

    @Before
    fun setupFixtureData() {
        val listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        val mapaAtributosDisplay = listaAtributos.associateWith { it }
        val mapaAtributosDescricao = listaAtributos.associateWith { "" }

        atletismo = Pericia(nome = "ATLETISMO", atributo = "AGILIDADE", basica = true, origem = "BASICO")
        atirar = Pericia(nome = "ATIRAR", atributo = "AGILIDADE", basica = false, origem = "BASICO")

        val listaPericias = listOf(atletismo, atirar)
        val mapaPericias = listaPericias.associateBy { it.nome }

        val listaAncestralidadesJson = listOf(
            RacialModifier(nome = "HUMANOS", atributos = emptyMap(), pericias = emptyMap()),
            RacialModifier(nome = "ELFOS", atributos = emptyMap(), pericias = mapOf("ATLETISMO" to 6))
        )

        val racialSkillStartMap = mapOf(
            "ELFOS" to mapOf("ATLETISMO" to 6)
        )
        val racialAttrMinMap = emptyMap<String, Map<String, Int>>()

        testSnapshot = GameDataSnapshot(
            listaComplicacoes = emptyList(),
            listaCoracoesCrystal = emptyList(),
            listaAncestralidadesJson = listaAncestralidadesJson,
            listaMonstroTemplates = emptyList(),
            racialAttrMinMap = racialAttrMinMap,
            racialSkillStartMap = racialSkillStartMap,
            listaAtributos = listaAtributos,
            mapaAtributosDisplay = mapaAtributosDisplay,
            listaPericias = listaPericias,
            mapaPericias = mapaPericias,
            mapaAtributosDescricao = mapaAtributosDescricao,
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

    private fun createStateWithSnapshot(snapshot: GameDataSnapshot = testSnapshot): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshot)
        return state
    }

    @Test
    fun snapshotRoundTrip_preservesCriticalCreationStacks() {
        // Need to update snapshot locally for this test case
        val complicacao = Complicacao(
            id = "desagradavel",
            name = "Desagradável",
            severity = "Menor",
            description = "",
            origem = "BASICO"
        )

        val vantagem = Vantagem(
            id = "alerta",
            nome = "Alerta",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Novato")
        )

        val localSnapshot = testSnapshot.copy(
            listaComplicacoes = listOf(complicacao),
            listaVantagens = listOf(vantagem)
        )

        val state = createStateWithSnapshot(localSnapshot)

        state.ensureAllAtributosRegistered()
        state.ensureAllPericiasRegistered()

        state.ancestralidade = "ELFOS"
        state.cpPaStack.add("PB")
        state.cpSpStack.add(Unit)
        state.cpPvStack.add(Unit)

        state.paCostStackPorAtributo.getValue("AGILIDADE").addAll(listOf(1, 2))
        state.valoresAtributos.getValue("AGILIDADE").intValue = 8

        state.baseIncsPorPericia[atletismo] = 1
        state.baseIncsPorPericia[atirar] = 2
        state.spCostStackPorPericia.getValue(atletismo).add(1)
        state.spCostStackPorPericia.getValue(atirar).addAll(listOf(1, 2))

        state.complicacoesSelecionadas[complicacao] = "Menor"
        state.vantagensSelecionadas.add(vantagem)

        val snapshot = state.toSnapshot()

        val restored = CriadorState()
        restored.updateGameData(localSnapshot)
        restored.restoreFromSnapshot(snapshot, mutableListOf())
        val restoredSnapshot = restored.toSnapshot()

        assertEquals(snapshot.recursos.cpPaStack, restoredSnapshot.recursos.cpPaStack)
        assertEquals(snapshot.recursos.cpSpStack, restoredSnapshot.recursos.cpSpStack)
        assertEquals(snapshot.atributos.paCostStackPorAtributo, restoredSnapshot.atributos.paCostStackPorAtributo)
        assertEquals(snapshot.pericias.baseIncsPorPericia, restoredSnapshot.pericias.baseIncsPorPericia)
        assertEquals(state.rawTotal(atletismo), restored.rawTotal(atletismo))
        assertEquals(state.rawTotal(atirar), restored.rawTotal(atirar))
        assertEquals(snapshot.selecoes.complicacoesSelecionadas, restoredSnapshot.selecoes.complicacoesSelecionadas)
        assertEquals(snapshot.selecoes.vantagens.map { it.id }, restoredSnapshot.selecoes.vantagens.map { it.id })
    }

    @Test
    fun rebuildAllPericiaStacks_keepsSkillPoolNonNegative() {
        val state = createStateWithSnapshot()
        state.ensureAllPericiasRegistered()

        state.baseIncsPorPericia[atirar] = 8

        val feedback = mutableListOf<String>()
        state.rebuildAllPericiaStacks(feedbackMessages = feedback, enforcePoolLimit = true)

        assertTrue(state.pontosPericia >= 0)
        assertTrue(
            state.spCostStackPorPericia.getValue(atirar).isNotEmpty() ||
                    state.rawTotal(atirar) == 0
        )
        assertTrue(feedback.isEmpty() || feedback.any { it.contains("Perícia", ignoreCase = true) })
    }

    @Test
    fun aplicarAncestralidade_appliesRacialSkillStart() {
        val state = createStateWithSnapshot()

        assertEquals(4, state.periciaStartRaw("HUMANOS", atletismo))

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("ELFOS", feedback)

        assertEquals("ELFOS", state.ancestralidade)
        assertEquals(6, state.periciaStartRaw("ELFOS", atletismo))
        assertEquals(6, state.rawTotal(atletismo))
    }

    @Test
    fun periciasFiltradasPorCompendio_consideraLivrosAtivosAlemDoBasico() {
        val pilotarBasico = Pericia(nome = "PILOTAR", atributo = "AGILIDADE", basica = false, origem = "BASICO")
        val pilotarSciFi = Pericia(nome = "PILOTAR", atributo = "AGILIDADE", basica = false, origem = "SCI_FI")
        val ocultismoFantasia = Pericia(nome = "OCULTISMO", atributo = "ASTUCIA", basica = false, origem = "FANTASIA")

        val localListaPericias = listOf(atletismo, pilotarBasico, pilotarSciFi, ocultismoFantasia)
        val localSnapshot = testSnapshot.copy(
            listaPericias = localListaPericias,
            mapaPericias = localListaPericias.associateBy { it.nome }
        )

        val state = createStateWithSnapshot(localSnapshot)

        state.compendioFantasiaAtivo = false
        state.compendioSciFiAtivo = false
        var filtradas = state.periciasFiltradasPorCompendio.map { it.nome to (it.origem ?: "") }
        assertTrue(filtradas.any { it.first == "PILOTAR" && it.second == "BASICO" })
        assertFalse(filtradas.any { it.first == "OCULTISMO" })

        state.compendioSciFiAtivo = true
        filtradas = state.periciasFiltradasPorCompendio.map { it.nome to (it.origem ?: "") }
        assertTrue(filtradas.any { it.first == "PILOTAR" && it.second == "SCI_FI" })

        state.compendioFantasiaAtivo = true
        filtradas = state.periciasFiltradasPorCompendio.map { it.nome to (it.origem ?: "") }
        assertTrue(filtradas.any { it.first == "OCULTISMO" && it.second == "FANTASIA" })
    }

}
