package com.example.swadebuilder.model

import android.content.res.AssetManager
import com.example.swadebuilder.AppData
import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.Json
import java.io.BufferedReader

object DataLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private inline fun <reified T> AssetManager.readJsonList(fileName: String): List<T> =
        open(fileName).bufferedReader().use { reader -> json.decodeFromString(reader.readText()) }

    private inline fun <reified T> AssetManager.readJsonListOrEmpty(fileName: String): List<T> =
        runCatching { readJsonList<T>(fileName) }.getOrElse { emptyList() }

    private inline fun <reified T> AssetManager.readMultipleJsonLists(files: List<String>): List<T> =
        files.flatMap { readJsonListOrEmpty<T>(it) }

    private inline fun <reified T> AssetManager.mergeJsonLists(
        primaryFile: String,
        extraFiles: List<String>
    ): List<T> = readJsonList<T>(primaryFile) + readMultipleJsonLists<T>(extraFiles)

    // Helper for single object
    private inline fun <reified T> AssetManager.readJsonObject(fileName: String): T =
        open(fileName).bufferedReader().use { reader -> json.decodeFromString(reader.readText()) }


    fun loadAllData(assets: AssetManager) {
        // Equipamentos (List)
        val equipmentFiles = listOf(
            "equipamentos_crystal.json",
            "equipamentos_adg.json",
            "equipamentos_sol_vapor.json",
            "equipamentos_wiseguys.json"
        )
        // mergeJsonLists assumes List<T>
        val allEquipCats = assets.mergeJsonLists<EquipamentoCategoria>("equipamentos.json", equipmentFiles)

        GlobalData.equipamentoCategorias = allEquipCats.filter { cat ->
            cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
        }
        GlobalData.superequipCategorias = assets.readJsonList<EquipamentoCategoria>("equipamentos.json").filter { cat ->
            cat.origem?.equals("super", ignoreCase = true) ?: false
        }

        // Crystal Hearts (List)
        GlobalData.listaCoracoesCrystal = assets.readJsonListOrEmpty("coracoes_crystal.json")

        // Super Poderes (List)
        GlobalData.listaSuperPoderes = assets.readJsonList("superpoderes.json")

        // Arcano Info (List)
        val arcanoList = assets.readJsonList<ArcanoInfo>("arcano_info.json")
        GlobalData.arcanoInfo = arcanoList.associate {
            it.key.uppercase().semAcentos().trim() to Triple(it.slots, it.pp, it.foco)
        }

        // Atributos (Object)
        val atributosData = assets.readJsonObject<AtributoList>("atributos.json")
        GlobalData.listaAtributos = atributosData.atributos.map { it.nome.keyify() }
        GlobalData.mapaAtributosDisplay = atributosData.atributos.associate { it.nome.keyify() to it.nome }

        // Pericias (Object)
        val periciasData = assets.readJsonObject<PericiaList>("pericias.json")
        GlobalData.listaPericias = periciasData.pericias.map { pj ->
            Pericia(
                nome     = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica   = pj.basica
            )
        }

        // Vantagens (List)
        val todasVantagens = assets.readJsonList<Vantagem>("Vantagens.json")
        val vantCrystal = assets.readJsonListOrEmpty<Vantagem>("vantagens_crystal.json")
        val vantagensExtras = assets.readMultipleJsonLists<Vantagem>(
            listOf(
                "vantagens_adg.json",
                "vantagens_sol_vapor.json",
                "vantagens_wiseguys.json"
            )
        )

        AppData.basicasVantagens = todasVantagens.filter { it.origem.equals("BASICO", true) }
        AppData.superVantagens = todasVantagens.filter { it.origem.equals("SUPER", true) }
        AppData.horrorVantagens = todasVantagens.filter { it.origem.equals("HORROR", true) }
        AppData.trilhadorVantagens = todasVantagens.filter { it.origem.equals("TRILHADOR", true) }

        GlobalData.listaVantagens = todasVantagens + vantCrystal + vantagensExtras

        // Update AppData specific lists from the global unified list to be safe
        AppData.arteDaGuerraVantagens = GlobalData.listaVantagens.filter { it.origem.equals("ARTE_DA_GUERRA", true) }
        AppData.superVantagensParaDetalhe = AppData.superVantagens

        // Complicacoes (List)
        val todasComplicacoes = assets.readJsonList<Complicacao>("complicacoes.json")
        val complicacaoExtras = assets.readMultipleJsonLists<Complicacao>(
            listOf(
                "complicacoes_crystal.json",
                "complicacoes_adg.json",
                "complicacoes_sol_vapor.json",
                "complicacoes_wiseguys.json"
            )
        )
        GlobalData.listaComplicacoes = todasComplicacoes + complicacaoExtras

        // Ancestralidades (List)
        val ancestralFiles = listOf(
            "ancestralidades_trilhador.json",
            "ancestralidades_sci_fi.json",
            "ancestralidades_deadlands.json",
            "ancestralidades_adg.json",
            "ancestralidades_crystal.json",
            "ancestralidades_sol_vapor.json",
            "ancestralidades_wiseguys.json"
        )
        GlobalData.listaAncestralidadesJson = assets.mergeJsonLists<RacialModifier>("listaancestralidade.json", ancestralFiles)

        // Monstros (List)
        GlobalData.listaMonstroTemplates = assets.readJsonList("monstros.json")

        // Maps
        GlobalData.racialAttrMinMap = GlobalData.listaAncestralidadesJson.associate { rm ->
            val m = rm.atributos
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }

        GlobalData.racialSkillStartMap = GlobalData.listaAncestralidadesJson.associate { rm ->
            val m = rm.pericias
                .mapKeys   { it.key.keyify() }
                .mapValues { 4 + it.value }
            rm.nome.keyify() to m
        }
    }
}
