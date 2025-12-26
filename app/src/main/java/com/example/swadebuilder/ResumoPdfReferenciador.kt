package com.example.swadebuilder

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
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
        appTheme = this.appTheme.name,
        portraitFileName = this.portraitFileName
    )
}

// =================================================================================================
// 2. ENTRY POINT FOR PDF GENERATION
// =================================================================================================

fun produzirEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
    val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")

    // Load Portrait if exists
    var portrait: Bitmap? = null
    dadosDoPersonagem.portraitFileName?.let { fileName ->
        val file = File(context.filesDir, "portraits/$fileName")
        if (file.exists()) {
            portrait = BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    gerarFichaEmPdf(pdfFile, dadosDoPersonagem, portrait)

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

fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem, portrait: Bitmap? = null) {
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

    // Page Border
    val borderPaint = Paint().apply {
        color = theme.primaryColor
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    val borderRect = RectF(margin/2, margin/2, pageWidth - margin/2, pageHeight - margin/2)
    canvas.drawRect(borderRect, borderPaint)

    val headerHeight = 140f
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
    drawHeader(canvas, headerRect, personagem, theme, portrait)

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

fun drawHeader(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme, portrait: Bitmap?) {
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

    // Portrait Setup
    val portraitW = 100f
    val portraitH = 120f
    val portraitX = rect.right - portraitW - 10f
    val portraitY = rect.top + 10f
    val portraitRect = RectF(portraitX, portraitY, portraitX + portraitW, portraitY + portraitH)

    if (portrait != null) {
        val path = Path().apply {
            if (theme.shapeType == ShapeType.CIRCLE) {
                // If it's a circle theme, maybe a rounded rect for portrait? Or circle?
                // Rect is better for portraits usually. Let's use slight rounded corners.
                addRoundRect(portraitRect, 10f, 10f, Path.Direction.CW)
            } else {
                addRect(portraitRect, Path.Direction.CW)
            }
        }
        canvas.save()
        canvas.clipPath(path)

        // Scale and Center Crop
        val scale = Math.max(portraitW / portrait.width, portraitH / portrait.height)
        val scaledW = portrait.width * scale
        val scaledH = portrait.height * scale
        val dx = portraitX + (portraitW - scaledW) / 2
        val dy = portraitY + (portraitH - scaledH) / 2
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(dx, dy)
        }
        canvas.drawBitmap(portrait, matrix, null)
        canvas.restore()

        // Border over portrait
        canvas.drawPath(path, paint) // Use same border paint
    } else {
        // Placeholder
        val placePaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        canvas.drawRect(portraitRect, placePaint)
        val textPaint = TextPaint().apply {
            color = Color.GRAY
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Retrato", portraitRect.centerX(), portraitRect.centerY(), textPaint)
    }

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

    // Adjust text area width to avoid overlapping portrait
    // Left area width = total width - portrait width - padding
    val textAreaWidth = rect.width() - portraitW - 30f // 30f for margins/gaps

    // Name - Check width
    val name = p.nome.ifBlank { "Sem Nome" }
    var displayedName = name
    // Simple truncation for now if too long
    if (titlePaint.measureText(displayedName) > textAreaWidth) {
        val avail = textAreaWidth
        val count = titlePaint.breakText(displayedName, true, avail, null)
        displayedName = displayedName.substring(0, count) + "..."
    }

    canvas.drawText(displayedName, rect.left + 10f, rect.top + 30f, titlePaint)

    // Ancestry & Rank
    val rank = calculateRank(p)
    val ancestryText = "${p.ancestralidade.titleCase()} - $rank"
    canvas.drawText(ancestryText, rect.left + 10f, rect.top + 50f, subtitlePaint)

    // Tracks (Wounds / Fatigue)
    // Draw below name/ancestry but to the left of portrait
    val trackX = rect.left + 10f
    val trackY = rect.top + 80f
    drawTrack(canvas, trackX, trackY, "Ferimentos", 3, -1, theme)
    drawTrack(canvas, trackX + 100f, trackY, "Fadiga", 2, -1, theme)
}

fun calculateRank(p: MeuPersonagem): String {
    return "Novato" // Placeholder
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
    var currX = x // Below label
    val trackY = y + 5f

    val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = theme.primaryColor
        strokeWidth = 1.5f
    }

    for (i in 1..boxes) {
        val r = RectF(currX, trackY, currX + boxSize, trackY + boxSize)
        canvas.drawRect(r, boxPaint)

        val pen = "-$i"
        // Draw penalty small inside or below? Below for clarity
        val penPaint = TextPaint(paint).apply { textSize = 8f; textAlign = Paint.Align.CENTER }
        canvas.drawText(pen, r.centerX(), r.bottom + 8f, penPaint)

        currX += boxSize + gap
    }

    // Incapacitated box logic if needed, simplifed here
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
    val attrSize = 40f

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

    val sortedSkills = p.pericias.entries.sortedByDescending { it.value }.map { entry ->
        val name = entry.key
        val value = entry.value
        val note = p.notasPericia[name]
        val noteStr = if (!note.isNullOrBlank()) " ($note)" else ""
        "$name d$value$noteStr"
    }

    if (sortedSkills.isEmpty()) {
        canvas.drawText("– Nenhuma", x, currY, bodyPaint)
        return currY + 14f
    }

    val count = sortedSkills.size
    val rowHeight = 14f

    if (count <= 12) {
        // Single column
        sortedSkills.forEach { txt ->
            canvas.drawText(txt, x, currY, bodyPaint)
            currY += rowHeight
        }
    } else {
        // Two columns
        val mid = (count + 1) / 2
        val col1 = sortedSkills.take(mid)
        val col2 = sortedSkills.drop(mid)

        val colWidth = width / 2
        val x2 = x + colWidth

        val startY = currY
        var col1Y = startY
        col1.forEach { txt ->
            // Check overflow? Assume fits for now or truncate
            // Truncate text to fit column
            val safeTxt = if (bodyPaint.measureText(txt) > colWidth - 5f) {
                val c = bodyPaint.breakText(txt, true, colWidth - 5f, null)
                txt.substring(0, c)
            } else txt
            canvas.drawText(safeTxt, x, col1Y, bodyPaint)
            col1Y += rowHeight
        }

        var col2Y = startY
        col2.forEach { txt ->
            val safeTxt = if (bodyPaint.measureText(txt) > colWidth - 5f) {
                val c = bodyPaint.breakText(txt, true, colWidth - 5f, null)
                txt.substring(0, c)
            } else txt
            canvas.drawText(safeTxt, x2, col2Y, bodyPaint)
            col2Y += rowHeight
        }

        currY = maxOf(col1Y, col2Y)
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

    val edges = p.vantagens // IDs only

    if (edges.isEmpty()) {
        canvas.drawText("– Nenhuma", x, currY, bodyPaint)
        return currY + 14f
    }

    // Simplification: just listing IDs since we don't have global access easily (we assume duplicate fix removed it)
    // But wait, the previous code DID use listaVantagens. And I removed the duplicate declaration.
    // That means `listaVantagens` MUST be available globally in the package.
    // If it is, I can use it.

    val names = edges.map { id ->
        // Try to find in global list if available, else ID
        try {
            com.example.swadebuilder.listaVantagens.firstOrNull { it.id == id }?.nome ?: id
        } catch (e: Exception) {
            id // Fallback
        }
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
// 6. CALCULATION HELPERS
// =================================================================================================

fun calcMovimento(personagem: MeuPersonagem): Int {
    val base = 6
    val racialPenalty = com.example.swadebuilder.listaAncestralidadesJson
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
    val mapPorId = com.example.swadebuilder.listaComplicacoes.associateBy { it.id.keyify() }
    return rawIds.map { compId ->
        val comp = mapPorId[compId.keyify()]
        if (comp != null) comp.name else compId.replace('_', ' ').titleCase()
    }
}
