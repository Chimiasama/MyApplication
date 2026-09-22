package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
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
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.ForcaMinimaCalculator
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Traço "Diminuto" (livro Fantasia, pág. 10): "equipamentos feitos para personagens
 * [Pequenas/Muito Pequenas/Minúsculas] pesam e custam [metade/um quarto/um décimo] do
 * valor listado". Cobre tanto a matemática pura (ForcaMinimaCalculator) quanto o caminho
 * de código real de CriadorState (pesoEquipamentoEfetivo/custoEquipamentoEfetivo/
 * totalPesoEquipamentos) — mesmo padrão de bug que o resto da auditoria vem caçando:
 * efeito cadastrado no catálogo mas nunca lido, ou lido do campo errado.
 */
class CriadorStateDiminutoEquipmentTest {

    private fun snapshotWith(racas: List<RacialModifier>): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR").associateWith { it },
        listaPericias = listOf(Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = emptyList<Vantagem>(),
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    // -----------------------------------------------------------------
    // 1) Matemática pura — os 3 tiers do livro (Pequeno/Muito Pequeno/Minúsculo)
    // -----------------------------------------------------------------

    @Test
    fun `divisor de equipamento Diminuto bate com o livro - metade um quarto um decimo`() {
        assertEquals(2.0, ForcaMinimaCalculator.divisorEquipamentoDiminuto(2), 0.0001) // Pequeno: metade
        assertEquals(4.0, ForcaMinimaCalculator.divisorEquipamentoDiminuto(3), 0.0001) // Muito Pequeno: um quarto
        assertEquals(10.0, ForcaMinimaCalculator.divisorEquipamentoDiminuto(4), 0.0001) // Minúsculo: um décimo
        assertEquals(1.0, ForcaMinimaCalculator.divisorEquipamentoDiminuto(0), 0.0001) // Sem Diminuto: sem desconto
    }

    @Test
    fun `custo inteiro reduzido nunca zera um item que ja custava algo`() {
        assertEquals(150, ForcaMinimaCalculator.custoInteiroReduzidoPorDiminuto(300, 2))
        assertEquals(1, ForcaMinimaCalculator.custoInteiroReduzidoPorDiminuto(3, 4)) // 3/10 arredonda pra 0, mas piso é 1
        assertEquals(0, ForcaMinimaCalculator.custoInteiroReduzidoPorDiminuto(0, 4)) // item já de graça continua de graça
    }

    // -----------------------------------------------------------------
    // 2) CriadorState real: raça Minúscula (Tamanho -4) compra equipamento comum
    // -----------------------------------------------------------------

    private fun estadoComRacaMinuscula(): CriadorState {
        val diminutoHab = RacialAbility(
            nome = "Diminuto (Minúsculo)",
            descricao = "Tamanho -4, Diminuto.",
            traitId = "DIMINUTO_TAMANHO_4",
            value = -4,
            pontos = -6
        )
        val racaMinuscula = RacialModifier(nome = "FAERIE", habilidades = listOf(diminutoHab), origem = "FANTASIA")
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(racaMinuscula)))
        state.ancestralidade = "FAERIE"
        return state
    }

    @Test
    fun `peso e custo de equipamento comum caem pra um decimo com raca Minuscula`() {
        val state = estadoComRacaMinuscula()
        val espada = EquipamentoItem(
            nome = "Espada Longa",
            peso = JsonPrimitive("4"),
            custo = JsonPrimitive("300")
        )
        assertEquals(0.4f, state.pesoEquipamentoEfetivo(espada)!!, 0.001f)
        assertEquals(30, state.custoEquipamentoEfetivo(espada))
    }

    @Test
    fun `peso total da mochila reflete o desconto de todos os itens comprados`() {
        val state = estadoComRacaMinuscula()
        val espada = EquipamentoItem(nome = "Espada Longa", peso = JsonPrimitive("4"), custo = JsonPrimitive("300"))
        val mochila = EquipamentoItem(nome = "Mochila", peso = JsonPrimitive("2"), custo = JsonPrimitive("10"))
        state.equipamentosComprados.add(espada)
        state.equipamentosComprados.add(mochila)
        // (4/10) + (2/10) = 0.6, não 6.0 (o que aconteceria sem o desconto aplicado)
        assertEquals(0.6f, state.totalPesoEquipamentos(), 0.001f)
    }

    @Test
    fun `sem Diminuto o peso e custo continuam o valor cheio do catalogo`() {
        val humanos = RacialModifier(nome = "HUMANOS", habilidades = emptyList(), origem = "BASICO")
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(humanos)))
        state.ancestralidade = "HUMANOS"

        val espada = EquipamentoItem(nome = "Espada Longa", peso = JsonPrimitive("4"), custo = JsonPrimitive("300"))
        assertEquals(4f, state.pesoEquipamentoEfetivo(espada)!!, 0.001f)
        assertEquals(300, state.custoEquipamentoEfetivo(espada))
    }

    @Test
    fun `peso nao numerico devolve null sem quebrar`() {
        val state = estadoComRacaMinuscula()
        val item = EquipamentoItem(nome = "Item Especial", peso = JsonPrimitive("Variável"), custo = JsonPrimitive("Variável"))
        assertNull(state.pesoEquipamentoEfetivo(item))
    }
}
