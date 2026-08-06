package com.example.swadebuilder

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.SecurityUtils
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.titleCase
import com.example.swadebuilder.util.toFancyTitleCase
import com.example.swadebuilder.toDiceString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

// =================================================================================================
// 1. DATA SNAPSHOT EXTENSION
// =================================================================================================

fun CriadorState.toMeuPersonagem(): MeuPersonagem {
    return MeuPersonagem(
        nome = this.nomePersonagem,
        atributos = this.valoresAtributos.mapValues { it.value.intValue },
        pericias = periciasComIdiomas().associate { per -> per.nome to this.rawTotal(per) },
        ancestralidade = this.ancestralidade,
        signoAdgSelecionado = this.signoAdgSelecionado,
        descendenteElementalSelecionado = this.descendenteElementalSelecionado,
        pacoteCulturalFantasiaSelecionado = this.pacoteCulturalFantasiaSelecionado,
        povoDoMarOpcao = this.povoDoMarOpcao,
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
        complicacoesTipos = this.complicacoesSelecionadas
            .filterValues { it != null }
            .mapKeys { it.key.id }
            .mapValues { it.value!! },
        transtornos = this.transtornos.map { it.id },
        equipamentos = this.equipamentosComprados.toList() + this.extrairArmasNaturais(),
        poderes = this.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
        manifestacoesPoderes = this.manifestacoesPoderes.toMap(),
        bonusPoderExtra = this.bonusPoderExtra,
        dinheiro = this.dinheiro,
        requisicao = this.requisicao,
        dadoRiqueza = if (this.usaRiqueza) this.dadoRiqueza else null,
        pontosRestantes = this.pontosVantagem,
        naturalArmorFromRace = this.naturalArmorFromRace,
        armorBase = this.armadura,
        modoSupers = this.modoSupers,
        modoMonstroAtivo = this.modoMonstroAtivo,
        tipoMonstroSelecionado = this.tipoMonstroSelecionado,
        superPontosTotais = this.superPontosTotais,
        superPontosDisponiveis = this.superPontosDisponiveis,
        superNivelCampanha = this.superNivelCampanha,
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
        compendioPathfinderAtivo = this.compendioPathfinderAtivo,
        compendioDeadlandsAtivo = this.compendioDeadlandsAtivo,
        compendioHorrorAtivo = this.compendioHorrorAtivo,
        compendioFantasiaAtivo = this.compendioFantasiaAtivo,
        compendioSciFiAtivo = this.compendioSciFiAtivo,
        compendioWiseguysAtivo = this.compendioWiseguysAtivo,
        compendioCidadeSolVaporAtivo = this.compendioCidadeSolVaporAtivo,
        dominio = if (this.compendioDeadlandsAtivo && this.vantagensSelecionadas.any { it.id == "atormentado" }) this.valorDominio() else null,
        coracaoCrystalSelecionado = this.coracaoCrystalSelecionado,
        tecnicasIniciaisTropo = this.tecnicasIniciaisFromTropo,
        reservaChi = if (this.compendioArteDaGuerraAtivo) this.reservaChi else null,
        notasPericia = this.notasPericia.toMap(),
        tamanho = this.tamanhoExibido(),
        movimentacao = this.valorMovimentacao(),
        resistencia = this.resistenciaBase(),
        appTheme = this.appTheme.name,
        portraitFileName = this.portraitFileName,
        regraFamaAtiva = this.optRegraFama,
        fama = this.valorFama(),
        usaRiqueza = this.usaRiqueza,
        usaRequisicao = this.usaRequisicao,
        modoProgressaoAtivo = this.modoProgressaoAtivo
    )
}

// =================================================================================================
// 2. ENTRY POINT FOR PDF GENERATION
// =================================================================================================

