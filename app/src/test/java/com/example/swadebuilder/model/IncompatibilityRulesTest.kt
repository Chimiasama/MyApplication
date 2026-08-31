package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IncompatibilityRulesTest {

    @Test
    fun `ligeiro conflita com lento e com a variante lento_ch`() {
        assertEquals(setOf("lento", "lento_ch"), IncompatibilityRules.complicacoesIncompativeisCom("ligeiro"))
        assertTrue("ligeiro" in IncompatibilityRules.vantagensIncompativeisCom("lento"))
        assertTrue("ligeiro" in IncompatibilityRules.vantagensIncompativeisCom("lento_ch"))
    }

    @Test
    fun `musculoso conflita com obeso nos dois sentidos`() {
        assertEquals(setOf("obeso"), IncompatibilityRules.complicacoesIncompativeisCom("musculoso"))
        assertEquals(setOf("musculoso"), IncompatibilityRules.vantagensIncompativeisCom("obeso"))
    }

    @Test
    fun `rico e podre_de_rico conflitam com pobreza nos dois sentidos`() {
        assertEquals(setOf("pobreza"), IncompatibilityRules.complicacoesIncompativeisCom("rico"))
        assertEquals(setOf("pobreza"), IncompatibilityRules.complicacoesIncompativeisCom("podre_de_rico"))
        assertEquals(setOf("rico", "podre_de_rico"), IncompatibilityRules.vantagensIncompativeisCom("pobreza"))
    }

    @Test
    fun `escolhido conflita com inimigo e com a variante inimigo_ch`() {
        assertEquals(setOf("inimigo", "inimigo_ch"), IncompatibilityRules.complicacoesIncompativeisCom("escolhido"))
        assertTrue("escolhido" in IncompatibilityRules.vantagensIncompativeisCom("inimigo"))
        assertTrue("escolhido" in IncompatibilityRules.vantagensIncompativeisCom("inimigo_ch"))
    }

    @Test
    fun `todas as variantes de Antecedente Arcano Milagres conflitam com Alma Penhorada e Alma Vendida`() {
        val esperado = setOf("comp_alma_penhorada", "comp_alma_vendida")
        assertEquals(esperado, IncompatibilityRules.complicacoesIncompativeisCom("antecedente_arcano_milagres"))
        assertEquals(esperado, IncompatibilityRules.complicacoesIncompativeisCom("aa_milagres"))
        assertEquals(esperado, IncompatibilityRules.complicacoesIncompativeisCom("antecedente_arcano_milagres_pf"))

        val vantagensQueConflitamComAlma = setOf(
            "antecedente_arcano_milagres",
            "aa_milagres",
            "antecedente_arcano_milagres_pf"
        )
        assertEquals(vantagensQueConflitamComAlma, IncompatibilityRules.vantagensIncompativeisCom("comp_alma_penhorada"))
        assertEquals(vantagensQueConflitamComAlma, IncompatibilityRules.vantagensIncompativeisCom("comp_alma_vendida"))
    }

    @Test
    fun `antecedente arcano tecnomagia conflita com maldicao do gremlin`() {
        assertEquals(setOf("comp_maldicao_gremlin"), IncompatibilityRules.complicacoesIncompativeisCom("aa_tecnomagia"))
        assertEquals(setOf("aa_tecnomagia"), IncompatibilityRules.vantagensIncompativeisCom("comp_maldicao_gremlin"))
    }

    @Test
    fun `tecnofobia conflita com taro engenheiro, mestre das caldeiras e mecanico cego`() {
        val esperado = setOf("taro_engenheiro", "mestre_das_caldeiras", "mecanico_cego")
        assertEquals(esperado, IncompatibilityRules.vantagensIncompativeisCom("comp_tecnofobia"))

        esperado.forEach { vantagemId ->
            assertEquals(setOf("comp_tecnofobia"), IncompatibilityRules.complicacoesIncompativeisCom(vantagemId))
        }
    }

    @Test
    fun `ids sem conflito conhecido retornam conjunto vazio`() {
        assertTrue(IncompatibilityRules.complicacoesIncompativeisCom("bloquear").isEmpty())
        assertTrue(IncompatibilityRules.vantagensIncompativeisCom("idoso").isEmpty())
    }
}
