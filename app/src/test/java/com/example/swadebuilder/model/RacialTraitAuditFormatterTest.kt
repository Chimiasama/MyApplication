package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Modo Auditoria: ID de traço" (ver AppPreferences.loadModoAuditoriaIdPuro,
 * AncestralidadesSection.kt "Ver detalhes"): o formatador precisa ignorar `nome`/`descricao`
 * reskinados de uma `RacialAbility` e mostrar só o id/traitId contra a definição oficial do
 * catálogo (`basico_habilidades_raciais.json`), pra quem audita conseguir separar "traço com
 * id de verdade" de "sujeira de hardcode".
 */
class RacialTraitAuditFormatterTest {

    private val catalogoOficial = listOf(
        HabilidadeCriacao(
            nome = "Arma de Sopro",
            custo = 2,
            descricao = "Pode cuspir fogo, frio, ácido ou outra energia (Modelo de Cone, rolagem de Atletismo, causa 2d6 de dano, 3d6 com ampliação).",
            id = "arma_de_sopro"
        )
    )

    @Test
    fun `id com entrada no catalogo oficial usa a descricao oficial, nunca o nome customizado`() {
        val hab = RacialAbility(
            nome = "Bafo Flamejante", // nome "reskinado" — não deve aparecer na definição
            descricao = "descrição de skin qualquer",
            id = "ARMA_DE_SOPRO"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertEquals(1, linhas.size)
        assertTrue(linhas[0].contains("[id=ARMA_DE_SOPRO]"))
        assertTrue(linhas[0].contains("Arma de Sopro: Pode cuspir fogo"))
        assertTrue(!linhas[0].contains("Bafo Flamejante"))
    }

    @Test
    fun `id so com LABEL, sem catalogo oficial, mostra o rotulo e avisa que nao tem entrada`() {
        val hab = RacialAbility(nome = "qualquer skin", descricao = "", id = "ARMADURA")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("[id=ARMADURA]"))
        assertTrue(linhas[0].contains("Armadura +2"))
        assertTrue(linhas[0].contains("sem entrada em basico_habilidades_raciais.json"))
    }

    @Test
    fun `GRANTED_EDGE mostra o targetRef como Vantagem Gratis, nao tenta achar id de traco`() {
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            traitId = "GRANTED_EDGE",
            targetRef = "Carismático",
            category = "racial_edge"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("Vantagem Grátis concedida ao personagem: Carismático"))
    }

    @Test
    fun `id totalmente sem catalogo e sem efeito vira aviso de hardcode`() {
        val hab = RacialAbility(nome = "Nome Qualquer", descricao = "", id = "ID_INEXISTENTE_QUALQUER")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("SEM CATÁLOGO"))
        assertTrue(linhas[0].contains("Nome Qualquer"))
    }

    @Test
    fun `traco empilhavel mostra x vezes e pontos multiplicados`() {
        val hab = RacialAbility(nome = "Tamanho", descricao = "", id = "TAMANHO_MAIS_1", vezes = 3)
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("x3"))
        assertTrue(linhas[0].contains("+3 pts"))
    }
}
