package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos

class ResolveAncestryTransitionContextUseCase {

    data class Params(
        val previousAncestry: String,
        val targetAncestry: String,
        val previousAncestryDef: RacialModifier?,
        val targetAncestryDef: RacialModifier?,
        val currentAutomaticAdvantages: List<String>,
        val meioElfoAgil: Boolean // Added flag
    )

    data class Result(
        val wasHumano: Boolean,
        val willBeHumano: Boolean,
        val previousFreeAdvantageKeys: Set<String>
    )

    fun execute(params: Params): Result {
        val previousAncestryKey = params.previousAncestry.keyify()
        val targetAncestryKey = params.targetAncestry.keyify()

        val wasHumano = previousAncestryKey == "HUMANOS" ||
            params.previousAncestryDef?.hasAdaptable() == true ||
            (previousAncestryKey == "MEIO-ELFOS" && !params.meioElfoAgil) // Half-Elf without Agile = Adaptable

        val willBeHumano = targetAncestryKey == "HUMANOS" ||
            params.targetAncestryDef?.hasAdaptable() == true

        val previousFreeAdvantageKeys = (
            params.currentAutomaticAdvantages.toSet() +
                when {
                    previousAncestryKey == "SAURIOS" -> setOf("Sentidos Aguçados", "Prontidão")
                    previousAncestryKey == "PEQUENINOS" -> setOf("Sorte")
                    previousAncestryKey == "CELESTIAIS" -> setOf("ANTECEDENTE ARCANO MILAGRES", "ANTECEDENTE ARCANO (MILAGRES)")
                    previousAncestryKey.contains("DEMONIO") -> setOf("AA_DEMONIO", "ANTECEDENTE ARCANO DEMONIO", "ANTECEDENTE ARCANO (DEMONIO)")
                    previousAncestryKey == "TRANSMORFOS" -> setOf("ANTECEDENTE ARCANO (DOM)", "ANTECEDENTE ARCANO DOM")
                    previousAncestryKey == "ANOES" -> setOf("CIBERTOLERÂNCIA")
                    else -> emptySet()
                }
            ).flatMap { advantageName ->
                listOf(advantageName.keyify(), advantageName.toLegacyKey())
            }
            .toSet()

        return Result(
            wasHumano = wasHumano,
            willBeHumano = willBeHumano,
            previousFreeAdvantageKeys = previousFreeAdvantageKeys
        )
    }

    private fun String.toLegacyKey(): String =
        trim()
            .lowercase()
            .semAcentos()
            .replace("(", "")
            .replace(")", "")
            .replace(Regex("\\s+"), "_")

    private fun RacialModifier.hasAdaptable(): Boolean {
        // Check new structure (habilidades ID/Name) or legacy (vantagensGratis)
        return habilidades.any { it.id == "ADAPTAVEL" || it.nome.keyify() == "ADAPTAVEL" } ||
            vantagensGratis.any { it.keyify() == "ADAPTAVEL" }
    }
}