suspend fun produzirEExibirFichaPdf(
    context: Context,
    dadosDoPersonagem: MeuPersonagem,
    listaAtributos: List<String>,
    mapaAtributosDisplay: Map<String, String>,
    listaComplicacoes: List<Complicacao>,
    listaVantagens: List<Vantagem>,
    listaPoderes: List<Poder>,
    onShowMessage: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            // Save to internal cache/pdfs/ to avoid exposing root external files and support FileProvider
            val pdfsDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
            val safeName = SecurityUtils.sanitizeFilename(dadosDoPersonagem.nome.ifBlank { "sem_nome" })
            // Use character name to avoid race conditions when generating multiple PDFs
            val pdfFile = File(pdfsDir, "ficha_$safeName.pdf")

            var portrait: Bitmap? = null
            dadosDoPersonagem.portraitFileName?.let { fileName ->
                try {
                    val portraitsDir = File(context.filesDir, "portraits")
                    val file = SecurityUtils.getSafeChildFile(portraitsDir, fileName)
                    if (file.exists()) {
                        portrait = BitmapFactory.decodeFile(file.absolutePath)
                    }
                } catch (e: Exception) {
                    // Path traversal attempt or invalid filename; ignore portrait
                }
            }

            gerarFichaEmPdf(
                pdfFile,
                dadosDoPersonagem,
                portrait,
                listaAtributos,
                mapaAtributosDisplay,
                listaComplicacoes,
                listaVantagens,
                listaPoderes
            )

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    clipData = ClipData.newRawUri(null, uri)
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    onShowMessage("Nenhum app de PDF encontrado.")
                }
            }
        } catch (e: Exception) {
            Log.e("PDFGeneration", "Erro ao gerar PDF", e)
            withContext(Dispatchers.Main) {
                onShowMessage("Erro ao gerar PDF: ${e.message}")
            }
        }
    }
}

// =================================================================================================
// 3. PDF BLOCK INFRASTRUCTURE
// =================================================================================================

interface PdfBlock {
    fun measure(width: Float, theme: PdfTheme): Float
    fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme)
    /**
     * Splits this block into two.
     * @param availableHeight The height available on the current page.
     * @return Pair(Head, Tail). Head is what fits, Tail is what remains.
     * If Head is null, nothing fits. If Tail is null, everything fits.
     */
    fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?>
}

abstract class TextListBlock(private val title: String, private val items: List<String>) : PdfBlock {
    override fun measure(width: Float, theme: PdfTheme): Float {
        if (items.isEmpty()) return 0f
        val paint = TextPaint().apply {
            textSize = 11f; typeface = theme.typefaceBody
        }
        val titleH = 20f
        var totalH = titleH
        items.forEach { item ->
            val sl = StaticLayout.Builder.obtain(item, 0, item.length, paint, width.toInt())
                .setAlignment(android.text.Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .setIncludePad(true)
                .build()
            totalH += sl.height + 5f // 5f padding
        }
        return totalH
    }

    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        if (items.isEmpty()) return
        val titlePaint = TextPaint().apply {
            color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true
        }
        canvas.drawText(title, x, y + 14f, titlePaint) // Title baseline approx

        val bodyPaint = TextPaint().apply {
            color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody
        }

        var currY = y + 20f
        items.forEach { item ->
            val sl = StaticLayout.Builder.obtain(item, 0, item.length, bodyPaint, width.toInt())
                .build()
            canvas.save()
            canvas.translate(x, currY)
            sl.draw(canvas)
            canvas.restore()
            currY += sl.height + 5f
        }
    }

    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        val fullHeight = measure(width, theme)
        if (fullHeight <= availableHeight) return this to null

        // Calculate how many items fit
        val paint = TextPaint().apply { textSize = 11f; typeface = theme.typefaceBody }
        val titleH = 20f
        var currentH = titleH

        if (availableHeight < titleH) return null to this // Can't even fit title

        val headItems = mutableListOf<String>()
        val tailItems = mutableListOf<String>()

        var inTail = false
        items.forEach { item ->
            if (!inTail) {
                val sl = StaticLayout.Builder.obtain(item, 0, item.length, paint, width.toInt()).build()
                val itemH = sl.height + 5f
                if (currentH + itemH <= availableHeight) {
                    headItems.add(item)
                    currentH += itemH
                } else {
                    inTail = true
                    tailItems.add(item)
                }
            } else {
                tailItems.add(item)
            }
        }

        val head = if (headItems.isEmpty() && availableHeight < titleH + 15f) null
                   else object : TextListBlock(title, headItems) {}

