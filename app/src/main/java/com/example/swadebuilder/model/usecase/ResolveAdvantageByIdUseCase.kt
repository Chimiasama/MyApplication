package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem

class ResolveAdvantageByIdUseCase {
    fun execute(vantagens: List<Vantagem>, vantagemId: String): Vantagem? {
        return vantagens.firstOrNull { it.id.equals(vantagemId, ignoreCase = true) }
    }
}
