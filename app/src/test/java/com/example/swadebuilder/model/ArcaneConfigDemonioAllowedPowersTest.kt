package com.example.swadebuilder.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O livro Cidade do Sol a Vapor mistura os poderes de TODOS os Antecedentes
 * Arcanos dele (Feiticeiro, Demônio, Milagres, Tecnomagia...) num único pool
 * por origem (ver poderes.json, livros: ["CIDADE_SOL_VAPOR"]). Sem uma
 * permittedSet própria, ArcaneConfig.getPermittedPowers("DEMONIO"/
 * "DEMONIO_MEIO") retornava null e PoderesSection.kt deixava vazar poderes
 * de outros Antecedentes (Ajuda, Cura, Ressurreição, Santuário, Sobrecarga)
 * pra demônios/meio-demônios — achado pelo usuário testando o app. Estes
 * testes fixam a lista correta: Magia Negra (Feiticeiro) + poderes
 * exclusivos de demônio, com a troca de Disfarce Demoníaco puro pela versão
 * diluída no caso do Meio-Demônio.
 */
class ArcaneConfigDemonioAllowedPowersTest {

    @Test
    fun `demonio de sangue puro so ve magia negra mais os poderes exclusivos de demonio`() {
        val allowed = ArcaneConfig.getPermittedPowers("DEMONIO")!!

        // Magia Negra (Feiticeiro)
        assertTrue(allowed.contains("raio"))
        assertTrue(allowed.contains("invisibilidade"))
        assertTrue(allowed.contains("adivinhacao"))

        // Exclusivos de demônio
        assertTrue(allowed.contains("disfarce_demoniaco"))
        assertTrue(allowed.contains("elo_mental_demonio"))
        assertTrue(allowed.contains("telecinese_demonio"))
        assertTrue(allowed.contains("voar_demonio"))
        assertTrue(allowed.contains("leitura_mental_demonio"))
        assertTrue(allowed.contains("limpeza_mental_demonio"))
        assertTrue(allowed.contains("drenar_pontos_de_poder_demonio"))

        // Nunca a versão diluída (essa é só do Meio-Demônio)
        assertFalse(allowed.contains("disfarce_demoniaco_meio_demonio"))

        // Poderes de outros Antecedentes do mesmo livro não vazam
        assertFalse(allowed.contains("ajuda"))
        assertFalse(allowed.contains("cura"))
        assertFalse(allowed.contains("ressurreicao"))
        assertFalse(allowed.contains("santuario"))
        assertFalse(allowed.contains("sobrecarga"))
        assertFalse(allowed.contains("dadiva_do_guerreiro"))
    }

    @Test
    fun `meio-demonio ve os mesmos poderes, exceto disfarce demoniaco puro trocado pela versao diluida`() {
        val allowed = ArcaneConfig.getPermittedPowers("DEMONIO_MEIO")!!

        assertTrue(allowed.contains("raio"))
        assertTrue(allowed.contains("elo_mental_demonio"))
        assertTrue(allowed.contains("voar_demonio"))

        assertFalse(allowed.contains("disfarce_demoniaco"))
        assertTrue(allowed.contains("disfarce_demoniaco_meio_demonio"))

        assertFalse(allowed.contains("ajuda"))
        assertFalse(allowed.contains("cura"))
        assertFalse(allowed.contains("sobrecarga"))
    }
}
