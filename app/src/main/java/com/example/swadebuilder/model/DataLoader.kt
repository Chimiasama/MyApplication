package com.example.swadebuilder.model

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.util.CustomCrystalHeartStorage
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Loads JSON game data from assets into global variables.
 * Refactored for Lazy Loading.
 */
object DataLoader {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    // Cache for loaded file content (FileName -> Any)
    // Stores List<T> or specific wrapper types (AtributoList, PericiaList)
    private val dataCache = mutableMapOf<String, Any>()

    private data class ModuleFile(val fileName: String, val originOverride: String? = null)

    // --- Module Definitions ---

    private val equipmentModules = listOf(
        ModuleFile("basico_equipamentos.json"),
        ModuleFile("fantasia_equipamentos.json", originOverride = "FANTASIA"),
        ModuleFile("horror_equipamentos.json", originOverride = "HORROR"),
        ModuleFile("scifi_equipamentos.json", originOverride = "SCI_FI"),
        ModuleFile("crystal_equipamentos.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("pathfinder_equipamentos.json", originOverride = "PATHFINDER"),
        ModuleFile("super_equipamentos.json", originOverride = "SUPER"),
        ModuleFile("wiseguys_equipamentos.json", originOverride = "WISEGUYS"),
        ModuleFile("adg_equipamentos.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_equipamentos.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_equipamentos.json", originOverride = "DEADLANDS")
    )

    private val skillModules = listOf(
        ModuleFile("basico_pericias.json"),
        ModuleFile("adg_pericias.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("fantasia_pericias.json", originOverride = "FANTASIA"),
        ModuleFile("horror_pericias.json", originOverride = "HORROR"),
        ModuleFile("wiseguys_pericias.json", originOverride = "WISEGUYS"),
        ModuleFile("scifi_pericias.json", originOverride = "SCI_FI"),
        ModuleFile("deadlands_pericias.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_pericias.json", originOverride = "PATHFINDER"),
        ModuleFile("sol_vapor_pericias.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("crystal_pericias.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("super_pericias.json", originOverride = "SUPER")
    )

    private val advantageModules = listOf(
        ModuleFile("basico_vantagens.json"),
        ModuleFile("fantasia_vantagens.json", originOverride = "FANTASIA"),
        ModuleFile("horror_vantagens.json", originOverride = "HORROR"),
        ModuleFile("scifi_vantagens.json", originOverride = "SCI_FI"),
        ModuleFile("crystal_vantagens.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("super_vantagens.json", originOverride = "SUPER"),
        ModuleFile("wiseguys_vantagens.json", originOverride = "WISEGUYS"),
        ModuleFile("adg_vantagens.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_vantagens.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_vantagens.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_vantagens.json", originOverride = "PATHFINDER")
    )

    private val complicationModules = listOf(
        ModuleFile("basico_complicacoes.json"),
        ModuleFile("fantasia_complicacoes.json", originOverride = "FANTASIA"),
        ModuleFile("horror_complicacoes.json", originOverride = "HORROR"),
        ModuleFile("scifi_complicacoes.json", originOverride = "SCI_FI"),
        ModuleFile("super_complicacoes.json", originOverride = "SUPER"),
        ModuleFile("wiseguys_complicacoes.json", originOverride = "WISEGUYS"),
        ModuleFile("crystal_complicacoes.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("adg_complicacoes.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_complicacoes.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_complicacoes.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_complicacoes.json", originOverride = "PATHFINDER")
    )

    private val ancestryModules = listOf(
        ModuleFile("basico_ancestralidades.json"),
        ModuleFile("fantasia_ancestralidades.json", originOverride = "FANTASIA"),
        ModuleFile("horror_ancestralidades.json", originOverride = "HORROR"),
        ModuleFile("scifi_ancestralidades.json", originOverride = "SCI_FI"),
        ModuleFile("wiseguys_ancestralidades.json", originOverride = "WISEGUYS"),
        ModuleFile("crystal_ancestralidades.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("adg_ancestralidades.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("sol_vapor_ancestralidades.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("deadlands_ancestralidades.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_ancestralidades.json", originOverride = "PATHFINDER"),
        ModuleFile("super_ancestralidades.json", originOverride = "SUPER")
    )

    private val powerModules = listOf(
        ModuleFile("basico_poderes.json"),
        ModuleFile("fantasia_poderes.json", originOverride = "FANTASIA"),
        ModuleFile("scifi_poderes.json", originOverride = "SCI_FI"),
        ModuleFile("horror_poderes.json", originOverride = "HORROR"),
        ModuleFile("deadlands_poderes.json", originOverride = "DEADLANDS"),
        ModuleFile("pathfinder_poderes.json", originOverride = "PATHFINDER"),
        ModuleFile("crystal_poderes.json", originOverride = "CRYSTAL_HEART"),
        ModuleFile("sol_vapor_poderes.json", originOverride = "CIDADE_SOL_VAPOR"),
        ModuleFile("wiseguys_poderes.json", originOverride = "WISEGUYS"),
        ModuleFile("adg_poderes.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("adg_tecnicas_chi.json", originOverride = "ARTE_DA_GUERRA"),
        ModuleFile("super_poderes_base.json", originOverride = "SUPER")
    )

    // --- Loading Logic ---

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.loadAndMerge(
        modules: List<ModuleFile>,
        activeKeys: Set<String>,
        noinline transform: (T, String?) -> T = { item, _ -> item }
    ): List<T> {
        return modules.filter {
            val key = it.originOverride?.uppercase() ?: "BASICO"
            key in activeKeys
        }.flatMap { module ->
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut(module.fileName) {
                try {
                    open(module.fileName).use { input ->
                        json.decodeFromStream<List<T>>(input)
                    }
                } catch (e: Exception) {
                    Log.e(
                        "SWADE_DEBUG",
                        "[DataLoader] falha ao carregar ${module.fileName} (originOverride=${module.originOverride}): ${e::class.simpleName}: ${e.message}",
                        e
                    )
                    emptyList<T>()
                }
            } as List<T>

            cached.map { item ->
                if (module.originOverride != null) transform(item, module.originOverride) else item
            }
        }
    }

    private var loadedArcanoInfoList: List<ArcanoInfo> = emptyList()

    @OptIn(ExperimentalSerializationApi::class)
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun loadCore(context: Context): GameDataSnapshot {
        return updateActiveModules(context, setOf("BASICO"))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun updateActiveModules(context: Context, activeModules: Set<String>): GameDataSnapshot {
        val keys = activeModules + "BASICO" // Always include basic
        val assets = context.assets

        val replacementBookKeys = setOf(
            "FANTASIA",
            "HORROR",
            "SCI_FI",
            "PATHFINDER",
            "DEADLANDS",
            "CRYSTAL_HEART",
            "ARTE_DA_GUERRA",
            "CIDADE_SOL_VAPOR",
            "WISEGUYS"
        )
        val shouldReplaceBasico = keys.any { it in replacementBookKeys }

        // 1. Equipamentos
        val equipmentModulesToLoad = if ("CRYSTAL_HEART" in keys) {
            equipmentModules.filter { it.fileName == "crystal_equipamentos.json" }
        } else if (shouldReplaceBasico) {
            equipmentModules.filter { it.fileName != "basico_equipamentos.json" }
        } else {
            equipmentModules
        }

        val allEquip = assets.loadAndMerge<EquipamentoCategoria>(equipmentModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }
        val localListaEquipamentos = allEquip.flatMap { it.itens }

        val localEquipamentoCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
        )
        val localSuperequipCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }
        )

        // 2. Crystal Hearts
        val localListaCoracoesCrystal = if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val hearts = dataCache.getOrPut("crystal_coracoes.json") {
                runCatching {
                    assets.open("crystal_coracoes.json")
                        .use { input -> json.decodeFromStream<List<CrystalHeart>>(input) }
                }.getOrElse { emptyList<CrystalHeart>() }
            } as List<CrystalHeart>
            val customHearts = CustomCrystalHeartStorage.load(context)
            (hearts + customHearts).distinctBy { it.id }
        } else {
            emptyList()
        }

        // 3. Super Poderes
        val localListaSuperPoderes = if ("SUPER" in keys) {
            @Suppress("UNCHECKED_CAST")
            val supers = dataCache.getOrPut("super_poderes.json") {
                runCatching {
                    assets.open("super_poderes.json")
                        .use { input -> json.decodeFromStream<List<SuperPoder>>(input) }
                }.getOrElse { emptyList<SuperPoder>() }
            } as List<SuperPoder>
            supers
        } else {
            emptyList()
        }

        // 4. Arcano Info (Always load core)
        @Suppress("UNCHECKED_CAST")
        val arcanoList = dataCache.getOrPut("geral_arcano_info.json") {
            runCatching {
                assets.open("geral_arcano_info.json")
                    .use { input -> json.decodeFromStream<List<ArcanoInfo>>(input) }
            }.getOrElse { emptyList<ArcanoInfo>() }
        } as List<ArcanoInfo>

        loadedArcanoInfoList = arcanoList
        // arcanoInfo removed from global write, handled in snapshot

        // 5. Atributos (Always load core)
        val atributosData = dataCache.getOrPut("geral_atributos.json") {
            runCatching {
                loadJsonAsset<AtributoList>(context, "geral_atributos.json")
            }.getOrElse { AtributoList(emptyList()) }
        } as AtributoList

        val localListaAtributos = atributosData.atributos.map { it.nome.keyify() }
        val localMapaAtributosDisplay = atributosData.atributos.associate { it.nome.keyify() to it.nome }

        // 6. Pericias
        val skillModulesToLoad = if (shouldReplaceBasico) {
            skillModules.filter { it.fileName != "basico_pericias.json" }
        } else {
            skillModules
        }

        val todasPericiasJson = skillModulesToLoad.filter {
            val key = it.originOverride?.uppercase() ?: "BASICO"
            key in keys
        }.flatMap { module ->
            val pListWrapper = dataCache.getOrPut(module.fileName) {
                // Try loading as PericiaList (wrapped)
                val asWrapper = runCatching {
                    loadJsonAsset<PericiaList>(context, module.fileName)
                }.getOrNull()

                if (asWrapper != null) {
                    asWrapper
                } else {
                    // Try loading as List<PericiaJson> (direct)
                    val asList = runCatching {
                        loadJsonAsset<List<PericiaJson>>(context, module.fileName)
                    }.getOrNull()

                    if (asList != null) {
                        PericiaList(asList)
                    } else {
                        PericiaList(emptyList())
                    }
                }
            } as PericiaList

            val pList = pListWrapper.pericias
            if (module.originOverride != null) {
                pList.map { it.copy(origem = module.originOverride) }
            } else {
                pList
            }
        }

        val rawPericias = todasPericiasJson.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica,
                origem   = pj.origem,
                descricao = pj.descricao
            )
        }

        val localListaPericias = rawPericias
        val localMapaPericias = localListaPericias.associateBy { it.nome.keyify() }

        val localMapaAtributosDescricao = atributosData.atributos.associate {
            it.nome.keyify() to (it.descricao ?: "")
        }

        // 7. Vantagens
        val advantagesToLoad = if (shouldReplaceBasico) {
            advantageModules.filter { it.fileName != "basico_vantagens.json" }
        } else {
            advantageModules
        }

        val todasVantagens = assets.loadAndMerge<Vantagem>(advantagesToLoad, keys) { item, override ->
             if (override != null) item.copy(origem = override) else item
        }

        val localListaVantagens = buildList {
            addAll(todasVantagens)

            if (shouldReplaceBasico && none { it.id == "antecedente_arcano" }) {
                @Suppress("UNCHECKED_CAST")
                val basicoVantagens = dataCache.getOrPut("basico_vantagens.json") {
                    runCatching {
                        assets.open("basico_vantagens.json")
                            .use { input -> json.decodeFromStream<List<Vantagem>>(input) }
                    }.getOrElse { emptyList<Vantagem>() }
                } as List<Vantagem>

                basicoVantagens
                    .firstOrNull { it.id == "antecedente_arcano" }
                    ?.let { add(it) }
            }
        }

        if ("CIDADE_SOL_VAPOR" in keys) {
            val steamAll = todasVantagens.filter { canonicalOriginKey(it.origem) == "CIDADE_SOL_VAPOR" }
            Log.d(
                "SWADE_DEBUG",
                "[DataLoader] keys=$keys, shouldReplaceBasico=$shouldReplaceBasico, " +
                    "vantagens_total=${todasVantagens.size}, sol_vapor_total=${steamAll.size}"
            )
            steamAll.take(20).forEach { vant ->
                Log.d(
                    "SWADE_DEBUG",
                    "[DataLoader] sol_vapor id=${vant.id}, origem=${vant.origem}, nome=${vant.nomeExibicao}"
                )
            }
        }

        // 8. Tropos e Complicações
        val adgTropos = if ("ARTE_DA_GUERRA" in keys) {
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut("adg_tropos.json") {
                runCatching { loadJsonAsset<List<Tropo>>(context, "adg_tropos.json") }.getOrElse { emptyList<Tropo>() }
            } as List<Tropo>
            cached
        } else emptyList()

        val chTropos = if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val cached = dataCache.getOrPut("crystal_tropos.json") {
                runCatching { loadJsonAsset<List<Tropo>>(context, "crystal_tropos.json") }.getOrElse { emptyList<Tropo>() }
            } as List<Tropo>
            cached
        } else emptyList()

        val localListaTropos = adgTropos + chTropos

        val complicationModulesToLoad = if (shouldReplaceBasico) {
            complicationModules.filter { it.fileName != "basico_complicacoes.json" }
        } else {
            complicationModules
        }

        val localListaComplicacoes = assets.loadAndMerge<Complicacao>(complicationModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        // 9. Ancestralidades
        val ancestriesToLoad = if (shouldReplaceBasico) {
            ancestryModules.filter { it.fileName != "basico_ancestralidades.json" }
        } else {
            ancestryModules
        }
        val localListaAncestralidadesJson = assets.loadAndMerge<RacialModifier>(ancestryModules.filter {
            // Apply filtering logic similar to other modules if needed,
            // or simply reuse the `ancestriesToLoad` calculated above.
            // The original code calculated `ancestriesToLoad` but then passed it to `loadAndMerge`.
            // Here we just use the variable we defined.
            it in ancestriesToLoad
        }, keys)

        // 10. Monstros
        val localListaMonstroTemplates = if ("HORROR" in keys) {
            @Suppress("UNCHECKED_CAST")
            val monstros = dataCache.getOrPut("horror_monstros.json") {
                runCatching {
                    assets.open("horror_monstros.json")
                        .use { input -> json.decodeFromStream<List<MonstroTemplate>>(input) }
                }.getOrElse { emptyList<MonstroTemplate>() }
            } as List<MonstroTemplate>
            monstros
        } else {
            emptyList()
        }

        // 11. Mapas Raciais
        val localRacialAttrMinMap = localListaAncestralidadesJson.associate { rm ->
            val m = rm.atributos
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        val localRacialSkillStartMap = localListaAncestralidadesJson.associate { rm ->
            val m = rm.pericias
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        // 12. Regras de Criação de Raça (Unused mostly but cached)
        // Kept for consistency if needed later

        // 13. Poderes
        val powerModulesToLoad = if (shouldReplaceBasico) {
            powerModules.filter { it.fileName != "basico_poderes.json" }
        } else {
            powerModules
        }
        val todosPoderes = assets.loadAndMerge<Poder>(powerModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        val localListaPoderes = todosPoderes

        return GameDataSnapshot(
            listaComplicacoes = localListaComplicacoes,
            listaCoracoesCrystal = localListaCoracoesCrystal,
            listaAncestralidadesJson = localListaAncestralidadesJson,
            listaMonstroTemplates = localListaMonstroTemplates,
            racialAttrMinMap = localRacialAttrMinMap,
            racialSkillStartMap = localRacialSkillStartMap,
            listaAtributos = localListaAtributos,
            mapaAtributosDisplay = localMapaAtributosDisplay,
            listaPericias = localListaPericias,
            mapaPericias = localMapaPericias,
            mapaAtributosDescricao = localMapaAtributosDescricao,
            listaVantagens = localListaVantagens,
            listaPoderes = localListaPoderes,
            listaTropos = localListaTropos,
            listaEquipamentos = localListaEquipamentos,
            equipamentoCategorias = localEquipamentoCategorias,
            superequipCategorias = localSuperequipCategorias,
            listaSuperPoderes = localListaSuperPoderes,
            arcanoInfo = loadedArcanoInfoList
        )
    }

    private fun deduplicarEquipamentoCategorias(
        categorias: List<EquipamentoCategoria>
    ): List<EquipamentoCategoria> {
        return categorias.map { categoria ->
            val itensDeduplicados = categoria.itens.distinctBy { equipamentoKey(it) }
            if (itensDeduplicados.size == categoria.itens.size) {
                categoria
            } else {
                categoria.copy(itens = itensDeduplicados)
            }
        }
    }

    private fun equipamentoKey(item: EquipamentoItem): String = listOfNotNull(
        item.nome.keyify(),
        item.custo?.toString(),
        item.peso?.toString(),
        item.origem?.keyify(),
        item.subtipo?.keyify(),
        item.subsubtipo?.keyify(),
        item.forcaMin?.toString(),
        item.armadura?.toString(),
        item.aparar?.toString(),
        item.observacoes?.toString(),
        item.dano?.toString(),
        item.pa?.toString(),
        item.cdt?.toString(),
        item.distancia?.toString(),
        item.tiros?.toString(),
        item.tamanho?.toString(),
        item.manobrabilidade?.toString(),
        item.velMaxima?.toString(),
        item.resistencia?.toString(),
        item.tripulacao?.toString(),
        item.pmf?.toString(),
        item.malfuncionamento?.toString(),
        item.tensao?.toString(),
        item.mods_slots?.toString()
    ).joinToString("|")
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> loadJsonAsset(context: Context, fileName: String): T {
    val json = Json { ignoreUnknownKeys = true }
    return context.assets.open(fileName).use { input ->
        json.decodeFromStream(input)
    }
}
