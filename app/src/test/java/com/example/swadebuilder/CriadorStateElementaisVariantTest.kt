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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Elementais (Sci-Fi): a troca Padrão -> "Ar, Fogo ou Água" (Forte+Resistência
 * vira Forma de Energia) morava direto num `if (key == "ELEMENTAIS")` em
 * CriadorState.applyAncestryVariantAdjustments(), construindo RacialAbility na
 * mão — migrado pra ler AncestryVariantRegistry.elementaisScifi() via
 * ResolveAncestryVariantPackageUseCase, mesmo padrão já usado por
 * scifiVariantDrivenKeys (Drakens etc.). Estes testes fixam que a troca
 * continua produzindo exatamente os mesmos traços de antes.
 */
class CriadorStateElementaisVariantTest {

    private fun elementais(): RacialModifier = RacialModifier(
        nome = "ELEMENTAIS",
        origem = "SCI_FI",
        habilidades = listOf(
            RacialAbility(nome = "MUITO FORTE", descricao = "", id = "MUITO_FORTE"),
            RacialAbility(nome = "RESISTÊNCIA +2", descricao = "", id = "RESISTENCIA", vezes = 2)
        ),
        opcoes = listOf("Padrão", "Ar, Fogo ou Água")
    )

    private fun snapshot(): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = listOf(elementais()),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList<Pericia>(),
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

    @Test
    fun `Padrao mantem Muito Forte e Resistencia, sem Forma de Energia`() {
        val state = CriadorState()
        state.updateGameData(snapshot())
        state.compendioSciFiAtivo = true
        state.ancestralidade = "ELEMENTAIS"
        state.scifiVariant = "Padrão"

        val habilidades = state.currentAncestryDef?.habilidades.orEmpty()
        assertTrue(habilidades.any { it.id == "MUITO_FORTE" })
        assertTrue(habilidades.any { it.id == "RESISTENCIA" })
        assertFalse(habilidades.any { it.id == "FORMA_DE_ENERGIA" })
        assertFalse(habilidades.any { it.id == "AJUSTE_FORMA_DE_ENERGIA" })
    }

    @Test
    fun `Ar Fogo ou Agua troca Muito Forte e Resistencia por Forma de Energia mais ajuste de orcamento`() {
        val state = CriadorState()
        state.updateGameData(snapshot())
        state.compendioSciFiAtivo = true
        state.ancestralidade = "ELEMENTAIS"
        state.scifiVariant = "Ar, Fogo ou Água"

        val habilidades = state.currentAncestryDef?.habilidades.orEmpty()
        assertFalse(habilidades.any { it.id == "MUITO_FORTE" })
        assertFalse(habilidades.any { it.id == "RESISTENCIA" })

        val formaDeEnergia = habilidades.firstOrNull { it.id == "FORMA_DE_ENERGIA" }
        assertTrue(formaDeEnergia != null)
        assertFalse(formaDeEnergia!!.invisivel)

        val ajuste = habilidades.firstOrNull { it.id == "AJUSTE_FORMA_DE_ENERGIA" }
        assertTrue(ajuste != null)
        assertEquals(2, ajuste!!.pontos)
        assertTrue(ajuste.invisivel)
        assertEquals(2, ajuste.resolvedPontos())
    }
}
