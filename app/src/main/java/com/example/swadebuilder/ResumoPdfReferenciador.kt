package com.example.swadebuilder

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.titleCase
import java.io.File
import java.io.FileOutputStream

// =================================================================================================
// 1. DATA SNAPSHOT EXTENSION
// =================================================================================================

fun CriadorState.toMeuPersonagem(): MeuPersonagem {
    return MeuPersonagem(
        nome = this.nomePersonagem,
        atributos = this.valoresAtributos.mapValues { it.value.intValue },
        pericias = periciasComIdiomas().associate { per -> per.nome to this.rawTotal(per) },
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
        transtornos = this.transtornos.map { it.id },
        equipamentos = this.equipamentosComprados.toList(),
        poderes = this.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
        manifestacoesPoderes = this.manifestacoesPoderes.toMap(),
        dinheiro = this.dinheiro,
        dadoRiqueza = if (this.usaRiqueza) this.dadoRiqueza else null,
        pontosRestantes = this.pontosVantagem,
        naturalArmorFromRace = this.naturalArmorFromRace,
        armorBase = this.armadura,
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
        heroisSemArmadura = this.heroisSemArmadura,
        compendioDeadlandsAtivo = this.compendioDeadlandsAtivo,
        compendioHorrorAtivo = this.compendioHorrorAtivo,
        dominio = if (this.compendioDeadlandsAtivo) this.valorDominio() else null,
        coracaoCrystalSelecionado = this.coracaoCrystalSelecionado,
        tecnicasIniciaisTropo = this.tecnicasIniciaisFromTropo,
        reservaChi = if (this.compendioArteDaGuerraAtivo) this.reservaChi else null,
        notasPericia = this.notasPericia.toMap(),
        tamanho = this.tamanhoExibido(),
        resistencia = this.resistenciaBase(),
        appTheme = this.appTheme.name
    )
}

// =================================================================================================
// 2. ENTRY POINT FOR PDF GENERATION
// =================================================================================================

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

// =================================================================================================
// 3. THEME ENGINE
// =================================================================================================

data class PdfTheme(
    val backgroundColor: Int,
    val primaryColor: Int,
    val accentColor: Int,
    val textColor: Int,
    val gridLineColor: Int,
    val headerBackground: Int,
    val typefaceTitle: Typeface,
    val typefaceBody: Typeface,
    val shapeType: ShapeType = ShapeType.CIRCLE
)

enum class ShapeType { CIRCLE, HEXAGON }

