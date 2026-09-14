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

    // Meio-Demônios (Cidade do Sol a Vapor): mesmo Antecedente Arcano dos
    // Demônios de sangue puro, mas via o sistema normal de slots/PP (3
    // slots, sem o slot fixo extra de Disfarce Demoníaco que os Demônios de
    // sangue puro têm — ver CriadorState.isStageBasedArcanoVariant, que
    // tira aa_demonio_meio_demonio do modo por estágio incondicionalmente).
    // A versão diluída de Disfarce Demoníaco continua exigindo a Vantagem
    // separada "Disfarce Demoníaco (Estágio Experiente)" antes de poder ser
    // escolhida como poder normal — CriadorState.atendeRequisitoEspecialDePoderPorArcano()
    // usa o mapa abaixo pra isso, independente de estar em modo por estágio.
    val SOL_VAPOR_DEMONIO_MEIO_POWER_REQUIREMENTS = mapOf(
        "disfarce_demoniaco_meio_demonio" to "disfarce_demoniaco_experiente_meio"
    )

    // Lista de poderes do AA (Demônio), fora do modo por estágio (sistema
    // normal de slots): o livro "Antecedente Arcano" de Demônio é Magia
    // Negra (SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE) mais os poderes exclusivos
    // de demônio (SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE) — sem isso, como
    // o livro Cidade do Sol a Vapor mistura poderes de todos os Antecedentes
    // Arcanos dele (Milagres, Tecnomagia etc.) num único pool por origem,
    // getPermittedPowers(arcKey) == null deixaria vazar poderes de outros
    // Antecedentes (Ajuda, Cura, Ressurreição, Santuário, Sobrecarga...) pra
    // demônios/meio-demônios.
    val SOL_VAPOR_DEMONIO_ALLOWED_POWERS: Set<String> =
        SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE.keys + SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE.keys

    // Igual ao Demônio de sangue puro, exceto Disfarce Demoníaco "puro"
    // (slot fixo exclusivo de sangue puro) trocado pela versão diluída
    // (disfarce_demoniaco_meio_demonio, gated por
    // SOL_VAPOR_DEMONIO_MEIO_POWER_REQUIREMENTS).
    val SOL_VAPOR_DEMONIO_MEIO_ALLOWED_POWERS: Set<String> =
        (SOL_VAPOR_DEMONIO_ALLOWED_POWERS - "disfarce_demoniaco") + "disfarce_demoniaco_meio_demonio"

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

    // Explosão aparece listada tanto em Novato quanto em Experiente no texto
    // do livro (docs/swade_csv_livro_dos_mortais, linhas ~7148 e ~7201) — sem
    // nota explicando a repetição. Mantido como Novato (a ocorrência mais
    // cedo/permissiva) pra não restringir o poder além do que o texto sugere;
    // ver docs/reports/book_index/csv.md pra essa ressalva.
    val SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE = linkedMapOf(
        "ajuda" to "Novato",
        "andar_nas_paredes" to "Novato",
        "atordoar" to "Novato",
        "aumentar_reduzir_caracteristica" to "Novato",
        "cegar" to "Novato",
        "confusao" to "Novato",
        "deflexao" to "Novato",
        "devastacao" to "Novato",
        "detectar_ocultar_arcano" to "Novato",
        "enredar" to "Novato",
        "explosao" to "Novato",
        "iluminar_obscurecer" to "Novato",
        "medo" to "Novato",
        "protecao" to "Novato",
        "protecao_arcana" to "Novato",
        "raio" to "Novato",
        "som_silencio" to "Novato",
        "visao_sombria" to "Novato",
        "campo_de_dano" to "Experiente",
        "dissipar" to "Experiente",
        "ferir" to "Experiente",
        "invisibilidade" to "Experiente",
        "morosidade_velocidade" to "Experiente",
        "sono" to "Experiente",
        "visao_distante" to "Experiente",
        "limpeza_mental" to "Veterano",
        "sobrecarga" to "Veterano"
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


    // ── Deadlands ──────────────────────────────────────────────────────────
    // Todas as listas abaixo foram reconstruídas linha a linha contra
    // docs/swade_deadlands e docs/swade_deadlands_compendio (auditoria de
    // 2026). A versão anterior usava, em quase todos os casos, os ids
    // "aliases" de Pathfinder de cada poder (ex.: "guerreiro" em vez de
    // "dadiva_do_guerreiro") — como o pool de poderes de um personagem de
    // Deadlands é pré-filtrado pra origem DEADLANDS/BASICO antes do allow-
    // list entrar, esses ids nunca apareciam no pool pra começo de conversa:
    // a lista antiga não fazia absolutamente nada de útil.
    val DEADLANDS_ABENCOADO = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "cura", "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano",
        "devastacao", "dissipar", "empatia", "entorpecimento", "falar_idioma", "ferir",
        "iluminar_obscurecer", "manipulacao_elemental", "morosidade_velocidade",
        "protecao", "protecao_ambiental", "protecao_arcana", "ressurreicao",
        "santificar", "simbolo_sagrado_2"
    )

    val DEADLANDS_XAMA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes",
        "aumentar_reduzir_caracteristica", "banir", "caminhada_selvagem", "cavar",
        "cegar", "confusao", "conjurar_aliado", "crescimento_encolhimento", "cura",
        "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano", "devastacao",
        "disfarce", "dissipar", "drenar_pontos_de_poder", "empatia", "enredar",
        "entorpecimento", "falar_idioma", "ferir", "intangibilidade", "maldicao_3",
        "manipulacao_elemental", "medo", "morosidade_velocidade", "mudanca_de_forma",
        "protecao", "protecao_ambiental", "protecao_arcana", "ressurreicao",
        "santificar", "simbolo_sagrado_2", "sono", "teleporte", "visao_distante",
        "visao_sombria"
    )

    val DEADLANDS_HUCKSTER = setOf(
        "adivinhacao", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "barreira", "bugigangas_2", "campo_de_dano",
        "cegar", "confusao", "conjurar_aliado", "deflexao", "detectar_ocultar_arcano",
        "devastacao", "disfarce", "dissipar", "empatia", "enfeiticar_municao",
        "enredar", "entorpecimento", "falar_idioma", "fantoche", "iluminar_obscurecer",
        "ilusao", "intangibilidade", "invisibilidade", "leitura_de_objeto",
        "manipulacao_elemental", "medo", "morosidade_velocidade", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "rajada", "som_silencio",
        "sono", "telecinese", "teleporte", "visao_distante"
    )

    val DEADLANDS_MESTRE_CHI = setOf(
        "ajuda", "andar_nas_paredes", "aumentar_reduzir_caracteristica", "cavar",
        "cura", "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano",
        "empatia", "entorpecimento", "ferir", "maldicao_3", "morosidade_velocidade",
        "protecao", "protecao_ambiental", "protecao_arcana", "visao_distante",
        "visao_sombria"
    )

    // Livro (docs/swade_deadlands): allow-list explícita de 47 poderes, não um
    // "tudo menos 2" — o antigo DEADLANDS_CIENTISTA_LOUCO_BLOCKED (bloqueando
    // só consagrar/simbolo_sagrado, ambos ids de Pathfinder que nunca
    // apareceriam no pool de Deadlands de qualquer forma) deixava passar mais
    // de 30 poderes que o livro não permite pra Cientista Louco.
    val DEADLANDS_CIENTISTA_LOUCO = setOf(
        "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "barreira", "campo_de_dano", "cavar",
        "cegar", "confusao", "crescimento_encolhimento", "cura", "dadiva_do_guerreiro",
        "deflexao", "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "entorpecimento", "explosao",
        "falar_idioma", "fantoche", "ferir", "iluminar_obscurecer", "ilusao",
        "intangibilidade", "invisibilidade", "limpeza_mental", "manipulacao_elemental",
        "medo", "morosidade_velocidade", "protecao", "protecao_ambiental",
        "protecao_arcana", "raio", "rajada", "som_silencio", "sono", "telecinese",
        "teleporte", "visao_distante", "visao_sombria", "voar", "zumbi"
    )

    // Livro (docs/swade_deadlands_compendio): Bruxa (magia negra/Conjurar,
    // "Corrupção") e Vuduísmo (Fé, houngan/mambo) são dois Antecedentes
    // Arcanos DISTINTOS de Deadlands — a versão anterior reaproveitava
    // incorretamente a lista de Voduísta de Horror pra Bruxa de Deadlands, que
    // não é sequer o mesmo tema (e a Voduísta de Deadlands em si nunca tinha
    // lista nenhuma, caindo pra "sem restrição").
    val DEADLANDS_BRUXA = setOf(
        "adivinhacao", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "conjurar_aliado", "crescimento_encolhimento", "dadiva_do_guerreiro",
        "deflexao", "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "explosao", "fantoche",
        "iludir", "iluminar_obscurecer", "ilusao", "invisibilidade", "leitura_de_objeto",
        "limpeza_mental", "maldicao_3", "manipulacao_elemental", "medo",
        "morosidade_velocidade", "mudanca_de_forma", "pavor", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "som_silencio", "sono",
        "transformar", "visao_distante", "visao_sombria", "voar", "zumbi"
    )

    val DEADLANDS_VODUISTA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "aspecto_do_loa_rada", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "cura", "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano",
        "devastacao", "dissipar", "empatia", "falar_idioma", "fantoche", "ferir",
        "furia_do_loa_petro", "leitura_de_objeto", "maldicao_3", "medo",
        "morosidade_velocidade", "protecao", "protecao_arcana", "santificar",
        "simbolo_sagrado_2", "visao_sombria", "zumbi"
    )

    // ── Fantasia ───────────────────────────────────────────────────────────
    // Mesma auditoria/mesmo problema de origem que Deadlands: a versão
    // anterior usava sistematicamente os ids de Pathfinder em vez dos de
    // Fantasia (ex.: "guerreiro" em vez de "dadiva_do_guerreiro").
    val FANTASIA_ALQUIMISTA = setOf(
        "ajuda", "amigo_das_feras", "andar_nas_paredes",
        "aumentar_reduzir_caracteristica", "banir", "cegar", "confusao",
        "crescimento_encolhimento", "cura", "dadiva_do_guerreiro", "deflexao",
        "detectar_ocultar_arcano", "empatia", "enredar", "explosao", "falar_idioma",
        "fantoche", "ferir", "iluminar_obscurecer", "intangibilidade",
        "invisibilidade", "medo", "morosidade_velocidade", "mudanca_de_forma",
        "protecao", "protecao_ambiental", "rajada", "ressurreicao", "sono",
        "visao_distante", "visao_sombria", "voar"
    )

    val FANTASIA_BARDO = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "confusao", "cura",
        "dadiva_do_guerreiro", "detectar_ocultar_arcano", "dissipar",
        "drenar_pontos_de_poder", "elo_mental", "empatia", "falar_idioma", "fantoche",
        "ferir", "leitura_mental", "medo", "morosidade_velocidade", "protecao_arcana",
        "som_silencio", "sono"
    )

    val FANTASIA_DRUIDA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "barreira", "bencao", "campo_de_dano",
        "cavar", "conjurar_animal", "conjurar_item", "conjurar_monstro",
        "crescimento_encolhimento", "cura", "dadiva_do_guerreiro", "deflexao",
        "devastacao", "disfarce", "enredar", "ferir", "intervencao_mistica",
        "manipulacao_elemental", "medo", "morosidade_velocidade", "mudanca_de_forma",
        "protecao", "protecao_ambiental", "protecao_arcana", "raio", "santuario",
        "som_silencio", "visao_distante", "visao_sombria", "voar"
    )

    val FANTASIA_NECROMANTE = setOf(
        "adivinhacao", "ajuda", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "conjurar_mortovivo", "cura", "dadiva_do_guerreiro", "deflexao",
        "detectar_ocultar_arcano", "devastacao", "dissipar", "drenar_pontos_de_poder",
        "elo_mental", "empatia", "enredar", "ferir", "iluminar_obscurecer",
        "intangibilidade", "invisibilidade", "leitura_de_objeto", "leitura_mental",
        "limpeza_mental", "medo", "morosidade_velocidade", "protecao",
        "protecao_arcana", "raio", "ressurreicao", "som_silencio", "sono",
        "telecinese", "teleporte", "trancar_destrancar", "visao_distante",
        "visao_sombria", "voar", "zumbi"
    )

    // 9 Antecedentes Arcanos de Fantasia sem cobertura nenhuma até esta
    // auditoria (caiam em "sem restrição", mostrando o pool inteiro do
    // livro). Clérigo fica de fora de propósito — o livro (l.6622-6626) diz
    // pra escolher um Domínio, já tratado à parte via fantasia_dominios.json.
    val FANTASIA_DIABOLISTA = setOf(
        "adivinhacao", "andar_nas_paredes", "aumentar_reduzir_caracteristica",
        "banir", "barreira", "campo_de_dano", "cegar", "confusao", "conjurar_aliado",
        "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano", "devastacao",
        "disfarce", "dissipar", "drenar_pontos_de_poder", "enredar", "explosao",
        "falar_idioma", "fantoche", "ferir", "iluminar_obscurecer", "ilusao",
        "localizar", "maldicao", "manipulacao_elemental", "medo",
        "morosidade_velocidade", "protecao", "protecao_ambiental", "protecao_arcana",
        "raio", "rajada", "som_silencio", "telecinese", "teleporte",
        "trancar_destrancar", "viagem_planar", "videncia", "visao_distante",
        "visao_sombria", "voar", "zumbi"
    )

    // Antecedente Arcano (Elementalista) de Fantasia — CHAVE COLIDE com o
    // sistema de Elementalista por Tropo da Arte da Guerra (mesmo
    // subtipoArcano normalizado "ELEMENTALISTA"). Resolvida por origem do
    // livro em getPermittedPowers(), não pela chave sozinha — ver ali.
    val FANTASIA_ELEMENTALISTA = setOf(
        "adivinhacao", "ajuda", "barreira", "campo_de_dano", "cavar", "confusao",
        "conjurar_monstro", "cura", "deflexao", "devastacao", "enredar", "explosao",
        "manipulacao_elemental", "protecao", "protecao_ambiental", "raio", "rajada",
        "telecinese", "viagem_planar", "voar"
    )

    val FANTASIA_ENGENHOQUEIRO = setOf(
        "andar_nas_paredes", "atordoar", "campo_de_dano", "cegar", "confusao",
        "detectar_ocultar_arcano", "enredar", "explosao", "iluminar_obscurecer",
        "protecao_ambiental", "raio", "rajada", "sono", "trancar_destrancar",
        "visao_distante", "visao_sombria", "voar"
    )

    val FANTASIA_FEITICEIRO = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "bencao",
        "campo_de_dano", "cavar", "cegar", "confusao", "conjurar_aliado",
        "conjurar_animal", "conjurar_monstro", "cura", "dadiva_do_guerreiro",
        "deflexao", "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "explosao", "falar_idioma",
        "ferir", "intervencao_mistica", "leitura_de_objeto", "manipulacao_elemental",
        "medo", "morosidade_velocidade", "mudanca_de_forma", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "rajada", "ressurreicao",
        "som_silencio", "sono", "visao_distante", "visao_sombria"
    )

    // Livro (l.7195-7211): de propósito o mais curto de Fantasia.
    val FANTASIA_ILUSIONISTA = setOf(
        "confusao", "deflexao", "detectar_ocultar_arcano", "disfarce",
        "iluminar_obscurecer", "ilusao", "invisibilidade", "medo", "som_silencio"
    )

    val FANTASIA_INVOCADOR = setOf(
        "andar_nas_paredes", "aumentar_reduzir_caracteristica", "cavar",
        "conjurar_aliado", "conjurar_animal", "conjurar_monstro",
        "crescimento_encolhimento", "cura", "detectar_ocultar_arcano", "localizar",
        "morosidade_velocidade", "mudanca_de_forma", "protecao_arcana",
        "visao_distante", "visao_sombria", "voar"
    )

    // Mago (l.7317-7357): "a maior seleção de magias de todos os
    // Antecedentes Arcanos" — de propósito o mais amplo, ao lado de Bruxo.
    val FANTASIA_MAGO = setOf(
        "amigo_das_feras", "ancora_planar", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "barreira", "campo_de_dano", "cavar",
        "cegar", "confusao", "conjurar_aliado", "conjurar_item",
        "crescimento_encolhimento", "dadiva_do_guerreiro", "deflexao", "desejo",
        "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "explosao", "falar_idioma",
        "fantoche", "iluminar_obscurecer", "ilusao", "intangibilidade",
        "intervencao_mistica", "invisibilidade", "leitura_de_objeto", "leitura_mental",
        "limpeza_mental", "localizar", "maldicao", "manipulacao_elemental", "medo",
        "morosidade_velocidade", "mudanca_de_forma", "parar_o_tempo", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "rajada", "sono", "ferir",
        "telecinese", "teleporte", "trancar_destrancar", "viagem_planar", "videncia",
        "visao_distante", "visao_sombria", "voar", "zumbi"
    )

    // Xamã de Fantasia (subtipoArcano "XAMA_FANTASIA") — distinto do Xamã de
    // Deadlands (DEADLANDS_XAMA, subtipoArcano "XAMA"); só 25 dos 44 poderes
    // se sobrepõem, então cada um precisa mesmo da própria lista.
    val FANTASIA_XAMA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "bencao",
        "campo_de_dano", "cavar", "cegar", "confusao", "conjurar_animal",
        "conjurar_monstro", "cura", "deflexao", "dadiva_do_guerreiro",
        "detectar_ocultar_arcano", "disfarce", "dissipar", "drenar_pontos_de_poder",
        "empatia", "enredar", "explosao", "falar_idioma", "ferir",
        "intervencao_mistica", "leitura_de_objeto", "manipulacao_elemental", "medo",
        "devastacao", "morosidade_velocidade", "mudanca_de_forma", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "rajada", "ressurreicao",
        "som_silencio", "sono", "visao_distante", "visao_sombria"
    )

    val FANTASIA_BRUXO = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "bencao", "cavar",
        "cegar", "confusao", "conjurar_aliado", "conjurar_animal", "conjurar_item",
        "crescimento_encolhimento", "cura", "dadiva_do_guerreiro", "deflexao",
        "desejo", "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "explosao", "falar_idioma",
        "fantoche", "ferir", "iluminar_obscurecer", "ilusao", "intervencao_mistica",
        "invisibilidade", "leitura_de_objeto", "leitura_mental", "limpeza_mental",
        "localizar", "maldicao", "manipulacao_elemental", "medo",
        "morosidade_velocidade", "mudanca_de_forma", "protecao", "protecao_ambiental",
        "protecao_arcana", "raio", "rajada", "som_silencio", "sono", "telecinese",
        "trancar_destrancar", "videncia", "visao_distante", "visao_sombria", "voar"
    )

    // ── Horror ─────────────────────────────────────────────────────────────
    // Demonologista e Voduísta reconstruídos linha a linha (a versão
    // anterior misturava ids de Pathfinder que não existem pra Horror com
    // ids inválidos/typos — "código morto sobre código morto"). Os outros 6
    // (+ Corrompido, sem restrição por decisão explícita do livro) nunca
    // tinham cobertura nenhuma.
    val HORROR_DEMONOLOGISTA = setOf(
        "adivinhacao", "andar_nas_paredes", "banir", "barreira", "campo_de_dano",
        "conjurar_aliado", "conjurar_aliado_3", "conjurar_demonio", "consagrar_solo",
        "detectar_ocultar_arcano", "devastacao", "dissipar", "drenar_pontos_de_poder",
        "exorcismo", "explosao", "falar_idioma", "fantoche", "ferir",
        "horrores_ilusorios", "iluminar_obscurecer", "ilusao", "maldicao_2", "medo",
        "pesadelos", "protecao_ambiental", "protecao_arcana", "raio", "rancor",
        "rajada", "sessao_espirita", "visao_sombria"
    )

    val HORROR_VODUISTA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "aspecto_de_loa_rada", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cegar", "confusao",
        "consagrar_solo", "cura", "dadiva_do_guerreiro", "deflexao",
        "detectar_ocultar_arcano", "devastacao", "dissipar", "empatia", "exorcismo",
        "falar_idioma", "fantoche", "ferir", "furia_de_loa_petro", "horrores_ilusorios",
        "leitura_de_objeto", "localizar_3", "maldicao_2", "medo",
        "morosidade_velocidade", "pesadelos", "protecao", "protecao_arcana",
        "santuario", "santuario_2", "sessao_espirita", "sentir_cadaver",
        "visao_sombria", "zumbi"
    )

    val HORROR_ALQUIMISTA = setOf(
        "ajuda", "amigo_das_feras", "andar_nas_paredes", "aumentar_reduzir_caracteristica",
        "banir", "cegar", "confusao", "crescimento_encolhimento", "cura",
        "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano", "empatia",
        "enredar", "explosao", "falar_idioma", "fantoche", "ferir",
        "iluminar_obscurecer", "intangibilidade", "invisibilidade", "medo",
        "morosidade_velocidade", "mudanca_de_forma", "protecao", "protecao_ambiental",
        "rajada", "ressurreicao", "sono", "visao_distante", "visao_sombria", "voar"
    )

    // Antecedente Arcano (Bruxaria): livro descreve como o "amplo leque" de
    // Horror — de propósito o mais amplo desse livro.
    val HORROR_BRUXARIA = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "cavar", "cegar",
        "confusao", "conjurar_aliado", "conjurar_aliado_3", "conjurar_demonio",
        "crescimento_encolhimento", "cura", "dadiva_do_guerreiro", "deflexao",
        "detectar_ocultar_arcano", "devastacao", "disfarce", "dissipar",
        "drenar_pontos_de_poder", "empatia", "enredar", "explosao", "exorcismo",
        "falar_idioma", "fantoche", "ferir", "horrores_ilusorios", "iluminar_obscurecer",
        "ilusao", "invisibilidade", "leitura_de_objeto", "leitura_mental",
        "limpeza_mental", "localizar_3", "manipulacao_elemental", "maldicao_2", "medo",
        "morosidade_velocidade", "mortalha_da_cova", "mudanca_de_forma", "pesadelos",
        "protecao", "protecao_ambiental", "protecao_arcana", "rajada", "rancor",
        "raio", "sentir_cadaver", "sessao_espirita", "som_silencio", "sono",
        "suprimir_transformacao", "telecinese", "trancar_destrancar_3", "videncia_3",
        "visao_distante", "visao_sombria", "voar"
    )

    // Nota: subtipoArcano "CLERIGO" colide (após normalização de acento) com
    // o Antecedente Arcano (Clérigo) de Fantasia, mas isso é inofensivo — o
    // Clérigo de Fantasia já é interceptado ANTES do allow-list por um caso
    // especial em PoderesSection.kt (compendioFantasiaAtivo && arcKey ==
    // "CLERIGO" -> filtro por Domínio, ignora getPermittedPowers por
    // completo), e os dois livros nunca ficam ativos ao mesmo tempo.
    val HORROR_CLERIGO = setOf(
        "adivinhacao", "ajuda", "amigo_das_feras", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "confusao",
        "consagrar_solo", "cura", "dadiva_do_guerreiro", "deflexao",
        "detectar_ocultar_arcano", "devastacao", "dissipar", "empatia", "enredar",
        "exorcismo", "falar_idioma", "ferir", "iluminar_obscurecer",
        "morosidade_velocidade", "protecao", "protecao_ambiental", "protecao_arcana",
        "ressurreicao", "santuario", "santuario_2", "som_silencio", "sono",
        "videncia_3", "visao_distante", "visao_sombria"
    )

    val HORROR_INVESTIGADOR_PSIQUICO = setOf(
        "adivinhacao", "amigo_das_feras", "aumentar_reduzir_caracteristica", "cegar",
        "confusao", "elo_mental", "empatia", "fantoche", "horrores_ilusorios",
        "leitura_de_objeto", "leitura_mental", "limpeza_mental", "localizar_3",
        "maldicao_2", "medo", "som_silencio", "sono", "suprimir_transformacao",
        "videncia_3"
    )

    val HORROR_MEDIUM = setOf(
        "adivinhacao", "amigo_das_feras", "banir", "consagrar_solo",
        "detectar_ocultar_arcano", "dissipar", "empatia", "exorcismo", "falar_idioma",
        "leitura_de_objeto", "localizar_3", "mortalha_da_cova", "protecao_arcana",
        "santuario", "santuario_2", "sentir_cadaver", "sessao_espirita", "videncia_3",
        "visao_sombria"
    )

    val HORROR_OCULTISTA = setOf(
        "adivinhacao", "amigo_das_feras", "andar_nas_paredes", "atordoar",
        "aumentar_reduzir_caracteristica", "banir", "barreira", "campo_de_dano",
        "cegar", "confusao", "conjurar_aliado", "conjurar_aliado_3", "conjurar_demonio",
        "dadiva_do_guerreiro", "deflexao", "detectar_ocultar_arcano", "devastacao",
        "disfarce", "dissipar", "drenar_pontos_de_poder", "empatia", "enredar",
        "exorcismo", "explosao", "falar_idioma", "fantoche", "ferir",
        "iluminar_obscurecer", "ilusao", "intangibilidade", "invisibilidade",
        "leitura_de_objeto", "localizar_3", "maldicao_2", "manipulacao_elemental",
        "medo", "morosidade_velocidade", "mortalha_da_cova", "pesadelos", "protecao",
        "protecao_ambiental", "protecao_arcana", "raio", "rajada", "rancor",
        "santuario", "santuario_2", "sentir_cadaver", "sessao_espirita", "som_silencio",
        "sono", "suprimir_transformacao", "telecinese", "teleporte",
        "trancar_destrancar_3", "videncia_3", "visao_distante", "visao_sombria",
        "zumbi"
    )

    val HORROR_VIDENTE = setOf(
        "adivinhacao", "deflexao", "detectar_ocultar_arcano", "empatia",
        "leitura_de_objeto", "localizar_3", "protecao", "sessao_espirita",
        "videncia_3", "visao_distante", "visao_sombria"
    )

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

    // IMPORTANTE: a chave recebida aqui NUNCA é o id do catálogo (ex.:
    // "antecedente_arcano_abencoado") — é `Vantagem.subtipoArcano` já normalizado
    // por `normAAKey()` (maiúsculo, sem acento) em ArcanoExtensions.kt/toArcanoKey().
    // Os `when` abaixo usavam chaves no formato do id ("aa_abencoado", "aa_bardo"
    // etc.), que nunca batem com o valor normalizado real (ex.: "ABENCOADO",
    // "BARDO") — toda essa restrição de lista de poderes por Antecedente Arcano
    // ficava morta, e o personagem via a lista cheia do livro (todos os AAs
    // daquele cenário misturados) em vez da lista correta do seu próprio AA.
    //
    // `origem` (o campo `Vantagem.origem`/tag de livro, ex.: "FANTASIA",
    // "HORROR", "DEADLANDS") desambigua as poucas chaves normalizadas que
    // colidem entre dois Antecedentes Arcanos de LIVROS DIFERENTES — como só
    // um livro fica ativo por vez (TelaInicial.kt trava a seleção), a origem
    // da Vantagem realmente selecionada sempre identifica corretamente qual
    // delas usar:
    // - "ALQUIMIA": Fantasia (Alquimista) x Horror (Alquimista) — mesmo nome,
    //   listas de poderes diferentes.
    // - "ELEMENTALISTA": Fantasia (Antecedente Arcano real) x sistema de
    //   Elementalista por Tropo da Arte da Guerra (sem Vantagem
    //   correspondente — chega aqui com origem nula, cai no default/ADG).
    // - "MESTRE DO CHI": só Deadlands tem Antecedente Arcano com esse nome;
    //   o Mestre do Chi por Tropo da Arte da Guerra (sem Vantagem, origem
    //   nula) não deve herdar a lista de Deadlands — por isso exige
    //   origem == "DEADLANDS" explicitamente em vez de cair no default.
    fun getPermittedPowers(arcaneKey: String, origem: String? = null): Set<String>? {
        if (arcaneKey == "ALQUIMIA" && origem == "HORROR") return HORROR_ALQUIMISTA
        if (arcaneKey == "ELEMENTALISTA" && origem == "FANTASIA") return FANTASIA_ELEMENTALISTA
        if (arcaneKey == "MESTRE DO CHI" && origem != "DEADLANDS") return null

        return when (arcaneKey) {
            "ABENCOADO" -> DEADLANDS_ABENCOADO
            "XAMA" -> DEADLANDS_XAMA
            "MASCATE" -> DEADLANDS_HUCKSTER
            "MESTRE DO CHI" -> DEADLANDS_MESTRE_CHI
            "CIENTISTA LOUCO" -> DEADLANDS_CIENTISTA_LOUCO
            "VODUISTA" -> DEADLANDS_VODUISTA
            "ALQUIMIA" -> FANTASIA_ALQUIMISTA
            "BARDO" -> FANTASIA_BARDO
            "DRUIDA" -> FANTASIA_DRUIDA
            "NECROMANTE" -> FANTASIA_NECROMANTE
            "DIABOLISTA" -> FANTASIA_DIABOLISTA
            "ENGENHOQUEIRO" -> FANTASIA_ENGENHOQUEIRO
            "FEITICEIRO" -> if (origem == "FANTASIA") FANTASIA_FEITICEIRO else null
            "ILUSIONISTA" -> FANTASIA_ILUSIONISTA
            "INVOCADOR" -> FANTASIA_INVOCADOR
            "MAGO" -> FANTASIA_MAGO
            "XAMA_FANTASIA" -> FANTASIA_XAMA
            "BRUXO" -> FANTASIA_BRUXO
            "INFERNAL" -> HORROR_DEMONOLOGISTA
            "VODU" -> HORROR_VODUISTA
            "FEITICARIA" -> HORROR_BRUXARIA
            "CLERIGO" -> HORROR_CLERIGO
            "INVESTIGADOR PSIQUICO" -> HORROR_INVESTIGADOR_PSIQUICO
            "ESPIRITUAL" -> HORROR_MEDIUM
            "OCULTISTA" -> HORROR_OCULTISTA
            "PRECOGNICAO" -> HORROR_VIDENTE
            // Corrompido (Horror): o próprio livro diz que qualquer poder pode
            // servir, à critério do jogador e do mestre — sem lista fixa, de
            // propósito (subtipoArcano "ABERRACAO", sem entrada aqui).
            "BRUXA" -> DEADLANDS_BRUXA
            "ELEMENTALISTA" -> ARTE_GUERRA_ELEMENTALISTA
            "DEMONIO" -> SOL_VAPOR_DEMONIO_ALLOWED_POWERS
            "DEMONIO_MEIO" -> SOL_VAPOR_DEMONIO_MEIO_ALLOWED_POWERS
            // Crystal Heart (livro, linhas ~4199-4201): o Antecedente Arcano (Canalizar
            // Cristal) não usa Pontos de Poder nem escolhe poderes livremente — todo o
            // acesso a poder vem exclusivamente do Coração de Cristal equipado
            // (crystal_coracoes.json, fora deste sistema). Sem essa entrada, a chave
            // caía no "sem restrição" e abria 1 slot de poder livre que o livro não tem.
            "CANALIZAR CRISTAL" -> emptySet()
            else -> null
        }
    }

    fun getStageBasedPowersByStage(arcaneKey: String): Map<String, String> {
        return when (arcaneKey) {
            "MILAGRES" -> SOL_VAPOR_MILAGRES_POWERS_BY_STAGE
            "FEITICEIRO" -> SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE
            "DEMONIO" -> SOL_VAPOR_FEITICEIRO_POWERS_BY_STAGE + SOL_VAPOR_DEMONIO_EXTRA_POWERS_BY_STAGE
            // DEMONIO_MEIO não usa mais o sistema por estágio (ver
            // CriadorState.isStageBasedArcanoVariant) — sem ramo aqui.
            "TECNOMAGIA" -> SOL_VAPOR_TECNOMAGIA_POWERS_BY_STAGE
            // Anjos usam a mesma lista de poderes dos Abençoados (Milagres),
            // mas sem precisar de Guerreiro do Senhor/Ira do Senhor pra usar
            // os poderes de combate — por isso NÃO há entrada correspondente
            // em getStageBasedPowerRequirement() abaixo pra "ANJO": o default
            // (null = sem exigência) já reproduz essa regra do livro.
            "ANJO" -> SOL_VAPOR_MILAGRES_POWERS_BY_STAGE
            else -> emptyMap()
        }
    }

    fun getStageBasedPowerRequirement(arcaneKey: String, powerId: String): String? {
        return when (arcaneKey) {
            "MILAGRES" -> SOL_VAPOR_MILAGRES_POWER_REQUIREMENTS[powerId]
            "DEMONIO_MEIO" -> SOL_VAPOR_DEMONIO_MEIO_POWER_REQUIREMENTS[powerId]
            else -> null
        }
    }
}
