package com.example.swadebuilder.util

import com.example.swadebuilder.model.PersonagemSnapshot
import com.example.swadebuilder.model.SnapshotAtributos
import com.example.swadebuilder.model.SnapshotFlags
import com.example.swadebuilder.model.SnapshotPericias
import com.example.swadebuilder.model.SnapshotProgresso
import com.example.swadebuilder.model.SnapshotRecursos
import com.example.swadebuilder.model.SnapshotSelecoes
import com.example.swadebuilder.model.SnapshotSupers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRevisionManagerTest {

    private val manager = CharacterRevisionManager(maxRevisions = 3)

    private fun dummySnapshot(name: String): PersonagemSnapshot {
        return PersonagemSnapshot(
            id = "char-123",
            nome = name,
            timestamp = 1000L,
            appTheme = "Standard",
            anotacoes = "",
            flags = SnapshotFlags(
                cartaSelvagem = true,
                maisPontosPericias = false,
                modoSupers = false,
                compendioFantasiaAtivo = false,
                compendioHorrorAtivo = false,
                modoMonstroAtivo = false,
                tipoMonstroSelecionado = null,
                usarEspecializacoesDePericia = false,
                grandesResponsabilidades = false,
                nasceUmHeroi = false,
                soldadoCargaAtivo = false,
                permiteMultiAntecedenteArcano = false,
                meioElfoAgil = false,
                celestialAAMilagresDesabilitado = false,
                jovemAutoPequeno = false,
                jovemMalusPa = 0,
                jovemMalusSp = 0,
                idosoBonusSp = 0,
                obesoBonusSize = 0,
                obesoMalusMov = 0,
                bonusPoderExtra = 0
            ),
            recursos = SnapshotRecursos(
                dinheiro = 500,
                pontosVantagem = 0,
                pontosAtributo = 0,
                pontosComplicacaoGastos = 0,
                paFromProgress = 0,
                spFromProgress = 0,
                legendaryAttrReservations = 0,
                cpPaStack = emptyList(),
                cpSpStack = emptyList(),
                cpPvStack = emptyList(),
                cpRecursosStack = emptyList()
            ),
            atributos = SnapshotAtributos(
                ancestralidade = "HUMANO",
                valoresAtributos = emptyMap(),
                paCostStackPorAtributo = emptyMap()
            ),
            pericias = SnapshotPericias(
                baseIncsPorPericia = emptyMap(),
                compIncsPorPericia = emptyMap(),
                spCostStackPorPericia = emptyMap(),
                compCostStackPorPericia = emptyMap(),
                especializacoesPorPericia = emptyMap()
            ),
            selecoes = SnapshotSelecoes(
                vantagens = emptyList(),
                vantagensAutomaticas = emptyList(),
                vantagensRaciais = emptyList(),
                desvantagensAutomaticas = emptyList(),
                desvantagensRaciais = emptyList(),
                complicacoesSelecionadas = emptyList(),
                reservasComplicacaoMaior = emptyMap(),
                poderesSelecionados = emptyList(),
                poderSlotsPorArcano = emptyMap(),
                novosPoderesStacksPorArcano = emptyMap(),
                arcanoEmCompraViaXpKey = null,
                arcanoSnapshotAntesDaCompra = null
            ),
            progresso = SnapshotProgresso(
                progresso = 1,
                progressosDisponiveis = 0,
                stageXpSpent = emptyMap(),
                xpSlots = emptyList(),
                advancementHistory = emptyList(),
                frozenSkillIncrements = emptyMap(),
                skillAdvancementInProgress = false,
                skillsForCurrentAdvancement = emptyList(),
                advantageAdvancementInProgress = false,
                advantageForCurrentAdvancement = null,
                attributeAdvancementInProgress = false,
                attributeStageForCurrentAdvancement = null,
                stageNameForCurrentAdvancement = null,
                attributeStacksBeforeAdvancement = null,
                attributeUsedReservation = false,
                overrideStageForVantagem = null,
                emProgresso = false,
                modoProgressaoAtivo = true,
                mostrandoVantagensProgresso = false,
                mostrandoPericiasProgresso = false,
                mostrandoAtributosProgresso = false,
                mostrandoPoderesProgresso = false,
                frozenAdvantageCount = 0,
                stageNameForCurrentAdvancementSnapshot = null
            ),
            supers = SnapshotSupers(
                superInvestments = emptyList(),
                superNivelCampanha = null,
                usarSemPontosDePoder = false,
                superPontosTotais = 0,
                superPontosDisponiveis = 0,
                superLimite = 0,
                superLimitePorPoder = 0,
                poderFavoritoId = null,
                limiteDePoderDaCampanha = 0,
                bonusApararFromPower = 0,
                bonusResFromPower = 0,
                armorFromPower = 0,
                bonusMovimentacaoFromPower = 0,
                vantagensDePoder = emptyList(),
                gastosPorPoder = emptyMap(),
                faseSupersAtiva = false,
                comprasPpPorEstagio = emptyMap(),
                comprasAttrPorEstagio = emptyMap(),
                superPontosDisponiveisFlag = false
            )
        )
    }

    @Test
    fun testCreateAndAddRevision() {
        var history = CharacterRevisionHistory(characterId = "char-123")
        assertFalse(manager.canUndo(history))

        val rev1 = manager.createRevision(
            snapshot = dummySnapshot("Hero v1"),
            reason = "Aumento de Perícia: Lutar",
            stageName = "Novato 1"
        )
        history = manager.addRevision(history, rev1)

        assertTrue(manager.canUndo(history))
        assertEquals(1, history.revisions.size)
        assertEquals("Novato 1", history.revisions.last().stageName)
        assertEquals("Aumento de Perícia: Lutar", history.revisions.last().reason)
    }

    @Test
    fun testMaxRevisionsCap() {
        var history = CharacterRevisionHistory(characterId = "char-123")
        for (i in 1..5) {
            val rev = manager.createRevision(
                snapshot = dummySnapshot("Hero v$i"),
                reason = "Aumento $i",
                stageName = "Estágio $i"
            )
            history = manager.addRevision(history, rev)
        }

        assertEquals(3, history.revisions.size)
        assertEquals("Estágio 3", history.revisions.first().stageName)
        assertEquals("Estágio 5", history.revisions.last().stageName)
    }

    @Test
    fun testPopLastRevision() {
        var history = CharacterRevisionHistory(characterId = "char-123")
        val rev1 = manager.createRevision(dummySnapshot("Hero v1"), "Aumento 1", "Novato 1")
        val rev2 = manager.createRevision(dummySnapshot("Hero v2"), "Aumento 2", "Novato 2")

        history = manager.addRevision(history, rev1)
        history = manager.addRevision(history, rev2)

        val (historyAfterPop1, popped1) = manager.popLastRevision(history)
        assertNotNull(popped1)
        assertEquals("Novato 2", popped1?.stageName)
        assertEquals(1, historyAfterPop1.revisions.size)

        val (historyAfterPop2, popped2) = manager.popLastRevision(historyAfterPop1)
        assertNotNull(popped2)
        assertEquals("Novato 1", popped2?.stageName)
        assertEquals(0, historyAfterPop2.revisions.size)

        val (emptyHistory, poppedNull) = manager.popLastRevision(historyAfterPop2)
        assertNull(poppedNull)
        assertFalse(manager.canUndo(emptyHistory))
    }
}
