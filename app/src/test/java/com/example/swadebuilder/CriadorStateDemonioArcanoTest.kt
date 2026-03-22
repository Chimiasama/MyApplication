package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateDemonioArcanoTest {

    @Test
    fun `aa demonio marca disfarce demoniaco como poder fixo`() {
        val state = CriadorState()

        assertTrue(state.isFixedPower("Demônio", "disfarce_demoniaco"))
    }

    @Test
    fun `aa demonio usa poderes disponiveis por estagio e nao slots`() {
        val state = CriadorState().apply {
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_demonio",
                    nome = "ANTECEDENTE ARCANO (Demônio)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        assertTrue(state.usaPoderesDisponiveisPorEstagio("DEMONIO"))
        assertEquals(0, state.getSlotsCountForArcano("DEMONIO"))
        assertEquals("Heroico", state.poderesDisponiveisPorEstagioParaArcano("DEMONIO")["drenar_pontos_de_poder_demonio"])
    }
}
