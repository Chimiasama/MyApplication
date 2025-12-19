package com.example.swadebuilder

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.util.keyify
import java.io.File
import java.io.FileOutputStream

/**
 * Fonte única para Resumo e PDF.
 * - Snapshot do state -> MeuPersonagem
 * - buildSummaryLines() -> usado por UI e PDF
 * - gerarFichaEmPdf() / produzirEExibirFichaPdf()
 */

// ✅ 1) Snapshot único do state
fun CriadorState.toMeuPersonagem(): MeuPersonagem {
    return MeuPersonagem(
        nome = this.nomePersonagem,
        atributos = this.valoresAtributos.mapValues { it.value.intValue },
        pericias = listaPericias.associate { per -> per.nome to this.rawTotal(per) },
        ancestralidade = this.ancestralidade,
        celestialAAMilagresDesabilitado = this.celestialAAMilagresDesabilitado,
        tropoSelecionadoId = this.tropoSelecionado?.id,
        vantagens = this.vantagensSelecionadas.map { it.id },
        advantageChoices = this.vantagensSelecionadas
            .groupBy { it.id }
            .mapValues { (_, list) ->
                list.mapNotNull { it.choice }.filter { it.isNotBlank() }
            },
        vantagensRaciais = this.vantagensRaciais.toList(),
        desvantagensRaciais = this.desvantagensRaciais.toList(),
        complicacoes = this.complicacoesSelecionadas
            .filterValues { it != null }
            .keys
            .map { it.id },
        // PROMPT 3: Include Transtornos in PDF snapshot
        transtornos = this.transtornos.map { it.id },
        equipamentos = this.equipamentosComprados.toList(),
        poderes = this.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
        manifestacoesPoderes = this.manifestacoesPoderes.toMap(),
        dinheiro = this.dinheiro,
        dadoRiqueza = if (this.usaRiqueza) this.dadoRiqueza else null,
        pontosRestantes = this.pontosVantagem,
        naturalArmorFromRace = this.naturalArmorFromRace,
        armorBase = this.armadura,

        // supers
        modoSupers = this.modoSupers,
        modoMonstroAtivo = this.modoMonstroAtivo,
        tipoMonstroSelecionado = this.tipoMonstroSelecionado,
        superPontosTotais = this.superPontosTotais,
        superPontosDisponiveis = this.superPontosDisponiveis,
        limitePorPoderPadrao = this.limitePorPoderPadrao,
        limiteFavorecido = this.limiteFavorecido,
        poderFavoritoId = this.poderFavoritoId,
        superInvestments = this.superInvestments.toList(),
        bonusApararFromPower = this.bonusApararFromPower,
        bonusResFromPower = this.bonusResFromPower,
        armorFromPower = this.armorFromPower,
        bonusMovimentacaoFromPower = this.bonusMovimentacaoFromPower,
        vantagensDePoder = this.vantagensDePoder.toSet(),
        gastosPorPoder = this.gastosPorPoder.toMap(),
        limiteDePoderDaCampanha = this.limiteDePoderDaCampanha,

        anotacoes = this.anotacoes,
        soldadoCargaAtivo = this.soldadoCargaAtivo,
        modoOficialAtivo = this.modoOficialAtivo,
        compendioArteDaGuerraAtivo = this.compendioArteDaGuerraAtivo,
        compendioDeadlandsAtivo = this.compendioDeadlandsAtivo,
        compendioHorrorAtivo = this.compendioHorrorAtivo,
        sanidade = if (this.compendioHorrorAtivo) this.valorSanidade() else null,
        dominio = if (this.compendioDeadlandsAtivo) this.valorDominio() else null,
        coracaoCrystalSelecionado = this.coracaoCrystalSelecionado,
        tecnicasIniciaisTropo = this.tecnicasIniciaisFromTropo,
        reservaChi = if (this.compendioArteDaGuerraAtivo) this.reservaChi else null,
        // PROMPT 5: Include Skill Notes in PDF snapshot
        notasPericia = this.notasPericia.toMap()
    )
}

private fun complicationDisplayNames(rawIds: List<String>, modoOficialAtivo: Boolean): List<String> {
    val showOfficialNames = EditionConfig.isFullEdition && modoOficialAtivo
    val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }

    return rawIds.map { compId ->
        val comp = mapPorId[compId.keyify()]
        if (comp != null) {
            if (showOfficialNames && !comp.originalName.isNullOrBlank()) {
                comp.originalName
            } else {
                comp.name
            }
        } else {
            compId.replace('_', ' ')
                .lowercase()
                .replaceFirstChar { it.titlecase() }
        }
    }
}

