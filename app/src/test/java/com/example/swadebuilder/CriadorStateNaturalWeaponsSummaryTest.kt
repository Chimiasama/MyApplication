package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateNaturalWeaponsSummaryTest {

    @Test
    fun `garras de demonio aparecem como garras e escondem desarmado`() {
        val state = CriadorState().apply {
            ancestralidade = "DEMONIO"
        }

        state.adicionarVantagem(
            Vantagem(
                id = "garras_demonio",
                nome = "GARRAS (Demônio)",
                categoria = Categoria.MONSTRUOSAS,
                requisitos = Requisito(estagio = "Novato")
            )
        )

        val naturais = state.extrairArmasNaturais()
        val garras = naturais.firstOrNull { it.nome.contains("Garras", ignoreCase = true) }

        assertTrue("Esperava arma natural Garras no resumo", garras != null)
        assertEquals("For+d4", (garras?.dano as? JsonPrimitive)?.content)
        assertFalse("Não deve exibir Desarmado/Ataque Natural quando já há arma natural específica", naturais.any { it.nome == "Ataque Natural" })
    }


    @Test
    fun `garras de demonio em dobro evoluem para for d6 e pa 2`() {
        val state = CriadorState().apply { ancestralidade = "DEMONIO" }

        repeat(2) {
            state.adicionarVantagem(
                Vantagem(
                    id = "garras_demonio",
                    nome = "GARRAS (Demônio)",
                    categoria = Categoria.MONSTRUOSAS,
                    requisitos = Requisito(estagio = "Novato")
                )
            )
        }

        val garras = state.extrairArmasNaturais().firstOrNull { it.nome.contains("Garras", ignoreCase = true) }
        assertEquals("For+d6", (garras?.dano as? JsonPrimitive)?.content)
        assertEquals("2", (garras?.pa as? JsonPrimitive)?.content)
    }

    @Test
    fun `mordida de demonio em dobro recebe pa 2 mantendo for d6`() {
        val state = CriadorState().apply { ancestralidade = "DEMONIO" }

        repeat(2) {
            state.adicionarVantagem(
                Vantagem(
                    id = "mordida_demonio",
                    nome = "MORDIDA (Demônio)",
                    categoria = Categoria.MONSTRUOSAS,
                    requisitos = Requisito(estagio = "Novato")
                )
            )
        }

        val mordida = state.extrairArmasNaturais().firstOrNull { it.nome.contains("Mordida", ignoreCase = true) }
        assertEquals("For+d6", (mordida?.dano as? JsonPrimitive)?.content)
        assertEquals("2", (mordida?.pa as? JsonPrimitive)?.content)
    }

}
