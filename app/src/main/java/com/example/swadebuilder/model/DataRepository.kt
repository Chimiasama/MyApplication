package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.R
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class DataRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadEquipamentoCategorias(): List<EquipamentoCategoria> {
        return context.loadJsonAsset("equipamentos.json")
    }

    fun loadSuperPoderes(): List<SuperPoder> {
        return context.loadJsonAsset("superpoderes.json")
    }

    fun loadArcanoInfo(): List<ArcanoInfo> {
        return context.loadJsonAsset("arcano_info.json")
    }

    fun loadAtributos(): AtributoList {
        return context.loadJsonAsset("atributos.json")
    }

    fun loadPericias(): List<Pericia> {
        val periciasData: PericiaList = context.loadJsonAsset("pericias.json")
        return periciasData.pericias.map { pj ->
            Pericia(
                nome = pj.nome,
                atributo = pj.atributo.uppercase().semAcentos(),
                basica = pj.basica
            )
        }
    }

    fun loadVantagens(): List<Vantagem> {
        return context.loadJsonAsset("Vantagens.json")
    }

    fun loadComplicacoes(): List<Complicacao> {
        val jsonString = context.assets.open("complicacoes.json").bufferedReader().use { it.readText() }
        return json.decodeFromString(ListSerializer(Complicacao.serializer()), jsonString)
    }

    fun loadRacialModifiers(): List<RacialModifier> {
        return context.loadJsonAsset("listaancestralidade.json")
    }

    fun loadVantagensText(): String {
        return context.assets.open("Vantagens.json").bufferedReader().use { it.readText() }
    }

    fun loadComplicacoesText(): String {
        return context.assets.open("complicacoes.json").bufferedReader().use { it.readText() }
    }

    fun loadAtributosText(): String {
        return context.resources.openRawResource(R.raw.atributos).bufferedReader().use { it.readText() }
    }

    fun loadAncestralidadesText(): String {
        return context.resources.openRawResource(R.raw.ancestralidades).bufferedReader().use { it.readText() }
    }

    fun loadPericiasDescriptions(): Map<String, String> {
        val jsonText = context.resources.openRawResource(R.raw.pericias).bufferedReader().use { it.readText() }
        val lista = Json.decodeFromString<List<PericiaDescricaoJson>>(jsonText)
        return lista.associate { pericia ->
            val key = pericia.nome.uppercase().semAcentos()
            key to pericia.descricao.trim()
        }
    }
}
