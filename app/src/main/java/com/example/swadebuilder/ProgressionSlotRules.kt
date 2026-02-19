package com.example.swadebuilder

import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.listaDeEstagios
import com.example.swadebuilder.model.dynamicStageCaps

fun stageIndexForSlot(slotIndex: Int): Int {
    if (slotIndex < 0) return 0

    var currentTotal = 0
    for (i in dynamicStageCaps.indices) {
        val cap = dynamicStageCaps[i]
        if (slotIndex < currentTotal + cap) {
            return i
        }
        currentTotal += cap
    }

    return (listaDeEstagios.size - 1).coerceAtLeast(0)
}

fun stageForSlot(slotIndex: Int): Estagio = listaDeEstagios[stageIndexForSlot(slotIndex)]
