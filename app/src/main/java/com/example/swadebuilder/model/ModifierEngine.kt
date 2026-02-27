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
        val ancestral = state.getAncestralidadeDef(ancestralName)

        ancestral?.let { anc ->
            val rawSources =
                anc.vantagensGratis +
                    anc.habilidades.map { it.nome } +
                    anc.desvantagens +
                    state.vantagensRaciais +
                    state.vantagensAutomaticas +
                    state.desvantagensRaciais +
                    state.desvantagensAutomaticas
            val sources = rawSources.toMutableList().apply {
                val ancestryKey = anc.nome.keyify()
                val allTraitKeys = (
                    this +
                        state.vantagensRaciais +
                        state.vantagensAutomaticas +
                        state.desvantagensRaciais +
                        state.desvantagensAutomaticas
                    )
                    .map { it.keyify() }
                    .toSet()

                // Sci-Fi Aquarianos (Semi-aquáticos) replace "Aquático" and "Resistência" traits.
                // Defensive normalization to avoid stale base traits leaking into mechanics,
                // even when ancestry base data is still present for display/back-compat paths.
                val hasSemiAquatico = allTraitKeys.any { key ->
                    key.contains("SEMI") && key.contains("AQUATIC")
                }

                if (ancestryKey == "AQUARIANOS" && hasSemiAquatico) {
                    removeAll { trait ->
                        val key = trait.keyify()
                        key == "AQUATICO" || key == "RESISTENCIA"
                    }
                }

                val isAvianosAveRapina = ancestryKey == "AVIANOS" &&
                    allTraitKeys.any { it.contains("FORMA ALIENIGENA") } &&
                    allTraitKeys.any { it.contains("HABITANTE DE GRAVIDADE") }

                if (isAvianosAveRapina) {
                    removeAll { trait ->
                        val key = trait.keyify()
                        key == "FRAGIL" || key == "NAO SABE NADAR"
                    }
                }
            }.distinctBy { it.keyify() }
            val abilityDescriptions = anc.habilidades.map { it.descricao }

            // Size from Ancestry (Tamanho X)
            // Fix: Exclude "DIMINUTO" entries to avoid double-counting if they contain "Tamanho" in text (e.g., "Diminuto (Tamanho -3)")
            val sizeSource = sources.firstOrNull {
                it.contains("TAMANHO", ignoreCase = true) && !it.keyify().startsWith("DIMINUTO")
            }

            val racialSizeFromText = abilityDescriptions
                .firstNotNullOfOrNull { desc ->
                    val key = desc.keyify()
                    val fromSize = Regex("""TAMANHO\s*([\+\-]\s*\d+)""").find(key)
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.replace(" ", "")
                        ?.toIntOrNull()
                    if (fromSize != null) return@firstNotNullOfOrNull fromSize

                    if (key.contains("REDUZINDO SEU TAMANHO") && key.contains("EM 1")) return@firstNotNullOfOrNull -1
                    if (key.contains("ADICIONE") && key.contains("A SUA RESISTENCIA") && key.contains("TAMANHO +1")) return@firstNotNullOfOrNull 1
                    null
                }

            val racialSize = if (sizeSource != null) {
                sizeSource.substringAfter("TAMANHO", "") // Try uppercase first
                    .ifBlank { sizeSource.substringAfter("Tamanho", "") } // Try title case
                    .trim()
                    .toIntOrNull()
                    ?: 0
            } else if (sources.any { it.keyify() == "PEQUENOS" || it.keyify() == "PEQUENO" }) {
                -1
            } else {
                racialSizeFromText ?: 0
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

            val hasLentoRacial = sources.any {
                val key = it.keyify()
                key == "LENTO" || key.endsWith("LENTO")
            }
            if (hasLentoRacial) {
                modifiers.add(Modifier("racial_pace_lento", SourceType.ANCESTRALIDADE, "Lento", ModifierTarget.PACE, -1))
            }

            // Diminuto (Ancestralidade)
            // Se tiver "DIMINUTO" nas desvantagens, habilidades ou vantagens grátis, aplica penalidade de Tamanho
            val diminutoSource = sources.firstOrNull { it.keyify().startsWith("DIMINUTO") }

            fun addDiminuto(sizeVal: Int, sourceLabel: String) {
                modifiers.add(Modifier(
                    id = "racial_diminuto",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = sourceLabel,
                    target = ModifierTarget.SIZE_DISPLAY,
                    value = sizeVal
                ))
                modifiers.add(Modifier(
                    id = "racial_diminuto_tough",
                    sourceType = SourceType.ANCESTRALIDADE,
                    sourceName = sourceLabel,
                    target = ModifierTarget.SIZE_TOUGHNESS,
                    value = sizeVal
                ))
            }

            if (diminutoSource != null) {
                val k = diminutoSource.keyify()
                // Default is -4 (Tiny) as per Fantasy/Horror standard if not specified
                val sizeVal = when {
                    k.contains("TAMANHO -2") -> -2
                    k.contains("TAMANHO -3") -> -3
                    k.contains("TAMANHO -4") -> -4
                    else -> -4
                }
                addDiminuto(sizeVal, "Diminuto")
            } else if (state.compendioSciFiAtivo && anc.nome.keyify() == "FERAIS") {
                val variant = state.resolveSciFiVariantSelectionFor(
                    ancestryName = anc.nome,
                    availableOptions = anc.opcoes
                )
                val feralSize = if (variant == "Menor") -4 else -3
                addDiminuto(feralSize, "Diminuto (Feral)")
            }

            // Resistência (Auto advantage or racial trait)
            // Checks for FRAGIL/ESGUIOS (-1)
            val hasFragil = sources.any { it.keyify() == "FRAGIL" }
            val hasEsguios = anc.habilidades.any { it.nome.contains("Esguios", ignoreCase = true) }

            if (hasFragil) {
                modifiers.add(Modifier("racial_fragil", SourceType.ANCESTRALIDADE, "Frágil", ModifierTarget.TOUGHNESS_FLAT, -1))
            }
            if (hasEsguios) {
                modifiers.add(Modifier("racial_esguios", SourceType.ANCESTRALIDADE, "Esguios", ModifierTarget.TOUGHNESS_FLAT, -1))
            }

            // Checks for RESISTENCIA/FEROCIDADE (+1)
            // NOTE: exact token "RESISTENCIA" to avoid matching "RESISTENCIA AMBIENTAL"
            val hasResistencia = sources.any { it.keyify() == "RESISTENCIA" }
            val hasFerocidade = anc.habilidades.any { it.nome.contains("Ferocidade", ignoreCase = true) }

            if (hasResistencia) {
                modifiers.add(Modifier("racial_resistencia", SourceType.ANCESTRALIDADE, "Resistência", ModifierTarget.TOUGHNESS_FLAT, 1))
            }
            if (hasFerocidade) {
                modifiers.add(Modifier("racial_ferocidade", SourceType.ANCESTRALIDADE, "Ferocidade Orc", ModifierTarget.TOUGHNESS_FLAT, 1))
            }

            if (state.compendioSciFiAtivo && anc.nome.keyify() == "MIMICOS") {
                val variant = state.resolveSciFiVariantSelectionFor(
                    ancestryName = anc.nome,
                    availableOptions = anc.opcoes
                )
                val hasResistenciaMaisUm = sources.any { src ->
                    val key = src.keyify()
                    key.contains("RESISTENCIA") && key.contains("+1")
                }
                if (variant == "Resistente" && !hasResistenciaMaisUm) {
                    modifiers.add(
                        Modifier(
                            id = "racial_mimicos_resistente",
                            sourceType = SourceType.ANCESTRALIDADE,
                            sourceName = "Resistente",
                            target = ModifierTarget.TOUGHNESS_FLAT,
                            value = 1
                        )
                    )
                }
            }

            // Generic Parsing
            sources.forEach { str ->
                val k = str.keyify()
                if (k.contains("RESISTENCIA")) {
                    val match = Regex("""RESISTENCIA\s*(\+|\-)\s*(\d+)""").find(k) // Enhanced regex to capture sign and spaces
                    if (match != null) {
                        val sign = match.groupValues[1]
                        val value = match.groupValues[2].toInt()
                        val finalValue = if (sign == "-") -value else value

                        // Avoid duplicates if caught by hardcoded check above (e.g. Frágil might say "Resistência -1")
                        // But Fragil logic above relies on name "FRAGIL". This regex handles explicit "+1" or "-1".
                        // Aquarianos: "Resistência +1".
                        modifiers.add(Modifier("racial_res_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, finalValue))
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
            if (vant.id == "couro_blindado") {
                modifiers.add(Modifier("edge_couro_blindado_armor", SourceType.VANTAGEM, vant.nome, ModifierTarget.ARMOR, 4))
            }
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
            if (vant.id == "resistencia_lobo") {
                modifiers.add(Modifier("edge_resistencia_lobo", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
            }
            if (vant.id == "resistencia_anjo") {
                modifiers.add(Modifier("edge_resistencia_anjo", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
            }
            if (vant.id == "resistencia_divina") {
                modifiers.add(Modifier("edge_resistencia_divina", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
            }
        }

        // 4.5 Monster templates
        state.getMonstroSelecionado()?.let { monstro ->
            if (monstro.id == "lobisomem") {
                modifiers.add(Modifier("monster_lobisomem_pace", SourceType.OUTRO, monstro.nome, ModifierTarget.PACE, 2))
            }
            if (monstro.id == "monstro_retalhos") {
                modifiers.add(Modifier("monster_retalhos_tough", SourceType.OUTRO, monstro.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
            }
            if (monstro.id == "mumia") {
                modifiers.add(Modifier("monster_mumia_tough", SourceType.OUTRO, monstro.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
                modifiers.add(Modifier("monster_mumia_pace", SourceType.OUTRO, monstro.nome, ModifierTarget.PACE, -1))
            }
            if (monstro.id == "revivido") {
                modifiers.add(Modifier("monster_revivido_tough", SourceType.OUTRO, monstro.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
            }
            if (monstro.id == "vampiro") {
                modifiers.add(Modifier("monster_vampiro_tough", SourceType.OUTRO, monstro.nome, ModifierTarget.TOUGHNESS_FLAT, 2))
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
        val raw = sizeRawDisplay(state)
        // Check if character is Diminuto/Tiny to bypass clamp
        val isDiminuto = collect(state).any { it.id == "racial_diminuto" }

        // If not Diminuto, clamp minimum visual size to -1
        return if (!isDiminuto && raw < -1) {
            -1
        } else {
            raw.coerceIn(-4, 20)
        }
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
