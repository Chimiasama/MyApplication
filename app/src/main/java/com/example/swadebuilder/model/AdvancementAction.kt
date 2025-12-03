package com.example.swadebuilder.model

sealed class AdvancementAction(open val progressCost: Int, open val stageName: String) {
    data class SpendOnAdvantage(
        val advantageId: String,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName)

    data class IncreaseAttribute(
        val attributeName: String,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName)

    data class SpendOnSkills(
        val skillsIncreased: List<String>,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName)

    data class RemoveHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName)

    data class ReserveLegendaryAttribute(
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName)
}
