package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

class ApplyAncestryUseCase {

    data class Input(
        val newAncestryName: String,
        val previousAncestryName: String,
        val availableAncestries: List<RacialModifier>,
        val allAdvantages: List<Vantagem>,
        val allHindrances: List<Complicacao>,
        val racialAttrMinMap: Map<String, Map<String, Int>>,
        val currentAttributesRaw: Map<String, Int>,
        val currentAttributeStacks: Map<String, List<Int>>,
        val currentSelectedEdges: List<Vantagem>,
        val currentAutoEdges: List<String>,
        val compendioArteDaGuerraAtivo: Boolean = false,
        val signoAdgSelecionado: String? = null
    )

    data class Output(
        val newAttributesRaw: Map<String, Int>,
        val newAttributeStacks: Map<String, List<Int>>,
        val edgesToAdd: List<Vantagem>,
        val edgesToRemove: List<Vantagem>,
        val autoAdvantages: List<String>,
        val autoHindrances: List<String>,
        val racialAdvantages: List<String>,
        val racialHindrances: List<String>,
        val pvAdjustment: Int,
        val paRefunded: Int,
        val naturalArmor: Int,
        val resetSigno: Boolean,
        val feedbackMessages: List<String>
    )

    fun execute(input: Input): Output {
        val messages = mutableListOf<String>()
        val edgesToRemove = mutableListOf<Vantagem>()
        val edgesToAdd = mutableListOf<Vantagem>()
        var pvAdj = 0

        val prevAncDef = getAncestralidadeDef(input.previousAncestryName, input.availableAncestries)
        val newAncDef = getAncestralidadeDef(input.newAncestryName, input.availableAncestries)

        // --- 1. Human (Adaptable) Logic ---
        val wasHumano = (input.previousAncestryName == "HUMANOS" || prevAncDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true)
        val vaiSerHumano = (input.newAncestryName == "HUMANOS" || newAncDef?.vantagensGratis?.any { it.keyify() == "ADAPTAVEL" } == true)

        if (wasHumano && !vaiSerHumano) {
            val prevFreeKeys = getPrevFreeKeys(input.previousAncestryName, input.currentAutoEdges)
            val candidate = findEdgeToRemove(input.currentSelectedEdges, prevFreeKeys)

            if (candidate != null) {
                edgesToRemove.add(candidate)
                messages.add("Vantagem ${candidate.nome} removida para compensar a troca de Ancestralidade.")
            } else {
                pvAdj = -1
            }
        } else if (!wasHumano && vaiSerHumano) {
            pvAdj = 1
        }

        // --- 2. Racial Traits ---
        val prevAutoKeys = getPrevFreeKeys(input.previousAncestryName, input.currentAutoEdges)

        input.currentSelectedEdges.forEach { edge ->
             if (edge.nome.keyify() in prevAutoKeys && edge !in edgesToRemove) {
                 edgesToRemove.add(edge)
             }
        }

        val newRacialAdvantages = newAncDef?.vantagensGratis ?: emptyList()
        val newRacialHindrances = newAncDef?.desvantagens ?: emptyList()

        val newAutoAdvantages = newRacialAdvantages.toMutableList()
        val newAutoHindrances = newRacialHindrances.toMutableList()

        newAncDef?.vantagensGratis?.forEach { featString ->
            val featKey = featString.keyify()
            val edge = input.allAdvantages.firstOrNull {
                it.nome.keyify() == featKey || it.id == featString || it.id.keyify() == featKey
            }
            if (edge != null) {
                val alreadyHas = input.currentSelectedEdges.any { it.id == edge.id } &&
                                 edgesToRemove.none { it.id == edge.id }
                if (!alreadyHas) {
                    edgesToAdd.add(edge)
                }
            }
        }

        var naturalArmor = 0

        when (input.newAncestryName.uppercase()) {
            "SAURIOS" -> {
                naturalArmor = 2
            }
            "PEQUENINOS" -> {
                listOf("Sorte", "Espirituoso").forEach { name ->
                     val edge = input.allAdvantages.firstOrNull { it.nome.equals(name, ignoreCase = true) }
                     if (edge != null && input.currentSelectedEdges.none { it.id == edge.id }) {
                         edgesToAdd.add(edge)
                     }
                }
                if (newAutoHindrances.none { it.contains("Tamanho", ignoreCase = true) }) newAutoHindrances.add("Tamanho -1")
                if (newAutoHindrances.none { it.contains("Movimentação Reduzida", ignoreCase = true) }) newAutoHindrances.add("Movimentação Reduzida")
            }
            "CELESTIAIS" -> {
                val aa = input.allAdvantages.firstOrNull { it.id == "antecedente_arcano_milagres" }
                if (aa != null && input.currentSelectedEdges.none { it.id == aa.id }) {
                    edgesToAdd.add(aa)
                }
                if ("ANTECEDENTE ARCANO (MILAGRES)" !in newAutoAdvantages) {
                    newAutoAdvantages.add("ANTECEDENTE ARCANO (MILAGRES)")
                }
            }
            "HUMANO (WISEGUYS)".keyify() -> {
                 val aa = input.allAdvantages.firstOrNull { it.nome.equals("Conexões (Máfia)", ignoreCase = true) }
                 if (aa != null && input.currentSelectedEdges.none { it.id == aa.id }) {
                     edgesToAdd.add(aa)
                 }
            }
        }

        // --- 3. Attribute Recalculation ---
        val newAttributesRaw = input.currentAttributesRaw.toMutableMap()
        val newAttributeStacks = input.currentAttributeStacks.toMutableMap()
        var paRefundedTotal = 0

        val attributeKeys = input.currentAttributesRaw.keys

        attributeKeys.forEach { attrKey ->
            val min = getRacialAttributeMin(input.newAncestryName, attrKey, input.racialAttrMinMap)
            val max = getRacialAttributeMax(input.newAncestryName, attrKey, input.racialAttrMinMap)

            val stack = input.currentAttributeStacks[attrKey]?.toMutableList() ?: mutableListOf()

            var raw = min
            var keptStackCount = 0

            for (cost in stack) {
                val candidate = if (raw < 12) raw + 2 else raw + 1
                if (candidate > max) {
                    break
                }
                raw = candidate
                keptStackCount++
            }

            if (keptStackCount < stack.size) {
                val removedCount = stack.size - keptStackCount
                paRefundedTotal += removedCount
                newAttributeStacks[attrKey] = stack.take(keptStackCount)
            }

            newAttributesRaw[attrKey] = raw
        }

        var resetSigno = false
        if (input.compendioArteDaGuerraAtivo) {
            val isHumanAdg = input.newAncestryName.keyify().contains("HUMANO")
            if (isHumanAdg && input.signoAdgSelecionado == null) {
                resetSigno = true
            } else if (!isHumanAdg && input.signoAdgSelecionado != null) {
                resetSigno = true
            }
        }

        return Output(
            newAttributesRaw = newAttributesRaw,
            newAttributeStacks = newAttributeStacks,
            edgesToAdd = edgesToAdd,
            edgesToRemove = edgesToRemove,
            autoAdvantages = newAutoAdvantages,
            autoHindrances = newAutoHindrances,
            racialAdvantages = newRacialAdvantages,
            racialHindrances = newRacialHindrances,
            pvAdjustment = pvAdj,
            paRefunded = paRefundedTotal,
            naturalArmor = naturalArmor,
            resetSigno = resetSigno,
            feedbackMessages = messages
        )
    }

