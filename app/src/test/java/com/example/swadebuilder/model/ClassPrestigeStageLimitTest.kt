package com.example.swadebuilder.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassPrestigeStageLimitTest {

    private val classeMonge = Vantagem(
        id = "classe_monge",
        nome = "Monge",
        categoria = Categoria.CLASSE,
        origem = "PATHFINDER",
        requisitos = Requisito()
    )

    private val prestigio = Vantagem(
        id = "prestigio_test",
        nome = "Prestígio Teste",
        categoria = Categoria.PRESTIGIO,
        origem = "PATHFINDER",
        requisitos = Requisito()
    )


    private val vantagemDeClasse = Vantagem(
        id = "poderes_misticos_monge",
        nome = "Poderes Místicos: Monge",
        categoria = Categoria.VANTAGEM_DE_CLASSE,
        origem = "PATHFINDER",
        requisitos = Requisito(estagio = "Experiente")
    )

    private val combate = Vantagem(
        id = "bloqueio",
        nome = "Bloqueio",
        categoria = Categoria.COMBATE,
        origem = "PATHFINDER",
        requisitos = Requisito()
    )

    @Test
    fun `bloqueia nova classe no mesmo estágio quando já houve compra de classe`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Experiente")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(history.atingiuLimiteClasseOuPrestigioNoEstagio("Experiente", prestigio, catalogo))
        assertFalse(history.atingiuLimiteClasseOuPrestigioNoEstagio("Veterano", prestigio, catalogo))
    }

    @Test
    fun `nao bloqueia vantagens que nao sejam classe ou prestigio`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Experiente")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertFalse(history.atingiuLimiteClasseOuPrestigioNoEstagio("Experiente", combate, catalogo))
    }

    @Test
    fun `bloqueia compra em novato quando classe foi adquirida na criacao`() {
        val history = emptyList<AdvancementAction>()
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)
        val selecionadas = listOf(classeMonge)

        assertTrue(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Novato",
                nova = prestigio,
                vantagensCatalogo = catalogo,
                vantagensSelecionadas = selecionadas
            )
        )
    }
    @Test
    fun `compra de vantagem de classe tambem consome limite do estágio`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "poderes_misticos_monge", stageName = "Experiente")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(history.atingiuLimiteClasseOuPrestigioNoEstagio("Experiente", classeMonge, catalogo))
        assertTrue(history.atingiuLimiteClasseOuPrestigioNoEstagio("Experiente", prestigio, catalogo))
        assertFalse(history.atingiuLimiteClasseOuPrestigioNoEstagio("Veterano", classeMonge, catalogo))
    }

}
