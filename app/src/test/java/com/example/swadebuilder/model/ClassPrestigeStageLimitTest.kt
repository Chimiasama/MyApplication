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

    @Test
    fun `bloqueia segunda classe na criacao quando ja existe familia classe selecionada`() {
        val selecionadas = listOf(classeMonge)

        assertTrue(selecionadas.classeExclusivaBloqueada(prestigio))
        assertTrue(selecionadas.classeExclusivaBloqueada(vantagemDeClasse))
        assertFalse(selecionadas.classeExclusivaBloqueada(combate))
    }

    // --- Savage Pathfinder no Estágio Lendário: "uma por Estágio" vira "uma a cada quatro
    // Progressos gastos no Estágio" (docs/swade_pathfinder_basico, l.6783-6788) — sem essa
    // exceção, uma personagem Pathfinder fica travada pra sempre após a 1ª compra no Lendário.

    @Test
    fun `sem Pathfinder ativo, Lendario continua travado em uma por Estagio pra sempre`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Lendário")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Lendário",
                nova = prestigio,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 100,
                pathfinderAtivo = false
            )
        )
    }

    @Test
    fun `com Pathfinder ativo, Lendario bloqueia a 2a compra antes de 4 Progressos gastos`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Lendário")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Lendário",
                nova = prestigio,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 3,
                pathfinderAtivo = true
            )
        )
    }

    @Test
    fun `com Pathfinder ativo, Lendario libera a 2a compra apos 4 Progressos gastos`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Lendário")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertFalse(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Lendário",
                nova = prestigio,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 4,
                pathfinderAtivo = true
            )
        )
    }

    @Test
    fun `com Pathfinder ativo, a 3a compra no Lendario exige 8 Progressos gastos`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Lendário"),
            AdvancementAction.SpendOnAdvantage(advantageId = "prestigio_test", stageName = "Lendário")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Lendário",
                nova = vantagemDeClasse,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 7,
                pathfinderAtivo = true
            )
        )
        assertFalse(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Lendário",
                nova = vantagemDeClasse,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 8,
                pathfinderAtivo = true
            )
        )
    }

    @Test
    fun `Pathfinder ativo nao muda nada fora do Lendario, continua uma por Estagio`() {
        val history = listOf(
            AdvancementAction.SpendOnAdvantage(advantageId = "classe_monge", stageName = "Experiente")
        )
        val catalogo = listOf(classeMonge, prestigio, vantagemDeClasse, combate)

        assertTrue(
            history.atingiuLimiteClasseOuPrestigioNoEstagio(
                stageName = "Experiente",
                nova = prestigio,
                vantagensCatalogo = catalogo,
                progressoGastoNoEstagio = 100,
                pathfinderAtivo = true
            )
        )
    }
}
