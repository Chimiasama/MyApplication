package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatePowerPointsLimitUseCaseTest {

    private val useCase = ValidatePowerPointsLimitUseCase()

    private val pontosDePoder = Vantagem(
        id = "pontos_de_poder",
        nome = "PONTOS DE PODER",
        categoria = Categoria.PODER,
        origem = "BASICO",
        requisitos = Requisito()
    )

    // "Pontos de Poder pode ser selecionada mais de uma vez, mas apenas uma vez por Estágio"
    // — o teto (maxPpPurchasesAllowed) é CUMULATIVO desde o Novato, então o valor comparado
    // com ele precisa ser a soma de compras em TODOS os Estágios, não só o atual. Comparar
    // com "compras só neste Estágio" (bug antigo) reabre o teto a cada Estágio novo e permite
    // comprar muito mais que uma vez por Estágio.

    @Test
    fun `bloqueia quando o total cumulativo ja bateu no teto do Estagio atual`() {
        // Novato + Experiente já compradas (2 no total); no Veterano (índice 2) o teto
        // cumulativo é 3 — ainda cabe 1, mas não 2.
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 2,
            maxPpPurchasesAllowed = 3,
            currentSelectionCount = 2
        )
        assertTrue(useCase.execute(input))
    }

    @Test
    fun `bloqueia compra extra no mesmo Estagio quando o teto cumulativo ja foi atingido`() {
        // Sem o fix, comparar só "compras neste Estágio" (0, estágio novo) contra o teto
        // cumulativo (3) liberaria erroneamente até 3 compras SÓ no Veterano, além das que já
        // foram feitas em estágios anteriores.
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 3,
            maxPpPurchasesAllowed = 3,
            currentSelectionCount = 3
        )
        assertFalse(useCase.execute(input))
    }

    @Test
    fun `libera compra de catchup quando estagios anteriores foram pulados`() {
        // Chegou no Veterano (teto cumulativo 3) sem nunca ter comprado antes — pode comprar
        // até 3 vezes de uma vez (catch-up dos Estágios perdidos).
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 3,
            currentSelectionCount = 0
        )
        assertTrue(useCase.execute(input))
    }

    @Test
    fun `Lendario libera compra ilimitada usando Int MAX_VALUE como teto`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 50,
            maxPpPurchasesAllowed = Int.MAX_VALUE,
            currentSelectionCount = 50
        )
        assertTrue(useCase.execute(input))
    }

    @Test
    fun `vantagem generica nao-infinita ainda respeita maxSelections`() {
        val vantagemUnica = Vantagem(
            id = "vantagem_unica",
            nome = "VANTAGEM ÚNICA",
            categoria = Categoria.SOCIAIS,
            origem = "BASICO",
            requisitos = Requisito(),
            limiteCompra = "uma_vez",
            maxSelections = 1
        )
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = vantagemUnica,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 1
        )
        assertFalse(useCase.execute(input))
    }

    // --- "uma vez por Estágio" GENÉRICO (Pontos de Chi, Presa, Poder do Sangue, Vontade
    // Sombria) — mesma ideia de Pontos de Poder (catch-up cumulativo), mas SEM a exceção de
    // teto ilimitado no Lendário, e sem passar pela lógica de maxSelections (que travaria em
    // 1 compra pra sempre, já que nenhuma delas define maxSelections no catálogo).

    private val pontosDeChi = Vantagem(
        id = "pontos_de_chi",
        nome = "PONTOS DE CHI",
        categoria = Categoria.CHI,
        origem = "ARTE_DA_GUERRA",
        requisitos = Requisito(),
        limiteCompra = "uma_vez_por_estagio"
    )

    @Test
    fun `generico bloqueia quando o total cumulativo ja bateu no teto do Estagio atual`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 3,
            estagioPurchasesGenerico = 3,
            maxEstagioPurchasesGenericoAllowed = 3
        )
        assertFalse(useCase.execute(input))
    }

    @Test
    fun `generico libera compra de catchup quando estagios anteriores foram pulados`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 0,
            estagioPurchasesGenerico = 0,
            maxEstagioPurchasesGenericoAllowed = 3
        )
        assertTrue(useCase.execute(input))
    }

    @Test
    fun `generico nao tem excecao no Lendario, ao contrario de Pontos de Poder`() {
        // Mesmo teto (5) e mesmo total já comprado (5) que uma Pontos de Poder no Lendário
        // aceitaria sem limite — mas uma Vantagem "uma_vez_por_estagio" comum continua presa
        // ao teto cumulativo normal.
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 5,
            estagioPurchasesGenerico = 5,
            maxEstagioPurchasesGenericoAllowed = 5
        )
        assertFalse(useCase.execute(input))
    }
}
