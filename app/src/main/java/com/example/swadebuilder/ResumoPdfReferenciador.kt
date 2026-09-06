package com.example.swadebuilder

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.graphics.withClip
import androidx.core.graphics.withTranslation
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.ui.sections.asText
import com.example.swadebuilder.ui.sections.toResumo
import com.example.swadebuilder.ui.theme.AppTheme
import com.example.swadebuilder.util.GenericNameMapper
import com.example.swadebuilder.util.SecurityUtils
import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.toFancyTitleCase
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
        dadoCorrida = this.valorDadoCorrida(),
        resistencia = this.resistenciaBase(),
        appTheme = this.appTheme.name,
        portraitFileName = this.portraitFileName,
        regraFamaAtiva = this.optRegraFama,
        fama = this.valorFama(),
        usaRiqueza = this.usaRiqueza,
        usaRequisicao = this.usaRequisicao,
        modoProgressaoAtivo = this.modoProgressaoAtivo,
        mechasSelecionados = this.mechasSelecionados.toList(),
        ciberneticosInstalados = this.ciberneticosInstalados.toList(),
        samuraiPosturasSelecionadas = this.samuraiPosturasSelecionadas.toList(),
        dominioClerigoSelecionado = this.dominioClerigoSelecionado,
        dominioClerigoPathfinderSelecionado = this.dominioClerigoPathfinderSelecionado,
        scifiVariant = if (this.compendioSciFiAtivo) this.scifiVariant else null
    )
}

/**
 * Páginas extras (opcionais) da ficha, geradas só quando o personagem tem o conteúdo
 * correspondente. O diálogo de exportação deixa o jogador escolher quais incluir antes
 * de gerar o PDF — assim ele pode reduzir a contagem de páginas pra imprimir.
 */
enum class FichaPdfSecao(val titulo: String) {
    PODERES("Antecedente Arcano"),
    SUPERPODERES("Superpoderes"),
    MECHAS("Mechas"),
    EQUIPAMENTOS("Equipamentos"),
    CIBERNETICOS("Cibernéticos")
}

