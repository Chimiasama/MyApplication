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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `isAttributeFreeForMonster` usava uma lista fixa (Agilidade/Força/Vigor) que
 * só coincidia com Lobisomem/Monstro de Retalhos/Múmia/Vampiro. Fantasma,
 * Demônio e Revivido bonificam Espírito e ficavam sem o benefício; Anjo e
 * Fantasma ganhavam o benefício em atributos que seu template nem bonifica.
 */
class CriadorStateMonsterFreeAttributeTest {

    private fun snapshotComMonstros(): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = listOf(
            RacialModifier(nome = "HUMANOS", atributos = emptyMap(), pericias = emptyMap(), origem = "BASICO")
        ),
        listaMonstroTemplates = listOf(
            MonstroTemplate(id = "fantasma", nome = "Fantasma", descricao = "teste", atributosBonus = mapOf("Espirito" to 1)),
            MonstroTemplate(id = "lobisomem", nome = "Lobisomem", descricao = "teste", atributosBonus = mapOf("Agilidade" to 2, "Forca" to 2, "Vigor" to 2)),
            MonstroTemplate(id = "anjo", nome = "Anjo", descricao = "teste", atributosBonus = mapOf("Fe" to 1, "Forca" to 2, "Vigor" to 2))
        ),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
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

    @Test
    fun `fantasma libera Espirito, nao Agilidade Forca ou Vigor`() {
        val state = CriadorState()
        state.updateGameData(snapshotComMonstros())
        state.modoMonstroAtivo = true
        state.tipoMonstroSelecionado = "fantasma"

        assertTrue(state.isAttributeFreeForMonster("Espírito"))
        assertFalse(state.isAttributeFreeForMonster("Agilidade"))
        assertFalse(state.isAttributeFreeForMonster("Força"))
        assertFalse(state.isAttributeFreeForMonster("Vigor"))
    }

    @Test
    fun `lobisomem libera Agilidade Forca e Vigor, nao Espirito`() {
        val state = CriadorState()
        state.updateGameData(snapshotComMonstros())
        state.modoMonstroAtivo = true
        state.tipoMonstroSelecionado = "lobisomem"

        assertTrue(state.isAttributeFreeForMonster("Agilidade"))
        assertTrue(state.isAttributeFreeForMonster("Força"))
        assertTrue(state.isAttributeFreeForMonster("Vigor"))
        assertFalse(state.isAttributeFreeForMonster("Espírito"))
    }

    @Test
    fun `bonus de pericia (Fe) do anjo nao conta como atributo livre`() {
        val state = CriadorState()
        state.updateGameData(snapshotComMonstros())
        state.modoMonstroAtivo = true
        state.tipoMonstroSelecionado = "anjo"

        assertTrue(state.isAttributeFreeForMonster("Força"))
        assertTrue(state.isAttributeFreeForMonster("Vigor"))
        assertFalse(state.isAttributeFreeForMonster("Agilidade"))
        assertFalse(state.isAttributeFreeForMonster("Espírito"))
    }

    @Test
    fun `sem modo monstro ativo nada e livre`() {
        val state = CriadorState()
        state.updateGameData(snapshotComMonstros())
        state.modoMonstroAtivo = false
        state.tipoMonstroSelecionado = "lobisomem"

        assertFalse(state.isAttributeFreeForMonster("Força"))
    }
}
