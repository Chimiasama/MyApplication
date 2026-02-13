package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.AppData
import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.equipamentoCategorias
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaCoracoesCrystal
import com.example.swadebuilder.listaEquipamentos
import com.example.swadebuilder.listaMonstroTemplates
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaPoderes
import com.example.swadebuilder.listaSuperPoderes
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDescricao
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.mapaPericias
import com.example.swadebuilder.model.usecase.ValidateGameDataSnapshotIntegrityUseCase
import com.example.swadebuilder.racialAttrMinMap
import com.example.swadebuilder.racialSkillStartMap
import com.example.swadebuilder.superequipCategorias
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Snapshot imutável dos dados de jogo carregados para uma combinação de livros/módulos.
 */
data class GameDataSnapshot(
    val listaComplicacoes: List<Complicacao>,
    val listaCoracoesCrystal: List<CrystalHeart>,
    val listaAncestralidadesJson: List<RacialModifier>,
    val listaMonstroTemplates: List<MonstroTemplate>,
    val racialAttrMinMap: Map<String, Map<String, Int>>,
    val racialSkillStartMap: Map<String, Map<String, Int>>,
    val listaAtributos: List<String>,
    val mapaAtributosDisplay: Map<String, String>,
    val listaPericias: List<Pericia>,
    val mapaPericias: Map<String, Pericia>,
    val mapaAtributosDescricao: Map<String, String>,
    val listaVantagens: List<Vantagem>,
    val listaPoderes: List<Poder>,
    val listaTropos: List<Tropo>,
    val listaEquipamentos: List<EquipamentoItem>,
    val equipamentoCategorias: List<EquipamentoCategoria>,
    val superequipCategorias: List<EquipamentoCategoria>,
    val listaSuperPoderes: List<SuperPoder>,
    val arcanoInfo: List<ArcanoInfo>
)

interface GameDataRepository {
    suspend fun load(context: Context, activeModules: Set<String>): GameDataSnapshot
}

/**
 * Implementação inicial de Fase 1.
 *
 * Mantém compatibilidade com o legado (globais) usando o DataLoader atual,
 * mas passa a expor os dados por um contrato explícito de repositório.
 */
class AssetGameDataRepository : GameDataRepository {
    private val validateGameDataSnapshotIntegrityUseCase = ValidateGameDataSnapshotIntegrityUseCase()

    override suspend fun load(context: Context, activeModules: Set<String>): GameDataSnapshot =
        withContext(Dispatchers.IO) {
            val snapshot = if (activeModules.isEmpty()) {
                DataLoader.loadCore(context)
            } else {
                DataLoader.updateActiveModules(context, activeModules)
            }

            val integrity = validateGameDataSnapshotIntegrityUseCase.execute(snapshot)
            check(integrity.ok) {
                "Falha de integridade no carregamento de dados: ${integrity.issues.joinToString(" | ")}"
            }

            // --- LEGACY COMPATIBILITY BRIDGE ---
            // Update global variables for legacy consumers.
            // This is temporary until all consumers use the Repository/Snapshot directly.
            updateLegacyGlobals(snapshot)
            // -----------------------------------

            snapshot
        }

    private fun updateLegacyGlobals(snapshot: GameDataSnapshot) {
        // AppData Globals (Special handling)
        AppData.basicasVantagens = snapshot.listaVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = snapshot.listaVantagens.filter { it.origem.equals("SUPER", ignoreCase = true) }
        AppData.horrorVantagens = snapshot.listaVantagens.filter { it.origem.equals("HORROR", ignoreCase = true) }
        AppData.pathfinderVantagens = snapshot.listaVantagens.filter { it.origem.equals("PATHFINDER", ignoreCase = true) }
        AppData.superVantagensParaDetalhe = AppData.superVantagens

        // Standard Globals
        listaVantagens = snapshot.listaVantagens
        listaTropos = snapshot.listaTropos
        listaEquipamentos = snapshot.listaEquipamentos
        listaPoderes = snapshot.listaPoderes
        equipamentoCategorias = snapshot.equipamentoCategorias
        superequipCategorias = snapshot.superequipCategorias
        listaSuperPoderes = snapshot.listaSuperPoderes
        listaComplicacoes = snapshot.listaComplicacoes
        listaCoracoesCrystal = snapshot.listaCoracoesCrystal
        listaAncestralidadesJson = snapshot.listaAncestralidadesJson
        listaMonstroTemplates = snapshot.listaMonstroTemplates
        racialAttrMinMap = snapshot.racialAttrMinMap
        racialSkillStartMap = snapshot.racialSkillStartMap
        listaAtributos = snapshot.listaAtributos
        mapaAtributosDisplay = snapshot.mapaAtributosDisplay
        listaPericias = snapshot.listaPericias
        mapaPericias = snapshot.mapaPericias
        mapaAtributosDescricao = snapshot.mapaAtributosDescricao

        // This is a var in MainActivity... tricky.
        // We will need to update the one in GameDataGlobals.kt or whereever it is defined.
        // ArcanoInfo is defined in MainActivity.kt as a global var.
        // We can access it if we import it.
        arcanoInfo = snapshot.arcanoInfo.associate {
            it.key.uppercase().trim() to Triple(it.slots, it.pp, it.foco) // simplified normalization logic for bridge
        }
    }
}
