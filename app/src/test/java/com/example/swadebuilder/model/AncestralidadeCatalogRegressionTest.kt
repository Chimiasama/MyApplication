package com.example.swadebuilder.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Um teste de regressão por bug específico já encontrado e corrigido nas rodadas desta
 * auditoria (ver docs/auditoria_mecanica_racas_2026-08-31.md) — trava o fato exato no catálogo
 * real (ancestralidades.json) pra nenhum deles voltar a acontecer silenciosamente. Não
 * substitui AncestralidadeCatalogBudgetTest (que varre TODAS as raças pelo total de pontos);
 * este aqui documenta, um por um, os bugs concretos que já apareceram — id duplicado/errado
 * fazendo duas raças diferentes colidirem no mesmo efeito, ou um valor (PA, custo) que o texto
 * do livro não bate com o que o catálogo tinha.
 */
class AncestralidadeCatalogRegressionTest {

    private fun racas(): List<JsonObject> {
        val candidatos = listOf(
            File("src/main/assets/ancestralidades.json"),
            File("app/src/main/assets/ancestralidades.json")
        )
        val arquivo = candidatos.firstOrNull { it.isFile }
            ?: throw IllegalStateException(
                "Não encontrei ancestralidades.json em nenhum dos caminhos candidatos: " +
                    candidatos.joinToString { it.path }
            )
        val arr: JsonArray = Json.parseToJsonElement(arquivo.readText()).jsonArray
        return arr.map { it.jsonObject }
    }

    private fun JsonObject.livros(): List<String> =
        (this["livros"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()

    private fun racaDoLivro(nome: String, livro: String): JsonObject {
        val encontradas = racas().filter {
            it["nome"]?.jsonPrimitive?.content.equals(nome, ignoreCase = true) && livro in it.livros()
        }
        assertEquals("Esperava exatamente 1 raça '$nome' no livro $livro, achei ${encontradas.size}", 1, encontradas.size)
        return encontradas.first()
    }

    private fun habilidade(raca: JsonObject, id: String): JsonObject {
        val habilidades = (raca["habilidades"] as? JsonArray) ?: JsonArray(emptyList())
        val achada = habilidades.map { it.jsonObject }.firstOrNull { it["id"]?.jsonPrimitive?.content == id }
        assertNotNull(
            "Raça '${raca["nome"]?.jsonPrimitive?.content}' não tem nenhuma habilidade com id=$id",
            achada
        )
        return achada!!
    }

    private fun paDaPrimeiraArmaNatural(habilidade: JsonObject): Int {
        val armas = (habilidade["armasNaturais"] as? JsonArray) ?: JsonArray(emptyList())
        assertTrue("Habilidade sem armasNaturais[]", armas.isNotEmpty())
        val pa = armas[0].jsonObject["pa"]
        assertNotNull("armasNaturais[0] sem campo pa", pa)
        return pa!!.jsonPrimitive.content.toInt()
    }

    @Test
    fun `Draconianos Mal-Humorado concede a Complicacao Arrogante pelo id, nao pelo nome`() {
        val draconianos = racaDoLivro("DRACONIANOS", "FANTASIA")
        val malHumorado = habilidade(draconianos, "ARROGANTE")
        assertEquals("RACIAL_HINDRANCE", malHumorado["traitId"]?.jsonPrimitive?.content)
        assertEquals("Arrogante", malHumorado["targetRef"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Goblins Pequenos usa o id compartilhado TAMANHO_MENOS_1, nao um id proprio orfao`() {
        val goblins = racaDoLivro("GOBLINS", "FANTASIA")
        habilidade(goblins, "TAMANHO_MENOS_1")
    }

    @Test
    fun `Infernais Natureza Diabolica usa o id compartilhado BONUS_PERICIA_1, nao um id proprio orfao`() {
        val infernais = racaDoLivro("INFERNAIS", "FANTASIA")
        habilidade(infernais, "BONUS_PERICIA_1")
    }

    @Test
    fun `Anoes Fantasia Robusto usa id=RESISTENTE, nao colide com o conceito real de Robusto`() {
        val anoesFantasia = racaDoLivro("ANÕES", "FANTASIA")
        val robusto = habilidade(anoesFantasia, "RESISTENTE")
        assertEquals("ROBUSTO", robusto["nome"]?.jsonPrimitive?.content)
    }

    @Test
    fun `Povo Ratazana e Rakashanos do livro Fantasia tem Garras com PA 2, conforme o livro`() {
        val ratazana = racaDoLivro("POVO RATAZANA", "FANTASIA")
        assertEquals(2, paDaPrimeiraArmaNatural(habilidade(ratazana, "GARRAS")))

        val rakashanos = racaDoLivro("RAKASHANOS", "FANTASIA")
        assertEquals(2, paDaPrimeiraArmaNatural(habilidade(rakashanos, "GARRAS")))
    }

    @Test
    fun `Rakashanos de outros livros mantem Garras sem PA (so a versao Fantasia ganhou PA)`() {
        for (livro in listOf("BASICO", "HORROR", "SCI_FI", "SUPER")) {
            val raca = racaDoLivro("RAKASHANOS", livro)
            assertEquals(0, paDaPrimeiraArmaNatural(habilidade(raca, "GARRAS_SEM_PA")))
        }
    }

    @Test
    fun `Povo Ratazana Sucateiro usa o id em maiusculas, consistente com o resto do catalogo`() {
        val ratazana = racaDoLivro("POVO RATAZANA", "FANTASIA")
        habilidade(ratazana, "SUCATEIRO")
    }
}
