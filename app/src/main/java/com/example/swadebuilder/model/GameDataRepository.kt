package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.model.usecase.ValidateGameDataSnapshotIntegrityUseCase
import com.example.swadebuilder.util.keyify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

internal class ModuleSnapshotCache(private val maxSize: Int = 3) {
    private val cache = LinkedHashMap<String, GameDataSnapshot>(maxSize, 0.75f, true)

    fun get(key: String): GameDataSnapshot? = cache[key]

    fun put(key: String, snapshot: GameDataSnapshot) {
        cache[key] = snapshot
        if (cache.size > maxSize) {
            val oldestKey = cache.entries.iterator().next().key
            cache.remove(oldestKey)
        }
    }

    fun clear() {
        cache.clear()
    }
}

/**
 * Implementação inicial de Fase 1.
 *
 * Mantém compatibilidade com o legado (globais) usando o DataLoader atual,
 * mas passa a expor os dados por um contrato explícito de repositório.
 */
class AssetGameDataRepository : GameDataRepository {
    private val validateGameDataSnapshotIntegrityUseCase = ValidateGameDataSnapshotIntegrityUseCase()
    private val cache = ModuleSnapshotCache(maxSize = 4)
    private val cacheMutex = Mutex()

    override suspend fun load(context: Context, activeModules: Set<String>): GameDataSnapshot =
        withContext(Dispatchers.IO) {
            val cacheKey = activeModules
                .map { it.trim().uppercase() }
                .sorted()
                .joinToString("|")

            cacheMutex.withLock {
                cache.get(cacheKey)
            }?.let { return@withContext it }

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

            cacheMutex.withLock {
                cache.put(cacheKey, sanitizedSnapshot)
            }

            sanitizedSnapshot
        }
}

internal fun sanitizeSnapshotForRuntime(snapshot: GameDataSnapshot): GameDataSnapshot {
    fun <T> dedupePreferLast(items: List<T>, keySelector: (T) -> String): List<T> {
        if (items.isEmpty()) return emptyList()

        val lastIndexByKey = HashMap<String, Int>(items.size)
        val normalizedKeys = ArrayList<String>(items.size)

        items.forEachIndexed { index, item ->
            val normalizedKey = keySelector(item).trim().lowercase()
            normalizedKeys.add(normalizedKey)
            lastIndexByKey[normalizedKey] = index
        }

        return items.filterIndexed { index, _ ->
            lastIndexByKey[normalizedKeys[index]] == index
        }
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
