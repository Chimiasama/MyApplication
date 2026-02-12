package com.example.swadebuilder.model

import android.content.Context
import android.test.mock.MockContext
import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.listaPericias
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorViewModelGameDataSnapshotTest {

    @Test
    fun `carregarDadosDeJogo usa snapshot do repositorio para especializacoes`() = runBlocking {
        listaPericias = listOf(Pericia("GLOBAL", "AGILIDADE", basica = true, origem = "BASICO"))

        val periciaSnapshot = Pericia("ATLETISMO", "AGILIDADE", basica = true, origem = "SCI_FI")
        val vm = CriadorViewModel(
            gameDataRepository = FakeGameDataRepository(snapshotBase(pericias = listOf(periciaSnapshot)))
        )
        vm.state.usarEspecializacoesDePericia = true

        vm.carregarDadosDeJogo(MockContext(), setOf("SCI_FI"))
        vm.ensureDefaultSpecializations()

        assertTrue(vm.state.especializacoesPorPericia.containsKey("ATLETISMO"))
        assertFalse(vm.state.especializacoesPorPericia.containsKey("GLOBAL"))
    }

    @Test
    fun `aplicarGameDataSnapshot permite usar snapshot sem contexto android`() {
        listaPericias = emptyList()

        val vm = CriadorViewModel()
        vm.state.usarEspecializacoesDePericia = true
        vm.aplicarGameDataSnapshot(
            snapshotBase(pericias = listOf(Pericia("PESQUISA", "ASTUCIA", basica = true, origem = "BASICO")))
        )

        vm.ensureDefaultSpecializations()

        assertTrue(vm.state.especializacoesPorPericia.containsKey("PESQUISA"))
    }

    private fun snapshotBase(
        pericias: List<Pericia> = emptyList()
    ): GameDataSnapshot {
        return GameDataSnapshot(
            listaComplicacoes = emptyList(),
            listaCoracoesCrystal = emptyList(),
            listaAncestralidadesJson = emptyList(),
            listaMonstroTemplates = emptyList(),
            racialAttrMinMap = emptyMap(),
            racialSkillStartMap = emptyMap(),
            listaAtributos = emptyList(),
            mapaAtributosDisplay = emptyMap(),
            listaPericias = pericias,
            mapaPericias = pericias.associateBy { it.nome },
            mapaAtributosDescricao = emptyMap(),
            listaVantagens = emptyList(),
            listaPoderes = emptyList(),
            listaTropos = emptyList(),
            listaEquipamentos = emptyList(),
            equipamentoCategorias = emptyList(),
            superequipCategorias = emptyList(),
            listaSuperPoderes = emptyList<SuperPoder>(),
            arcanoInfo = emptyList<ArcanoInfo>()
        )
    }

    private class FakeGameDataRepository(
        private val snapshot: GameDataSnapshot
    ) : GameDataRepository {
        override suspend fun load(context: Context, activeModules: Set<String>): GameDataSnapshot = snapshot
    }
}
