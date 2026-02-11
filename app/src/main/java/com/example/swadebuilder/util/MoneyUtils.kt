package com.example.swadebuilder.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

object MoneyUtils {
    private val pathfinderCostRegex = Regex("""^([\d.,]+)\s*([A-Za-z]{2})?$""")

    private fun parseLocalizedNumber(raw: String): Double? {
        val value = raw.trim().replace(" ", "")
        if (value.isBlank()) return null

        val commaCount = value.count { it == ',' }
        val dotCount = value.count { it == '.' }

        val normalized = when {
            commaCount > 0 && dotCount > 0 -> {
                val decimalSeparator = if (value.lastIndexOf(',') > value.lastIndexOf('.')) ',' else '.'
                val thousandsSeparator = if (decimalSeparator == ',') '.' else ','
                value
                    .replace(thousandsSeparator.toString(), "")
                    .replace(decimalSeparator, '.')
            }

            commaCount > 0 || dotCount > 0 -> {
                val separator = if (commaCount > 0) ',' else '.'
                val separatorCount = if (separator == ',') commaCount else dotCount

                if (separatorCount > 1) {
                    value.replace(separator.toString(), "")
                } else {
                    val index = value.indexOf(separator)
                    val decimalDigits = value.length - index - 1
                    val isThousands = decimalDigits == 3 && index > 0

                    if (isThousands) value.replace(separator.toString(), "")
                    else value.replace(separator, '.')
                }
            }

            else -> value
        }

        return normalized.toDoubleOrNull()
    }

    // Helper to parse costs into a single integer base unit.
    // For Pathfinder (Buscatrilha), this is copper pieces (pc).
    // For standard SWADE, this is dollars.
    fun parseCostInBaseUnit(
        costJson: JsonElement?,
        isPathfinder: Boolean
    ): Int {
        if (costJson == null) return 0
        val content = (costJson as? JsonPrimitive)?.content?.trim() ?: return 0
        if (content == "-") return 0

        if (isPathfinder) {
            val match = pathfinderCostRegex.matchEntire(content) ?: return 0
            val value = parseLocalizedNumber(match.groupValues[1]) ?: return 0
            val unit = match.groupValues.getOrNull(2)?.lowercase().orEmpty()

            return when (unit) {
                "pl" -> (value * 1000).roundToInt()
                "po", "" -> (value * 100).roundToInt()
                "pp" -> (value * 10).roundToInt()
                "pc" -> value.roundToInt()
                else -> (value * 100).roundToInt() // fallback: ouro
            }
        } else {
            // Standard system just uses integers usually, but let's support decimal just in case
            return parseLocalizedNumber(content)?.roundToInt() ?: 0
        }
    }

    fun formatCurrency(amount: Int, isPathfinder: Boolean): String {
        if (isPathfinder) {
            var remaining = amount
            val pl = remaining / 1000
            remaining %= 1000
            val po = remaining / 100
            remaining %= 100
            val pp = remaining / 10
            val pc = remaining % 10

            // Exibe todos os campos para dar a sensação de "espaços de moedas" que se atualizam
            // pl = Platina, po = Ouro, pp = Prata, pc = Cobre
            return "$pl pl  $po po  $pp pp  $pc pc"
        } else {
            return "$ $amount"
        }
    }
}
