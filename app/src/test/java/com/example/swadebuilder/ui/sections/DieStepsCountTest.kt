package com.example.swadebuilder.ui.sections

import org.junit.Assert.assertEquals
import org.junit.Test

class DieStepsCountTest {

    @Test
    fun `conta passos normais abaixo de d12`() {
        assertEquals(0, dieStepsCount(4, 4))
        assertEquals(1, dieStepsCount(4, 6))
        assertEquals(4, dieStepsCount(4, 12))
    }

    @Test
    fun `conta passos de um em um acima de d12, nao pulando valores`() {
        // Bug real corrigido: `when (curr) { 12 -> 13; else -> curr + 2 }` só
        // tratava a transição exata 12->13; qualquer valor JÁ acima de 12
        // (13, 14, 15...) caía no `else` e voltava a somar 2, pulando um
        // passo a cada vez (13->15->17 em vez de 13->14->15->16->17) e
        // subcontando o total sempre que o intervalo cruzasse 2+ passos
        // acima de d12.
        assertEquals(1, dieStepsCount(12, 13))
        assertEquals(2, dieStepsCount(12, 14))
        assertEquals(3, dieStepsCount(12, 15))
        assertEquals(9, dieStepsCount(4, 17))  // d4 -> d12+5 (Meio-Gigante): 4 passos até d12 + 5 acima
        assertEquals(7, dieStepsCount(8, 17))  // d8 -> d12+5: bug relatado pelo usuário (retornava 5)
    }

    @Test
    fun `simetrico independente da ordem dos argumentos`() {
        assertEquals(dieStepsCount(8, 17), dieStepsCount(17, 8))
    }

    @Test
    fun `custo acumulado de pericia dobra acima do atributo e nao pula passos acima de d12`() {
        // Atributo em d12+2 (14): custo normal (1pt) até chegar no atributo,
        // dobrado (2pts) dali em diante.
        assertEquals(4, calcularCustoAcumuladoPericia(startRaw = 8, attrRaw = 14, targetRaw = 14)) // 8->10->12->13->14: 4 passos x1
        assertEquals(6, calcularCustoAcumuladoPericia(startRaw = 8, attrRaw = 14, targetRaw = 15)) // + 1 passo x2 acima do atributo
    }
}
