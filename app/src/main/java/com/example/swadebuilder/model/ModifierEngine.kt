package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.listaAncestralidadesJson
import com.example.swadebuilder.util.keyify

enum class ModifierTarget {
    SIZE_DISPLAY,
    SIZE_TOUGHNESS,
    TOUGHNESS_FLAT,
    ARMOR
}

enum class StackRule {
    ADD, MAX, MIN, OVERRIDE
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

        // 1. Equipamento (Armadura)
        state.equipamentosComprados.forEach { item ->
            val armorVal = (item.armadura as? kotlinx.serialization.json.JsonPrimitive)
                ?.content?.toIntOrNull() ?: 0

            if (armorVal > 0) {
                // Checa se é item de Mecha/Veículo que não deve somar
                val isMechaOrVehicle = item.subtipo?.uppercase()?.let { s ->
                    s.contains("VEICULO") || s.contains("VEÍCULO") ||
                            s.contains("CHASSIS") || s.contains("MECHA")
                } == true

                val shouldExclude = isMechaOrVehicle

                if (!shouldExclude) {
                    modifiers.add(
                        Modifier(
                            id = "equip_${item.nome.keyify()}",
                            sourceType = SourceType.OUTRO,
                            sourceName = item.nome,
                            target = ModifierTarget.ARMOR,
                            value = armorVal
                        )
                    )
                }
            }
        }

