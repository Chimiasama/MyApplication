package com.example.swadebuilder

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.GrupoAlternativo
import com.example.swadebuilder.model.HabilidadeCriacao
import com.example.swadebuilder.model.MonstroTemplate
import com.example.swadebuilder.model.RacialTraitAuditFormatter
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.model.paraTropo
import com.example.swadebuilder.model.usecase.ValidatePrerequisiteUseCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TropoAuditAndDrenarAlmaTest {

    @Test
    fun testDrenarAlmaWithPoderesMisticosAndEspiritoD10() {
        val validator = ValidatePrerequisiteUseCase()

        val drenarAlmaVantagem = Vantagem(
            id = "drenar_a_alma",
            nome = "Drenar a Alma",
            categoria = Categoria.PODER,
            requisitos = Requisito(
                gruposAlternativos = listOf(
                    GrupoAlternativo(
                        vantagens = listOf("antecedente_arcano"),
                        periciaMinOpcional = mapOf("Fé" to 10)
                    ),
                    GrupoAlternativo(
                        vantagens = listOf("poderes_misticos"),
                        atributos = mapOf("Espírito" to 10)
                    )
                )
            )
        )

        val poderesMisticosAnjo = Vantagem(
            id = "poderes_misticos_anjo",
            nome = "Poderes Místicos (Anjo)",
            categoria = Categoria.PODER,
            requisitos = Requisito()
        )

        // Valid with Espírito d10 (10)
        val validInput = ValidatePrerequisiteUseCase.Input(
            vantagem = drenarAlmaVantagem,
            vantagensSelecionadas = listOf(poderesMisticosAnjo),
            complicacoesSelecionadas = emptyList(),
            valoresAtributos = mapOf("ESPIRITO" to 10)
        )
        assertTrue("Drenar a Alma should be valid with poderes_misticos_anjo and Espírito d10", validator.execute(validInput))

        // Invalid with Espírito d8 (8)
        val invalidInput = ValidatePrerequisiteUseCase.Input(
            vantagem = drenarAlmaVantagem,
            vantagensSelecionadas = listOf(poderesMisticosAnjo),
            complicacoesSelecionadas = emptyList(),
            valoresAtributos = mapOf("ESPIRITO" to 8)
        )
        assertFalse("Drenar a Alma should be invalid with Espírito d8", validator.execute(invalidInput))
    }

    @Test
    fun testTropoAuditFormatter() {
        val monstro = MonstroTemplate(
            id = "anjo",
            nome = "Anjo",
            descricao = "Ser angelical",
            atributosBonus = mapOf("Forca" to 2, "Vigor" to 2)
        )
        val tropo = monstro.paraTropo()

        val catalogo = listOf(
            HabilidadeCriacao(
                id = "VOO_MOV_12",
                nome = "Voo",
                custo = 2,
                descricao = "Pode voar com Movimentação 12."
            )
        )

        val formatted = RacialTraitAuditFormatter.formatar(
            habilidades = tropo.habilidades,
            catalogoOficial = catalogo,
            idsExclusivos = emptyMap(),
            allVantagens = emptyList()
        )

        assertTrue("Formatted audit lines should not be empty", formatted.isNotEmpty())
        assertTrue("Should contain attribute boost for Força", formatted.any { it.contains("Forca") || it.contains("ATTRIBUTE_BOOST") })
    }
}
