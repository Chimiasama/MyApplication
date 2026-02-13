package com.example.swadebuilder

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.SuperPoder
import kotlinx.serialization.json.JsonElement

@Deprecated("Use GameDataStore.vantagens()")
var listaVantagens by mutableStateOf<List<Vantagem>>(emptyList())

@Deprecated("Use GameDataStore.tropos()")
var listaTropos by mutableStateOf<List<Tropo>>(emptyList())

@Deprecated("Use GameDataStore.equipamentos()")
var listaEquipamentos by mutableStateOf<List<EquipamentoItem>>(emptyList())

@Deprecated("Use GameDataStore.poderes()")
var listaPoderes by mutableStateOf<List<Poder>>(emptyList())

@Deprecated("Use GameDataStore.equipamentoCategorias()")
var equipamentoCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())

@Deprecated("Use GameDataStore.superequipCategorias()")
var superequipCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())

@Deprecated("Use GameDataStore.superPoderes()")
var listaSuperPoderes by mutableStateOf<List<SuperPoder>>(emptyList())

@Deprecated("Use GameDataStore.complicacoes()")
var listaComplicacoes by mutableStateOf<List<Complicacao>>(emptyList())

@Deprecated("Use GameDataStore.coracoesCrystal()")
var listaCoracoesCrystal by mutableStateOf<List<CrystalHeart>>(emptyList())

@Deprecated("Use GameDataStore")
var listaAncestralidadesJson by mutableStateOf<List<RacialModifier>>(emptyList())

@Deprecated("Use GameDataStore")
var listaMonstroTemplates by mutableStateOf<List<MonstroTemplate>>(emptyList())

@Deprecated("Use GameDataStore")
var racialAttrMinMap by mutableStateOf<Map<String, Map<String,Int>>>(emptyMap())

@Deprecated("Use GameDataStore")
var racialSkillStartMap by mutableStateOf<Map<String, Map<String,Int>>>(emptyMap())

@Deprecated("Use GameDataStore")
var listaAtributos by mutableStateOf<List<String>>(emptyList())

@Deprecated("Use GameDataStore")
var mapaAtributosDisplay by mutableStateOf<Map<String, String>>(emptyMap())

@Deprecated("Use GameDataStore")
var listaPericias by mutableStateOf<List<Pericia>>(emptyList())

@Deprecated("Use GameDataStore")
var mapaPericias by mutableStateOf<Map<String, Pericia>>(emptyMap())

@Deprecated("Use GameDataStore")
var mapaAtributosDescricao by mutableStateOf<Map<String, String>>(emptyMap())

data class Estagio(
    val nome: String,
    val minProgress: Int,
    val maxProgress: Int
)

val listaDeEstagios = listOf(
    Estagio("Novato", 0, 3),
    Estagio("Experiente", 4, 7),
    Estagio("Veterano", 8, 11),
    Estagio("Heroico", 12, 15),
    Estagio("Lendário", 16, Int.MAX_VALUE)
)

val nivelParaEstagio = mapOf(
    "N" to listaDeEstagios.first { it.nome == "Novato" },
    "E" to listaDeEstagios.first { it.nome == "Experiente" },
    "V" to listaDeEstagios.first { it.nome == "Veterano" },
    "H" to listaDeEstagios.first { it.nome == "Heroico" },
    "L" to listaDeEstagios.first { it.nome == "Lendário" }
)

const val TOTAL_PROGRESS_LIMIT = 20
val dynamicStageCaps = listaDeEstagios.mapIndexed { idx, st ->
    val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
    if (idx < listaDeEstagios.lastIndex)
        st.maxProgress - prevMax
    else
        (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
}
