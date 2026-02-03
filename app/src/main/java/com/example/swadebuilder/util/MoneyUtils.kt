package com.example.swadebuilder.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt

object MoneyUtils {
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
            val parts = content.split(" ")
            if (parts.isNotEmpty()) {
                // Handle "5,5 po" -> "5.5"
                // Replace dot thousands with nothing? Actually standard format for currency might use dots for thousands.
                // But for decimals, comma is common in BR. "5.500" vs "5,5".
                // If we assume standard "5.5" or "5,5" is decimal...
                // Safest is to replace comma with dot for parsing.
                // But if dot is thousand separator... "1.000" -> 1000.
                // Simple heuristic: remove dots (thousands), replace comma with dot (decimal).

                val numberString = parts[0].replace(".", "").replace(",", ".")
                val value = numberString.toDoubleOrNull() ?: return 0

                if (parts.size > 1) {
                    return when (parts[1].lowercase()) {
                        "pl" -> (value * 1000).roundToInt()
                        "po" -> (value * 100).roundToInt()
                        "pp" -> (value * 10).roundToInt()
                        "pc" -> value.roundToInt()
                        else -> (value * 100).roundToInt() // Assume gold (po -> pc) if unit is weird
                    }
                }
                return (value * 100).roundToInt() // Assume gold (po -> pc) if no unit
            }
            return 0
        } else {
            // Standard system just uses integers usually, but let's support decimal just in case
            val numberString = content.replace(".", "").replace(",", ".")
            return numberString.toDoubleOrNull()?.roundToInt() ?: 0
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