fun getPdfTheme(themeName: String): PdfTheme {
    val theme = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.DEFAULT }

    return when (theme) {
        AppTheme.MEDIEVAL, AppTheme.DEFAULT -> PdfTheme(
            backgroundColor = Color.rgb(248, 244, 235), // Parchment
            primaryColor = Color.rgb(92, 64, 51),       // Dark Brown
            accentColor = Color.rgb(184, 134, 11),      // Dark Goldenrod
            textColor = Color.BLACK,
            gridLineColor = Color.rgb(160, 82, 45),     // Sienna
            headerBackground = Color.rgb(229, 214, 200),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.CYBERPUNK, AppTheme.SCIFI -> PdfTheme(
            backgroundColor = Color.BLACK,
            primaryColor = Color.rgb(0, 255, 65),       // Matrix Green
            accentColor = Color.rgb(0, 229, 255),       // Cyan
            textColor = Color.WHITE,
            gridLineColor = Color.rgb(0, 100, 0),       // Dark Green
            headerBackground = Color.rgb(20, 20, 20),
            typefaceTitle = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.HORROR, AppTheme.HALLOWEEN -> PdfTheme(
            backgroundColor = Color.rgb(20, 5, 5),      // Very Dark Red/Black
            primaryColor = Color.rgb(180, 20, 20),      // Blood Red
            accentColor = Color.rgb(100, 100, 100),     // Gray
            textColor = Color.rgb(240, 240, 230),       // Off-White
            gridLineColor = Color.rgb(80, 0, 0),        // Dark Red
            headerBackground = Color.rgb(40, 10, 10),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
        else -> PdfTheme( // Fallback (WW2, Pride, etc.) - clean style
            backgroundColor = Color.WHITE,
            primaryColor = Color.BLACK,
            accentColor = Color.DKGRAY,
            textColor = Color.BLACK,
            gridLineColor = Color.LTGRAY,
            headerBackground = Color.LTGRAY,
            typefaceTitle = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
    }
}

// =================================================================================================
// 4. MAIN RENDERING LOGIC
// =================================================================================================

fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem) {
    val doc = PdfDocument()
    // A4 size in points: 595 x 842
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas
    val theme = getPdfTheme(personagem.appTheme)

    // 1. Background
    canvas.drawColor(theme.backgroundColor)

    // Layout Constants
    val margin = 30f
    val pageWidth = 595f
    val pageHeight = 842f
    val contentWidth = pageWidth - (margin * 2)

    val headerHeight = 120f
    val derivedStatsHeight = 60f
    val columnGap = 20f
    val leftColWidth = (contentWidth - columnGap) * 0.35f
    val rightColWidth = (contentWidth - columnGap) * 0.65f

    val headerRect = RectF(margin, margin, pageWidth - margin, margin + headerHeight)
    val derivedRect = RectF(margin, headerRect.bottom + 10f, pageWidth - margin, headerRect.bottom + 10f + derivedStatsHeight)

    val leftColX = margin
    val rightColX = margin + leftColWidth + columnGap
    val colTopY = derivedRect.bottom + 20f

    // 2. Draw Header
    drawHeader(canvas, headerRect, personagem, theme)

    // 3. Draw Derived Stats Band
    drawDerivedStats(canvas, derivedRect, personagem, theme)

    // 4. Draw Columns
    var leftY = colTopY
    leftY = drawAttributesGraphic(canvas, leftColX, leftY, leftColWidth, personagem, theme)
    leftY += 20f
    leftY = drawSkillsList(canvas, leftColX, leftY, leftColWidth, personagem, theme)
    leftY += 20f
    leftY = drawHindrances(canvas, leftColX, leftY, leftColWidth, personagem, theme)

    var rightY = colTopY
    rightY = drawEdges(canvas, rightColX, rightY, rightColWidth, personagem, theme)
    rightY += 10f
    rightY = drawPowers(canvas, rightColX, rightY, rightColWidth, personagem, theme)
    rightY += 10f
    rightY = drawWeaponsTable(canvas, rightColX, rightY, rightColWidth, personagem, theme)
    rightY += 10f
    rightY = drawEquipmentList(canvas, rightColX, rightY, rightColWidth, personagem, theme)
    rightY += 10f
    drawNotes(canvas, rightColX, rightY, rightColWidth, personagem, theme)

    doc.finishPage(page)
    FileOutputStream(destino).use { out -> doc.writeTo(out) }
    doc.close()
}

// =================================================================================================
// 5. DRAWING HELPERS
// =================================================================================================

fun drawHeader(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme) {
    val paint = Paint().apply {
        color = theme.headerBackground
        style = Paint.Style.FILL
    }
    canvas.drawRect(rect, paint)

    // Border
    paint.style = Paint.Style.STROKE
    paint.color = theme.primaryColor
    paint.strokeWidth = 2f
    canvas.drawRect(rect, paint)

    // Text Setup
    val titlePaint = TextPaint().apply {
        color = theme.primaryColor
        typeface = theme.typefaceTitle
        textSize = 24f
        isAntiAlias = true
    }
    val subtitlePaint = TextPaint().apply {
        color = theme.textColor
        typeface = theme.typefaceBody
        textSize = 12f
        isAntiAlias = true
    }

    // Name
    canvas.drawText(p.nome.ifBlank { "Sem Nome" }, rect.left + 10f, rect.top + 30f, titlePaint)

    // Ancestry & Rank
    val rank = calculateRank(p)
    val ancestryText = "${p.ancestralidade.titleCase()} - $rank"
    canvas.drawText(ancestryText, rect.left + 10f, rect.top + 50f, subtitlePaint)

    // Tracks (Wounds / Fatigue)
    // Draw on the right side of the header
    val trackX = rect.right - 180f
    val trackY = rect.top + 20f
    drawTrack(canvas, trackX, trackY, "Ferimentos", 3, -1, theme) // 3 boxes (-1, -2, -3) + Incapacitated handled visually
    drawTrack(canvas, trackX, trackY + 40f, "Fadiga", 2, -1, theme)
}

fun calculateRank(p: MeuPersonagem): String {
    // Basic estimation based on XP/Advances logic if available, or just generic
    // Since we don't store XP directly here, we can infer or leave blank.
    // Using simple logic based on total advancements if possible, but for now placeholder:
    return "Novato" // Or better logic if available
}

fun drawTrack(canvas: Canvas, x: Float, y: Float, label: String, boxes: Int, current: Int, theme: PdfTheme) {
    val paint = Paint().apply {
        color = theme.textColor
        typeface = theme.typefaceBody
        textSize = 10f
        isAntiAlias = true
    }
    canvas.drawText(label, x, y, paint)

    val boxSize = 12f
    val gap = 5f
    var currX = x + 60f

    val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = theme.primaryColor
        strokeWidth = 1.5f
    }

    for (i in 1..boxes) {
        val r = RectF(currX, y - 10f, currX + boxSize, y - 10f + boxSize)
        canvas.drawRect(r, boxPaint)

        // Label inside/below? Just simple label -1, -2 etc
        val pen = "-$i"
        canvas.drawText(pen, currX + 2f, y + boxSize + 10f, paint)

        currX += boxSize + gap
    }

    // Incapacitated
    val incR = RectF(currX, y - 10f, currX + boxSize, y - 10f + boxSize)
    canvas.drawRect(incR, boxPaint)
    canvas.drawText("Inc", currX - 2f, y + boxSize + 10f, paint)
}

fun drawDerivedStats(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme) {
    // Calculators
    val aparar = calcAparar(p)
    val resistencia = calcResistencia(p)
    val mov = calcMovimento(p)

    // Draw 3 boxes
    val boxWidth = rect.width() / 3

    val labels = listOf("Aparar", "Resistência", "Movimentação")
    val values = listOf(aparar.toString(), resistencia, mov.toString())

    for (i in 0..2) {
        val bx = rect.left + (i * boxWidth)
        val r = RectF(bx, rect.top, bx + boxWidth, rect.bottom)

        // Box bg
        val bgPaint = Paint().apply { color = if (i % 2 == 0) theme.headerBackground else theme.backgroundColor; style = Paint.Style.FILL }
        canvas.drawRect(r, bgPaint)

        // Border
        val borderPaint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRect(r, borderPaint)

        // Text
        val labelPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; textAlign = Paint.Align.CENTER; typeface = theme.typefaceBody }
        val valPaint = TextPaint().apply { color = theme.primaryColor; textSize = 24f; textAlign = Paint.Align.CENTER; typeface = theme.typefaceTitle; isFakeBoldText = true }

        canvas.drawText(labels[i], r.centerX(), r.top + 15f, labelPaint)
        canvas.drawText(values[i], r.centerX(), r.bottom - 15f, valPaint)
    }
}