fun secoesPdfDisponiveis(personagem: MeuPersonagem): Set<FichaPdfSecao> {
    val isPathfinderGnome = personagem.compendioPathfinderAtivo && personagem.ancestralidade.uppercase().contains("GNOMO")
    val gear = personagem.equipamentos.filterNot { it.dano != null }
    return buildSet {
        if (personagem.poderes.isNotEmpty() || isPathfinderGnome) add(FichaPdfSecao.PODERES)
        if (personagem.modoSupers && personagem.gastosPorPoder.isNotEmpty()) add(FichaPdfSecao.SUPERPODERES)
        if (personagem.mechasSelecionados.isNotEmpty()) add(FichaPdfSecao.MECHAS)
        if (gear.isNotEmpty()) add(FichaPdfSecao.EQUIPAMENTOS)
        if (personagem.ciberneticosInstalados.isNotEmpty()) add(FichaPdfSecao.CIBERNETICOS)
    }
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
    listaSuperPoderes: List<SuperPoder> = emptyList(),
    // Ver gerarFichaEmPdf.
    especieId: String? = null,
    secoesIncluidas: Set<FichaPdfSecao> = FichaPdfSecao.entries.toSet(),
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
                listaPoderes,
                listaSuperPoderes,
                especieId,
                secoesIncluidas
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
            canvas.withTranslation(x, currY) {
                sl.draw(this)
            }
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
                           items.forEach {
                               val sl = StaticLayout.Builder.obtain(it, 0, it.length, bPaint, width.toInt()).build()
                                canvas.withTranslation(x, cy) {
                                    sl.draw(this)
                                }
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

// Atributos: ver drawAttributesRow (faixa horizontal na página 1, fora do fluxo de blocos).

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

    private val separator = "    "
    private val rowHeight = 16f

    // Perícias lado a lado, "fluindo" pela largura toda e só quebrando linha quando não
    // cabe mais — igual a um parágrafo, em vez de uma coluna estreita com uma por linha
    // (o layout de coluna cheia dá espaço de sobra pra isso) ou espremidas em duas colunas.
    private fun wrapIntoLines(width: Float, paint: Paint): List<String> {
        if (skills.isEmpty()) return emptyList()
        val sepWidth = paint.measureText(separator)
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        var currentWidth = 0f
        skills.forEach { txt ->
            val txtWidth = paint.measureText(txt)
            val addWidth = if (currentLine.isEmpty()) txtWidth else txtWidth + sepWidth
            if (currentLine.isNotEmpty() && currentWidth + addWidth > width) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(txt)
                currentWidth = txtWidth
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(separator)
                currentLine.append(txt)
                currentWidth += addWidth
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    override fun measure(width: Float, theme: PdfTheme): Float {
        if (skills.isEmpty()) return 34f // Title + "None"
        val paint = TextPaint().apply { textSize = 11f; typeface = theme.typefaceBody }
        return 20f + (wrapIntoLines(width, paint).size * rowHeight)
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

        wrapIntoLines(width, bodyPaint).forEach { line ->
            canvas.drawText(line, x, currY + 11f, bodyPaint)
            currY += rowHeight
        }
    }

    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (measure(width, theme) <= availableHeight) return this to null
        // Moving whole block is safer/easier for layout than splitting mid-list.
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

            val showOfficialNames = EditionConfig.isFullEdition && p.modoOficialAtivo
            val nomeArma = if (showOfficialNames && !w.originalName.isNullOrBlank()) w.originalName else w.nomeExibicao

            val data = listOf(
                nomeArma,
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

// =================================================================================================
// 3b. BLOCKS FOR DEDICATED (SEPARATED-BY-TOPIC) PAGES
// =================================================================================================

/** Cabeçalho leve dentro de uma página dedicada (ex.: nome do Antecedente Arcano). */
class SmallHeaderBlock(private val text: String) : PdfBlock {
    private val h = 26f
    override fun measure(width: Float, theme: PdfTheme): Float = h
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val paint = TextPaint().apply { color = theme.primaryColor; typeface = theme.typefaceTitle; textSize = 13f; isFakeBoldText = true }
        canvas.drawText(text, x, y + 14f, paint)
        val linePaint = Paint().apply { color = theme.accentColor; strokeWidth = 1f }
        canvas.drawLine(x, y + 20f, x + width, y + 20f, linePaint)
    }
    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (h <= availableHeight) return this to null
        return null to this
    }
}

/** Só estatísticas do poder — sem descrição do que ele faz, por pedido explícito. */
data class PowerCardSpec(
    val nome: String,
    val estagio: String,
    val pp: String,
    val distancia: String,
    val duracao: String,
    val manifestacoes: List<String> = emptyList()
)

private fun drawStatCard(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, title: String, statLines: List<String>, extraLines: List<String>, theme: PdfTheme) {
    val rect = RectF(x, y, x + w, y + h)
    val bg = Paint().apply { color = theme.headerBackground; style = Paint.Style.FILL }
    canvas.drawRoundRect(rect, 6f, 6f, bg)
    val border = Paint().apply { color = theme.accentColor; style = Paint.Style.STROKE; strokeWidth = 1.2f }
    canvas.drawRoundRect(rect, 6f, 6f, border)

    val titlePaint = TextPaint().apply { color = theme.primaryColor; typeface = theme.typefaceTitle; textSize = 12.5f; isFakeBoldText = true }
    canvas.drawText(truncate(title, titlePaint, w - 16f), x + 8f, y + 16f, titlePaint)

    val statPaint = TextPaint().apply { color = theme.textColor; typeface = theme.typefaceBody; textSize = 9.5f }
    var ty = y + 30f
    statLines.forEach { line ->
        canvas.drawText(truncate(line, statPaint, w - 16f), x + 8f, ty, statPaint)
        ty += 13f
    }
    if (extraLines.isNotEmpty()) {
        val extraPaint = TextPaint().apply { color = theme.accentColor; typeface = theme.typefaceBody; textSize = 9f }
        extraLines.forEach { line ->
            canvas.drawText(truncate(line, extraPaint, w - 16f), x + 8f, ty, extraPaint)
            ty += 12f
        }
    }
}

/** Até dois cards de poder lado a lado, pra parecer uma grade sem sair do fluxo de coluna única. */
class PowerCardRowBlock(private val cards: List<PowerCardSpec>) : PdfBlock {
    private val cardHeight = if (cards.any { it.manifestacoes.isNotEmpty() }) 78f else 62f
    override fun measure(width: Float, theme: PdfTheme): Float = cardHeight
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val gap = 10f
        val cardW = if (cards.size > 1) (width - gap) / 2f else width
        cards.forEachIndexed { i, spec ->
            val cx = x + i * (cardW + gap)
            val statLines = listOf(
                "Estágio: ${spec.estagio}   •   PP: ${spec.pp}",
                "Alcance: ${spec.distancia}   •   Duração: ${spec.duracao}"
            )
            val extraLines = if (spec.manifestacoes.isNotEmpty()) {
                listOf("Manifestações: " + spec.manifestacoes.joinToString(", "))
            } else emptyList()
            drawStatCard(canvas, cx, y, cardW, cardHeight - 8f, spec.nome, statLines, extraLines, theme)
        }
    }
    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (cardHeight <= availableHeight) return this to null
        return null to this
    }
}

/** Só estatísticas do superpoder — sem os modificadores em prosa, mesma regra dos Poderes. */
data class SuperPoderCardSpec(
    val nome: String,
    val estagio: String,
    val custoBase: String,
    val investido: Int
)

/** Até dois cards de superpoder lado a lado, mesmo layout de [PowerCardRowBlock]. */
class SuperPoderCardRowBlock(private val cards: List<SuperPoderCardSpec>) : PdfBlock {
    private val cardHeight = 62f
    override fun measure(width: Float, theme: PdfTheme): Float = cardHeight
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val gap = 10f
        val cardW = if (cards.size > 1) (width - gap) / 2f else width
        cards.forEachIndexed { i, spec ->
            val cx = x + i * (cardW + gap)
            val statLines = listOf(
                "Estágio: ${spec.estagio}",
                "Custo base: ${spec.custoBase}   •   Investido: ${spec.investido} SP"
            )
            drawStatCard(canvas, cx, y, cardW, cardHeight - 8f, spec.nome, statLines, emptyList(), theme)
        }
    }
    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (cardHeight <= availableHeight) return this to null
        return null to this
    }
}

