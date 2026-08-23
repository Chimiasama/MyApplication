package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentVisibilityTest {

    @Test
    fun `cidade do sol a vapor mantém seletor generico de antecedente arcano visivel`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val genericArcane = Vantagem(
            id = "antecedente_arcano",
            nome = "ANTECEDENTE ARCANO",
            categoria = Categoria.ANTECEDENTE,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = genericArcane,
            multiplosAAHabilitados = false
        )

        assertTrue(visible)
    }

    @Test
    fun `cidade do sol a vapor mostra AA especifico quando multiplos antecedentes estão habilitados`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val specificArcane = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.ANTECEDENTE,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = specificArcane,
            multiplosAAHabilitados = true
        )

        assertTrue(visible)
    }

    @Test
    fun `cidade do sol a vapor esconde AA especifico quando multiplos antecedentes estão desabilitados`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val specificArcane = Vantagem(
            id = "aa_magia_negra",
            nome = "ANTECEDENTE ARCANO (Magia Negra)",
            categoria = Categoria.ANTECEDENTE,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = specificArcane,
            multiplosAAHabilitados = false
        )

        assertFalse(visible)
    }

    @Test
    fun `cidade do sol a vapor aceita alias de origem sol e vapor`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
        }

        val steamAdvantage = Vantagem(
            id = "engenhoca_1",
            nome = "Engenhoca",
            categoria = Categoria.PROFISSIONAL,
            origem = "SOL E VAPOR",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = steamAdvantage,
            multiplosAAHabilitados = true
        )

        assertTrue(visible)
    }

    @Test
    fun `pathfinder esconde AA específico de magia quando multi antecedentes está desabilitado`() {
        val state = CriadorState().apply {
            compendioPathfinderAtivo = true
            permiteMultiAntecedenteArcano = false
        }

        val pfMagicArcane = Vantagem(
            id = "antecedente_arcano_magia_pf",
            nome = "ANTECEDENTE ARCANO (Magia)",
            categoria = Categoria.ANTECEDENTE,
            origem = "PATHFINDER",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = pfMagicArcane,
            multiplosAAHabilitados = false
        )

        assertFalse(visible)
    }

    @Test
    fun `pathfinder esconde AA específico de milagres quando multi antecedentes está desabilitado`() {
        val state = CriadorState().apply {
            compendioPathfinderAtivo = true
            permiteMultiAntecedenteArcano = false
        }

        val pfMiraclesArcane = Vantagem(
            id = "antecedente_arcano_milagres_pf",
            nome = "ANTECEDENTE ARCANO (Milagres)",
            categoria = Categoria.ANTECEDENTE,
            origem = "PATHFINDER",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = pfMiraclesArcane,
            multiplosAAHabilitados = false
        )

        assertFalse(visible)
    }

    @Test
    fun `pathfinder mantém AA genérico visível quando multi antecedentes está desabilitado`() {
        val state = CriadorState().apply {
            compendioPathfinderAtivo = true
            permiteMultiAntecedenteArcano = false
        }

        val genericArcane = Vantagem(
            id = "antecedente_arcano",
            nome = "ANTECEDENTE ARCANO",
            categoria = Categoria.ANTECEDENTE,
            origem = "BASICO",
            requisitos = Requisito()
        )

        val visible = state.isVantagemVisible(
            vant = genericArcane,
            multiplosAAHabilitados = false
        )

        assertTrue(visible)
    }

    @Test
    fun `novos poderes fica invisivel para magia negra`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_magia_negra",
                    nome = "ANTECEDENTE ARCANO (Magia Negra)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        val novosPoderes = Vantagem(
            id = "novos_poderes",
            nome = "NOVOS PODERES",
            categoria = Categoria.PODER,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        assertFalse(state.isVantagemVisible(novosPoderes, multiplosAAHabilitados = true))
    }

    @Test
    fun `novos poderes fica invisivel para aa demonio`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_demonio",
                    nome = "ANTECEDENTE ARCANO (Demônio)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        val novosPoderes = Vantagem(
            id = "novos_poderes",
            nome = "NOVOS PODERES",
            categoria = Categoria.PODER,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        assertFalse(state.isVantagemVisible(novosPoderes, multiplosAAHabilitados = true))
    }

    @Test
    fun `novos poderes fica invisivel para aa milagres de sol e vapor`() {
        val state = CriadorState().apply {
            compendioCidadeSolVaporAtivo = true
            vantagensSelecionadas.add(
                Vantagem(
                    id = "aa_milagres",
                    nome = "ANTECEDENTE ARCANO (Milagres)",
                    categoria = Categoria.PODER,
                    origem = "SOL_VAPOR",
                    requisitos = Requisito()
                )
            )
        }

        val novosPoderes = Vantagem(
            id = "novos_poderes",
            nome = "NOVOS PODERES",
            categoria = Categoria.PODER,
            origem = "CIDADE_SOL_VAPOR",
            requisitos = Requisito()
        )

        assertFalse(state.isVantagemVisible(novosPoderes, multiplosAAHabilitados = true))
    }

    @Test
    fun `todos livros de cenario ativos sao incluidos em getActiveOrigins e getActiveModuleKeys`() {
        val testCases = listOf(
            Pair({ s: CriadorState -> s.compendioPathfinderAtivo = true }, "PATHFINDER"),
            Pair({ s: CriadorState -> s.compendioDeadlandsAtivo = true }, "DEADLANDS"),
            Pair({ s: CriadorState -> s.compendioCrystalHeartAtivo = true }, "CRYSTAL_HEART"),
            Pair({ s: CriadorState -> s.compendioArteDaGuerraAtivo = true }, "ARTE_DA_GUERRA"),
            Pair({ s: CriadorState -> s.compendioCidadeSolVaporAtivo = true }, "CIDADE_SOL_VAPOR"),
            Pair({ s: CriadorState -> s.compendioWiseguysAtivo = true }, "WISEGUYS")
        )

        for ((activator, expectedKey) in testCases) {
            val state = CriadorState().apply { activator(this) }
            val origins = state.getActiveOrigins()
            val moduleKeys = state.getActiveModuleKeys()

            assertTrue("$expectedKey deve estar em activeOrigins", expectedKey in origins)
            assertTrue("$expectedKey deve estar em activeModuleKeys", expectedKey in moduleKeys)
        }
    }

    @Test
    fun `complicacoes do pathfinder sao visiveis quando compendioPathfinderAtivo esta verdadeiro`() {
        val state = CriadorState().apply {
            compendioPathfinderAtivo = true
        }

        val pfComplication = Complicacao(
            id = "almofadinha",
            name = "ALMOFADINHA",
            severity = "menor",
            description = "Descrição",
            origem = "PATHFINDER"
        )

        assertTrue(state.isComplicacaoVisible(pfComplication))
    }

    @Test
    fun `livros de cenario autonomos excluem BASICO mas compendios de expansao mantem BASICO em getActiveOrigins`() {
        val pathfinderState = CriadorState().apply { compendioPathfinderAtivo = true }
        assertFalse("BASICO não deve estar ativo para Pathfinder", "BASICO" in pathfinderState.getActiveOrigins())

        val adgState = CriadorState().apply { compendioArteDaGuerraAtivo = true }
        assertFalse("BASICO não deve estar ativo para Arte da Guerra", "BASICO" in adgState.getActiveOrigins())

        val fantasiaState = CriadorState().apply { compendioFantasiaAtivo = true }
        assertTrue("BASICO deve permanecer ativo para Compêndio de Fantasia", "BASICO" in fantasiaState.getActiveOrigins())

        val horrorState = CriadorState().apply { compendioHorrorAtivo = true }
        assertTrue("BASICO deve permanecer ativo para Compêndio de Horror", "BASICO" in horrorState.getActiveOrigins())

        val scifiState = CriadorState().apply { compendioSciFiAtivo = true }
        assertTrue("BASICO deve permanecer ativo para Compêndio de Sci-Fi", "BASICO" in scifiState.getActiveOrigins())
    }

}
