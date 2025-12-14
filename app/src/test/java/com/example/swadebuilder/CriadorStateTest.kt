package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
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
}
