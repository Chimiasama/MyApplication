package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OriginKeysTest {

    @Test
    fun `cidade do sol e vapor aponta para asset de sol vapor`() {
        assertEquals("SOL_VAPOR", powerAssetOriginKey("CIDADE_SOL_VAPOR"))
        assertEquals("SOL_VAPOR", powerAssetOriginKey("CIDADE DO SOL A VAPOR"))
    }

    @Test
    fun `origens legadas de sol vapor continuam normalizadas`() {
        assertEquals("SOL_VAPOR", powerAssetOriginKey("SOL_VAPOR"))
        assertEquals("SOL_VAPOR", powerAssetOriginKey("SOL E VAPOR"))
    }

    @Test
    fun `livros de cenario e companheiros tem prioridade sobre o Basico`() {
        assertTrue(originPriority("SCI_FI") > originPriority("BASICO"))
        assertTrue(originPriority("FANTASIA") > originPriority("BASICO"))
        assertTrue(originPriority("HORROR") > originPriority("BASICO"))
        assertTrue(originPriority("ARTE_DA_GUERRA") > originPriority("BASICO"))
        assertEquals(0, originPriority("BASICO"))
        assertEquals(0, originPriority(null))
    }

    @Test
    fun `horror vence fantasia que vence os demais companheiros`() {
        assertTrue(originPriority("HORROR") > originPriority("FANTASIA"))
        assertTrue(originPriority("FANTASIA") > originPriority("SCI_FI"))
    }

    private data class Racial(val nome: String, val origem: String, val opcoes: List<String> = emptyList())

    @Test
    fun `distinctByOriginPriority mantem item unico sem colisao`() {
        val itens = listOf(Racial("Elfos", "SCI_FI"))
        val result = itens.distinctByOriginPriority({ it.origem }, { it.nome.uppercase() })
        assertEquals(1, result.size)
        assertEquals("SCI_FI", result.first().origem)
    }

    @Test
    fun `distinctByOriginPriority prefere livro companheiro sobre o Basico em colisao`() {
        // Regressão do bug real: com Sci-Fi ativo junto do Básico, "Anões" existe nos dois
        // livros com conteúdo diferente (só a versão Sci-Fi tem a variante Ciber). Um
        // distinctBy ingênuo mantinha a primeira ocorrência do arquivo (Básico, sem opções),
        // escondendo a variante Ciber da tela de seleção de raça.
        val basico = Racial("Anões", "BASICO", opcoes = emptyList())
        val sciFi = Racial("Anões", "SCI_FI", opcoes = listOf("Básico", "Ciber"))

        val resultBasicoPrimeiro = listOf(basico, sciFi)
            .distinctByOriginPriority({ it.origem }, { it.nome.uppercase() })
        val resultSciFiPrimeiro = listOf(sciFi, basico)
            .distinctByOriginPriority({ it.origem }, { it.nome.uppercase() })

        // O vencedor deve ser sempre o de maior prioridade (Sci-Fi), independente da ordem
        // de chegada na lista de entrada.
        for (result in listOf(resultBasicoPrimeiro, resultSciFiPrimeiro)) {
            assertEquals(1, result.size)
            assertEquals("SCI_FI", result.first().origem)
            assertEquals(listOf("Básico", "Ciber"), result.first().opcoes)
        }
    }

    @Test
    fun `distinctByOriginPriority preserva itens de nomes diferentes`() {
        val itens = listOf(
            Racial("Anões", "BASICO"),
            Racial("Anões", "SCI_FI", opcoes = listOf("Básico", "Ciber")),
            Racial("Elfos", "SCI_FI")
        )
        val result = itens.distinctByOriginPriority({ it.origem }, { it.nome.uppercase() })
        assertEquals(2, result.size)
        assertTrue(result.any { it.nome == "Anões" && it.origem == "SCI_FI" })
        assertTrue(result.any { it.nome == "Elfos" })
    }
}
