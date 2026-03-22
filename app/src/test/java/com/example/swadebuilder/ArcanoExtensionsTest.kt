package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcanoExtensionsTest {

    @Test
    fun `aa magia negra mapeia para chave feiticeiro`() {
        val vantagem = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.PODER,
            origem = "SOL_VAPOR",
            requisitos = Requisito()
        )

        assertEquals("FEITICEIRO", vantagem.toArcanoKey())
    }
}
