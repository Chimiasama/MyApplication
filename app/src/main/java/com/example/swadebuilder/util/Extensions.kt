package com.example.swadebuilder.util

import com.example.swadebuilder.model.EquipamentoItem

fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"

val EquipamentoItem.passageiros
    get() = this.tripulacao
val EquipamentoItem.blindagem
    get() = this.resistencia
