package com.example.swadebuilder.model

import android.content.Context
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
import com.example.swadebuilder.racialAttrMinMap
import com.example.swadebuilder.racialSkillStartMap
import com.example.swadebuilder.superequipCategorias
import com.example.swadebuilder.model.usecase.ValidateGameDataSnapshotIntegrityUseCase
import com.example.swadebuilder.util.keyify
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

            val sanitizedSnapshot = sanitizeSnapshotForRuntime(snapshot)

            val integrity = validateGameDataSnapshotIntegrityUseCase.execute(sanitizedSnapshot)
            check(integrity.ok) {
                "Falha de integridade no carregamento de dados: ${integrity.issues.joinToString(" | ")}" 
            }

            // --- Phase 9 Compatibility Bridge ---
            // Explicitly write to global variables here (side-effect), keeping DataLoader pure.
            // This allows us to track legacy usage and eventually remove it.
            listaComplicacoes = sanitizedSnapshot.listaComplicacoes
            listaCoracoesCrystal = sanitizedSnapshot.listaCoracoesCrystal
            listaAncestralidadesJson = sanitizedSnapshot.listaAncestralidadesJson
            listaMonstroTemplates = sanitizedSnapshot.listaMonstroTemplates
            racialAttrMinMap = sanitizedSnapshot.racialAttrMinMap
            racialSkillStartMap = sanitizedSnapshot.racialSkillStartMap
            listaAtributos = sanitizedSnapshot.listaAtributos
            mapaAtributosDisplay = sanitizedSnapshot.mapaAtributosDisplay
            listaPericias = sanitizedSnapshot.listaPericias
            mapaPericias = sanitizedSnapshot.mapaPericias
            mapaAtributosDescricao = sanitizedSnapshot.mapaAtributosDescricao
            listaVantagens = sanitizedSnapshot.listaVantagens
            listaPoderes = sanitizedSnapshot.listaPoderes
            listaTropos = sanitizedSnapshot.listaTropos
            listaEquipamentos = sanitizedSnapshot.listaEquipamentos
            equipamentoCategorias = sanitizedSnapshot.equipamentoCategorias
            superequipCategorias = sanitizedSnapshot.superequipCategorias
            listaSuperPoderes = sanitizedSnapshot.listaSuperPoderes
            // ArcanoInfo is special as it's a Map in globals but List in snapshot,
            // but DataLoader previously wrote to the global `arcanoInfo`.
            // Let's check the type mismatch.
            // In DataLoader (previous): arcanoInfo = arcanoList.associate { ... }
            // In Snapshot: val arcanoInfo: List<ArcanoInfo>
            // In GameDataGlobals: var arcanoInfo by mutableStateOf<Map<String, Triple<Int, Int, String>>>(emptyMap())
            // So we need to transform it back to the map expected by the global.
            arcanoInfo = sanitizedSnapshot.arcanoInfo.associate {
                it.key.uppercase().trim() to Triple(it.slots, it.pp, it.foco)
            }

            sanitizedSnapshot
        }
}

internal fun sanitizeSnapshotForRuntime(snapshot: GameDataSnapshot): GameDataSnapshot {
    fun <T> dedupePreferLast(items: List<T>, keySelector: (T) -> String): List<T> {
        return items
            .asReversed()
            .distinctBy { keySelector(it).trim().lowercase() }
            .asReversed()
    }

    val sanitizedPericias = dedupePreferLast(snapshot.listaPericias) { it.nome.keyify() }
    val sanitizedVantagens = dedupePreferLast(snapshot.listaVantagens) { it.id.keyify() }
    val sanitizedPoderes = dedupePreferLast(snapshot.listaPoderes) { it.id.keyify() }

    return snapshot.copy(
        listaPericias = sanitizedPericias,
        mapaPericias = sanitizedPericias.associateBy { it.nome.keyify() },
        listaVantagens = sanitizedVantagens,
        listaPoderes = sanitizedPoderes
    )
}
