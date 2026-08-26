package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RacialGrantResolverTest {

    @Test
    fun `vantagem gratis real liga ao id do catalogo e custa 2`() {
        val link = RacialGrantResolver.resolveVantagemGratis("PRONTIDÃO")
        assertEquals("prontidao", link.vantagemId)
        assertEquals(2, link.custo)
    }

    @Test
    fun `vantagem gratis que ja vem como id ainda resolve certo`() {
        val link = RacialGrantResolver.resolveVantagemGratis("aa_agente_syn")
        assertEquals("aa_agente_syn", link.vantagemId)
        assertEquals(3, link.custo)
    }

    @Test
    fun `vantagem gratis sem contrapartida real cai no catalogo de tracos`() {
        val link = RacialGrantResolver.resolveVantagemGratis("RESISTÊNCIA")
        assertNull(link.vantagemId)
        assertEquals(RacialTraitPointCatalog.custoDe("RESISTENCIA"), link.custo)
    }

    @Test
    fun `desvantagem real liga ao id da complicacao e usa a severidade certa`() {
        val menor = RacialGrantResolver.resolveDesvantagem("DESASTRADO")
        assertEquals("desastrado", menor.complicacaoId)
        assertEquals(-1, menor.custo)

        val maior = RacialGrantResolver.resolveDesvantagem("DEPENDÊNCIA")
        assertEquals("dependencia", maior.complicacaoId)
        assertEquals(-2, maior.custo)
    }

    @Test
    fun `forasteiro usa a severidade do texto sobre a mesma complicacao generica`() {
        val menor = RacialGrantResolver.resolveDesvantagem("FORASTEIRO (Menor)")
        val maior = RacialGrantResolver.resolveDesvantagem("FORASTEIRO (Maior)")
        assertEquals("forasteiro", menor.complicacaoId)
        assertEquals("forasteiro", maior.complicacaoId)
        assertEquals(-1, menor.custo)
        assertEquals(-2, maior.custo)
    }

    @Test
    fun `azarado usa a severidade real do catalogo, nao a anotacao da ancestralidade`() {
        // complicacoes.json define Azarado como Maior fixo; a anotação em
        // ancestralidades.json pra Nekomimi (Menor) está errada - o catálogo
        // real é a fonte de verdade.
        val link = RacialGrantResolver.resolveDesvantagem("AZARADO")
        assertEquals(-2, link.custo)
    }

    @Test
    fun `texto desconhecido nao quebra, so nao pontua`() {
        val link = RacialGrantResolver.resolveDesvantagem("Isso não existe em lugar nenhum")
        assertEquals(0, link.custo)
        assertNull(link.complicacaoId)
    }
}