    private fun getAncestralidadeDef(name: String, list: List<RacialModifier>): RacialModifier? {
        return list.firstOrNull { it.nome.keyify() == name.keyify() }
    }

    private fun getPrevFreeKeys(ancestryName: String, currentAutoEdges: List<String>): Set<String> {
        val keys = currentAutoEdges.map { it.keyify() }.toMutableSet()
        when (ancestryName.uppercase()) {
            "SAURIOS" -> keys.addAll(listOf("Sentidos Aguçados", "Prontidão").map { it.keyify() })
            "PEQUENINOS" -> keys.add("Sorte".keyify())
            "CELESTIAIS" -> keys.addAll(listOf("ANTECEDENTE ARCANO MILAGRES", "ANTECEDENTE ARCANO (MILAGRES)").map { it.keyify() })
        }
        return keys
    }

    private fun findEdgeToRemove(currentEdges: List<Vantagem>, autoKeys: Set<String>): Vantagem? {
        val candidates = currentEdges.filter { v ->
            val isScenarioEdge = v.id == "superpoderes" ||
                    v.id == "agente_syn" ||
                    v.id == "aa_agente_syn" ||
                    (v.id == "conexoes" && v.choice?.equals("Máfia", ignoreCase = true) == true)

            val isRacialFree = v.nome.keyify() in autoKeys

            !isRacialFree && !isScenarioEdge && v.categoria.name != "PODER" && !isUsedAsPrereq(v, currentEdges)
        }

        return candidates.lastOrNull()
    }

    private fun isUsedAsPrereq(v: Vantagem, allEdges: List<Vantagem>): Boolean {
        return allEdges.any { other ->
            if (other == v) return@any false
            other.requisitos.vantagensPrevias.any { prevId ->
                when (prevId.keyify()) {
                    "antecedente_arcano", "antecedente_arcano:*" -> {
                        v.id.startsWith("antecedente_arcano_") ||
                        (v.id == "antecedente_arcano" && !v.choice.isNullOrBlank())
                    }
                    else -> {
                        v.id.keyify() == prevId.keyify()
                    }
                }
            }
        }
    }

    private fun getRacialAttributeMin(ancestry: String, attrKey: String, racialAttrMinMap: Map<String, Map<String, Int>>): Int {
        return racialAttrMinMap[ancestry.keyify()]?.get(attrKey.keyify()) ?: 4
    }

    private fun getRacialAttributeMax(ancestry: String, attrKey: String, racialAttrMinMap: Map<String, Map<String, Int>>): Int {
        val min = getRacialAttributeMin(ancestry, attrKey, racialAttrMinMap)
        val extra = (min - 4).coerceAtLeast(0) / 2
        return 12 + extra
    }
}