/** Estatísticas do mecha — chassi, defesas, mobilidade — sem prosa de mods/sistemas. */
class MechaCardBlock(private val m: com.example.swadebuilder.model.MechaItem) : PdfBlock {
    private val extras = buildList {
        if (m.customizacoes.blindagem_extra > 0) add("Blindagem extra: +${m.customizacoes.blindagem_extra}")
        if (m.customizacoes.propulsores) add("Propulsores instalados")
        if (m.mods_instalados.isNotEmpty()) add("Mods: " + m.mods_instalados.joinToString { it.nome })
        if (m.armas_equipadas.isNotEmpty()) add("Armas: " + m.armas_equipadas.joinToString())
        if (m.sistemas_instalados.isNotEmpty()) add("Sistemas: " + m.sistemas_instalados.joinToString())
    }
    private val cardHeight = 74f + (extras.size * 12f)
    override fun measure(width: Float, theme: PdfTheme): Float = cardHeight
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val statLines = listOf(
            "Chassi: ${m.categoria_chassi}   •   Tamanho: ${m.tamanho}   •   Manobrabilidade: ${m.manobrabilidade}",
            "Vel. Máxima: ${m.vel_maxima}\"   •   Resistência: ${m.resistencia_base}   •   Armadura: ${m.armadura_base}",
            "Ferimentos: ${m.ferimentos}   •   Força: ${m.forca}   •   Energia: ${m.energia_dias} dias"
        )
        drawStatCard(canvas, x, y, width, cardHeight - 8f, m.nome, statLines, extras, theme)
    }
    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (cardHeight <= availableHeight) return this to null
        return null to this
    }
}

/** Estatísticas do cibernético — tensão e modificadores — sem o texto de efeito. */
class CiberneticoCardBlock(private val c: com.example.swadebuilder.model.CiberneticoItem) : PdfBlock {
    private val cardHeight = if (c.modificacoes.isNotEmpty()) 62f else 48f
    override fun measure(width: Float, theme: PdfTheme): Float = cardHeight
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        val statLines = listOf("Tensão: ${c.strain_custo}")
        val extraLines = if (c.modificacoes.isNotEmpty()) listOf("Modificadores: " + c.modificacoes.joinToString(", ")) else emptyList()
        drawStatCard(canvas, x, y, width, cardHeight - 8f, c.nome, statLines, extraLines, theme)
    }
    override fun split(availableHeight: Float, width: Float, theme: PdfTheme): Pair<PdfBlock?, PdfBlock?> {
        if (cardHeight <= availableHeight) return this to null
        return null to this
    }
}