        // Tail doesn't need title repeated usually, but for context maybe?
        // Let's assume continuation doesn't repeat title to save space, or user can infer.
        // Actually, clearer if we don't repeat title.
        val tail = if (tailItems.isEmpty()) null
                   else object : TextListBlock("", tailItems) {
                       override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
                           // Override to skip title logic/space if empty title
                           if (items.isEmpty()) return
                           val bPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }
                           var cy = y
                           items.forEach { it ->
                                val sl = StaticLayout.Builder.obtain(it, 0, it.length, bPaint, width.toInt()).build()
                                canvas.save()
                                canvas.translate(x, cy)
                                sl.draw(canvas)
                                canvas.restore()
                                cy += sl.height + 5f
                           }
                       }
                       override fun measure(width: Float, theme: PdfTheme): Float {
                           if (items.isEmpty()) return 0f
                           val bPaint = TextPaint().apply { textSize = 11f; typeface = theme.typefaceBody }
                           var h = 0f
                           items.forEach {
                               val sl = StaticLayout.Builder.obtain(it, 0, it.length, bPaint, width.toInt()).build()
                               h += sl.height + 5f
                           }
                           return h
                       }
                   }

        return head to tail
    }
}

class AttributeBlock(
    private val p: MeuPersonagem,
    private val listaAtributos: List<String>,
    private val mapaAtributosDisplay: Map<String, String>
) : PdfBlock {
    override fun measure(width: Float, theme: PdfTheme): Float {
        // Fixed height: Title + count * 50f
        return 30f + (listaAtributos.size * 50f)
    }

    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
        canvas.drawText("Atributos", x, y + 14f, titlePaint)

        var currY = y + 30f
        listaAtributos.forEach { attr ->
            val value = p.atributos[attr] ?: 4
            val display = mapaAtributosDisplay[attr] ?: attr
            drawAttributeShape(canvas, x + 25f, currY + 20f, value.toDiceString(), theme)
            val namePaint = TextPaint().apply { color = theme.textColor; textSize = 12f; typeface = theme.typefaceBody; isFakeBoldText = true }
            canvas.drawText(display, x + 60f, currY + 25f, namePaint)
            currY += 50f
        }
    }

    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        // Attributes are atomic for simplicity. If they don't fit, push to next page.
        if (measure(width, theme) <= availableHeight) return this to null
        return null to this
    }
}

class SkillListBlock(private val p: MeuPersonagem) : PdfBlock {
    private val skills = p.pericias.entries
        .filter { it.value > 0 } // Fix: Filter out d0 (value <= 0)
        .sortedByDescending { it.value }
        .map { entry ->
            val name = entry.key
            val value = entry.value
            val note = p.notasPericia[name]
            val noteStr = if (!note.isNullOrBlank()) " ($note)" else ""
            "$name ${value.toDiceString()}$noteStr"
        }

    override fun measure(width: Float, theme: PdfTheme): Float {
        if (skills.isEmpty()) return 34f // Title + "None"
        val rowHeight = 14f
        val count = skills.size
        val rows = if (count <= 12) count else (count + 1) / 2
        return 20f + (rows * rowHeight)
    }

    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
        canvas.drawText("Perícias", x, y + 14f, titlePaint)

        var currY = y + 20f
        val bodyPaint = TextPaint().apply { color = theme.textColor; textSize = 11f; typeface = theme.typefaceBody }

        if (skills.isEmpty()) {
            canvas.drawText("– Nenhuma", x, currY + 11f, bodyPaint)
            return
        }

        val rowHeight = 14f
        val count = skills.size

        if (count <= 12) {
            skills.forEach { txt ->
                canvas.drawText(txt, x, currY + 11f, bodyPaint)
                currY += rowHeight
            }
        } else {
            val mid = (count + 1) / 2
            val col1 = skills.take(mid)
            val col2 = skills.drop(mid)
            val colWidth = width / 2
            val x2 = x + colWidth

            val startY = currY
            col1.forEachIndexed { i, txt ->
                // Truncate
                val safeTxt = truncate(txt, bodyPaint, colWidth - 5f)
                canvas.drawText(safeTxt, x, startY + (i * rowHeight) + 11f, bodyPaint)
            }
            col2.forEachIndexed { i, txt ->
                val safeTxt = truncate(txt, bodyPaint, colWidth - 5f)
                canvas.drawText(safeTxt, x2, startY + (i * rowHeight) + 11f, bodyPaint)
            }
        }
    }

    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (measure(width, theme) <= availableHeight) return this to null
        // If split needed, revert to simple 1-col list logic?
        // Or just move whole block. Moving whole block is safer/easier for layout.
        return null to this
    }
}

class WeaponTableBlock(private val p: MeuPersonagem) : PdfBlock {
    private val weapons = p.equipamentos.filter { it.dano != null }

    override fun measure(width: Float, theme: PdfTheme): Float {
        if (weapons.isEmpty()) return 0f
        val rowHeight = 20f
        // Header + rows + borders
        return 20f + 20f + (weapons.size * rowHeight) + 5f
    }

    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        if (weapons.isEmpty()) return
        val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
        canvas.drawText("Armas", x, y + 14f, titlePaint)

