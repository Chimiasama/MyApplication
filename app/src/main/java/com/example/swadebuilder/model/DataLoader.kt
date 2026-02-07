package com.example.swadebuilder.model

import android.content.Context
import android.content.res.AssetManager
import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.swadebuilder.mapaPericiasDescricao
import com.example.swadebuilder.racialAttrMinMap
import com.example.swadebuilder.racialSkillStartMap
import com.example.swadebuilder.superequipCategorias
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
                    emptyList<T>()
                }
            } as List<T>

            cached.map { item ->
                if (module.originOverride != null) transform(item, module.originOverride) else item
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun loadCore(context: Context) {
        updateActiveModules(context, setOf("BASICO"))
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun updateActiveModules(context: Context, activeModules: Set<String>) {
        val keys = activeModules + "BASICO" // Always include basic
        val assets = context.assets

        // 1. Equipamentos
        val equipmentModulesToLoad = if ("CRYSTAL_HEART" in keys) {
            equipmentModules.filter { it.fileName == "crystal_equipamentos.json" }
        } else {
            equipmentModules
        }

        val allEquip = assets.loadAndMerge<EquipamentoCategoria>(equipmentModulesToLoad, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }
        listaEquipamentos = allEquip.flatMap { it.itens }

        equipamentoCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
        )
        superequipCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }
        )

        // 2. Crystal Hearts
        if ("CRYSTAL_HEART" in keys) {
            @Suppress("UNCHECKED_CAST")
            val hearts = dataCache.getOrPut("crystal_coracoes.json") {
                runCatching {
                    assets.open("crystal_coracoes.json")
                        .use { input -> json.decodeFromStream<List<CrystalHeart>>(input) }
                }.getOrElse { emptyList<CrystalHeart>() }
            } as List<CrystalHeart>
            listaCoracoesCrystal = hearts
        } else {
            listaCoracoesCrystal = emptyList()
        }

        // 3. Super Poderes
        if ("SUPER" in keys) {
            @Suppress("UNCHECKED_CAST")
            val supers = dataCache.getOrPut("super_poderes.json") {
                runCatching {
                    assets.open("super_poderes.json")
                        .use { input -> json.decodeFromStream<List<SuperPoder>>(input) }
                }.getOrElse { emptyList<SuperPoder>() }
            } as List<SuperPoder>
            listaSuperPoderes = supers
        } else {
            listaSuperPoderes = emptyList()
        }

        // 4. Arcano Info (Always load core)
        @Suppress("UNCHECKED_CAST")
        val arcanoList = dataCache.getOrPut("geral_arcano_info.json") {
            runCatching {
                assets.open("geral_arcano_info.json")
                    .use { input -> json.decodeFromStream<List<ArcanoInfo>>(input) }
            }.getOrElse { emptyList<ArcanoInfo>() }
        } as List<ArcanoInfo>

        arcanoInfo = arcanoList.associate {
            it.key
                .uppercase()
                .semAcentos()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }

        // 5. Atributos (Always load core)
        val atributosData = dataCache.getOrPut("geral_atributos.json") {
            runCatching {
                loadJsonAsset<AtributoList>(context, "geral_atributos.json")
            }.getOrElse { AtributoList(emptyList()) }
        } as AtributoList

        listaAtributos = atributosData.atributos.map { it.nome.keyify() }
        mapaAtributosDisplay = atributosData.atributos.associate { it.nome.keyify() to it.nome }

        // 6. Pericias
        val skillModulesToLoad = if ("CRYSTAL_HEART" in keys) {
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

        // Descrições
        @Suppress("UNCHECKED_CAST")
        val periciasDescList = dataCache.getOrPut("pericias_desc.json") {
            runCatching {
                assets.open("pericias_desc.json").use { input ->
                    json.decodeFromStream<List<PericiaDescricaoJson>>(input)
                }
            }.getOrElse { emptyList<PericiaDescricaoJson>() }
        } as List<PericiaDescricaoJson>

        mapaPericiasDescricao = periciasDescList.associate { it.nome.keyify() to it.descricao }

        listaPericias = rawPericias.map { pericia ->
            val desc = pericia.descricao ?: mapaPericiasDescricao[pericia.nome.keyify()]
            pericia.copy(descricao = desc)
        }
        mapaPericias = listaPericias.associateBy { it.nome.keyify() }

        mapaAtributosDescricao = atributosData.atributos.associate {
            it.nome.keyify() to (it.descricao ?: "")
        }

        // 7. Vantagens
        val todasVantagens = assets.loadAndMerge<Vantagem>(advantageModules, keys) { item, override ->
             if (override != null) item.copy(origem = override) else item
        }

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter { it.origem.equals("SUPER", ignoreCase = true) }
        AppData.horrorVantagens = todasVantagens.filter { it.origem.equals("HORROR", ignoreCase = true) }
        AppData.pathfinderVantagens = todasVantagens.filter { it.origem.equals("PATHFINDER", ignoreCase = true) }

        listaVantagens = todasVantagens
        AppData.superVantagensParaDetalhe = AppData.superVantagens

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

        listaTropos = adgTropos + chTropos

        listaComplicacoes = assets.loadAndMerge<Complicacao>(complicationModules, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        // 9. Ancestralidades
        val ancestriesToLoad = if ("DEADLANDS" in keys || "PATHFINDER" in keys || "CRYSTAL_HEART" in keys) {
            ancestryModules.filter { it.fileName != "basico_ancestralidades.json" }
        } else {
            ancestryModules
        }
        listaAncestralidadesJson = assets.loadAndMerge<RacialModifier>(ancestriesToLoad, keys)

        // 10. Monstros
        if ("HORROR" in keys) {
            @Suppress("UNCHECKED_CAST")
            val monstros = dataCache.getOrPut("horror_monstros.json") {
                runCatching {
                    assets.open("horror_monstros.json")
                        .use { input -> json.decodeFromStream<List<MonstroTemplate>>(input) }
                }.getOrElse { emptyList<MonstroTemplate>() }
            } as List<MonstroTemplate>
            listaMonstroTemplates = monstros
        } else {
            listaMonstroTemplates = emptyList()
        }

        // 11. Mapas Raciais
        racialAttrMinMap = listaAncestralidadesJson.associate { rm ->
            val m = rm.atributos
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        racialSkillStartMap = listaAncestralidadesJson.associate { rm ->
            val m = rm.pericias
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        // 12. Regras de Criação de Raça (Unused mostly but cached)
        // Kept for consistency if needed later

        // 13. Poderes
        val todosPoderes = assets.loadAndMerge<Poder>(powerModules, keys) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }
        listaPoderes = todosPoderes
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
