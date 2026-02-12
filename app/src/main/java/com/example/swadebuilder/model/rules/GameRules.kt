package com.example.swadebuilder.model.rules

import com.example.swadebuilder.model.ids.PathfinderCurrencyIds

data class StartingResources(
    val dinheiro: Int,
    val carteiraPathfinder: Map<String, Int> = emptyMap()
)

interface GameRules {
    fun startingResources(): StartingResources
}

object BaseRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
}

object FantasiaRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 300)
}

object SciFiRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 1000)
}

object DeadlandsRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 250)
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
}

class RulesResolver {
    fun resolve(
        compendioPathfinderAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        compendioFantasiaAtivo: Boolean
    ): GameRules {
        return when {
            compendioPathfinderAtivo -> PathfinderRules
            compendioSciFiAtivo -> SciFiRules
            compendioDeadlandsAtivo -> DeadlandsRules
            compendioFantasiaAtivo -> FantasiaRules
            else -> BaseRules
        }
    }
}
