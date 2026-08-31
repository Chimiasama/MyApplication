package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.IncompatibilityRules
import com.example.swadebuilder.model.Vantagem

class ValidateConflictsUseCase {

    data class Input(
        val vantagem: Vantagem,
        val complicacoesSelecionadas: Map<Complicacao, String?>
    )

    fun execute(input: Input): Boolean {
        // 14) Conflitos com complicações
        val compsConfl = IncompatibilityRules.complicacoesIncompativeisCom(input.vantagem.id)
        if (input.complicacoesSelecionadas.keys.any { it.id in compsConfl }) return false

        return true
    }
}
