package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Estagio
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
        val getBestPericia: (String) -> Pericia?,
        val modoLivre: Boolean
    )

    fun execute(vantagem: Vantagem, context: Context): Boolean {
        if (context.modoLivre) return true

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
                    vantagem = vantagem,
                    valoresAtributos = context.valoresAtributos,
                    pericias = context.pericias,
                    rawTotalPericia = context.rawTotalPericia,
                    ancestralidadeDef = context.ancestralidadeDef,
                    tipoMonstroSelecionado = context.tipoMonstroSelecionado,
                    cartaSelvagem = context.cartaSelvagem,
                    getBestPericia = context.getBestPericia
                )
            )) return false

        // 3. Prerequisites (Previous Edges)
        if (!validatePrerequisiteUseCase.execute(
                ValidatePrerequisiteUseCase.Input(
                    vantagem,
                    context.vantagensSelecionadas,
                    context.complicacoesSelecionadas.keys
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
        val currentSelectionCount = context.vantagensSelecionadas.count { it.id.keyify() == vantagem.id.keyify() }
        if (!validatePowerPointsLimitUseCase.execute(
                ValidatePowerPointsLimitUseCase.Input(
                    vantagem,
                    context.ppPurchasesThisRank,
                    context.maxPpPurchasesAllowed,
                    currentSelectionCount
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
