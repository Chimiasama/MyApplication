package com.example.swadebuilder

import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.TropoEscolha
import com.example.swadebuilder.model.targetRefPorEscolhaTropo
import com.example.swadebuilder.util.keyify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `CriadorState.aplicarTropo()`/`escolherTropoOpcao()` — reconcilia `vantagensRaciais` com as
 * Vantagens grátis que o Tropo selecionado concede AGORA (mesmo padrão de `aplicarTipoMonstro`,
 * reaproveitando `vantagensGratisEfetivas()`), inclusive quando é a PRÓPRIA escolha do jogador
 * dentro do Tropo que decide qual Vantagem é concedida (ex.: Artista Marcial "Potencial
 * Físico": Agilidade->Esquiva, Força->Bloquear, Vigor->Reflexos de Combate).
 */
class TropoAplicarGrantsTest {

    private fun estadoBase(): CriadorState {
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
                listaVantagens = emptyList(),
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
    fun `escolher um Tropo com GRANTED_EDGE concede a Vantagem de verdade em vantagensRaciais`() {
        val state = estadoBase()
        state.aplicarTropo(tropoComGrant("tropo_a", "comando"))

        assertTrue(state.vantagensRaciais.any { it.keyify() == "comando".keyify() })
    }

    @Test
    fun `trocar de Tropo remove a Vantagem do anterior e concede a do novo`() {
        val state = estadoBase()
        state.aplicarTropo(tropoComGrant("tropo_a", "comando"))
        state.aplicarTropo(tropoComGrant("tropo_b", "dominio"))

        assertFalse(state.vantagensRaciais.any { it.keyify() == "comando".keyify() })
        assertTrue(state.vantagensRaciais.any { it.keyify() == "dominio".keyify() })
    }

    @Test
    fun `voltar pra nenhum Tropo remove a Vantagem concedida`() {
        val state = estadoBase()
        state.aplicarTropo(tropoComGrant("tropo_a", "comando"))
        state.aplicarTropo(null)

        assertFalse(state.vantagensRaciais.any { it.keyify() == "comando".keyify() })
    }

    // Artista Marcial "Potencial Físico" (Arte da Guerra): a MESMA TropoEscolha decide qual
    // Vantagem é concedida, não só qual perícia/atributo sobe (Agilidade->Esquiva,
    // Força->Bloquear, Vigor->Reflexos de Combate) — modelado abaixo com as OPÇÕES da escolha
    // já sendo os próprios ids de Vantagem, e um único GRANTED_EDGE cujo targetRef é a
    // escolha em si (`targetRefPorEscolhaTropo`), resolvido por `habilidadesDoTropoResolvidas`.
    @Test
    fun `trocar uma TropoEscolha que muda a Vantagem concedida reconcilia vantagensRaciais tambem`() {
        // Modelagem simplificada: 1 habilidade só, cujo targetRef final depende inteiramente
        // da escolha (resolvida "na mão" via 3 Tropos diferentes simulando 3 opções) —
        // confirma que escolherTropoOpcao() dispara a mesma reconciliação de aplicarTropo().
        val tropoComEscolhaQueMudaGrant = Tropo(
            id = "tropo_teste", nome = "Teste", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "",
            habilidades = listOf(
                RacialAbility(nome = "Grant Dinâmico", descricao = "", category = "racial_edge", traitId = "GRANTED_EDGE", targetRef = targetRefPorEscolhaTropo("qual_vantagem"))
            ),
            escolhas = listOf(TropoEscolha(id = "qual_vantagem", rotulo = "Qual Vantagem", opcoes = listOf("esquiva", "bloquear", "reflexos_de_combate"), padrao = "esquiva"))
        )
        val state = estadoBase()
        state.aplicarTropo(tropoComEscolhaQueMudaGrant)
        assertTrue(state.vantagensRaciais.any { it.keyify() == "esquiva".keyify() })

        state.escolherTropoOpcao("qual_vantagem", "bloquear")

        assertFalse(state.vantagensRaciais.any { it.keyify() == "esquiva".keyify() })
        assertTrue(state.vantagensRaciais.any { it.keyify() == "bloquear".keyify() })
    }
}
