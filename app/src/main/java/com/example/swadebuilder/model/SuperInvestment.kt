package com.example.swadebuilder.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
sealed class PowerEffect {
    @Serializable
    data class SuperAtributo(val attrKey: String, val steps: Int) : PowerEffect()
    @Serializable
    data class SuperPericia(val periciaKey: String, val steps: Int) : PowerEffect()
    @Serializable
    data class BonusArmadura(val value: Int) : PowerEffect()
    @Serializable
    data class BonusResistencia(val value: Int) : PowerEffect()
    @Serializable
    data class BonusAparar(val value: Int) : PowerEffect()
    @Serializable
    data class BonusMovimentacao(val value: Int) : PowerEffect()
    @Serializable
    data class SuperVantagem(val vantagemId: String) : PowerEffect()
    @Serializable
    data class Generico(val nome: String) : PowerEffect()
}

@Serializable
data class SuperInvestment(
    val id: String = UUID.randomUUID().toString(),
    val powerId: String,
    val displayName: String,
    val cost: Int,
    val effect: PowerEffect,
    val baseCost: Int = cost,
    val modifiers: Map<String, Int> = emptyMap()
)
