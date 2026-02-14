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

    // Fase 4 (continuação): políticas de cenário para visibilidade de vantagens
    fun blocksArcaneBackgrounds(): Boolean = false
    fun allowsGenericArcaneSelector(): Boolean = false
    fun forbiddenAdvantageIds(): Set<String> = emptySet()
    fun hidePowerCategoryAdvantagesExceptMysticPowers(): Boolean = false
}

object BaseRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
}

object FantasiaRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 300)
    override fun defaultAncestralidade(): String = "HUMANOS"
    override fun allowsGenericArcaneSelector(): Boolean = true
}

object HorrorRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
    override fun allowsGenericArcaneSelector(): Boolean = true
}

object SciFiRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 1000)
    override fun defaultAncestralidade(): String = "HUMANOS"
}

object DeadlandsRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 250)
    override fun defaultAncestralidade(): String = "Humano"
    override fun allowsGenericArcaneSelector(): Boolean = true
}

object CrystalHeartRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "As Ilhas"
    override fun mandatoryAdvantageIds(): Set<String> = setOf(AdvantageIds.AA_AGENTE_SYN)
    override fun defaultCrystalHeartId(): String? = CrystalHeartIds.HEART_STARTER
    override fun blocksArcaneBackgrounds(): Boolean = true
}

object ArteDaGuerraRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
    override fun blocksArcaneBackgrounds(): Boolean = true
    override fun forbiddenAdvantageIds(): Set<String> = setOf("resistencia_arcana", "resistencia_arcana_aprimorada")
    override fun hidePowerCategoryAdvantagesExceptMysticPowers(): Boolean = true
}

object WiseguysRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
    override fun blocksArcaneBackgrounds(): Boolean = true
    override fun forbiddenAdvantageIds(): Set<String> = setOf(
        "resistencia_arcana",
        "resistencia_arcana_aprimorada",
        "aristocrata",
        "chi",
        "campeao",
        "matador_de_gigantes",
        "corajoso"
    )

    override fun hidePowerCategoryAdvantagesExceptMysticPowers(): Boolean = true
}

object CidadeSolVaporRules : GameRules {
    override fun startingResources(): StartingResources = StartingResources(dinheiro = 500)
    override fun defaultAncestralidade(): String = "HUMANOS"
    override fun allowsGenericArcaneSelector(): Boolean = true
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
    override fun allowsGenericArcaneSelector(): Boolean = true
    override fun forbiddenAdvantageIds(): Set<String> = setOf(
        "antecedente_arcano_ciencia_estranha",
        "antecedente_arcano_psionicos",
        "antecedente_arcano_dom",
        "rico",
        "podre_de_rico"
    )
}

class RulesResolver {
    fun resolve(
        compendioPathfinderAtivo: Boolean,
        compendioSciFiAtivo: Boolean,
        compendioDeadlandsAtivo: Boolean,
        compendioFantasiaAtivo: Boolean,
        compendioCrystalHeartAtivo: Boolean,
        compendioHorrorAtivo: Boolean,
        compendioArteDaGuerraAtivo: Boolean,
        compendioCidadeSolVaporAtivo: Boolean,
        compendioWiseguysAtivo: Boolean
    ): GameRules {
        return when {
            compendioPathfinderAtivo -> PathfinderRules
            compendioSciFiAtivo -> SciFiRules
            compendioDeadlandsAtivo -> DeadlandsRules
            compendioWiseguysAtivo -> WiseguysRules
            compendioArteDaGuerraAtivo -> ArteDaGuerraRules
            compendioCidadeSolVaporAtivo -> CidadeSolVaporRules
            compendioCrystalHeartAtivo -> CrystalHeartRules
            compendioHorrorAtivo -> HorrorRules
            compendioFantasiaAtivo -> FantasiaRules
            else -> BaseRules
        }
    }
}
