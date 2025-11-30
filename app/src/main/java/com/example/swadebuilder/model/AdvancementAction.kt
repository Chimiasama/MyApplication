package com.example.swadebuilder.model

sealed class AdvancementAction {
    data class GainAdvantage(val advantageId: String) : AdvancementAction()
    data class IncreaseAttribute(val attributeName: String) : AdvancementAction()
    data class IncreaseSkill(val skillName: String, val isHigh: Boolean) : AdvancementAction()
    data class RemoveHindrance(val hindranceId: String) : AdvancementAction()
}
