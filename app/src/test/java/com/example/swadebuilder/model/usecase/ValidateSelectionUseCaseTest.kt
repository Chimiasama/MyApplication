package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.Estagio
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateSelectionUseCaseTest {

    private val useCase = ValidateSelectionUseCase()

    private fun createBaseContext(): ValidateSelectionUseCase.Context {
        // currentSelectionCount removed from Context
        return ValidateSelectionUseCase.Context(
            ancestralidade = "HUMANOS",
            ancestralidadeDef = null,
            compendioCrystalHeartAtivo = false,
            compendioFantasiaAtivo = false,
            compendioPathfinderAtivo = false,
            compendioHorrorAtivo = false,
            compendioArteDaGuerraAtivo = false,
            valoresAtributos = mapOf("FORCA" to 6, "AGILIDADE" to 6, "VIGOR" to 6, "ASTUCIA" to 6, "ESPIRITO" to 6),
            pericias = emptyList(),
            rawTotalPericia = { 0 },
            tipoMonstroSelecionado = null,
            cartaSelvagem = true,
            complicacoesSelecionadas = emptyMap(),
            ppPurchasesThisRank = 0,
            maxPpPurchasesAllowed = 1,
            vantagensSelecionadas = emptyList(),
            emProgresso = false,
            superInvestments = emptyList(),
            listaAtributos = listOf("FORCA", "AGILIDADE", "VIGOR", "ASTUCIA", "ESPIRITO"),
            atributoMaxRaw = { 12 },
            periciaCapRaw = { 12 },
            permiteMultiAntecedenteArcano = false,
            estagioAtual = Estagio("Novato", 0, 3),
            listaDeEstagios = listOf(Estagio("Novato", 0, 3)),
            overrideStageForVantagem = null,
            effectiveProgressoParaVantagens = 0,
            nivelParaEstagio = emptyMap(),
            nasceUmHeroi = false,
            pvFromXpOutstanding = 0,
            tropoSelecionadoId = null,
            getBestPericia = { null }
        )
    }

    @Test
    fun `accepts valid advantage`() {
        val v = Vantagem(id = "sorte", nome = "Sorte", categoria = Categoria.SOCIAIS, origem = "BASICO", requisitos = Requisito())
        val context = createBaseContext()
        assertTrue(useCase.execute(v, context))
    }

    @Test
    fun `rejects blocked scenario advantage (Fantasy Mage)`() {
        val v = Vantagem(id = "mago", nome = "Mago", categoria = Categoria.PROFISSIONAL, origem = "BASICO", requisitos = Requisito())
        val context = createBaseContext().copy(compendioFantasiaAtivo = true)
        assertFalse(useCase.execute(v, context))
    }

    @Test
    fun `rejects if attribute requirement not met`() {
        val v = Vantagem(
            id = "forte", nome = "Forte", categoria = Categoria.COMBATE, origem = "BASICO",
            requisitos = Requisito(atributoMin = mapOf("FORCA" to 8))
        )
        val context = createBaseContext().copy(valoresAtributos = mapOf("FORCA" to 6)) // Have d6, need d8
        assertFalse(useCase.execute(v, context))
    }

    @Test
    fun `accepts if attribute requirement met`() {
        val v = Vantagem(
            id = "forte", nome = "Forte", categoria = Categoria.COMBATE, origem = "BASICO",
            requisitos = Requisito(atributoMin = mapOf("FORCA" to 8))
        )
        val context = createBaseContext().copy(valoresAtributos = mapOf("FORCA" to 8)) // Have d8
        assertTrue(useCase.execute(v, context))
    }

    @Test
    fun `rejects incompatible advantage (Lento vs Ligeiro)`() {
        val ligeiro = Vantagem(id = "ligeiro", nome = "Ligeiro", categoria = Categoria.ANTECEDENTE, origem = "BASICO", requisitos = Requisito())
        val lento = Complicacao(id = "lento", name = "Lento", severity = "Maior", description = "", origem = "BASICO")

        val context = createBaseContext().copy(
            complicacoesSelecionadas = mapOf(lento to "Maior")
        )

        assertFalse(useCase.execute(ligeiro, context))
    }
}