        var currY = y + 20f
        val rowHeight = 20f

        val cols = listOf("Arma", "Dist", "Dano", "PA", "CdT", "Peso")
        val colWeights = listOf(3f, 1f, 1.5f, 0.5f, 0.5f, 0.8f)
        val totalWeight = colWeights.sum()
        val unitW = width / totalWeight
        val colWidths = colWeights.map { it * unitW }

        // Header
        val headerPaint = Paint().apply { color = theme.gridLineColor; style = Paint.Style.FILL }
        canvas.drawRect(x, currY, x + width, currY + rowHeight, headerPaint)

        val headerTextPaint = TextPaint().apply { color = Color.WHITE; textSize = 10f; typeface = theme.typefaceBody; isFakeBoldText = true }
        var cx = x
        cols.forEachIndexed { i, t ->
            canvas.drawText(t, cx + 2f, currY + 14f, headerTextPaint)
            cx += colWidths[i]
        }
        currY += rowHeight

        val rowPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; typeface = theme.typefaceBody }
        val linePaint = Paint().apply { color = theme.gridLineColor; strokeWidth = 1f }

        weapons.forEach { w ->
            cx = x

            val naturalKeywords = listOf(
                "Desarmado", "Ataque Natural", "Garra", "Mordida",
                "Casco", "Chifre", "Cabeça Dura", "Ferrão",
                "Toque Arrepiante", "Toque da Morte", "Toque Venenoso", "Tentáculo"
            )
            val isNaturalWeapon = naturalKeywords.any { w.nome.contains(it, ignoreCase = true) }

            val cdtStr = if (isNaturalWeapon) "-" else w.cdt?.toString()?.replace("\"", "") ?: "1"
            val pesoStr = if (isNaturalWeapon) "-" else w.peso?.toString()?.replace("\"", "") ?: "-"

            val paVal = w.pa?.toString()?.replace("\"", "")
            val paStr = if (isNaturalWeapon && (paVal == null || paVal == "0" || paVal.isBlank())) "-" else paVal ?: "0"

            val data = listOf(
                w.nome,
                w.distancia?.toString()?.replace("\"", "") ?: "-",
                w.dano?.toString()?.replace("\"", "") ?: "-",
                paStr,
                cdtStr,
                pesoStr
            )
            data.forEachIndexed { i, txt ->
                val safe = truncate(txt, rowPaint, colWidths[i] - 2f)
                canvas.drawText(safe, cx + 2f, currY + 14f, rowPaint)
                cx += colWidths[i]
                canvas.drawLine(cx, currY, cx, currY + rowHeight, linePaint)
            }
            canvas.drawLine(x, currY + rowHeight, x + width, currY + rowHeight, linePaint)
            currY += rowHeight
        }
        canvas.drawLine(x, y + 20f, x, currY, linePaint)
        canvas.drawLine(x + width, y + 20f, x + width, currY, linePaint)
    }

    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        val h = measure(width, theme)
        if (h <= availableHeight) return this to null
        // Atomic table for now
        return null to this
    }
}

// Helper
fun truncate(txt: String, paint: Paint, width: Float): String {
    if (paint.measureText(txt) <= width) return txt
    val c = paint.breakText(txt, true, width, null)
    return if (c > 0) txt.substring(0, c) else ""
}

// =================================================================================================
// 4. MAIN GENERATION LOGIC
// =================================================================================================

