package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierEngineAdgAncestryTest {

    @Test
    fun `generic aparar minus from racial disadvantage applies parry penalty`() {
        val state = CriadorState().apply {
            ancestralidade = "Tanukimimi (Tanuki)"
            listaAncestralidadesJson = listOf(
                RacialModifier(
                    nome = "Tanukimimi (Tanuki)",
                    origem = "ARTE_DA_GUERRA",
                    atributos = emptyMap(),
                    pericias = emptyMap(),
                    vantagensGratis = emptyList(),
                    desvantagens = listOf("APARAR -1"),
                    habilidades = emptyList()
                )
            )
        }

        val modifiers = ModifierEngine.collect(state)

        assertTrue(modifiers.any { it.id == "racial_parry_generic" && it.value == -1 })
    }
}
