package com.example.swadebuilder

import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.listaDeEstagios

fun stageIndexForSlot(slotIndex: Int): Int {
    if (slotIndex < 0) return 0

    // O slot representa o XP que está sendo gasto agora.
    // Ex.: slot 0 = XP 1, slot 1 = XP 2 ... slot 3 = XP 4 (já Experiente).
    val progressAfterSpend = slotIndex + 1

    return listaDeEstagios.indexOfFirst { progressAfterSpend in it.minProgress..it.maxProgress }
        .takeIf { it >= 0 }
        ?: listaDeEstagios.lastIndex
}

fun stageForSlot(slotIndex: Int): Estagio = listaDeEstagios[stageIndexForSlot(slotIndex)]
