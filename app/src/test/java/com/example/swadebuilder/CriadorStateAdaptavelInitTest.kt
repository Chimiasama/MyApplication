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
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateAdaptavelInitTest {

    @Test
    fun `adaptavel fica disponivel apos carregar dados com humanos sem abrir ancestrais`() {
        val state = CriadorState()

        val humanos = RacialModifier(
            nome = "HUMANOS",
            atributos = emptyMap(),
            pericias = emptyMap(),
            vantagensGratis = listOf("ADAPTAVEL"),
            origem = "BASICO"
        )

        val snapshot = GameDataSnapshot(
            listaComplicacoes = emptyList<Complicacao>(),
            listaCoracoesCrystal = emptyList<CrystalHeart>(),
            listaAncestralidadesJson = listOf(humanos),
            listaMonstroTemplates = emptyList<MonstroTemplate>(),
            racialAttrMinMap = emptyMap(),
            racialSkillStartMap = emptyMap(),
            listaAtributos = emptyList(),
            mapaAtributosDisplay = emptyMap(),
            listaPericias = emptyList<Pericia>(),
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

        state.updateGameData(snapshot)

        assertTrue(state.temAdaptavel())
        assertTrue(state.adaptavelSlotAvailable)
    }
}
