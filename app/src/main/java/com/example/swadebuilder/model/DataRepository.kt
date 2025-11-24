package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.util.loadJsonAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DataRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    lateinit var allEquipCategorias: List<EquipamentoCategoria>
    lateinit var equipamentoCategorias: List<EquipamentoCategoria>
    lateinit var superequipCategorias: List<EquipamentoCategoria>
    lateinit var listaSuperPoderes: List<SuperPoder>
    lateinit var arcanoInfo: Map<String, Triple<Int, Int, String>>
    lateinit var listaAtributos: List<String>
    lateinit var mapaAtributosDisplay: Map<String, String>
    lateinit var listaPericias: List<Pericia>
    lateinit var todasVantagens: List<Vantagem>
    lateinit var listaComplicacoes: List<Complicacao>
    lateinit var listaAncestralidadesJson: List<RacialModifier>
    lateinit var racialAttrMinMap: Map<String, Map<String, Int>>
    lateinit var racialSkillStartMap: Map<String, Map<String, Int>>

    suspend fun loadAllData() {
        withContext(Dispatchers.IO) {
            val allEquipJson = context.assets.open("equipamentos.json").bufferedReader().use { it.readText() }
            allEquipCategorias = json.decodeFromString(allEquipJson)
            equipamentoCategorias = allEquipCategorias.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true)?.not() ?: true
            }
            superequipCategorias = allEquipCategorias.filter { cat ->
                cat.origem?.equals("super", ignoreCase = true) ?: false
            }

            val superPoderesJson = context.assets.open("superpoderes.json").bufferedReader().use { it.readText() }
            listaSuperPoderes = json.decodeFromString(superPoderesJson)

            val arcanoJson = context.assets.open("arcano_info.json").bufferedReader().use { it.readText() }
            val arcanoList: List<ArcanoInfo> = Json.decodeFromString(arcanoJson)
            arcanoInfo = arcanoList.associate {
                it.key.uppercase().semAcentos().trim() to Triple(it.slots, it.pp, it.foco)
            }

            val atributosData = context.loadJsonAsset<AtributoList>("atributos.json")
            listaAtributos = atributosData.atributos.map { it.nome.uppercase().semAcentos() }
            mapaAtributosDisplay = atributosData.atributos.associate { it.nome.uppercase().semAcentos() to it.nome }

            val periciasData = context.loadJsonAsset<PericiaList>("pericias.json")
            listaPericias = periciasData.pericias.map { pj ->
                Pericia(
                    nome = pj.nome,
                    atributo = pj.atributo.uppercase().semAcentos(),
                    basica = pj.basica
                )
            }

            todasVantagens = context.loadJsonAsset("Vantagens.json")

            val complicacoesJson = context.assets.open("complicacoes.json").bufferedReader().use { it.readText() }
            listaComplicacoes = json.decodeFromString(ListSerializer(Complicacao.serializer()), complicacoesJson)

            val ancestralRaw = context.assets.open("listaancestralidade.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            listaAncestralidadesJson = Json.decodeFromString<List<RacialModifier>>(ancestralRaw)

            racialAttrMinMap = listaAncestralidadesJson.associate { rm ->
                val m = rm.atributos
                    .mapKeys { it.key.uppercase().semAcentos() }
                    .mapValues { 4 + it.value }
                rm.nome.uppercase().semAcentos() to m
            }

            racialSkillStartMap = listaAncestralidadesJson.associate { rm ->
                val m = rm.pericias
                    .mapKeys { it.key.uppercase().semAcentos() }
                    .mapValues { 4 + it.value }
                rm.nome.uppercase().semAcentos() to m
            }
        }
    }
}
