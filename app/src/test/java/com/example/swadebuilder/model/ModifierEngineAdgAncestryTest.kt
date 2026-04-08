package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierEngineAdgAncestryTest {

    private fun stateWithAncestry(ancestralidade: String, modifier: RacialModifier): CriadorState {
        return CriadorState().apply {
            this.ancestralidade = ancestralidade
            updateGameData(
                GameDataSnapshot(
                    listaComplicacoes = emptyList(),
                    listaCoracoesCrystal = emptyList(),
                    listaAncestralidadesJson = listOf(modifier),
                    listaMonstroTemplates = emptyList(),
                    racialAttrMinMap = emptyMap(),
                    racialSkillStartMap = emptyMap(),
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
            )
        }
    }

    @Test
    fun `generic aparar minus from racial disadvantage applies parry penalty`() {
        val state = stateWithAncestry(
            ancestralidade = "Tanukimimi (Tanuki)",
            modifier = RacialModifier(
                nome = "Tanukimimi (Tanuki)",
                origem = "ARTE_DA_GUERRA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = listOf("APARAR -1"),
                habilidades = emptyList()
            )
        )

        val modifiers = ModifierEngine.collect(state)

        assertTrue(modifiers.any { it.id == "racial_parry_generic" && it.value == -1 })
    }

    @Test
    fun `explicit racial movement bonus applies to pace`() {
        val state = stateWithAncestry(
            ancestralidade = "Usagimimi (Coelho)",
            modifier = RacialModifier(
                nome = "Usagimimi (Coelho)",
                origem = "ARTE_DA_GUERRA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = emptyList(),
                habilidades = emptyList(),
                movimentacao = 2
            )
        )

        val modifiers = ModifierEngine.collect(state)

        assertTrue(modifiers.any { it.id == "racial_pace_explicit" && it.value == 2 })
    }

    @Test
    fun `povo rato size penalty is not double counted`() {
        val state = stateWithAncestry(
            ancestralidade = "Povo Rato",
            modifier = RacialModifier(
                nome = "Povo Rato",
                origem = "FANTASIA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = emptyList(),
                habilidades = listOf(
                    RacialAbility(
                        nome = "DIMINUTO (Tamanho -4)",
                        descricao = "Membros do povo rato medem cerca de 18 centímetros de altura. Isso lhes confere Tamanho -4 e concede os benefícios de Minúsculo...",
                        id = "DIMINUTO",
                        category = "racial_trait_positive"
                    )
                )
            )
        )

        val modifiers = ModifierEngine.collect(state)
        val sizeModifiers = modifiers.filter { it.target == ModifierTarget.SIZE_DISPLAY }

        assertTrue(sizeModifiers.size == 1)
        assertTrue(sizeModifiers.first().value == -4)
    }

    @Test
    fun `terracota gains plus three toughness`() {
        val state = stateWithAncestry(
            ancestralidade = "Terracota",
            modifier = RacialModifier(
                nome = "Terracota",
                origem = "ARTE_DA_GUERRA",
                atributos = emptyMap(),
                pericias = emptyMap(),
                vantagensGratis = emptyList(),
                desvantagens = emptyList(),
                habilidades = listOf(
                    RacialAbility(
                        nome = "Nascidos de Argila",
                        descricao = "Terracotas são um equilíbrio entre carne e pedra.",
                        id = "NASCIDOS_DE_ARGILA",
                        category = "racial_trait_positive"
                    )
                )
            )
        )

        val modifiers = ModifierEngine.collect(state)

        assertTrue(
            modifiers.any {
                it.id == "racial_terracota_res" &&
                    it.target == ModifierTarget.TOUGHNESS_FLAT &&
                    it.value == 3
            }
        )
    }
}
