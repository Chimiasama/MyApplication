package com.example.swadebuilder.model

import com.example.swadebuilder.toDiceString

sealed class AdvancementAction(open val progressCost: Int, open val stageName: String) {
    abstract fun getDisplayText(
        getAdvantageName: (String) -> String,
        getSkillValue: (String) -> Int,
        getHindranceName: (String) -> String
    ): String

    data class SpendOnAdvantage(
        val advantageId: String,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
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
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
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
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            val uniqueSkills = skillsIncreased.distinct()
            val text = uniqueSkills.joinToString(", ") { skillName ->
                val diceValue = getSkillValue(skillName)
                "$skillName ${diceValue.toDiceString()}"
            }
            return "Perícias: $text"
        }
    }

    data class RemoveMinorHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            return "Remover Complicação Menor: ${getHindranceName(hindranceId)}"
        }
    }

    data class RemoveMajorHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            return "Remover Complicação Maior: ${getHindranceName(hindranceId)}"
        }
    }

    data class ReserveRemoveMajorHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            return "Reservar Remoção: ${getHindranceName(hindranceId)}"
        }
    }

    data class ReduceMajorHindrance(
        val hindranceId: String,
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            return "Reduzir Complicação: ${getHindranceName(hindranceId)}"
        }
    }

    data class ReserveLegendaryAttribute(
        override val stageName: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost, stageName) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getSkillValue: (String) -> Int,
            getHindranceName: (String) -> String
        ): String {
            return "Reservar Aumento de Atributo (Lendário)"
        }
    }
}
