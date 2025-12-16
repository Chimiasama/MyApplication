package com.example.swadebuilder.ui.sections

import com.example.swadebuilder.model.EquipamentoItem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

internal data class EquipamentoResumo(
    val linhaArma: String?,
    val linhaGeral: String?,
    val linhaVeiculo: String?,
    val observacao: String?,
    val custo: String?
)

internal fun JsonElement?.asText(): String? = when (this) {
    is JsonPrimitive -> this.content
    else -> this?.toString()
}?.takeIf { it.isNotBlank() }

internal fun EquipamentoItem.toResumo(): EquipamentoResumo {
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
            malfuncionamento.asText()?.takeIf { it.isNotBlank() }?.let { malf ->
                if (isNotEmpty()) append("\n")
                append("Malfuncionamento: $malf")
            }
            pmf.asText()?.takeIf { it.isNotBlank() }?.let { pmfValue ->
                if (isNotEmpty()) append("\n")
                append("PMF: $pmfValue")
            }
        }.ifBlank { null },
        custo = custo.asText(),
    )
}

private val EquipamentoItem.passageiros
    get() = this.tripulacao

private val EquipamentoItem.blindagem
    get() = this.resistencia
