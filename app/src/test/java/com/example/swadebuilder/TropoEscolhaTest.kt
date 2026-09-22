package com.example.swadebuilder

import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.TropoEscolha
import com.example.swadebuilder.model.targetRefPorEscolhaTropo
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Mecanismo genérico de "escolha do jogador que decide o alvo de uma habilidade de Tropo" —
 * caso real: Kensai (Youxia, Arte da Guerra) escolhe se a Arma Predileta bonifica Lutar,
 * Atirar ou Atletismo, e a MESMA escolha decide tanto o bônus de perícia quanto (quando o
 * caminho de Vantagem de Tropo existir) o alvo da Vantagem concedida (ver
 * docs/auditoria_mecanica_racas_2026-08-31.md rodada 43).
 */
class TropoEscolhaTest {

    private fun stateComTropo(tropo: Tropo): CriadorState {
        val state = CriadorState()
        state.updateGameData(
            GameDataSnapshot(
                listaComplicacoes = emptyList(),
                listaCoracoesCrystal = emptyList(),
                listaAncestralidadesJson = listOf(RacialModifier(nome = "Humano")),
                listaMonstroTemplates = emptyList(),
                listaAtributos = emptyList(),
                mapaAtributosDisplay = emptyMap(),
                listaPericias = listOf(
                    Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true),
                    Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true),
                    Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)
                ),
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
        state.tropoSelecionado = tropo
        return state
    }

    private val kensaiTropo = Tropo(
        id = "tropo_youxia", nome = "Youxia", categoria = "TROPO", origem = "ARTE_DA_GUERRA",
        descricao = "",
        habilidades = listOf(
            RacialAbility(
                nome = "Arma Predileta (Kensai)", descricao = "",
                traitId = "SKILL_STEP_UP", targetRef = targetRefPorEscolhaTropo("kensai_pericia"), value = 1
            )
        ),
        escolhas = listOf(TropoEscolha(id = "kensai_pericia", rotulo = "Perícia da Arma Predileta", opcoes = listOf("Lutar", "Atirar", "Atletismo"), padrao = "Lutar"))
    )

    @Test
    fun `sem escolha explicita, resolve pro padrao da TropoEscolha`() {
        val state = stateComTropo(kensaiTropo)

        assertEquals("Lutar", state.tropoEscolhaAtual("kensai_pericia"))
        val lutar = Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)
        assertEquals(6, state.periciaStartRaw("Humano", lutar)) // d4 base + 1 passo (relativo) = d6
    }

    @Test
    fun `escolha explicita do jogador troca o alvo do bonus de pericia`() {
        val state = stateComTropo(kensaiTropo)
        state.escolherTropoOpcao("kensai_pericia", "Atirar")

        assertEquals("Atirar", state.tropoEscolhaAtual("kensai_pericia"))
        val atirar = Pericia(nome = "Atirar", atributo = "AGILIDADE", basica = true)
        val lutar = Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)
        assertEquals(6, state.periciaStartRaw("Humano", atirar)) // agora é Atirar que sobe
        assertEquals(4, state.periciaStartRaw("Humano", lutar)) // Lutar volta a não ter bônus
    }

    @Test
    fun `habilidadesDoTropoResolvidas substitui o targetRef pela escolha atual, nunca vaza o marcador cru`() {
        val state = stateComTropo(kensaiTropo)
        state.escolherTropoOpcao("kensai_pericia", "Atletismo")

        val resolvidas = state.habilidadesDoTropoResolvidas
        assertEquals(1, resolvidas.size)
        assertEquals("Atletismo", resolvidas[0].targetRef)
    }

    @Test
    fun `escolha com valor invalido (fora da lista de opcoes) cai no padrao, nunca quebra`() {
        val state = stateComTropo(kensaiTropo)
        state.escolherTropoOpcao("kensai_pericia", "Perícia Que Não Existe")

        assertEquals("Lutar", state.tropoEscolhaAtual("kensai_pericia"))
    }
}
