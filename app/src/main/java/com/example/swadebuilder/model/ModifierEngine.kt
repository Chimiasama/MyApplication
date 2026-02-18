package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.keyify

enum class ModifierTarget {
    SIZE_DISPLAY,
    SIZE_TOUGHNESS,
    TOUGHNESS_FLAT,
    ARMOR,
    PACE,
    PARRY
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
        val ancestral = state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralName.keyify() }

        ancestral?.let { anc ->
            val sources = anc.vantagensGratis + anc.habilidades.map { it.nome } + anc.desvantagens

            // Size from Ancestry (Tamanho X)
            // Fix: Exclude "DIMINUTO" entries to avoid double-counting if they contain "Tamanho" in text (e.g., "Diminuto (Tamanho -3)")
            val sizeSource = sources.firstOrNull {
                it.contains("TAMANHO", ignoreCase = true) && !it.keyify().startsWith("DIMINUTO")
            }

            val racialSize = if (sizeSource != null) {
                sizeSource.substringAfter("TAMANHO", "") // Try uppercase first
                    .ifBlank { sizeSource.substringAfter("Tamanho", "") } // Try title case
                    .trim()
                    .toIntOrNull()
                    ?: 0
            } else {
                0
            }

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

            // Movimentação Racial (Pace)
            // 1. Explicit Field
            if (anc.movimentacao != 0) {
                modifiers.add(Modifier("racial_pace_explicit", SourceType.ANCESTRALIDADE, anc.nome, ModifierTarget.PACE, anc.movimentacao))
            }

            // 2. Pathfinder Slow Races
            val isPathfinderSlowRace = state.compendioPathfinderAtivo &&
                    (anc.id == "anc_anaopathfinder" ||
                            anc.id == "anc_gnomopathfinder" ||
                            anc.id == "anc_halflingpathfinder")
            if (isPathfinderSlowRace) {
                modifiers.add(Modifier("racial_pace_pathfinder", SourceType.ANCESTRALIDADE, "Raça Lenta (Pathfinder)", ModifierTarget.PACE, -1))
            }

            // 3. Keyword Checks (Movimentação Reduzida)
            val hasMovReduzida = anc.desvantagens.any {
                val k = it.keyify()
                k.contains("MOVIMENTACAO") && k.contains("REDUZIDA")
            } || anc.habilidades.any {
                val k = it.nome.keyify()
                k.contains("MOVIMENTACAO") && k.contains("REDUZIDA")
            }
            if (hasMovReduzida) {
                modifiers.add(Modifier("racial_pace_reduced", SourceType.ANCESTRALIDADE, "Movimentação Reduzida", ModifierTarget.PACE, -1))
            }

            // Diminuto (Ancestralidade)
            // Se tiver "DIMINUTO" nas desvantagens, habilidades ou vantagens grátis, aplica penalidade de Tamanho
            val diminutoSource = sources.firstOrNull { it.keyify().startsWith("DIMINUTO") }

            if (diminutoSource != null) {
                val k = diminutoSource.keyify()
                // Default is -4 (Tiny) as per Fantasy/Horror standard if not specified
                val sizeVal = when {
                    k.contains("TAMANHO -2") -> -2
                    k.contains("TAMANHO -3") -> -3
                    k.contains("TAMANHO -4") -> -4
                    else -> -4
                }

                 modifiers.add(Modifier(
                    id = "racial_diminuto",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = "Diminuto",
                    target = ModifierTarget.SIZE_DISPLAY,
                    value = sizeVal
                ))
                modifiers.add(Modifier(
                    id = "racial_diminuto_tough",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = "Diminuto",
                    target = ModifierTarget.SIZE_TOUGHNESS,
                    value = sizeVal
                ))
            }

            // Resistência (Auto advantage or racial trait)
            // Checks for FRAGIL/ESGUIOS (-1)
            // Fix: Check abilities list for "Frágil" as well, since it might not be in disadvantages
            val hasFragil = state.desvantagensRaciais.any { it.keyify() == "FRAGIL" } ||
                    anc.habilidades.any { it.nome.keyify() == "FRAGIL" }
            val hasEsguios = anc.habilidades.any { it.nome.contains("Esguios", ignoreCase = true) }

            if (hasFragil) {
                modifiers.add(Modifier("racial_fragil", SourceType.ANCESTRALIDADE, "Frágil", ModifierTarget.TOUGHNESS_FLAT, -1))
            }
            if (hasEsguios) {
                modifiers.add(Modifier("racial_esguios", SourceType.ANCESTRALIDADE, "Esguios", ModifierTarget.TOUGHNESS_FLAT, -1))
            }

            // Checks for RESISTENCIA/FEROCIDADE (+1)
            // Fix: Check abilities list for "Resistência" (Aquarianos)
            val hasResistencia = state.vantagensAutomaticas.any { it.keyify() == "RESISTENCIA" } ||
                    anc.habilidades.any { it.nome.keyify() == "RESISTENCIA" }
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
                // Avoid double counting named abilities processed above
                if (str.keyify() == "RESISTENCIA" || str.keyify() == "FRAGIL") return@forEach

                if (k.contains("RESISTENCIA")) {
                    val matchPlus = Regex("""RESISTENCIA\s*\+(\d+)""").find(k)
                    val matchMinus = Regex("""RESISTENCIA\s*\-(\d+)""").find(k)

                    if (matchPlus != null) {
                        val valInt = matchPlus.groupValues[1].toInt()
                        modifiers.add(Modifier("racial_res_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, valInt))
                    } else if (matchMinus != null) {
                        val valInt = matchMinus.groupValues[1].toInt()
                        modifiers.add(Modifier("racial_res_generic_neg", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, -valInt))
                    } else {
                        // Attempt to parse phrases like "Adicione +1 a sua Resistência"
                        val matchFree = Regex("""\+(\d+).*RESISTENCIA""").find(k)
                        if (matchFree != null) {
                            val valInt = matchFree.groupValues[1].toInt()
                            modifiers.add(Modifier("racial_res_generic_free", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, valInt))
                        }
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
                if (k.contains("MOVIMENTACAO")) {
                    // Only apply generic regex if explicit field is 0 (to match legacy logic fallback)
                    if (anc.movimentacao == 0) {
                        val bonusMatch = Regex("""MOVIMENTACAO\s*\+(\d+)""").find(k)
                        if (bonusMatch != null) {
                             modifiers.add(Modifier("racial_pace_generic_plus", SourceType.ANCESTRALIDADE, str, ModifierTarget.PACE, bonusMatch.groupValues[1].toInt()))
                        }
                        val malusMatch = Regex("""MOVIMENTACAO\s*\-(\d+)""").find(k)
                        if (malusMatch != null) {
                             modifiers.add(Modifier("racial_pace_generic_minus", SourceType.ANCESTRALIDADE, str, ModifierTarget.PACE, -malusMatch.groupValues[1].toInt()))
                        }
                    }
                }
                if (k.contains("APARAR")) {
                    val bonusMatch = Regex("""APARAR\s*\+(\d+)""").find(k)
                    if (bonusMatch != null) {
                        modifiers.add(Modifier("racial_parry_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.PARRY, bonusMatch.groupValues[1].toInt()))
                    }
                }
            }
        }

        // 3. Complications
        state.complicacoesSelecionadas.entries.forEach { (comp, nivel) ->
            val key = comp.id.keyify()
            if (key == "PEQUENO") {
                modifiers.add(Modifier("comp_pequeno_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, -1))
                modifiers.add(Modifier("comp_pequeno_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, -1))
            }
            if (key == "OBESO") {
                modifiers.add(Modifier("comp_obeso_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, 1))
                modifiers.add(Modifier("comp_obeso_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, 1))
                modifiers.add(Modifier("comp_obeso_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, -1))
            }
            if (key == "IDOSO" || key.endsWith("IDOSO")) {
                modifiers.add(Modifier("comp_idoso_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, -1))
            }
            if (key == "LENTO" || key.endsWith("LENTO")) {
                val penalty = if (nivel == "Maior") -2 else -1
                modifiers.add(Modifier("comp_lento_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, penalty))
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
            if (key == "LIGEIRO") {
                modifiers.add(Modifier("edge_ligeiro_pace", SourceType.VANTAGEM, vant.nome, ModifierTarget.PACE, 2))
            }
            if (key == "BLOQUEAR") {
                modifiers.add(Modifier("edge_bloquear_parry", SourceType.VANTAGEM, vant.nome, ModifierTarget.PARRY, 1))
            }
            if (key == "BLOQUEAR APRIMORADO") {
                modifiers.add(Modifier("edge_bloquear_imp_parry", SourceType.VANTAGEM, vant.nome, ModifierTarget.PARRY, 1))
            }
        }

        // 5. Powers / Other
        if (state.bonusResFromPower != 0) {
            modifiers.add(Modifier("power_bonus_res", SourceType.OUTRO, "Poderes", ModifierTarget.TOUGHNESS_FLAT, state.bonusResFromPower))
        }
        if (state.bonusMovimentacaoFromPower != 0) {
            modifiers.add(Modifier("power_bonus_pace", SourceType.OUTRO, "Poderes", ModifierTarget.PACE, state.bonusMovimentacaoFromPower))
        }
        if (state.bonusApararFromPower != 0) {
            modifiers.add(Modifier("power_bonus_parry", SourceType.OUTRO, "Poderes", ModifierTarget.PARRY, state.bonusApararFromPower))
        }

        // 6. Signos (Arte da Guerra)
        if (state.compendioArteDaGuerraAtivo && state.ancestralidade.keyify().contains("HUMANO")) {
            val sign = state.signoAdgSelecionado
            if (sign != null) {
                if (sign.equals("Tartaruga", ignoreCase = true)) {
                    modifiers.add(Modifier("sign_tartaruga_tough", SourceType.OUTRO, "Signo Tartaruga", ModifierTarget.TOUGHNESS_FLAT, 1))
                }
                if (sign.equals("Garça", ignoreCase = true)) {
                    modifiers.add(Modifier("sign_garca_parry", SourceType.OUTRO, "Signo Garça", ModifierTarget.PARRY, 1))
                }
            }
        }

        // 7. Equipment Toughness (Resistência)
        val nonStackingArmorToughness = mutableListOf<Pair<String, Int>>()

        state.equipamentosComprados.forEach { item ->
            val resVal = (item.resistencia as? kotlinx.serialization.json.JsonPrimitive)
                ?.content?.toIntOrNull() ?: 0

            if (resVal > 0) {
                val obs = (item.observacoes as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
                // Checks for "Stacks with armor" note
                val explicitlyStacks = obs.contains("Acumula com armaduras", ignoreCase = true)
                // Treat as "Armor" (non-stacking by default) if it has the 'armadura' field defined
                val isArmor = item.armadura != null

                if (isArmor && !explicitlyStacks) {
                    nonStackingArmorToughness.add(item.nome to resVal)
                } else {
                    // Stacking (Accessories, Shields, or explicit stacking armor)
                    modifiers.add(
                        Modifier(
                            id = "equip_tough_${item.nome.keyify()}",
                            sourceType = SourceType.OUTRO,
                            sourceName = item.nome,
                            target = ModifierTarget.TOUGHNESS_FLAT,
                            value = resVal
                        )
                    )
                }
            }
        }

        // Add the best non-stacking armor toughness
        if (nonStackingArmorToughness.isNotEmpty()) {
            val best = nonStackingArmorToughness.maxByOrNull { it.second }
            if (best != null) {
                modifiers.add(
                    Modifier(
                        id = "equip_tough_armor_max",
                        sourceType = SourceType.OUTRO,
                        sourceName = "${best.first} (Armadura)",
                        target = ModifierTarget.TOUGHNESS_FLAT,
                        value = best.second
                    )
                )
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
        val total = sizeRawDisplay(state)

        // Fix: If race is "Small" (Size -1) and takes "Small" Hindrance (-1), total is -2.
        // But visual size limit for Small races is -1 unless they are effectively "Diminuto" (Tiny).
        // We approximate "Diminuto" as a race that STARTS with size <= -2 or has "DIMINUTO" trait.
        // If race starts with Size -1 (e.g. Halfling) and ends up -2, we cap at -1.

        val ancestralName = state.ancestralidade
        val ancestral = state.listaAncestralidadesJson.firstOrNull { it.nome.keyify() == ancestralName.keyify() }

        // Determine racial base size
        var racialSize = 0
        ancestral?.let { anc ->
            val sources = anc.vantagensGratis + anc.habilidades.map { it.nome } + anc.desvantagens
            val sizeSource = sources.firstOrNull {
                it.contains("TAMANHO", ignoreCase = true) && !it.keyify().startsWith("DIMINUTO")
            }
            if (sizeSource != null) {
                val valStr = sizeSource.substringAfter("TAMANHO", "").ifBlank { sizeSource.substringAfter("Tamanho", "") }
                racialSize = valStr.trim().toIntOrNull() ?: 0
            }

            // Check Diminuto explicitly
            val diminutoSource = sources.firstOrNull { it.keyify().startsWith("DIMINUTO") }
            if (diminutoSource != null) {
                // Diminuto races (like Pixies) usually start at -2 or lower
                // If found, we assume they are naturally tiny, so we don't clamp at -1.
                return total.coerceIn(-4, 20)
            }
        }

        // Logic: If I am NOT naturally Tiny (Size <= -2), I cannot go below -1.
        if (racialSize > -2) {
            if (total < -1) return -1
        }

        return total.coerceIn(-4, 20)
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