/** Tabela de equipamentos gerais (não-arma) — nome, custo, peso. Sem observações (prosa). */
class GearTableBlock(private val rows: List<Triple<String, String, String>>) : PdfBlock {
    override fun measure(width: Float, theme: PdfTheme): Float {
        if (rows.isEmpty()) return 0f
        val rowHeight = 20f
        return 20f + 20f + (rows.size * rowHeight) + 5f
    }
    override fun draw(canvas: Canvas, x: Float, y: Float, width: Float, theme: PdfTheme) {
        if (rows.isEmpty()) return
        var currY = y
        val rowHeight = 20f

        val cols = listOf("Item", "Custo", "Peso")
        val colWeights = listOf(4f, 1f, 1f)
        val totalWeight = colWeights.sum()
        val unitW = width / totalWeight
        val colWidths = colWeights.map { it * unitW }

        val headerPaint = Paint().apply { color = theme.gridLineColor; style = Paint.Style.FILL }
        canvas.drawRect(x, currY, x + width, currY + rowHeight, headerPaint)
        val headerTextPaint = TextPaint().apply { color = Color.WHITE; textSize = 10f; typeface = theme.typefaceBody; isFakeBoldText = true }
        var cx = x
        cols.forEachIndexed { i, t ->
            canvas.drawText(t, cx + 4f, currY + 14f, headerTextPaint)
            cx += colWidths[i]
        }
        currY += rowHeight

        val rowPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; typeface = theme.typefaceBody }
        val linePaint = Paint().apply { color = theme.gridLineColor; strokeWidth = 1f }
        rows.forEach { (nome, custo, peso) ->
            cx = x
            val data = listOf(nome, custo, peso)
            data.forEachIndexed { i, txt ->
                val safe = truncate(txt, rowPaint, colWidths[i] - 4f)
                canvas.drawText(safe, cx + 4f, currY + 14f, rowPaint)
                cx += colWidths[i]
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
    listaPoderes: List<Poder>,
    listaSuperPoderes: List<SuperPoder> = emptyList(),
    // Id estável da espécie da ancestralidade atual (RacialModifier.especieId,
    // ex.: state.currentAncestryDef?.especieId), resolvido pelo chamador —
    // ver drawHeader/calcAparar. Null pra raça customizada (nunca aciona
    // regra oficial por engano) ou quando o chamador não o resolveu.
    especieId: String? = null,
    secoesIncluidas: Set<FichaPdfSecao> = FichaPdfSecao.entries.toSet()
) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val theme = getPdfTheme(personagem.appTheme)

    // Fila única, coluna cheia — Atributos vira faixa horizontal (ver drawAttributesRow) e
    // cada seção abaixo dela (Perícias, Complicações, Vantagens, Armas, Anotações) ocupa
    // sua própria linha de largura total, em vez das duas colunas fixas de antes.
    val mainQueue = ArrayDeque<PdfBlock>()
    mainQueue.add(SkillListBlock(personagem))

    val hindranceNames = mutableListOf<String>()
    val mapPorId = listaComplicacoes.associateBy { it.id.keyify() }
    val showOfficialNames = EditionConfig.isFullEdition && personagem.modoOficialAtivo
    personagem.complicacoes.forEach { id ->
        val comp = mapPorId[id.keyify()]
        if (comp != null) {
            val name = if (showOfficialNames && !comp.originalName.isNullOrBlank()) comp.originalName else comp.nomeExibicao
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
            val mappedFallback = if (!EditionConfig.isFullEdition) GenericNameMapper.map(id.replace('_', ' ')) else id.replace('_', ' ')
            hindranceNames.add(mappedFallback.toFancyTitleCase())
        }
    }
    mainQueue.add(object : TextListBlock("Complicações", hindranceNames) {})

    // Edges
    val edgeNames = personagem.vantagens.map { id ->
        try {
            val v = listaVantagens.firstOrNull { it.id == id }
            val baseName = if (v != null) {
                if (showOfficialNames && !v.originalName.isNullOrBlank()) v.originalName else v.nomeExibicao
            } else {
                if (!EditionConfig.isFullEdition) GenericNameMapper.map(id) else id
            }
            // Check for choice
            val choice = personagem.advantageChoices[id]?.firstOrNull()
            if (choice != null) {
                val mappedChoice = if (!EditionConfig.isFullEdition) GenericNameMapper.map(choice) else choice
                "${baseName.toFancyTitleCase()} (${mappedChoice.trim().toFancyTitleCase()})"
            } else {
                baseName.toFancyTitleCase()
            }
        } catch(e: Exception) {
            val fallback = if (!EditionConfig.isFullEdition) GenericNameMapper.map(id) else id
            fallback.toFancyTitleCase()
        }
    }
    mainQueue.add(object : TextListBlock("Vantagens", edgeNames) {})

    // Poderes agora ganham página dedicada (ver renderPoderesPages logo abaixo do loop
    // principal) — página 1 fica só com o essencial de combate/perícias.
    val isPathfinderGnome = personagem.compendioPathfinderAtivo && personagem.ancestralidade.uppercase().contains("GNOMO")

    // Superpoderes — resumo de campanha fica na página principal; o detalhamento por
    // poder (stats) vira página dedicada (ver renderSectionPages abaixo do loop).
    if (personagem.modoSupers &&
        (personagem.superPontosTotais > 0 || personagem.gastosPorPoder.isNotEmpty())
    ) {
        val nivelStr = personagem.superNivelCampanha?.let { "Nível $it" } ?: "–"
        val superLines = listOf(
            "Nível da Campanha: $nivelStr",
            "Superpontos: ${personagem.superPontosTotais} (disponíveis: ${personagem.superPontosDisponiveis})",
            "Limite por Poder: ${personagem.limitePorPoderPadrao}"
        )
        mainQueue.add(object : TextListBlock("Superpoderes", superLines) {})
    }

    // Weapons — fica na página principal por ser referência constante em combate.
    mainQueue.add(WeaponTableBlock(personagem))

    // Equipamentos gerais, Mechas e Cibernéticos agora têm página dedicada (abaixo).

    // Notes
    if (personagem.anotacoes.isNotBlank()) {
        mainQueue.add(object : TextListBlock("Anotações", listOf(personagem.anotacoes)) {})
    }

    var pageIndex = 0

    while (mainQueue.isNotEmpty()) {
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
            val attributesH = 90f
            val headerRect = RectF(margin, margin, w - margin, margin + headerH)
            // Estatísticas derivadas (Aparar, Resistência, etc.) agora vão dentro do
            // próprio cabeçalho, no vão entre o nome e o retrato — ver drawHeader.
            drawHeader(canvas, headerRect, personagem, theme, portrait, especieId)

            // Atributos em uma faixa horizontal (em vez de empilhados na coluna
            // esquerda) — ocupa bem menos altura, sobrando espaço pro resto da página 1.
            val attributesRect = RectF(margin, headerRect.bottom + 10f, w - margin, headerRect.bottom + 10f + attributesH)
            drawAttributesRow(canvas, attributesRect, personagem, theme, listaAtributos, mapaAtributosDisplay)

            contentTop = attributesRect.bottom + 20f
        } else {
            contentTop = margin + 20f // Small margin on subsequent pages
        }

        val contentW = w - (margin * 2)
        val bottomLimit = h - margin

        // Cada seção ocupa a largura toda da página, empilhada de cima pra baixo —
        // sem mais colunas fixas lado a lado.
        var currY = contentTop
        while (mainQueue.isNotEmpty()) {
            val block = mainQueue.first()
            val available = bottomLimit - currY
            val (head, tail) = block.split(available, contentW, theme)

            if (head != null) {
                head.draw(canvas, margin, currY, contentW, theme)
                currY += head.measure(contentW, theme) + 10f
            }

            if (tail != null) {
                mainQueue.removeFirst()
                mainQueue.addFirst(tail)
                break // Page full
            } else {
                mainQueue.removeFirst() // Fully drawn
            }

            if (currY >= bottomLimit) break
        }

        doc.finishPage(page)
    }

