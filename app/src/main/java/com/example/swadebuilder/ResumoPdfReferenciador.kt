package com.example.swadebuilder

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.swadebuilder.model.MeuPersonagem
import com.example.swadebuilder.util.keyify
import java.io.File
import java.io.FileOutputStream

// 1) Snapshot único do state
fun CriadorState.toMeuPersonagem(): MeuPersonagem {
    // Calcula os atributos derivados
    val derived = DerivedAttributesCalculator(this).calculate()

    return MeuPersonagem(
        nome = this.nomePersonagem,
        ancestralidade = this.ancestralidade,
        celestialAAMilagresDesabilitado = this.celestialAAMilagresDesabilitado,

        atributos = this.valoresAtributos.mapValues { it.value.intValue },
        pericias = this.periciasVisiveis,

        aparar = derived.aparar,
        resistencia = derived.resistencia,
        tamanho = derived.tamanho,
        movimento = derived.movimento,
        armadura = derived.armadura,

        vantagens = this.vantagensSelecionadas.map { it.nome },
        complicacoes = this.complicacoesSelecionadas.keys.map { it.name },
        desvantagensRaciais = this.desvantagensRaciais.toList(),

        equipamentos = this.equipamentosComprados.toList(),
        dinheiro = this.dinheiro,

        poderes = this.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
        pontosRestantes = this.pontosVantagem,

        // Supers
        modoSupers = this.modoSupers,
        superPontosTotais = this.superPontosTotais,
        superPontosDisponiveis = this.superPontosDisponiveis,
        limitePorPoderPadrao = this.limitePorPoderPadrao,
        limiteFavorecido = this.limiteFavorecido,
        poderFavoritoId = this.poderFavoritoId,
        superInvestments = this.superInvestments.toList(),
        bonusPararFromPower = this.bonusPararFromPower,
        bonusResFromPower = this.bonusResFromPower,
        armorFromPower = this.armorFromPower,
        bonusMovimentacaoFromPower = this.bonusMovimentacaoFromPower,
        vantagensDePoder = this.vantagensDePoder.toSet(),
        gastosPorPoder = this.gastosPorPoder.toMap(),
        limiteDePoderDaCampanha = this.limiteDePoderDaCampanha,

        anotacoes = this.anotacoes
    )
}

// 2) Calculadora de Atributos Derivados
class DerivedAttributesCalculator(private val state: CriadorState) {
    private val vantagens by lazy { state.vantagensSelecionadas.map { it.nome.keyify() } }
    private val complicacoes by lazy { state.complicacoesSelecionadas.keys.map { it.name.keyify() } }

    data class DerivedValues(
        val aparar: Int,
        val resistencia: Int,
        val tamanho: Int,
        val movimento: Int,
        val armadura: Int
    )

    fun calculate(): DerivedValues {
        val tamanho = calcTamanho()
        val resistencia = calcResistencia(tamanho)
        val movimento = calcMovimento()
        val aparar = calcAparar()
        val armadura = calcArmadura()
        return DerivedValues(aparar, resistencia, tamanho, movimento, armadura)
    }

    private fun calcTamanho(): Int {
        val ancestralidade = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == state.ancestralidade }
        val racialSize = ancestralidade?.desvantagens
            ?.firstOrNull { it.startsWith("TAMANHO", ignoreCase = true) }
            ?.substringAfter("TAMANHO")?.trim()?.toIntOrNull() ?: 0
        val obesoBonus = if (complicacoes.any { it == "obeso" }) 1 else 0
        val pequenoPenalty = if (complicacoes.any { it == "pequeno" }) -1 else 0
        return racialSize + obesoBonus + pequenoPenalty
    }

    private fun calcResistencia(tamanho: Int): Int {
        val vigor = state.valoresAtributos["VIGOR"]?.intValue ?: 4
        val base = 2 + (vigor / 2)
        val resistenciaBonus = if (vantagens.any { it == "resistencia" }) 1 else 0
        val fragilPenalty = if (complicacoes.any { it == "fragil" }) -1 else 0
        val brigaoBonus = vantagens.count { it in listOf("brigao", "pugilista") }
        return (base + resistenciaBonus + fragilPenalty + brigaoBonus + tamanho + state.bonusResFromPower).coerceAtLeast(0)
    }

    private fun calcMovimento(): Int {
        val base = 6
        val ancestralidade = listaAncestralidadesJson.firstOrNull { it.nome.keyify() == state.ancestralidade }
        val racialPenalty = ancestralidade?.desvantagens
            ?.any { it.contains("MOVIMENTAÇÃO REDUZIDA", ignoreCase = true) }
            .let { if (it == true) 1 else 0 }
        val lentoPenalty = if (complicacoes.any { it == "lento" }) 1 else 0
        val idosoPenalty = if (complicacoes.any { it == "idoso" }) 1 else 0
        val obesoPenalty = if (complicacoes.any { it == "obeso" }) 1 else 0
        val ligeiroBonus = if (vantagens.any { it == "ligeiro" }) 2 else 0
        return (base - racialPenalty - lentoPenalty - idosoPenalty - obesoPenalty + ligeiroBonus + state.bonusMovimentacaoFromPower).coerceAtLeast(0)
    }

    private fun calcAparar(): Int {
        val lutar = state.periciasVisiveis["Lutar"] ?: 0
        val base = 2 + (lutar / 2)
        val bloquearBonus = if (vantagens.any { it == "bloquear" }) 1 else 0
        val bloquearAprimoradoBonus = if (vantagens.any { it == "bloquear-aprimorado" }) 1 else 0
        return base + bloquearBonus + bloquearAprimoradoBonus + state.bonusPararFromPower
    }

    private fun calcArmadura(): Int {
        return state.armorFromPower.coerceAtLeast(0)
    }
}

