package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.Estagio
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateSelectionUseCaseTest {

    private val useCase = ValidateSelectionUseCase()

    // Helper to create a dummy context with defaults
    private fun createContext(
        compendioCrystalHeartAtivo: Boolean = false,
        atributosRaw: Map<String, Int> = emptyMap(),
        vantagensSelecionadas: List<Vantagem> = emptyList(),
        estagioAtual: Estagio = Estagio("Novato", 0, 19)
    ): ValidateSelectionUseCase.SelectionContext {
        return ValidateSelectionUseCase.SelectionContext(
            ancestralidade = "Humanos",
            racialDef = null,
            estagioAtual = estagioAtual,
            listaEstagios = listOf(Estagio("Novato", 0, 19), Estagio("Aguerrido", 20, 39)),
            atributosRaw = atributosRaw,
            periciasRaw = emptyMap(),
            getBestPericia = { null },
            getRawTotal = { 0 },
            getMaxAttributeRaw = { 12 },
            vantagensSelecionadas = vantagensSelecionadas,
            complicacoesSelecionadas = emptyMap(),
            cartaSelvagem = true,
            isMonstro = false,
            tipoMonstro = null,
            emProgresso = false,
            nasceUmHeroi = false,
            pvFromXpOutstanding = 0,
            permiteMultiAntecedenteArcano = false,
            comprasPpPorEstagioSum = 0,
            maxComprasPpAteAgora = 1,
            superInvestmentsCount = 0,
            compendioFantasiaAtivo = false,
            compendioHorrorAtivo = false,
            compendioPathfinderAtivo = false,
            compendioCrystalHeartAtivo = compendioCrystalHeartAtivo,
            compendioArteDaGuerraAtivo = false
        )
    }

    private val novato = Estagio("Novato", 0, 19)
    private val aguerrido = Estagio("Aguerrido", 20, 39)

    @Test
    fun `should block forbidden advantages in Crystal Heart`() {
        val context = createContext(compendioCrystalHeartAtivo = true)
        val v = Vantagem(id = "rico", nome = "Rico", categoria = Categoria.SOCIAIS, requisitos = Requisito())

        val result = useCase.execute(v, context)
        assertFalse(result.allowed)
    }

    @Test
    fun `should allow allowed advantages in Crystal Heart`() {
        val context = createContext(compendioCrystalHeartAtivo = true)
        val v = Vantagem(id = "valid_edge", nome = "Valid", categoria = Categoria.COMBATE, requisitos = Requisito())

        val result = useCase.execute(v, context)
        assertTrue(result.allowed)
    }

    @Test
    fun `should validate attribute prerequisites`() {
        val context = createContext(atributosRaw = mapOf("AGILIDADE" to 6)) // d6
        val v = Vantagem(
            id = "test", nome = "Test", categoria = Categoria.COMBATE,
            requisitos = Requisito(atributoMin = mapOf("AGILIDADE" to 8)) // Requires d8
        )

        val result = useCase.execute(v, context)
        assertFalse("Should fail d8 requirement with d6", result.allowed)

        val contextPass = createContext(atributosRaw = mapOf("AGILIDADE" to 8))
        val resultPass = useCase.execute(v, contextPass)
        assertTrue("Should pass d8 requirement with d8", resultPass.allowed)
    }

    @Test
    fun `should validate rank prerequisites`() {
        val context = createContext(estagioAtual = novato)
        val v = Vantagem(
            id = "seasoned_edge", nome = "Seasoned Edge", categoria = Categoria.COMBATE,
            requisitos = Requisito(estagio = "Aguerrido")
        )

        val result = useCase.execute(v, context)
        assertFalse("Novice cannot take Seasoned edge", result.allowed)

        val contextSeasoned = createContext(estagioAtual = aguerrido)
        val resultPass = useCase.execute(v, contextSeasoned)
        assertTrue("Seasoned can take Seasoned edge", resultPass.allowed)
    }
}
