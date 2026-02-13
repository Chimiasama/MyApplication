package com.example.swadebuilder

fun stageIndexForSlot(slotIndex: Int): Int {
    if (slotIndex < 0) return 0

    val idx = slotIndex / 4
    val maxIndex = (listaDeEstagios.size - 1).coerceAtLeast(0)

    return idx.coerceAtMost(maxIndex)
}

fun stageForSlot(slotIndex: Int): Estagio = listaDeEstagios[stageIndexForSlot(slotIndex)]
