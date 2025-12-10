package com.example.swadebuilder.model

import com.example.swadebuilder.toDiceString

enum class HindranceChangeType {
    RESERVATION,
    REDUCE_TO_MINOR,
    REMOVE
}

sealed class AdvancementAction(open val progressCost: Int, open val stageName: String) {
    abstract fun getDisplayText(
        getAdvantageName: (String) -> String,
        getSkillValue: (String) -> Int
    ): String

    data class SpendOnAdvantage(
        val advantageId: String,
        override val stageName: String,
        val arcanoKey: String? = null,
        val previousArcanoSlots: List<String?>? = null,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            return "Vantagem: ${getAdvantageName(advantageId)}"
        }
    }

    data class IncreaseAttribute(
        val attributeName: String,
        val usedLegendaryReservation: Boolean = false,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            return "Atributo: $attributeName"
        }
    }

    data class SpendOnSkills(
        val skillsIncreased: List<String>,
        val recordedSkillValues: Map<String, Int>? = null,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            val uniqueSkills = skillsIncreased.distinct()
            val text = uniqueSkills.joinToString(", ") { skillName ->
                val diceValue = recordedSkillValues?.get(skillName) ?: getSkillValue(skillName)
                "$skillName ${diceValue.toDiceString()}"
            }
            return "Perícias: $text"
        }
    }

    data class RemoveHindrance(
        val hindranceId: String,
        val changeType: HindranceChangeType,
        val previousLevel: String?,
        val usedReservation: Boolean = false,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            val actionLabel = when (changeType) {
                HindranceChangeType.RESERVATION   -> "Reserva de Complicação"
                HindranceChangeType.REDUCE_TO_MINOR -> "Redução de Complicação"
                HindranceChangeType.REMOVE        -> "Remover Complicação"
            }
            return "$actionLabel: $hindranceId"
        }
    }

    data class ReserveLegendaryAttribute(
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            return "Reservar Aumento de Atributo (Lendário)"
        }
    }
}
