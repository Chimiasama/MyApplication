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
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaCoracoesCrystal
import com.example.swadebuilder.listaEquipamentos
import com.example.swadebuilder.listaMonstroTemplates
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaTropos
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.mapaAtributosDescricao
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.mapaPericias
import com.example.swadebuilder.mapaPericiasDescricao
import com.example.swadebuilder.mapaPericiasDescricaoAdg
import com.example.swadebuilder.racialAttrMinMap
import com.example.swadebuilder.racialSkillStartMap
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Loads all JSON game data from assets into global variables.
 * This refactors the logic previously found in MainActivity.onCreate.
 */
object DataLoader {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private data class ModuleFile(val fileName: String, val originOverride: String? = null)

    // --- Module Definitions ---

    private val equipmentModules = listOf(
        ModuleFile("basico_equipamentos.json"),
        ModuleFile("fantasia_equipamentos.json"),
        ModuleFile("horror_equipamentos.json"),
        ModuleFile("scifi_equipamentos.json"),
        ModuleFile("crystal_equipamentos.json"),
        ModuleFile("pathfinder_equipamentos.json"),
        ModuleFile("super_equipamentos.json"),
        ModuleFile("wiseguys_equipamentos.json"),
        ModuleFile("adg_equipamentos.json"),
        ModuleFile("sol_vapor_equipamentos.json"),
        ModuleFile("deadlands_equipamentos.json")
    )

    private val skillModules = listOf(
        ModuleFile("basico_pericias.json"),
        ModuleFile("adg_pericias.json"),
        ModuleFile("fantasia_pericias.json")
    )

    private val advantageModules = listOf(
        ModuleFile("basico_vantagens.json"),
        ModuleFile("fantasia_vantagens.json", originOverride = "FANTASIA"),
        ModuleFile("horror_vantagens.json"),
        ModuleFile("scifi_vantagens.json"),
        ModuleFile("crystal_vantagens.json"),
        ModuleFile("super_vantagens.json"),
        ModuleFile("wiseguys_vantagens.json"),
        ModuleFile("adg_vantagens.json"),
        ModuleFile("sol_vapor_vantagens.json"),
        ModuleFile("deadlands_vantagens.json"),
        ModuleFile("pathfinder_vantagens.json")
    )

    private val complicationModules = listOf(
        ModuleFile("basico_complicacoes.json"),
        ModuleFile("fantasia_complicacoes.json"),
        ModuleFile("horror_complicacoes.json"),
        ModuleFile("scifi_complicacoes.json", originOverride = "SCI_FI"),
        ModuleFile("super_complicacoes.json"),
        ModuleFile("wiseguys_complicacoes.json"),
        ModuleFile("crystal_complicacoes.json"),
        ModuleFile("adg_complicacoes.json"),
        ModuleFile("sol_vapor_complicacoes.json"),
        ModuleFile("deadlands_complicacoes.json"),
        ModuleFile("pathfinder_complicacoes.json")
    )

    private val ancestryModules = listOf(
        ModuleFile("basico_ancestralidades.json"),
        ModuleFile("fantasia_ancestralidades.json"),
        ModuleFile("horror_ancestralidades.json"),
        ModuleFile("scifi_ancestralidades.json"),
        ModuleFile("super_ancestralidades.json"),
        ModuleFile("wiseguys_ancestralidades.json"),
        ModuleFile("crystal_ancestralidades.json"),
        ModuleFile("adg_ancestralidades.json"),
        ModuleFile("sol_vapor_ancestralidades.json"),
        ModuleFile("deadlands_ancestralidades.json"),
        ModuleFile("pathfinder_ancestralidades.json")
    )

    // --- Loading Logic ---

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.readJsonList(fileName: String): List<T> =
        open(fileName).use { input -> json.decodeFromStream(input) }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.loadAndMerge(
        modules: List<ModuleFile>,
        crossinline transform: (T, String?) -> T = { item, _ -> item }
    ): List<T> {
        return modules.flatMap { module ->
            runCatching {
                readJsonList<T>(module.fileName).map { item ->
                    if (module.originOverride != null) {
                        transform(item, module.originOverride)
                    } else {
                        item
                    }
                }
            }.getOrElse { emptyList() }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun load(context: Context): MainActivityData {
        val assets = context.assets

        // 1. Equipamentos
        val allEquip = assets.loadAndMerge<EquipamentoCategoria>(equipmentModules)
        listaEquipamentos = allEquip.flatMap { it.itens }

        val equipamentoCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
        )
        // Includes all categories with origin SUPER (from any file)
        val superequipCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }
        )

        // 2. Crystal Hearts
        listaCoracoesCrystal = runCatching {
            assets.open("crystal_coracoes.json")
                .use { input -> json.decodeFromStream<List<CrystalHeart>>(input) }
        }.getOrElse { emptyList() }

