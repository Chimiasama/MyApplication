package com.example.swadebuilder

import com.example.swadebuilder.model.ArcanoInfo
import com.example.swadebuilder.model.ArmaNatural
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
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.SuperPoder
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ArmaNatural.escalavel deixou de ser um booleano solto setável por raça — achado real:
 * Centauros "Cascos" tinha o booleano `escalavel=true` vinculado à instância da raça, quando
 * na verdade quem decide é o id do traço que concedeu a arma (GARRAS_SEM_PA), igual a
 * qualquer outra Garra. Agora é sempre derivado de RacialTraitPointCatalog
 * .armaNaturalEscalavel(id) — sem esse campo pra setar, não tem como uma raça nova errar o
 * valor. Estes testes conferem que Cascos (Garra, via id GARRAS_SEM_PA) escala com Artista
 * Marcial, e que Chifres (id CHIFRES_MAIORES, não é família Garra) não escala.
 */
class CriadorStateArmaNaturalEscalavelTest {

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

    private val centauros = RacialModifier(
        nome = "CENTAUROS",
        habilidades = listOf(
            RacialAbility(
                nome = "CASCOS", descricao = "", id = "GARRAS_SEM_PA", category = "racial_trait_positive",
                armasNaturais = listOf(ArmaNatural(nome = "Cascos", dano = "For+d4", pa = 0))
            )
        )
    )

    private val minotauros = RacialModifier(
        nome = "MINOTAUROS",
        habilidades = listOf(
            RacialAbility(
                nome = "CHIFRES", descricao = "", id = "CHIFRES_MAIORES", category = "racial_trait_positive",
                armasNaturais = listOf(ArmaNatural(nome = "Chifres", dano = "For+d6", pa = 0))
            )
        )
    )

    @Test
    fun `id da familia Garra e escalavel, id de Chifre nao e`() {
        assertTrue(RacialTraitPointCatalog.armaNaturalEscalavel("GARRAS_SEM_PA"))
        assertTrue(RacialTraitPointCatalog.armaNaturalEscalavel("GARRAS"))
        assertFalse(RacialTraitPointCatalog.armaNaturalEscalavel("CHIFRES_MAIORES"))
        assertFalse(RacialTraitPointCatalog.armaNaturalEscalavel("MORDIDA"))
        assertFalse(RacialTraitPointCatalog.armaNaturalEscalavel(null))
    }

    @Test
    fun `Cascos do Centauro escalam com Artista Marcial, sem precisar de booleano na raca`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(centauros)))
        state.ancestralidade = "CENTAUROS"

        val semArtista = state.extrairArmasNaturais().first { it.nome == "Cascos" }
        assertEquals("For+d4", (semArtista.dano as JsonPrimitive).content)

        state.vantagensSelecionadas.add(Vantagem(id = "artista_marcial", nome = "ARTISTA MARCIAL", categoria = Categoria.COMBATE, requisitos = Requisito()))
        val comArtista = state.extrairArmasNaturais().first { it.nome == "Cascos" }
        assertEquals("For+d6", (comArtista.dano as JsonPrimitive).content)
    }

    @Test
    fun `Chifres do Minotauro NAO escalam com Artista Marcial`() {
        val state = CriadorState()
        state.updateGameData(snapshotWith(listOf(minotauros)))
        state.ancestralidade = "MINOTAUROS"
        state.vantagensSelecionadas.add(Vantagem(id = "artista_marcial", nome = "ARTISTA MARCIAL", categoria = Categoria.COMBATE, requisitos = Requisito()))

        val chifres = state.extrairArmasNaturais().first { it.nome == "Chifres" }
        assertEquals("For+d6", (chifres.dano as JsonPrimitive).content)
    }
}
