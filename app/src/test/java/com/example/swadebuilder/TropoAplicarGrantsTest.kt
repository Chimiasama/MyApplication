package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.TropoEscolha
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.targetRefPorEscolhaTropo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `CriadorState.selecionarTropo()`/`escolherTropoOpcao()` — reconciliam
 * `vantagensSelecionadas`/`vantagensAutomaticasDoTropo` com as Vantagens que
 * `habilidadesDoTropoResolvidas` concede AGORA (mesmo mecanismo/lista que
 * `Tropo.ganhaAoComprar` já usa, só que lendo `habilidades[]`), inclusive quando é a PRÓPRIA
 * escolha do jogador dentro do Tropo que decide qual Vantagem é concedida (ex.: Artista
 * Marcial "Potencial Físico": Agilidade->Esquiva, Força->Bloquear, Vigor->Reflexos de
 * Combate).
 */
class TropoAplicarGrantsTest {

    private fun vantagem(id: String) = Vantagem(
        id = id, nome = id, categoria = Categoria.COMBATE, requisitos = Requisito(estagio = "Novato")
    )

    private fun estadoBase(vararg vantagensIds: String): CriadorState {
        val state = CriadorState()
        state.updateGameData(
            GameDataSnapshot(
                listaComplicacoes = emptyList(),
                listaCoracoesCrystal = emptyList(),
                listaAncestralidadesJson = listOf(RacialModifier(nome = "Humano")),
                listaMonstroTemplates = emptyList(),
                listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
                mapaAtributosDisplay = emptyMap(),
                listaPericias = listOf(Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)),
                mapaPericias = emptyMap(),
                mapaAtributosDescricao = emptyMap(),
                listaVantagens = vantagensIds.map { vantagem(it) },
                listaPoderes = emptyList(),
                listaTropos = emptyList(),
                listaEquipamentos = emptyList(),
                equipamentoCategorias = emptyList(),
                superequipCategorias = emptyList(),
                listaSuperPoderes = emptyList(),
                arcanoInfo = emptyList()
            )
        )
        state.ancestralidade = "Humano"
        return state
    }

    private fun tropoComGrant(id: String, targetRef: String) = Tropo(
        id = id, nome = id, categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "",
        habilidades = listOf(
            RacialAbility(nome = "Grant", descricao = "", category = "racial_edge", traitId = "GRANTED_EDGE", targetRef = targetRef)
        )
    )

    @Test
    fun `selecionar um Tropo com GRANTED_EDGE concede a Vantagem de verdade em vantagensSelecionadas`() {
        val state = estadoBase("comando")
        state.selecionarTropo(tropoComGrant("tropo_a", "comando"))

        assertTrue(state.vantagensSelecionadas.any { it.id == "comando" })
        assertTrue(state.vantagensAutomaticasDoTropo.contains("comando"))
    }

    @Test
    fun `trocar de Tropo remove a Vantagem do anterior e concede a do novo`() {
        val state = estadoBase("comando", "dominio")
        state.selecionarTropo(tropoComGrant("tropo_a", "comando"))
        state.selecionarTropo(tropoComGrant("tropo_b", "dominio"))

        assertFalse(state.vantagensSelecionadas.any { it.id == "comando" })
        assertTrue(state.vantagensSelecionadas.any { it.id == "dominio" })
    }

    @Test
    fun `voltar pra nenhum Tropo remove a Vantagem concedida`() {
        val state = estadoBase("comando")
        state.selecionarTropo(tropoComGrant("tropo_a", "comando"))
        state.selecionarTropo(null)

        assertFalse(state.vantagensSelecionadas.any { it.id == "comando" })
    }

    @Test
    fun `trocar uma TropoEscolha que muda a Vantagem concedida reconcilia vantagensSelecionadas tambem`() {
        // Artista Marcial "Potencial Físico" (Arte da Guerra): a MESMA TropoEscolha decide
        // qual Vantagem é concedida — modelado com as próprias opções da escolha já sendo os
        // ids reais de Vantagem, e um único GRANTED_EDGE cujo targetRef é a escolha em si.
        val tropo = Tropo(
            id = "tropo_teste", nome = "Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "",
            habilidades = listOf(
                RacialAbility(nome = "Grant Dinâmico", descricao = "", category = "racial_edge", traitId = "GRANTED_EDGE", targetRef = targetRefPorEscolhaTropo("qual_vantagem"))
            ),
            escolhas = listOf(TropoEscolha(id = "qual_vantagem", rotulo = "Qual Vantagem", opcoes = listOf("esquiva", "bloquear", "reflexos_de_combate"), padrao = "esquiva"))
        )
        val state = estadoBase("esquiva", "bloquear", "reflexos_de_combate")
        state.selecionarTropo(tropo)
        assertTrue(state.vantagensSelecionadas.any { it.id == "esquiva" })

        state.escolherTropoOpcao("qual_vantagem", "bloquear")

        assertFalse(state.vantagensSelecionadas.any { it.id == "esquiva" })
        assertTrue(state.vantagensSelecionadas.any { it.id == "bloquear" })
    }

    @Test
    fun `ganhaAoComprar antigo e habilidades novo convivem sem conflito no mesmo Tropo`() {
        val tropo = Tropo(
            id = "tropo_hibrido", nome = "Híbrido", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "",
            ganhaAoComprar = listOf("comando"),
            habilidades = listOf(
                RacialAbility(nome = "Grant Novo", descricao = "", category = "racial_edge", traitId = "GRANTED_EDGE", targetRef = "dominio")
            )
        )
        val state = estadoBase("comando", "dominio")
        state.selecionarTropo(tropo)

        assertTrue(state.vantagensSelecionadas.any { it.id == "comando" })
        assertTrue(state.vantagensSelecionadas.any { it.id == "dominio" })
    }
}
