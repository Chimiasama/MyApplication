package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Categoria
import com.example.swadebuilder.model.Requisito
import com.example.swadebuilder.model.Vantagem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResolveGrantedAncestryAdvantagesUseCaseTest {

    private val useCase = ResolveGrantedAncestryAdvantagesUseCase()

    @Test
    fun `resolves by name id and keyified id`() {
        val advantages = listOf(
            Vantagem(
                id = "sorte",
                nome = "Sorte",
                categoria = Categoria.SOCIAIS,
                origem = "BASICO",
                requisitos = Requisito()
            ),
            Vantagem(
                id = "antecedente_arcano_poderes",
                nome = "Antecedente Arcano (Poderes)",
                categoria = Categoria.ANTECEDENTE,
                origem = "SOL_VAPOR",
                requisitos = Requisito()
            ),
            Vantagem(
                id = "ameacador_special",
                nome = "Ameaçador Special",
                categoria = Categoria.COMBATE,
                origem = "BASICO",
                requisitos = Requisito()
            )
        )

        val result = useCase.execute(
            ResolveGrantedAncestryAdvantagesUseCase.Params(
                grantedAdvantageNamesOrIds = listOf("Sorte", "antecedente_arcano_poderes", "ameacador special"),
                allAdvantages = advantages,
                selectedAdvantages = emptyList()
            )
        )

        assertEquals(listOf("sorte", "antecedente_arcano_poderes", "ameacador_special"), result.advantagesToAdd.map { it.id })
    }

    @Test
    fun `skips already selected and removes duplicates by id`() {
        val advantages = listOf(
            Vantagem(
                id = "sorte",
                nome = "Sorte",
                categoria = Categoria.SOCIAIS,
                origem = "BASICO",
                requisitos = Requisito()
            ),
            Vantagem(
                id = "espirituoso",
                nome = "Espirituoso",
                categoria = Categoria.SOCIAIS,
                origem = "BASICO",
                requisitos = Requisito()
            )
        )

        val result = useCase.execute(
            ResolveGrantedAncestryAdvantagesUseCase.Params(
                grantedAdvantageNamesOrIds = listOf("Sorte", "sorte", "espirituoso", "inexistente"),
                allAdvantages = advantages,
                selectedAdvantages = listOf(advantages.first())
            )
        )

        assertEquals(1, result.advantagesToAdd.size)
        assertTrue(result.advantagesToAdd.any { it.id == "espirituoso" })
    }
}
