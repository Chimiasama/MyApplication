package com.example.swadebuilder.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.swadebuilder.buildSummaryLines
import com.example.swadebuilder.listaAtributos
import com.example.swadebuilder.mapaAtributosDisplay
import com.example.swadebuilder.model.MeuPersonagem
import kotlin.math.ceil

class PdfLayoutManager(private val doc: PdfDocument, private val pageInfo: PdfDocument.PageInfo) {
    private val paints = PdfPaints()
    private val params = PdfLayoutParams(
        pageInfo = pageInfo,
        marginLeft = 40f,
        marginRight = 40f,
        marginTop = 40f,
        marginBottom = 40f
    )
    private var y: Float = params.marginTop
    private var currentPage: PdfDocument.Page
    private var canvas: Canvas
    private var pageNumber = 1

    init {
        currentPage = doc.startPage(pageInfo)
        canvas = currentPage.canvas
    }

    fun draw(personagem: MeuPersonagem) {
        drawHeader(personagem)
        drawTwoColumnSection(personagem)
        drawFullWidthSections(personagem)
        drawFooter()
        doc.finishPage(currentPage)
    }

    private fun startNewPage() {
        drawFooter()
        doc.finishPage(currentPage)
        pageNumber++
        currentPage = doc.startPage(pageInfo)
        canvas = currentPage.canvas
        y = params.marginTop
    }

    private fun drawHeader(personagem: MeuPersonagem) {
        val headerHeight = 80f
        val rect = RectF(params.marginLeft, y, params.marginLeft + params.usableWidth, y + headerHeight)
        canvas.drawRoundRect(rect, 8f, 8f, paints.headerFill)
        canvas.drawRoundRect(rect, 8f, 8f, paints.boxStroke)

        val title = personagem.nome.ifBlank { "(Sem nome)" }
        val titleWidth = paints.title.measureText(title)
        val titleX = rect.centerX() - titleWidth / 2
        val titleY = rect.centerY() - paints.title.fontMetrics.ascent / 2 - 10
        canvas.drawText(title, titleX, titleY, paints.title)

        val subtitle = "Ancestralidade: ${personagem.ancestralidade}"
        val subtitleWidth = paints.subtitle.measureText(subtitle)
        val subtitleX = rect.centerX() - subtitleWidth / 2
        val subtitleY = titleY + paints.subtitle.fontMetrics.let { it.descent - it.ascent } + 5
        canvas.drawText(subtitle, subtitleX, subtitleY, paints.subtitle)

        y += headerHeight + 20f
    }

    private fun drawTwoColumnSection(personagem: MeuPersonagem) {
        val leftColumnWidth = params.usableWidth * 0.4f
        val rightColumnWidth = params.usableWidth * 0.6f - 20f
        val startY = y

        drawAttributes(personagem, leftColumnWidth)
        drawDerivedAttributes(personagem, leftColumnWidth)
        val leftHeight = y

        y = startY
        val summaryLines = buildSummaryLines(personagem)
        val skills = summaryLines
            .dropWhile { it != "Perícias" }
            .drop(1)
            .takeWhile { it.isNotEmpty() }
        val midPoint = ceil(skills.size / 2.0).toInt()
        val skillsCol1 = skills.subList(0, midPoint)
        val skillsCol2 = skills.subList(midPoint, skills.size)

        drawSectionBox("Perícias", skillsCol1, rightColumnWidth / 2, params.marginLeft + leftColumnWidth + 20f, isAttributeOrSkill = true)
        val col1Y = y
        y = startY
        drawSectionBox("", skillsCol2, rightColumnWidth / 2, params.marginLeft + leftColumnWidth + 20f + rightColumnWidth / 2, isAttributeOrSkill = true)
        val col2Y = y

        y = maxOf(leftHeight, col1Y, col2Y) + 10f
    }

    private fun drawAttributes(personagem: MeuPersonagem, width: Float) {
        val content = listaAtributos.map { attrKey ->
            val label = mapaAtributosDisplay[attrKey] ?: attrKey
            val value = personagem.atributos[attrKey] ?: 4
            "$label: d$value"
        }
        drawSectionBox("Atributos", content, width, params.marginLeft, isAttributeOrSkill = true)
    }

    private fun drawDerivedAttributes(personagem: MeuPersonagem, width: Float) {
        val summaryLines = buildSummaryLines(personagem)
        val derivedAttributes = summaryLines
            .dropWhile { it != "Atributos derivados" }
            .drop(1)
            .takeWhile { it.isNotEmpty() }
        drawSectionBox("Derivados", derivedAttributes, width, params.marginLeft)
    }

    private fun drawFullWidthSections(personagem: MeuPersonagem) {
        val summaryLines = buildSummaryLines(personagem)
        val sections = listOf(
            "Vantagens", "Complicações", "Recursos & Equipamentos",
            "Poderes arcanos", "Superpoderes", "Anotações"
        )

        sections.forEach { sectionTitle ->
            val content = summaryLines
                .dropWhile { it != sectionTitle }
                .drop(1)
                .takeWhile { it.isNotEmpty() }

            if (content.isNotEmpty()) {
                drawSectionBox(sectionTitle, content, params.usableWidth, params.marginLeft)
            }
        }
    }