        // 3. Super Poderes
        val listaSuperPoderes: List<SuperPoder> =
            assets.open("superpoderes.json")
                .use { input -> json.decodeFromStream<List<SuperPoder>>(input) }

        // 4. Arcano Info
        val arcanoList: List<ArcanoInfo> =
            assets.open("arcano_info.json")
                .use { input -> json.decodeFromStream<List<ArcanoInfo>>(input) }
        arcanoInfo = arcanoList.associate {
            it.key
                .uppercase()
                .semAcentos()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }

        // 5. Atributos
        val atributosData = loadJsonAsset<AtributoList>(context, "atributos.json")
        listaAtributos = atributosData.atributos
            .map { it.nome.keyify() }
        mapaAtributosDisplay = atributosData.atributos
            .associate { it.nome.keyify() to it.nome }

        // 6. Pericias
        // Special handling for nested "pericias" object in PericiaList wrapper
        val todasPericiasJson = skillModules.flatMap { module ->
            runCatching {
                loadJsonAsset<PericiaList>(context, module.fileName).pericias
            }.getOrElse { emptyList() }
        }

        listaPericias = todasPericiasJson.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica,
                origem   = pj.origem,
                descricao = pj.descricao
            )
        }
        mapaPericias = listaPericias.associateBy { it.nome.keyify() }

        // Carrega descrições de perícias
        val periciasDescList = runCatching {
            assets.open("pericias_desc.json").use { input ->
                json.decodeFromStream<List<PericiaDescricaoJson>>(input)
            }
        }.getOrElse { emptyList() }

        val periciasDescAdgList = runCatching {
            assets.open("adg_pericias_desc.json").use { input ->
                json.decodeFromStream<List<PericiaDescricaoJson>>(input)
            }
        }.getOrElse { emptyList() }

        mapaPericiasDescricao = periciasDescList.associate { it.nome.keyify() to it.descricao }
        mapaPericiasDescricaoAdg = periciasDescAdgList.associate { it.nome.keyify() to it.descricao }

        listaPericias = listaPericias.map { pericia ->
            val desc = pericia.descricao ?: mapaPericiasDescricao[pericia.nome.keyify()]
            pericia.copy(descricao = desc)
        }

        // Carrega descrições de atributos
        mapaAtributosDescricao = atributosData.atributos.associate {
            it.nome.keyify() to (it.descricao ?: "")
        }

        // 7. Vantagens
        val todasVantagens = assets.loadAndMerge<Vantagem>(advantageModules) { item, override ->
             if (override != null) item.copy(origem = override) else item
        }

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter { it.origem.equals("SUPER", ignoreCase = true) }
        AppData.horrorVantagens = todasVantagens.filter { it.origem.equals("HORROR", ignoreCase = true) }
        AppData.pathfinderVantagens = todasVantagens.filter { it.origem.equals("PATHFINDER", ignoreCase = true) }

        listaVantagens = todasVantagens
        AppData.superVantagensParaDetalhe = AppData.superVantagens

        // 8. Tropos e Complicações
        val adgTropos = runCatching {
            loadJsonAsset<List<Tropo>>(context, "adg_tropos.json")
        }.getOrElse { emptyList() }
        val chTropos = runCatching {
            loadJsonAsset<List<Tropo>>(context, "crystal_tropos.json")
        }.getOrElse { emptyList() }

        listaTropos = adgTropos + chTropos

        listaComplicacoes = assets.loadAndMerge<Complicacao>(complicationModules) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        // 9. Ancestralidades
        listaAncestralidadesJson = assets.loadAndMerge<RacialModifier>(ancestryModules)

        // 10. Monstros
        listaMonstroTemplates = assets
            .open("monstros.json")
            .use { input -> json.decodeFromStream<List<MonstroTemplate>>(input) }

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

        // 12. Regras de Criação de Raça
        val regrasCriacaoRaca = runCatching {
            loadJsonAsset<RegrasCriacaoRacaJson>(context, "basico_habilidades_criacao.json").tabela_criacao
        }.getOrNull()

        return MainActivityData(equipamentoCategorias, superequipCategorias, listaSuperPoderes, regrasCriacaoRaca)
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

// Helper data classes for loading context
data class MainActivityData(
    val equipamentoCategorias: List<EquipamentoCategoria>,
    val superequipCategorias: List<EquipamentoCategoria>,
    val listaSuperPoderes: List<SuperPoder>,
    val regrasCriacaoRaca: TabelaCriacaoRaca? = null
)

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> loadJsonAsset(context: Context, fileName: String): T {
    val json = Json { ignoreUnknownKeys = true }
    return context.assets.open(fileName).use { input ->
        json.decodeFromStream(input)
    }
}