    // Páginas dedicadas, separadas por tópico — só entram se o personagem tiver o
    // conteúdo correspondente e a seção estiver marcada no diálogo de exportação.
    if (FichaPdfSecao.PODERES in secoesIncluidas && (personagem.poderes.isNotEmpty() || isPathfinderGnome)) {
        renderSectionPages(doc, pageInfo, theme, FichaPdfSecao.PODERES.titulo, buildPoderesBlocks(personagem, listaPoderes, isPathfinderGnome, mapaAtributosDisplay))
    }
    if (FichaPdfSecao.SUPERPODERES in secoesIncluidas && personagem.modoSupers && personagem.gastosPorPoder.isNotEmpty()) {
        renderSectionPages(doc, pageInfo, theme, FichaPdfSecao.SUPERPODERES.titulo, buildSuperPoderesBlocks(personagem, listaSuperPoderes))
    }
    if (FichaPdfSecao.MECHAS in secoesIncluidas && personagem.mechasSelecionados.isNotEmpty()) {
        renderSectionPages(doc, pageInfo, theme, FichaPdfSecao.MECHAS.titulo, buildMechasBlocks(personagem))
    }
    if (FichaPdfSecao.EQUIPAMENTOS in secoesIncluidas) {
        renderSectionPages(doc, pageInfo, theme, FichaPdfSecao.EQUIPAMENTOS.titulo, buildEquipamentosBlocks(personagem, showOfficialNames))
    }
    if (FichaPdfSecao.CIBERNETICOS in secoesIncluidas && personagem.ciberneticosInstalados.isNotEmpty()) {
        renderSectionPages(doc, pageInfo, theme, FichaPdfSecao.CIBERNETICOS.titulo, buildCiberneticosBlocks(personagem))
    }

