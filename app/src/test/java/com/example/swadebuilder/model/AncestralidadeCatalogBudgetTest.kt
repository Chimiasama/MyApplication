package com.example.swadebuilder.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Varredura sobre TODAS as raças oficiais do catálogo real (ancestralidades.json), em vez de
 * um cenário roteirizado só: para cada raça, soma o custo em pontos de cada traço de
 * `habilidades[]` (mesma fórmula de `RacialAbility.resolvedPontos()` — `RacialTraitPointCatalog
 * .custoDe(traitId ?: id, value, severity, pontos) * vezes`) e confere que bate com o orçamento
 * dela mesma (`pontosRaciaisEsperados` — 2 pra maioria, mas já calibrado por raça: 3 pra Arte da
 * Guerra, 4 pra Pathfinder/Crystal Heart/Centauros, etc.).
 *
 * Esse é exatamente o tipo de checagem que as ~17 rodadas manuais desta auditoria (ver
 * docs/auditoria_mecanica_racas_2026-08-31.md) vinham fazendo à mão, raça por raça: os bugs de
 * Centauros, Draconianos, Goblins, Infernais, Povo Ratazana/Rakashanos etc. eram todos um id de
 * traço errado ou duplicado fazendo o total fechar errado — exatamente o que este teste pega
 * sozinho, pra qualquer raça, sem precisar de outra rodada de auditoria manual.
 *
 * Lê o JSON direto do disco (não passa por `DataLoader`/`Context` — o gate de confiabilidade,
 * scripts/phase6_reliability_gate.sh, exige que todo uso de DataLoader passe pelo repositório
 * em GameDataRepository.kt, e testes de JVM pura não têm Context do Android pra isso de
 * qualquer forma). Não precisa de `RacialModifier`/`RacialAbility` inteiros — só dos mesmos
 * campos que `RacialTraitPointCatalog.custoDe()` já usa.
 */
class AncestralidadeCatalogBudgetTest {

    /**
     * Raças cujo total de pontos só fecha depois da injeção dinâmica de traço feita por
     * `AncestryVariantRegistry` (a "variante padrão" ativa por default some um traço que não
     * mora em `habilidades[]` no catálogo estático) — ex.: Ferais (Sci-Fi) ganham Diminuto
     * (Tamanho -3) só pela Variante, não pelo JSON puro (ver comentário em
     * RacialTraitPointCatalog.kt, "DIMINUTO_TAMANHO_3"). Somar só o catálogo estático pra essas
     * dá um total menor do que o efetivo em jogo — não é bug, é limite de um teste que não
     * resolve Variantes (isso exigiria rodar CriadorState com Context real via Robolectric, fora
     * do escopo deste teste). Identificado batendo cada raça aqui contra
     * `AncestryVariantRegistry.kt` (toda raça com `ancestralidadeId` cadastrado lá que também
     * aparecia como mismatch nesta varredura).
     */
    private val racasComTracosInjetadosDinamicamente = setOf(
        "FERAIS" to "SCI_FI",
        "FLORANS" to "SCI_FI",
        "GELATINOIDES" to "SCI_FI",
        "INSETOIDES" to "SCI_FI",
        "MÍMICOS" to "SCI_FI",
        "UMVEE (FILHOS DA LUA)" to "ARTE_DA_GUERRA"
    )

    private fun catalogFile(): File {
        val candidatos = listOf(
            File("src/main/assets/ancestralidades.json"),
            File("app/src/main/assets/ancestralidades.json")
        )
        return candidatos.firstOrNull { it.isFile }
            ?: throw IllegalStateException(
                "Não encontrei ancestralidades.json em nenhum dos caminhos candidatos: " +
                    candidatos.joinToString { it.path }
            )
    }

    private fun JsonObject.strOrNull(key: String): String? {
        val v = this[key] ?: return null
        if (v is JsonNull) return null
        return v.jsonPrimitive.content
    }

    private fun JsonObject.intOrDefault(key: String, default: Int): Int {
        val v = this[key] ?: return default
        if (v is JsonNull) return default
        return v.jsonPrimitive.intOrNull ?: default
    }

    @Test
    fun `toda raca oficial fecha no orcamento de pontos raciais dela mesma`() {
        val texto = catalogFile().readText()
        val racas: JsonArray = Json.parseToJsonElement(texto).jsonArray
        assertTrue(
            "Catálogo carregado com poucas raças (${racas.size}) — path errado?",
            racas.size > 80
        )

        val falhas = mutableListOf<String>()

        for (elem: JsonElement in racas) {
            val obj = elem.jsonObject
            val nome = obj.strOrNull("nome") ?: "(sem nome)"
            val livros = (obj["livros"] as? JsonArray)?.map { it.jsonPrimitive.content } ?: emptyList()
            val esperado = obj.intOrDefault("pontosRaciaisEsperados", 2)
            val habilidades = (obj["habilidades"] as? JsonArray) ?: JsonArray(emptyList())

            val ehDinamica = livros.any { livro -> (nome.uppercase() to livro) in racasComTracosInjetadosDinamicamente }
            if (ehDinamica) continue

            var soma = 0
            for (hElem in habilidades) {
                val h = hElem.jsonObject
                val id = h.strOrNull("id")
                val traitId = h.strOrNull("traitId")
                val severity = h.strOrNull("severity")
                val value = h.intOrDefault("value", 1)
                val pontos = h.intOrDefault("pontos", 0)
                val vezes = h.intOrDefault("vezes", 1)

                val resolvedTraitId = traitId ?: id ?: ""
                soma += RacialTraitPointCatalog.custoDe(resolvedTraitId, value, severity, pontos) * vezes
            }

            if (soma != esperado) {
                falhas += "$nome (livros=$livros): soma=$soma, esperado=$esperado"
            }
        }

        assertTrue(
            "Raças com pontos raciais fora do orçamento delas mesmas (id de traço errado/" +
                "faltando, ou pontosRaciaisEsperados desatualizado):\n" +
                falhas.joinToString("\n"),
            falhas.isEmpty()
        )
    }
}
