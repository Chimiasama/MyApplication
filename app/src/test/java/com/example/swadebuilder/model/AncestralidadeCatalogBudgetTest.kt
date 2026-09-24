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
     *
     * "Humano (Império San)" entrou aqui na migração do Signo de Nascença
     * (14 opções, ver docs/auditoria_mecanica_racas_2026-08-31.md,
     * rodada 33): a raça base no catálogo só carrega o traço-marcador
     * "SIGNOS_DE_NASCENCA" (0 pts, card de referência dos 13 Signos) — o
     * orçamento de 3 pontos só fecha depois que
     * `AncestryVariantRegistry.humanoArteDaGuerraSignos()` resolve QUAL
     * Signo está ativo (ver `ValidateAncestryOptionBudgetsUseCaseTest`,
     * que já confere as 14 opções isoladamente contra esse mesmo
     * orçamento — este teste aqui só varre o catálogo estático, sem
     * resolver Seleção nenhuma).
     */
    private val racasComTracosInjetadosDinamicamente = setOf(
        "FERAIS" to "SCI_FI",
        "FLORANS" to "SCI_FI",
        "GELATINOIDES" to "SCI_FI",
        "INSETOIDES" to "SCI_FI",
        "MÍMICOS" to "SCI_FI",
        "UMVEE (FILHOS DA LUA)" to "ARTE_DA_GUERRA",
        "HUMANO (IMPÉRIO SAN)" to "ARTE_DA_GUERRA"
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
    fun `bonus de atributo do catalogo usa traitId e alvo estruturados`() {
        val racas: JsonArray = Json.parseToJsonElement(catalogFile().readText()).jsonArray
        val boosts = racas.flatMap { ancestry ->
            (ancestry.jsonObject["habilidades"] as? JsonArray).orEmpty().map { it.jsonObject }
        }.filter { it.strOrNull("traitId") == "ATTRIBUTE_BOOST" }

        assertTrue("Catálogo não contém bônus de atributo estruturados", boosts.isNotEmpty())
        val targetsByCleanId = mapOf("RESISTENTE" to "Vigor", "FORTE" to "Força")
        targetsByCleanId.forEach { (cleanId, expectedTarget) ->
            val ability = boosts.firstOrNull { it.strOrNull("id") == cleanId || it.strOrNull("id") == "anc_anoes_robusto" }
            assertTrue("Não encontrei $cleanId migrado para ATTRIBUTE_BOOST", ability != null)
            assertTrue(
                "$cleanId deveria apontar para $expectedTarget, mas foi $ability",
                ability?.strOrNull("targetRef") == expectedTarget
            )
        }
        boosts.forEach { ability ->
            val target = ability.strOrNull("targetRef")
            assertTrue(
                "ATTRIBUTE_BOOST sem atributo alvo: $ability",
                target in setOf("Agilidade", "Astúcia", "Espírito", "Força", "Vigor")
            )
        }
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

    /**
     * Regressão de um bug real relatado pelo usuário (testado no app de verdade, build da CI):
     * com só o compêndio Básico ativo, `RacialTraitAuditFormatter.calcularIdsExclusivos` era
     * chamada em `AncestralidadesSection.kt` com `state.listaAncestralidadesJson` — que só tem
     * as raças dos livros ligados NA SESSÃO ATUAL (ver `DataLoader.ancestryVisibleOrigins`),
     * não o catálogo inteiro. Androides "Construto" (`id=CONSTRUTO`) aparecia marcado
     * "exclusivo-desta-raça" no modo auditoria, porque Golens (Fantasia, também usa
     * `CONSTRUTO`, mesmo efeito mecânico) não estava carregado com só o Básico ativo — a
     * função em si estava certa (não conta como exclusivo quando 2+ raças usam o id), só
     * recebia um catálogo incompleto. O fix real é de wiring (a UI passa a carregar
     * `ancestralidades.json` bruto, sem filtro de compêndio, só pra esta pergunta) — este
     * teste não cobre o wiring (fora do alcance de um teste puro-JVM sem Context/Compose),
     * só a premissa de dados que o fix depende: confirma que os dois ids realmente são
     * compartilhados no catálogo oficial, então NUNCA deveriam aparecer como exclusivos
     * quando `calcularIdsExclusivos` recebe o catálogo completo — e trava caso algum dia
     * alguém desalinhe os dois ids sem querer (o que reverteria o comportamento certo do
     * fix sem nenhum teste acusar).
     */
    @Test
    fun `Construto de Androides e Golens compartilham o mesmo id, entao nunca deveria aparecer como exclusivo`() {
        val texto = catalogFile().readText()
        val racasJson: JsonArray = Json.parseToJsonElement(texto).jsonArray

        val racas = racasJson.map { it.jsonObject }.map { obj ->
            val habilidades = (obj["habilidades"] as? JsonArray)?.map { hElem ->
                val h = hElem.jsonObject
                RacialAbility(
                    nome = h.strOrNull("nome") ?: "",
                    descricao = h.strOrNull("descricao") ?: "",
                    id = h.strOrNull("id")
                )
            } ?: emptyList()
            RacialModifier(nome = obj.strOrNull("nome") ?: "", habilidades = habilidades)
        }

        val racasComConstruto = racas.filter { r -> r.habilidades.any { it.id == "CONSTRUTO" } }
            .map { it.nome }
            .distinct()
        assertTrue(
            "Esperava Androides E Golens com id=CONSTRUTO no catálogo real — achado: $racasComConstruto " +
                "(se só 1 raça usa esse id agora, o cenário deste teste mudou; confira se o fix de " +
                "AncestralidadesSection.kt ainda é necessário)",
            racasComConstruto.size >= 2
        )

        val exclusivos = RacialTraitAuditFormatter.calcularIdsExclusivos(racas)
        assertTrue(
            "CONSTRUTO apareceu como exclusivo (${exclusivos["CONSTRUTO"]}) mesmo compartilhado " +
                "entre $racasComConstruto — calcularIdsExclusivos só deve marcar um id como " +
                "exclusivo quando exatamente 1 raça o usa no catálogo completo",
            exclusivos["CONSTRUTO"] == null
        )
    }
}