    FileOutputStream(destino).use { out -> doc.writeTo(out) }
    doc.close()
}

// =================================================================================================
// 4b. DEDICATED PAGE RENDERING (separadas por tópico, coluna única)
// =================================================================================================

private fun renderSectionPages(
    doc: PdfDocument,
    pageInfo: PdfDocument.PageInfo,
    theme: PdfTheme,
    sectionTitle: String,
    blocks: List<PdfBlock>
) {
    if (blocks.isEmpty()) return
    val margin = 30f
    val queue = ArrayDeque(blocks)
    val w = pageInfo.pageWidth.toFloat()
    val h = pageInfo.pageHeight.toFloat()
    val bottomLimit = h - margin
    val contentW = w - (margin * 2)

    while (queue.isNotEmpty()) {
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(theme.backgroundColor)
        val borderPaint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 1f }
        canvas.drawRect(margin / 2, margin / 2, w - margin / 2, h - margin / 2, borderPaint)

        var currY = drawSectionBanner(canvas, margin, w, sectionTitle, theme)

        while (queue.isNotEmpty()) {
            val block = queue.first()
            val available = bottomLimit - currY
            val (head, tail) = block.split(available, contentW, theme)

            if (head != null) {
                head.draw(canvas, margin, currY, contentW, theme)
                currY += head.measure(contentW, theme) + 10f
            }

            if (tail != null) {
                queue.removeFirst()
                queue.addFirst(tail)
                break
            } else {
                queue.removeFirst()
            }

            if (currY >= bottomLimit) break
        }

        doc.finishPage(page)
    }
}

