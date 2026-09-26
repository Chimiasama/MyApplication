package com.example.swadebuilder.model

import kotlinx.serialization.json.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * "Modo Auditoria: ID de traço" (ver AppPreferences.loadModoAuditoriaIdPuro,
 * AncestralidadesSection.kt "Ver detalhes"): o formatador precisa ignorar `nome`/`descricao`
 * reskinados de uma `RacialAbility` e mostrar só o id/traitId contra a definição oficial do
 * catálogo (`basico_habilidades_raciais.json`), pra quem audita conseguir separar "traço com
 * id de verdade" de "sujeira de hardcode".
 */
class RacialTraitAuditFormatterTest {

    private val catalogoOficial = listOf(
        HabilidadeCriacao(
            nome = "Arma de Sopro",
            custo = 2,
            descricao = "Pode cuspir fogo, frio, ácido ou outra energia (Modelo de Cone, rolagem de Atletismo, causa 2d6 de dano, 3d6 com ampliação).",
            id = "arma_de_sopro"
        )
    )

    @Test
    fun `id com entrada no catalogo oficial usa a descricao oficial, nunca o nome customizado`() {
        val hab = RacialAbility(
            nome = "Bafo Flamejante", // nome "reskinado" — não deve aparecer na definição
            descricao = "descrição de skin qualquer",
            id = "ARMA_DE_SOPRO"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertEquals(1, linhas.size)
        assertTrue(linhas[0].contains("[id=ARMA_DE_SOPRO]"))
        assertTrue(linhas[0].contains("Arma de Sopro: Pode cuspir fogo"))
        assertTrue(!linhas[0].contains("Bafo Flamejante"))
    }

    @Test
    fun `id oficial de catalogo mostra o rotulo e marca Catalogo Oficial`() {
        val hab = RacialAbility(nome = "qualquer skin", descricao = "", id = "ARMADURA")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("[id=ARMADURA]"))
        assertTrue(linhas[0].contains("[Catálogo Oficial] Armadura +2"))
    }

