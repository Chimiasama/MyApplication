package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ApplyHumanAncestryTransitionUseCase {

    data class Params(
        val wasHumano: Boolean,
        val vaiSerHumano: Boolean,
        val forceLoseHumanBonus: Boolean = false,
        val pontosVantagemAtuais: Int,
        val vantagensSelecionadas: List<Vantagem>,
        val prevFreeKeys: Set<String>
    )

    data class Result(
        val novosPontosVantagem: Int,
        val vantagemRemovida: Vantagem? = null
    )

    fun execute(params: Params): Result {
        val shouldLoseHumanBonus = params.wasHumano && (!params.vaiSerHumano || params.forceLoseHumanBonus)

        if (shouldLoseHumanBonus) {
            val candidatos = params.vantagensSelecionadas.filter { vantagem ->
                !isRacialFree(vantagem, params.prevFreeKeys) &&
                    !isUsedAsPrereq(vantagem, params.vantagensSelecionadas) &&
                    !isScenarioEdge(vantagem) &&
                    !vantagem.categoria.name.equals("PODER", ignoreCase = true)
            }

            val removida = candidatos.lastOrNull()
            if (removida != null) {
                return Result(
                    novosPontosVantagem = params.pontosVantagemAtuais,
                    vantagemRemovida = removida
                )
            }

            return Result(
                novosPontosVantagem = (params.pontosVantagemAtuais - 1).coerceAtLeast(0)
            )
        }

        if (!params.wasHumano && params.vaiSerHumano) {
            return Result(novosPontosVantagem = params.pontosVantagemAtuais + 1)
        }

        return Result(novosPontosVantagem = params.pontosVantagemAtuais)
    }

    private fun isRacialFree(vantagem: Vantagem, prevFreeKeys: Set<String>): Boolean =
        vantagem.nome.keyify() in prevFreeKeys

    private fun isUsedAsPrereq(vantagem: Vantagem, selecionadas: List<Vantagem>): Boolean =
        selecionadas.any { other ->
            other != vantagem && other.requisitos.vantagensPrevias.any { prevId ->
                when (prevId.keyify().replace(" ", "_")) {
                    "ANTECEDENTE_ARCANO", "ANTECEDENTE_ARCANO:*" -> {
                        other.id.startsWith("antecedente_arcano_") ||
                            other.id.startsWith("aa_") ||
                            (other.id == "antecedente_arcano" && !other.choice.isNullOrBlank())
                    }

                    else -> other.id.keyify().replace(" ", "_") == prevId.keyify().replace(" ", "_")
                }
            }
        }

    private fun isScenarioEdge(vantagem: Vantagem): Boolean =
        vantagem.id == "superpoderes" ||
            vantagem.id == "agente_syn" ||
            vantagem.id == "aa_agente_syn" ||
            (vantagem.id == "conexoes" && vantagem.choice?.equals("Máfia", ignoreCase = true) == true)
}