// ✅ 2) Abrir/compartilhar PDF
fun produzirEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
    val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")

    gerarFichaEmPdf(pdfFile, dadosDoPersonagem)

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Nenhum app de PDF encontrado.", Toast.LENGTH_SHORT).show()
    }
}

// ✅ 3) Texto-base do resumo (UI e PDF)
fun buildSummaryLines(personagem: MeuPersonagem): List<String> {
    val lines = mutableListOf<String>()

    val showOfficialNames = EditionConfig.isFullEdition && personagem.modoOficialAtivo

    val ancestralidadeNomeObj = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == personagem.ancestralidade }

    val ancestralidadeNome: String = if (showOfficialNames && ancestralidadeNomeObj?.originalName != null) {
        ancestralidadeNomeObj.originalName
    } else {
        ancestralidadeNomeObj?.nome ?: personagem.ancestralidade
    }

    val monstroNome = if (personagem.modoMonstroAtivo) {
        val tipoNome = listaMonstroTemplates.find { it.id == personagem.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        " (Monstro: $tipoNome)"
    } else ""

    val vantagensNomeKey: List<String> = listaVantagens
        .filter { it.id in personagem.vantagens }
        .map { it.nome.keyify() }
    val complicacoesNomeadas: List<String> = complicationDisplayNames(personagem.complicacoes, showOfficialNames)
    // PROMPT 3: Display Transtornos names
    val transtornosNomeados: List<String> = complicationDisplayNames(personagem.transtornos, showOfficialNames)

    val vantagemChoices: MutableMap<String, MutableList<String>> = personagem.advantageChoices
        .mapValues { it.value.toMutableList() }
        .toMutableMap()

    val allComplicationsKeys: List<String> =
        personagem.complicacoes + personagem.desvantagensRaciais + personagem.transtornos

    fun temComp(key: String): Boolean =
        allComplicationsKeys.any { it.keyify() == key }

    fun racialSize(): Int =
        listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
            ?.desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
            ?.substringAfter("TAMANHO")
            ?.trim()
            ?.toIntOrNull()
            ?: 0

    fun tamanhoTotal(): Int {
        val base = racialSize()
        val obesoBonus = if (temComp("OBESO")) 1 else 0
        val pequenoPenalty = if (temComp("PEQUENO")) -1 else 0
        return base + obesoBonus + pequenoPenalty
    }

    fun resistenciaBase(): Int {
        val vigorRaw = personagem.atributos["VIGOR"] ?: 4
        val base = 2 + (vigorRaw / 2)

        val bonusPos =
            if (vantagensNomeKey.any { it == "RESISTENCIA" }) 1 else 0
        val bonusNeg =
            if (allComplicationsKeys.any { it.keyify() == "FRAGIL" }) -1 else 0

        val brigaoBonus = vantagensNomeKey.count { it in listOf("BRIGAO", "PUGILISTA") }

        return (base + bonusPos + bonusNeg + brigaoBonus + tamanhoTotal())
            .coerceAtLeast(0)
    }

    fun resistenciaFinal(): Int =
        resistenciaBase() + personagem.bonusResFromPower

    fun calcMovimento(): Int {
        val base = 6

        val racialPenalty =
            listaAncestralidadesJson
                .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
                ?.desvantagens
                ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
                .takeIf { it == true }
                ?.let { 1 }
                ?: 0

        val lentoPenalty = if (temComp("LENTO")) 1 else 0
        val idosoPenalty = if (temComp("IDOSO")) 1 else 0
        val obesoPenalty = if (temComp("OBESO")) 1 else 0
        val ligeiroBonus =
            if (vantagensNomeKey.any { it == "LIGEIRO" }) 2 else 0

        return (
                base
                        - racialPenalty
                        - lentoPenalty
                        - idosoPenalty
                        - obesoPenalty
                        + ligeiroBonus
                        + personagem.bonusMovimentacaoFromPower
                ).coerceAtLeast(0)
    }

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
            .mapNotNull { it.effect as? com.example.swadebuilder.model.PowerEffect.SuperPericia }
            .filter { it.periciaKey.equals("Lutar", ignoreCase = true) }
            .sumOf { it.steps }
        val lutarComSupers = applySuperStepsFrom(lutarRawBase, lutarStepsFromSupers)
        val jutsuComSupers = jutsuRawBase

        val base = 2 + (maxOf(lutarComSupers, jutsuComSupers) / 2)

        val bloquearBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusApararFromPower
    }

    fun calcArmaduraEfetiva(): Int {
        val melhorExterna = personagem.armorFromPower.coerceAtLeast(personagem.armorBase)
        return (melhorExterna + personagem.naturalArmorFromRace).coerceAtLeast(0)
    }

    fun calcChi(): Int {
        personagem.reservaChi?.let { return it }

        val espRaw = personagem.atributos["ESPIRITO"] ?: 0
        val racialPenalty = if (personagem.ancestralidade.keyify() == "TERRACOTA") 1 else 0
        val chiBonus = listaVantagens
            .filter { it.id in personagem.vantagens }
            .count { it.categoria == com.example.swadebuilder.model.Categoria.CHI }

        return (espRaw / 2 - racialPenalty + chiBonus).coerceAtLeast(0)
    }

    val aparar = calcAparar()
    val resFinal = resistenciaFinal()
    val tamanho = tamanhoTotal()
    val mov = calcMovimento()
    val armadura = calcArmaduraEfetiva()
    val chi = calcChi()
    val resistenciaTexto =
        if (armadura > 0) "${resFinal}(${armadura})" else resFinal.toString()

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
    if (personagem.sanidade != null) {
        lines += "Sanidade: ${personagem.sanidade}"
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
        lines += "$label d$valor"
    }
    lines += ""

    val periciasParaMostrar = listaPericias.filter { per ->
        per.basica || (personagem.pericias[per.nome] ?: 0) >
                periciaStartRaw(personagem.ancestralidade, per)
    }

    lines += "Perícias"
    if (periciasParaMostrar.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        periciasParaMostrar.forEach { per ->
            val raw = personagem.pericias[per.nome] ?: 0
            // PROMPT 5: Include skill notes in output
            val note = personagem.notasPericia[per.nome]
            val noteStr = if (!note.isNullOrBlank()) " ($note)" else ""
            lines += "${per.nome} d$raw$noteStr"
        }
    }
    lines += ""

    lines += "Recursos & Equipamentos"
    if (personagem.dadoRiqueza != null) {
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

    lines += "Vantagens"
    if (personagem.vantagens.isEmpty()) {
        lines += "– Nenhuma"
    } else {
        val nomesVantagens = listaVantagens
            .filter { it.id in personagem.vantagens }
            .map { vant ->
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
    if (personagem.vantagensRaciais.isNotEmpty()) {
        lines += "Vantagens Raciais: ${personagem.vantagensRaciais.joinToString(", ")}"
    }
    lines += ""

    lines += "Complicações"
    val allComplicationsList = complicacoesNomeadas + transtornosNomeados.map { "$it (Transtorno)" } + personagem.desvantagensRaciais
    val complicacoesText = allComplicationsList
            .joinToString(", ")
            .ifBlank { "– Nenhuma" }
    lines += complicacoesText
    if (personagem.desvantagensRaciais.isNotEmpty()) {
        lines += "Anotações Raciais: ${personagem.desvantagensRaciais.joinToString(", ")}"
    }
    lines += ""

    if (personagem.poderes.isNotEmpty()) {
        lines += "Poderes arcanos"
        personagem.poderes.forEach { (arcanoKey, lista) ->
            val label = arcanoKey
                .lowercase()
                .replace('_', ' ')
                .replaceFirstChar { it.titlecase() }

            lines += if (lista.isEmpty()) {
                "• $label: – nenhum poder escolhido"
            } else {
                val poderesComManifestacao = lista.map { poderId ->
                    val manifestacao = personagem.manifestacoesPoderes[poderId]
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                    if (manifestacao != null) "$poderId (${manifestacao})" else poderId
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

    return lines
}

// ✅ 4) Montagem do PDF (usa buildSummaryLines)
fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

    val marginLeft = 32f
    val marginRight = 32f
    val marginTop = 40f
    val marginBottom = 36f

    val bodyPaint = Paint().apply { textSize = 12f }
    val bodyFm = bodyPaint.fontMetrics
    val bodyLineHeight = bodyFm.descent - bodyFm.ascent + bodyFm.leading

    val sectionTitlePaint = Paint(bodyPaint).apply {
        textSize = 13.5f
        isFakeBoldText = true
    }
    val titlePaint = Paint(bodyPaint).apply {
        textSize = 18f
        isFakeBoldText = true
    }
    val subtitlePaint = Paint(bodyPaint).apply {
        textSize = 12.5f
        isFakeBoldText = true
        color = Color.DKGRAY
    }

    val boxStroke = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.rgb(92, 64, 51)
        strokeWidth = 2f
    }
    val boxFill = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(248, 244, 235)
    }
    val headerFill = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(229, 214, 200)
    }

    var page = doc.startPage(pageInfo)
    var canvas = page.canvas
    var y = marginTop

    fun newPage() {
        doc.finishPage(page)
        page = doc.startPage(pageInfo)
        canvas = page.canvas
        y = marginTop
    }

    fun wrapLine(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")

        val wrapped = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val count = paint.breakText(text, start, text.length, true, maxWidth, null)
            wrapped += text.substring(start, start + count)
            start += count
        }
        return wrapped
    }

    fun drawSection(title: String, contentLines: List<String>) {
        val maxWidth = pageInfo.pageWidth - marginLeft - marginRight
        val padding = 12f
        val headerHeight = sectionTitlePaint.fontMetrics.let { it.descent - it.ascent } + padding

        val normalizedContent = contentLines.map { line ->
            if (line.startsWith("•") || line.startsWith("–") || line.contains(":")) line else "• $line"
        }

        val wrappedLines = normalizedContent.flatMap { wrapLine(it, bodyPaint, maxWidth - (padding * 2)) }
        val contentHeight = (wrappedLines.size * bodyLineHeight) + padding
        val totalHeight = headerHeight + contentHeight + padding

        if (y + totalHeight > pageInfo.pageHeight - marginBottom) {
            newPage()
        }

        val rect = RectF(
            marginLeft,
            y,
            pageInfo.pageWidth - marginRight,
            y + totalHeight
        )

        val headerRect = RectF(rect.left, rect.top, rect.right, rect.top + headerHeight)

        canvas.drawRoundRect(rect, 18f, 18f, boxFill)
        canvas.drawRoundRect(headerRect, 18f, 18f, headerFill)
        canvas.drawRoundRect(rect, 18f, 18f, boxStroke)

        val headerBaseline = headerRect.top + padding - sectionTitlePaint.fontMetrics.ascent
        canvas.drawText(title, rect.left + padding, headerBaseline, sectionTitlePaint)

        var contentY = headerRect.bottom + padding - bodyFm.ascent
        wrappedLines.forEach { line ->
            canvas.drawText(line, rect.left + padding, contentY, bodyPaint)
            contentY += bodyLineHeight
        }

        y = rect.bottom + 10f
    }

    val title = "Ficha de ${personagem.nome.ifBlank { "(sem nome)" }}"
    val titleHeight = titlePaint.fontMetrics.let { it.descent - it.ascent }
    val subtitleHeight = subtitlePaint.fontMetrics.let { it.descent - it.ascent }

    val monstroTxt = if (personagem.modoMonstroAtivo) {
        val tipoNome = listaMonstroTemplates.find { it.id == personagem.tipoMonstroSelecionado }?.nome ?: "Desconhecido"
        " - Monstro: $tipoNome"
    } else ""

    canvas.drawText(title, marginLeft, y, titlePaint)
    y += titleHeight + 6f
    canvas.drawText("Ancestralidade: ${personagem.ancestralidade}$monstroTxt", marginLeft, y, subtitlePaint)
    y += subtitleHeight + 12f

    val lines = buildSummaryLines(personagem)
    val sectionTitles = setOf(
        "Identidade",
        "Atributos derivados",
        "Atributos",
        "Perícias",
        "Recursos & Equipamentos",
        "Vantagens",
        "Complicações",
        "Poderes arcanos",
        "Superpoderes",
        "Anotações"
    )

    val sections = mutableListOf<Pair<String, MutableList<String>>>()
    var currentTitle = "Resumo"
    var buffer = mutableListOf<String>()

    fun flushSection() {
        if (buffer.isNotEmpty()) {
            sections += currentTitle to buffer
            buffer = mutableListOf()
        }
    }

    lines.forEach { linha ->
        when {
            linha in sectionTitles -> {
                flushSection()
                currentTitle = linha
            }
            linha.isBlank() -> flushSection()
            else -> buffer += linha
        }
    }
    flushSection()

    sections.forEach { (titleSecao, conteudo) ->
        drawSection(titleSecao, conteudo)
    }

    doc.finishPage(page)
    FileOutputStream(destino).use { out -> doc.writeTo(out) }
    doc.close()
}
