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
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.paraTropo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `isAttributeFreeForMonster` usava uma lista fixa (Agilidade/Força/Vigor) que
 * só coincidia com Lobisomem/Monstro de Retalhos/Múmia/Vampiro. Fantasma,
 * Demônio e Revivido bonificam Espírito e ficavam sem o benefício; Anjo e
 * Fantasma ganhavam o benefício em atributos que seu template nem bonifica.
 *
 * Monstro Heroico virou Tropo de verdade (rodada 44) — `isAttributeFreeForMonster`
 * agora deriva de `tropoSelecionado` (categoria="MONSTRO") em vez de
 * `tipoMonstroSelecionado`/`MonstroTemplate.atributosBonus` direto, via a mesma
 * conversão `MonstroTemplate.paraTropo()` que `DataLoader` usa de verdade.
 */
class CriadorStateMonsterFreeAttributeTest {

    private fun snapshotBase(): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = listOf(
            RacialModifier(nome = "HUMANOS", origem = "BASICO")
        ),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = listOf(Pericia(nome = "Fé", atributo = "ESPIRITO", basica = false)),
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

    private val fantasma = MonstroTemplate(id = "fantasma", nome = "Fantasma", descricao = "teste", atributosBonus = mapOf("Espirito" to 1))
    private val lobisomem = MonstroTemplate(id = "lobisomem", nome = "Lobisomem", descricao = "teste", atributosBonus = mapOf("Agilidade" to 2, "Forca" to 2, "Vigor" to 2))
    private val anjo = MonstroTemplate(id = "anjo", nome = "Anjo", descricao = "teste", atributosBonus = mapOf("Fe" to 1, "Forca" to 2, "Vigor" to 2))

    private fun estadoCom(monstro: MonstroTemplate?): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshotBase())
        state.selecionarTropo(monstro?.paraTropo())
        return state
    }

    @Test
    fun `fantasma libera Espirito, nao Agilidade Forca ou Vigor`() {
        val state = estadoCom(fantasma)

        assertTrue(state.isAttributeFreeForMonster("Espírito"))
        assertFalse(state.isAttributeFreeForMonster("Agilidade"))
        assertFalse(state.isAttributeFreeForMonster("Força"))
        assertFalse(state.isAttributeFreeForMonster("Vigor"))
    }

    @Test
    fun `lobisomem libera Agilidade Forca e Vigor, nao Espirito`() {
        val state = estadoCom(lobisomem)

        assertTrue(state.isAttributeFreeForMonster("Agilidade"))
        assertTrue(state.isAttributeFreeForMonster("Força"))
        assertTrue(state.isAttributeFreeForMonster("Vigor"))
        assertFalse(state.isAttributeFreeForMonster("Espírito"))
    }

    @Test
    fun `bonus de pericia (Fe) do anjo nao conta como atributo livre`() {
        val state = estadoCom(anjo)

        assertTrue(state.isAttributeFreeForMonster("Força"))
        assertTrue(state.isAttributeFreeForMonster("Vigor"))
        assertFalse(state.isAttributeFreeForMonster("Agilidade"))
        assertFalse(state.isAttributeFreeForMonster("Espírito"))
    }

    @Test
    fun `sem nenhum Tropo selecionado nada e livre`() {
        val state = estadoCom(null)

        assertFalse(state.isAttributeFreeForMonster("Força"))
    }

    @Test
    fun `um Tropo de Arte da Guerra (categoria TROPO, nao MONSTRO) tambem nao libera atributo`() {
        val state = CriadorState()
        state.updateGameData(snapshotBase())
        state.selecionarTropo(
            Tropo(id = "tropo_x", nome = "Tropo X", categoria = "TROPO", origem = "ARTE_DA_GUERRA", descricao = "")
        )

        assertFalse(state.isAttributeFreeForMonster("Força"))
    }
}
