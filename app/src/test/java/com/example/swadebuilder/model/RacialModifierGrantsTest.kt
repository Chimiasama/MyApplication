package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * `RacialModifier.resolvedVantagensGratis()`/`resolvedDesvantagens()` — usados pelo caminho
 * REAL de aplicar uma ancestralidade (`CriadorState.aplicarAncestralidade`,
 * `ApplyAncestryChangeCoordinatorUseCase`) pra conceder Vantagens/Complicações automáticas.
 * Achado real (auditoria pedida pelo usuário): os dois métodos reimplementavam essa leitura
 * separado das funções de topo de arquivo já corretas ([vantagensGratisEfetivas]/
 * [desvantagensEfetivas]), e o ramo `category == "racial_hindrance"` de resolvedDesvantagens()
 * nunca priorizava `targetRef` (ao contrário do irmão resolvedVantagensGratis(), que já
 * priorizava targetRef pro ramo `racial_edge`) — uma Complicação reskinada por targetRef (ex.:
 * Draconianos "Mal-Humorado" concedendo "Arrogante") virava a desvantagem automática com o
 * nome de EXIBIÇÃO da raça em vez do nome real do catálogo de Complicações.
 */
class RacialModifierGrantsTest {

    @Test
    fun `resolvedDesvantagens prioriza targetRef sobre o nome reskinado, igual resolvedVantagensGratis ja fazia`() {
        val raca = RacialModifier(
            nome = "Draconianos",
            habilidades = listOf(
                RacialAbility(
                    nome = "Mal-Humorado", // nome de exibição/skin — não é o nome real da Complicação
                    descricao = "",
                    id = "ARROGANTE",
                    category = "racial_hindrance",
                    traitId = "RACIAL_HINDRANCE",
                    targetRef = "Arrogante",
                    severity = "Maior"
                )
            )
        )
        assertEquals(listOf("Arrogante (Maior)"), raca.resolvedDesvantagens())
    }

    @Test
    fun `resolvedVantagensGratis prioriza targetRef sobre o nome reskinado`() {
        val raca = RacialModifier(
            nome = "Kitsunemimi (Raposa)",
            habilidades = listOf(
                RacialAbility(
                    nome = "Socialmente Sofisticados",
                    descricao = "",
                    id = "SOCIALMENTE_SOFISTICADOS",
                    category = "racial_edge",
                    traitId = "GRANTED_EDGE",
                    targetRef = "Cativar o Ambiente"
                )
            )
        )
        assertEquals(listOf("Cativar o Ambiente"), raca.resolvedVantagensGratis())
    }

    @Test
    fun `sem targetRef, cai no nome cru da habilidade, igual antes`() {
        val raca = RacialModifier(
            nome = "Avianos",
            habilidades = listOf(
                RacialAbility(
                    nome = "Não Sabe Nadar",
                    descricao = "",
                    id = "NAO_SABE_NADAR",
                    category = "racial_hindrance",
                    severity = "Menor"
                )
            )
        )
        assertEquals(listOf("Não Sabe Nadar (Menor)"), raca.resolvedDesvantagens())
    }
}
