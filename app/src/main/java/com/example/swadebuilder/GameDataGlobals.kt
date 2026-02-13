package com.example.swadebuilder

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem

var listaVantagens by mutableStateOf<List<Vantagem>>(emptyList())
var listaTropos by mutableStateOf<List<Tropo>>(emptyList())
var listaEquipamentos by mutableStateOf<List<EquipamentoItem>>(emptyList())
var listaPoderes by mutableStateOf<List<Poder>>(emptyList())

var equipamentoCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())
var superequipCategorias by mutableStateOf<List<EquipamentoCategoria>>(emptyList())

var listaSuperPoderes by mutableStateOf<List<SuperPoder>>(emptyList())

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
