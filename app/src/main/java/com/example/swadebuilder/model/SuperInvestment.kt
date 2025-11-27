package com.example.swadebuilder.model

import java.util.UUID

sealed class PowerEffect {
    data class SuperAtributo(val attrKey: String, val steps: Int) : PowerEffect()
    data class SuperPericia(val periciaKey: String, val steps: Int) : PowerEffect()
    data class BonusArmadura(val value: Int) : PowerEffect()
    data class BonusResistencia(val value: Int) : PowerEffect()
    data class BonusAparar(val value: Int) : PowerEffect()
    data class BonusMovimentacao(val value: Int) : PowerEffect()
    data class SuperVantagem(val vantagemId: String) : PowerEffect()
    data class Generico(val nome: String) : PowerEffect()
}

data class SuperInvestment(
    val id: String = UUID.randomUUID().toString(),
    val powerId: String,
    val displayName: String,
    val cost: Int,
    val effect: PowerEffect,
    val baseCost: Int = cost,
    val modifiers: Map<String, Int> = emptyMap()
)
