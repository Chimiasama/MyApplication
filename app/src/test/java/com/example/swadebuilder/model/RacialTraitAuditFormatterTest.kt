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
    fun `id totalmente sem catalogo, CUSTOS nem efeito vira aviso de hardcode`() {
        val hab = RacialAbility(nome = "Nome Qualquer", descricao = "", id = "ID_INEXISTENTE_QUALQUER")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("SEM CATÁLOGO"))
        assertTrue(linhas[0].contains("Nome Qualquer"))
    }

    @Test
    fun `id sem catalogo oficial e sem LABEL, mas com custo calibrado em CUSTOS, NAO vira aviso de hardcode`() {
        // "CARISMATICO" (Transmorfos) é um caso real: sem entrada genérica em
        // basico_habilidades_raciais.json nem LABEL, mas com custo calibrado em
        // RacialTraitPointCatalog.CUSTOS — é um traço bem específico da raça, não sujeira.
        val hab = RacialAbility(nome = "Carismático (racial)", descricao = "", id = "CARISMATICO")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(!linhas[0].contains("SEM CATÁLOGO"))
        assertTrue(linhas[0].contains("Traço específico desta raça"))
        assertTrue(linhas[0].contains("+2 pts"))
    }

    @Test
    fun `traco empilhavel mostra x vezes e pontos multiplicados`() {
        val hab = RacialAbility(nome = "Tamanho", descricao = "", id = "TAMANHO_MAIS_1", vezes = 3)
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("x3"))
        assertTrue(linhas[0].contains("+3 pts"))
    }

    @Test
    fun `id exclusivo de uma raca ganha a etiqueta no cabecalho`() {
        val hab = RacialAbility(nome = "Magia Gnômica", descricao = "", id = "MAGIA_GNOMICA")
        val semExclusividade = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(!semExclusividade[0].contains("exclusivo-desta-raça"))

        val comExclusividade = RacialTraitAuditFormatter.formatar(
            listOf(hab), catalogoOficial, mapOf("MAGIA_GNOMICA" to "Gnomo")
        )
        assertTrue(comExclusividade[0].contains("exclusivo-desta-raça"))
    }

    @Test
    fun `traco migrado pro par generico ATTRIBUTE_BOOST ainda acha o LABEL do id original`() {
        // Achado real (auditoria pedida pelo usuário): a migração de Atributo/Perícia
        // Aumentada pra traitId="ATTRIBUTE_BOOST"/"SKILL_BOOST" (id mantido só pra
        // identidade/auditoria) fazia a formatação buscar LABEL/catálogo oficial pela chave
        // RESOLVIDA ("ATTRIBUTE_BOOST", sem entrada própria), perdendo o match do id
        // ORIGINAL ("ARMADURA", que tem LABEL "Armadura +2") e caindo no ramo de "sem LABEL
        // nem catálogo" mesmo com um label real cadastrado.
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            id = "ARMADURA",
            traitId = "ATTRIBUTE_BOOST",
            targetRef = "VIGOR"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("[id=ATTRIBUTE_BOOST"))
        assertTrue(!linhas[0].contains("sem LABEL nem catálogo"))
        assertTrue(linhas[0].contains("Armadura +2"))
    }

    @Test
    fun `GRANTED_EDGE calcula o custo dinamicamente pelo Estagio da Vantagem real, nao fixo em 2`() {
        val vantagemExperiente = Vantagem(
            id = "vantagem_experiente_teste",
            nome = "Vantagem Experiente Teste",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Experiente")
        )
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            traitId = "GRANTED_EDGE",
            targetRef = "Vantagem Experiente Teste",
            category = "racial_edge"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial, allVantagens = listOf(vantagemExperiente))
        assertTrue(linhas[0].contains("+3 pts"))
    }

    @Test
    fun `calcularIdsExclusivos so marca ids usados por exatamente uma raca`() {
        val racaA = RacialModifier(
            nome = "Raça A",
            habilidades = listOf(
                RacialAbility(nome = "Traço Comum", descricao = "", id = "COMUM"),
                RacialAbility(nome = "Traço Só de A", descricao = "", id = "SO_A")
            )
        )
        val racaB = RacialModifier(
            nome = "Raça B",
            habilidades = listOf(RacialAbility(nome = "Traço Comum", descricao = "", id = "COMUM"))
        )
        val exclusivos = RacialTraitAuditFormatter.calcularIdsExclusivos(listOf(racaA, racaB))
        assertEquals(mapOf("SO_A" to "Raça A"), exclusivos)
    }
}
