package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.titleCase
import kotlin.math.max

// =================================================================================================
// SHARED SUMMARY BUILDER (Used by ResumoSection.kt)
// =================================================================================================

fun buildSummaryLines(personagem: MeuPersonagem): List<String> {
    val lines = mutableListOf<String>()

    val showOfficialNames = personagem.modoOficialAtivo

    val ancestralidadeNomeObj = listaAncestralidadesJson
        .filter { it.nome.keyify() == personagem.ancestralidade }
        .filter { item ->
            val origin = item.origem.uppercase()
            when (origin) {
                "FANTASIA" -> personagem.compendioFantasiaAtivo
                "HORROR" -> personagem.compendioHorrorAtivo
                "ARTE_DA_GUERRA" -> personagem.compendioArteDaGuerraAtivo
                "DEADLANDS" -> personagem.compendioDeadlandsAtivo
                "WISEGUYS" -> personagem.compendioWiseguysAtivo
                "CIDADE_SOL_VAPOR" -> personagem.compendioCidadeSolVaporAtivo
                "CRYSTAL_HEART" -> personagem.coracaoCrystalSelecionado != null
                "FC", "SCIFI" -> personagem.compendioSciFiAtivo
                else -> {
                    if (origin.contains("TRILHADOR") || origin.contains("PATHFINDER")) personagem.compendioPathfinderAtivo
                    else true
                }
            }
        }
        .maxByOrNull { CriadorState.getOriginPriority(it.origem) }
        ?: listaAncestralidadesJson.firstOrNull { it.nome.keyify() == personagem.ancestralidade }

    val rawAncestralidadeNome = if (showOfficialNames && ancestralidadeNomeObj?.originalName != null) {
        ancestralidadeNomeObj.originalName
    } else {
        ancestralidadeNomeObj?.nome ?: personagem.ancestralidade
    }

    // Remove sufixos como (Buscatrilha), (Trilhador), etc.
    val ancestralidadeNome: String = rawAncestralidadeNome
        .replace(Regex("\\s*\\((Pathfinder|Buscatrilha|Trilhador|Mundo Ancestral)\\)"), "")
        .trim()
        .titleCase()

    val monstroNome = if (personagem.modoMonstroAtivo) {
        val tipoNome = listaMonstroTemplates.find { it.id == personagem.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        " (Monstro: $tipoNome)"
    } else ""

    val vantagensNomeKey: List<String> = listaVantagens
        .filter { it.id in personagem.vantagens }
        .map { it.nome.keyify() }
    val complicacoesNomeadas: List<String> = complicationDisplayNames(personagem.complicacoes, showOfficialNames)
    val transtornosNomeados: List<String> = complicationDisplayNames(personagem.transtornos, showOfficialNames)
    val complicacoesNomeKeyset = listaComplicacoes
        .flatMap { comp -> listOfNotNull(comp.name, comp.originalName) }
        .map { it.keyify() }
        .toSet()

    val vantagemChoices: MutableMap<String, MutableList<String>> = personagem.advantageChoices
        .mapValues { it.value.toMutableList() }
        .toMutableMap()

    val allComplicationsKeys: List<String> =
        personagem.complicacoes + personagem.desvantagensRaciais + personagem.transtornos

    fun temComp(key: String): Boolean =
        allComplicationsKeys.any { it.keyify() == key }

    fun applySuperStepsFrom(rawStart: Int, steps: Int): Int {
        var raw = rawStart
        var remaining = steps.coerceAtLeast(0)

        if (raw <= 0 && remaining > 0) {
            raw = 4
            remaining -= 1
        }

        repeat(remaining) {
            raw += if (raw < 12) 2 else 1
        }

        return raw
    }

    fun calcAparar(): Int {
        val lutarRawBase = personagem.pericias["Lutar"] ?: 0
        val jutsuRawBase = personagem.pericias["Jutsu"] ?: 0
        val lutarStepsFromSupers = personagem.superInvestments
            .mapNotNull { it.effect as? PowerEffect.SuperPericia }
            .filter { it.periciaKey.equals("Lutar", ignoreCase = true) }
            .sumOf { it.steps }
        val lutarComSupers = applySuperStepsFrom(lutarRawBase, lutarStepsFromSupers)
        val jutsuComSupers = jutsuRawBase

        val base = 2 + (max(lutarComSupers, jutsuComSupers) / 2)

        val bloquearBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusApararFromPower
    }

    fun calcChi(): Int {
        personagem.reservaChi?.let { return it }

        val espRaw = personagem.atributos["ESPIRITO"] ?: 0
        val racialPenalty = if (personagem.ancestralidade.keyify() == "TERRACOTA") 1 else 0
        val chiBonus = listaVantagens
            .filter { it.id in personagem.vantagens }
            .count { it.categoria == Categoria.CHI }

        return (espRaw / 2 - racialPenalty + chiBonus).coerceAtLeast(0)
    }

    val aparar = calcAparar()
    val resFinal = personagem.resistencia
    val tamanho = personagem.tamanho
    val mov = personagem.movimentacao
    val armadura = (max(personagem.armorFromPower, personagem.armorBase) + personagem.naturalArmorFromRace).coerceAtLeast(0)
    val temArmaduraDeEquip = personagem.equipamentos.any { it.armadura != null }
    val bonusSemArmadura =
        if (personagem.heroisSemArmadura && !temArmaduraDeEquip) 2 else 0
    val chi = calcChi()
    val resistenciaTotal = resFinal + armadura + bonusSemArmadura
    val resistenciaTexto =
        if ((armadura + bonusSemArmadura) > 0) "${resFinal}(${resistenciaTotal})" else resFinal.toString()

    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    lines += "Ancestralidade: $ancestralidadeNome$monstroNome"
    if (personagem.coracaoCrystalSelecionado != null) {
        lines += "Coração de Cristal: ${personagem.coracaoCrystalSelecionado.nome}"
    }
    lines += ""

    lines += "Atributos derivados"
    lines += "Aparar: $aparar"
    lines += "Resistência: $resistenciaTexto"
    if (personagem.compendioArteDaGuerraAtivo) {
        lines += "Reserva de Chi: $chi"
    }
    if (personagem.regraFamaAtiva) {
        lines += "Fama: ${personagem.fama}"
    }
    if (personagem.dominio != null) {
        lines += "Domínio: ${personagem.dominio}"
    }
    lines += "Tamanho: $tamanho"
    lines += "Movimento: $mov"
    lines += ""

    lines += "Atributos"
    listaAtributos.forEach { attrKey ->
        val label = mapaAtributosDisplay[attrKey] ?: attrKey
        val valor = personagem.atributos[attrKey] ?: 4
        lines += "$label: ${valor.toDiceString()}"
    }
    lines += ""

    val idiomaRegex = Regex("^Idiomas\\s+(\\d+)$", RegexOption.IGNORE_CASE)
    val idiomaBase = listaPericias.firstOrNull { it.nome.equals("Idiomas", ignoreCase = true) }
    val idiomaExtras = personagem.pericias.keys
        .filter { idiomaRegex.matches(it) }
        .sortedBy { idiomaRegex.find(it)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE }
    val periciasOrdenadas = buildList {
        val seen = mutableSetOf<String>()
        listaPericias.forEach { per ->
            if (seen.add(per.nome.keyify())) {
                add(per.nome)
                if (per.nome.equals("Idiomas", ignoreCase = true)) {
                    addAll(idiomaExtras)
                }
            }
        }
    }
    val periciasParaMostrar = periciasOrdenadas.mapNotNull { nome ->
        val basePericia = listaPericias.firstOrNull { it.nome == nome }
            ?: idiomaBase?.takeIf { idiomaRegex.matches(nome) }
        val raw = personagem.pericias[nome] ?: 0
        if (basePericia == null) return@mapNotNull null
        val shouldShow = raw > 0
        if (shouldShow) nome to raw else null
    }

    lines += "Perícias"
    if (periciasParaMostrar.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        periciasParaMostrar.forEach { (nome, raw) ->
            val note = personagem.notasPericia[nome]
            val noteStr = if (!note.isNullOrBlank()) " ($note)" else ""
            val displayNome = if (idiomaRegex.matches(nome)) "Idiomas" else nome
            lines += "$displayNome: d$raw$noteStr"
        }
    }
    lines += ""

    lines += "Recursos & Equipamentos"
    if (personagem.usaRiqueza && (personagem.dadoRiqueza != null && personagem.modoProgressaoAtivo)) {
        lines += "Riqueza: d${personagem.dadoRiqueza}"
    } else {
        lines += "Dinheiro restante: ${personagem.dinheiro}"
    }
    if (personagem.equipamentos.isEmpty()) {
        lines += "Equipamentos: – Nenhum"
    } else {
        lines += "Equipamentos:"
        personagem.equipamentos.forEach { eq ->
            val nomeEq = if (showOfficialNames && !eq.originalName.isNullOrBlank()) eq.originalName else eq.nome
            lines += "• $nomeEq"
        }
    }
    lines += ""

    // Create a lookup map: ID -> Best Definition
    val definitionMap = listaVantagens
        .groupBy { it.id.keyify() }
        .mapValues { (_, candidates) ->
            candidates.maxByOrNull { CriadorState.getOriginPriority(it.origem) }!!
        }

    lines += "Vantagens"
    if (personagem.vantagens.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        val nomesVantagens = personagem.vantagens.mapNotNull { id ->
            val vant = definitionMap[id.keyify()] ?: return@mapNotNull null

            val escolha = vantagemChoices[vant.id]?.removeFirstOrNull()
                ?.takeIf { it.isNotBlank() }
            val rawName = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName else vant.nome

            val baseNome = if (vant.id == "antecedente_arcano_milagres" && personagem.celestialAAMilagresDesabilitado) {
                "$rawName (DESABILITADO)"
            } else {
                rawName
            }
            if (escolha != null) "$baseNome (${escolha.trim()})" else baseNome
        }
        lines += nomesVantagens.joinToString(", ")
    }

    // Annotations for Armor Interference/Restrictions & Class Features (Pathfinder)
    if (personagem.compendioPathfinderAtivo) {
        val classEdges = personagem.vantagens.mapNotNull { definitionMap[it.keyify()] }
            .filter { it.categoria == Categoria.CLASSE }

        if (classEdges.isNotEmpty()) {
            lines += "Características de Classe"
            classEdges.forEach { vant ->
                val tags = vant.requisitos.tags
                lines += "• ${vant.nome}"

                // Armor Restrictions/Interference
                if (tags.contains("INTERFERENCIA_ARMADURA_LEVE")) {
                    lines += "  - Armadura: Interferência (Leve). Subtraem 4 de rolagens de Perícia Arcana, Agilidade e perícias baseadas em Agilidade se usarem armaduras ou escudos médios/pesados."
                }
                if (tags.contains("INTERFERENCIA_ARMADURA_QUALQUER")) {
                    lines += "  - Armadura: Interferência (Qualquer). Subtraem 4 de rolagens de Perícia Arcana, Agilidade e perícias baseadas em Agilidade se usarem qualquer armadura ou escudo."
                }
                if (tags.contains("RESTRICAO_ARMADURA_LEVE")) {
                    lines += "  - Armadura: Restrição (Leve). Subtraem 4 de rolagens de Agilidade e perícias baseadas em Agilidade se usarem armaduras ou escudos médios/pesados."
                }
                if (tags.contains("RESTRICAO_ARMADURA_MEDIA")) {
                    lines += "  - Armadura: Restrição (Média). Subtraem 4 de rolagens de Agilidade e perícias baseadas em Agilidade se usarem armaduras ou escudos pesados."
                }
                if (tags.contains("RESTRICAO_ARMADURA_QUALQUER")) {
                    lines += "  - Armadura: Restrição (Qualquer). Subtraem 4 de rolagens de Agilidade e perícias baseadas em Agilidade se usarem qualquer armadura ou escudo."
                }

                // Detect Magic (General Note for AB classes)
                if (!vant.subtipoArcano.isNullOrBlank()) {
                    lines += "  - Detectar Magia: Pode sentir auras mágicas/divinas a até 5 quadros (10m) como uma ação."
                }

                // Extract features from description (naive bullet point extraction)
                // Assuming bullets start with "•" or "-" or are distinctive
                // User Request: Show only the indicator (Name), not the full text.
                // Example: "• FÚRIA: Description..." -> "• FÚRIA"
                val descriptionLines = vant.descricao.lines()
                val featureLines = descriptionLines.filter { it.trim().startsWith("•") || it.trim().startsWith("-") }
                featureLines.forEach { f ->
                    val cleanLine = f.trim()
                    val titlePart = if (cleanLine.contains(":")) {
                        cleanLine.substringBefore(":")
                    } else if (cleanLine.contains(".")) {
                        cleanLine.substringBefore(".")
                    } else {
                        cleanLine
                    }
                    lines += "  $titlePart"
                }
            }
            lines += ""
        }
    } else {
        // Legacy/Generic display for other settings
        personagem.vantagens.forEach { vantId ->
            val vant = definitionMap[vantId.keyify()]
            if (vant != null) {
                val tags = vant.requisitos.tags
                if (tags.contains("INTERFERENCIA_ARMADURA_LEVE")) {
                    lines += "• Interferência de Armadura (Leve): -4 em perícias de Agilidade e Arcanas se usar armadura média/pesada."
                }
                if (tags.contains("INTERFERENCIA_ARMADURA_QUALQUER")) {
                    lines += "• Interferência de Armadura (Qualquer): -4 em perícias de Agilidade e Arcanas se usar qualquer armadura."
                }
                if (tags.contains("RESTRICAO_ARMADURA_LEVE")) {
                    lines += "• Restrição de Armadura (Leve): -4 em perícias de Agilidade se usar armadura média/pesada."
                }
                if (tags.contains("RESTRICAO_ARMADURA_MEDIA")) {
                    lines += "• Restrição de Armadura (Média): -4 em perícias de Agilidade se usar armadura pesada."
                }
                if (tags.contains("RESTRICAO_ARMADURA_QUALQUER")) {
                    lines += "• Restrição de Armadura (Qualquer): -4 em perícias de Agilidade se usar qualquer armadura."
                }
            }
        }
    }
    val habilidadesRaciais = ancestralidadeNomeObj?.habilidades?.map { it.nome } ?: emptyList()
    val allRacialTraits = (personagem.vantagensRaciais + habilidadesRaciais).distinctBy { it.keyify() }

    if (allRacialTraits.isNotEmpty()) {
        val displayVantagensRaciais = if (personagem.ancestralidade.keyify() == "SAURIOS") {
            allRacialTraits.map {
                if (it.keyify() == "PRONTIDAO") "Sentidos Aguçados" else it
            }
        } else {
            allRacialTraits
        }
        lines += "Características Raciais: ${displayVantagensRaciais.joinToString(", ")}"
    }
    lines += ""

    val desvantagensRaciaisComplicacoes = personagem.desvantagensRaciais.filter { desvantagem ->
        desvantagem.substringBefore("(").trim().keyify() in complicacoesNomeKeyset
    }
    val desvantagensRaciaisAnotacoes = personagem.desvantagensRaciais.filterNot { desvantagem ->
        desvantagem.substringBefore("(").trim().keyify() in complicacoesNomeKeyset
    }

    lines += "Complicações"
    val complicationKeys = complicacoesNomeadas.map { it.keyify() }.toMutableSet()
    val allComplicationsList = buildList {
        addAll(complicacoesNomeadas)
        addAll(transtornosNomeados.map { "$it (Transtorno)" })
        desvantagensRaciaisComplicacoes.forEach { comp ->
            val compKey = comp.substringBefore("(").trim().keyify()
            if (compKey !in complicationKeys) {
                add(comp)
                complicationKeys.add(compKey)
            }
        }
    }
    val complicacoesText = allComplicationsList
        .joinToString(", ")
        .ifBlank { "– Nenhuma" }
    lines += complicacoesText
    if (desvantagensRaciaisAnotacoes.isNotEmpty()) {
        lines += "Anotações Raciais: ${desvantagensRaciaisAnotacoes.joinToString(", ")}"
    }
    lines += ""

    if (personagem.poderes.isNotEmpty()) {
        lines += "Poderes arcanos"
        personagem.poderes.forEach { (arcanoKey, lista) ->
            val cleanKey = arcanoKey.uppercase().trim()
            val info = arcanoInfo[cleanKey]

            val details = if (cleanKey == "MISTICO") {
                "(10 PP)"
            } else if (info != null) {
                val (_, pp, foco) = info
                "($pp PP, $foco)"
            } else {
                ""
            }

            val labelBase = arcanoKey
                .lowercase()
                .replace('_', ' ')
                .replaceFirstChar { it.titlecase() }

            val label = if (details.isNotBlank()) "$labelBase $details" else labelBase

            lines += if (lista.isEmpty()) {
                "• $label: – nenhum poder escolhido"
            } else {
                val poderesComManifestacao = lista.map { poderId ->
                    val poderDef = listaPoderes.firstOrNull { it.id == poderId }
                    val displayNome = if (showOfficialNames && !poderDef?.id.isNullOrBlank()) {
                        // Logic for official names usually implies using a different property,
                        // but here we just have 'nome'. If we had 'originalName' in Poder, we'd use it.
                        // Poder struct only has 'nome'. Assuming 'nome' is what we want.
                        // If we wanted original names we'd need to update Poder model.
                        // For now, let's just use 'nome' if found, else ID.
                        poderDef?.nome ?: poderId
                    } else {
                        poderDef?.nome ?: poderId
                    }

                    val manifestacao = personagem.manifestacoesPoderes[poderId]
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                    if (manifestacao != null) "$displayNome (${manifestacao})" else displayNome
                }
                "• $label: ${poderesComManifestacao.joinToString(", ")}"
            }
        }
        lines += ""
    }

    if (personagem.modoSupers &&
        (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
    ) {
        lines += "Superpoderes"

        if (personagem.gastosPorPoder.isEmpty()) {
            lines += "– Nenhum superpoder registrado"
        } else {
            personagem.gastosPorPoder.forEach { (poderId, custo) ->
                lines += "• $poderId: $custo SP"
            }
        }

        lines += "Superpontos: ${personagem.superPontosTotais} (disponíveis: ${personagem.superPontosDisponiveis})"
        lines += "Limite por poder: ${personagem.limitePorPoderPadrao}"
        lines += ""
    }

    if (personagem.anotacoes.isNotBlank()) {
        lines += "Anotações"
        personagem.anotacoes.lines().forEach { linha -> lines += linha }
    }

    // Auto-notes for specific advantages
    val vantKeys = personagem.vantagens.map { it.keyify() }.toSet()
    if ("HERANCA" in vantKeys) {
        lines += "• Item de Herança: Escolha um item mundano ou mágico."
    }
    if ("CAVALEIRO" in vantKeys) {
        lines += "• Cavaleiro: Recebe Cavalo de Guerra, Sela, Armadura e Armas iniciais."
    }
    if ("MONTARIA" in vantKeys) {
        lines += "• Montaria: Recebe um cavalo leal."
    }

    return lines
}

private fun complicationDisplayNames(rawIds: List<String>, modoOficialAtivo: Boolean): List<String> {
    val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }
    return rawIds.map { compId ->
        val comp = mapPorId[compId.keyify()]
        if (comp != null) {
            if (modoOficialAtivo && !comp.originalName.isNullOrBlank()) {
                comp.originalName
            } else {
                comp.name
            }
        } else {
            compId.replace('_', ' ').titleCase()
        }
    }
}
