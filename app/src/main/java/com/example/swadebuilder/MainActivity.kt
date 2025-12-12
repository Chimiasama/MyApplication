package com.example.swadebuilder

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.swadebuilder.model.AtributoJson
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.theme.SWADEbuilderTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.loadJsonAsset
import com.example.swadebuilder.util.normAAKey
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.json.Json

lateinit var listaAtributosJson: List<AtributoJson>
lateinit var listaPericiasJson: List<Pericia>
lateinit var listaAncestralidadesJson: List<RacialModifier>
lateinit var listaDeEstagios: List<Estagio>
lateinit var listaComplicacoes: List<Complicacao>
lateinit var listaVantagens: List<Vantagem>
lateinit var listaPoderes: List<Poder>
lateinit var listaSuperPoderes: List<SuperPoder>
lateinit var listaEquipamentos: List<EquipamentoCategoria>
lateinit var listaMonstroTemplates: List<MonstroTemplate>

lateinit var listaAtributos: List<String>
lateinit var mapaAtributosDisplay: Map<String, String>
lateinit var listaPericias: List<Pericia>
lateinit var racialAttrMinMap: Map<String, Map<String, Int>>
lateinit var dynamicStageCaps: List<Int>

val TOTAL_PROGRESS_LIMIT = 20

lateinit var nivelParaEstagio: Map<String, Estagio>

lateinit var allPowersMap: Map<String, String>

