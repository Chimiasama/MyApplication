package com.example.swadebuilder.ui.sections

import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.util.ForcaMinimaCalculator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

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

// Casa um número (com vírgula ou ponto decimal) seguido de uma unidade opcional em texto
// ("300", "10.5", "5 po", "50 pc"...) — é o formato dos campos `peso`/`custo` do catálogo
// quando são um valor puro (não cobre textos livres tipo "Variável", "x2", "-5K * Tam",
// que representam multiplicadores/preços especiais, não o preço direto de um item).
private val NUMERO_COM_UNIDADE_REGEX = Regex("""^([\d.,]+)\s*([A-Za-zÀ-ÿ]*)$""")

private fun Double.paraTextoCompacto(): String {
    val arredondado = (this * 100).roundToInt() / 100.0
    return if (arredondado == arredondado.toLong().toDouble()) {
        arredondado.toLong().toString()
    } else {
        arredondado.toString().trimEnd('0').trimEnd('.')
    }
}

/**
 * Aplica o desconto de peso do traço Diminuto (livro Fantasia, pág. 10 — "equipamentos
 * feitos para personagens Pequenas/Muito Pequenas/Minúsculas pesam... metade/um quarto/
 * um décimo do valor listado") ao texto de `peso`, preservando a unidade original. Sem
 * Diminuto (`passosDiminuto` 0) ou texto não numérico (peso "Variável" etc.), devolve o
 * texto original sem mexer.
 */
fun pesoTextoComDiminuto(peso: JsonElement?, passosDiminuto: Int): String? {
    val raw = peso.asText() ?: return null
    if (passosDiminuto <= 0) return raw
    val match = NUMERO_COM_UNIDADE_REGEX.matchEntire(raw.trim()) ?: return raw
    val valorOriginal = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return raw
    val unidade = match.groupValues[2]
    val divisor = ForcaMinimaCalculator.divisorEquipamentoDiminuto(passosDiminuto)
    val valorTexto = (valorOriginal / divisor).paraTextoCompacto()
    return if (unidade.isBlank()) valorTexto else "$valorTexto $unidade"
}

/**
 * Mesma redução que `pesoTextoComDiminuto()`, só que pro texto de `custo` — mantém a
 * unidade monetária original (ex.: "po"/"pp"/"pl" do Compêndio do Buscatrilha, ou nenhuma
 * unidade no sistema padrão). Não usa `MoneyUtils`/`ForcaMinimaCalculator
 * .custoInteiroReduzidoPorDiminuto()` (esses arredondam pra inteiro numa unidade-base
 * discreta pra fins de saldo/orçamento) porque aqui é só texto de exibição — o valor
 * final pode ter casas decimais (ex.: "5 po" com Minúsculo vira "0.5 po").
 */
fun custoTextoComDiminuto(custo: JsonElement?, passosDiminuto: Int): String? {
    val raw = custo.asText() ?: return null
    if (passosDiminuto <= 0) return raw
    val match = NUMERO_COM_UNIDADE_REGEX.matchEntire(raw.trim()) ?: return raw
    val valorOriginal = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return raw
    val unidade = match.groupValues[2]
    val divisor = ForcaMinimaCalculator.divisorEquipamentoDiminuto(passosDiminuto)
    val valorTexto = (valorOriginal / divisor).paraTextoCompacto()
    return if (unidade.isBlank()) valorTexto else "$valorTexto $unidade"
}

fun EquipamentoItem.toResumo(passosDiminuto: Int = 0): EquipamentoResumo {
    val linhaArma = listOfNotNull(
        dano.asText()?.let { "Dano: $it" },
        pa.asText()?.let { "PA: $it" },
        cdt.asText()?.let { "CdT: $it" },
        distancia.asText()?.let { "Distância: $it" },
        tiros.asText()?.let { "Tiros: $it" },
        pmf.asText()?.let { "PMF: $it" },
        explosao.asText()?.let { "Área de Efeito: $it" },
    ).joinToString("  •  ")
        .takeIf { it.isNotBlank() }

    val linhaGeral = listOfNotNull(
        pesoTextoComDiminuto(peso, passosDiminuto)?.let { "Peso: $it" },
        forcaMin.asText()?.let { "Força mín.: $it" },
        armadura.asText()?.let { "Armadura: $it" },
        aparar.asText()?.let { "Aparar: $it" },
        cobertura.asText()?.let { "Cobertura: $it" },
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
        custo = custoTextoComDiminuto(custo, passosDiminuto),
    )
}

private val EquipamentoItem.passageiros
    get() = this.tripulacao

private val EquipamentoItem.blindagem
    get() = this.resistencia
