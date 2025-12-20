package com.example.swadebuilder

import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.util.keyify
import kotlin.math.max
import kotlin.math.min

enum class ModifierTarget {
    SIZE_DISPLAY,
    SIZE_TOUGHNESS,
    TOUGHNESS_FLAT,
    PACE_FLAT
}

enum class StackRule {
    ADD,
    MAX,
    MIN,
    OVERRIDE
}

enum class SourceType {
    ANCESTRALIDADE,
    COMPLICACAO,
    VANTAGEM,
    OUTRO
}

data class Modifier(
    val id: String,
    val sourceType: SourceType,
    val sourceName: String,
    val target: ModifierTarget,
    val value: Int,
    val stackRule: StackRule = StackRule.ADD
)

object ModifierEngine {
    fun collect(state: CriadorState): List<Modifier> {
        val modifiers = mutableListOf<Modifier>()
        val ancestral = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == state.ancestralidade }
        val racialSize = parseRacialSize(ancestral?.desvantagens)
        if (racialSize != 0) {
            val sourceName = ancestral?.nome ?: state.ancestralidade
            addSizeModifiers(
                modifiers,
                id = "racial_size_${state.ancestralidade.keyify()}",
                sourceType = SourceType.ANCESTRALIDADE,
                sourceName = sourceName,
                value = racialSize
            )
        }

        state.complicacoesSelecionadas.keys
            .firstOrNull { it.id.keyify() == "OBESO" }
            ?.let { comp ->
                addSizeModifiers(
                    modifiers,
                    id = "obeso_complicacao",
                    sourceType = SourceType.COMPLICACAO,
                    sourceName = comp.name,
                    value = 1
                )
            }

        state.complicacoesSelecionadas.keys
            .firstOrNull { it.id.keyify() == "PEQUENO" }
            ?.let { comp ->
                addSizeModifiers(
                    modifiers,
                    id = "pequeno_complicacao",
                    sourceType = SourceType.COMPLICACAO,
                    sourceName = comp.name,
                    value = -1
                )
            }

        state.vantagensSelecionadas
            .firstOrNull { it.nome.keyify() == "MUSCULOSO" }
            ?.let { vantagem ->
                addSizeModifiers(
                    modifiers,
                    id = "musculoso_vantagem",
                    sourceType = SourceType.VANTAGEM,
                    sourceName = vantagem.nome,
                    value = 1
                )
            }

        val resistenciaAuto = state.vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" }
        if (resistenciaAuto) {
            modifiers += Modifier(
                id = "resistencia_auto",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Resistência",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = 1
            )
        }

        val fragilAuto = state.desvantagensAutomaticas.any { it.keyify() == "FRAGIL" } ||
            state.desvantagensRaciais.any { it.keyify() == "FRAGIL" }
        if (fragilAuto) {
            modifiers += Modifier(
                id = "fragil",
                sourceType = SourceType.COMPLICACAO,
                sourceName = "Frágil",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = -1
            )
        }

        val brigaoCount = state.vantagensSelecionadas
            .count { it.nome.keyify() in listOf("BRIGAO", "PUGILISTA") }
        if (brigaoCount > 0) {
            modifiers += Modifier(
                id = "brigao_pugilista",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Brigão/Pugilista",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = brigaoCount
            )
        }

        val brawnyBonus = state.vantagensSelecionadas.any {
            val nk = it.nome.keyify()
            nk == "BRUTAMONTES" || nk == "BRAWNY"
        }
        if (brawnyBonus) {
            modifiers += Modifier(
                id = "brutamontes",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Brutamontes",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = 1
            )
        }

