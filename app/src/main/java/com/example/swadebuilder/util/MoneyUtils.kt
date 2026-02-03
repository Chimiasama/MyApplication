package com.example.swadebuilder.util

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

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
                // Remove thousand separators before parsing
                val value = parts[0].replace(".", "").toIntOrNull() ?: return 0
                if (parts.size > 1) {
                    return when (parts[1].lowercase()) {
                        "pl" -> value * 1000
                        "po" -> value * 100
                        "pp" -> value * 10
                        "pc" -> value
                        else -> value * 100 // Assume gold (po -> pc) if unit is weird
                    }
                }
                return value * 100 // Assume gold (po -> pc) if no unit
            }
            return 0
        } else {
            // Standard system just uses integers
            return content.toIntOrNull() ?: 0
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