/** Banner temático no topo de cada página dedicada: cor e forma (círculo/hexágono) seguem o tema do app. */
private fun drawSectionBanner(canvas: Canvas, margin: Float, pageWidth: Float, title: String, theme: PdfTheme): Float {
    val bannerHeight = 40f
    val rect = RectF(margin, margin, pageWidth - margin, margin + bannerHeight)
    val bgPaint = Paint().apply { color = theme.headerBackground; style = Paint.Style.FILL }
    canvas.drawRect(rect, bgPaint)
    val borderPaint = Paint().apply { color = theme.primaryColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
    canvas.drawRect(rect, borderPaint)

    val shapeCx = rect.left + 22f
    val shapeCy = rect.centerY()
    val shapePaint = Paint().apply { color = theme.accentColor; style = Paint.Style.FILL; isAntiAlias = true }
    if (theme.shapeType == ShapeType.HEXAGON) {
        val path = Path()
        val r = 11f
        for (i in 0..5) {
            val angle = Math.toRadians((60 * i).toDouble())
            val px = shapeCx + (r * Math.cos(angle)).toFloat()
            val py = shapeCy + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
        canvas.drawPath(path, shapePaint)
    } else {
        canvas.drawCircle(shapeCx, shapeCy, 11f, shapePaint)
    }

    val titlePaint = TextPaint().apply { color = theme.primaryColor; typeface = theme.typefaceTitle; textSize = 18f; isFakeBoldText = true }
    val metrics = titlePaint.fontMetrics
    val ty = rect.centerY() - (metrics.descent + metrics.ascent) / 2
    canvas.drawText(title, rect.left + 44f, ty, titlePaint)

    return rect.bottom + 20f
}

private fun buildPoderesBlocks(
    personagem: MeuPersonagem,
    listaPoderes: List<Poder>,
    isPathfinderGnome: Boolean,
    mapaAtributosDisplay: Map<String, String>
): List<PdfBlock> {
    val blocks = mutableListOf<PdfBlock>()

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
        val ppText = if (personagem.poderes.isEmpty()) "1" else "-"
        blocks.add(SmallHeaderBlock("Arcano: Truques"))
        blocks.add(
            PowerCardRowBlock(
                listOf(
                    PowerCardSpec(
                        nome = "Truques ($focoNome)",
                        estagio = "Novato",
                        pp = ppText,
                        distancia = "-",
                        duracao = "-",
                        manifestacoes = listOf("Iluminar", "Som", "Telecinese", "Amigo das Feras")
                    )
                )
            )
        )
    }

    personagem.poderes.forEach { (arc, ids) ->
        val arcLabel = "Arcano: ${arc.toFancyTitleCase()}".let { if (!EditionConfig.isFullEdition) GenericNameMapper.map(it) else it }
        blocks.add(SmallHeaderBlock(arcLabel))
        val specs = ids.map { id ->
            val poder = listaPoderes.firstOrNull { it.id == id }
            var nome = poder?.nome ?: id
            if (!EditionConfig.isFullEdition) nome = GenericNameMapper.map(nome)
            nome = nome.toFancyTitleCase()
            if (personagem.compendioPathfinderAtivo && arc.uppercase().trim() == "MISTICO") {
                nome = nome
                    .replace("Aumentar/Reduzir Característica", "Aumentar Característica")
                    .replace("Morosidade/Velocidade", "Velocidade")
            }
            PowerCardSpec(
                nome = nome,
                estagio = poder?.estagio ?: "-",
                pp = poder?.pontosDePoder ?: "-",
                distancia = poder?.distancia ?: "-",
                duracao = poder?.duracao ?: "-",
                manifestacoes = poder?.manifestacoes ?: emptyList()
            )
        }
        specs.chunked(2).forEach { pair -> blocks.add(PowerCardRowBlock(pair)) }
    }

    return blocks
}

private fun buildSuperPoderesBlocks(personagem: MeuPersonagem, listaSuperPoderes: List<SuperPoder>): List<PdfBlock> {
    if (personagem.gastosPorPoder.isEmpty()) return emptyList()
    val specs = personagem.gastosPorPoder.map { (poderId, custo) ->
        val cleanId = if (poderId.startsWith("sp_", ignoreCase = true)) poderId.substring(3) else poderId
        val sp = listaSuperPoderes.firstOrNull { "sp_${it.nome.keyify()}" == poderId }
        SuperPoderCardSpec(
            nome = (sp?.nome ?: cleanId).toFancyTitleCase(),
            estagio = sp?.estagio?.toFancyTitleCase() ?: "-",
            custoBase = sp?.custoBase ?: "-",
            investido = custo
        )
    }
    return specs.chunked(2).map { pair -> SuperPoderCardRowBlock(pair) }
}

private fun buildMechasBlocks(personagem: MeuPersonagem): List<PdfBlock> =
    personagem.mechasSelecionados.map { MechaCardBlock(it) }

private fun buildCiberneticosBlocks(personagem: MeuPersonagem): List<PdfBlock> =
    personagem.ciberneticosInstalados.map { CiberneticoCardBlock(it) }

private fun buildEquipamentosBlocks(personagem: MeuPersonagem, showOfficialNames: Boolean): List<PdfBlock> {
    val gear = personagem.equipamentos.filterNot { it.dano != null }
    if (gear.isEmpty()) return emptyList()
    val rows = gear.map { eq ->
        val name = if (showOfficialNames && !eq.originalName.isNullOrBlank()) eq.originalName else eq.nomeExibicao
        val custo = eq.toResumo().custo ?: "-"
        val peso = eq.peso.asText() ?: "-"
        Triple(name.toFancyTitleCase(), custo, peso)
    }
    return listOf(GearTableBlock(rows))
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
fun drawHeader(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme, portrait: Bitmap?, especieId: String? = null) {
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
        canvas.withClip(path) {
            val scale = Math.max(portraitW / portrait.width, portraitH / portrait.height)
            val scaledW = portrait.width * scale
            val scaledH = portrait.height * scale
            val dx = portraitRect.left + (portraitW - scaledW) / 2f
            val dy = portraitRect.top + (portraitH - scaledH) / 2f
            val matrix = android.graphics.Matrix().apply {
                postScale(scale, scale)
                postTranslate(dx, dy)
            }
            drawBitmap(portrait, matrix, null)
        }
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

    // Vão entre o nome e o retrato: em vez de deixar em branco, mostra as estatísticas
    // derivadas ali (texto simples, sem caixinha) — o nome passa a truncar antes dessa
    // coluna, em vez de ir quase até o retrato.
    val statsColumnLeft = rect.left + 240f
    val textAreaWidth = statsColumnLeft - rect.left - 20f
    var displayedName = p.nome.ifBlank { "Sem Nome" }
    if (titlePaint.measureText(displayedName) > textAreaWidth) {
        val c = titlePaint.breakText(displayedName, true, textAreaWidth, null)
        displayedName = displayedName.substring(0, c) + "..."
    }

    canvas.drawText(displayedName, rect.left + 10f, rect.top + 30f, titlePaint)
    val ancestralidadeTitulo = buildAncestralidadeDisplay(p, especieId = especieId)
    canvas.drawText("$ancestralidadeTitulo - Novato", rect.left + 10f, rect.top + 50f, subtitlePaint)

    if (p.coracaoCrystalSelecionado != null) {
        val heartName = if (!EditionConfig.isFullEdition) GenericNameMapper.map(p.coracaoCrystalSelecionado.nome) else p.coracaoCrystalSelecionado.nome
        val heartText = "Coração: $heartName"
        canvas.drawText(heartText, rect.left + 10f, rect.top + 70f, subtitlePaint)
    }

    val trackX = rect.left + 10f
    val trackY = if (p.coracaoCrystalSelecionado != null) rect.top + 95f else rect.top + 80f
    drawTrack(canvas, trackX, trackY, "Ferimentos", 3, -1, theme)
    drawTrack(canvas, trackX + 100f, trackY, "Fadiga", 2, -1, theme)

    val statLabelPaint = TextPaint().apply { color = theme.textColor; textSize = 10f; typeface = theme.typefaceBody }
    val statValuePaint = TextPaint().apply { color = theme.primaryColor; textSize = 12f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    val statPairs = listOf(
        "Aparar" to calcAparar(p, especieId).toString(),
        "Resistência" to calcResistencia(p),
        "Tamanho" to p.tamanho.toString(),
        "Movimentação" to p.movimentacao.toString(),
        "Corrida" to p.dadoCorrida
    )
    var statY = rect.top + 20f
    statPairs.forEach { (label, value) ->
        canvas.drawText(label, statsColumnLeft, statY, statLabelPaint)
        val labelWidth = statLabelPaint.measureText(label)
        canvas.drawText(value, statsColumnLeft + labelWidth + 6f, statY, statValuePaint)
        statY += 20f
    }
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

// Estatísticas derivadas: ver drawHeader (agora desenhadas dentro do cabeçalho, no vão
// entre o nome e o retrato, em vez de numa faixa colorida própria).

/** Atributos numa faixa horizontal (título + uma forma por atributo), em vez de empilhados
 *  numa coluna — o mesmo espaço vertical de antes dava pra só 5 atributos e sobrava
 *  muito vazio na coluna ao lado. */
fun drawAttributesRow(canvas: Canvas, rect: RectF, p: MeuPersonagem, theme: PdfTheme, listaAtributos: List<String>, mapaAtributosDisplay: Map<String, String>) {
    if (listaAtributos.isEmpty()) return

    val titlePaint = TextPaint().apply { color = theme.primaryColor; textSize = 14f; typeface = theme.typefaceTitle; isFakeBoldText = true }
    canvas.drawText("Atributos", rect.left, rect.top + 14f, titlePaint)

    val boxWidth = rect.width() / listaAtributos.size
    val shapeCy = rect.top + 42f
    val namePaint = TextPaint().apply { color = theme.textColor; textSize = 10f; typeface = theme.typefaceBody; isFakeBoldText = true; textAlign = Paint.Align.CENTER }

    listaAtributos.forEachIndexed { i, attr ->
        val value = p.atributos[attr] ?: 4
        val display = mapaAtributosDisplay[attr] ?: attr
        val cx = rect.left + (boxWidth * i) + (boxWidth / 2f)
        drawAttributeShape(canvas, cx, shapeCy, value.toDiceString(), theme)
        canvas.drawText(display, cx, shapeCy + 34f, namePaint)
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
fun calcAparar(personagem: MeuPersonagem, especieId: String? = null): Int {
    val lutar = personagem.pericias["Lutar"] ?: 0
    val jutsu = personagem.pericias["Jutsu"] ?: 0
    val base = 2 + (max(lutar, jutsu) / 2)
    val bloq = if (personagem.vantagens.contains(Constants.ID_BLOQUEAR)) 1 else 0
    val bloqImp = if (personagem.vantagens.contains(Constants.ID_BLOQUEAR_APRIMORADO)) 1 else 0

    // especieId (RacialModifier.especieId) resolvido pelo chamador — ver
    // gerarFichaEmPdf. Fica null pra raça customizada, então nunca casa com
    // "deaders"/"serranos"/"humano" por acidente; se ausente (chamador
    // antigo que não resolveu), cai no heurístico por nome de antes.
    val isDeaders = if (especieId != null) especieId == "deaders" else personagem.ancestralidade.keyify().contains("DEADERS")
    val hasApararBaixo = isDeaders || personagem.desvantagensRaciais.any { it.keyify() == "APARAR BAIXO" || it.keyify() == "APARAR_BAIXO" }
    val apararBaixoMod = if (hasApararBaixo) -2 else 0

    val isSerranos = if (especieId != null) especieId == "serranos" else personagem.ancestralidade.keyify().contains("SERRANOS")
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

    val isHumano = if (especieId != null) especieId == "humano" else personagem.ancestralidade.keyify().contains("HUMANO")
    val garcaParryBonus =
        if (
            personagem.compendioArteDaGuerraAtivo &&
            isHumano &&
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
