package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Signos de Nascença (Humano Arte da Guerra) migrados pro sistema de
 * Seleção (AncestryVariantRegistry.humanoArteDaGuerraSignos()), mesmo
 * padrão de Terracota/Umvee/Elementais/Meio-Elfo — em vez de uma cadeia de
 * "if (signId == 'BOI')" espalhada em CriadorState. Estes testes fixam que
 * a mecânica de cada Signo continua correta depois da migração, e que
 * trocar de Signo não deixa traço de um Signo anterior "vazando" (bug que
 * uma implementação ingênua de troca poderia introduzir).
 */
class CriadorStateSignoDeNascencaTest {

    private fun snapshotComHumanoAdg(): GameDataSnapshot {
        val humanoAdg = RacialModifier(
            nome = "HUMANOS",
            habilidades = listOf(
                RacialAbility(nome = "Signos de Nascença", descricao = "", id = "SIGNOS_DE_NASCENCA")
            ),
            origem = "ARTE_DA_GUERRA"
        )
        return GameDataSnapshot(
            listaComplicacoes = emptyList<Complicacao>(),
            listaCoracoesCrystal = emptyList<CrystalHeart>(),
            listaAncestralidadesJson = listOf(humanoAdg),
            listaMonstroTemplates = emptyList<MonstroTemplate>(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
            mapaAtributosDisplay = emptyMap(),
            listaPericias = listOf(
                Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true),
                Pericia(nome = "Acrobacia", atributo = "AGILIDADE", basica = false),
                Pericia(nome = "Curar", atributo = "ASTUCIA", basica = false)
            ),
            mapaPericias = emptyMap(),
            mapaAtributosDescricao = emptyMap(),
            listaVantagens = emptyList<Vantagem>(),
            listaPoderes = emptyList<Poder>(),
            listaTropos = emptyList<Tropo>(),
            listaEquipamentos = emptyList<EquipamentoItem>(),
            equipamentoCategorias = emptyList<EquipamentoCategoria>(),
            superequipCategorias = emptyList<EquipamentoCategoria>(),
            listaSuperPoderes = emptyList<SuperPoder>(),
            arcanoInfo = emptyList<ArcanoInfo>()
        )
    }

    private fun estadoComSigno(signo: String?): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshotComHumanoAdg())
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "HUMANOS"
        state.signoAdgSelecionado = signo
        return state
    }

    @Test
    fun `signo nenhum concede adaptavel e 15 pontos de pericia`() {
        val state = estadoComSigno("Nenhum")

        assertTrue(state.temAdaptavel())
        assertEquals(15, state.totalSpPool)
    }

    @Test
    fun `signo boi nao concede adaptavel nem pontos extra e sobe Forca`() {
        val state = estadoComSigno("Boi")

        assertFalse(state.temAdaptavel())
        assertEquals(12, state.totalSpPool)
        assertEquals(6, state.atributoMinRaw("Força"))
    }

    @Test
    fun `trocar de boi para dragao nao deixa Forca vazando e sobe Espirito`() {
        val state = estadoComSigno("Boi")
        assertEquals(6, state.atributoMinRaw("Força"))

        state.selecionarSigno("Dragão")

        assertEquals(4, state.atributoMinRaw("Força"))
        assertEquals(6, state.atributoMinRaw("Espírito"))
    }

    @Test
    fun `signo kirin soma 1 na reserva de chi`() {
        val semSigno = estadoComSigno(null)
        val comKirin = estadoComSigno("Kirin")

        assertEquals(semSigno.reservaChi + 1, comKirin.reservaChi)
    }

    @Test
    fun `signo lebre concede Curar d6`() {
        val state = estadoComSigno("Lebre")
        val curar = Pericia(nome = "Curar", atributo = "ASTUCIA", basica = false)

        assertEquals(6, state.periciaStartRaw(state.ancestralidade, curar))
    }

    @Test
    fun `signo garca concede Acrobacia d4, Atletismo d6 e Aparar mais 1`() {
        val state = estadoComSigno("Garça")
        val acrobacia = Pericia(nome = "Acrobacia", atributo = "AGILIDADE", basica = false)
        val atletismo = Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)

        assertEquals(4, state.periciaStartRaw(state.ancestralidade, acrobacia))
        assertEquals(6, state.periciaStartRaw(state.ancestralidade, atletismo))
        assertTrue(
            state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "APARAR" }
        )
    }

    @Test
    fun `signo tigre nao quebra nada mesmo sem nenhum efeito numerico modelado ainda`() {
        val state = estadoComSigno("Tigre")

        assertFalse(state.temAdaptavel())
        assertEquals(12, state.totalSpPool)
    }
}
