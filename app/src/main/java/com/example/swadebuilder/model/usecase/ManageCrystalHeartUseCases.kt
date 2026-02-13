package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.CrystalHeart

class UpsertCrystalHeartUseCase {
    fun execute(current: List<CrystalHeart>, saved: CrystalHeart): List<CrystalHeart> {
        val updated = current.toMutableList()
        val existingIndex = updated.indexOfFirst { it.id == saved.id }
        if (existingIndex >= 0) {
            updated[existingIndex] = saved
        } else {
            updated.add(saved)
        }
        return updated
    }
}

data class RemoveCrystalHeartResult(
    val updated: List<CrystalHeart>,
    val newSelected: CrystalHeart?
)

class RemoveCrystalHeartUseCase {
    fun execute(
        current: List<CrystalHeart>,
        heartIdToRemove: String,
        currentlySelectedId: String?
    ): RemoveCrystalHeartResult {
        val updated = current.filterNot { it.id == heartIdToRemove }
        val newSelected = if (currentlySelectedId == heartIdToRemove) {
            updated.firstOrNull { it.placeholder }
        } else {
            updated.firstOrNull { it.id == currentlySelectedId }
        }

        return RemoveCrystalHeartResult(updated = updated, newSelected = newSelected)
    }
}
