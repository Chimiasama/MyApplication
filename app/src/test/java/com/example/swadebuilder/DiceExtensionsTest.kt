package com.example.swadebuilder

import org.junit.Assert.assertEquals
import org.junit.Test

class DiceExtensionsTest {

    @Test
    fun `formata valores de dado em notação swade`() {
        assertEquals("-", 0.toDiceString())
        assertEquals("d4", 4.toDiceString())
        assertEquals("d12", 12.toDiceString())
        assertEquals("d12+1", 13.toDiceString())
    }
}
