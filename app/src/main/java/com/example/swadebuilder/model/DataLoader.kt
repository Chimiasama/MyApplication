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
import com.example.swadebuilder.listaPoderes
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

    data class CompendioFlags(
        val compendioFantasiaAtivo: Boolean = false,
        val compendioHorrorAtivo: Boolean = false,
        val compendioSciFiAtivo: Boolean = false,
        val compendioPathfinderAtivo: Boolean = false,
        val compendioDeadlandsAtivo: Boolean = false,
        val compendioCrystalHeartAtivo: Boolean = false,
        val compendioArteDaGuerraAtivo: Boolean = false,
        val compendioCidadeSolVaporAtivo: Boolean = false,
        val compendioWiseguysAtivo: Boolean = false,
        val modoSupers: Boolean = false,
        val modoMonstroAtivo: Boolean = false
    )

    private data class ModuleFile(
        val fileName: String,
        val originOverride: String? = null,
        val originKey: String? = null
    )

    private const val ORIGIN_FANTASIA = "FANTASIA"
    private const val ORIGIN_HORROR = "HORROR"
    private const val ORIGIN_SCI_FI = "SCI_FI"
    private const val ORIGIN_PATHFINDER = "PATHFINDER"
    private const val ORIGIN_DEADLANDS = "DEADLANDS"
    private const val ORIGIN_CRYSTAL_HEART = "CRYSTAL_HEART"
    private const val ORIGIN_ARTE_DA_GUERRA = "ARTE_DA_GUERRA"
    private const val ORIGIN_CIDADE_SOL_VAPOR = "CIDADE_SOL_VAPOR"
    private const val ORIGIN_WISEGUYS = "WISEGUYS"
    private const val ORIGIN_SUPER = "SUPER"

    // --- Module Definitions ---

    private val equipmentModules = listOf(
        ModuleFile("basico_equipamentos.json"),
        ModuleFile("fantasia_equipamentos.json", originOverride = ORIGIN_FANTASIA, originKey = ORIGIN_FANTASIA),
        ModuleFile("horror_equipamentos.json", originOverride = ORIGIN_HORROR, originKey = ORIGIN_HORROR),
        ModuleFile("scifi_equipamentos.json", originOverride = ORIGIN_SCI_FI, originKey = ORIGIN_SCI_FI),
        ModuleFile("crystal_equipamentos.json", originOverride = ORIGIN_CRYSTAL_HEART, originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("pathfinder_equipamentos.json", originOverride = ORIGIN_PATHFINDER, originKey = ORIGIN_PATHFINDER),
        ModuleFile("super_equipamentos.json", originOverride = ORIGIN_SUPER, originKey = ORIGIN_SUPER),
        ModuleFile("wiseguys_equipamentos.json", originOverride = ORIGIN_WISEGUYS, originKey = ORIGIN_WISEGUYS),
        ModuleFile("adg_equipamentos.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("sol_vapor_equipamentos.json", originOverride = ORIGIN_CIDADE_SOL_VAPOR, originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("deadlands_equipamentos.json", originOverride = ORIGIN_DEADLANDS, originKey = ORIGIN_DEADLANDS)
    )

    private val skillModules = listOf(
        ModuleFile("basico_pericias.json"),
        ModuleFile("adg_pericias.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("fantasia_pericias.json", originOverride = ORIGIN_FANTASIA, originKey = ORIGIN_FANTASIA),
        ModuleFile("horror_pericias.json", originOverride = ORIGIN_HORROR, originKey = ORIGIN_HORROR),
        ModuleFile("wiseguys_pericias.json", originOverride = ORIGIN_WISEGUYS, originKey = ORIGIN_WISEGUYS),
        ModuleFile("scifi_pericias.json", originOverride = ORIGIN_SCI_FI, originKey = ORIGIN_SCI_FI),
        ModuleFile("deadlands_pericias.json", originOverride = ORIGIN_DEADLANDS, originKey = ORIGIN_DEADLANDS),
        ModuleFile("pathfinder_pericias.json", originOverride = ORIGIN_PATHFINDER, originKey = ORIGIN_PATHFINDER),
        ModuleFile("sol_vapor_pericias.json", originOverride = ORIGIN_CIDADE_SOL_VAPOR, originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("crystal_pericias.json", originOverride = ORIGIN_CRYSTAL_HEART, originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("super_pericias.json", originOverride = ORIGIN_SUPER, originKey = ORIGIN_SUPER)
    )

    private val advantageModules = listOf(
        ModuleFile("basico_vantagens.json"),
        ModuleFile("fantasia_vantagens.json", originOverride = ORIGIN_FANTASIA, originKey = ORIGIN_FANTASIA),
        ModuleFile("horror_vantagens.json", originOverride = ORIGIN_HORROR, originKey = ORIGIN_HORROR),
        ModuleFile("scifi_vantagens.json", originOverride = ORIGIN_SCI_FI, originKey = ORIGIN_SCI_FI),
        ModuleFile("crystal_vantagens.json", originOverride = ORIGIN_CRYSTAL_HEART, originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("super_vantagens.json", originOverride = ORIGIN_SUPER, originKey = ORIGIN_SUPER),
        ModuleFile("wiseguys_vantagens.json", originOverride = ORIGIN_WISEGUYS, originKey = ORIGIN_WISEGUYS),
        ModuleFile("adg_vantagens.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("sol_vapor_vantagens.json", originOverride = ORIGIN_CIDADE_SOL_VAPOR, originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("deadlands_vantagens.json", originOverride = ORIGIN_DEADLANDS, originKey = ORIGIN_DEADLANDS),
        ModuleFile("pathfinder_vantagens.json", originOverride = ORIGIN_PATHFINDER, originKey = ORIGIN_PATHFINDER)
    )

    private val complicationModules = listOf(
        ModuleFile("basico_complicacoes.json"),
        ModuleFile("fantasia_complicacoes.json", originOverride = ORIGIN_FANTASIA, originKey = ORIGIN_FANTASIA),
        ModuleFile("horror_complicacoes.json", originOverride = ORIGIN_HORROR, originKey = ORIGIN_HORROR),
        ModuleFile("scifi_complicacoes.json", originOverride = ORIGIN_SCI_FI, originKey = ORIGIN_SCI_FI),
        ModuleFile("super_complicacoes.json", originOverride = ORIGIN_SUPER, originKey = ORIGIN_SUPER),
        ModuleFile("wiseguys_complicacoes.json", originOverride = ORIGIN_WISEGUYS, originKey = ORIGIN_WISEGUYS),
        ModuleFile("crystal_complicacoes.json", originOverride = ORIGIN_CRYSTAL_HEART, originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("adg_complicacoes.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("sol_vapor_complicacoes.json", originOverride = ORIGIN_CIDADE_SOL_VAPOR, originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("deadlands_complicacoes.json", originOverride = ORIGIN_DEADLANDS, originKey = ORIGIN_DEADLANDS),
        ModuleFile("pathfinder_complicacoes.json", originOverride = ORIGIN_PATHFINDER, originKey = ORIGIN_PATHFINDER)
    )

    private val ancestryModules = listOf(
        ModuleFile("basico_ancestralidades.json"),
        ModuleFile("fantasia_ancestralidades.json", originKey = ORIGIN_FANTASIA),
        ModuleFile("horror_ancestralidades.json", originKey = ORIGIN_HORROR),
        ModuleFile("scifi_ancestralidades.json", originKey = ORIGIN_SCI_FI),
        ModuleFile("wiseguys_ancestralidades.json", originKey = ORIGIN_WISEGUYS),
        ModuleFile("crystal_ancestralidades.json", originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("adg_ancestralidades.json", originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("sol_vapor_ancestralidades.json", originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("deadlands_ancestralidades.json", originKey = ORIGIN_DEADLANDS),
        ModuleFile("pathfinder_ancestralidades.json", originKey = ORIGIN_PATHFINDER),
        ModuleFile("super_ancestralidades.json", originKey = ORIGIN_SUPER)
    )

    private val powerModules = listOf(
        ModuleFile("basico_poderes.json"),
        ModuleFile("fantasia_poderes.json", originOverride = ORIGIN_FANTASIA, originKey = ORIGIN_FANTASIA),
        ModuleFile("scifi_poderes.json", originOverride = ORIGIN_SCI_FI, originKey = ORIGIN_SCI_FI),
        ModuleFile("horror_poderes.json", originOverride = ORIGIN_HORROR, originKey = ORIGIN_HORROR),
        ModuleFile("deadlands_poderes.json", originOverride = ORIGIN_DEADLANDS, originKey = ORIGIN_DEADLANDS),
        ModuleFile("pathfinder_poderes.json", originOverride = ORIGIN_PATHFINDER, originKey = ORIGIN_PATHFINDER),
        ModuleFile("crystal_poderes.json", originOverride = ORIGIN_CRYSTAL_HEART, originKey = ORIGIN_CRYSTAL_HEART),
        ModuleFile("sol_vapor_poderes.json", originOverride = ORIGIN_CIDADE_SOL_VAPOR, originKey = ORIGIN_CIDADE_SOL_VAPOR),
        ModuleFile("wiseguys_poderes.json", originOverride = ORIGIN_WISEGUYS, originKey = ORIGIN_WISEGUYS),
        ModuleFile("adg_poderes.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("adg_tecnicas_chi.json", originOverride = ORIGIN_ARTE_DA_GUERRA, originKey = ORIGIN_ARTE_DA_GUERRA),
        ModuleFile("super_poderes_base.json", originOverride = ORIGIN_SUPER, originKey = ORIGIN_SUPER)
    )

    // --- Loading Logic ---

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.readJsonList(fileName: String): List<T> =
        open(fileName).use { input -> json.decodeFromStream(input) }

    private fun enabledOrigins(flags: CompendioFlags): Set<String> = buildSet {
        if (flags.compendioFantasiaAtivo) add(ORIGIN_FANTASIA)
        if (flags.compendioHorrorAtivo) add(ORIGIN_HORROR)
        if (flags.compendioSciFiAtivo) add(ORIGIN_SCI_FI)
        if (flags.compendioPathfinderAtivo) add(ORIGIN_PATHFINDER)
        if (flags.compendioDeadlandsAtivo) add(ORIGIN_DEADLANDS)
        if (flags.compendioCrystalHeartAtivo) add(ORIGIN_CRYSTAL_HEART)
        if (flags.compendioArteDaGuerraAtivo) add(ORIGIN_ARTE_DA_GUERRA)
        if (flags.compendioCidadeSolVaporAtivo) add(ORIGIN_CIDADE_SOL_VAPOR)
        if (flags.compendioWiseguysAtivo) add(ORIGIN_WISEGUYS)
        if (flags.modoSupers) add(ORIGIN_SUPER)
    }

    private fun ModuleFile.isEnabled(enabledOrigins: Set<String>): Boolean {
        return originKey == null || enabledOrigins.contains(originKey)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.loadAndMerge(
        modules: List<ModuleFile>,
        enabledOrigins: Set<String>,
        crossinline transform: (T, String?) -> T = { item, _ -> item }
    ): List<T> {
        return modules.filter { it.isEnabled(enabledOrigins) }.flatMap { module ->
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
    fun load(context: Context, flags: CompendioFlags = CompendioFlags()): MainActivityData {
        val assets = context.assets
        val enabledOrigins = enabledOrigins(flags)

        // 1. Equipamentos
        val allEquip = assets.loadAndMerge<EquipamentoCategoria>(equipmentModules, enabledOrigins) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }
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
        listaCoracoesCrystal = if (flags.compendioCrystalHeartAtivo) {
            runCatching {
                assets.open("crystal_coracoes.json")
                    .use { input -> json.decodeFromStream<List<CrystalHeart>>(input) }
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        // 3. Super Poderes
        val listaSuperPoderes: List<SuperPoder> =
            if (flags.modoSupers) {
                assets.open("super_poderes.json")
                    .use { input -> json.decodeFromStream<List<SuperPoder>>(input) }
            } else {
                emptyList()
            }

        // 4. Arcano Info
        val arcanoList: List<ArcanoInfo> =
            assets.open("geral_arcano_info.json")
                .use { input -> json.decodeFromStream<List<ArcanoInfo>>(input) }
        arcanoInfo = arcanoList.associate {
            it.key
                .uppercase()
                .semAcentos()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }

        // 5. Atributos
        val atributosData = loadJsonAsset<AtributoList>(context, "geral_atributos.json")
        listaAtributos = atributosData.atributos
            .map { it.nome.keyify() }
        mapaAtributosDisplay = atributosData.atributos
            .associate { it.nome.keyify() to it.nome }

        // 6. Pericias
        // Special handling for nested "pericias" object in PericiaList wrapper
        val todasPericiasJson = skillModules.filter { it.isEnabled(enabledOrigins) }.flatMap { module ->
            runCatching {
                val pList = loadJsonAsset<PericiaList>(context, module.fileName).pericias
                if (module.originOverride != null) {
                    pList.map { it.copy(origem = module.originOverride) }
                } else {
                    pList
                }
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

        mapaPericiasDescricao = periciasDescList.associate { it.nome.keyify() to it.descricao }

        listaPericias = listaPericias.map { pericia ->
            val desc = pericia.descricao ?: mapaPericiasDescricao[pericia.nome.keyify()]
            pericia.copy(descricao = desc)
        }

        // Carrega descrições de atributos
        mapaAtributosDescricao = atributosData.atributos.associate {
            it.nome.keyify() to (it.descricao ?: "")
        }

        // 7. Vantagens
        val todasVantagens = assets.loadAndMerge<Vantagem>(advantageModules, enabledOrigins) { item, override ->
             if (override != null) item.copy(origem = override) else item
        }

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter { it.origem.equals("SUPER", ignoreCase = true) }
        AppData.horrorVantagens = todasVantagens.filter { it.origem.equals("HORROR", ignoreCase = true) }
        AppData.pathfinderVantagens = todasVantagens.filter { it.origem.equals("PATHFINDER", ignoreCase = true) }

        listaVantagens = todasVantagens
        AppData.superVantagensParaDetalhe = AppData.superVantagens

        // 8. Tropos e Complicações
        val adgTropos = if (flags.compendioArteDaGuerraAtivo) {
            runCatching {
                loadJsonAsset<List<Tropo>>(context, "adg_tropos.json")
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }
        val chTropos = if (flags.compendioCrystalHeartAtivo) {
            runCatching {
                loadJsonAsset<List<Tropo>>(context, "crystal_tropos.json")
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        listaTropos = adgTropos + chTropos

        listaComplicacoes = assets.loadAndMerge<Complicacao>(complicationModules, enabledOrigins) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }

        // 9. Ancestralidades
        listaAncestralidadesJson = assets.loadAndMerge<RacialModifier>(ancestryModules, enabledOrigins) { item, override ->
            // Optionally override origin for ancestries too if needed, but AncestralidadesSection handles it well.
            // Leaving as is for now to avoid changing working logic, unless requested.
            // Actually, for consistency, if we want to ensure "source of truth", we should override.
            // But let's stick to what was analyzed as broken (Advantages, Equipment).
            // AncestralidadesSection builds logic based on what is loaded.
            item
        }

        // 10. Monstros
        listaMonstroTemplates = if (flags.compendioHorrorAtivo || flags.modoMonstroAtivo) {
            assets
                .open("horror_monstros.json")
                .use { input -> json.decodeFromStream<List<MonstroTemplate>>(input) }
        } else {
            emptyList()
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

        // 12. Regras de Criação de Raça
        val regrasCriacaoRaca = runCatching {
            loadJsonAsset<RegrasCriacaoRacaJson>(context, "basico_habilidades_criacao.json").tabela_criacao
        }.getOrNull()

        // 13. Poderes (Magias/Milagres/Etc)
        val todosPoderes = assets.loadAndMerge<Poder>(powerModules, enabledOrigins) { item, override ->
            if (override != null) item.copy(origem = override) else item
        }
        listaPoderes = todosPoderes

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
