package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.Estagio
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperInvestment
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ValidateSelectionUseCase(
    private val validateScenarioRulesUseCase: ValidateScenarioRulesUseCase = ValidateScenarioRulesUseCase(),
    private val validateRequirementsUseCase: ValidateRequirementsUseCase = ValidateRequirementsUseCase(),
    private val validateConflictsUseCase: ValidateConflictsUseCase = ValidateConflictsUseCase(),
    private val validatePowerPointsLimitUseCase: ValidatePowerPointsLimitUseCase = ValidatePowerPointsLimitUseCase(),
    private val validateSpecialRulesUseCase: ValidateSpecialRulesUseCase = ValidateSpecialRulesUseCase(),
    private val validatePrerequisiteUseCase: ValidatePrerequisiteUseCase = ValidatePrerequisiteUseCase()
) {

    data class Context(
        val ancestralidade: String,
        val ancestralidadeDef: RacialModifier?,
        val compendioCrystalHeartAtivo: Boolean,
        val compendioFantasiaAtivo: Boolean,
        val compendioPathfinderAtivo: Boolean,
        val compendioHorrorAtivo: Boolean,
        val compendioArteDaGuerraAtivo: Boolean,
        val valoresAtributos: Map<String, Int>,
        val pericias: List<Pericia>,
        val rawTotalPericia: (Pericia) -> Int,
        val tipoMonstroSelecionado: String?,
        val cartaSelvagem: Boolean,
        val complicacoesSelecionadas: Map<Complicacao, String?>,
        val ppPurchasesThisRank: Int,
        val maxPpPurchasesAllowed: Int,
        val currentSelectionCount: Int,
        val vantagensSelecionadas: List<Vantagem>,
        val emProgresso: Boolean,
        val superInvestments: List<SuperInvestment>,
        val listaAtributos: List<String>,
        val atributoMaxRaw: (String) -> Int,
        val periciaCapRaw: (Pericia) -> Int,
        val permiteMultiAntecedenteArcano: Boolean,
        val estagioAtual: Estagio,
        val listaDeEstagios: List<Estagio>,
        val overrideStageForVantagem: String?,
        val effectiveProgressoParaVantagens: Int,
        val nivelParaEstagio: Map<String, Estagio>,
        val nasceUmHeroi: Boolean,
        val pvFromXpOutstanding: Int,
        val tropoSelecionadoId: String?,
        val getBestPericia: (String) -> Pericia?
    )

    fun execute(vantagem: Vantagem, context: Context): Boolean {

        // 1. Scenario Rules
        if (!validateScenarioRulesUseCase.execute(
                ValidateScenarioRulesUseCase.Input(
                    vantagem,
                    context.ancestralidade,
                    context.compendioCrystalHeartAtivo,
                    context.compendioFantasiaAtivo,
                    context.compendioPathfinderAtivo
                )
            )) return false

        // 2. Requirements (Attributes, Skills, Rank via Special, etc)
        if (!validateRequirementsUseCase.execute(
                ValidateRequirementsUseCase.Input(
                    vantagem,
                    context.valoresAtributos,
                    context.pericias,
                    context.rawTotalPericia,
                    context.ancestralidadeDef,
                    context.tipoMonstroSelecionado,
                    context.cartaSelvagem
                )
            )) return false

        // 3. Prerequisites (Previous Edges)
        if (!validatePrerequisiteUseCase.execute(
                ValidatePrerequisiteUseCase.Input(
                    vantagem,
                    context.vantagensSelecionadas,
                    context.complicacoesSelecionadas.keys.toList()
                )
            )) return false

        // 4. Conflicts
        if (!validateConflictsUseCase.execute(
                ValidateConflictsUseCase.Input(
                    vantagem,
                    context.complicacoesSelecionadas
                )
            )) return false

        // 5. Purchase Limits
        if (!validatePowerPointsLimitUseCase.execute(
                ValidatePowerPointsLimitUseCase.Input(
                    vantagem,
                    context.ppPurchasesThisRank,
                    context.maxPpPurchasesAllowed,
                    context.currentSelectionCount
                )
            )) return false

        // 6. Special Rules (Rank, Professional, etc)
        if (!validateSpecialRulesUseCase.execute(
                ValidateSpecialRulesUseCase.Input(
                    vantagem,
                    context.vantagensSelecionadas,
                    context.complicacoesSelecionadas,
                    context.emProgresso,
                    context.superInvestments,
                    context.listaAtributos,
                    context.valoresAtributos,
                    context.atributoMaxRaw,
                    context.pericias,
                    context.rawTotalPericia,
                    context.periciaCapRaw,
                    context.permiteMultiAntecedenteArcano,
                    context.compendioFantasiaAtivo,
                    context.compendioHorrorAtivo,
                    context.compendioPathfinderAtivo,
                    context.compendioCrystalHeartAtivo,
                    context.estagioAtual,
                    context.listaDeEstagios,
                    context.overrideStageForVantagem,
                    context.effectiveProgressoParaVantagens,
                    context.nivelParaEstagio,
                    context.nasceUmHeroi,
                    context.pvFromXpOutstanding,
                    context.compendioArteDaGuerraAtivo,
                    context.tropoSelecionadoId,
                    context.getBestPericia
                )
            )) return false

        return true
    }
}
