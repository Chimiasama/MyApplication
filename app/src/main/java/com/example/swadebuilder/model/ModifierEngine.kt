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
            // Template de Monstro Heroico (Horror): NÃO é raça nem variante de
            // raça — é uma camada de traços que se soma à ancestralidade
            // escolhida (ex.: Elfo + Vampiro). Por isso entra aqui como mais uma
            // fonte de nomes de traço, junto das da raça, em vez de qualquer
            // caminho específico por "qual monstro é esse": os checks abaixo
            // (hasMortoVivo, hasLentoRacial, hasVelocidadeRacial etc.) reagem à
            // presença do traço, não à identidade do monstro ou da raça.
            val monstro = state.getMonstroSelecionado()
            val monstroSources = monstro?.let { m ->
                m.habilidades.map { it.nome } +
                    // Complicações do monstro vêm como frase completa
                    // ("Lento: Movimentação reduzida em 1..."); só o rótulo
                    // antes dos ":" interessa pros checks por nome/id.
                    m.complicacoes.map { it.substringBefore(":").trim() }
            } ?: emptyList()

            val rawSources =
                anc.vantagensGratis +
                    anc.habilidades.map { it.nome } +
                    anc.desvantagens +
                    state.vantagensRaciais +
                    state.vantagensAutomaticas +
                    state.desvantagensRaciais +
                    state.desvantagensAutomaticas +
                    monstroSources
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

                val isCentauxGazela = state.compendioSciFiAtivo &&
                    ancestryKey == "CENTAUX" &&
                    state.resolveSciFiVariantSelectionFor(
                        ancestryName = anc.nome,
                        availableOptions = anc.opcoes
                    ).equals("Gazela", ignoreCase = true)

                if (isCentauxGazela) {
                    removeAll { trait ->
                        when (trait.keyify()) {
                            "MOVIMENTACAO +2", "TAMANHO +2", "GRANDE", "TAMANHO_MAIS_2", "MOVIMENTACAO_2" -> true
                            else -> false
                        }
                    }
                }
            }.distinctBy { it.keyify() }
            val abilityDescriptions = anc.habilidades.map { it.descricao }

            // Size from Ancestry (Tamanho X)
            // Fix: Exclude "DIMINUTO" entries to avoid double-counting if they contain "Tamanho" in text (e.g., "Diminuto (Tamanho -3)")
            val sizeSource = sources.firstOrNull {
                it.contains("TAMANHO", ignoreCase = true) && !it.keyify().startsWith("DIMINUTO")
            }

            val isDiminutoAncestry = sources.any { it.keyify().startsWith("DIMINUTO") } ||
                                     (state.compendioSciFiAtivo && anc.nome.keyify() == "FERAIS")

            val racialSizeFromText = abilityDescriptions
                .firstNotNullOfOrNull { desc ->
                    val key = desc.keyify()

                    val fromSize = Regex("""TAMANHO\s*([+-]\s*\d+)""").find(key)
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

            if (racialSize != 0 && !isDiminutoAncestry) {
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

            // Lento e Velocidade (Template de Monstro Heroico: Lobisomem) agora
            // saem do loop genérico de RacialTraitPointCatalog logo abaixo —
            // ver bloco "Bônus fixos de Resistência/Passo/Aparar".

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

            // Bônus fixos de Resistência/Passo/Aparar concedidos por id de
            // traço: um loop genérico sobre RacialTraitPointCatalog.EFEITOS
            // substitui o que antes era um `val hasX = ...; if (hasX)
            // modifiers.add(...)` por traço (Morto-Vivo, Metade Construto,
            // Frágil, Esguios, Resistência, Ferocidade Orc, Aparar Baixo,
            // Lento, Velocidade) — o traço só precisa estar presente (por id
            // na habilidade da raça/monstro, ou por nome solto pros grants
            // ainda guardados como texto em vantagensGratis/desvantagens), o
            // catálogo já diz o alvo e o valor. Ver RacialTraitEffect.
            val sourceKeys = sources.map { it.keyify() }.toSet()
            // Ids de habilidade da raça, restritos às que ainda têm o nome
            // presente em `sources` — algumas variantes (Aquarianos Semi-
            // aquático, Avianos Ave de Rapina) removem uma habilidade da raça
            // base filtrando o nome dela fora de `sources` (ver bloco de
            // exclusões por variante logo acima); ler direto de
            // `anc.habilidades` sem esse filtro reintroduziria o traço
            // removido por baixo do pano. Habilidades do Monstro Heroico não
            // passam por essa filtragem de variante de raça, então entram sem
            // restrição.
            val traitIds = anc.habilidades
                .filter { it.nome.keyify() in sourceKeys }
                .mapNotNull { it.id?.keyify() }
                .toSet() +
                (monstro?.habilidades?.mapNotNull { it.id?.keyify() } ?: emptySet())

            // FRAGIL/FRAGIL_MAIOR têm o mesmo nome de exibição ("Frágil"),
            // diferindo só na penalidade (-1 padrão vs -2 dos Demônios) — o
            // nome sozinho não distingue qual é qual, então esses dois só
            // contam por id de habilidade, nunca por nome solto em sources.
            val idsSoPorHabilidade = setOf("FRAGIL", "FRAGIL_MAIOR")

            fun traitPresente(id: String): Boolean {
                if (id in traitIds) return true
                if (id in idsSoPorHabilidade) return false
                return sourceKeys.any { it == id || it == id.replace('_', ' ') || it == id.replace('_', '-') }
            }

            RacialTraitPointCatalog.EFEITOS.forEach { (id, efeito) ->
                if (!traitPresente(id)) return@forEach
                val nomeExibicao = RacialTraitPointCatalog.LABEL[id] ?: id
                when (efeito) {
                    is RacialTraitEffect.ResistenciaBonus -> modifiers.add(
                        Modifier("racial_trait_${id}_res", SourceType.ANCESTRALIDADE, nomeExibicao, ModifierTarget.TOUGHNESS_FLAT, efeito.valor)
                    )
                    is RacialTraitEffect.PassoBonus -> modifiers.add(
                        Modifier("racial_trait_${id}_pace", SourceType.ANCESTRALIDADE, nomeExibicao, ModifierTarget.PACE, efeito.valor)
                    )
                    is RacialTraitEffect.ApararBonus -> modifiers.add(
                        Modifier("racial_trait_${id}_parry", SourceType.ANCESTRALIDADE, nomeExibicao, ModifierTarget.PARRY, efeito.valor)
                    )
                    else -> Unit
                }
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
                    val match = Regex("""RESISTENCIA\s*([+-])\s*(\d+)""").find(k) // Enhanced regex to capture sign and spaces
                    if (match != null) {
                        val sign = match.groupValues[1]
                        val value = match.groupValues[2].toInt()
                        val finalValue = if (sign == "-") -value else value

                        // Evita duplicar quando o loop genérico de traços (bloco
                        // "Bônus fixos de Resistência/Passo/Aparar" acima) já
                        // cobriu o mesmo valor pra essa raça — ids atualizados
                        // pra bater com "racial_trait_${id}_res" (ver EFEITOS).
                        val alreadyAdded = modifiers.any {
                            (it.id == "racial_trait_RESISTENCIA_res" && finalValue == 1) ||
                            (it.id == "racial_trait_FRAGIL_res" && finalValue == -1) ||
                            (it.id == "racial_trait_FRAGIL_MAIOR_res" && finalValue == -2) ||
                            (it.id == "racial_trait_ESGUIOS_res" && finalValue == -1) ||
                            (it.id == "racial_trait_FEROCIDADE_ORC_res" && finalValue == 1)
                        }

                        if (!alreadyAdded) {
                            modifiers.add(Modifier("racial_res_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.TOUGHNESS_FLAT, finalValue))
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
                        val malusMatch = Regex("""MOVIMENTACAO\s*-(\d+)""").find(k)
                        if (malusMatch != null) {
                            // Do not apply generic minus if "Movimentação Reduzida" was already added by explicit logic to prevent double penalty
                            val alreadyReduced = modifiers.any { it.id == "racial_pace_reduced" && it.value == -1 }
                            if (!alreadyReduced) {
                                modifiers.add(Modifier("racial_pace_generic_minus", SourceType.ANCESTRALIDADE, str, ModifierTarget.PACE, -malusMatch.groupValues[1].toInt()))
                            }
                        }
                    }
                }
                if (k.contains("APARAR")) {
                    val match = Regex("""APARAR\s*([+-])\s*(\d+)""").find(k)
                    if (match != null) {
                        val sign = match.groupValues[1]
                        val value = match.groupValues[2].toInt()
                        val finalValue = if (sign == "-") -value else value
                        modifiers.add(Modifier("racial_parry_generic", SourceType.ANCESTRALIDADE, str, ModifierTarget.PARRY, finalValue))
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

        // 4.5 Monster templates: Resistência (Morto-Vivo) e Passo (Velocidade,
        // Lento) do Template de Monstro Heroico agora são resolvidos dentro do
        // bloco "2. Ancestralidade" acima, pelos mesmos checks por nome/id de
        // traço que a ancestralidade usa (hasMortoVivo, hasVelocidadeRacial,
        // hasLentoRacial) — o monstro só entra como mais uma fonte de nomes em
        // `sources`/`monstroSources`, não como um `if (monstro.id == ...)`
        // separado por monstro.

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