fun gerarFichaEmPdf(
    destino: File,
    personagem: MeuPersonagem,
    portrait: Bitmap? = null,
    listaAtributos: List<String>,
    mapaAtributosDisplay: Map<String, String>,
    listaComplicacoes: List<Complicacao>,
    listaVantagens: List<Vantagem>,
    listaPoderes: List<Poder>
) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val theme = getPdfTheme(personagem.appTheme)

    // Queues
    val leftQueue = ArrayDeque<PdfBlock>()
    leftQueue.add(AttributeBlock(personagem, listaAtributos, mapaAtributosDisplay))
    leftQueue.add(SkillListBlock(personagem))

    val hindranceNames = mutableListOf<String>()
    val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }
    personagem.complicacoes.forEach { id ->
        val comp = mapPorId[id.keyify()]
        if (comp != null) {
            val name = if (personagem.modoOficialAtivo && !comp.originalName.isNullOrBlank()) comp.originalName!! else comp.name
            val baseName = name.toFancyTitleCase()

            val severityStr = comp.severity.trim().lowercase()
            val isMenor = severityStr.contains("menor")
            val isMaior = severityStr.contains("maior")

            val sevDisplay = when {
                isMenor && isMaior -> ""
                isMenor -> " (Menor)"
                isMaior -> " (Maior)"
                else -> ""
            }

            val userChoice = personagem.complicacoesTipos[id]?.let {
                val c = it.lowercase()
                if (c.contains("menor")) " (Menor)"
                else if (c.contains("maior")) " (Maior)"
                else ""
            } ?: sevDisplay

            hindranceNames.add("$baseName$userChoice")
        } else {
            hindranceNames.add(id.replace('_', ' ').toFancyTitleCase())
        }
    }
    leftQueue.add(object : TextListBlock("Complicações", hindranceNames) {})

    val rightQueue = ArrayDeque<PdfBlock>()

    // Edges
    val edgeNames = personagem.vantagens.map { id ->
        try {
            val v = listaVantagens.firstOrNull { it.id == id }
            val baseName = if (v != null) {
                if (personagem.modoOficialAtivo && !v.originalName.isNullOrBlank()) v.originalName!! else v.nomeExibicao
            } else {
                id
            }
            // Check for choice
            val choice = personagem.advantageChoices[id]?.firstOrNull()
            if (choice != null) {
                "${baseName.toFancyTitleCase()} (${choice.trim().toFancyTitleCase()})"
            } else {
                baseName.toFancyTitleCase()
            }
        } catch(e: Exception) { id.toFancyTitleCase() }
    }
    rightQueue.add(object : TextListBlock("Vantagens", edgeNames) {})

    // Powers
    val isPathfinderGnome = personagem.compendioPathfinderAtivo && personagem.ancestralidade.uppercase().contains("GNOMO")
    if (personagem.poderes.isNotEmpty() || isPathfinderGnome) {
        val powerLines = mutableListOf<String>()

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
            val abCount = personagem.poderes.size
            val ppText = if (abCount == 0) " (1 PP)" else ""
            powerLines.add("Arcano: Truques")
            powerLines.add("$focoNome$ppText - Iluminar, Som, Telecinese, Amigo das Feras")
        }

        personagem.poderes.forEach { (arc, list) ->
            powerLines.add("Arcano: ${arc.toFancyTitleCase()}")
            val namedList = list.map { id ->
                val pName = listaPoderes.firstOrNull { it.id == id }?.nome ?: id
                var displayNome = pName.toFancyTitleCase()

                if (personagem.compendioPathfinderAtivo && arc.uppercase().trim() == "MISTICO") {
                    displayNome = displayNome
                        .replace("Aumentar/Reduzir Característica", "Aumentar Característica")
                        .replace("Morosidade/Velocidade", "Velocidade")
                }

                displayNome
            }
            powerLines.add(namedList.joinToString(", "))
        }
        rightQueue.add(object : TextListBlock("Poderes", powerLines) {})
    }

    // Superpoderes
    if (personagem.modoSupers &&
        (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
    ) {
        val superLines = mutableListOf<String>()
        val nivelStr = personagem.superNivelCampanha?.let { "Nível $it" } ?: "–"
        superLines.add("Nível da Campanha: $nivelStr")
        superLines.add("Superpontos: ${personagem.superPontosTotais} (disponíveis: ${personagem.superPontosDisponiveis})")
        superLines.add("Limite por Poder: ${personagem.limitePorPoderPadrao}")
        superLines.add("")

        if (personagem.gastosPorPoder.isEmpty()) {
            superLines.add("– Nenhum superpoder registrado")
        } else {
            personagem.gastosPorPoder.forEach { (poderId, custo) ->
                val cleanId = if (poderId.startsWith("sp_", ignoreCase = true)) {
                    poderId.substring(3)
                } else {
                    poderId
                }
                superLines.add("${cleanId.toFancyTitleCase()}: $custo SP")
            }
        }
        rightQueue.add(object : TextListBlock("Superpoderes", superLines) {})
    }

    // Weapons
    rightQueue.add(WeaponTableBlock(personagem))

    // Gear
    val gear = personagem.equipamentos.filterNot { it.dano != null }.map { it.nome.toFancyTitleCase() }
    if (gear.isNotEmpty()) {
        rightQueue.add(object : TextListBlock("Outros Equipamentos", gear) {})
    }

    // Notes
    if (personagem.anotacoes.isNotBlank()) {
        rightQueue.add(object : TextListBlock("Anotações", listOf(personagem.anotacoes)) {})
    }

    var pageIndex = 0

    while (leftQueue.isNotEmpty() || rightQueue.isNotEmpty()) {
        pageIndex++
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(theme.backgroundColor)

        // Borders
        val margin = 30f
        val w = pageInfo.pageWidth.toFloat()
        val h = pageInfo.pageHeight.toFloat()
        val borderPaint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRect(margin/2, margin/2, w - margin/2, h - margin/2, borderPaint)

        var contentTop = margin

        // Header on Page 1
        if (pageIndex == 1) {
            val headerH = 140f
            val derivedH = 60f
            val headerRect = RectF(margin, margin, w - margin, margin + headerH)
            drawHeader(canvas, headerRect, personagem, theme, portrait)

            val derivedRect = RectF(margin, headerRect.bottom + 10f, w - margin, headerRect.bottom + 10f + derivedH)
            drawDerivedStats(canvas, derivedRect, personagem, theme)

            contentTop = derivedRect.bottom + 20f
        } else {
            contentTop = margin + 20f // Small margin on subsequent pages
        }

        val columnGap = 20f
        val contentW = w - (margin * 2)
        val leftW = (contentW - columnGap) * 0.35f
        val rightW = (contentW - columnGap) * 0.65f
        val rightX = margin + leftW + columnGap
        val bottomLimit = h - margin

        // Fill Left
        var currY = contentTop
        while (leftQueue.isNotEmpty()) {
            val block = leftQueue.first()
            val available = bottomLimit - currY
            val (head, tail) = block.split(available, leftW, theme)

            if (head != null) {
                head.draw(canvas, margin, currY, leftW, theme)
                currY += head.measure(leftW, theme) + 10f
            }

            if (tail != null) {
                leftQueue.removeFirst()
                leftQueue.addFirst(tail)
                break // Page full
            } else {
                leftQueue.removeFirst() // Fully drawn
            }

            if (currY >= bottomLimit) break
        }

        // Fill Right
        currY = contentTop
        while (rightQueue.isNotEmpty()) {
            val block = rightQueue.first()
            val available = bottomLimit - currY
            val (head, tail) = block.split(available, rightW, theme)

            if (head != null) {
                head.draw(canvas, rightX, currY, rightW, theme)
                currY += head.measure(rightW, theme) + 10f
            }

            if (tail != null) {
                rightQueue.removeFirst()
                rightQueue.addFirst(tail)
                break
            } else {
                rightQueue.removeFirst()
            }

            if (currY >= bottomLimit) break
        }

        doc.finishPage(page)
    }

    FileOutputStream(destino).use { out -> doc.writeTo(out) }
    doc.close()
}

