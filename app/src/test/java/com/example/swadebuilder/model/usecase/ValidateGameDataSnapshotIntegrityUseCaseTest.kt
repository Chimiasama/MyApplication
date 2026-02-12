package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.CrystalHeart
import com.example.swadebuilder.model.EquipamentoCategoria
import com.example.swadebuilder.model.EquipamentoItem
import com.example.swadebuilder.model.GameDataSnapshot
import com.example.swadebuilder.model.Poder
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.Tropo
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.Pericia
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateGameDataSnapshotIntegrityUseCaseTest {

    private val useCase = ValidateGameDataSnapshotIntegrityUseCase()

    @Test
    fun `retorna erro quando detecta ids duplicados`() {
        val snapshot = snapshot(
            vantagens = listOf(
                Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE),
                Vantagem(id = "ALERTA", nome = "Alerta 2", categoria = Categoria.COMBATE)
            )
        )

        val result = useCase.execute(snapshot)

        assertFalse(result.ok)
        assertTrue(result.issues.any { it.contains("Vantagens") })
    }

    @Test
    fun `retorna ok para snapshot consistente`() {
        val result = useCase.execute(snapshot())
        assertTrue(result.ok)
    }

    private fun snapshot(
        vantagens: List<Vantagem> = listOf(Vantagem(id = "alerta", nome = "Alerta", categoria = Categoria.COMBATE)),
        pericias: List<Pericia> = listOf(Pericia(nome = "Lutar", atributo = "AGILIDADE", basica = true)),
        poderes: List<Poder> = listOf(Poder(id = "rajada", nome = "Rajada", origem = "BASICO", estagio = "Novato", pontosDePoder = "1", distancia = "", duracao = "", descricao = ""))
    ) = GameDataSnapshot(
        listaComplicacoes = emptyList<Complicacao>(),
        listaCoracoesCrystal = emptyList<CrystalHeart>(),
        listaAncestralidadesJson = emptyList<RacialModifier>(),
        listaMonstroTemplates = emptyList(),
        racialAttrMinMap = emptyMap(),
        racialSkillStartMap = emptyMap(),
        listaAtributos = emptyList(),
        mapaAtributosDisplay = emptyMap(),
        listaPericias = pericias,
        mapaPericias = pericias.associateBy { it.nome.lowercase() },
        mapaAtributosDescricao = emptyMap(),
        listaVantagens = vantagens,
        listaPoderes = poderes,
        listaTropos = emptyList<Tropo>(),
        listaEquipamentos = emptyList<EquipamentoItem>(),
        equipamentoCategorias = emptyList<EquipamentoCategoria>(),
        superequipCategorias = emptyList<EquipamentoCategoria>(),
        listaSuperPoderes = emptyList(),
        arcanoInfo = emptyList()
    )
}
