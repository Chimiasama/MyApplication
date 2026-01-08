package com.example.swadebuilder.ui.sections

import com.example.swadebuilder.model.EquipamentoItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

data class EquipamentoResumo(
    val linhaArma: String?,
    val linhaGeral: String?,
    val linhaVeiculo: String?,
    val observacao: String?,
    val custo: String?
)

fun JsonElement?.asText(): String? = when (this) {
    is JsonPrimitive -> this.content
    else -> this?.toString()
}?.takeIf { it.isNotBlank() }

fun EquipamentoItem.toResumo(): EquipamentoResumo {
    val linhaArma = listOfNotNull(
        dano.asText()?.let { "Dano: $it" },
        pa.asText()?.let { "PA: $it" },
        cdt.asText()?.let { "CdT: $it" },
        distancia.asText()?.let { "Distância: $it" },
        tiros.asText()?.let { "Tiros: $it" },
        pmf.asText()?.let { "PMF: $it" },
    ).joinToString("  •  ")
        .takeIf { it.isNotBlank() }

    val linhaGeral = listOfNotNull(
        peso.asText()?.let { "Peso: $it" },
        forcaMin.asText()?.let { "Força mín.: $it" },
        armadura.asText()?.let { "Armadura: $it" },
        aparar.asText()?.let { "Aparar: $it" },
    ).joinToString("  •  ")
        .takeIf { it.isNotBlank() }

    val linhaVeiculo = listOfNotNull(
        velMaxima.asText()?.let { "Vel. máx.: $it" },
        manobrabilidade.asText()?.let { "Manobrabilidade: $it" },
        tamanho.asText()?.let { "Tamanho: $it" },
        resistencia.asText()?.let { "Resistência: $it" },
        tripulacao.asText()?.let { "Tripulação: $it" },
        blindagem.asText()?.let { "Blindagem: $it" },
        passageiros.asText()?.let { "Passageiros: $it" },
    ).joinToString("  •  ")
        .takeIf { it.isNotBlank() }

    return EquipamentoResumo(
        linhaArma = linhaArma,
        linhaGeral = linhaGeral,
        linhaVeiculo = linhaVeiculo,
        observacao = buildString {
            observacoes.asText()?.takeIf { it.isNotBlank() }?.let { append(it) }
            pmf.asText()?.takeIf { it.isNotBlank() }?.let { pmfValor ->
                if (isNotEmpty()) append("\n")
                append("PMF: $pmfValor")
            }
            malfuncionamento.asText()?.takeIf { it.isNotBlank() }?.let { malf ->
                if (isNotEmpty()) append("\n")
                append("Malfuncionamento: $malf")
            }
        }.ifBlank { null },
        custo = custo.asText(),
    )
}

private val EquipamentoItem.passageiros
    get() = this.tripulacao

private val EquipamentoItem.blindagem
    get() = this.resistencia

fun formatCurrency(amount: Int, isPathfinder: Boolean): String {
    if (isPathfinder) {
        if (amount == 0) return "0 pc"
        var remaining = amount
        val pl = remaining / 1000
        remaining %= 1000
        val po = remaining / 100
        remaining %= 100
        val pp = remaining / 10
        val pc = remaining % 10

        return listOfNotNull(
            if (pl > 0) "$pl pl" else null,
            if (po > 0) "$po po" else null,
            if (pp > 0) "$pp pp" else null,
            if (pc > 0) "$pc pc" else null
        ).joinToString(", ").ifEmpty { "0 pc" }
    } else {
        return "$ $amount"
    }
}
