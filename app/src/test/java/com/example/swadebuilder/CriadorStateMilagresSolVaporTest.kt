package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateMilagresSolVaporTest {

    @Test
    fun `aa milagres de sol e vapor usa poderes por estagio e nao slots`() {
        val state = CriadorState().apply {
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_milagres",
                    nome = "ANTECEDENTE ARCANO (Milagres)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        assertTrue(state.usaPoderesDisponiveisPorEstagio("MILAGRES"))
        assertEquals(0, state.getSlotsCountForArcano("MILAGRES"))
        assertEquals("Heroico", state.poderesDisponiveisPorEstagioParaArcano("MILAGRES")["ressurreicao"])
    }

    @Test
    fun `milagres exige guerreiro do senhor e ira do senhor nos poderes corretos`() {
        val state = CriadorState().apply {
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_milagres",
                    nome = "ANTECEDENTE ARCANO (Milagres)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        assertFalse(state.atendeRequisitoEspecialDePoderPorArcano("MILAGRES", "raio"))
        assertFalse(state.atendeRequisitoEspecialDePoderPorArcano("MILAGRES", "rajada"))

        state.vantagensSelecionadas.add(
            Vantagem(
                id = "guerreiro_do_senhor",
                nome = "GUERREIRO DO SENHOR",
                categoria = Categoria.PODER,
                origem = "SOL_VAPOR",
                requisitos = Requisito()
            )
        )
        assertTrue(state.atendeRequisitoEspecialDePoderPorArcano("MILAGRES", "raio"))
        assertFalse(state.atendeRequisitoEspecialDePoderPorArcano("MILAGRES", "rajada"))

        state.vantagensSelecionadas.add(
            Vantagem(
                id = "ira_do_senhor",
                nome = "IRA DO SENHOR",
                categoria = Categoria.PODER,
                origem = "SOL_VAPOR",
                requisitos = Requisito()
            )
        )
        assertTrue(state.atendeRequisitoEspecialDePoderPorArcano("MILAGRES", "rajada"))
    }
}
