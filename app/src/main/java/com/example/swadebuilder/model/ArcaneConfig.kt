package com.example.swadebuilder.model

object ArcaneConfig {
    val SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE = linkedMapOf(
        "andar_nas_paredes" to "Novato",
        "atordoar" to "Novato",
        "cegar" to "Novato",
        "confusao" to "Novato",
        "deflexao" to "Novato",
        "detectar_ocultar_arcano" to "Novato",
        "devastacao" to "Novato",
        "enredar" to "Novato",
        "ferir" to "Novato",
        "ilusao" to "Novato",
        "medo" to "Novato",
        "iluminar_obscurecer" to "Novato",
        "protecao_arcana" to "Novato",
        "protecao" to "Novato",
        "raio" to "Novato",
        "rajada" to "Novato",
        "aumentar_reduzir_caracteristica" to "Novato",
        "som_silencio" to "Novato",
        "visao_sombria" to "Novato",
        "campo_de_dano" to "Experiente",
        "disfarce" to "Experiente",
        "explosao" to "Experiente",
        "invisibilidade" to "Experiente",
        "morosidade_velocidade" to "Experiente",
        "sono" to "Experiente",
        "visao_distante" to "Experiente",
        "fantoche" to "Veterano",
        "limpeza_mental" to "Veterano",
        "adivinhacao" to "Heroico",
        "intangibilidade" to "Heroico"
    )

    val SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE = linkedMapOf(
        "disfarce_demoniaco" to "Novato",
        "elo_mental_demonio" to "Novato",
        "telecinese_demonio" to "Experiente",
        "voar_demonio" to "Veterano",
        "leitura_mental_demonio" to "Veterano",
        "limpeza_mental_demonio" to "Veterano",
        "drenar_pontos_de_poder_demonio" to "Heroico"
    )

    val SOL_VAPOR_MILAGRES_POWERS_BY_STAGE = linkedMapOf(
        "ajuda" to "Novato",
        "aumentar_reduzir_caracteristica" to "Novato",
        "cura" to "Novato",
        "deflexao" to "Novato",
        "detectar_ocultar_arcano" to "Novato",
        "enredar" to "Novato",
        "iluminar_obscurecer" to "Novato",
        "protecao_ambiental" to "Novato",
        "protecao_arcana" to "Novato",
        "protecao" to "Novato",
        "som_silencio" to "Novato",
        "visao_sombria" to "Novato",
        "atordoar" to "Experiente",
        "campo_de_dano" to "Experiente",
        "cegar" to "Experiente",
        "confusao" to "Experiente",
        "devastacao" to "Experiente",
        "dissipar" to "Experiente",
        "invisibilidade" to "Experiente",
        "morosidade_velocidade" to "Experiente",
        "raio" to "Experiente",
        "sono" to "Experiente",
        "dadiva_do_guerreiro" to "Veterano",
        "explosao" to "Veterano",
        "ferir" to "Veterano",
        "medo" to "Veterano",
        "rajada" to "Veterano",
        "adivinhacao" to "Heroico",
        "ressurreicao" to "Heroico"
    )

    val SOL_VAPOR_MILAGRES_POWER_REQUIREMENTS = mapOf(
        "atordoar" to "guerreiro_do_senhor",
        "campo_de_dano" to "guerreiro_do_senhor",
        "cegar" to "guerreiro_do_senhor",
        "confusao" to "guerreiro_do_senhor",
        "devastacao" to "guerreiro_do_senhor",
        "raio" to "guerreiro_do_senhor",
        "dadiva_do_guerreiro" to "ira_do_senhor",
        "explosao" to "ira_do_senhor",
        "ferir" to "ira_do_senhor",
        "medo" to "ira_do_senhor",
        "rajada" to "ira_do_senhor"
    )


