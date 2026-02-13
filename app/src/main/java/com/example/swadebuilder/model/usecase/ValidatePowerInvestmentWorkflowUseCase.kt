package com.example.swadebuilder.model.usecase

class ValidatePowerInvestmentWorkflowUseCase(
    private val validatePowerInvestmentUseCase: ValidatePowerInvestmentUseCase = ValidatePowerInvestmentUseCase(),
    private val validateSuperAttributeInvestmentUseCase: ValidateSuperAttributeInvestmentUseCase = ValidateSuperAttributeInvestmentUseCase(),
    private val validateSuperAdvantageInvestmentUseCase: ValidateSuperAdvantageInvestmentUseCase = ValidateSuperAdvantageInvestmentUseCase(),
    private val validateSpecialPowerRequirementsUseCase: ValidateSpecialPowerRequirementsUseCase = ValidateSpecialPowerRequirementsUseCase()
) {

    sealed interface EffectInput {
        data class SuperAtributo(
            val currentRaw: Int,
            val steps: Int,
            val applySteps: (Int, Int) -> Int
        ) : EffectInput

        data class SuperVantagem(
            val vantagemIdSolicitada: String,
            val vantagemNome: String?,
            val mensagemBloqueioClasse: String?,
            val jaPossuiVantagem: Boolean,
            val requisitosAtendidosIgnorandoEstagio: Boolean
        ) : EffectInput

        data class Generico(
            val effectNameKey: String,
            val ocultismoRaw: Int?,
            val cienciaRaw: Int?
        ) : EffectInput

        data object Other : EffectInput
    }

    data class Input(
        val poderId: String,
        val custo: Int,
        val superPontosDisponiveis: Int,
        val gastosPorPoder: Map<String, Int>,
        val limitePorPoder: Int,
        val limiteCompartilhadoArmaduraResistencia: Int,
        val effect: EffectInput
    )

    fun execute(input: Input): String? {
        val erroBasico = validatePowerInvestmentUseCase.execute(
            ValidatePowerInvestmentUseCase.Input(
                poderId = input.poderId,
                custo = input.custo,
                superPontosDisponiveis = input.superPontosDisponiveis,
                gastosPorPoder = input.gastosPorPoder,
                limitePorPoder = input.limitePorPoder,
                limiteCompartilhadoArmaduraResistencia = input.limiteCompartilhadoArmaduraResistencia
            )
        )
        if (erroBasico != null) return erroBasico

        return when (val effect = input.effect) {
            is EffectInput.SuperAtributo -> {
                validateSuperAttributeInvestmentUseCase.execute(
                    ValidateSuperAttributeInvestmentUseCase.Input(
                        currentRaw = effect.currentRaw,
                        steps = effect.steps,
                        applySteps = effect.applySteps
                    )
                )
            }

            is EffectInput.SuperVantagem -> {
                validateSuperAdvantageInvestmentUseCase.execute(
                    ValidateSuperAdvantageInvestmentUseCase.Input(
                        vantagemIdSolicitada = effect.vantagemIdSolicitada,
                        vantagemEncontrada = effect.vantagemNome?.let {
                            ValidateSuperAdvantageInvestmentUseCase.AdvantageRef(
                                id = effect.vantagemIdSolicitada,
                                nome = it
                            )
                        },
                        mensagemBloqueioClasse = effect.mensagemBloqueioClasse,
                        jaPossuiVantagem = effect.jaPossuiVantagem,
                        requisitosAtendidosIgnorandoEstagio = effect.requisitosAtendidosIgnorandoEstagio
                    )
                )
            }

            is EffectInput.Generico -> {
                validateSpecialPowerRequirementsUseCase.execute(
                    ValidateSpecialPowerRequirementsUseCase.Input(
                        effectNameKey = effect.effectNameKey,
                        ocultismoRaw = effect.ocultismoRaw,
                        cienciaRaw = effect.cienciaRaw
                    )
                )
            }

            EffectInput.Other -> null
        }
    }
}