fun drawAttributesGraphic(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Atributos", x, y, titlePaint)

    var currY = y + 30f
    val attrSize = 40f // Radius or size
    val gap = 15f

    // Grid layout for attributes: 2 per row? Or vertical list?
    // Vertical list with shape on left is nice.

    listaAtributos.forEach { attr ->
        val value = p.atributos[attr] ?: 4
        val display = mapaAtributosDisplay[attr] ?: attr

        drawAttributeShape(canvas, x + 25f, currY + 20f, "d$value", theme, attrSize)

        val namePaint = TextPaint().apply { color = theme.textColor; textSize = 12f; typeface = theme.typefaceBody; isFakeBoldText = true }
        canvas.drawText(display, x + 60f, currY + 25f, namePaint)

        currY += 50f
    }

    return currY
}

fun drawAttributeShape(canvas: Canvas, cx: Float, cy: Float, text: String, theme: PdfTheme, size: Float) {
    val paint = Paint().apply {
        color = theme.primaryColor
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    if (theme.shapeType == ShapeType.HEXAGON) {
        val path = Path()
        val r = 20f
        // Simple hexagon
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i).toDouble())
            val px = cx + (r * Math.cos(angle)).toFloat()
            val py = cy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, paint)
    } else {
        canvas.drawCircle(cx, cy, 20f, paint)
    }

    val textPaint = TextPaint().apply {
        color = theme.textColor
        textSize = 14f
        textAlign = Paint.Align.CENTER
        typeface = theme.typefaceTitle
    }
    // Centering text vertically
    val metrics = textPaint.fontMetrics
    val dy = (metrics.descent + metrics.ascent) / 2
    canvas.drawText(text, cx, cy - dy, textPaint)
}

fun drawSkillsList(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Perícias", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    // Sort logic similar to existing
    val sortedSkills = p.pericias.entries.sortedByDescending { it.value } // Basic sort by value

    sortedSkills.forEach { (name, value) ->
        val note = p.notasPericia[name]
        val noteStr = if (!note.isNullOrBlank()) " ($note)" else ""
        val txt = "$name d$value$noteStr"
        canvas.drawText(txt, x, currY, bodyPaint)
        currY += 14f
    }

    if (sortedSkills.isEmpty()) {
        canvas.drawText("– Nenhuma", x, currY, bodyPaint)
        currY += 14f
    }

    return currY
}

