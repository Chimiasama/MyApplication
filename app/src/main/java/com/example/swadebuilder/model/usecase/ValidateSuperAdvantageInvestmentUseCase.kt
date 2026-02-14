package com.example.swadebuilder.model.usecase

class ValidateSuperAdvantageInvestmentUseCase {

    data class Input(
        val vantagemIdSolicitada: String,
        val vantagemEncontrada: AdvantageRef?,
        val mensagemBloqueioClasse: String?,
        val jaPossuiVantagem: Boolean,
        val requisitosAtendidosIgnorandoEstagio: Boolean
    )

    data class AdvantageRef(
        val id: String,
        val nome: String
    )

    fun execute(input: Input): String? {
        val vantagem = input.vantagemEncontrada
            ?: return "Vantagem não encontrada: ${input.vantagemIdSolicitada}."

        if (input.mensagemBloqueioClasse != null) {
            return input.mensagemBloqueioClasse
        }

        if (input.jaPossuiVantagem) {
            return "Você já possui a vantagem ${vantagem.nome}."
        }

        if (!input.requisitosAtendidosIgnorandoEstagio) {
            return "Requisitos não atendidos para a vantagem (exceto Estágio)."
        }

        return null
    }
}
