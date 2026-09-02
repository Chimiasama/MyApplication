package com.example.swadebuilder

import com.example.swadebuilder.model.Constants
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BrutamontesAtletismoTest {

    private lateinit var state: CriadorState
    private lateinit var atletismo: Pericia
    private lateinit var brutamontes: Vantagem
    private lateinit var musculoso: Vantagem

    @Before
    fun setup() {
        state = CriadorState().apply {
            listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR")
            listaAtributos.forEach {
                valoresAtributos[it] = androidx.compose.runtime.mutableIntStateOf(4)
                paCostStackPorAtributo[it] = mutableListOf()
            }
        }

        atletismo = Pericia(
            nome = "Atletismo",
            atributo = "AGILIDADE",
            descricao = "Perícia de Atletismo",
            basica = true
        )

        brutamontes = Vantagem(
            id = Constants.ID_BRUTAMONTES,
            nome = "Brutamontes",
            categoria = com.example.swadebuilder.model.Categoria.ANTECEDENTE,
            requisitos = Requisito(),
            descricao = "Brutamontes se focam em força e capacidade física."
        )

        musculoso = Vantagem(
            id = Constants.ID_MUSCULOSO,
            nome = "Musculoso",
            categoria = com.example.swadebuilder.model.Categoria.ANTECEDENTE,
            requisitos = Requisito(),
            descricao = "Esta pessoa é muito grande ou está em forma."
        )

        state.listaPericias = listOf(atletismo)
        state.listaVantagens = listOf(brutamontes, musculoso)
        state.ensureAllPericiasRegistered()
        state.rebuildAllPericiaStacks()
    }

    @Test
    fun `atributoBaseParaPericia retorna AGILIDADE quando Brutamontes nao esta presente`() {
        assertEquals("AGILIDADE", state.atributoBaseParaPericia(atletismo))
    }

    @Test
    fun `atributoBaseParaPericia retorna AGILIDADE quando apenas Musculoso esta presente`() {
        state.adicionarVantagem(musculoso)
        assertEquals("AGILIDADE", state.atributoBaseParaPericia(atletismo))
    }

    @Test
    fun `atributoBaseParaPericia retorna FORCA quando Brutamontes esta presente`() {
        state.adicionarVantagem(brutamontes)
        assertEquals("FORCA", state.atributoBaseParaPericia(atletismo))
    }

    @Test
    fun `custo de Atletismo usa Forca quando Brutamontes esta ativo`() {
        // Agilidade = d4, Forca = d8
        state.valoresAtributos["AGILIDADE"]!!.intValue = 4
        state.valoresAtributos["FORCA"]!!.intValue = 8

        // Sem Brutamontes: subir Atletismo de d4 para d6 custa 2 SP (supera Agi d4)
        val regSemBrutamontes = state.calcularPericiaRules(atletismo, idosoActive = false, locked = false)
        assertEquals(2, regSemBrutamontes.cost)

        // Adiciona Brutamontes: subir Atletismo de d4 para d6 custa 1 SP (nao supera For d8)
        state.adicionarVantagem(brutamontes)
        val regComBrutamontes = state.calcularPericiaRules(atletismo, idosoActive = false, locked = false)
        assertEquals(1, regComBrutamontes.cost)
    }

    @Test
    fun `rebuildAllPericiaStacks recalcula custos e aplica refund ao remover Brutamontes`() {
        // Agilidade = d4, Forca = d8
        state.valoresAtributos["AGILIDADE"]!!.intValue = 4
        state.valoresAtributos["FORCA"]!!.intValue = 8

        // Adiciona Brutamontes
        state.adicionarVantagem(brutamontes)
        state.rebuildAllPericiaStacks()

        // Sobe Atletismo ate d8 com Brutamontes (d4->d6: 1 SP, d6->d8: 1 SP -> total 2 SP)
        val reg1 = state.calcularPericiaRules(atletismo, idosoActive = false, locked = false)
        state.increasePericiaFromAdvancement(atletismo, reg1.cost)
        val reg2 = state.calcularPericiaRules(atletismo, idosoActive = false, locked = false)
        state.increasePericiaFromAdvancement(atletismo, reg2.cost)

        assertEquals(8, state.rawTotal(atletismo))
        val spGastoComBrutamontes = state.spCostStackPorPericia[atletismo]?.sum() ?: 0
        assertEquals(2, spGastoComBrutamontes)

        // Remove Brutamontes -> rebuildAllPericiaStacks e chamado
        state.venderVantagem(brutamontes)

        // Agora com Agilidade d4:
        // Atletismo ate d8 exige: d4->d6 (2 SP), d6->d8 (2 SP) -> total 4 SP
        val spGastoSemBrutamontes = state.spCostStackPorPericia[atletismo]?.sum() ?: 0
        assertEquals(4, spGastoSemBrutamontes)
        assertEquals("AGILIDADE", state.atributoBaseParaPericia(atletismo))
    }
}
