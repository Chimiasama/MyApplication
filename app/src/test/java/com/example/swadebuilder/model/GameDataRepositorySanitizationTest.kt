package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameDataRepositorySanitizationTest {

    @Test
    fun `sanitizeSnapshotForRuntime remove duplicados e preserva ultima ocorrencia`() {
        val snapshot = fixtureSnapshot(
            pericias = listOf(
                Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true, origem = "BASICO"),
                Pericia(nome = "Atirar", atributo = "ESPIRITO", basica = false, origem = "SUPER")
            ),
            vantagens = listOf(
                Vantagem(id = "parceiro", nome = "Parceiro (Básico)", categoria = Categoria.SOCIAIS, origem = "BASICO", requisitos = Requisito()),
                Vantagem(id = "PARCEIRO", nome = "Parceiro (Super)", categoria = Categoria.SOCIAIS, origem = "SUPER", requisitos = Requisito())
            ),
            poderes = listOf(
                Poder(id = "rajada", nome = "Rajada (Básico)", origem = "BASICO", estagio = "Novato", pontosDePoder = "1", distancia = "", duracao = "", descricao = ""),
                Poder(id = "RAJADA", nome = "Rajada (Super)", origem = "SUPER", estagio = "Novato", pontosDePoder = "1", distancia = "", duracao = "", descricao = "")
            )
        )

        val sanitized = sanitizeSnapshotForRuntime(snapshot)

        assertEquals(1, sanitized.listaPericias.size)
        assertEquals(1, sanitized.listaVantagens.size)
        assertEquals(1, sanitized.listaPoderes.size)

        assertEquals("ESPIRITO", sanitized.listaPericias.single().atributo)
        assertEquals("Parceiro (Super)", sanitized.listaVantagens.single().nome)
        assertEquals("Rajada (Super)", sanitized.listaPoderes.single().nome)

        assertTrue(sanitized.mapaPericias.containsKey("ATIRAR"))
        assertEquals("ESPIRITO", sanitized.mapaPericias.getValue("ATIRAR").atributo)
    }

    private fun fixtureSnapshot(
        pericias: List<Pericia> = listOf(Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true)),
        vantagens: List<Vantagem> = listOf(Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE, requisitos = Requisito())),
        poderes: List<Poder> = listOf(Poder(id = "rajada", nome = "Rajada", origem = "BASICO", estagio = "Novato", pontosDePoder = "1", distancia = "", duracao = "", descricao = ""))
    ) = GameDataSnapshot(
        listaComplicacoes = emptyList(),
        listaCoracoesCrystal = emptyList(),
        listaAncestralidadesJson = emptyList(),
        listaMonstroTemplates = emptyList(),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
        listaAtributos = emptyList(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = pericias,
        mapaPericias = pericias.associateBy { it.nome.uppercase() },
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = vantagens,
        listaPoderes = poderes,
        listaTropos = emptyList(),
        listaEquipamentos = emptyList(),
        equipamentoCategorias = emptyList(),
        superequipCategorias = emptyList(),
        listaSuperPoderes = emptyList(),
        arcanoInfo = emptyList()
    )
}
