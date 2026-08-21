package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.PowerEffect
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.GenericNameMapper
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.toEditionDisplayName
import com.example.swadebuilder.util.toFancyTitleCase
import kotlin.math.max

private fun formatRacialAnnotationDisplay(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isBlank()) return trimmed

    val hasNarrativePunctuation = trimmed.any { it == ':' || it == ';' || it == '.' || it == '!' || it == '?' }
    return if (hasNarrativePunctuation) trimmed else trimmed.toFancyTitleCase()
}

fun buildAncestralidadeDisplay(personagem: MeuPersonagem, ancestralidadeNomeBase: String? = null): String {
    val baseRaw = (ancestralidadeNomeBase ?: personagem.ancestralidade)
    val baseOriginal = if (!EditionConfig.isFullEdition) {
        baseRaw.toEditionDisplayName().let { GenericNameMapper.map(it) }.toFancyTitleCase()
    } else {
        baseRaw.toFancyTitleCase()
    }
    val isHuman = baseOriginal.keyify().contains("HUMANO")

    if (personagem.compendioArteDaGuerraAtivo && isHuman) {
        val sign = personagem.signoAdgSelecionado
        val signLabel = if (sign.isNullOrBlank() || sign.equals("Nenhum", ignoreCase = true)) {
            "Sem Signo"
        } else {
            "Signo ${sign.toFancyTitleCase()}"
        }
        return "Humano $signLabel"
    }

    val sufixo = when {
        isHuman && !personagem.pacoteCulturalFantasiaSelecionado.isNullOrBlank() -> {
            val pack = personagem.pacoteCulturalFantasiaSelecionado
            if (pack.equals("Humano padrão", ignoreCase = true)) null else pack
        }
        baseOriginal.keyify().contains("DESCENDENTE ELEMENTAL") && !personagem.descendenteElementalSelecionado.isNullOrBlank() -> {
            personagem.descendenteElementalSelecionado
        }
        else -> null
    }

    return if (sufixo.isNullOrBlank()) baseOriginal else "$baseOriginal ($sufixo)"
}

// =================================================================================================
// SHARED SUMMARY BUILDER (Used by ResumoSection.kt)
// =================================================================================================

