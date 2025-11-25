package com.example.swadebuilder.model

import android.content.Context
import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.R
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.util.loadJsonAsset
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

class DataRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun loadEquipamentoCategorias(): List<EquipamentoCategoria> {
        val allEquipJson = context.assets
            .open("equipamentos.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(allEquipJson)
    }

    fun loadSuperPoderes(): List<SuperPoder> {
        val superPoderesJson = context.assets
            .open("superpoderes.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(superPoderesJson)
    }

    fun loadArcanoInfo(): Map<String, Triple<Int, Int, String>> {
        val arcanoJson = context.assets.open("arcano_info.json")
            .bufferedReader().use { it.readText() }
        val arcanoList: List<ArcanoInfo> =
            Json.decodeFromString(arcanoJson)
        return arcanoList.associate {
            it.key
                .uppercase()
                .trim() to Triple(it.slots, it.pp, it.foco)
        }
    }

    fun loadAtributos(): AtributoList {
        return context.loadJsonAsset("atributos.json")
    }

    fun loadPericias(): PericiaList {
        return context.loadJsonAsset("pericias.json")
    }

    fun loadVantagens(): List<Vantagem> {
        return context.loadJsonAsset("Vantagens.json")
    }

    fun loadComplicacoes(): List<Complicacao> {
        val complicacoesJson = context.assets
            .open("complicacoes.json")
            .bufferedReader()
            .use { it.readText() }
        return json.decodeFromString(
            ListSerializer(Complicacao.serializer()),
            complicacoesJson
        )
    }

    fun loadRacialModifiers(): List<RacialModifier> {
        val ancestralRaw = context.assets.open("listaancestralidade.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
        return Json.decodeFromString(ancestralRaw)
    }

    fun loadRawText(resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
        val reader = BufferedReader(InputStreamReader(inputStream))
        return reader.readText()
    }
}
