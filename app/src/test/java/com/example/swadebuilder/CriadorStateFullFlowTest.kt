package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * "Testes exageradamente perfeitos" — simula, ao nível do CriadorState (o mesmo caminho de
 * código que a UI chama, sem precisar de emulador/Compose UI Test), a sequência de ações de um
 * jogador criando personagem: escolhe raça, distribui pontos de atributo, compra Vantagem e
 * Complicação — e confere que os modificadores esperados realmente foram aplicados, não
 * silenciosamente ignorados (o padrão de bug mais comum encontrado nesta auditoria inteira:
 * id de traço errado/duplicado que faz o efeito nunca disparar).
 *
 * Duas partes:
 *  1) Fluxo sintético (fixture pequena, como os demais testes de CriadorState já existentes) —
 *     raça + atributos + Vantagem + Complicação juntos, uma interação completa.
 *  2) Fluxo com dado REAL do catálogo (ancestralidades.json lido do disco, igual a
 *     AncestralidadeCatalogBudgetTest) — roda ELFOS e HUMANOS de verdade pela resolução real de
 *     CriadorState (getAncestralidadeDef/atributoBaseRacial/ModifierEngine), não só a soma de
 *     pontos: pega bug de id que o teste de soma não pegaria (ex.: id certo mas EFEITOS
 *     mapeado pro atributo errado).
 */
class CriadorStateFullFlowTest {

