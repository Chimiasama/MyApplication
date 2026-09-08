package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.Categoria
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
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * getAncestralidadeDef() tinha um curto-circuito que pulava
 * applyAncestryVariantAdjustments() pra toda raça com um candidato só —
 * inclusive Meio-Demônio, onde mora a troca Adaptável/Antecedente Arcano
 * (Demônio) via meioDemonioAA. Resultado real (achado pelo usuário testando o
 * app): escolher o AA na raça nunca trocava nada — a raça ficava travada em
 * Adaptável, e a Vantagem do AA nunca era concedida de verdade. Estes testes
 * fixam o comportamento correto.
 */
class CriadorStateMeioDemonioTest {

    private fun meioDemonio(): RacialModifier = RacialModifier(
        nome = "Meio-Demônio",
        atributos = emptyMap(),
        pericias = emptyMap(),
        habilidades = listOf(
            RacialAbility(nome = "Adaptável", descricao = "", id = "ADAPTAVEL", category = "racial_trait_positive")
        ),
        origem = "CIDADE_SOL_VAPOR"
    )

    private fun aaDemonioMeioDemonio(): Vantagem = Vantagem(
        id = "aa_demonio_meio_demonio",
        nome = "ANTECEDENTE ARCANO (Demônio)",
        categoria = Categoria.PODER,
        requisitos = Requisito(estagio = "Novato"),
        subtipoArcano = "DEMONIO_MEIO"
    )

    private fun snapshot(): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = listOf(meioDemonio()),
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = emptyList<Pericia>(),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = listOf(aaDemonioMeioDemonio()),
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    @Test
    fun `meio-demonio comeca com Adaptavel disponivel por padrao`() {
        val state = CriadorState()
        state.updateGameData(snapshot())
        state.aplicarAncestralidade("Meio-Demônio", mutableListOf(), autoRefund = false)

        assertTrue(state.temAdaptavel())
        assertTrue(state.currentAncestryDef?.habilidades.orEmpty().any { it.id == "ADAPTAVEL" })
        assertFalse(state.vantagensSelecionadas.any { it.id == "aa_demonio_meio_demonio" })
    }

    @Test
    fun `escolher o AA troca Adaptavel pelo traco vinculado e concede a Vantagem de verdade`() {
        val state = CriadorState()
        state.updateGameData(snapshot())
        state.aplicarAncestralidade("Meio-Demônio", mutableListOf(), autoRefund = false)

        state.selecionarMeioDemonioTraco(true)

        // O toggle precisa passar por applyAncestryVariantAdjustments — sem
        // isso currentAncestryDef nunca refletia a escolha (bug real).
        val habilidades = state.currentAncestryDef?.habilidades.orEmpty()
        assertFalse(habilidades.any { it.id == "ADAPTAVEL" })
        assertTrue(habilidades.any { it.id == "ANTECEDENTE_ARCANO_DEMONIO_MEIO" && it.targetRef == "aa_demonio_meio_demonio" })

        // Adaptável não pode mais aparecer como Vantagem disponível pro jogador.
        assertFalse(state.temAdaptavel())

        // A Vantagem real precisa ter sido concedida de verdade (aparecer na
        // aba Vantagens/Resumo), não só no texto de Características.
        assertTrue(state.vantagensSelecionadas.any { it.id == "aa_demonio_meio_demonio" })
        assertTrue(state.temAntecedenteArcano())
    }

    @Test
    fun `voltar para Adaptavel remove a Vantagem do AA`() {
        val state = CriadorState()
        state.updateGameData(snapshot())
        state.aplicarAncestralidade("Meio-Demônio", mutableListOf(), autoRefund = false)
        state.selecionarMeioDemonioTraco(true)

        state.selecionarMeioDemonioTraco(false)

        assertTrue(state.temAdaptavel())
        assertFalse(state.vantagensSelecionadas.any { it.id == "aa_demonio_meio_demonio" })
    }
}
