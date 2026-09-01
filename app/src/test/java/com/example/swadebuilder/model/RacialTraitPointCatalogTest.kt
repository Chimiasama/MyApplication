package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RacialTraitPointCatalogTest {

    @Test
    fun `custoDe eh case e acento insensivel via keyify`() {
        assertEquals(-1, RacialTraitPointCatalog.custoDe("fragil"))
        assertEquals(-1, RacialTraitPointCatalog.custoDe("FRAGIL"))
        assertEquals(1, RacialTraitPointCatalog.custoDe("cabecada"))
    }

    @Test
    fun `custoDe retorna zero pra id desconhecido ou nulo`() {
        assertEquals(0, RacialTraitPointCatalog.custoDe(null))
        assertEquals(0, RacialTraitPointCatalog.custoDe("ID_QUE_NAO_EXISTE"))
    }

    @Test
    fun `tamanho mais e menos 1 tem sinais opostos apos a correcao da colisao de id`() {
        assertEquals(1, RacialTraitPointCatalog.custoDe("TAMANHO_MAIS_1"))
        assertEquals(-1, RacialTraitPointCatalog.custoDe("TAMANHO_MENOS_1"))
    }

    @Test
    fun `cabecada e cabeca dura sao traços diferentes com custos diferentes`() {
        assertEquals(1, RacialTraitPointCatalog.custoDe("CABECADA"))
        assertEquals(-2, RacialTraitPointCatalog.custoDe("CABECA_DURA"))
    }

    @Test
    fun `custos batem com o catalogo oficial de criacao de racas quando ha equivalente`() {
        // basico_habilidades_raciais.json é a fonte de verdade; conferindo
        // alguns casos que mudaram na recalibração (a correção mais comum:
        // penalidade num ATRIBUTO vale -2, não -1 como penalidade de perícia).
        assertEquals(5, RacialTraitPointCatalog.custoDe("ACAO_ADICIONAL")) // oficial: acao_adicional
        assertEquals(8, RacialTraitPointCatalog.custoDe("CONSTRUTO")) // oficial: construto
        assertEquals(8, RacialTraitPointCatalog.custoDe("MORTO_VIVO")) // oficial: morto_vivo
        assertEquals(1, RacialTraitPointCatalog.custoDe("MORDIDA")) // oficial: mordida (For+d4)
        assertEquals(1, RacialTraitPointCatalog.custoDe("RESISTENCIA")) // oficial: resistencia_racial (+1)
        assertEquals(-2, RacialTraitPointCatalog.custoDe("SEM_INSTRUCAO")) // -1 Astúcia é penalidade de ATRIBUTO (oficial penalidade_atributo_1 = -2)
        assertEquals(-1, RacialTraitPointCatalog.custoDe("OBVIO")) // -1 Furtividade é penalidade de PERÍCIA (oficial penalidade_pericia_1 = -1)
    }

    @Test
    fun `efeitoDe retorna AtributoStep pros tracos de atributo de alvo fixo`() {
        val efeito = RacialTraitPointCatalog.efeitoDe("AGIL")
        assertTrue(efeito is RacialTraitEffect.AtributoStep)
        assertEquals("Agilidade", (efeito as RacialTraitEffect.AtributoStep).atributo)
        assertEquals(1, efeito.passos)
    }

    @Test
    fun `efeitoDe retorna dois passos pra muito forte e muito resistente`() {
        val forte = RacialTraitPointCatalog.efeitoDe("MUITO_FORTE") as RacialTraitEffect.AtributoStep
        val resistente = RacialTraitPointCatalog.efeitoDe("MUITO_RESISTENTE") as RacialTraitEffect.AtributoStep
        assertEquals(2, forte.passos)
        assertEquals(2, resistente.passos)
    }

    @Test
    fun `efeitoDe retorna Nenhum pra tracos sem gancho mecanico numerico ou id desconhecido`() {
        // FORASTEIRO é só Complicação (sem alvo numérico modelado) — continua Nenhum.
        assertEquals(RacialTraitEffect.Nenhum, RacialTraitPointCatalog.efeitoDe("FORASTEIRO"))
        assertEquals(RacialTraitEffect.Nenhum, RacialTraitPointCatalog.efeitoDe(null))
        assertEquals(RacialTraitEffect.Nenhum, RacialTraitPointCatalog.efeitoDe("ID_QUE_NAO_EXISTE"))
    }

    @Test
    fun `efeitoDe retorna bonus fixo de Resistencia Passo e Aparar`() {
        // FRAGIL e APARAR_BAIXO são EMPILHÁVEIS (ver VEZES_MAX): o valor aqui
        // é sempre o de UMA compra só — "FRAGIL_MAIOR" não existe mais como id
        // próprio, virou FRAGIL com vezes=2 (ver labelComVezes abaixo).
        assertEquals(RacialTraitEffect.ResistenciaBonus(-1), RacialTraitPointCatalog.efeitoDe("FRAGIL"))
        assertEquals(RacialTraitEffect.ResistenciaBonus(2), RacialTraitPointCatalog.efeitoDe("MORTO_VIVO"))
        assertEquals(RacialTraitEffect.ResistenciaBonus(3), RacialTraitPointCatalog.efeitoDe("METADE_CONSTRUTO"))
        assertEquals(RacialTraitEffect.PassoBonus(-1), RacialTraitPointCatalog.efeitoDe("LENTO"))
        assertEquals(RacialTraitEffect.ApararBonus(-1), RacialTraitPointCatalog.efeitoDe("APARAR_BAIXO"))
    }

    @Test
    fun `traços empilháveis escalam efeito e rótulo por vezes`() {
        assertEquals(3, RacialTraitPointCatalog.vezesMaxDe("RESISTENCIA"))
        assertEquals(2, RacialTraitPointCatalog.vezesMaxDe("FRAGIL"))
        assertEquals(1, RacialTraitPointCatalog.vezesMaxDe("MORTO_VIVO")) // não empilhável

        assertEquals("Resistência +2", RacialTraitPointCatalog.labelComVezes("RESISTENCIA", 2))
        // FRAGIL também é ResistenciaBonus (por baixo dos panos): o rótulo
        // final mostra o alvo real do efeito (Resistência), não o nome do
        // traço — mesma convenção usada pra RESISTENCIA acima.
        assertEquals("Resistência -2", RacialTraitPointCatalog.labelComVezes("FRAGIL", 2))
        assertEquals("Aparar -3", RacialTraitPointCatalog.labelComVezes("APARAR_BAIXO", 3))
        assertEquals("Armadura +6", RacialTraitPointCatalog.labelComVezes("ARMADURA", 3))
    }

    @Test
    fun `placeholders de selecao tem custo zero`() {
        assertEquals(0, RacialTraitPointCatalog.custoDe("DONS_DA_NATUREZA"))
        assertEquals(0, RacialTraitPointCatalog.custoDe("SIGNOS_DE_NASCENCA"))
    }

    @Test
    fun `todo id usado em ancestralidades tem entrada no catalogo`() {
        // Assinatura mínima de sanidade: nenhum custo positivo nem negativo
        // extrapola a escala documentada (-4..8, mesma faixa do catálogo
        // oficial — Construto e Morto-Vivo chegam a 8).
        RacialTraitPointCatalog.CUSTOS.values.forEach { custo ->
            assertTrue("custo $custo fora da escala documentada", custo in -4..8)
        }
    }
}