    private fun snapshotWith(
        racas: List<RacialModifier>,
        vantagens: List<Vantagem> = emptyList(),
        complicacoes: List<Complicacao> = emptyList()
    ): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = complicacoes,
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR").associateWith { it },
        listaPericias = listOf(
            Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true),
            Pericia(nome = "Intimidar", atributo = "ASTUCIA", basica = false)
        ),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = vantagens,
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    // ---------------------------------------------------------------------
    // 1) Fluxo sintético: raça + atributos + Vantagem + Complicação juntos
    // ---------------------------------------------------------------------

    @Test
    fun `fluxo completo - raca neutra, atributo distribuido, Vantagem e Complicacao compradas, tudo reflete no estado final`() {
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), origem = "BASICO")
        val alerta = Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE, requisitos = Requisito(estagio = "Novato"))
        val desagradavel = Complicacao(id = "desagradavel", name = "Desagradável", severity = "Menor", description = "", origem = "BASICO")

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(humanos), listOf(alerta), listOf(desagradavel)))

        // Abre o app "no livro Básico, sem regra extra" == nenhum compendioXAtivo ligado
        // (todos nascem false por padrão em CriadorState) — não precisa setar nada.
        state.ancestralidade = "HUMANOS"

        // Distribui pontos de atributo (mesma forma que a UI escreve o valor final).
        state.valoresAtributos.getValue("VIGOR").intValue = 8
        state.valoresAtributos.getValue("FORCA").intValue = 6

        // Compra Vantagem e Complicação.
        state.vantagensSelecionadas.add(alerta)
        state.complicacoesSelecionadas[desagradavel] = "Menor"

        // Nada disso pode ter sido silenciosamente ignorado.
        assertEquals("HUMANOS", state.ancestralidade)
        assertEquals(8, state.valoresAtributos.getValue("VIGOR").intValue)
        assertEquals(6, state.valoresAtributos.getValue("FORCA").intValue)
        assertTrue("Vantagem comprada não apareceu em vantagensSelecionadas", state.vantagensSelecionadas.contains(alerta))
        assertEquals("Menor", state.complicacoesSelecionadas[desagradavel])

        // Humanos sem nenhum traço extra: o piso de Vigor continua o padrão (d4), a
        // distribuição de pontos é que elevou o valor exibido acima — os dois números são
        // independentes (piso racial vs. valor comprado), e nenhum dos dois pode ter vazado
        // um pro outro.
        assertEquals(4, state.atributoMinRaw("Vigor"))
    }

    // ---------------------------------------------------------------------
    // 2) Fluxo com dado REAL do catálogo — mesma leitura de arquivo que
    //    AncestralidadeCatalogBudgetTest/AncestralidadeCatalogRegressionTest usam.
    // ---------------------------------------------------------------------

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

    private fun JsonObject.boolOrDefault(key: String, default: Boolean): Boolean {
        val v = this[key] ?: return default
        if (v is JsonNull) return default
        return v.jsonPrimitive.content.toBooleanStrictOrNull() ?: default
    }

    /** Reconstrói um RacialAbility real a partir do objeto JSON cru — mesmos campos/nomes de
     * ancestralidades.json (RacialAbility é @Serializable com esses nomes de campo). */
    private fun racialAbilityFromJson(obj: JsonObject): RacialAbility = RacialAbility(
        nome = obj.strOrNull("nome") ?: "",
        descricao = obj.strOrNull("descricao") ?: "",
        descricaoLite = obj.strOrNull("descricaoLite"),
        id = obj.strOrNull("id"),
        category = obj.strOrNull("category"),
        severity = obj.strOrNull("severity"),
        traitId = obj.strOrNull("traitId"),
        targetRef = obj.strOrNull("targetRef"),
        value = obj.intOrDefault("value", 1),
        pontos = obj.intOrDefault("pontos", 0),
        invisivel = obj.boolOrDefault("invisivel", false),
        vezes = obj.intOrDefault("vezes", 1)
    )

    /** Carrega uma raça específica (por nome + livro) do catálogo real como RacialModifier de
     * verdade, pronta pra passar pra GameDataSnapshot — mesmo mapeamento de campos que
     * DataLoader.kt faz ao ler ancestralidades.json (sem depender de DataLoader/Context). */
    private fun racaReal(nome: String, livro: String): RacialModifier {
        val arr: JsonArray = Json.parseToJsonElement(catalogFile().readText()).jsonArray
        val obj = arr.map { it.jsonObject }.first {
            it.strOrNull("nome").equals(nome, ignoreCase = true) &&
                livro in ((it["livros"] as? JsonArray)?.map { l -> l.jsonPrimitive.content } ?: emptyList())
        }
        val habilidades = (obj["habilidades"] as? JsonArray)?.map { racialAbilityFromJson(it.jsonObject) } ?: emptyList()
        return RacialModifier(
            id = obj.strOrNull("id"),
            nome = obj.strOrNull("nome") ?: nome,
            descricao = obj.strOrNull("descricao"),
            habilidades = habilidades,
            origem = livro,
            pontosRaciaisEsperados = obj.intOrDefault("pontosRaciaisEsperados", 2)
        )
    }

    @Test
    fun `Elfos do Basico real ganham Agilidade d6 (traco Agil) pela resolucao de verdade do CriadorState`() {
        val elfos = racaReal("ELFOS", "BASICO")
        val humanos = racaReal("HUMANOS", "BASICO")

        val agil = elfos.habilidades.first { it.id == "AGIL" }
        assertEquals("ATTRIBUTE_BOOST", agil.traitId)
        assertEquals("Agilidade", agil.targetRef)

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(elfos, humanos)))
        state.ancestralidade = "ELFOS"

        assertEquals(
            "Elfos do Básico deveriam ter Agilidade d6 (traço Ágil, id=AGIL) — se isso falhar, o " +
                "id do traço no catálogo real não está mais mapeado em RacialTraitPointCatalog.EFEITOS",
            6,
            state.atributoMinRaw("Agilidade")
        )
    }

    @Test
    fun `Humanos do Basico real ganham Adaptavel de verdade (nao so o texto)`() {
        val humanos = racaReal("HUMANOS", "BASICO")

        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(humanos)))
        state.ancestralidade = "HUMANOS"

        assertTrue(
            "Humanos do Básico deveriam ter Adaptável de verdade (traço Adaptável, id=ADAPTAVEL)",
            state.temAdaptavel()
        )
    }
}
