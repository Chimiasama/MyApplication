package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.CustomAncestryVariant
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.HabilidadeCriacao
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Peça 4 do plano (ver docs/auditoria_mecanica_racas_2026-08-31.md,
 * "Variante Customizada escopada por uma opção específica"): uma Variante
 * Customizada pode sobrescrever só UMA opção de Seleção de uma raça (ex.: só
 * o Signo Dragão do Humano Arte da Guerra), deixando as outras 100%
 * oficiais. `CustomAncestryVariant.opcaoAlvoId` (nulo = raça inteira, como
 * sempre) + `CriadorState.currentSelectionOptionId()` decidem isso.
 */
class CriadorStateCustomVariantScopedOptionTest {

    private fun snapshotComHumanoAdg(): GameDataSnapshot {
        val humanoAdg = RacialModifier(
            nome = "HUMANOS",
            habilidades = listOf(
                RacialAbility(nome = "Signos de Nascença", descricao = "", id = "SIGNOS_DE_NASCENCA")
            ),
            origem = "ARTE_DA_GUERRA"
        )
        return GameDataSnapshot(
            listaComplicacoes = emptyList<Complicacao>(),
            listaCoracoesCrystal = emptyList<CrystalHeart>(),
            listaAncestralidadesJson = listOf(humanoAdg),
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
    }

    private fun estadoComSigno(signo: String?): CriadorState {
        val state = CriadorState()
        state.updateGameData(snapshotComHumanoAdg())
        state.compendioArteDaGuerraAtivo = true
        state.ancestralidade = "HUMANOS"
        state.signoAdgSelecionado = signo
        return state
    }

    private fun variantScopedToDragao(): CustomAncestryVariant = CustomAncestryVariant(
        id = "var_dragao_custom",
        ancestralidadeId = "HUMANOS",
        nome = "Dragão Ancestral",
        tracosAdicionados = listOf(
            HabilidadeCriacao(nome = "Fôlego Dracônico", custo = 0, descricao = "Traço bespoke de teste.", id = "folego_draconico")
        ),
        opcaoAlvoId = "dragao"
    )

    @Test
    fun `variante escopada ao Dragao aplica quando Dragao esta ativo`() {
        val state = estadoComSigno("Dragão")
        state.listaVariantesRaciaisCustom = listOf(variantScopedToDragao())
        state.selecionarVarianteRacialCustom("var_dragao_custom")

        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "folego_draconico" })
    }

    @Test
    fun `variante escopada ao Dragao NAO aplica quando outro signo esta ativo`() {
        val state = estadoComSigno("Boi")
        state.listaVariantesRaciaisCustom = listOf(variantScopedToDragao())
        state.selecionarVarianteRacialCustom("var_dragao_custom")

        assertFalse(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "folego_draconico" })
        // A opção Boi continua 100% oficial (Força d6 do signo, sem interferência).
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "FORTE" })
    }

    @Test
    fun `variante nao escopada (opcaoAlvoId nulo) continua aplicando pra qualquer opcao`() {
        val naoEscopada = variantScopedToDragao().copy(id = "var_geral", opcaoAlvoId = null)
        val state = estadoComSigno("Boi")
        state.listaVariantesRaciaisCustom = listOf(naoEscopada)
        state.selecionarVarianteRacialCustom("var_geral")

        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "folego_draconico" })
    }

    @Test
    fun `trocar de Dragao pra outro signo desativa a variante escopada sem precisar desmarcar`() {
        val state = estadoComSigno("Dragão")
        state.listaVariantesRaciaisCustom = listOf(variantScopedToDragao())
        state.selecionarVarianteRacialCustom("var_dragao_custom")
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "folego_draconico" })

        state.selecionarSigno("Kirin")

        assertFalse(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "folego_draconico" })
    }
}
