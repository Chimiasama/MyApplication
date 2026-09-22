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
import com.example.swadebuilder.model.RacialTraitEffect
import com.example.swadebuilder.model.RacialTraitPointCatalog
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

/**
 * Lista de "Características Raciais" (traços da própria ancestralidade, pelo skin dela —
 * ver comentário abaixo) — usada tanto pelo Resumo (`buildSummaryLines`, que só monta a
 * linha "Características Raciais: ...") quanto pelo PDF (`ResumoPdfReferenciador
 * .gerarFichaEmPdf`, que desenha isso como seção própria). Extraída pra função à parte pra
 * não duplicar essa lógica (bastante intrincada, cheia de casos por espécie) nos dois
 * lugares — um bug corrigido aqui vale pros dois de graça.
 */
fun buildRacialTraitsList(
    personagem: MeuPersonagem,
    allAdvantages: List<Vantagem>,
    ancestralidadeAtual: RacialModifier?,
    ancestralidadeNomeObj: RacialModifier?,
    especieIdAtual: String?,
    showOfficialNames: Boolean
): List<String> {
    val definitionMap = allAdvantages
        .groupBy { it.id.keyify() }
        .mapValues { (_, candidates) ->
            candidates.maxByOrNull { CriadorState.getOriginPriority(it.origem) }!!
        }

    val habilidadesRaciaisBaseRaw = (ancestralidadeAtual ?: ancestralidadeNomeObj)?.habilidades?.filter { it.category != "racial_hindrance" }?.map { it.nome } ?: emptyList()
    val isAvianosAveRapina = personagem.compendioSciFiAtivo &&
        especieIdAtual == "avianos" &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify() == "FORMA ALIENIGENA" } &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify().startsWith("HABITANTE DE GRAVIDADE") }

    val isAquarianosSemiaquaticos = personagem.compendioSciFiAtivo &&
        especieIdAtual == "aquarianos" &&
        personagem.vantagensRaciais.any {
            val key = it.substringBefore("(").trim().keyify()
            key.contains("SEMI") && key.contains("AQUATIC")
        }

    val isElfosComunitario = personagem.compendioSciFiAtivo &&
        especieIdAtual == "elfos" &&
        personagem.vantagensRaciais.any { it.substringBefore("(").trim().keyify() == "COMUNITARIO" }

    val isCentauxGazela = personagem.compendioSciFiAtivo &&
        especieIdAtual == "centaux" &&
        personagem.vantagensRaciais.any {
            it.substringBefore("(").trim().keyify() == "MOVIMENTACAO +4"
        }

    val habilidadesRaciaisBase = habilidadesRaciaisBaseRaw.toMutableList().apply {
        // Defensive normalization for variant substitution when base ancestry definition is used.
        // If variant traits are present in character snapshot, hide replaced base traits.
        val racialTraitKeys = personagem.vantagensRaciais
            .map { it.substringBefore("(").trim().keyify() }
            .toSet()
        if (especieIdAtual == "aquarianos" &&
            racialTraitKeys.any { it.contains("SEMI") && it.contains("AQUATIC") }
        ) {
            removeAll { it.keyify() == "AQUATICO" || it.keyify() == "RESISTENCIA" }
        }

        if (especieIdAtual == "elfos" && racialTraitKeys.contains("COMUNITARIO")) {
            removeAll { it.keyify() == "DESASTRADO" }
        }

        // Pacote Cultural de Humanos (Fantasia): Adaptável removido/Fraqueza
        // Ambiental e Penalidade em Cavalgar adicionados não são mais um
        // "when" hardcoded aqui — CriadorState.applyAncestryVariantAdjustments
        // já resolve isso direto em habilidades[] da raça (Adaptável
        // removido de verdade, traços negativos com category=
        // "racial_trait_negative"), então `habilidadesRaciaisBaseRaw` acima
        // (lido de `ancestralidadeAtual.habilidades`) já reflete os dois sem
        // precisar de ajuste manual aqui.

        if (isCentauxGazela) {
            removeAll { it.keyify() == "MOVIMENTACAO +2" || it.keyify() == "TAMANHO +2" }
        }

        if (especieIdAtual == "draconianos") {
            removeAll { it.keyify() == "ARROGANTE" }
        }

        if (especieIdAtual == "mineradores_geneticos") {
            if (personagem.vantagensRaciais.any { it.keyify() == "ADAPTACAO GRAVITACIONAL" || it.keyify() == "ADAPTAÇÃO GRAVITACIONAL" } ||
                personagem.vantagens.any { it.keyify() == "ADAPTACAO_GRAVITACIONAL" }
            ) {
                removeAll { it.keyify() == "DEPENDENCIA ATMOSFERICA" || it.keyify() == "DEPENDÊNCIA ATMOSFÉRICA" || it.keyify() == "FORTE" }
            }
        }

        if (especieIdAtual == "oraculos") {
            if (personagem.vantagensRaciais.any { it.keyify().contains("PODERES MISTICOS (TELEPATA)") || it.keyify().contains("PODERES MÍSTICOS (TELEPATA)") } ||
                personagem.vantagens.any { it.keyify() == "PODERES_MISTICOS" }
            ) {
                removeAll { it.keyify() == "NOCAO DO PERIGO" || it.keyify() == "NOÇÃO DO PERIGO" }
            }
        }

        if (especieIdAtual == "seres_sinteticos") {
            val hasVariantComplication = personagem.desvantagensRaciais.any {
                val key = it.keyify()
                key.contains("PROCURADO") || key.contains("FORASTEIRO")
            }
            if (hasVariantComplication) {
                removeAll { it.keyify() == "PROGRAMADO" }
            }
        }

        if (especieIdAtual == "soldados_geneticos") {
            val hasZeroG = personagem.vantagensRaciais.any { it.keyify().contains("ADAPTACAO GRAVITACIONAL") }
            if (hasZeroG) {
                removeAll { it.keyify() == "NERVOS DE ACO" }
            }
        }
    }

    val habilidadesRaciais = if (especieIdAtual == "descendente_elemental") {
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
    val racialAbilityMap = (ancestralidadeAtual ?: ancestralidadeNomeObj)?.habilidades?.associateBy { it.id?.keyify() ?: it.nome.keyify() } ?: emptyMap()

    val isAdgHuman = personagem.compendioArteDaGuerraAtivo && especieIdAtual == "humano"
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

    return if (isAdgHuman) {
        adgHumanSignTrait
    } else {
        val isTanukimimiWithPositiveThoughts = especieIdAtual == "tanukimimi" &&
            habilidadesRaciais.any { it.keyify() == "PENSAMENTOS POSITIVOS" }

        // Toda habilidade `category == "racial_edge"` da raça concede uma Vantagem de
        // verdade (mesmo mecanismo de `vantagensGratisEfetivas()`, RacialModifier.kt) —
        // essa Vantagem concedida entra em `personagem.vantagensRaciais` (pelo id/
        // targetRef, pra lógica de requisito/automação em outro lugar do app) E a
        // habilidade em si já entra em `habilidadesRaciais` (pelo `nome`, o skin da
        // raça pra ela, ex.: Sáurios "Sentidos Aguçados" concede a Vantagem
        // "Prontidão"). Sem esse filtro, as duas apareciam juntas aqui ("Sentidos
        // Aguçados, Prontidão") — só que a segunda é a MESMA coisa, só sem o skin. Só
        // sobra em `vantagensRaciaisSemSkinEstatico` uma Vantagem concedida que NÃO
        // vem de uma habilidade estática da própria raça (ex.: injetada em tempo de
        // execução por uma Variante custom, sem entrada correspondente em
        // `habilidades[]`) — essa aparece aqui do jeito normal (sem skin pra usar).
        val vantagensCobertasPorHabilidadeEstatica = (ancestralidadeAtual ?: ancestralidadeNomeObj)
            ?.habilidades
            ?.filter { it.category == "racial_edge" }
            ?.map { hab -> (hab.targetRef?.takeIf { it.isNotBlank() } ?: hab.id ?: hab.nome).keyify() }
            ?.toSet()
            ?: emptySet()
        val vantagensRaciaisSemSkinEstatico = personagem.vantagensRaciais
            .filterNot { it.keyify() in vantagensCobertasPorHabilidadeEstatica }

        // isFeralWithInsanidade removido: "Insanidade" (habilidade única que
        // mencionava Furioso E Sanguinário no texto) virou dois traços de
        // verdade — SANGUINARIO (Complicação, habilidade própria "Insanidade
        // (Sanguinário)") e Furioso (Vantagem real, concedida via
        // vantagensGratis) — não tem mais duplicata pra esconder aqui, Furioso
        // deve aparecer normalmente como qualquer outra Vantagem concedida.
        (habilidadesRaciais + vantagensRaciaisSemSkinEstatico)
            .filterNot { trait ->
                isElfosComunitario && trait.keyify() == "DESASTRADO"
            }
            .filterNot { trait ->
                isTanukimimiWithPositiveThoughts && trait.keyify() == "IMPULSO"
            }
            .filterNot { trait ->
                isCentauxGazela && (trait.keyify() == "MOVIMENTACAO +2" || trait.keyify() == "TAMANHO +2")
            }
            .filterNot { it.keyify() == Constants.ID_AA_AGENT_SYN.keyify() }
            .map { trait ->
                val key = trait.keyify()
                // 1. Check Advantages (Grantable Edges)
                val vant = definitionMap[key]
                if (vant != null) {
                    if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName.toFancyTitleCase() else vant.nome.toFancyTitleCase()
                } else {
                    // 2. Check Racial Abilities (Definition Name)
                    val ability = racialAbilityMap[key]
                    if (ability != null) {
                        // Use the display name from JSON (preserves symbols like '/')
                        // But ensure consistent casing (Title Case) unless punctuation suggests otherwise
                        val formatted = formatRacialAnnotationDisplay(ability.nome)
                        if (!EditionConfig.isFullEdition) GenericNameMapper.map(formatted) else formatted
                    } else {
                        // 3. Fallback
                        val formatted = trait.toFancyTitleCase()
                        if (!EditionConfig.isFullEdition) GenericNameMapper.map(formatted) else formatted
                    }
                }
            }
            .distinctBy { it.keyify() } // Deduplicate BY resolved name
    }
}

