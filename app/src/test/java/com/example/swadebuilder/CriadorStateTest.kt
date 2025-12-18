package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CriadorStateTest {
    @Before
    fun setupGlobals() {
        listaAncestralidadesJson = emptyList()
        listaMonstroTemplates = emptyList()
        racialAttrMinMap = emptyMap()
        racialSkillStartMap = emptyMap()
        listaAtributos = listOf("VIGOR")
        mapaAtributosDisplay = mapOf("VIGOR" to "Vigor")
        listaPericias = emptyList()
        mapaPericias = emptyMap()
        listaVantagens = emptyList()
    }

    @Test
    fun `nao permite vantagem sem cumprir atributo minimo`() {
        val vantagem = Vantagem(
            id = "atraente",
            nome = "ATRAENTE",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato",
                atributoMin = mapOf("Vigor" to 6)
            )
        )

        val state = CriadorState()
        state.valoresAtributos["VIGOR"]!!.intValue = 4

        assertFalse(state.podeSelecionar(vantagem))
    }

    @Test
    fun `permite vantagem apos atingir atributo minimo`() {
        val vantagem = Vantagem(
            id = "atraente",
            nome = "ATRAENTE",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato",
                atributoMin = mapOf("Vigor" to 6)
            )
        )

        val state = CriadorState()
        state.valoresAtributos["VIGOR"]!!.intValue = 8

        assertTrue(state.podeSelecionar(vantagem))
    }

    @Test
    fun `bloqueia segundo antecedente arcano quando multi desativado`() {
        val arcanoMagia = Vantagem(
            id = "antecedente_arcano_magia",
            nome = "Antecedente Arcano (Magia)",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato"
            )
        )
        val arcanoMilagres = Vantagem(
            id = "antecedente_arcano_milagres",
            nome = "Antecedente Arcano (Milagres)",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato"
            )
        )

        val state = CriadorState()
        state.permiteMultiAntecedenteArcano = false
        state.vantagensSelecionadas.add(arcanoMagia)

        assertFalse(state.podeSelecionar(arcanoMilagres))
    }

    @Test
    fun `permite multiplos antecedentes arcanos quando habilitado`() {
        val arcanoDom = Vantagem(
            id = "antecedente_arcano_dom",
            nome = "Antecedente Arcano (Dom)",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato"
            )
        )
        val arcanoPsionico = Vantagem(
            id = "antecedente_arcano_psionico",
            nome = "Antecedente Arcano (Psionicos)",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(
                estagio = "Novato"
            )
        )

        val state = CriadorState()
        state.permiteMultiAntecedenteArcano = true
        state.vantagensSelecionadas.add(arcanoDom)

        assertTrue(state.podeSelecionar(arcanoPsionico))
    }

    @Test
    fun `snapshot restaura pilhas completas de recursos`() {
        val state = CriadorState()

        state.cpPaStack.addAll(listOf("PB", "PB"))
        repeat(3) { state.cpSpStack.add(Unit) }
        repeat(2) { state.cpPvStack.add(Unit) }
        state.cpRecursosStack.add(Unit)
        state.dinheiro = 1234
        state.pontosComplicacaoGastos = 5

        val snapshot = state.toSnapshot()

        state.cpPaStack.clear()
        state.cpSpStack.clear()
        state.cpPvStack.clear()
        state.cpRecursosStack.clear()
        state.dinheiro = 0
        state.pontosComplicacaoGastos = 0

        state.restoreFromSnapshot(snapshot, mutableListOf())

        assertEquals(listOf("PB", "PB"), state.cpPaStack.toList())
        assertEquals(3, state.cpSpStack.size)
        assertEquals(2, state.cpPvStack.size)
        assertEquals(1, state.cpRecursosStack.size)
        assertEquals(1234, state.dinheiro)
        assertEquals(5, state.pontosComplicacaoGastos)
    }

    @Test
    fun `brutamontes vincula atletismo a forca e aumenta custo quando forca menor`() {
        val atletismo = Pericia("Atletismo", "AGILIDADE", basica = true)

        listaAtributos = listOf("AGILIDADE", "FORCA")
        mapaAtributosDisplay = mapOf("AGILIDADE" to "Agilidade", "FORCA" to "Força")
        listaPericias = listOf(atletismo)
        mapaPericias = mapOf("ATLETISMO" to atletismo)
        racialAttrMinMap = mapOf("HUMANOS" to mapOf("AGILIDADE" to 4, "FORCA" to 4))
        racialSkillStartMap = emptyMap()

        val state = CriadorState()
        state.valoresAtributos["AGILIDADE"]!!.intValue = 8
        state.valoresAtributos["FORCA"]!!.intValue = 4
        state.baseIncsPorPericia[atletismo] = 1

        val brutamontes = Vantagem(
            id = "brawny",
            nome = "Brutamontes",
            categoria = Categoria.ANTECEDENTE,
            requisitos = Requisito(estagio = "Novato")
        )

        state.vantagensSelecionadas.add(brutamontes)

        val snapshot = state.calcularPericiaRules(atletismo, idosoActive = false, locked = false)

        assertEquals("FORCA", snapshot.attrKey)
        assertEquals(2, snapshot.cost)
    }
}
