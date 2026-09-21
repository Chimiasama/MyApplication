package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Golpe de Asa" (requisito de livro: "Asas") e "Queimar" (requisito de livro: "Arma de
 * Sopro") — trocamos a checagem da tag manual solta em `ancestralidades.json` pelo traço
 * de VERDADE da raça (RacialTraitPointCatalog.temTracoVoo()/temArmaDeSopro()). Achado real
 * ao mexer nisso: Draconianos tinham a tag "asas" cadastrada mesmo sem o traço Voo — o
 * livro (Fantasia, Ideias Variantes de Draconianos) deixa claro que asas/Voo são opcionais
 * pra essa raça, não o padrão. Testa exatamente esse caso real do catálogo.
 */
class ValidateRequirementsUseCaseTagsTest {

    private val useCase = ValidateRequirementsUseCase()

    private val golpeDeAsa = Vantagem(
        id = "golpe_de_asa",
        nome = "GOLPE DE ASA",
        categoria = Categoria.COMBATE,
        origem = "FANTASIA",
        requisitos = Requisito(tags = listOf("asas"))
    )

    private val queimar = Vantagem(
        id = "queimar",
        nome = "QUEIMAR",
        categoria = Categoria.COMBATE,
        origem = "FANTASIA",
        requisitos = Requisito(atributoMin = mapOf("Vigor" to 8), tags = listOf("arma_de_sopro"))
    )

    private fun inputFor(vantagem: Vantagem, ancDef: RacialModifier?, atributos: Map<String, Int> = emptyMap()) =
        ValidateRequirementsUseCase.Input(
            vantagem = vantagem,
            valoresAtributos = atributos,
            pericias = emptyList<Pericia>(),
            rawTotalPericia = { 0 },
            ancestralidadeDef = ancDef,
            tipoMonstroSelecionado = null,
            cartaSelvagem = false
        )

    @Test
    fun `Draconianos SEM traco Voo nao pode comprar Golpe de Asa, mesmo com a tag antiga cadastrada`() {
        // Réplica do catálogo real: Draconianos tem "asas" na tag manual, mas NENHUM
        // traço de Voo em habilidades[] (a raça-padrão não voa, só as Ideias Variantes
        // dizem "se draconianos no seu cenário tiverem asas, adicione Voo").
        val draconianosComTagAsasMasSemVoo = RacialModifier(
            nome = "DRACONIANOS",
            habilidades = listOf(
                RacialAbility(nome = "ARMA DE SOPRO", descricao = "", id = "ARMA_DE_SOPRO", category = "racial_trait_positive")
            ),
            tags = listOf("arma_de_sopro", "asas"),
            especieId = "draconianos"
        )

        assertFalse(useCase.execute(inputFor(golpeDeAsa, draconianosComTagAsasMasSemVoo)))
    }

    @Test
    fun `Avianos com traco Voo pode comprar Golpe de Asa`() {
        val avianos = RacialModifier(
            nome = "AVIANOS",
            habilidades = listOf(
                RacialAbility(nome = "VOO", descricao = "", id = "VOO_MOV_12", category = "racial_trait_positive")
            ),
            tags = listOf("asas"),
            especieId = "avianos"
        )

        assertTrue(useCase.execute(inputFor(golpeDeAsa, avianos)))
    }

    @Test
    fun `Fadas com Voo Movimentacao 6 tambem pode comprar Golpe de Asa (qualquer tier de Voo)`() {
        val fadas = RacialModifier(
            nome = "FADAS",
            habilidades = listOf(
                RacialAbility(nome = "VOO", descricao = "", id = "VOO_MOV_6", category = "racial_trait_positive")
            ),
            tags = listOf("asas"),
            especieId = "fadas"
        )

        assertTrue(useCase.execute(inputFor(golpeDeAsa, fadas)))
    }

    @Test
    fun `Draconianos com traco Arma de Sopro pode comprar Queimar`() {
        val draconianos = RacialModifier(
            nome = "DRACONIANOS",
            habilidades = listOf(
                RacialAbility(nome = "ARMA DE SOPRO", descricao = "", id = "ARMA_DE_SOPRO", category = "racial_trait_positive")
            ),
            tags = listOf("arma_de_sopro", "asas"),
            especieId = "draconianos"
        )

        assertTrue(useCase.execute(inputFor(queimar, draconianos, mapOf("Vigor" to 8))))
    }

    @Test
    fun `Raca sem Arma de Sopro nao pode comprar Queimar`() {
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), especieId = "humano")

        assertFalse(useCase.execute(inputFor(queimar, humanos, mapOf("Vigor" to 8))))
    }
}
