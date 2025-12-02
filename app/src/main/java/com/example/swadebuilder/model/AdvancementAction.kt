package com.example.swadebuilder.model

sealed class AdvancementAction(open val progressCost: Int) {
    data class SpendOnAdvantage(
        val advantageId: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost)

    data class IncreaseAttribute(
        val attributeName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost)

    data class SpendOnSkills(
        val skillsIncreased: List<String>,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost)

    data class RemoveHindrance(
        val hindranceId: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost)
}