    // Deadlands
    val DEADLANDS_ABENCOADO = setOf(
        "adivinhacao", "alivio", "amigo_feras", "amortecer", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "consagrar", "curar", "deflexao", "detectar_ocultar_arcano", "devastacao",
        "dissipar", "empatia", "falar_idioma", "golpear", "guerreiro", "iluminar",
        "intimidar", "manipular_elementos", "protecao", "protecao_ambiental",
        "protecao_arcana", "ressurreicao", "simbolo_sagrado", "socorro", "vendaval",
        "busca_visao", "caminhar_ermos"
    )

    val DEADLANDS_XAMA = setOf(
        "adivinhacao", "amigo_feras", "armadura", "aumentar_reduzir_caracteristica",
        "busca_visao", "caminhar_ermos", "cavar", "consagrar", "curar", "dissipar",
        "enredar", "falar_idioma", "golpear", "guerreiro", "intimidar",
        "manipular_elementos", "metamorfose", "mirar", "protecao", "protecao_ambiental",
        "rapidez", "socorro", "teleporte", "velocidade", "vendaval",
        "simbolo_sagrado"
    )

    val DEADLANDS_HUCKSTER = setOf(
        "adivinhacao", "animacao", "aumentar_reduzir_caracteristica", "barreira",
        "bolso_dimensional", "bugigangas", "cegar", "confusao", "dano_campo",
        "deflexao", "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_poder", "elo_mental", "empatia", "furia", "ilusao",
        "intangibilidade", "intimidar", "invisibilidade", "jogatina", "leitura_mentes",
        "luz_escuridao", "manipular_elementos", "marionete", "mirar", "protecao_arcana",
        "raio", "rajada", "telecinese", "teleporte"
    )

    val DEADLANDS_MESTRE_CHI = setOf(
        "andar_paredes", "armadura", "aumentar_reduzir_caracteristica", "deflexao",
        "explosao", "golpear", "guerreiro", "mirar", "protecao_ambiental", "raio",
        "rapidez", "velocidade", "voo"
    )

    // Mad Scientist Logic: All EXCEPT "consagrar", "simbolo_sagrado"
    val DEADLANDS_CIENTISTA_LOUCO_BLOCKED = setOf("consagrar", "simbolo_sagrado")

    // Fantasy
    val FANTASIA_ALQUIMISTA = setOf(
        "alivio", "amigo_feras", "armadura", "aumentar_reduzir_caracteristica", "cegar",
        "crescimento_encolhimento", "curar", "disfarce", "dissipar", "drenar_pontos_poder",
        "enredar", "explosao", "falar_idioma", "ferir", "furia", "intangibilidade",
        "invisibilidade", "luz_escuridao", "metamorfose", "protecao_ambiental", "raio",
        "rajada", "rapidez", "teleporte", "velocidade", "voo", "zumbi"
    )

    val FANTASIA_BARDO = setOf(
        "adivinhacao", "alivio", "amigo_feras", "atordoar", "aumentar_reduzir_caracteristica",
        "cegar", "confusao", "curar", "deflexao", "detectar_ocultar_arcano", "disfarce",
        "dissipar", "drenar_pontos_poder", "elo_mental", "empatia", "falar_idioma",
        "guerreiro", "ilusao", "invisibilidade", "leitura_mentes", "luz_escuridao",
        "marionete", "medo", "peregrino", "protecao_arcana", "som", "sono"
    )

    val FANTASIA_DRUIDA = setOf(
        "amigo_feras", "armadura", "atordoar", "aumentar_reduzir_caracteristica",
        "barreira", "caminhar_ermos", "cavar", "crescimento_encolhimento", "curar",
        "detectar_ocultar_arcano", "dissipar", "enredar", "falar_idioma", "guerreiro",
        "luz_escuridao", "manipular_elementos", "metamorfose", "protecao_ambiental",
        "rajada", "ressurreicao", "som", "teleporte", "velocidade", "vendaval",
        "visao_no_escuro", "voo"
    )

