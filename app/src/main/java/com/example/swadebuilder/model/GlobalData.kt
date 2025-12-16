package com.example.swadebuilder.model

import com.example.swadebuilder.ArcanoInfo
import com.example.swadebuilder.Estagio
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.SuperPoder
import com.example.swadebuilder.TOTAL_PROGRESS_LIMIT

object GlobalData {
    var arcanoInfo: Map<String, Triple<Int, Int, String>> = emptyMap()
    var listaComplicacoes: List<Complicacao> = emptyList()
    var listaCoracoesCrystal: List<CrystalHeart> = emptyList()
    var listaAncestralidadesJson: List<RacialModifier> = emptyList()
    var listaMonstroTemplates: List<MonstroTemplate> = emptyList()

    var racialAttrMinMap: Map<String, Map<String, Int>> = emptyMap()
    var racialSkillStartMap: Map<String, Map<String, Int>> = emptyMap()

    var listaAtributos: List<String> = emptyList()
    var mapaAtributosDisplay: Map<String, String> = emptyMap()

    var listaPericias: List<Pericia> = emptyList()
    var listaVantagens: List<Vantagem> = emptyList()

    var listaSuperPoderes: List<SuperPoder> = emptyList()
    var equipamentoCategorias: List<EquipamentoCategoria> = emptyList()
    var superequipCategorias: List<EquipamentoCategoria> = emptyList()

    val listaDeEstagios = listOf(
        Estagio("Novato",     0,  3),
        Estagio("Experiente", 4,  7),
        Estagio("Veterano",   8, 11),
        Estagio("Heroico",   12, 15),
        Estagio("Lendário",  16, Int.MAX_VALUE)
    )

    val nivelParaEstagio by lazy {
        mapOf(
            "N" to listaDeEstagios.first { it.nome == "Novato" },
            "E" to listaDeEstagios.first { it.nome == "Experiente" },
            "V" to listaDeEstagios.first { it.nome == "Veterano" },
            "H" to listaDeEstagios.first { it.nome == "Heroico" },
            "L" to listaDeEstagios.first { it.nome == "Lendário" }
        )
    }

    val dynamicStageCaps by lazy {
        listaDeEstagios.mapIndexed { idx, st ->
            val prevMax = listaDeEstagios.getOrNull(idx - 1)?.maxProgress ?: 0
            if (idx < listaDeEstagios.lastIndex)
                st.maxProgress - prevMax
            else
                (TOTAL_PROGRESS_LIMIT - prevMax).coerceAtLeast(0)
        }
    }
}
