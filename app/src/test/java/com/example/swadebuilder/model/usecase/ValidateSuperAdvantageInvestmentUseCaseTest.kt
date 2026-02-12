package com.example.swadebuilder.model.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateSuperAdvantageInvestmentUseCaseTest {

    private val useCase = ValidateSuperAdvantageInvestmentUseCase()

    @Test
    fun `retorna erro quando vantagem nao existe`() {
        val erro = useCase.execute(
            ValidateSuperAdvantageInvestmentUseCase.Input(
                vantagemIdSolicitada = "vant_x",
                vantagemEncontrada = null,
                mensagemBloqueioClasse = null,
                jaPossuiVantagem = false,
                requisitosAtendidosIgnorandoEstagio = false
            )
        )

        assertEquals("Vantagem não encontrada: vant_x.", erro)
    }

    @Test
    fun `retorna bloqueio de classe quando informado`() {
        val erro = useCase.execute(
            ValidateSuperAdvantageInvestmentUseCase.Input(
                vantagemIdSolicitada = "vant_x",
                vantagemEncontrada = ValidateSuperAdvantageInvestmentUseCase.AdvantageRef("vant_x", "Vant X"),
                mensagemBloqueioClasse = "bloqueio",
                jaPossuiVantagem = false,
                requisitosAtendidosIgnorandoEstagio = true
            )
        )

        assertEquals("bloqueio", erro)
    }

    @Test
    fun `retorna erro quando vantagem ja foi comprada`() {
        val erro = useCase.execute(
            ValidateSuperAdvantageInvestmentUseCase.Input(
                vantagemIdSolicitada = "vant_x",
                vantagemEncontrada = ValidateSuperAdvantageInvestmentUseCase.AdvantageRef("vant_x", "Vant X"),
                mensagemBloqueioClasse = null,
                jaPossuiVantagem = true,
                requisitosAtendidosIgnorandoEstagio = true
            )
        )

        assertEquals("Você já possui a vantagem Vant X.", erro)
    }

    @Test
    fun `retorna erro quando requisitos nao sao atendidos`() {
        val erro = useCase.execute(
            ValidateSuperAdvantageInvestmentUseCase.Input(
                vantagemIdSolicitada = "vant_x",
                vantagemEncontrada = ValidateSuperAdvantageInvestmentUseCase.AdvantageRef("vant_x", "Vant X"),
                mensagemBloqueioClasse = null,
                jaPossuiVantagem = false,
                requisitosAtendidosIgnorandoEstagio = false
            )
        )

        assertEquals("Requisitos não atendidos para a vantagem (exceto Estágio).", erro)
    }

    @Test
    fun `retorna null quando validacao passa`() {
        val erro = useCase.execute(
            ValidateSuperAdvantageInvestmentUseCase.Input(
                vantagemIdSolicitada = "vant_x",
                vantagemEncontrada = ValidateSuperAdvantageInvestmentUseCase.AdvantageRef("vant_x", "Vant X"),
                mensagemBloqueioClasse = null,
                jaPossuiVantagem = false,
                requisitosAtendidosIgnorandoEstagio = true
            )
        )

        assertNull(erro)
    }
}
