package com.example.swadebuilder.model

object ArcaneConfig {

    // Deadlands
    val DEADLANDS_ABENCOADO = setOf(
        "adivinhacao", "alivio", "amigo_feras", "amortecer", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "consagrar", "curar", "deflexao", "detectar_ocultar_arcano", "devastacao",
        "dissipar", "empatia", "falar_idioma", "golpear", "guerreiro", "iluminar",
        "intimidar", "manipular_elementos", "protecao", "protecao_ambiental",
        "protecao_arcana", "ressurreicao", "simbolo_sagrado", "socorro", "vendaval"
    )

    val DEADLANDS_XAMA = setOf(
        "adivinhacao", "amigo_feras", "armadura", "aumentar_reduzir_caracteristica",
        "busca_visao", "caminhar_ermos", "cavar", "consagrar", "curar", "dissipar",
        "enredar", "falar_idioma", "golpear", "guerreiro", "intimidar",
        "manipular_elementos", "metamorfose", "mirar", "protecao", "protecao_ambiental",
        "rapidez", "socorro", "teleporte", "velocidade", "vendaval"
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
        "dissipar", "drenar_pontos_poder", "empatia", "falar_idioma", "golpear", "maldicao",
        "marionete", "medo", "protecao", "ressurreicao", "santuario", "sono", "zumbi"
    )

    fun getPermittedPowers(arcaneKey: String): Set<String>? {
        return when (arcaneKey) {
            "aa_abencoado" -> DEADLANDS_ABENCOADO
            "aa_xama" -> DEADLANDS_XAMA
            "aa_vigarista" -> DEADLANDS_HUCKSTER // "Mascate/Huckster" usually 'vigarista' in translation
            "aa_mestre_chi" -> DEADLANDS_MESTRE_CHI
            "aa_alquimista" -> FANTASIA_ALQUIMISTA
            "aa_bardo" -> FANTASIA_BARDO
            "aa_druida" -> FANTASIA_DRUIDA
            "aa_necromante" -> FANTASIA_NECROMANTE
            "aa_demonologista" -> HORROR_DEMONOLOGISTA
            "aa_voduista" -> HORROR_VODUISTA
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
}
