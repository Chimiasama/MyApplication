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

/**
 * Fonte única para Resumo e PDF.
 * - Snapshot do state -> MeuPersonagem
 * - buildSummaryLines() -> usado por UI e PDF
 * - gerarFichaEmPdf() / salvarEExibirFichaPdf()
 */

// ✅ 1) Snapshot único do state
fun CriadorState.toMeuPersonagem(): MeuPersonagem {
    return MeuPersonagem(
        nome = this.nomePersonagem,
        atributos = this.valoresAtributos.mapValues { it.value.intValue },
        pericias = listaPericias.associate { per -> per.nome to this.rawTotal(per) },
        ancestralidade = this.ancestralidade,
        celestialAAMilagresDesabilitado = this.celestialAAMilagresDesabilitado,
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
        equipamentos = this.equipamentosComprados.toList(),
        poderes = this.poderSlotsPorArcano.mapValues { (_, slots) -> slots.filterNotNull() },
        dinheiro = this.dinheiro,
        pontosRestantes = this.pontosVantagem,
        naturalArmorFromRace = this.naturalArmorFromRace,
        armorBase = this.armadura,

        // supers
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

        anotacoes = this.anotacoes,
        soldadoCargaAtivo = this.soldadoCargaAtivo
    )
}

// ✅ 2) Abrir/compartilhar PDF
fun salvarEExibirFichaPdf(context: Context, dadosDoPersonagem: MeuPersonagem) {
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

    val ancestralidadeNome: String = listaAncestralidadesJson
        .firstOrNull { it.nome.keyify() == personagem.ancestralidade }
        ?.nome ?: personagem.ancestralidade

    val vantagensNomeKey: List<String> = listaVantagens
        .filter { it.id in personagem.vantagens }
        .map { it.nome.keyify() }
    val vantagemChoices: MutableMap<String, MutableList<String>> = personagem.advantageChoices
        .mapValues { it.value.toMutableList() }
        .toMutableMap()

    val allComplicationsKeys: List<String> =
        personagem.complicacoes + personagem.desvantagensRaciais

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
        val lutarStepsFromSupers = personagem.superInvestments
            .mapNotNull { it.effect as? com.example.swadebuilder.model.PowerEffect.SuperPericia }
            .filter { it.periciaKey.equals("Lutar", ignoreCase = true) }
            .sumOf { it.steps }
        val lutarComSupers = applySuperStepsFrom(lutarRawBase, lutarStepsFromSupers)

        val base = 2 + (lutarComSupers / 2)

        val bloquearBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR" }) 1 else 0
        val bloquearAprimoradoBonus =
            if (vantagensNomeKey.any { it == "BLOQUEAR APRIMORADO" }) 1 else 0

        return base + bloquearBonus + bloquearAprimoradoBonus + personagem.bonusPararFromPower
    }

    fun calcArmaduraEfetiva(): Int {
        val melhorExterna = personagem.armorFromPower.coerceAtLeast(personagem.armorBase)
        return (melhorExterna + personagem.naturalArmorFromRace).coerceAtLeast(0)
    }

    val aparar = calcAparar()
    val resFinal = resistenciaFinal()
    val tamanho = tamanhoTotal()
    val mov = calcMovimento()
    val armadura = calcArmaduraEfetiva()
    val resistenciaTexto =
        if (armadura > 0) "${resFinal}(${armadura})" else resFinal.toString()

    lines += "Identidade"
    lines += "Nome: ${personagem.nome.ifBlank { "(sem nome)" }}"
    lines += "Ancestralidade: $ancestralidadeNome"
    lines += ""

    lines += "Atributos derivados"
    lines += "Aparar: $aparar"
    lines += "Resistência: $resistenciaTexto"
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
            lines += "${per.nome} d$raw"
        }
    }
    lines += ""

    lines += "Recursos & Equipamentos"
    lines += "Dinheiro restante: ${personagem.dinheiro}"
    if (personagem.equipamentos.isEmpty()) {
        lines += "Equipamentos: – Nenhum"
    } else {
        lines += "Equipamentos:"
        personagem.equipamentos.forEach { eq ->
            lines += "• ${eq.nome}"
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
                val baseNome = if (vant.id == "antecedente_arcano_milagres" && personagem.celestialAAMilagresDesabilitado) {
                    "${vant.nome} (DESABILITADO)"
                } else {
                    vant.nome
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
    val complicacoesText = personagem.complicacoes.map { compId ->
        listaComplicacoes.firstOrNull { it.id == compId }?.name ?: compId
    }.joinToString(", ").ifBlank { "– Nenhuma" }
    lines += complicacoesText
    if (personagem.desvantagensRaciais.isNotEmpty()) {
        val desvantagensNomes = personagem.desvantagensRaciais.map { desvantagemId ->
            // Tenta encontrar pelo id, se não, usa o texto original (para casos como "Tamanho -1")
            listaComplicacoes.firstOrNull { it.id.keyify() == desvantagemId.keyify() }?.name ?: desvantagemId
        }
        lines += "Anotações Raciais: ${desvantagensNomes.joinToString(", ")}"
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
                val nomesPoderes = lista.map { poderId ->
                    listaPoderes.firstOrNull { it.id == poderId }?.nome ?: poderId
                }
                "• $label: ${nomesPoderes.joinToString(", ")}"
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
                val poderName = listaPoderes.firstOrNull { it.id == poderId }?.nome ?: poderId
                lines += "• $poderName: $custo SP"
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

    val marginLeft = 40f
    val marginRight = 40f
    val marginTop = 50f
    val marginBottom = 40f

    val paint = Paint().apply { textSize = 12f }
    val fm = paint.fontMetrics
    val lineHeight = fm.descent - fm.ascent + fm.leading

    var page = doc.startPage(pageInfo)
    var canvas = page.canvas
    var y = marginTop

    fun newPage() {
        doc.finishPage(page)
        page = doc.startPage(pageInfo)
        canvas = page.canvas
        y = marginTop
    }

    fun drawWrapped(text: String) {
        var start = 0
        val maxWidth = pageInfo.pageWidth - marginLeft - marginRight
        while (start < text.length) {
            val count = paint.breakText(text, start, text.length, true, maxWidth, null)
            val line = text.substring(start, start + count)
            if (y + lineHeight > pageInfo.pageHeight - marginBottom) {
                newPage()
            }
            canvas.drawText(line, marginLeft, y, paint)
            y += lineHeight
            start += count
        }
    }

    val titlePaint = Paint(paint).apply {
        textSize = 16f
        isFakeBoldText = true
    }
    val title = "Ficha de ${personagem.nome}"

    val titleFm = titlePaint.fontMetrics
    val titleHeight = titleFm.descent - titleFm.ascent + titleFm.leading
    if (y + titleHeight > pageInfo.pageHeight - marginBottom) {
        newPage()
    }
    canvas.drawText(title, marginLeft, y, titlePaint)
    y += titleHeight + 12f

    val lines = buildSummaryLines(personagem)
    for (linha in lines) drawWrapped(linha)

    doc.finishPage(page)
    FileOutputStream(destino).use { out -> doc.writeTo(out) }
    doc.close()
}


