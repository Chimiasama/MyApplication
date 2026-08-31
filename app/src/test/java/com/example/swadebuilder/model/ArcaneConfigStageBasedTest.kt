package com.example.swadebuilder.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArcaneConfigStageBasedTest {

    @Test
    fun `tecnomagia usa a lista propria de poderes por estagio`() {
        val powers = ArcaneConfig.getStageBasedPowersByStage("TECNOMAGIA")

        assertEquals("Novato", powers["andar_nas_paredes"])
        assertEquals("Experiente", powers["ferir"])
        assertEquals("Veterano", powers["sobrecarga"])
        // Não é uma cópia da lista de Milagres/Feiticeiro: alguns poderes
        // dessas listas simplesmente não existem para tecnomagos.
        assertNull(powers["cura"])
        assertNull(powers["fantoche"])
    }

    @Test
    fun `anjo reutiliza a mesma lista de poderes dos abencoados`() {
        val anjo = ArcaneConfig.getStageBasedPowersByStage("ANJO")
        val milagres = ArcaneConfig.getStageBasedPowersByStage("MILAGRES")

        assertEquals(milagres, anjo)
    }

    @Test
    fun `anjo nao exige Guerreiro do Senhor nem Ira do Senhor para os poderes de combate`() {
        // O livro diz explicitamente que anjos usam sem essas Vantagens os
        // poderes que abençoados só destravam com Guerreiro do Senhor/Ira do
        // Senhor — só existe requirement map pra "MILAGRES", nunca pra "ANJO".
        assertNull(ArcaneConfig.getStageBasedPowerRequirement("ANJO", "atordoar"))
        assertNull(ArcaneConfig.getStageBasedPowerRequirement("ANJO", "explosao"))
        assertNull(ArcaneConfig.getStageBasedPowerRequirement("ANJO", "dadiva_do_guerreiro"))

        // Enquanto isso, os mesmos poderes continuam exigindo as Vantagens
        // quando o Antecedente é Milagres (Abençoado).
        assertEquals("guerreiro_do_senhor", ArcaneConfig.getStageBasedPowerRequirement("MILAGRES", "atordoar"))
        assertEquals("ira_do_senhor", ArcaneConfig.getStageBasedPowerRequirement("MILAGRES", "explosao"))
    }
}