fun drawHindrances(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Complicações", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    // Logic to list hindrances
    val all = mutableListOf<String>()
    all.addAll(complicationDisplayNames(p.complicacoes, p.modoOficialAtivo))
    // Add racial downsides if not duplicates logic (simplified here)

    if (all.isEmpty()) {
        canvas.drawText("– Nenhuma", x, currY, bodyPaint)
        currY += 14f
    } else {
        all.forEach { h ->
            val sl = StaticLayout.Builder.obtain(h, 0, h.length, bodyPaint, width.toInt())
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()

            canvas.save()
            canvas.translate(x, currY)
            sl.draw(canvas)
            canvas.restore()
            currY += sl.height + 5f
        }
    }
    return currY
}

fun drawEdges(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Vantagens", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    val edges = p.vantagens // IDs only, ideally fetch names.
    // Simplified: assuming we can't easily fetch full names without loading JSON here,
    // but the file has access to `listaVantagens`.

    if (edges.isEmpty()) {
        canvas.drawText("– Nenhuma", x, currY, bodyPaint)
        return currY + 14f
    }

    // Group logic if needed, or simple list
    // Use multi-line text for comma separated list or bullet points
    val names = edges.map { id ->
        listaVantagens.firstOrNull { it.id == id }?.nome ?: id
    }
    val text = names.joinToString(", ")

    val sl = StaticLayout.Builder.obtain(text, 0, text.length, bodyPaint, width.toInt())
        .build()

    canvas.save()
    canvas.translate(x, currY)
    sl.draw(canvas)
    canvas.restore()

    return currY + sl.height
}

fun drawPowers(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    if (p.poderes.isEmpty()) return y

    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Poderes", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    p.poderes.forEach { (arcano, list) ->
        val header = "Arcano: $arcano"
        canvas.drawText(header, x, currY, TextPaint(bodyPaint).apply { isFakeBoldText = true })
        currY += 14f

        val powersText = list.joinToString(", ")
        val sl = StaticLayout.Builder.obtain(powersText, 0, powersText.length, bodyPaint, width.toInt()).build()
        canvas.save()
        canvas.translate(x, currY)
        sl.draw(canvas)
        canvas.restore()
        currY += sl.height + 10f
    }

    return currY
}

fun drawWeaponsTable(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val weapons = p.equipamentos.filter { it.dano != null }
    if (weapons.isEmpty()) return y

    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Armas", x, y, titlePaint)
    var currY = y + 20f

    // Table Columns: Weapon | Dist | Dano | PA | CdT | Peso
    val cols = listOf("Arma", "Dist", "Dano", "PA", "CdT", "Peso")
    val colWeights = listOf(3f, 1f, 1.5f, 0.5f, 0.5f, 0.8f) // relative widths
    val totalWeight = colWeights.sum()
    val unitW = width / totalWeight
    val colWidths = colWeights.map { it * unitW }

    // Header Row
    val headerPaint = Paint().apply { color = theme.gridLineColor; style = Paint.Style.FILL }
    val headerTextPaint = TextPaint().apply { color = Color.WHITE; textSize = 10f; typeface = theme.typefaceBody; isFakeBoldText = true }

    val rowHeight = 20f
    val headerRect = RectF(x, currY, x + width, currY + rowHeight)
    canvas.drawRect(headerRect, headerPaint)

    var cx = x
    cols.forEachIndexed { i, title ->
        canvas.drawText(title, cx + 2f, currY + 14f, headerTextPaint)
        cx += colWidths[i]
    }
    currY += rowHeight

    // Rows
    val rowPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; typeface = theme.typefaceBody }
    val linePaint = Paint().apply { color = theme.gridLineColor; strokeWidth = 1f }

    weapons.forEach { w ->
        cx = x
        val data = listOf(
            w.nome,
            w.distancia?.toString()?.replace("\"", "") ?: "-",
            w.dano?.toString()?.replace("\"", "") ?: "-",
            w.pa?.toString()?.replace("\"", "") ?: "0",
            w.cdt?.toString()?.replace("\"", "") ?: "1",
            w.peso?.toString()?.replace("\"", "") ?: "-"
        )

        data.forEachIndexed { i, txt ->
            // Truncate if too long for simple table
            val safeTxt = if (txt.length > 15 && i == 0) txt.take(12) + "..." else txt
            canvas.drawText(safeTxt, cx + 2f, currY + 14f, rowPaint)
            cx += colWidths[i]

            // vertical lines
            canvas.drawLine(cx, currY, cx, currY + rowHeight, linePaint)
        }
        // horizontal line
        canvas.drawLine(x, currY + rowHeight, x + width, currY + rowHeight, linePaint)

        currY += rowHeight
    }
    // outer border
    canvas.drawLine(x, y + 20f, x, currY, linePaint)
    canvas.drawLine(x + width, y + 20f, x + width, currY, linePaint)

    return currY
}

