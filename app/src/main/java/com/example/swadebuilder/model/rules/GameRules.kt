package com.example.swadebuilder.model.rules

import com.example.swadebuilder.model.ids.AdvantageIds
import com.example.swadebuilder.model.ids.CrystalHeartIds
import com.example.swadebuilder.model.ids.PathfinderCurrencyIds

data class StartingResources(
    val dinheiro: Int,
    val carteiraPathfinder: Map<String, Int> = emptyMap()
)

interface GameRules {
    fun startingResources(): StartingResources
    fun defaultAncestralidade(): String
    fun mandatoryAdvantageIds(): Set<String> = emptySet()
    fun defaultCrystalHeartId(): String? = null
}

object BaseRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
}

object FantasiaRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 300)
    override fun defaultAncestralidade(): String = "HUMANOS"
}

object SciFiRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 1000)
    override fun defaultAncestralidade(): String = "HUMANOS"
}

object DeadlandsRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 250)
    override fun defaultAncestralidade(): String = "Humano"
}

object CrystalHeartRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "As Ilhas"
    override fun mandatoryAdvantageIds(): Set<String> = setOf(AdvantageIds.AA_AGENTE_SYN)
    override fun defaultCrystalHeartId(): String? = CrystalHeartIds.HEART_STARTER
}

object PathfinderRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(
        dinheiro = 30000,
        carteiraPathfinder = mapOf(
            PathfinderCurrencyIds.PL to 0,
            PathfinderCurrencyIds.PO to 300,
            PathfinderCurrencyIds.PP to 0,
            PathfinderCurrencyIds.PC to 0
        )
    )

    override fun defaultAncestralidade(): String = "Humano (Pathfinder)"
}

class RulesResolver {
    fun resolve(
        compendioPathfinderAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioCrystalHeartAtivo: Boolean
    ): GameRules {
        return when {
            compendioPathfinderAtivo -> PathfinderRules
            compendioSciFiAtivo -> SciFiRules
            compendioDeadlandsAtivo -> DeadlandsRules
            compendioCrystalHeartAtivo -> CrystalHeartRules
            compendioFantasiaAtivo -> FantasiaRules
            else -> BaseRules
        }
    }
}
