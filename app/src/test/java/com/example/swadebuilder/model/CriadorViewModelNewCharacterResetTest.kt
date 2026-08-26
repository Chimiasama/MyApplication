package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `CriadorState` é uma única instância reaproveitada entre personagens (não há
 * um estado novo por personagem) — então "Criar Novo Personagem" precisa
 * zerar manualmente todo campo que não seja recomputado do zero. Uma
 * auditoria encontrou vários campos que ficavam "grudados" no valor do
 * personagem anterior porque só eram limpos por métodos específicos (ex.:
 * `removeYoung()`, `selecionarPacoteCulturalFantasia()`) que o reset nunca
 * chamava. Este teste finge um personagem "sujo" com esses campos preenchidos
 * e confere que `resetToEmptyState()` (usado por "Novo Personagem"/"Limpar
 * Ficha") os devolve ao padrão.
 */
class CriadorViewModelNewCharacterResetTest {

    private fun snapshotComHumanos(): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList(),
        listaCoracoesCrystal = emptyList(),
        listaAncestralidadesJson = listOf(
            RacialModifier(nome = "HUMANOS", atributos = emptyMap(), pericias = emptyMap(), origem = "BASICO")
        ),
        listaMonstroTemplates = emptyList(),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList(),
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

    @Test
    fun `resetToEmptyState nao deixa vazar campos do personagem anterior`() {
        val vm = CriadorViewModel()
        vm.aplicarGameDataSnapshot(snapshotComHumanos())

        // "Suja" o estado como se um personagem anterior tivesse usado essas
        // opções — nenhuma delas é reescrita pela transição de ancestralidade
        // em si, só pelo reset explícito (ou por handlers que o reset ignora).
        val state = vm.state
        state.povoDoMarOpcao = "Penalidade em Cavalgar"
        state.senhoresCavalosExtra = true
        state.senhoresCavalosCompensacao = "Código de Honra"
        state.protagonistaRollTecnicas = 4
        state.protagonistaRollPericia = 8
        state.protagonistaRollVantagem = 6
        state.protagonistaRollQualidade = 2
        state.protagonistaRollHabilidade = 10
        state.protagonistaBonusPv = true
        state.vantagensAutomaticasDoProtagonista.add("ALGO")
        state.vantagensSlotProtagonista.add("ALGO")
        state.requisicao = 999
        state.modoOficialAtivo = true
        state.permiteMultiAntecedenteArcano = true
        state.poderesSelecionados.add("poder_fantasma")
        state.equipSectionFilters[EquipSuperType.ARMAS] = setOf("filtro")
        state.superPoderEmFoco = "algum_poder"
        state.ancestralidadeEmFoco = "ALGUMA_RACA"
        state.applyYoungMajor(
            Complicacao(
                id = "pequeno",
                name = "Pequeno",
                severity = "menor",
                description = "teste",
                origem = "BASICO"
            )
        )
        state.obesoBonusSize = 1
        state.obesoMalusMov = -1
        state.idosoBonusSp = 5
        state.bonusPoderExtra = 3

        vm.resetToEmptyState()

        assertNull(state.povoDoMarOpcao)
        assertFalse(state.senhoresCavalosExtra)
        assertNull(state.senhoresCavalosCompensacao)
        assertNull(state.protagonistaRollTecnicas)
        assertNull(state.protagonistaRollPericia)
        assertNull(state.protagonistaRollVantagem)
        assertNull(state.protagonistaRollQualidade)
        assertNull(state.protagonistaRollHabilidade)
        assertFalse(state.protagonistaBonusPv)
        assertTrue(state.vantagensAutomaticasDoProtagonista.isEmpty())
        assertTrue(state.vantagensSlotProtagonista.isEmpty())
        assertEquals(1, state.requisicao)
        assertFalse(state.modoOficialAtivo)
        assertFalse(state.permiteMultiAntecedenteArcano)
        assertTrue(state.poderesSelecionados.isEmpty())
        assertTrue(state.equipSectionFilters.isEmpty())
        assertNull(state.superPoderEmFoco)
        assertNull(state.ancestralidadeEmFoco)
        assertFalse(state.jovemAutoPequeno)
        assertEquals(0, state.obesoBonusSize)
        assertEquals(0, state.obesoMalusMov)
        assertEquals(0, state.idosoBonusSp)
        assertEquals(0, state.bonusPoderExtra)
    }
}