// =================================================================================================
// 5. SHARED THEME & HELPERS
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
        AppTheme.DEFAULT -> PdfTheme(
            backgroundColor = Color.rgb(247, 241, 230),
            primaryColor = Color.rgb(141, 29, 44),
            accentColor = Color.rgb(201, 155, 74),
            textColor = Color.rgb(46, 42, 38),
            gridLineColor = Color.rgb(184, 168, 150),
            headerBackground = Color.rgb(241, 230, 214),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.MEDIEVAL -> PdfTheme(
            backgroundColor = Color.rgb(248, 244, 235),
            primaryColor = Color.rgb(141, 110, 99),
            accentColor = Color.rgb(62, 39, 35),
            textColor = Color.rgb(62, 39, 35),
            gridLineColor = Color.rgb(141, 110, 99),
            headerBackground = Color.rgb(229, 214, 200),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.CYBERPUNK -> PdfTheme(
            backgroundColor = Color.BLACK,
            primaryColor = Color.rgb(0, 255, 65),
            accentColor = Color.rgb(0, 229, 255),
            textColor = Color.rgb(0, 255, 65),
            gridLineColor = Color.rgb(0, 59, 0),
            headerBackground = Color.rgb(5, 5, 5),
            typefaceTitle = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.SCIFI -> PdfTheme(
            backgroundColor = Color.rgb(5, 11, 20),
            primaryColor = Color.rgb(0, 229, 255),
            accentColor = Color.rgb(207, 216, 220),
            textColor = Color.rgb(207, 216, 220),
            gridLineColor = Color.rgb(19, 27, 38),
            headerBackground = Color.rgb(12, 19, 30),
            typefaceTitle = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
            shapeType = ShapeType.HEXAGON
        )
        AppTheme.HORROR -> PdfTheme(
            backgroundColor = Color.rgb(10, 10, 10),
            primaryColor = Color.rgb(138, 11, 11),
            accentColor = Color.rgb(158, 123, 91),
            textColor = Color.rgb(205, 198, 185),
            gridLineColor = Color.rgb(75, 26, 26),
            headerBackground = Color.rgb(18, 11, 11),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
        AppTheme.HALLOWEEN -> PdfTheme(
            backgroundColor = Color.rgb(13, 10, 15),
            primaryColor = Color.rgb(255, 109, 0),
            accentColor = Color.rgb(98, 0, 234),
            textColor = Color.rgb(255, 195, 122),
            gridLineColor = Color.rgb(98, 0, 234),
            headerBackground = Color.rgb(19, 16, 23),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
        AppTheme.WW2 -> PdfTheme(
            backgroundColor = Color.rgb(240, 230, 140),
            primaryColor = Color.rgb(47, 79, 79),
            accentColor = Color.rgb(75, 83, 32),
            textColor = Color.BLACK,
            gridLineColor = Color.rgb(75, 83, 32),
            headerBackground = Color.rgb(195, 176, 145),
            typefaceTitle = Typeface.create(Typeface.SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
        AppTheme.MINIMALIST -> PdfTheme(
            backgroundColor = Color.WHITE,
            primaryColor = Color.BLACK,
            accentColor = Color.rgb(232, 245, 233),
            textColor = Color.BLACK,
            gridLineColor = Color.rgb(189, 189, 189),
            headerBackground = Color.rgb(245, 245, 245),
            typefaceTitle = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
            typefaceBody = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
            shapeType = ShapeType.CIRCLE
        )
    }
}

// Helpers reused from previous implementation
fun drawHeader(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme, portrait: Bitmap?) {
    // ... (Same logic as before)
    val paint = Paint().apply {
        color = theme.headerBackground
        style = Paint.Style.FILL
    }
    canvas.drawRect(rect, paint)
    paint.style = Paint.Style.STROKE
    paint.color = theme.primaryColor
    paint.strokeWidth = 2f
    canvas.drawRect(rect, paint)

    val portraitW = 100f
    val portraitH = 120f
    val portraitX = rect.right - portraitW - 10f
    val portraitY = rect.top + 10f
    val portraitRect = RectF(portraitX, portraitY, portraitX + portraitW, portraitY + portraitH)

    if (portrait != null) {
        val path = Path().apply {
            if (theme.shapeType == ShapeType.CIRCLE) {
                addRoundRect(portraitRect, 10f, 10f, Path.Direction.CW)
            } else {
                addRect(portraitRect, Path.Direction.CW)
            }
        }
        canvas.save()
        canvas.clipPath(path)
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
        canvas.drawPath(path, paint)
    } else {
        val placePaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
        canvas.drawRect(portraitRect, placePaint)
        val textPaint = TextPaint().apply {
            color = Color.GRAY; textSize = 10f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Retrato", portraitRect.centerX(), portraitRect.centerY(), textPaint)
    }

    val titlePaint = TextPaint().apply {
        color = theme.primaryColor; typeface = theme.typefaceTitle; textSize = 24f; isAntiAlias = true
    }
    val subtitlePaint = TextPaint().apply {
        color = theme.textColor; typeface = theme.typefaceBody; textSize = 12f; isAntiAlias = true
    }

    val textAreaWidth = rect.width() - portraitW - 30f
    var displayedName = p.nome.ifBlank { "Sem Nome" }
    if (titlePaint.measureText(displayedName) > textAreaWidth) {
        val c = titlePaint.breakText(displayedName, true, textAreaWidth, null)
        displayedName = displayedName.substring(0, c) + "..."
    }

    canvas.drawText(displayedName, rect.left + 10f, rect.top + 30f, titlePaint)
    val ancestralidadeTitulo = buildAncestralidadeDisplay(p)
    canvas.drawText("$ancestralidadeTitulo - Novato", rect.left + 10f, rect.top + 50f, subtitlePaint)

    if (p.coracaoCrystalSelecionado != null) {
        val heartText = "Coração: ${p.coracaoCrystalSelecionado.nome}"
        canvas.drawText(heartText, rect.left + 10f, rect.top + 70f, subtitlePaint)
    }

    val trackX = rect.left + 10f
    val trackY = if (p.coracaoCrystalSelecionado != null) rect.top + 95f else rect.top + 80f
    drawTrack(canvas, trackX, trackY, "Ferimentos", 3, -1, theme)
    drawTrack(canvas, trackX + 100f, trackY, "Fadiga", 2, -1, theme)
}

fun drawTrack(canvas: Canvas, x: Float, y: Float, label: String, boxes: Int, current: Int, theme: PdfTheme) {
    val paint = Paint().apply { color = theme.textColor; typeface = theme.typefaceBody; textSize = 10f }
    canvas.drawText(label, x, y, paint)
    val boxSize = 12f; val gap = 5f; var currX = x; val trackY = y + 5f
    val boxPaint = Paint().apply { style = Paint.Style.STROKE; color = theme.primaryColor; strokeWidth = 1.5f }
    for (i in 1..boxes) {
        val r = RectF(currX, trackY, currX + boxSize, trackY + boxSize)
        canvas.drawRect(r, boxPaint)
        val penPaint = TextPaint(paint).apply { textSize = 8f; textAlign = Paint.Align.CENTER }
        canvas.drawText("-$i", r.centerX(), r.bottom + 8f, penPaint)
        currX += boxSize + gap
    }
}

fun drawDerivedStats(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme) {
    val aparar = calcAparar(p)
    val resistencia = calcResistencia(p)
    val mov = p.movimentacao
    val boxWidth = rect.width() / 3
    val labels = listOf("Aparar", "Resistência", "Movimentação")
    val values = listOf(aparar.toString(), resistencia, mov.toString())
    for (i in 0..2) {
        val bx = rect.left + (i * boxWidth)
        val r = RectF(bx, rect.top, bx + boxWidth, rect.bottom)
        val bgPaint = Paint().apply { color = if (i % 2 == 0) theme.headerBackground else theme.backgroundColor; style = Paint.Style.FILL }
        canvas.drawRect(r, bgPaint)
        val borderPaint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRect(r, borderPaint)
        val labelPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; textAlign = Paint.Align.CENTER; typeface = theme.typefaceBody }
        val valPaint = TextPaint().apply { color = theme.primaryColor; textSize = 24f; textAlign = Paint.Align.CENTER; typeface = theme.typefaceTitle; isFakeBoldText = true }
        canvas.drawText(labels[i], r.centerX(), r.top + 15f, labelPaint)
        canvas.drawText(values[i], r.centerX(), r.bottom - 15f, valPaint)
    }
}

fun drawAttributeShape(canvas: Canvas, cx: Float, cy: Float, text: String, theme: PdfTheme) {
    val paint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }
    if (theme.shapeType == ShapeType.HEXAGON) {
        val path = Path()
        val r = 20f
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i).toDouble())
            val px = cx + (r * Math.cos(angle)).toFloat()
            val py = cy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close(); canvas.drawPath(path, paint)
    } else {
        canvas.drawCircle(cx, cy, 20f, paint)
    }
    val textPaint = TextPaint().apply { color = theme.textColor; textSize = 14f; textAlign = Paint.Align.CENTER; typeface = theme.typefaceTitle }
    val metrics = textPaint.fontMetrics; val dy = (metrics.descent + metrics.ascent) / 2
    canvas.drawText(text, cx, cy - dy, textPaint)
}

// Calculation Helpers
fun calcAparar(personagem: MeuPersonagem): Int {
    val lutar = personagem.pericias["Lutar"] ?: 0
    val jutsu = personagem.pericias["Jutsu"] ?: 0
    val base = 2 + (max(lutar, jutsu) / 2)
    val bloq = if (personagem.vantagens.any { it.keyify() == "BLOQUEAR" }) 1 else 0
    val bloqImp = if (personagem.vantagens.any { it.keyify() == "BLOQUEAR APRIMORADO" }) 1 else 0

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

    return (base + bloq + bloqImp + personagem.bonusApararFromPower + apararBaixoMod + serranosApararMod + racialParryBonus + garcaParryBonus).coerceAtLeast(0)
}

fun calcResistencia(personagem: MeuPersonagem): String {
    val arm = (max(personagem.armorFromPower, personagem.armorBase) + personagem.naturalArmorFromRace).coerceAtLeast(0)
    val res = personagem.resistencia
    val total = res + arm
    return if (arm > 0) "$res($total)" else res.toString()
}
