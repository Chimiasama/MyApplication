package com.example.swadebuilder.phase0

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaEquipamentos
import com.example.swadebuilder.listaMonstroTemplates
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaPoderes
import com.example.swadebuilder.listaSuperPoderes
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDescricao
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.mapaPericias
import com.example.swadebuilder.racialAttrMinMap
import com.example.swadebuilder.racialSkillStartMap
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Phase0CriticalFlowsTest {

    private lateinit var atletismo: Pericia
    private lateinit var atirar: Pericia

    @Before
    fun setupFixtureData() {
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
        mapaAtributosDisplay = listaAtributos.associateWith { it }
        mapaAtributosDescricao = listaAtributos.associateWith { "" }

        atletismo = Pericia(nome = "ATLETISMO", atributo = "AGILIDADE", basica = true, origem = "BASICO")
        atirar = Pericia(nome = "ATIRAR", atributo = "AGILIDADE", basica = false, origem = "BASICO")

        listaPericias = listOf(atletismo, atirar)
        mapaPericias = listaPericias.associateBy { it.nome }

        listaAncestralidadesJson = listOf(
            RacialModifier(nome = "HUMANOS", atributos = emptyMap(), pericias = emptyMap()),
            RacialModifier(nome = "ELFOS", atributos = emptyMap(), pericias = mapOf("ATLETISMO" to 6))
        )

        racialSkillStartMap = mapOf(
            "ELFOS" to mapOf("ATLETISMO" to 6)
        )
        racialAttrMinMap = emptyMap()

        listaVantagens = emptyList()
        listaComplicacoes = emptyList()
        listaPoderes = emptyList()
        listaEquipamentos = emptyList()
        listaMonstroTemplates = emptyList()
        listaTropos = emptyList()
        listaSuperPoderes = emptyList()
    }

    @Test
    fun snapshotRoundTrip_preservesCriticalCreationStacks() {
        val state = CriadorState()
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

        val complicacao = Complicacao(
            id = "desagradavel",
            name = "Desagradável",
            severity = "Menor",
            description = "",
            origem = "BASICO"
        )
        state.complicacoesSelecionadas[complicacao] = "Menor"

        val vantagem = Vantagem(
            id = "alerta",
            nome = "Alerta",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Novato")
        )
        state.vantagensSelecionadas.add(vantagem)

        val snapshot = state.toSnapshot()

        val restored = CriadorState()
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
        val state = CriadorState()
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
        val state = CriadorState()

        assertEquals(4, state.periciaStartRaw("HUMANOS", atletismo))

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("ELFOS", feedback)

        assertEquals("ELFOS", state.ancestralidade)
        assertEquals(6, state.periciaStartRaw("ELFOS", atletismo))
        assertEquals(6, state.rawTotal(atletismo))
    }
    @Test
    fun periciasFiltradasPorCompendio_consideraLivrosAtivosAlemDoBasico() {
        val state = CriadorState()

        val pilotarBasico = Pericia(nome = "PILOTAR", atributo = "AGILIDADE", basica = false, origem = "BASICO")
        val pilotarSciFi = Pericia(nome = "PILOTAR", atributo = "AGILIDADE", basica = false, origem = "SCI_FI")
        val ocultismoFantasia = Pericia(nome = "OCULTISMO", atributo = "ASTUCIA", basica = false, origem = "FANTASIA")

        listaPericias = listOf(atletismo, pilotarBasico, pilotarSciFi, ocultismoFantasia)
        mapaPericias = listaPericias.associateBy { it.nome }

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