lateinit var arcanoInfo: Map<String, Pair<Int, Int>> // <ArcanoKey, <Powers, PP>>

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listaAtributosJson = loadJsonAsset(this, "atributos.json")
        listaAtributos = listaAtributosJson.map { it.nome.uppercase() }
        mapaAtributosDisplay = listaAtributosJson.associate {
            it.nome.uppercase() to it.nome
        }

        listaPericiasJson = loadJsonAsset(this, "pericias.json")
        listaPericias = listaPericiasJson.sortedBy { it.nome }

        val rawAncestralidades = assets.open("ancestralidades.txt").bufferedReader().use { it.readText() }
        val parsedAncestralidades = parseRacialModifiers(rawAncestralidades)

        val trilhadorAncestralidades: List<RacialModifier> =
            loadJsonAsset(this, "ancestralidades_trilhador.json")
        val scifiAncestralidades: List<RacialModifier> =
            loadJsonAsset(this, "ancestralidades_sci_fi.json")
        val deadlandsAncestralidades: List<RacialModifier> =
            loadJsonAsset(this, "ancestralidades_deadlands.json")

        listaAncestralidadesJson = parsedAncestralidades + trilhadorAncestralidades + scifiAncestralidades + deadlandsAncestralidades

        listaMonstroTemplates = loadJsonAsset(this, "monstros.json")

        racialAttrMinMap = listaAncestralidadesJson.associate { rm ->
            val minMap = mutableMapOf<String, Int>()
            rm.habilidades.forEach { hab ->
                val lines = hab.descricao.split("\n", ";", ".")
                lines.forEach { line ->
                    if (line.contains("d6", ignoreCase = true) && !line.contains("perícia", ignoreCase = true)) {
                        listaAtributos.forEach { attr ->
                            if (line.contains(attr, ignoreCase = true)) {
                                minMap[attr] = 6
                            }
                        }
                    }
                }
            }
            rm.nome.keyify() to minMap
        }

        listaDeEstagios = loadJsonAsset(this, "estagios.json")

        dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            if (idx < listaDeEstagios.lastIndex) {
                st.maxProgress - prevMax
            } else {
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
            }
        }

        nivelParaEstagio = listaDeEstagios.associateBy { it.nome.uppercase() }
        val nivelParaEstagioLower = listaDeEstagios.associateBy { it.nome.lowercase() }
        nivelParaEstagio = nivelParaEstagio + nivelParaEstagioLower

        val json = Json { ignoreUnknownKeys = true }

        val rawComplicacoes = assets.open("complicacoes.json").bufferedReader().use { it.readText() }
        val baseComplicacoes: List<Complicacao> = json.decodeFromString(rawComplicacoes)

        val trilhadorComplicacoes: List<Complicacao> = try {
            val raw = assets.open("complicacoes_trilhador.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        val deadlandsComplicacoes: List<Complicacao> = try {
            val raw = assets.open("complicacoes_deadlands.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        listaComplicacoes = (baseComplicacoes + trilhadorComplicacoes + deadlandsComplicacoes)
            .sortedBy { it.name }


        val rawVantagens = assets.open("Vantagens.json").bufferedReader().use { it.readText() }
        val baseVantagens: List<Vantagem> = json.decodeFromString(rawVantagens)

        val trilhadorVantagens: List<Vantagem> = try {
            val raw = assets.open("vantagens_trilhador.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        val horrorVantagens: List<Vantagem> = try {
            val raw = assets.open("vantagens_horror.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        val sciFiVantagens: List<Vantagem> = try {
            val raw = assets.open("vantagens_sci_fi.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        val deadlandsVantagens: List<Vantagem> = try {
            val raw = assets.open("vantagens_deadlands.json").bufferedReader().use { it.readText() }
            json.decodeFromString(raw)
        } catch (e: Exception) { emptyList() }

        listaVantagens = baseVantagens + trilhadorVantagens + horrorVantagens + sciFiVantagens + deadlandsVantagens

        listaPoderes = loadJsonAsset(this, "poderes.json")
        listaSuperPoderes = loadJsonAsset(this, "superpoderes.json")

        allPowersMap = buildMap {
            putAll(listaPoderes.associate { it.id to it.nome })
            putAll(listaSuperPoderes.associate { it.nome.keyify() to it.nome })
        }

        val baseEquip = loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos.json")
        val fantasyEquip = try {
            loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos_fantasia.json")
        } catch(e: Exception) { emptyList() }
        val horrorEquip = try {
            loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos_horror.json")
        } catch(e: Exception) { emptyList() }
        val trilhadorEquip = try {
            loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos_trilhador.json")
        } catch(e: Exception) { emptyList() }
        val scifiEquip = try {
            loadJsonAsset<List<EquipamentoCategoria>>(this, "ciberneticos.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "chassis_sci_fi.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "armaduras_poderosas.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos_sci_fi.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "robos.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "veiculos_sci_fi.json") +
            loadJsonAsset<List<EquipamentoCategoria>>(this, "naves.json")
        } catch(e: Exception) { emptyList() }
        val deadlandsEquip = try {
            loadJsonAsset<List<EquipamentoCategoria>>(this, "equipamentos_deadlands.json")
        } catch(e: Exception) { emptyList() }

        listaEquipamentos = baseEquip + fantasyEquip + horrorEquip + trilhadorEquip + scifiEquip + deadlandsEquip

        arcanoInfo = listaVantagens
            .filter { it.id.startsWith("antecedente_arcano") || it.id.startsWith("aa_") }
            .associate { v ->
                val desc = v.descricao.lowercase()

                val powersRegex = Regex("(\\d+)\\s+poderes")
                val ppRegex     = Regex("(\\d+)\\s+(pontos de poder|pp)")

                val powers = powersRegex.find(desc)?.groupValues?.get(1)?.toIntOrNull() ?: 3
                val pp     = ppRegex.find(desc)?.groupValues?.get(1)?.toIntOrNull()     ?: 10

                val key = if (v.id == "antecedente_arcano" && v.choice != null) {
                    "antecedente_arcano:${v.choice}".normAAKey()
                } else {
                    v.id.normAAKey()
                }

                key to (powers to pp)
            }

        setContent {
            SWADEbuilderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UnifiedScreen()
                }
            }
        }
    }
}

fun parseRacialModifiers(raw: String): List<RacialModifier> {
    val list = mutableListOf<RacialModifier>()
    val entries = raw.split(Regex("(?m)^(?=[A-ZÃÁÂÊÉÍÓÔÚÇ ]+:)")).filter { it.isNotBlank() }

    entries.forEach { entry ->
        val lines = entry.trim().lines()
        if (lines.isEmpty()) return@forEach

        val header = lines.first()
        val raceName = header.substringBefore(":").trim()
        val restOfHeader = header.substringAfter(":", "").trim()

        val fullText = (listOf(restOfHeader) + lines.drop(1)).joinToString(" ")

        val vantRegex = Regex("(?i)Vantagens Raciais Gratuitas:\\s*(.*?)(?=(Desvantagens Raciais:|Atributos Raciais:|$))")
        val desvRegex = Regex("(?i)Desvantagens Raciais:\\s*(.*?)(?=(Vantagens Raciais Gratuitas:|Atributos Raciais:|$))")

        val vantMatch = vantRegex.find(fullText)
        val desvMatch = desvRegex.find(fullText)

        val vantStr = vantMatch?.groupValues?.get(1) ?: ""
        val desvStr = desvMatch?.groupValues?.get(1) ?: ""

        // Processa vantagens
        val vants = vantStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val cleanVants = vants.map { v ->
            // Remove sufixos como (se não for...) ou descrições longas
            // Simplificação: pega só o nome principal se possível
            // Mas cuidado com "Sentidos Aguçados (Visão)"
            v
        }

        val desvs = desvStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

        list.add(
            RacialModifier(
                id = raceName.keyify(),
                nome = raceName,
                vantagensGratis = cleanVants,
                desvantagens = desvs
            )
        )
    }

    return list
}
