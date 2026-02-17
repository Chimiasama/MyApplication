package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ValidatePrerequisiteUseCase {

    data class Input(
        val vantagem: Vantagem,
        val vantagensSelecionadas: List<Vantagem>,
        val complicacoesSelecionadas: Collection<Complicacao>
    )

    private val ameacadorComplicacoesLiberadoras = setOf(
        "sanguinario",
        "desagradavel",
        "sem_escrupulos",
        "feio",
        "sombrio",
        "sinistro"
    ).map { it.keyify() }.toSet()

    private val ameacadorId = "ameacador".keyify()

    private fun atendePreviasPorComplicacaoParaAmeacador(v: Vantagem, complicacoes: Collection<Complicacao>): Boolean {
        if (v.id.keyify() != ameacadorId) return false

        val requisitadas = v.requisitos.vantagensPrevias.map { it.keyify() }.toSet()
        val liberadoras = (ameacadorComplicacoesLiberadoras + requisitadas)
        val selecionadas = complicacoes.map { it.id.keyify() }.toSet()

        return selecionadas.any { it in liberadoras }
    }

    fun execute(input: Input): Boolean {
        val v = input.vantagem
        if (v.requisitos.vantagensPrevias.isEmpty()) return true

        if (atendePreviasPorComplicacaoParaAmeacador(v, input.complicacoesSelecionadas)) return true

        val faltam = v.requisitos.vantagensPrevias.any { prevId ->
            when (prevId.keyify().replace(" ", "_")) {
                "ANTECEDENTE_ARCANO", "ANTECEDENTE_ARCANO:*" -> {
                    input.vantagensSelecionadas.none { poss ->
                        poss.id.startsWith("antecedente_arcano_") ||
                                poss.id.startsWith("aa_") ||
                                (poss.id == "antecedente_arcano" && !poss.choice.isNullOrBlank())
                    }
                }
                else -> {
                    input.vantagensSelecionadas.none { poss ->
                        poss.id.keyify().replace(" ", "_") == prevId.keyify().replace(" ", "_")
                    }
                }
            }
        }
        return !faltam
    }
}