    @Test
    fun `GRANTED_EDGE mostra o targetRef como Vantagem Gratis, nao tenta achar id de traco`() {
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            traitId = "GRANTED_EDGE",
            targetRef = "Carismático",
            category = "racial_edge"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("Vantagem Grátis concedida ao personagem: Carismático"))
    }

    @Test
    fun `id totalmente sem catalogo nem efeito vira aviso de regra unica fora do catalogo`() {
        val hab = RacialAbility(nome = "Nome Qualquer", descricao = "", id = "ID_INEXISTENTE_QUALQUER")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("Regra Única da Raça / Fora do Catálogo"))
        assertTrue(linhas[0].contains("SEM CATÁLOGO"))
    }

    @Test
    fun `id sem catalogo oficial e sem LABEL, mas com custo calibrado em CUSTOS, e marcado como Regra Unica da Raca`() {
        val hab = RacialAbility(nome = "Carismático (racial)", descricao = "", id = "CARISMATICO")
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("Regra Única da Raça / Fora do Catálogo"))
        assertTrue(linhas[0].contains("Traço específico desta raça"))
        assertTrue(linhas[0].contains("+2 pts"))
    }

    @Test
    fun `traco empilhavel mostra x vezes e pontos multiplicados`() {
        val hab = RacialAbility(nome = "Tamanho", descricao = "", id = "TAMANHO_MAIS_1", vezes = 3)
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("x3"))
        assertTrue(linhas[0].contains("+3 pts"))
    }

    @Test
    fun `id exclusivo de uma raca ganha a etiqueta no cabecalho`() {
        val hab = RacialAbility(nome = "Magia Gnômica", descricao = "", id = "MAGIA_GNOMICA")
        val semExclusividade = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(!semExclusividade[0].contains("exclusivo-desta-raça"))

        val comExclusividade = RacialTraitAuditFormatter.formatar(
            listOf(hab), catalogoOficial, mapOf("MAGIA_GNOMICA" to "Gnomo")
        )
        assertTrue(comExclusividade[0].contains("exclusivo-desta-raça"))
    }

    @Test
    fun `traco migrado pro par generico ATTRIBUTE_BOOST ainda acha o LABEL do id original`() {
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            id = "ARMADURA",
            traitId = "ATTRIBUTE_BOOST",
            targetRef = "VIGOR"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial)
        assertTrue(linhas[0].contains("[id=ATTRIBUTE_BOOST"))
        assertTrue(linhas[0].contains("Armadura +2"))
    }

    @Test
    fun `GRANTED_EDGE calcula o custo dinamicamente pelo Estagio da Vantagem real, nao fixo em 2`() {
        val vantagemExperiente = Vantagem(
            id = "vantagem_experiente_teste",
            nome = "Vantagem Experiente Teste",
            categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Experiente")
        )
        val hab = RacialAbility(
            nome = "skin qualquer",
            descricao = "",
            traitId = "GRANTED_EDGE",
            targetRef = "Vantagem Experiente Teste",
            category = "racial_edge"
        )
        val linhas = RacialTraitAuditFormatter.formatar(listOf(hab), catalogoOficial, allVantagens = listOf(vantagemExperiente))
        assertTrue(linhas[0].contains("+3 pts"))
    }

    @Test
    fun `calcularIdsExclusivos so marca ids usados por exatamente uma raca`() {
        val racaA = RacialModifier(
            nome = "Raça A",
            habilidades = listOf(
                RacialAbility(nome = "Traço Comum", descricao = "", id = "COMUM"),
                RacialAbility(nome = "Traço Só de A", descricao = "", id = "SO_A")
            )
        )
        val racaB = RacialModifier(
            nome = "Raça B",
            habilidades = listOf(RacialAbility(nome = "Traço Comum", descricao = "", id = "COMUM"))
        )
        val exclusivos = RacialTraitAuditFormatter.calcularIdsExclusivos(listOf(racaA, racaB))
        assertEquals(mapOf("SO_A" to "Raça A"), exclusivos)
    }

    @Test
    fun `racial_hindrance e racial_edge nunca contam pra exclusividade, mesmo sendo a unica raca com aquele id`() {
        val androides = RacialModifier(
            nome = "Androides",
            habilidades = listOf(
                RacialAbility(nome = "Pacifista", descricao = "", id = "PACIFISTA", category = "racial_hindrance", severity = "Maior"),
                RacialAbility(nome = "Carismático (racial)", descricao = "", id = "CARISMATICO", category = "racial_edge")
            )
        )
        val outraRaca = RacialModifier(
            nome = "Outra Raça",
            habilidades = listOf(RacialAbility(nome = "Traço Qualquer", descricao = "", id = "TRACO_QUALQUER"))
        )

        val exclusivos = RacialTraitAuditFormatter.calcularIdsExclusivos(listOf(androides, outraRaca))

        assertTrue(!exclusivos.containsKey("PACIFISTA"))
        assertTrue(!exclusivos.containsKey("CARISMATICO"))

        val linhas = RacialTraitAuditFormatter.formatar(androides.habilidades, catalogoOficial, exclusivos)
        assertTrue(!linhas[0].contains("exclusivo-desta-raça"))
        assertTrue(!linhas[1].contains("exclusivo-desta-raça"))
    }

    @Test
    fun `varrer todas as racas em ancestralidades json e confirmar que nenhuma habilidade cai em aviso de SEM CATALOGO`() {
        fun findAssetFile(fileName: String): File {
            val candidatos = listOf(File("src/main/assets/$fileName"), File("app/src/main/assets/$fileName"))
            return candidatos.firstOrNull { it.isFile } ?: error("Arquivo $fileName não encontrado")
        }
        val jsonText = findAssetFile("ancestralidades.json").readText()
        val racas: JsonArray = Json.parseToJsonElement(jsonText).jsonArray
        val catalogoText = findAssetFile("basico_habilidades_raciais.json").readText()
        val catalogoArr: JsonArray = Json.parseToJsonElement(catalogoText).jsonArray

        val catalogoOficial = catalogoArr.map { elem ->
            val obj = elem.jsonObject
            HabilidadeCriacao(
                nome = obj["nome"]?.jsonPrimitive?.content ?: "",
                custo = obj["custo"]?.jsonPrimitive?.intOrNull ?: 0,
                descricao = obj["descricao"]?.jsonPrimitive?.content ?: "",
                id = obj["id"]?.jsonPrimitive?.content
            )
        }

        val falhas = mutableListOf<String>()

        racas.forEach { elem ->
            val obj = elem.jsonObject
            val racaNome = obj["nome"]?.jsonPrimitive?.content ?: ""
            val habilidadesArr = (obj["habilidades"] as? JsonArray) ?: JsonArray(emptyList())

            val habilidades = habilidadesArr.map { hElem ->
                val h = hElem.jsonObject
                RacialAbility(
                    nome = h["nome"]?.jsonPrimitive?.content ?: "",
                    descricao = h["descricao"]?.jsonPrimitive?.content ?: "",
                    id = h["id"]?.jsonPrimitive?.content,
                    category = h["category"]?.jsonPrimitive?.content,
                    traitId = h["traitId"]?.jsonPrimitive?.content,
                    targetRef = h["targetRef"]?.jsonPrimitive?.content,
                    value = h["value"]?.jsonPrimitive?.intOrNull ?: 1
                )
            }

            val formatadas = RacialTraitAuditFormatter.formatar(habilidades, catalogoOficial)
            formatadas.forEach { linha ->
                if (linha.contains("SEM CATÁLOGO")) {
                    falhas += "$racaNome: $linha"
                }
            }
        }

        assertTrue("Habilidades de raças sem catálogo/efeito encontradas:\n" + falhas.joinToString("\n"), falhas.isEmpty())
    }
}
