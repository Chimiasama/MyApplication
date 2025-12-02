package com.example.swadebuilder.model

sealed class AdvancementAction(open val progressCost: Int) {
    abstract fun getDisplayText(
        getAdvantageName: (String) -> String,
        getHindranceName: (String) -> String
    ): String

    data class SpendOnAdvantage(
        val advantageId: String,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getHindranceName: (String) -> String
        ): String {
            return "Vantagem: ${getAdvantageName(advantageId)}"
        }
    }

    data class IncreaseAttribute(
        val attributeName: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getHindranceName: (String) -> String
        ): String {
            return "Atributo: $attributeName"
        }
    }

    data class SpendOnSkills(
        val skillsIncreased: List<String>,
        override val progressCost: Int = 1
    ) : AdvancementAction(progressCost) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getHindranceName: (String) -> String
        ): String {
            // Limita a exibição para não poluir a UI se muitas perícias forem aumentadas
            val skillsToShow = skillsIncreased.take(2)
            val displayText = skillsToShow.joinToString()
            return if (skillsIncreased.size > 2) {
                "Perícias: $displayText..."
            } else {
                "Perícias: $displayText"
            }
        }
    }

    data class RemoveHindrance(
        val hindranceId: String,
        override val progressCost: Int
    ) : AdvancementAction(progressCost) {
        override fun getDisplayText(
            getAdvantageName: (String) -> String,
            getHindranceName: (String) -> String
        ): String {
            return "Removeu: ${getHindranceName(hindranceId)}"
        }
    }
}
