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
    // — SEM acumular Estágios pulados ("pega agora ou já era"; a única exceção do livro pra
    // guardar Progressos pra depois é Complicação). O teto (maxPpPurchasesAllowed) é sempre 1
    // por Estágio (ou ilimitado no Lendário), e o valor comparado com ele é só o que já foi
    // comprado NO ESTÁGIO ATUAL — nunca uma soma cumulativa de Estágios anteriores.

    @Test
    fun `bloqueia segunda compra no mesmo Estagio`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 1,
            maxPpPurchasesAllowed = 1,
            currentSelectionCount = 1
        )
        assertFalse(useCase.execute(input))
    }

    @Test
    fun `libera a primeira compra do Estagio mesmo com Estagios anteriores pulados`() {
        // Chegou no Veterano sem nunca ter comprado antes — tem direito a exatamente 1 compra
        // agora, não a um catch-up dos Estágios perdidos (essas oportunidades já eram).
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDePoder,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 1,
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
    // Sombria) — mesma ideia de Pontos de Poder (sem acumular Estágios pulados), mas SEM a
    // exceção de teto ilimitado no Lendário, e sem passar pela lógica de maxSelections (que
    // travaria em 1 compra pra sempre, já que nenhuma delas define maxSelections no catálogo).

    private val pontosDeChi = Vantagem(
        id = "pontos_de_chi",
        nome = "PONTOS DE CHI",
        categoria = Categoria.CHI,
        origem = "ARTE_DA_GUERRA",
        requisitos = Requisito(),
        limiteCompra = "uma_vez_por_estagio"
    )

    @Test
    fun `generico bloqueia segunda compra no mesmo Estagio`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 3,
            estagioPurchasesGenerico = 1,
            maxEstagioPurchasesGenericoAllowed = 1
        )
        assertFalse(useCase.execute(input))
    }

    @Test
    fun `generico libera a primeira compra do Estagio mesmo com Estagios anteriores pulados`() {
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 0,
            estagioPurchasesGenerico = 0,
            maxEstagioPurchasesGenericoAllowed = 1
        )
        assertTrue(useCase.execute(input))
    }

    @Test
    fun `generico nao tem excecao no Lendario, ao contrario de Pontos de Poder`() {
        // Mesmo já tendo 5 compras anteriores registradas (uma por Estágio, ao longo do
        // tempo), o teto genérico continua sendo 1 por Estágio — sem a exceção ilimitada que
        // só Pontos de Poder tem no Lendário.
        val input = ValidatePowerPointsLimitUseCase.Input(
            vantagem = pontosDeChi,
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 0,
            currentSelectionCount = 5,
            estagioPurchasesGenerico = 1,
            maxEstagioPurchasesGenericoAllowed = 1
        )
        assertFalse(useCase.execute(input))
    }
}