    val FANTASIA_NECROMANTE = setOf(
        "adivinhacao", "armadura", "atordoar", "aumentar_reduzir_caracteristica", "banir",
        "barreira", "cegar", "confusao", "cova", "curar", "dano_campo",
        "detectar_ocultar_arcano", "devastacao", "disfarce", "drenar_pontos_poder",
        "enterrar", "escuridao", "falar_idioma", "furia", "intangibilidade", "intimidar",
        "leitura_mentes", "maldicao", "marionete", "raio", "ressurreicao", "zumbi"
    )

    // Horror
    val HORROR_DEMONOLOGISTA = setOf(
        "adivinhacao", "atordoar", "aumentar_reduzir_caracteristica", "banir", "barreira",
        "cegar", "convocar_aliado", "dano_campo", "deflexao", "detectar_ocultar_arcano",
        "devastacao", "drenar_pontos_poder", "entidade", "fogo_infernal", "invocar_demonio",
        "marionete", "medo", "protecao_arcana", "teleporte", "zumbi"
    )

    val HORROR_VODUISTA = setOf(
        "adivinhacao", "alivio", "amigo_feras", "atordoar", "aumentar_reduzir_caracteristica",
        "barreira", "confusao", "convocar_aliado", "curar", "detectar_ocultar_arcano",
        "dissipar", "drenar_pontos_poder", "empatia", "entidade", "falar_idioma", "golpear",
        "maldicao", "marionete", "medo", "protecao", "ressurreicao", "santuario", "sentir_cadaver",
        "sono", "zumbi"
    )

    val DEADLANDS_BRUXA = HORROR_VODUISTA

    val ARTE_GUERRA_ELEMENTALISTA = setOf(
        "manobra_armadura",
        "manobra_barreira",
        "manobra_raio",
        "manobra_cavar",
        "manobra_planar",
        "manobra_cura",
        "manobra_impacto",
        "manobra_nadar",
        "manobra_desequilibrar"
    )

    fun getPermittedPowers(arcaneKey: String): Set<String>? {
        return when (arcaneKey) {
            "aa_abencoado" -> DEADLANDS_ABENCOADO
            "aa_xama" -> DEADLANDS_XAMA
            "aa_vigarista", "aa_mascate" -> DEADLANDS_HUCKSTER
            "aa_mestre_chi" -> DEADLANDS_MESTRE_CHI
            "aa_alquimista" -> FANTASIA_ALQUIMISTA
            "aa_bardo" -> FANTASIA_BARDO
            "aa_druida" -> FANTASIA_DRUIDA
            "aa_necromante" -> FANTASIA_NECROMANTE
            "aa_demonologista" -> HORROR_DEMONOLOGISTA
            "aa_voduista" -> HORROR_VODUISTA
            "aa_bruxa" -> DEADLANDS_BRUXA
            "ELEMENTALISTA" -> ARTE_GUERRA_ELEMENTALISTA
            // Mad Scientist is special, returning null here to signify "check blocked" or handle differently
            else -> null
        }
    }

    fun getBlockedPowers(arcaneKey: String): Set<String> {
        return when (arcaneKey) {
            "aa_cientista_louco" -> DEADLANDS_CIENTISTA_LOUCO_BLOCKED
            else -> emptySet()
        }
    }

    fun getStageBasedPowersByStage(arcaneKey: String): Map<String, String> {
        return when (arcaneKey) {
            "MILAGRES" -> SOL_VAPOR_MILAGRES_POWERS_BY_STAGE
            "FEITICEIRO" -> SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE
            "DEMONIO" -> SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE + SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE
            else -> emptyMap()
        }
    }

    fun getStageBasedPowerRequirement(arcaneKey: String, powerId: String): String? {
        return when (arcaneKey) {
            "MILAGRES" -> SOL_VAPOR_MILAGRES_POWER_REQUIREMENTS[powerId]
            else -> null
        }
    }
}
