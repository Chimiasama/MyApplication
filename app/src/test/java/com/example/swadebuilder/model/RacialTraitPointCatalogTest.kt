package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RacialTraitPointCatalogTest {

    @Test
    fun `custoDe eh case e acento insensivel via keyify`() {
        assertEquals(-1, RacialTraitPointCatalog.custoDe("fragil"))
        assertEquals(-1, RacialTraitPointCatalog.custoDe("FRAGIL"))
        assertEquals(2, RacialTraitPointCatalog.custoDe("cabecada"))
    }

    @Test
    fun `custoDe retorna zero pra id desconhecido ou nulo`() {
        assertEquals(0, RacialTraitPointCatalog.custoDe(null))
        assertEquals(0, RacialTraitPointCatalog.custoDe("ID_QUE_NAO_EXISTE"))
    }

    @Test
    fun `tamanho mais e menos 1 tem sinais opostos apos a correcao da colisao de id`() {
        assertEquals(2, RacialTraitPointCatalog.custoDe("TAMANHO_MAIS_1"))
        assertEquals(-1, RacialTraitPointCatalog.custoDe("TAMANHO_MENOS_1"))
    }

    @Test
    fun `cabecada e cabeca dura sao traços diferentes com custos diferentes`() {
        assertEquals(2, RacialTraitPointCatalog.custoDe("CABECADA"))
        assertEquals(-1, RacialTraitPointCatalog.custoDe("CABECA_DURA"))
    }

    @Test
    fun `placeholders de selecao tem custo zero`() {
        assertEquals(0, RacialTraitPointCatalog.custoDe("DONS_DA_NATUREZA"))
        assertEquals(0, RacialTraitPointCatalog.custoDe("SIGNOS_DE_NASCENCA"))
    }

    @Test
    fun `todo id usado em ancestralidades tem entrada no catalogo`() {
        // Assinatura mínima de sanidade: nenhum custo positivo nem negativo
        // extrapola a escala documentada (-4..4).
        RacialTraitPointCatalog.CUSTOS.values.forEach { custo ->
            assertTrue("custo $custo fora da escala documentada", custo in -4..4)
        }
    }
}
