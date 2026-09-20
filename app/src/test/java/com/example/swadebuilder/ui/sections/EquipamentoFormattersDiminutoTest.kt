package com.example.swadebuilder.ui.sections

import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Formatação de peso/custo com o desconto do traço Diminuto (livro Fantasia, pág. 10) —
 * texto de exibição da lista de equipamentos e do PDF, preservando a unidade original
 * (ex.: "po" do Compêndio do Buscatrilha) e sem mexer em textos não numéricos
 * ("Variável", "x2" etc., que são multiplicadores/preços especiais, não o preço direto).
 */
class EquipamentoFormattersDiminutoTest {

    @Test
    fun `sem Diminuto devolve o texto original`() {
        assertEquals("300", custoTextoComDiminuto(JsonPrimitive("300"), passosDiminuto = 0))
        assertEquals("4", pesoTextoComDiminuto(JsonPrimitive("4"), passosDiminuto = 0))
    }

    @Test
    fun `Pequeno reduz pela metade`() {
        assertEquals("150", custoTextoComDiminuto(JsonPrimitive("300"), passosDiminuto = 2))
        assertEquals("2", pesoTextoComDiminuto(JsonPrimitive("4"), passosDiminuto = 2))
    }

    @Test
    fun `Minusculo reduz pra um decimo e preserva a unidade pathfinder`() {
        assertEquals("0.5 po", custoTextoComDiminuto(JsonPrimitive("5 po"), passosDiminuto = 4))
    }

    @Test
    fun `texto nao numerico nao e alterado`() {
        assertEquals("Variável", custoTextoComDiminuto(JsonPrimitive("Variável"), passosDiminuto = 4))
        assertEquals("x2", custoTextoComDiminuto(JsonPrimitive("x2"), passosDiminuto = 4))
    }

    @Test
    fun `null continua null`() {
        assertNull(custoTextoComDiminuto(null, passosDiminuto = 4))
        assertNull(pesoTextoComDiminuto(null, passosDiminuto = 4))
    }
}
