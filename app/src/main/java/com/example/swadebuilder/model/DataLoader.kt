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

    @OptIn(ExperimentalSerializationApi::class)
    private inline fun <reified T> AssetManager.readJsonList(fileName: String): List<T> =
        open(fileName).use { input -> json.decodeFromStream(input) }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun load(context: Context): MainActivityData {
        val assets = context.assets

        // 1. Equipamentos
        val baseEquip = assets.readJsonList<EquipamentoCategoria>("basico_equipamentos.json")
        val suppEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("suplementos_equipamentos.json")
        }.getOrElse { emptyList() }
        val fantasyEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("fantasia_equipamentos.json")
        }.getOrElse { emptyList() }
        val horrorEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("horror_equipamentos.json")
        }.getOrElse { emptyList() }
        val scifiEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("scifi_equipamentos.json")
        }.getOrElse { emptyList() }
        val crystalEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("equipamentos_crystal.json")
        }.getOrElse { emptyList() }
        val trilhadorEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("equipamentos_trilhador.json")
        }.getOrElse { emptyList() }
        val superEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("super_equipamentos.json")
        }.getOrElse { emptyList() }
        val wiseguysEquip = runCatching {
            assets.readJsonList<EquipamentoCategoria>("wiseguys_equipamentos.json")
        }.getOrElse { emptyList() }
        val allEquip = baseEquip + suppEquip + fantasyEquip + horrorEquip + scifiEquip + crystalEquip + trilhadorEquip + superEquip + wiseguysEquip

        listaEquipamentos = allEquip.flatMap { it.itens }

        val equipamentoCategorias = deduplicarEquipamentoCategorias(
            allEquip.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
        )
        val superequipCategorias = deduplicarEquipamentoCategorias(
            (baseEquip + suppEquip).filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }
        )

        // 2. Crystal Hearts
        listaCoracoesCrystal = runCatching {
            assets.open("coracoes_crystal.json")
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
        val periciasCoreData = loadJsonAsset<PericiaList>(context, "basico_pericias.json")
        val periciasSuppData = runCatching {
            loadJsonAsset<PericiaList>(context, "suplementos_pericias.json")
        }.getOrElse { PericiaList(emptyList()) }
        val periciasAdgData = runCatching {
            loadJsonAsset<PericiaList>(context, "pericias_adg.json")
        }.getOrElse { PericiaList(emptyList()) }

        val todasPericiasJson = periciasCoreData.pericias + periciasSuppData.pericias + periciasAdgData.pericias

        listaPericias = todasPericiasJson.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica,
                origem   = pj.origem
            )
        }
        mapaPericias = listaPericias.associateBy { it.nome.keyify() }

        // Carrega descrições de perícias (novo arquivo JSON, ex-txt)
        val periciasDescList = runCatching {
            assets.open("pericias_desc.json").use { input ->
                json.decodeFromStream<List<PericiaDescricaoJson>>(input)
            }
        }.getOrElse { emptyList() }

        val periciasDescAdgList = runCatching {
            assets.open("pericias_desc_adg.json").use { input ->
                json.decodeFromStream<List<PericiaDescricaoJson>>(input)
            }
        }.getOrElse { emptyList() }

        mapaPericiasDescricao = periciasDescList.associate { it.nome.keyify() to it.descricao }
        mapaPericiasDescricaoAdg = periciasDescAdgList.associate { it.nome.keyify() to it.descricao }

        // Carrega descrições de atributos (novo arquivo JSON, ex-txt)
        val atributosDescList = runCatching {
            assets.open("atributos_desc.json").use { input ->
                json.decodeFromStream<List<PericiaDescricaoJson>>(input) // Reutiliza DTO pois estrutura é igual
            }
        }.getOrElse { emptyList() }
        mapaAtributosDescricao = atributosDescList.associate { it.nome.keyify() to it.descricao }

        // 7. Vantagens
        val coreVantagens: List<Vantagem> = loadJsonAsset(context, "basico_vantagens.json")
        val suppVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "suplementos_vantagens.json")
        }.getOrElse { emptyList() }
        val fantasyVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "fantasia_vantagens.json")
        }.getOrElse { emptyList() }
        val horrorVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "horror_vantagens.json")
        }.getOrElse { emptyList() }
        val scifiVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "scifi_vantagens.json")
        }.getOrElse { emptyList() }
        val crystalVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "vantagens_crystal.json")
        }.getOrElse { emptyList() }
        val superVantagensList: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "super_vantagens.json")
        }.getOrElse { emptyList() }
        val wiseguysVantagens: List<Vantagem> = runCatching {
            loadJsonAsset<List<Vantagem>>(context, "wiseguys_vantagens.json")
        }.getOrElse { emptyList() }
        val todasVantagens = coreVantagens + suppVantagens + fantasyVantagens + horrorVantagens + scifiVantagens + crystalVantagens + superVantagensList + wiseguysVantagens

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter {
            it.origem.equals("SUPER", ignoreCase = true)
        }
        AppData.horrorVantagens = todasVantagens.filter {
            it.origem.equals("HORROR", ignoreCase = true)
        }
        AppData.buscatrilhaVantagens = todasVantagens.filter {
            it.origem.equals("BUSCATRILHA", ignoreCase = true) ||
                    it.origem.equals("FANTASIABUSCATRILHA", ignoreCase = true)
        }

        listaVantagens = todasVantagens
        AppData.superVantagensParaDetalhe = AppData.superVantagens

        // 8. Tropos e Complicações
        val adgTropos = runCatching {
            loadJsonAsset<List<Tropo>>(context, "tropos_adg.json")
        }.getOrElse { emptyList() }
        val chTropos = runCatching {
            loadJsonAsset<List<Tropo>>(context, "tropos_ch.json")
        }.getOrElse { emptyList() }

        listaTropos = adgTropos + chTropos
        val coreComplicacoes = loadJsonAsset<List<Complicacao>>(context, "basico_complicacoes.json")
        val suppComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "suplementos_complicacoes.json")
        }.getOrElse { emptyList() }
        val fantasyComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "fantasia_complicacoes.json")
        }.getOrElse { emptyList() }
        val horrorComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "horror_complicacoes.json")
        }.getOrElse { emptyList() }
        val scifiComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "scifi_complicacoes.json")
        }.getOrElse { emptyList() }
        val superComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "super_complicacoes.json")
        }.getOrElse { emptyList() }
        val wiseguysComplicacoes = runCatching {
            loadJsonAsset<List<Complicacao>>(context, "wiseguys_complicacoes.json")
        }.getOrElse { emptyList() }
        listaComplicacoes = coreComplicacoes + suppComplicacoes + fantasyComplicacoes + horrorComplicacoes + scifiComplicacoes + superComplicacoes + wiseguysComplicacoes

        // 9. Ancestralidades
        val coreAncestries = assets.readJsonList<RacialModifier>("basico_listaancestralidade.json")
        val suppAncestries = runCatching {
            assets.readJsonList<RacialModifier>("suplementos_listaancestralidade.json")
        }.getOrElse { emptyList() }
        val fantasyAncestries = runCatching {
            assets.readJsonList<RacialModifier>("fantasia_listaancestralidade.json")
        }.getOrElse { emptyList() }
        val horrorAncestries = runCatching {
            assets.readJsonList<RacialModifier>("horror_listaancestralidade.json")
        }.getOrElse { emptyList() }
        val scifiAncestries = runCatching {
            assets.readJsonList<RacialModifier>("scifi_listaancestralidade.json")
        }.getOrElse { emptyList() }
        val superAncestries = runCatching {
            assets.readJsonList<RacialModifier>("super_listaancestralidade.json")
        }.getOrElse { emptyList() }
        val wiseguysAncestries = runCatching {
            assets.readJsonList<RacialModifier>("wiseguys_ancestralidades.json")
        }.getOrElse { emptyList() }
        listaAncestralidadesJson = coreAncestries + suppAncestries + fantasyAncestries + horrorAncestries + scifiAncestries + superAncestries + wiseguysAncestries

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

        return MainActivityData(equipamentoCategorias, superequipCategorias, listaSuperPoderes)
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
    val listaSuperPoderes: List<SuperPoder>
)

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> loadJsonAsset(context: Context, fileName: String): T {
    val json = Json { ignoreUnknownKeys = true }
    return context.assets.open(fileName).use { input ->
        json.decodeFromStream(input)
    }
}
