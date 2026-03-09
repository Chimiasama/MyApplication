package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Pericia
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class CriadorStateKirinSignTest {

    private fun sorte() = Vantagem(
        id = "sorte",
        nome = "Sorte",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito()
    )

    @Test
    fun `kirin trata sorte como vantagem automatica do signo`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "HUMANOS"
            signoAdgSelecionado = "Kirin"
        }

        val sorte = sorte()
        state.adicionarVantagem(sorte)

        assertTrue(state.isVantagemAutomatica(sorte))
        assertFalse(state.podeRemoverVantagem(sorte).first)
    }

    private fun elevarOMoral() = Vantagem(
        id = "elevar_o_moral",
        nome = "Elevar o Moral",
        categoria = Categoria.SOCIAIS,
        requisitos = Requisito()
    )

    @Test
    fun `raposa usa id correto elevar o moral`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "HUMANOS"
            val snapshot = com.example.swadebuilder.model.GameDataSnapshot(
                 listaVantagens = listOf(elevarOMoral()),
                 listaAncestralidadesJson = emptyList(),
                 listaComplicacoes = emptyList(),
                 listaCoracoesCrystal = emptyList(),
                 listaMonstroTemplates = emptyList(),
                 racialAttrMinMap = emptyMap(),
                 racialSkillStartMap = emptyMap(),
                 listaAtributos = emptyList(),
                 mapaAtributosDisplay = emptyMap(),
                 listaPericias = emptyList(),
                 mapaPericias = emptyMap(),
                 mapaAtributosDescricao = emptyMap(),
                 listaPoderes = emptyList(),
                 listaTropos = emptyList(),
                 listaEquipamentos = emptyList(),
                 equipamentoCategorias = emptyList(),
                 superequipCategorias = emptyList(),
                 listaSuperPoderes = emptyList(),
                 arcanoInfo = emptyList()
             )
            updateGameData(snapshot)
        }

        state.selecionarSigno("Raposa")

        assertTrue(state.vantagensSelecionadas.any { it.id == "elevar_o_moral" })
        assertTrue("elevar_o_moral" in state.vantagensAutomaticasDoSigno)
    }


    @Test
    fun `adg nao ignora cap de pericia por valor inicial racial`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Akaimimi (Panda Vermelho)"
            val snapshot = com.example.swadebuilder.model.GameDataSnapshot(
                racialSkillStartMap = mapOf(
                    "AKAIMIMI (PANDA VERMELHO)" to mapOf(
                        "CONVENCAO" to 6,
                        "CONHECIMENTO GERAL" to 8
                    )
                ),
                listaAncestralidadesJson = emptyList(),
                listaComplicacoes = emptyList(),
                listaCoracoesCrystal = emptyList(),
                listaMonstroTemplates = emptyList(),
                racialAttrMinMap = emptyMap(),
                listaAtributos = emptyList(),
                mapaAtributosDisplay = emptyMap(),
                listaPericias = emptyList(),
                mapaPericias = emptyMap(),
                mapaAtributosDescricao = emptyMap(),
                listaVantagens = emptyList(),
                listaPoderes = emptyList(),
                listaTropos = emptyList(),
                listaEquipamentos = emptyList(),
                equipamentoCategorias = emptyList(),
                superequipCategorias = emptyList(),
                listaSuperPoderes = emptyList(),
                arcanoInfo = emptyList()
            )
            updateGameData(snapshot)
        }

        val convencao = com.example.swadebuilder.model.Pericia("Convenção", "ASTUCIA", true)
        val conhecimento = com.example.swadebuilder.model.Pericia("Conhecimento Geral", "ASTUCIA", true)

        assertEquals(13, state.periciaCapRaw(convencao))
        assertEquals(13, state.periciaCapRaw(conhecimento))
    }


    @Test
    fun `kitsunemimi permite escolher uma pericia para iniciar em d4`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Kitsunemimi (Raposa)"
            kitsunemimiPericiaEscolhida = "Pesquisar"
        }

        val pesquisar = com.example.swadebuilder.model.Pericia("Pesquisar", "ASTUCIA", false)
        val intimidar = com.example.swadebuilder.model.Pericia("Intimidar", "ESPIRITO", false)

        assertEquals(4, state.periciaStartRaw(state.ancestralidade, pesquisar))
        assertEquals(0, state.periciaStartRaw(state.ancestralidade, intimidar))
    }

    @Test
    fun `usagimimi permite escolher pericia da adg para iniciar em d6`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Usagimimi (Coelho)"
            usagimimiPericiaEscolhida = "Provocar"
        }

        val provocar = Pericia("Provocar", "ESPIRITO", false, origem = "ARTE_DA_GUERRA")

        assertEquals(6, state.periciaStartRaw(state.ancestralidade, provocar))
    }

    @Test
    fun `transicao escolhida por usagimimi bloqueia tropos nao elementalistas`() {
        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Usagimimi (Coelho)"
            usagimimiPericiaEscolhida = "Transição"
        }

        val samurai = Tropo(
            id = "tropo_samurai",
            nome = "Samurai",
            categoria = "TROPO",
            origem = "ARTE_DA_GUERRA",
            descricao = "",
            ganhaAoComprar = emptyList(),
            periciasGratuitas = emptyMap()
        )

        val elementalista = samurai.copy(id = "tropo_elementalista", nome = "Elementalista")

        assertFalse(state.podeSelecionarTropoPorRestricoesAtuais(samurai))
        assertTrue(state.podeSelecionarTropoPorRestricoesAtuais(elementalista))
        assertTrue(state.podeSelecionarTropoPorRestricoesAtuais(null))
    }

    @Test
    fun `trocar de elementalista remove transicao e devolve sp mesmo fora de usagimimi`() {
        val transicao = Pericia("Transição", "ASTUCIA", false, origem = "ARTE_DA_GUERRA")
        val elementalista = Tropo(
            id = "tropo_elementalista",
            nome = "Elementalista",
            categoria = "TROPO",
            origem = "ARTE_DA_GUERRA",
            descricao = "",
            ganhaAoComprar = emptyList(),
            periciasGratuitas = emptyMap()
        )

        val state = CriadorState().apply {
            compendioArteDaGuerraAtivo = true
            ancestralidade = "Humano (Império San)"
            listaPericias = listOf(transicao)
            listaAtributos = listOf("ASTUCIA")
            valoresAtributos.clear()
            valoresAtributos["ASTUCIA"] = androidx.compose.runtime.mutableIntStateOf(6)
            ensurePericiasRegistered(listaPericias)
            tropoSelecionado = elementalista
            baseIncsPorPericia[transicao] = 1
            spCostStackPorPericia.getValue(transicao).add(2)
        }

        state.selecionarTropo(null)

        assertEquals(0, state.baseIncsPorPericia[transicao])
        assertTrue(state.spCostStackPorPericia.getValue(transicao).isEmpty())
        assertEquals(0, state.rawTotal(transicao))
    }

}