fun buildAncestralidadeDisplay(
    personagem: MeuPersonagem,
    ancestralidadeNomeBase: String? = null,
    // Id estável da espécie (RacialModifier.especieId) já resolvido pelo
    // chamador, quando disponível — ver `especieIdAtual` em
    // buildSummaryLines(). Fica null pra qualquer raça customizada, então
    // uma raça custom com nome parecido de "Humano"/"Descendente Elemental"
    // nunca aciona esse sufixo por engano. Opcional (default null) só para
    // não quebrar chamador que ainda não tenha o id resolvido à mão — nesse
    // caso cai no heurístico por nome de exibição de antes.
    especieId: String? = null
): String {
    val baseRaw = (ancestralidadeNomeBase ?: personagem.ancestralidade)
    val baseOriginal = if (!EditionConfig.isFullEdition) {
        baseRaw.toEditionDisplayName().let { GenericNameMapper.map(it) }.toFancyTitleCase()
    } else {
        baseRaw.toFancyTitleCase()
    }
    val isHuman = if (especieId != null) especieId == "humano" else baseOriginal.keyify().contains("HUMANO")

    if (personagem.compendioArteDaGuerraAtivo && isHuman) {
        val sign = personagem.signoAdgSelecionado
        val signLabel = if (sign.isNullOrBlank() || sign.equals("Nenhum", ignoreCase = true)) {
            "Sem Signo"
        } else {
            "Signo ${sign.toFancyTitleCase()}"
        }
        return "Humano $signLabel"
    }

    val isDescendenteElemental = if (especieId != null) especieId == "descendente_elemental" else baseOriginal.keyify().contains("DESCENDENTE ELEMENTAL")

    val sufixo = when {
        isHuman && !personagem.pacoteCulturalFantasiaSelecionado.isNullOrBlank() -> {
            val pack = personagem.pacoteCulturalFantasiaSelecionado
            if (pack.equals("Humano padrão", ignoreCase = true) || pack.equals("Padrão", ignoreCase = true)) null else pack
        }
        isDescendenteElemental && !personagem.descendenteElementalSelecionado.isNullOrBlank() -> {
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
    arcanoInfo: Map<String, Triple<Int, Int, String>>,
    // Ancestralidade já resolvida (getAncestralidadeDef/currentAncestryDef), com os
    // ajustes de uma eventual Variante custom de raça já aplicados (traços
    // removidos já fora de .habilidades). Opcional pra não quebrar outro
    // caller que ainda não a tenha à mão; quando ausente, cai de volta pro
    // lookup cru por nome (não reflete Variante, comportamento de antes).
    ancestralidadeAtual: RacialModifier? = null
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

    // Identificador estável da "espécie" da ancestralidade resolvida (ver
    // RacialModifier.especieId) — usado por toda regra abaixo que precisa
    // saber "esta ficha é da raça oficial X" sem comparar nome de exibição.
    // Fica null para qualquer raça customizada pelo jogador (nunca
    // preenchido na criação customizada), então uma raça custom com nome
    // parecido de uma oficial nunca aciona essas regras por engano.
    val especieIdAtual = (ancestralidadeAtual ?: ancestralidadeNomeObj)?.especieId
        // Fallback defensivo: a resolução acima (ancestralidadeNomeObj) exige
        // `personagem.ancestralidade` já perfeitamente normalizado (mesmo
        // keyify do nome do catálogo), e pode falhar silenciosamente se não
        // estiver (ex.: acento remanescente por alguma inconsistência a
        // montante). Refaz a busca normalizando os dois lados, só para achar
        // o especieId — não substitui ancestralidadeNomeObj em si, que
        // continua controlando nome/habilidades exibidos como sempre.
        ?: listaAncestralidades.firstOrNull { it.nome.keyify() == personagem.ancestralidade.keyify() }?.especieId

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
                    comp.originalName.toFancyTitleCase()
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


    val complicacoesNomeadas: List<String> = complicationDisplayNames(personagem.complicacoes, showOfficialNames)
    val transtornosNomeados: List<String> = complicationDisplayNames(personagem.transtornos, showOfficialNames)
    val complicacoesNomeKeyset = listaComplicacoes
        .flatMap { comp -> listOfNotNull(comp.name, comp.originalName) }
        .map { it.keyify() }
        .toSet()

    val vantagemChoices: MutableMap<String, MutableList<String>> = personagem.advantageChoices
        .mapValues { it.value.toMutableList() }
        .toMutableMap()

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
            if (personagem.vantagens.contains(Constants.ID_BLOQUEAR)) 1 else 0
        val bloquearAprimoradoBonus =
            if (personagem.vantagens.contains(Constants.ID_BLOQUEAR_APRIMORADO)) 1 else 0

        val hasApararBaixo = personagem.desvantagensRaciais.any { it.keyify() == "APARAR BAIXO" || it.keyify() == "APARAR_BAIXO" }
        val apararBaixoMod = if (hasApararBaixo) -2 else 0

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

        // Bônus/penalidade de Aparar vindo de um traço racial ESTÁTICO (ex.:
        // Garça "Aparar +1 (Garça)", id="APARAR"; Tanukimimi "Aparar Baixo",
        // id="APARAR_BAIXO") — lido pelo mesmo catálogo genérico
        // (RacialTraitPointCatalog.efeitoDe/ApararBonus) que ModifierEngine
        // já usa pra qualquer raça, em vez de checar por Signo/raça
        // específica. Antes só a Garça tinha esse bônus aqui, hardcoded por
        // id de Signo (achado real: Tanukimimi tem o traço oposto,
        // "APARAR_BAIXO", category=racial_trait_negative — nunca cai em
        // `desvantagensRaciais`, então o `apararBaixoMod` acima nunca o via;
        // esse scan genérico cobre os dois, sem precisar saber o nome de
        // nenhuma raça).
        val racialTraitApararBonus = (ancestralidadeAtual ?: ancestralidadeNomeObj)
            ?.habilidades
            ?.sumOf { hab ->
                val efeito = RacialTraitPointCatalog.efeitoDe(hab.resolvedTraitId(), hab.targetRef, hab.value)
                if (efeito is RacialTraitEffect.ApararBonus) efeito.valor * hab.vezes.coerceAtLeast(1) else 0
            } ?: 0

        val total =
            base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusApararFromPower +
                apararBaixoMod + racialParryBonus + racialTraitApararBonus
        return total.coerceAtLeast(0)
    }

    fun calcChi(): Int {
        personagem.reservaChi?.let { return it }

        val espRaw = personagem.atributos["ESPIRITO"] ?: 0
        val racialPenalty = if (especieIdAtual == "terracota") 1 else 0
        // Só `pontos_de_chi` aumenta a Reserva Máxima (+4 por compra, ver
        // CriadorState.reservaChi) — as outras Vantagens de categoria CHI são Técnicas que
        // GASTAM Chi já existente, não somam nada à reserva.
        val chiBonus = 4 * personagem.vantagens.count { it == "pontos_de_chi" }

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

    // Também usado mais abaixo (desvantagensRaciaisAnotacoes) além de dentro de
    // buildRacialTraitsList() — mantido aqui igual, cálculo puro e barato.
    val isAvianosAveRapina = personagem.compendioSciFiAtivo &&
        especieIdAtual == "avianos" &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify() == "FORMA ALIENIGENA" } &&
        personagem.desvantagensRaciais.any { it.substringBefore("(").trim().keyify().startsWith("HABITANTE DE GRAVIDADE") }

    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    val ancestralidadeDisplay = buildAncestralidadeDisplay(personagem, ancestralidadeNome, especieIdAtual)
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
    if (personagem.vantagens.contains("ciborgue")) {
        lines += ""
        lines += "Aviso (Vantagem Ciborgue): o app não desconta dinheiro, então os $20K em implantes " +
            "que a Vantagem concede de graça precisam ser adicionados manualmente em Cibernéticos, com o " +
            "aval do mestre. Além disso, pelo livro o Ciborgue não faz mais testes de cura natural (deve " +
            "ser consertado) e exige uma Complicação Maior extra ligada aos implantes ou uma rolagem " +
            "permanente na tabela de Efeitos Colaterais — nenhum dos dois é aplicado automaticamente aqui."
    }
    if (!personagem.scifiVariant.isNullOrBlank()) {
        lines += "Variante Racial: ${personagem.scifiVariant}"
    }
    if (personagem.samuraiPosturasSelecionadas.isNotEmpty()) {
        lines += "Posturas de Samurai: ${personagem.samuraiPosturasSelecionadas.joinToString(", ")}"
    }
    (personagem.dominioClerigoSelecionado ?: personagem.dominioClerigoPathfinderSelecionado)?.let {
        lines += "Domínio do Clérigo: $it"
    }
    lines += "Tamanho: $tamanho"
    lines += "Movimento: $mov"
    lines += "Corrida: ${personagem.dadoCorrida}"
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
        lines += "Riqueza: ${personagem.dadoRiqueza.toDiceString()}"
    } else {
        lines += "Dinheiro restante: ${personagem.dinheiro}"
    }
    if (personagem.equipamentos.isEmpty()) {
        lines += "Equipamentos: – Nenhum"
    } else {
        lines += "Equipamentos:"
        personagem.equipamentos.forEach { eq ->
            val nomeEq = if (showOfficialNames && !eq.originalName.isNullOrBlank()) eq.originalName.toFancyTitleCase() else eq.nomeExibicao.toFancyTitleCase()
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
            val rawName = if (showOfficialNames && !vant.originalName.isNullOrBlank()) vant.originalName.toFancyTitleCase() else vant.nomeExibicao.toFancyTitleCase()

        val isCustom = vant.origem.equals("CUSTOM", ignoreCase = true) || vant.id.startsWith("custom:") || vant.id.startsWith("fanmade:")
        val customBadge = if (isCustom) " ⓒ" else ""
            val baseNome = if (vant.id == "antecedente_arcano_milagres" && personagem.celestialAAMilagresDesabilitado) {
            "$rawName (DESABILITADO)$customBadge"
            } else {
            "$rawName$customBadge"
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
    val allRacialTraits = buildRacialTraitsList(
        personagem = personagem,
        allAdvantages = allAdvantages,
        ancestralidadeAtual = ancestralidadeAtual,
        ancestralidadeNomeObj = ancestralidadeNomeObj,
        especieIdAtual = especieIdAtual,
        showOfficialNames = showOfficialNames
    )

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
            especieIdAtual == "gnomo"

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

            // "MÚLTIPLOS ANTECEDENTES ARCANOS" (Fantasia/Horror/Pathfinder/Sci-Fi, texto
            // idêntico nos quatro livros): usa a MAIOR reserva inicial de Pontos de Poder
            // entre todos os Antecedentes Arcanos E Poderes Místicos ativos, compartilhada
            // — Místico (10 PP fixos) entra nesse máximo igual a qualquer outro, não uma
            // conta separada (bug real: um personagem com Místico E outro Antecedente
            // Arcano via dois números de PP diferentes em vez de uma reserva só). O livro
            // Básico tem sua PRÓPRIA regra opcional de múltiplos Antecedentes Arcanos, com
            // pool separado por Antecedente — não usa esse compartilhamento, então o gate
            // abaixo cai pra fórmula antiga (independente) fora desses 4 livros; Místico só
            // existe dentro deles, então nunca aparece no fallback.
            val usaReservaCompartilhada = personagem.compendioFantasiaAtivo || personagem.compendioHorrorAtivo ||
                personagem.compendioPathfinderAtivo || personagem.compendioSciFiAtivo
            val sharedPP = if (usaReservaCompartilhada) {
                val chavesArcano = filteredPowers.keys.map { it.uppercase().trim() }
                val maxBaseOutros = chavesArcano.filter { it != "MISTICO" }.mapNotNull { arcanoInfo[it]?.second }.maxOrNull() ?: 0
                val temMistico = chavesArcano.contains("MISTICO")
                val maxBaseCompartilhado = if (temMistico) maxOf(maxBaseOutros, 10) else maxBaseOutros
                val gnomeBonusCompartilhado = if (isPathfinderGnome) 1 else 0
                maxBaseCompartilhado + personagem.bonusPoderExtra + gnomeBonusCompartilhado
            } else 0

            filteredPowers.forEach { (arcanoKey, lista) ->
                val cleanKey = arcanoKey.uppercase().trim()
                val info = arcanoInfo[cleanKey]

                val details = if (cleanKey == "MISTICO") {
                    "($sharedPP PP)"
                } else if (info != null) {
                    val (_, pp, foco) = info
                    val ppExibido = if (usaReservaCompartilhada) sharedPP else pp + personagem.bonusPoderExtra
                    "($ppExibido PP, $foco)"
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
                    val poderesComManifestacao = lista.mapIndexed { poderIdx, poderId ->
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

                        // Manifestação escrita pelo próprio jogador (ver CriadorState.
                        // manifestacoesPoderes) — chave por posição na lista, não pelo id do
                        // poder, porque Novos Poderes permite repetir um poder já conhecido.
                        val manifestacao = personagem.manifestacoesPoderes["$arcanoKey#$poderIdx"]
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