    private fun drawFooter() {
        val footerText = "Gerado por SWADE Builder"
        val pageNumberText = "Página $pageNumber"
        val footerY = pageInfo.pageHeight - params.marginBottom

        val footerTextWidth = paints.body.measureText(footerText)
        canvas.drawText(footerText, pageInfo.pageWidth - params.marginRight - footerTextWidth, footerY, paints.body)
        canvas.drawText(pageNumberText, params.marginLeft, footerY, paints.body)
    }

    private fun drawDiceIcon(x: Float, y: Float, value: Int) {
        val diceSteps = listOf(4, 6, 8, 10, 12)
        val boxSize = 8f
        val spacing = 3f
        var currentX = x

        val totalWidth = (boxSize * diceSteps.size) + (spacing * (diceSteps.size - 1))
        currentX -= totalWidth

        for (step in diceSteps) {
            val rect = RectF(currentX, y - boxSize - 3f, currentX + boxSize, y - 3f)
            if (value >= step) {
                canvas.drawRect(rect, paints.diceFill)
            }
            canvas.drawRect(rect, paints.diceStroke)
            currentX += boxSize + spacing
        }
    }

    private fun drawAttributeLine(line: String, x: Float, y: Float, lineWidth: Float) {
        val parts = line.split(": d")
        if (parts.size != 2) {
            canvas.drawText(line, x, y, paints.body)
            return
        }
        val label = parts[0]
        val diceValue = parts[1].toIntOrNull() ?: 0

        canvas.drawText(label, x, y, paints.body)
        drawDiceIcon(x + lineWidth, y, diceValue)
    }

    private fun drawSectionBox(
        title: String, lines: List<String>, width: Float, xOffset: Float,
        isAttributeOrSkill: Boolean = false
    ) {
        val padding = 10f
        val headerHeight = if (title.isNotEmpty()) paints.sectionTitle.fontMetrics.let { it.descent - it.ascent } + padding else 0f

        val wrappedLines = lines.flatMap { wrapLine(it, paints.body, width - (padding * 2)) }
        val contentHeight = (wrappedLines.size * paints.bodyLineHeight) + padding
        val totalHeight = headerHeight + contentHeight + padding

        if (y + totalHeight > pageInfo.pageHeight - params.marginBottom) {
            startNewPage()
        }

        val rect = RectF(xOffset, y, xOffset + width, y + totalHeight)
        if (title.isNotEmpty()) {
            val headerRect = RectF(rect.left, rect.top, rect.right, rect.top + headerHeight)
            canvas.drawRoundRect(rect, 8f, 8f, paints.boxFill)
            canvas.drawRoundRect(headerRect, 8f, 8f, paints.headerFill)
            canvas.drawRoundRect(rect, 8f, 8f, paints.boxStroke)
            val headerBaseline = headerRect.top + padding - paints.sectionTitle.fontMetrics.ascent
            canvas.drawText(title, rect.left + padding, headerBaseline, paints.sectionTitle)
        } else {
            canvas.drawRoundRect(rect, 8f, 8f, paints.boxFill)
            canvas.drawRoundRect(rect, 8f, 8f, paints.boxStroke)
        }

        var contentY = rect.top + headerHeight + padding - paints.bodyFm.ascent
        wrappedLines.forEach { line ->
            if (isAttributeOrSkill && line.contains(": d")) {
                drawAttributeLine(line, rect.left + padding, contentY, width - (padding * 2))
            } else {
                canvas.drawText(line, rect.left + padding, contentY, paints.body)
            }
            contentY += paints.bodyLineHeight
        }

        y = rect.bottom + 10f
    }

    private fun wrapLine(text: String, paint: Paint, maxWidth: Float): List<String> {
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
}

class PdfLayoutParams(
    val pageInfo: PdfDocument.PageInfo,
    val marginLeft: Float,
    val marginRight: Float,
    val marginTop: Float,
    val marginBottom: Float
) {
    val usableWidth = pageInfo.pageWidth - marginLeft - marginRight
}

class PdfPaints {
    val body = Paint().apply { textSize = 9f; color = Color.BLACK }
    val bodyFm = body.fontMetrics
    val bodyLineHeight = bodyFm.descent - bodyFm.ascent + bodyFm.leading

    val sectionTitle = Paint(body).apply {
        textSize = 11f
        isFakeBoldText = true
        color = Color.rgb(60, 60, 60)
    }
    val title = Paint(body).apply {
        textSize = 22f
        isFakeBoldText = true
        color = Color.rgb(150, 0, 0)
    }
    val subtitle = Paint(body).apply {
        textSize = 10f
        isFakeBoldText = false
        color = Color.GRAY
    }
    val boxStroke = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.rgb(220, 220, 220)
        strokeWidth = 1.5f
    }
    val boxFill = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    val headerFill = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(245, 245, 245)
    }
    val diceFill = Paint().apply {
        style = Paint.Style.FILL
        color = Color.rgb(80, 80, 80)
    }
    val diceStroke = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.rgb(180, 180, 180)
        strokeWidth = 1f
    }
}