// 3) Abrir/compartilhar PDF
fun salvarEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
    val pdfFile = File(context.getExternalFilesDir(null), "ficha_preenchida.pdf")
    gerarFichaEmPdf(pdfFile, dadosDoPersonagem)
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
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

// 4) Montagem do PDF
fun gerarFichaEmPdf(destino: File, personagem: MeuPersonagem) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    var page = doc.startPage(pageInfo)
    var canvas = page.canvas

    val paint = Paint()
    paint.textSize = 12f
    var y = 40f

    fun drawText(text: String, x: Float, isBold: Boolean = false) {
        paint.isFakeBoldText = isBold
        canvas.drawText(text, x, y, paint)
    }

    fun nextLine() {
        y += 16f
    }

    // Título
    paint.textSize = 18f
    drawText("Ficha de ${personagem.nome}", 40f, true)
    nextLine()
    nextLine()

    // Seções
    paint.textSize = 12f
    val sectionPaint = Paint(paint).apply { isFakeBoldText = true }

    // Identidade e Derivados
    canvas.drawText("Identidade", 40f, y, sectionPaint)
    nextLine()
    drawText("Raça: ${personagem.ancestralidade}", 50f)
    nextLine()
    val resistenciaTexto = if (personagem.armadura > 0) "${personagem.resistencia}(${personagem.armadura})" else "${personagem.resistencia}"
    drawText("Aparar: ${personagem.aparar} | Resistência: $resistenciaTexto | Movimento: ${personagem.movimento} | Tamanho: ${personagem.tamanho}", 50f)
    nextLine()
    nextLine()

    // Atributos e Perícias
    val col1X = 40f
    val col2X = 300f
    val startY = y

    canvas.drawText("Atributos", col1X, y, sectionPaint)
    nextLine()
    personagem.atributos.forEach { (nome, dado) ->
        drawText("• $nome: d$dado", col1X + 10)
        nextLine()
    }

    y = startY
    canvas.drawText("Perícias", col2X, y, sectionPaint)
    nextLine()
    if (personagem.pericias.isEmpty()) {
        drawText("– Nenhuma", col2X + 10)
        nextLine()
    } else {
        personagem.pericias.forEach { (nome, dado) ->
            drawText("• $nome: d$dado", col2X + 10)
            nextLine()
            if (y > 780f) { // Page break
                doc.finishPage(page)
                val newPage = doc.startPage(pageInfo)
                canvas = newPage.canvas
                y = 40f
            }
        }
    }

    y = maxOf(y, startY + (personagem.atributos.size + 1) * 16f)
    nextLine()

    // Vantagens e Complicações
    canvas.drawText("Vantagens", col1X, y, sectionPaint)
    nextLine()
    if (personagem.vantagens.isEmpty()) {
        drawText("– Nenhuma", col1X + 10)
    } else {
        drawText(personagem.vantagens.joinToString(", "), col1X + 10)
    }
    nextLine()
    nextLine()

    canvas.drawText("Complicações", col1X, y, sectionPaint)
    nextLine()
    val allComps = personagem.complicacoes + personagem.desvantagensRaciais
    if (allComps.isEmpty()) {
        drawText("– Nenhuma", col1X + 10)
    } else {
        drawText(allComps.joinToString(", "), col1X + 10)
    }
    nextLine()
    nextLine()

    // Poderes
    if (personagem.poderes.isNotEmpty()) {
        canvas.drawText("Poderes Arcanos", 40f, y, sectionPaint)
        nextLine()
        personagem.poderes.forEach { (arcano, lista) ->
            val poderes = if (lista.isEmpty()) "– nenhum" else lista.joinToString(", ")
            drawText("• $arcano: $poderes", 50f)
            nextLine()
        }
        nextLine()
    }

    if (personagem.modoSupers) {
        canvas.drawText("Superpoderes", 40f, y, sectionPaint)
        nextLine()
        if (personagem.gastosPorPoder.isEmpty()) {
            drawText("– Nenhum", 50f)
        } else {
            personagem.gastosPorPoder.forEach { (poder, custo) ->
                drawText("• $poder: $custo SP", 50f)
                nextLine()
            }
        }
        nextLine()
    }

    // Anotações
    if (personagem.anotacoes.isNotBlank()) {
        canvas.drawText("Anotações", 40f, y, sectionPaint)
        nextLine()
        personagem.anotacoes.lines().forEach {
            drawText(it, 50f)
            nextLine()
        }
    }

    doc.finishPage(page)
    try {
        FileOutputStream(destino).use { out ->
            doc.writeTo(out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        doc.close()
    }
}
