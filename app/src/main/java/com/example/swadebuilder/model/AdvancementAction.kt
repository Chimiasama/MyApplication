package com.example.swadebuilder.model

import com.example.swadebuilder.toDiceString

sealed class AdvancementAction(open val progressCost: Int, open val stageName: String) {
    abstract fun getDisplayText(
        getAdvantageName: (String) -> String,
        getSkillValue: (String) -> Int
    ): String

    data class SpendOnAdvantage(
        val advantageId: String,
        override val stageName: String,
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
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            val uniqueSkills = skillsIncreased.distinct()
            val text = uniqueSkills.joinToString(", ") { skillName ->
                val diceValue = getSkillValue(skillName)
                "$skillName ${diceValue.toDiceString()}"
            }
            return "Perícias: $text"
        }
    }

    data class RemoveHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int
        ): String {
            return "Remover Complicação: $hindranceId" // Substituir por nome se disponível
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
