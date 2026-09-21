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
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Descendente Elemental (Fantasia) migrado pro sistema de Seleção
 * (AncestryVariantRegistry.descendenteElemental(), mesmo padrão de Herança/
 * Signo). Achado real corrigido nesta rodada: o bloco que resolvia
 * `descendenteElementalSelecionado` em CriadorState ficava DEPOIS de um
 * early-return que sempre disparava pra esta raça (ela não tem `opcoes`),
 * então escolher um elemento nunca tinha efeito nenhum em produção — estes
 * testes fixam que agora tem.
 */
class CriadorStateDescendenteElementalTest {

    private fun snapshotComRaca(): GameDataSnapshot {
        val raca = RacialModifier(
            nome = "DESCENDENTE ELEMENTAL",
            habilidades = listOf(
                RacialAbility(nome = "Resistência Ambiental", descricao = "", id = "RESISTENCIA_AMBIENTAL"),
                RacialAbility(nome = "Forasteiro", descricao = "", id = "FORASTEIRO", category = "racial_hindrance", severity = "Menor"),
                RacialAbility(nome = "Elemento Ancestral", descricao = "", id = "ELEMENTO_ANCESTRAL")
            ),
            origem = "FANTASIA"
        )
        return GameDataSnapshot(
            listaComplicacoes = emptyList<Complicacao>(),
            listaCoracoesCrystal = emptyList<CrystalHeart>(),
            listaAncestralidadesJson = listOf(raca),
            listaMonstroTemplates = emptyList<MonstroTemplate>(),
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
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
    }

    private fun estadoComElemento(elemento: String?): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshotComRaca())
        state.compendioFantasiaAtivo = true
        state.ancestralidade = "DESCENDENTE ELEMENTAL"
        state.selecionarDescendenteElemental(elemento)
        return state
    }

    @Test
    fun `elemento Agua concede Aquatico`() {
        val state = estadoComElemento("Água")
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "AQUATICO" })
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().none { it.id == "ELEMENTO_ANCESTRAL" })
    }

    @Test
    fun `elemento Terra concede Solido como Rocha e sobe Vigor`() {
        val state = estadoComElemento("Terra")
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "SOLIDO_COMO_ROCHA" })
        assertEquals(6, state.atributoMinRaw("Vigor"))
    }

    @Test
    fun `elemento Fogo concede a Vantagem Rapido de verdade`() {
        val state = estadoComElemento("Fogo")
        val habilidades = state.currentAncestryDef?.habilidades.orEmpty()
        assertTrue(habilidades.any { it.traitId == "GRANTED_EDGE" && it.targetRef == "rapido" })
        assertTrue(state.currentAncestryDef?.resolvedVantagensGratis().orEmpty().contains("rapido"))
    }

    @Test
    fun `Resistencia Ambiental e Forasteiro permanecem independente do elemento`() {
        val state = estadoComElemento("Fogo")
        val habilidades = state.currentAncestryDef?.habilidades.orEmpty()
        assertTrue(habilidades.any { it.id == "RESISTENCIA_AMBIENTAL" })
        assertTrue(habilidades.any { it.id == "FORASTEIRO" })
        assertTrue(habilidades.any { it.id == "RAPIDO" })
    }

    @Test
    fun `trocar de Terra para Ar nao deixa Solido como Rocha vazando`() {
        val state = estadoComElemento("Terra")
        assertEquals(6, state.atributoMinRaw("Vigor"))

        state.selecionarDescendenteElemental("Ar")

        assertEquals(4, state.atributoMinRaw("Vigor"))
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "AR_INTERNO" })
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().none { it.id == "SOLIDO_COMO_ROCHA" })
    }
}