        return modifiers
    }

    fun collect(personagem: MeuPersonagem): List<Modifier> {
        val modifiers = mutableListOf<Modifier>()
        val ancestral = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
        val racialSize = parseRacialSize(ancestral?.desvantagens)
        if (racialSize != 0) {
            val sourceName = ancestral?.nome ?: personagem.ancestralidade
            addSizeModifiers(
                modifiers,
                id = "racial_size_${personagem.ancestralidade.keyify()}",
                sourceType = SourceType.ANCESTRALIDADE,
                sourceName = sourceName,
                value = racialSize
            )
        }

        if (personagem.complicacoes.any { it.keyify() == "OBESO" }) {
            addSizeModifiers(
                modifiers,
                id = "obeso_complicacao",
                sourceType = SourceType.COMPLICACAO,
                sourceName = complicationName("OBESO"),
                value = 1
            )
        }

        if (personagem.complicacoes.any { it.keyify() == "PEQUENO" }) {
            addSizeModifiers(
                modifiers,
                id = "pequeno_complicacao",
                sourceType = SourceType.COMPLICACAO,
                sourceName = complicationName("PEQUENO"),
                value = -1
            )
        }

        val vantagemKeys = personagem.vantagens.map { vantagemKey(it) }
        val musculosoIndex = vantagemKeys.indexOfFirst { it == "MUSCULOSO" }
        if (musculosoIndex >= 0) {
            val sourceName = vantagemName(personagem.vantagens[musculosoIndex])
            addSizeModifiers(
                modifiers,
                id = "musculoso_vantagem",
                sourceType = SourceType.VANTAGEM,
                sourceName = sourceName,
                value = 1
            )
        }

        if (vantagemKeys.any { it == "RESISTENCIA" }) {
            modifiers += Modifier(
                id = "resistencia_vantagem",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Resistência",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = 1
            )
        }

        val allComplicationKeys =
            personagem.complicacoes + personagem.desvantagensRaciais + personagem.transtornos
        if (allComplicationKeys.any { it.keyify() == "FRAGIL" }) {
            modifiers += Modifier(
                id = "fragil",
                sourceType = SourceType.COMPLICACAO,
                sourceName = "Frágil",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = -1
            )
        }

        val brigaoCount = vantagemKeys.count { it in listOf("BRIGAO", "PUGILISTA") }
        if (brigaoCount > 0) {
            modifiers += Modifier(
                id = "brigao_pugilista",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Brigão/Pugilista",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = brigaoCount
            )
        }

        if (vantagemKeys.any { it == "BRUTAMONTES" || it == "BRAWNY" }) {
            modifiers += Modifier(
                id = "brutamontes",
                sourceType = SourceType.VANTAGEM,
                sourceName = "Brutamontes",
                target = ModifierTarget.TOUGHNESS_FLAT,
                value = 1
            )
        }

        return modifiers
    }

    fun sum(state: CriadorState, target: ModifierTarget): Int =
        sum(collect(state), target)

    fun sum(personagem: MeuPersonagem, target: ModifierTarget): Int =
        sum(collect(personagem), target)

    fun sizeRawDisplay(state: CriadorState): Int =
        sum(state, ModifierTarget.SIZE_DISPLAY)

    fun sizeDisplay(state: CriadorState): Int =
        sizeRawDisplay(state).coerceIn(-1, 3)

    fun sizeForToughness(state: CriadorState): Int =
        sum(state, ModifierTarget.SIZE_TOUGHNESS)

    fun sizeRawDisplay(personagem: MeuPersonagem): Int =
        sum(personagem, ModifierTarget.SIZE_DISPLAY)

    fun sizeDisplay(personagem: MeuPersonagem): Int =
        sizeRawDisplay(personagem).coerceIn(-1, 3)

    fun sizeForToughness(personagem: MeuPersonagem): Int =
        sum(personagem, ModifierTarget.SIZE_TOUGHNESS)

    fun toughnessBase(state: CriadorState): Int {
        val vigorRaw = state.valoresAtributos["VIGOR"]?.intValue ?: 4
        val base = 2 + (vigorRaw / 2)
        return (base + sizeForToughness(state) + sum(state, ModifierTarget.TOUGHNESS_FLAT))
            .coerceAtLeast(0)
    }

    fun toughnessBase(personagem: MeuPersonagem): Int {
        val vigorRaw = personagem.atributos["VIGOR"] ?: 4
        val base = 2 + (vigorRaw / 2)
        return (base + sizeForToughness(personagem) + sum(personagem, ModifierTarget.TOUGHNESS_FLAT))
            .coerceAtLeast(0)
    }

    fun breakdown(state: CriadorState, target: ModifierTarget): List<Modifier> =
        collect(state).filter { it.target == target }

    fun breakdown(personagem: MeuPersonagem, target: ModifierTarget): List<Modifier> =
        collect(personagem).filter { it.target == target }

    private fun sum(modifiers: List<Modifier>, target: ModifierTarget): Int {
        val relevant = modifiers.filter { it.target == target }
        if (relevant.isEmpty()) {
            return 0
        }
        var addSum = 0
        var maxValue: Int? = null
        var minValue: Int? = null
        var overrideValue: Int? = null

        for (modifier in relevant) {
            when (modifier.stackRule) {
                StackRule.ADD -> addSum += modifier.value
                StackRule.MAX -> maxValue = if (maxValue == null) {
                    modifier.value
                } else {
                    max(maxValue, modifier.value)
                }
                StackRule.MIN -> minValue = if (minValue == null) {
                    modifier.value
                } else {
                    min(minValue, modifier.value)
                }
                StackRule.OVERRIDE -> overrideValue = modifier.value
            }
        }

        if (overrideValue != null) {
            return overrideValue
        }

        var result = addSum
        if (maxValue != null) {
            result = max(result, maxValue)
        }
        if (minValue != null) {
            result = min(result, minValue)
        }
        return result
    }

    private fun addSizeModifiers(
        modifiers: MutableList<Modifier>,
        id: String,
        sourceType: SourceType,
        sourceName: String,
        value: Int
    ) {
        modifiers += Modifier(
            id = "${id}_display",
            sourceType = sourceType,
            sourceName = sourceName,
            target = ModifierTarget.SIZE_DISPLAY,
            value = value
        )
        modifiers += Modifier(
            id = "${id}_toughness",
            sourceType = sourceType,
            sourceName = sourceName,
            target = ModifierTarget.SIZE_TOUGHNESS,
            value = value
        )
    }

    private fun parseRacialSize(desvantagens: List<String>?): Int {
        return desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
            ?.substringAfter("TAMANHO")
            ?.trim()
            ?.toIntOrNull()
            ?: 0
    }

    private fun vantagemKey(id: String): String {
        return listaVantagens.firstOrNull { it.id == id }?.nome?.keyify() ?: id.keyify()
    }

    private fun vantagemName(id: String): String {
        return listaVantagens.firstOrNull { it.id == id }?.nome ?: id
    }

    private fun complicationName(id: String): String {
        return listaComplicacoes.firstOrNull { it.id.keyify() == id.keyify() }?.name ?: id
    }
}
