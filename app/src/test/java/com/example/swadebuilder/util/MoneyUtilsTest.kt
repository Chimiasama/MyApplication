package com.example.swadebuilder.util

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilsTest {

    @Test
    fun parseCostInBaseUnit_nonPathfinder_parsesDecimalWithDot() {
        val amount = MoneyUtils.parseCostInBaseUnit(JsonPrimitive("1.5"), isPathfinder = false)

        assertEquals(2, amount)
    }

    @Test
    fun parseCostInBaseUnit_nonPathfinder_parsesThousandSeparators() {
        val amount = MoneyUtils.parseCostInBaseUnit(JsonPrimitive("1.000"), isPathfinder = false)

        assertEquals(1000, amount)
    }

    @Test
    fun parseCostInBaseUnit_pathfinder_parsesWithoutWhitespaceBeforeUnit() {
        val amount = MoneyUtils.parseCostInBaseUnit(JsonPrimitive("2,5po"), isPathfinder = true)

        assertEquals(250, amount)
    }

    @Test
    fun parseCostInBaseUnit_pathfinder_parsesInternationalFormat() {
        val amount = MoneyUtils.parseCostInBaseUnit(JsonPrimitive("1,234.56 po"), isPathfinder = true)

        assertEquals(123456, amount)
    }
}
