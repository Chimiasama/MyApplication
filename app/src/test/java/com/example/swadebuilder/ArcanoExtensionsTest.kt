package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcanoExtensionsTest {

    @Test
    fun `aa magia negra mapeia para chave feiticeiro via subtipoArcano do catalogo`() {
        // "Magia Negra" é nome de sabor (Sol & Vapor) pro Antecedente Arcano
        // Feiticeiro — subtipoArcano é quem carrega esse mapeamento agora
        // (vantagens.json), não mais um `when` de texto em toArcanoKey().
        val vantagem = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.PODER,
            origem = "SOL_VAPOR",
            requisitos = Requisito(),
            subtipoArcano = "FEITICEIRO"
        )

        assertEquals("FEITICEIRO", vantagem.toArcanoKey())
    }

    @Test
    fun `antecedente arcano customizado sem subtipoArcano cai no nome entre parenteses`() {
        val vantagem = Vantagem(
            id = "aa_custom_ordem_da_teia",
            nome = "ANTECEDENTE ARCANO (Ordem da Teia)",
            categoria = Categoria.PODER,
            origem = "CUSTOM",
            requisitos = Requisito()
        )

        assertEquals("ORDEM DA TEIA", vantagem.toArcanoKey())
    }
}
