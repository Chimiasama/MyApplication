package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.util.autoTraitId
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

            // Movimentação Racial (Pace)
            // 1. Explicit Field
            if (anc.movimentacao != 0) {
                modifiers.add(Modifier("racial_pace_explicit", SourceType.ANCESTRALIDADE, anc.nome, ModifierTarget.PACE, anc.movimentacao))
            }

            // Tamanho, Movimentação Reduzida/aumentada, Resistência,
            // Aparar e Armadura Natural concedidos por id de traço: um loop
            // genérico sobre RacialTraitPointCatalog.EFEITOS substitui os
            // regex que antes liam "TAMANHO ±N"/"RESISTÊNCIA ±N"/
            // "MOVIMENTAÇÃO ±N"/"ARMADURA +N" do NOME do traço — o traço só
            // precisa estar presente (por id na habilidade da raça/monstro,
            // ou por nome solto pros grants ainda guardados como texto em
            // vantagensGratis/desvantagens), o catálogo já diz o alvo e o
            // valor. Ver RacialTraitEffect.
            val sourceKeys = sources.map { it.keyify() }.toSet()
            // Ids de habilidade da raça, restritos às que ainda têm o nome
            // presente em `sources` — algumas variantes (Aquarianos Semi-
            // aquático, Avianos Ave de Rapina, Centaux Gazela) removem uma
            // habilidade da raça base filtrando o nome dela fora de
            // `sources` (ver bloco de exclusões por variante logo acima);
            // ler direto de `anc.habilidades` sem esse filtro
            // reintroduziria o traço removido por baixo do pano. Habilidades
            // do Monstro Heroico não passam por essa filtragem de variante
            // de raça, então entram sem restrição.
            val traitIds = anc.habilidades
                .filter { it.nome.keyify() in sourceKeys }
                .mapNotNull { it.id?.keyify() }
                .toSet() +
                (monstro?.habilidades?.mapNotNull { it.id?.keyify() } ?: emptySet())

            // Alguns traços de Variante/Seleção (Centaux Gazela, Drakens/
            // Mímicos/Ferais "Padrão", Umvee Correnteza/Pedregoso etc.) ainda
            // chegam como texto solto em vantagensRaciais/desvantagensRaciais
            // (ver CriadorState.addIfAbsent), não como RacialAbility com id —
            // autoTraitId() deriva desse texto o MESMO id que addIfAbsent já
            // atribuiria, então o traço é reconhecido pelo id real dele, não
            // por regex sobre o texto. Só entra nesse fallback quem NÃO já
            // tem uma habilidade própria (com id de verdade) pro mesmo nome —
            // sem isso, uma raça cujo texto de habilidade já usada por
            // traitIds (ex.: Povo Rato/Fadas "DIMINUTO (Tamanho -4)", id
            // "DIMINUTO") também bateria com o auto-slug de outra raça que
            // injeta o mesmo texto solto por Variante (Ferais "Menor", id
            // sintético "DIMINUTO_TAMANHO_4") e contaria o efeito em dobro.
            val habilidadeNomeKeys = (
                anc.habilidades.map { it.nome.keyify() } +
                    (monstro?.habilidades?.map { it.nome.keyify() } ?: emptyList())
                ).toSet()
            val autoIdKeys = sources
                .filterNot { it.keyify() in habilidadeNomeKeys }
                .map { it.autoTraitId() }
                .toSet()

            // FRAGIL/FRAGIL_MAIOR têm o mesmo nome de exibição ("Frágil"),
            // diferindo só na penalidade (-1 padrão vs -2 dos Demônios) — o
            // nome sozinho não distingue qual é qual, então esses dois só
            // contam por id de habilidade, nunca por nome solto em sources.
            val idsSoPorHabilidade = setOf("FRAGIL", "FRAGIL_MAIOR")

            fun traitPresente(id: String): Boolean {
                if (id in traitIds) return true
                if (id in idsSoPorHabilidade) return false
                return id in sourceKeys || id in autoIdKeys
            }

            // sizeDisplay() trava o Tamanho mostrado em -1 no mínimo, exceto
            // pras raças Diminutas/Minúsculas do livro (Fadas, Povo Rato,
            // Ferais) — que continuam identificadas pela presença de um
            // Modifier com este id, não pelo nome da raça.
            fun aplicarEfeito(id: String, efeito: RacialTraitEffect, nomeExibicao: String) {
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
                    is RacialTraitEffect.TamanhoBonus -> {
                        // Id do próprio Modifier de Tamanho vira "racial_diminuto" quando
                        // minusculo=true — é essa presença que sizeDisplay() já checava
                        // pra decidir se trava a exibição em -1 ou deixa passar (Fadas,
                        // Povo Rato, Ferais). Um Modifier extra só pra marcar isso duplicaria
                        // a contagem de SIZE_DISPLAY (ver ModifierEngineAdgAncestryTest
                        // "povo rato size penalty is not double counted").
                        val sizeId = if (efeito.minusculo) "racial_diminuto" else "racial_trait_${id}_size"
                        modifiers.add(Modifier(sizeId, SourceType.ANCESTRALIDADE, nomeExibicao, ModifierTarget.SIZE_DISPLAY, efeito.valor))
                        modifiers.add(Modifier("racial_trait_${id}_size_tough", SourceType.ANCESTRALIDADE, nomeExibicao, ModifierTarget.SIZE_TOUGHNESS, efeito.valor))
                    }
                    // Armadura Natural não vira Modifier aqui — a Armadura final
                    // do personagem é resolvida à parte
                    // (ResolveAncestrySpecificAdjustmentsUseCase.naturalArmorFromRace,
                    // que já lê este mesmo efeito por id), não pelo
                    // ModifierTarget.ARMOR deste motor.
                    is RacialTraitEffect.ArmaduraBonus -> Unit
                    is RacialTraitEffect.Composite -> efeito.efeitos.forEach { sub -> aplicarEfeito(id, sub, nomeExibicao) }
                    is RacialTraitEffect.AtributoStep, is RacialTraitEffect.PericiaStep, RacialTraitEffect.Nenhum -> Unit
                }
            }

            RacialTraitPointCatalog.EFEITOS.forEach { (id, efeito) ->
                if (!traitPresente(id)) return@forEach
                val nomeExibicao = RacialTraitPointCatalog.LABEL[id] ?: id
                aplicarEfeito(id, efeito, nomeExibicao)
            }
        }

        // 3. Complications
        state.complicacoesSelecionadas.entries.forEach { (comp, nivel) ->
            if (comp.id == Constants.ID_PEQUENO) {
                modifiers.add(Modifier("comp_pequeno_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, -1))
                modifiers.add(Modifier("comp_pequeno_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, -1))
            }
            if (comp.id == Constants.ID_OBESO) {
                modifiers.add(Modifier("comp_obeso_size", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_DISPLAY, 1))
                modifiers.add(Modifier("comp_obeso_tough", SourceType.COMPLICACAO, comp.name, ModifierTarget.SIZE_TOUGHNESS, 1))
                modifiers.add(Modifier("comp_obeso_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, -1))
            }
            if (comp.id == Constants.ID_IDOSO) {
                modifiers.add(Modifier("comp_idoso_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, -1))
            }
            if (comp.id == Constants.ID_LENTO) {
                val penalty = if (nivel == "Maior") -2 else -1
                modifiers.add(Modifier("comp_lento_pace", SourceType.COMPLICACAO, comp.name, ModifierTarget.PACE, penalty))
            }
        }

        // 4. Advantages
        state.vantagensSelecionadas.forEach { vant ->
            if (vant.id == "couro_blindado") {
                modifiers.add(Modifier("edge_couro_blindado_armor", SourceType.VANTAGEM, vant.nome, ModifierTarget.ARMOR, 4))
            }
            if (vant.id == Constants.ID_MUSCULOSO) {
                modifiers.add(Modifier("edge_musculoso_size", SourceType.VANTAGEM, vant.nome, ModifierTarget.SIZE_DISPLAY, 1))
                modifiers.add(Modifier("edge_musculoso_tough", SourceType.VANTAGEM, vant.nome, ModifierTarget.SIZE_TOUGHNESS, 1))
            }
            if (vant.id == Constants.ID_BRUTAMONTES) {
                modifiers.add(Modifier("edge_brutamontes", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 1))
            }
            if (vant.id == Constants.ID_BRIGAO || vant.id == Constants.ID_PUGILISTA) {
                modifiers.add(Modifier("edge_brigao", SourceType.VANTAGEM, vant.nome, ModifierTarget.TOUGHNESS_FLAT, 1))
            }
            if (vant.id == Constants.ID_LIGEIRO) {
                modifiers.add(Modifier("edge_ligeiro_pace", SourceType.VANTAGEM, vant.nome, ModifierTarget.PACE, 2))
            }
            if (vant.id == Constants.ID_BLOQUEAR) {
                modifiers.add(Modifier("edge_bloquear_parry", SourceType.VANTAGEM, vant.nome, ModifierTarget.PARRY, 1))
            }
            if (vant.id == Constants.ID_BLOQUEAR_APRIMORADO) {
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
