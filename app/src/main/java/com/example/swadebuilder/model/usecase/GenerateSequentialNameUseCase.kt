package com.example.swadebuilder.model.usecase

class GenerateSequentialNameUseCase(
    private val defaultName: String = "Nome"
) {
    fun execute(
        baseName: String,
        existingNames: List<String>,
        usarParenteses: Boolean
    ): String {
        val normalizedExisting = existingNames.map { it.lowercase() }.toSet()
        val desiredBase = baseName.ifBlank { defaultName }

        if (!normalizedExisting.contains(desiredBase.lowercase())) {
            return desiredBase
        }

        var counter = 2
        var candidate: String

        do {
            candidate = if (usarParenteses) {
                "$desiredBase ($counter)"
            } else {
                "$desiredBase $counter"
            }
            counter++
        } while (normalizedExisting.contains(candidate.lowercase()))

        return candidate
    }
}
