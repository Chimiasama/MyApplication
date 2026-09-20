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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CriadorStateTransmorfosPoderTest {

    private fun snapshotWith(racas: List<RacialModifier>, vantagens: List<Vantagem>): GameDataSnapshot = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = racas,
        listaMonstroTemplates = emptyList<MonstroTemplate>(),
        listaAtributos = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR"),
        mapaAtributosDisplay = listOf("AGILIDADE", "ASTUCIA", "ESPIRITO", "FORCA", "VIGOR").associateWith { it },
        listaPericias = listOf(Pericia(nome = "Atletismo", atributo = "AGILIDADE", basica = true)),
        mapaPericias = emptyMap(),
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = vantagens,
        listaPoderes = emptyList<Poder>(),
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList<SuperPoder>(),
        arcanoInfo = emptyList<ArcanoInfo>()
    )

    // Cópia exata do JSON real de TRANSMORFOS (ancestralidades.json, livro FANTASIA).
    private val transmorfos = RacialModifier(
        nome = "TRANSMORFOS",
        habilidades = listOf(
            RacialAbility(
                nome = "CARISMÁTICO",
                descricao = "Começam gratuitamente com a Vantagem Carismático.",
                id = "CARISMATICO",
                category = "racial_trait_positive"
            ),
            RacialAbility(
                nome = "MUDAR DE FORMA",
                descricao = "Transmorfos têm Antecedente Arcano (Dom). Como ação livre limitada, podem mudar sua aparência física da mesma forma que o poder disfarce com a Limitação Pessoal.",
                id = "ANTECEDENTE_ARCANO_PODER",
                category = "racial_trait_positive"
            ),
            RacialAbility(
                nome = "SEGREDO",
                descricao = "Mantêm sua habilidade em segredo.",
                id = "SEGREDO",
                category = "racial_hindrance",
                severity = "Maior"
            )
        ),
        origem = "FANTASIA",
        especieId = "transmorfos"
    )

    private val genericAntecedenteArcano = Vantagem(
        id = "antecedente_arcano",
        nome = "ANTECEDENTE ARCANO",
        categoria = Categoria.ANTECEDENTE,
        origem = "HORROR",
        requisitos = Requisito(),
        grupoId = "antecedente_arcano",
        choiceOptions = listOf("Dom", "Magia", "Milagres", "Psiônicos", "Ciência Estranha")
    )

    private val carismatico = Vantagem(
        id = "carismatico",
        nome = "Carismático",
        categoria = Categoria.SOCIAIS,
        origem = "BASICO",
        requisitos = Requisito()
    )

    @Test
    fun `Transmorfos recebe Antecedente Arcano Dom mesmo so com Fantasia ativo (sem Horror Sci-Fi)`() {
        // Simula o cenário mais comum: só o Compêndio de Fantasia ligado (onde
        // Transmorfos vive), sem Horror/Sci-Fi — livros de onde vem a entrada
        // ESPECÍFICA "antecedente_arcano_dom" no catálogo oficial. O fallback
        // genérico (ResolveAncestryRacialPackageUseCase, usa "antecedente_arcano"
        // + escolha "Dom") precisa cobrir esse caso.
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(transmorfos), listOf(genericAntecedenteArcano, carismatico)))
        state.compendioFantasiaAtivo = true

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("TRANSMORFOS", feedback)

        val arcanos = state.vantagensSelecionadas.filter { it.id == "antecedente_arcano" || it.id == "antecedente_arcano_dom" }
        assertEquals(
            "Esperava exatamente 1 Antecedente Arcano concedido, achei: ${arcanos.map { it.id to it.choice }}",
            1,
            arcanos.size
        )
        assertEquals("DOM", arcanos.first().choice?.uppercase())
    }

    @Test
    fun `Traco Carismatico do Transmorfo NAO concede a Vantagem de verdade (achado, nao corrigido ainda)`() {
        // Documenta o estado ATUAL: a descrição da habilidade diz "Começam
        // gratuitamente com a Vantagem Carismático", mas nada no pacote racial
        // de TRANSMORFOS (ensureAdvantageNames/ensureAdvantageIds) concede essa
        // Vantagem de verdade — nem o id "CARISMATICO" está em EFEITOS pra virar
        // efeito automático. Este teste trava o comportamento atual (falha se
        // alguém corrigir isso sem atualizar o teste) — ver relatório da rodada.
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(transmorfos), listOf(genericAntecedenteArcano, carismatico)))
        state.compendioFantasiaAtivo = true

        val feedback = mutableListOf<String>()
        state.aplicarAncestralidade("TRANSMORFOS", feedback)

        assertTrue(
            "Se este teste falhar, é sinal de que Carismático PASSOU a ser concedido — bom, é o comportamento correto; só ajustar o teste.",
            state.vantagensSelecionadas.none { it.id == "carismatico" }
        )
    }
}