fun drawEquipmentList(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    val others = p.equipamentos.filterNot { it.dano != null }
    if (others.isEmpty()) return y

    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Outros Equipamentos", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    val txt = others.joinToString(", ") { it.nome }
    val sl = StaticLayout.Builder.obtain(txt, 0, txt.length, bodyPaint, width.toInt()).build()

    canvas.save()
    canvas.translate(x, currY)
    sl.draw(canvas)
    canvas.restore()

    return currY + sl.height
}

fun drawNotes(canvas: Canvas, x: Float, y: Float, width: Float, p: MeuPersonagem, theme: PdfTheme): Float {
    if (p.anotacoes.isBlank()) return y

    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Anotações", x, y, titlePaint)

    var currY = y + 20f
    val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

    val sl = StaticLayout.Builder.obtain(p.anotacoes, 0, p.anotacoes.length, bodyPaint, width.toInt()).build()

    canvas.save()
    canvas.translate(x, currY)
    sl.draw(canvas)
    canvas.restore()

    return currY + sl.height
}


// =================================================================================================
// 6. CALCULATION HELPERS (Copied/Adapted from original logic for consistency)
// =================================================================================================

fun calcMovimento(personagem: MeuPersonagem): Int {
    val base = 6
    val racialPenalty = listaAncestralidadesJson
            .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
            ?.desvantagens
            ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
            .takeIf { it == true }
            ?.let { 1 }
            ?: 0

    val lentoPenalty = if (personagem.complicacoes.any { it.keyify().contains("LENTO") }) 1 else 0 // simplified
    val idosoPenalty = if (personagem.complicacoes.any { it.keyify().contains("IDOSO") }) 1 else 0
    val obesoPenalty = if (personagem.complicacoes.any { it.keyify().contains("OBESO") }) 1 else 0
    val ligeiroBonus = if (personagem.vantagens.any { it.keyify() == "LIGEIRO" }) 2 else 0

    return (base - racialPenalty - lentoPenalty - idosoPenalty - obesoPenalty + ligeiroBonus + personagem.bonusMovimentacaoFromPower).coerceAtLeast(0)
}

fun calcAparar(personagem: MeuPersonagem): Int {
    val lutarRawBase = personagem.pericias["Lutar"] ?: 0
    val jutsuRawBase = personagem.pericias["Jutsu"] ?: 0

    // Simplified logic for supers (skipping exact step recalc for now to keep it concise, relying on base pericia val)
    // If strict adherence needed, would need full logic.
    // Using simple max logic:
    val base = 2 + (maxOf(lutarRawBase, jutsuRawBase) / 2)
    val bloquearBonus = if (personagem.vantagens.any { it.keyify() == "BLOQUEAR" }) 1 else 0
    val bloquearAprimoradoBonus = if (personagem.vantagens.any { it.keyify() == "BLOQUEAR APRIMORADO" }) 1 else 0

    return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusApararFromPower
}

fun calcResistencia(personagem: MeuPersonagem): String {
    val armadura = (personagem.armorFromPower.coerceAtLeast(personagem.armorBase) + personagem.naturalArmorFromRace).coerceAtLeast(0)
    val temArmaduraDeEquip = personagem.equipamentos.any { it.armadura != null }
    val bonusSemArmadura = if (personagem.heroisSemArmadura && !temArmaduraDeEquip) 2 else 0

    val resFinal = personagem.resistencia
    val resistenciaTotal = resFinal + armadura + bonusSemArmadura

    return if ((armadura + bonusSemArmadura) > 0) "${resFinal}(${resistenciaTotal})" else resFinal.toString()
}

private fun complicationDisplayNames(rawIds: List<String>, modoOficialAtivo: Boolean): List<String> {
    val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }
    return rawIds.map { compId ->
        val comp = mapPorId[compId.keyify()]
        if (comp != null) comp.name else compId.replace('_', ' ').titleCase()
    }
}
