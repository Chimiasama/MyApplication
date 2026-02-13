package com.example.swadebuilder.model.usecase

class RebuildSkillStacksUseCase {
    fun execute(rebuildAction: () -> Unit) {
        rebuildAction()
    }
}