fun buildSummaryLines(
    personagem: MeuPersonagem,
    allAdvantages: List<Vantagem>,
    listaAncestralidades: List<RacialModifier>,
    listaMonstros: List<MonstroTemplate>,
    listaComplicacoes: List<Complicacao>,
    listaAtributos: List<String>,
    mapaAtributosDisplay: Map<String, String>,
    listaPericias: List<Pericia>,
    listaPoderes: List<Poder>,
    arcanoInfo: Map<String, Triple<Int, Int, String>>
): List<String> {
    val lines = mutableListOf<String>()

    val showOfficialNames = EditionConfig.isFullEdition && personagem.modoOficialAtivo

    val ancestralidadeNomeObj = listaAncestralidades
        .filter { it.nome.keyify() == personagem.ancestralidade }
        .filter { item ->
            when (val origin = item.origem.uppercase()) {
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
        ?: listaAncestralidades.firstOrNull { it.nome.keyify() == personagem.ancestralidade }

    val rawAncestralidadeNome = if (showOfficialNames && ancestralidadeNomeObj?.originalName != null) {
        ancestralidadeNomeObj.originalName
    } else {
        val baseName = ancestralidadeNomeObj?.nome ?: personagem.ancestralidade
        if (!EditionConfig.isFullEdition) baseName.toEditionDisplayName().let { GenericNameMapper.map(it) } else baseName
    }

    // Remove sufixos como (Buscatrilha), (Trilhador), etc.
    val ancestralidadeNome: String = rawAncestralidadeNome
        .replace(Regex("\\s*\\((Pathfinder|Buscatrilha|Trilhador|Mundo Ancestral)\\)"), "")
        .trim()
        .toFancyTitleCase()

    val monstroNome = if (personagem.modoMonstroAtivo) {
        val tipoNome = listaMonstros.find { it.id == personagem.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        " (Monstro: $tipoNome)"
    } else ""

    fun complicationDisplayNames(rawIds: List<String>, modoOficialAtivo: Boolean): List<String> {
        val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }
        fun normalizeForasteiro(text: String): String {
            val forasteiroWithDegree = Regex("""^FORASTEIRO\s*\((MENOR|MAIOR)\)$""", RegexOption.IGNORE_CASE)
            return if (forasteiroWithDegree.matches(text.trim())) "Forasteiro" else text
        }
        val showOfficial = EditionConfig.isFullEdition && modoOficialAtivo
        return rawIds.map { compId ->
            val comp = mapPorId[compId.keyify()]
            if (comp != null) {
                val baseName = if (showOfficial && !comp.originalName.isNullOrBlank()) {
                    comp.originalName!!.toFancyTitleCase()
                } else {
                    comp.nomeExibicao.toFancyTitleCase()
                }

                val severityStr = comp.severity.trim().lowercase()
                val isMenor = severityStr.contains("menor")
                val isMaior = severityStr.contains("maior")

                val sevDisplay = when {
                    isMenor && isMaior -> ""
                    isMenor -> " (Menor)"
                    isMaior -> " (Maior)"
                    else -> ""
                }

                // For racial complications, the user's selected complication degree is not stored in complications,
                // but the base complication's severity is shown if it is unambiguous.
                // However, character's standard selected complications degrees are stored in `personagem.complicacoesTipos`
                val userChoice = personagem.complicacoesTipos[compId]?.let {
                    val c = it.lowercase()
                    if (c.contains("menor")) " (Menor)"
                    else if (c.contains("maior")) " (Maior)"
                    else ""
                } ?: sevDisplay

                normalizeForasteiro("$baseName$userChoice")
            } else {
                normalizeForasteiro(compId.replace('_', ' ').toFancyTitleCase())
            }
        }
    }


    val vantagensNomeKey: List<String> = allAdvantages
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

        val isDeaders = personagem.ancestralidade.keyify().contains("DEADERS")
        val hasApararBaixo = isDeaders || personagem.desvantagensRaciais.any { it.keyify() == "APARAR BAIXO" || it.keyify() == "APARAR_BAIXO" }
        val apararBaixoMod = if (hasApararBaixo) -2 else 0

        val isSerranos = personagem.ancestralidade.keyify().contains("SERRANOS")
        val serranosApararMod = if (isSerranos) 2 else 0

        val racialParryBonus = (personagem.vantagensRaciais + personagem.desvantagensRaciais)
            .sumOf { raw ->
                val normalized = raw.keyify().replace('_', ' ')
                Regex("""APARAR\s*([+-])\s*(\d+)""")
                    .find(normalized)
                    ?.let { match ->
                        val value = match.groupValues[2].toInt()
                        if (match.groupValues[1] == "-") -value else value
                    }
                    ?: 0
            }

        val garcaParryBonus =
            if (
                personagem.compendioArteDaGuerraAtivo &&
                personagem.ancestralidade.keyify().contains("HUMANO") &&
                personagem.signoAdgSelecionado.equals("Garça", ignoreCase = true)
            ) 1 else 0

        val total =
            base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusApararFromPower +
                apararBaixoMod + serranosApararMod + racialParryBonus + garcaParryBonus
        return total.coerceAtLeast(0)
    }

    fun calcChi(): Int {
        personagem.reservaChi?.let { return it }

        val espRaw = personagem.atributos["ESPIRITO"] ?: 0
        val racialPenalty = if (personagem.ancestralidade.keyify() == "TERRACOTA") 1 else 0
        val chiBonus = allAdvantages
            .filter { it.id in personagem.vantagens }
            .count { it.categoria == Categoria.CHI }

        return (espRaw / 2 - racialPenalty + chiBonus).coerceAtLeast(0)
    }

    val aparar = calcAparar()
    val resFinal = personagem.resistencia
    val tamanho = personagem.tamanho
    val mov = personagem.movimentacao
    val armadura = (max(personagem.armorFromPower, personagem.armorBase) + personagem.naturalArmorFromRace).coerceAtLeast(0)
    val chi = calcChi()
    val resistenciaTotal = resFinal + armadura
    val resistenciaTexto =
        if (armadura > 0) "${resFinal}(${resistenciaTotal})" else resFinal.toString()

    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    val ancestralidadeDisplay = buildAncestralidadeDisplay(personagem, ancestralidadeNome)
    lines += "$ancestralidadeDisplay$monstroNome"
    if (personagem.coracaoCrystalSelecionado != null) {
        val heartName = if (!EditionConfig.isFullEdition) GenericNameMapper.map(personagem.coracaoCrystalSelecionado.nome) else personagem.coracaoCrystalSelecionado.nome
        lines += "Coração de Cristal: $heartName"
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
    if (personagem.usaRequisicao) {
        lines += "Requisição: ${personagem.requisicao}"
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
            val displayNome = if (idiomaRegex.matches(nome)) "Idiomas" else nome.toFancyTitleCase()
            lines += "$displayNome: ${raw.toDiceString()}$noteStr"
        }
    }
    lines += ""

    if (personagem.mechasSelecionados.isNotEmpty()) {
        lines += "Mechas:"
        personagem.mechasSelecionados.forEach { m ->
            val extras = mutableListOf<String>()
            if (m.customizacoes.blindagem_extra > 0) extras += "Blindagem +${m.customizacoes.blindagem_extra}"
            if (m.customizacoes.propulsores) extras += "Propulsores"
            if (m.mods_instalados.isNotEmpty()) {
                extras += "Mods: " + m.mods_instalados.joinToString { it.nome }
            }
            if (m.armas_equipadas.isNotEmpty()) {
                extras += "Armas: " + m.armas_equipadas.joinToString()
            }
            val extraStr = if (extras.isNotEmpty()) " (${extras.joinToString("; ")})" else ""
            lines += "• ${m.nome}$extraStr"
        }
        lines += ""
    }

    if (personagem.ciberneticosInstalados.isNotEmpty()) {
        lines += "Cibernéticos Instalados:"
        personagem.ciberneticosInstalados.forEach { c ->
            val effStr = if (c.efeito.isNotBlank()) " (${c.efeito})" else ""
            lines += "• ${c.nome} [Tensão ${c.strain_custo}]$effStr"
        }
        lines += ""
    }

    lines += "Recursos & Equipamentos"
    if (personagem.usaRequisicao) {
        lines += "Requisição: ${personagem.requisicao}"
    } else if (personagem.usaRiqueza && (personagem.dadoRiqueza != null && personagem.modoProgressaoAtivo)) {
        lines += "Riqueza: ${personagem.dadoRiqueza!!.toDiceString()}"
    } else {
        lines += "Dinheiro restante: ${personagem.dinheiro}"
    }
    if (personagem.equipamentos.isEmpty()) {
        lines += "Equipamentos: – Nenhum"
    } else {
        lines += "Equipamentos:"
        personagem.equipamentos.forEach { eq ->
            val nomeEq = if (showOfficialNames && !eq.originalName.isNullOrBlank()) eq.originalName!!.toFancyTitleCase() else eq.nomeExibicao.toFancyTitleCase()
            lines += "• $nomeEq"
        }
    }
    lines += ""

    // Create a lookup map: ID -> Best Definition
    val definitionMap = allAdvantages
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
            val rawName = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName!!.toFancyTitleCase() else vant.nome.toFancyTitleCase()

            val baseNome = if (vant.id == "antecedente_arcano_milagres" && personagem.celestialAAMilagresDesabilitado) {
                "$rawName (DESABILITADO)"
            } else {
                rawName
            }
            if (escolha != null) "$baseNome (${escolha.trim().toFancyTitleCase()})" else baseNome
        }.distinct()
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
                lines += "• ${vant.nome.toFancyTitleCase()}"

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
    val habilidadesRaciaisBaseRaw = ancestralidadeNomeObj?.habilidades?.filter { it.category != "racial_hindrance" }?.map { it.nome } ?: emptyList()
    val isAvianosAveRapina = personagem.compendioSciFiAtivo &&
        personagem.ancestralidade.keyify() == "AVIANOS" &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify() == "FORMA ALIENIGENA" } &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify().startsWith("HABITANTE DE GRAVIDADE") }

    val isAquarianosSemiaquaticos = personagem.compendioSciFiAtivo &&
        personagem.ancestralidade.keyify() == "AQUARIANOS" &&
        personagem.vantagensRaciais.any {
            val key = it.substringBefore("(").trim().keyify()
            key.contains("SEMI") && key.contains("AQUATIC")
        }

    val isElfosComunitario = personagem.compendioSciFiAtivo &&
        personagem.ancestralidade.keyify() == "ELFOS" &&
        personagem.vantagensRaciais.any { it.substringBefore("(").trim().keyify() == "COMUNITARIO" }

    val isCentauxGazela = personagem.compendioSciFiAtivo &&
        personagem.ancestralidade.keyify() == "CENTAUX" &&
        personagem.vantagensRaciais.any {
            it.substringBefore("(").trim().keyify() == "MOVIMENTACAO +4"
        }

    val habilidadesRaciaisBase = habilidadesRaciaisBaseRaw.toMutableList().apply {
        // Defensive normalization for variant substitution when base ancestry definition is used.
        // If variant traits are present in character snapshot, hide replaced base traits.
        val racialTraitKeys = personagem.vantagensRaciais
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()
        if (personagem.ancestralidade.keyify() == "AQUARIANOS" &&
            racialTraitKeys.any { it.contains("SEMI") && it.contains("AQUATIC") }
        ) {
            removeAll { it.keyify() == "AQUATICO" || it.keyify() == "RESISTENCIA" }
        }

        if (personagem.ancestralidade.keyify() == "ELFOS" && racialTraitKeys.contains("COMUNITARIO")) {
            removeAll { it.keyify() == "DESASTRADO" }
        }

        if (personagem.ancestralidade.keyify().contains("HUMANO")) {
            val pack = personagem.pacoteCulturalFantasiaSelecionado
            if (!pack.isNullOrBlank() && !pack.equals("Humano padrão", ignoreCase = true)) {
                removeAll { it.keyify() == "ADAPTAVEL" }
            }
            if (personagem.compendioFantasiaAtivo) {
                when (pack) {
                    "Nômades do Deserto" -> {
                        if (none { it.keyify() == "FRAQUEZA_AMBIENTAL" }) {
                            add("Fraqueza Ambiental (Frio)")
                        }
                    }
                    "Povo da Montanha" -> {
                        if (none { it.keyify() == "FRAQUEZA_AMBIENTAL" }) {
                            add("Fraqueza Ambiental (Calor)")
                        }
                    }
                    "Povo do Mar" -> {
                        if (personagem.povoDoMarOpcao == "Penalidade em Cavalgar" && none { it.keyify() == "PENALIDADE_CAVALGAR" }) {
                            add("Penalidade em Cavalgar")
                        }
                    }
                }
            }
        }

        if (isCentauxGazela) {
            removeAll { it.keyify() == "MOVIMENTACAO +2" || it.keyify() == "TAMANHO +2" }
        }

        if (personagem.ancestralidade.keyify() == "DRACONIANOS") {
            removeAll { it.keyify() == "ARROGANTE" }
        }

        if (personagem.ancestralidade.keyify() == "MINERADORES GENETICOS" || personagem.ancestralidade.keyify() == "MINERADORES GENÉTICOS") {
            if (personagem.vantagensRaciais.any { it.keyify() == "ADAPTACAO GRAVITACIONAL" || it.keyify() == "ADAPTAÇÃO GRAVITACIONAL" } ||
                personagem.vantagens.any { it.keyify() == "ADAPTACAO_GRAVITACIONAL" }
            ) {
                removeAll { it.keyify() == "DEPENDENCIA ATMOSFERICA" || it.keyify() == "DEPENDÊNCIA ATMOSFÉRICA" || it.keyify() == "FORTE" }
            }
        }

        if (personagem.ancestralidade.keyify() == "ORACULOS" || personagem.ancestralidade.keyify() == "ORÁCULOS") {
            if (personagem.vantagensRaciais.any { it.keyify().contains("PODERES MISTICOS (TELEPATA)") || it.keyify().contains("PODERES MÍSTICOS (TELEPATA)") } ||
                personagem.vantagens.any { it.keyify() == "PODERES_MISTICOS" }
            ) {
                removeAll { it.keyify() == "NOCAO DO PERIGO" || it.keyify() == "NOÇÃO DO PERIGO" }
            }
        }

        if (personagem.ancestralidade.keyify() == "SERES SINTETICOS" || personagem.ancestralidade.keyify() == "SERES_SINTETICOS") {
            val hasVariantComplication = personagem.desvantagensRaciais.any {
                val key = it.keyify()
                key.contains("PROCURADO") || key.contains("FORASTEIRO")
            }
            if (hasVariantComplication) {
                removeAll { it.keyify() == "PROGRAMADO" }
            }
        }

        if (personagem.ancestralidade.keyify().contains("SOLDADOS GENETICOS") || personagem.ancestralidade.keyify().contains("SOLDADO GENETICO")) {
            val hasZeroG = personagem.vantagensRaciais.any { it.keyify().contains("ADAPTACAO GRAVITACIONAL") }
            if (hasZeroG) {
                removeAll { it.keyify() == "NERVOS DE ACO" }
            }
        }
    }

    val habilidadesRaciais = if (personagem.ancestralidade.keyify().contains("DESCENDENTE ELEMENTAL")) {
        val elem = personagem.descendenteElementalSelecionado?.keyify()
        habilidadesRaciaisBase
            .map { it.substringBefore("(").trim() }
            .filter {
                val key = it.keyify()
                when {
                    key == "AQUATICO" -> elem == "AGUA"
                    key == "AR INTERNO" -> elem == "AR"
                    key == "RAPIDO" -> elem == "FOGO"
                    key == "SOLIDO COMO ROCHA" -> elem == "TERRA"
                    key == "RESISTENCIA AMBIENTAL" || key == "FORASTEIRO" -> true
                    else -> true
                }
            }
    } else {
        habilidadesRaciaisBase
    }.toMutableList().apply {
        if (isAvianosAveRapina) {
            removeAll { it.keyify() == "FRAGIL" || it.keyify() == "NAO SABE NADAR" }
            if (none { it.keyify() == "HABITANTE DE GRAVIDADE ZERO/BAIXA" }) {
                add("Habitante de Gravidade Zero/Baixa")
            }
            if (none { it.keyify() == "FORMA ALIENIGENA" }) {
                add("Forma Alienígena")
            }
        }

        if (isAquarianosSemiaquaticos) {
            removeAll { it.keyify() == "AQUATICO" || it.keyify() == "RESISTENCIA" }
            if (none { it.keyify() == "SEMIAQUATICO" }) {
                add("Semiaquático")
            }
            if (none { it.keyify() == "TOQUE VENENOSO" }) {
                add("Toque Venenoso")
            }
        }

        if (isElfosComunitario) {
            removeAll { it.keyify() == "DESASTRADO" }
            if (none { it.keyify() == "COMUNITARIO" }) {
                add("Comunitário")
            }
        }
    }
    // Prioritize manual entries (habilidadesRaciais) over IDs (vantagensRaciais) to preserve formatting (e.g. "Adaptável" vs "ADAPTÁVEL")
    // Fix: Normalize IDs to Names using Ancestry Definition to prevent duplicates (e.g. "Armadura +2" vs "Armadura 2") and fix formatting (e.g. "Mordida/Garras")
    val racialAbilityMap = ancestralidadeNomeObj?.habilidades?.associateBy { it.id?.keyify() ?: it.nome.keyify() } ?: emptyMap()

    val isAdgHuman = personagem.compendioArteDaGuerraAtivo && personagem.ancestralidade.keyify().contains("HUMANO")
    val adgHumanSignTrait = if (isAdgHuman) {
        val sign = personagem.signoAdgSelecionado
        if (sign.isNullOrBlank() || sign.equals("Nenhum", ignoreCase = true)) {
            listOf("Sem Signo")
        } else {
            listOf("Signo ${sign.toFancyTitleCase()}")
        }
    } else {
        emptyList()
    }

    val allRacialTraits = if (isAdgHuman) {
        adgHumanSignTrait
    } else {
        val isTanukimimiWithPositiveThoughts = personagem.ancestralidade.keyify().contains("TANUKIMIMI") &&
            habilidadesRaciais.any { it.keyify() == "PENSAMENTOS POSITIVOS" }
        val isFeralWithInsanidade = personagem.ancestralidade.keyify() == "FERAL" &&
            habilidadesRaciais.any { it.keyify() == "INSANIDADE" }
        (habilidadesRaciais + personagem.vantagensRaciais)
            .filterNot { trait ->
                isElfosComunitario && trait.keyify() == "DESASTRADO"
            }
            .filterNot { trait ->
                isTanukimimiWithPositiveThoughts && trait.keyify() == "IMPULSO"
            }
            .filterNot { trait ->
                isFeralWithInsanidade && trait.keyify() == "FURIOSO"
            }
            .filterNot { trait ->
                isCentauxGazela && (trait.keyify() == "MOVIMENTACAO +2" || trait.keyify() == "TAMANHO +2")
            }
            .filterNot { trait ->
                personagem.ancestralidade.keyify() == "SERRANOS" && trait.keyify() == "NOCAO DE PERIGO"
            }
            .filterNot { it.keyify() == Constants.ID_AA_AGENT_SYN.keyify() }
            .map { trait ->
                val key = trait.keyify()
                if (personagem.ancestralidade.keyify() == "SAURIOS" && key == "PRONTIDAO") {
                    "Sentidos Aguçados"
                } else {
                    // 1. Check Advantages (Grantable Edges)
                    val vant = definitionMap[key]
                    if (vant != null) {
                        if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName!!.toFancyTitleCase() else vant.nome.toFancyTitleCase()
                    } else {
                        // 2. Check Racial Abilities (Definition Name)
                        val ability = racialAbilityMap[key]
                        if (ability != null) {
                            // Skin: Nekomimi "Fortuna Dá" should display as "Sorte" (book label),
                            // while keeping behavior textual (not a free Edge).
                            if (ability.id?.keyify() == "FORTUNA_DA" || ability.nome.keyify() == "FORTUNA DA") {
                                "Sorte"
                            } else if (personagem.ancestralidade.keyify() == "POVO RATO" && (ability.id?.keyify() == "FOBIA" || ability.nome.keyify() == "FOBIA")) {
                                "Fobia - Gatos (Menor)"
                            } else {
                                // Use the display name from JSON (preserves symbols like '/')
                                // But ensure consistent casing (Title Case) unless punctuation suggests otherwise
                                val formatted = formatRacialAnnotationDisplay(ability.nome)
                                if (!EditionConfig.isFullEdition) GenericNameMapper.map(formatted) else formatted
                            }
                        } else {
                            // 3. Fallback
                            val formatted = trait.toFancyTitleCase()
                            if (!EditionConfig.isFullEdition) GenericNameMapper.map(formatted) else formatted
                        }
                    }
                }
            }
            .distinctBy { it.keyify() } // Deduplicate BY resolved name
    }

    if (allRacialTraits.isNotEmpty()) {
        lines += "Características Raciais: ${allRacialTraits.joinToString(", ")}"
    }
    lines += ""

    fun complicationWithSeverity(raw: String): String {
        val forasteiroWithDegree = Regex("""^FORASTEIRO\s*\((MENOR|MAIOR)\)$""", RegexOption.IGNORE_CASE)
        if (forasteiroWithDegree.matches(raw.trim())) return "Forasteiro"
        if (raw.contains("(")) return raw
        val compKey = raw.substringBefore("(").trim().keyify()
        if (compKey == "FORASTEIRO") return "Forasteiro"
        val def = listaComplicacoes.firstOrNull { comp ->
            comp.name.keyify() == compKey || (comp.originalName?.keyify() == compKey)
        } ?: return raw
        val sev = def.severity.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return "$raw ($sev)"
    }

    val desvantagensRaciaisComplicacoes = personagem.desvantagensRaciais
        .filter { desvantagem ->
            desvantagem.substringBefore("(").trim().keyify() in complicacoesNomeKeyset
        }
        .map { complicationWithSeverity(it) }
    val desvantagensRaciaisAnotacoes = personagem.desvantagensRaciais
        .filterNot { desvantagem ->
            desvantagem.substringBefore("(").trim().keyify() in complicacoesNomeKeyset
        }
        .filterNot { desvantagem ->
            if (!isAvianosAveRapina) return@filterNot false
            val key = desvantagem.substringBefore("(").trim().keyify()
            key == "FORMA ALIENIGENA" || key.startsWith("SENTIDOS AGUCADOS")
        }

    lines += "Complicações"
    val complicationKeys = complicacoesNomeadas.map { it.substringBefore("(").trim().keyify() }.toMutableSet()
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
        lines += "Anotações Raciais: ${desvantagensRaciaisAnotacoes.joinToString(", ") { formatRacialAnnotationDisplay(it) }}"
    }
    lines += ""

    val isPathfinderGnome = personagem.compendioPathfinderAtivo &&
            personagem.ancestralidade.uppercase().contains("GNOMO")

    if (personagem.poderes.isNotEmpty() || isPathfinderGnome) {
        val filteredPowers = personagem.poderes.filterKeys { key ->
            val cleanKey = key.uppercase().trim()
            cleanKey != "CANALIZAR CRISTAL"
        }

        if (filteredPowers.isNotEmpty() || isPathfinderGnome) {
            lines += "Poderes arcanos"

            if (isPathfinderGnome) {
                val astucia = personagem.atributos["Astúcia"] ?: 4
                val fe = personagem.pericias["Fé"] ?: 0
                val conjurar = personagem.pericias["Conjurar"] ?: 0
                val focoMax = maxOf(astucia, fe, conjurar)
                val astuciaName = mapaAtributosDisplay["Astúcia"] ?: "Astúcia"
                val focoNome = when {
                    focoMax == astucia -> astuciaName
                    focoMax == fe -> "Fé"
                    else -> "Conjurar"
                }

                // If they have other powers, their PP adds up elsewhere, but the trait gives 1 PP specifically for these if no other AB
                val abCount = filteredPowers.size
                val ppText = if (abCount == 0) " (1 PP)" else ""
                lines += "• Truques: $focoNome$ppText - Iluminar, Som, Telecinese, Amigo das Feras"
            }

            val hasStandardAB = filteredPowers.keys.any { it.uppercase().trim() != "MISTICO" }

            filteredPowers.forEach { (arcanoKey, lista) ->
                val cleanKey = arcanoKey.uppercase().trim()
                val info = arcanoInfo[cleanKey]

                val details = if (cleanKey == "MISTICO") {
                    val gnomeBonus = if (isPathfinderGnome && !hasStandardAB) 1 else 0
                    val finalPp = 10 + gnomeBonus
                    "($finalPp PP)"
                } else if (info != null) {
                    val (_, pp, foco) = info
                    val gnomeBonus = if (isPathfinderGnome) 1 else 0
                    val basePP = pp + personagem.bonusPoderExtra + gnomeBonus
                    "($basePP PP, $foco)"
                } else {
                    ""
                }

                val labelBase = arcanoKey
                    .lowercase()
                    .replace('_', ' ')
                    .toFancyTitleCase()
                    .let { if (!EditionConfig.isFullEdition) GenericNameMapper.map(it) else it }

                val label = if (details.isNotBlank()) "$labelBase $details" else labelBase

                lines += if (lista.isEmpty()) {
                    "• $label: – nenhum poder escolhido"
                } else {
                    val poderesComManifestacao = lista.map { poderId ->
                        val poderDef = listaPoderes.firstOrNull { it.id == poderId }
                        val baseNome = poderDef?.nome ?: poderId
                        var displayNome = if (!EditionConfig.isFullEdition) GenericNameMapper.map(baseNome) else baseNome
                        displayNome = displayNome.toFancyTitleCase()

                        // Text replacements for Pathfinder Místico (positive aspects only)
                        if (personagem.compendioPathfinderAtivo && cleanKey == "MISTICO") {
                            displayNome = displayNome
                                .replace("Aumentar/Reduzir Característica", "Aumentar Característica")
                                .replace("Morosidade/Velocidade", "Velocidade")
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
    }

    if (personagem.modoSupers &&
        (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
    ) {
        lines += "Superpoderes"

        if (personagem.gastosPorPoder.isEmpty()) {
            lines += "– Nenhum superpoder registrado"
        } else {
            personagem.gastosPorPoder.forEach { (poderId, custo) ->
                val cleanId = if (poderId.startsWith("sp_", ignoreCase = true)) {
                    poderId.substring(3)
                } else {
                    poderId
                }
                lines += "${cleanId.toFancyTitleCase()}: $custo SP"
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