        // 2. Ancestralidade
        val ancestralName = state.ancestralidade
        val ancestral = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralName.keyify() }

        ancestral?.let { anc ->
            val sources = anc.vantagensGratis + anc.habilidades.map { it.nome } + anc.desvantagens

            // Size from Ancestry (Tamanho X)
            val sizeSource = sources.firstOrNull { it.contains("TAMANHO", ignoreCase = true) }

            val racialSize = sizeSource
                ?.substringAfter("TAMANHO", "") // Try uppercase first
                ?.ifBlank { sizeSource?.substringAfter("Tamanho", "") } // Try title case
                ?.trim()
                ?.toIntOrNull()
                ?: 0

            if (racialSize != 0) {
                modifiers.add(Modifier(
                    id = "racial_size",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = anc.nome,
                    target = ModifierTarget.SIZE_DISPLAY,
                    value = racialSize
                ))
                modifiers.add(Modifier(
                    id = "racial_size_toughness",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = anc.nome,
                    target = ModifierTarget.SIZE_TOUGHNESS,
                    value = racialSize
                ))
            }

            // Diminuto (Ancestralidade)
            // Se tiver "DIMINUTO" nas desvantagens, habilidades ou vantagens grátis, aplica Tamanho -4
            val hasDiminuto = anc.desvantagens.any { it.keyify() == "DIMINUTO" } ||
                    anc.vantagensGratis.any { it.keyify() == "DIMINUTO" } ||
                    anc.habilidades.any { it.nome.keyify() == "DIMINUTO" }

            if (hasDiminuto) {
                 modifiers.add(Modifier(
                    id = "racial_diminuto",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = "Diminuto",
                    target = ModifierTarget.SIZE_DISPLAY,
                    value = -4
                ))
                modifiers.add(Modifier(
                    id = "racial_diminuto_tough",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = "Diminuto",
                    target = ModifierTarget.SIZE_TOUGHNESS,
                    value = -4
                ))
            }

            // Resistência (Auto advantage or racial trait)
            // Checks for FRAGIL/ESGUIOS (-1)
            val hasFragil = state.desvantagensRaciais.any { it.keyify() == "FRAGIL" }
            val hasEsguios = anc.habilidades.any { it.nome.contains("Esguios", ignoreCase = true) }

            if (hasFragil) {
                modifiers.add(Modifier("racial_fragil", SourceType.ANCESTRALIDADE, "Frágil", ModifierTarget.TOUGHNESS_FLAT, -1))
            }
            if (hasEsguios) {
                modifiers.add(Modifier("racial_esguios", SourceType.ANCESTRALIDADE, "Esguios", ModifierTarget.TOUGHNESS_FLAT, -1))
            }

            // Checks for RESISTENCIA/FEROCIDADE (+1)
            val hasResistencia = state.vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" }
            val hasFerocidade = anc.habilidades.any { it.nome.contains("Ferocidade", ignoreCase = true) }

            if (hasResistencia) {
                modifiers.add(Modifier("racial_resistencia", SourceType.ANCESTRALIDADE, "Resistência", ModifierTarget.TOUGHNESS_FLAT, 1))
            }
            if (hasFerocidade) {
                modifiers.add(Modifier("racial_ferocidade", SourceType.ANCESTRALIDADE, "Ferocidade Orc", ModifierTarget.TOUGHNESS_FLAT, 1))
            }

            // Generic Parsing
            sources.forEach { str ->
                val k = str.keyify()
                if (k.contains("RESISTENCIA")) {
                    val match = Regex("""RESISTENCIA\s*\+(\d+)""").find(k)
                    if (match != null) {
                        val valInt = match.groupValues[1].toInt()
                        // Avoid duplicates if caught by hardcoded check above
                        // (see reasoning in previous revision)
                        modifiers.add(Modifier("racial_res_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, valInt))
                    }
                }
                if (k.contains("ARMADURA")) {
                    val match = Regex("""ARMADURA(.*?)\+(\d+)""").find(k)
                    if (match != null) {
                        val valInt = match.groupValues[2].toInt()
                        if (state.naturalArmorFromRace == 0) {
                            modifiers.add(Modifier("racial_armor_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.ARMOR, valInt))
                        }
                    }
                }
            }
        }

        // 3. Complications
        state.complicacoesSelecionadas.keys.forEach { comp ->
            val key = comp.id.keyify()
            if (key == "PEQUENO") {
                modifiers.add(Modifier("comp_pequeno_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, -1))
                modifiers.add(Modifier("comp_pequeno_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, -1))
            }
            if (key == "OBESO") {
                modifiers.add(Modifier("comp_obeso_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, 1))
                modifiers.add(Modifier("comp_obeso_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, 1))
            }
        }

        // 4. Advantages
        state.vantagensSelecionadas.forEach { vant ->
            val key = vant.nome.keyify()
            if (key == "MUSCULOSO") {
                modifiers.add(Modifier("edge_musculoso_size", SourceType.VANTAGEM, vant.nome, ModifierTarget.SIZE_DISPLAY, 1))
                modifiers.add(Modifier("edge_musculoso_tough", SourceType.VANTAGEM, vant.nome, ModifierTarget.SIZE_TOUGHNESS, 1))
            }
            if (key == "BRUTAMONTES" || key == "BRAWNY") {
                modifiers.add(Modifier("edge_brutamontes", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 1))
            }
            if (key == "BRIGAO" || key == "PUGILISTA") {
                modifiers.add(Modifier("edge_brigao", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 1))
            }
        }

        // 5. Powers / Other
        if (state.bonusResFromPower != 0) {
            modifiers.add(Modifier("power_bonus_res", SourceType.OUTRO, "Poderes", ModifierTarget.TOUGHNESS_FLAT, state.bonusResFromPower))
        }

        // 6. Signos (Arte da Guerra)
        if (state.compendioArteDaGuerraAtivo && state.ancestralidade.keyify().contains("HUMANO")) {
            val sign = state.signoAdgSelecionado
            if (sign != null) {
                if (sign.equals("Tartaruga", ignoreCase = true)) {
                    modifiers.add(Modifier("sign_tartaruga_tough", SourceType.OUTRO, "Signo Tartaruga", ModifierTarget.TOUGHNESS_FLAT, 1))
                }
            }
        }

        return modifiers
    }

    fun sum(state: CriadorState, target: ModifierTarget): Int {
        return collect(state).filter { it.target == target }.sumOf { it.value }
    }

    fun sizeRawDisplay(state: CriadorState): Int {
        return sum(state, ModifierTarget.SIZE_DISPLAY)
    }

    fun sizeDisplay(state: CriadorState): Int {
        return sizeRawDisplay(state).coerceIn(-1, 3)
    }

    fun sizeForToughness(state: CriadorState): Int {
        return sum(state, ModifierTarget.SIZE_TOUGHNESS)
    }

    fun toughnessBase(state: CriadorState): Int {
        val vigorRaw = state.valoresAtributos["VIGOR"]?.intValue ?: 4
        val base = 2 + (vigorRaw / 2)
        val sizeMod = sizeForToughness(state)
        val flatMod = sum(state, ModifierTarget.TOUGHNESS_FLAT)

        return (base + sizeMod + flatMod).coerceAtLeast(0)
    }
}
