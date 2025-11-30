package com.example.swadebuilder.model

sealed class AdvancementAction {
    data class SpendOnAdvantage(val advantageId: String) : AdvancementAction()
    data class IncreaseAttribute(val attributeName: String) : AdvancementAction()
    data class SpendOnSkills(val skillsIncreased: List<String>) : AdvancementAction()
    data class RemoveHindrance(val hindranceId: String) : AdvancementAction()
}
