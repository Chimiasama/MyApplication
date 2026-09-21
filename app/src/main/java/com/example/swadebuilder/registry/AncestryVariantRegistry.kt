package com.example.swadebuilder.registry

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.ArmaNatural
import com.example.swadebuilder.model.FixedPackageOption
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.TraitTargetKind
import com.example.swadebuilder.model.VariantGroup
import com.example.swadebuilder.model.VariantOption

/**
 * Catálogo central de Variantes/Seleções por ancestralidade, indexado por id
 * estável (mesmo valor de `ancestralidade.keyify()` usado no resto do app).
 * Lote piloto: Terracota, Umvee, Elementais (Sci-Fi), Anões. Lote 2: as 19
 * raças Sci-Fi com Variante de 2 opções (Rakashanos, Sáurios, Aquarianos,
 * Avianos, Elfos, Humanos, Centaux, Drakens, Ferais, Florans, Gelatinoides,
 * Insetoides, Mímicos, Mineradores Genéticos, Oráculos, Possessores,
 * Quadroides, Soldados Genéticos, Yetis). Lote 3: Robôs e Seres Sintéticos —
 * confirmado contra ancestralidades.json que "Padrão" é uma opção real em
 * `opcoes` pras duas (não um `else` sem correspondência), então a
 * ambiguidade que tinha adiado esse par no lote 2 não existe de verdade.
 *
 * Uma raça ausente daqui simplesmente não tem variante nem seleção conhecida
 * pelo motor novo (ex.: a maioria das raças de Fantasia/Horror sem nenhuma
 * escolha embutida — só Adaptável ou um traço fixo, sem opção nenhuma).
 */
object AncestryVariantRegistry {

    private val configs: Map<String, AncestryVariantConfig> = listOf(
        terracota(),
        umvee(),
        elementaisScifi(),
        anoes(),
        rakashanos(),
        aquarianos(),
        avianos(),
        elfos(),
        humanos(),
        centaux(),
        drakens(),
        ferais(),
        florans(),
        gelatinoides(),
        insetoidesScifi(),
        mimicos(),
        mineradoresGeneticos(),
        oraculos(),
        possessores(),
        quadroides(),
        soldadosGeneticos(),
        yetis(),
        robos(),
        seresSinteticos(),
        descendenteElemental(),
        humanoFantasia(),
        meioElfoHeranca("BASICO"),
        meioElfoHeranca("FANTASIA"),
        meioElfoHeranca("HORROR"),
        meioElfoHeranca("SUPER"),
        meioDemonio(),
        humanoArteDaGuerraSignos(),
        meioOrc(),
        feralArteDaGuerra(),
        kitsunemimiArteDaGuerra(),
        gnomoPathfinder(),
        usagimimiArteDaGuerra()
    ).associateBy { configKey(it.livro, it.ancestralidadeId) }

    private fun configKey(livro: String, ancestralidadeId: String): String = "$livro::$ancestralidadeId"

    /**
     * Cada livro registra suas próprias Variantes — "HUMANOS" do Sci-Fi
     * (Baixa Gravidade/Minerador) e "HUMANOS" do Fantasia (Pacotes
     * Culturais) são raças diferentes que só compartilham o nome de
     * exibição, então `livro` é obrigatório pra desambiguar (ver comentário
     * de AncestryVariantConfig).
     */
    fun get(ancestralidadeId: String, livro: String): AncestryVariantConfig? = configs[configKey(livro, ancestralidadeId)]

    /**
     * Ids de ancestralidade cujo lote de Variante Sci-Fi (2 e 3, ver
     * comentário da classe) já foi migrado pra este registro — usado por
     * `ResolveAncestrySpecificAdjustmentsUseCase` e `CriadorState` pra
     * decidir se a raça resolve traços/habilidades por aqui em vez de pelos
     * blocos hardcoded antigos. Antes desta constante existiam duas cópias
     * manuais idênticas desta lista, uma em cada arquivo.
     *
     * Deliberadamente NÃO inclui o lote piloto (Terracota, Umvee, Elementais,
     * Anões): essas quatro raças também estão em `configs`, mas resolvem
     * traços/habilidades por um caminho diferente, anterior a este lote.
     */
    val scifiVariantDrivenKeys: Set<String> = setOf(
        "RAKASHANOS", "AQUARIANOS", "AVIANOS", "ELFOS", "HUMANOS",
        "CENTAUX", "DRAKENS", "FERAIS", "FLORANS", "GELATINOIDES", "INSETOIDES",
        "MIMICOS", "MINERADORES GENETICOS", "ORACULOS", "POSSESSORES",
        "QUADROIDES", "SOLDADOS GENETICOS", "YETIS", "ROBOS", "SERES SINTETICOS"
    )

    // --- Lote 2: raças Sci-Fi com Variante real de 2 opções (Básico/Padrão +
    // 1 reconfiguração de cenário), migradas de ResolveAncestrySpecificAdjustmentsUseCase
    // (bloco `if (isSciFiActive) { if (ancKey == "X") ... }`). Mesmo padrão do
    // lote piloto: cada opção carrega só as ADIÇÕES/REMOÇÕES de traços — a
    // Armadura Natural (quando difere de 0, ex.: Sáurios/Insetoides) segue
    // como exceção pontual na camada de wiring
    // (ResolveAncestrySpecificAdjustmentsUseCase), igual ao Umvee Pedregoso.

    private fun rakashanos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "RAKASHANOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "basico",
                    nome = "Básico",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("SANGUINÁRIO (Maior)", "SANGUINARIO_MAIOR"))
                    )
                ),
                VariantOption(
                    id = "brincalhao",
                    nome = "Brincalhão",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("CURIOSO (Maior)", "CURIOSO_MAIOR")),
                        desvantagensParaRemover = listOf("SANGUINÁRIO", "SANGUINÁRIO (Maior)")
                    )
                )
            )
        )
    )

    // sauriosScifi() removido: Sáurios não é raça do Sci-Fi Companion (só
    // Básico/Fantasia/Horror/Super) — a entrada "SAÚRIOS"/SCI_FI em
    // ancestralidades.json era um erro de cadastro (nome com acento errado,
    // "SAÚRIOS" em vez de "SÁURIOS"), removida junto com este config.

    private fun aquarianos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "AQUARIANOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(id = "basico", nome = "Básico", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "semi_aquaticos",
                    nome = "Semi-aquáticos",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("SEMIAQUÁTICO", "SEMIAQUATICO"),
                            TraitAddition("TOQUE VENENOSO", "TOQUE_VENENOSO")
                        ),
                        tracosParaRemoverPorNome = listOf("AQUÁTICO", "RESISTÊNCIA")
                    )
                )
            )
        )
    )

    private fun avianos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "AVIANOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "basico",
                    nome = "Básico",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(
                            TraitAddition("FRÁGIL", "FRAGIL"),
                            TraitAddition("NÃO SABE NADAR", "NAO_SABE_NADAR")
                        )
                    )
                ),
                VariantOption(
                    id = "ave_de_rapina",
                    nome = "Ave de rapina",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(
                            TraitAddition("HABITANTE DE GRAVIDADE ZERO/BAIXA", "HABITANTE_DE_GRAVIDADE_ZERO_BAIXA"),
                            TraitAddition("FORMA ALIENÍGENA", "FORMA_ALIENIGENA"),
                            // Id deliberadamente distinto de "SENTIDOS_AGUCADOS" (o
                            // aumento de Perceber do catálogo oficial): esta entrada
                            // já era listada como desvantagem no conteúdo original,
                            // não como o traço positivo — manter os dois ids
                            // diferentes preserva esse comportamento em vez de
                            // conceder um bônus de Perceber não pretendido aqui.
                            TraitAddition("SENTIDOS AGUÇADOS (Olhos de Águia)", "SENTIDOS_AGUCADOS_OLHOS_DE_AGUIA")
                        ),
                        tracosParaRemoverPorNome = listOf("FRÁGIL", "FRAGIL", "NÃO SABE NADAR", "NAO SABE NADAR"),
                        desvantagensParaRemover = listOf("NÃO SABE NADAR", "NÃO SABE NADAR (Menor)", "FRÁGIL")
                    )
                )
            )
        )
    )

    private fun elfos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ELFOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "basico",
                    nome = "Básico",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("DESASTRADO (Menor)", "DESASTRADO_MENOR"))
                    )
                ),
                VariantOption(
                    id = "comunitario",
                    nome = "Comunitário",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("COMUNITÁRIO", "COMUNITARIO")),
                        tracosParaRemoverPorNome = listOf("DESASTRADO"),
                        desvantagensParaAdicionar = listOf(TraitAddition("TRANSTORNO DE SEPARAÇÃO", "TRANSTORNO_DE_SEPARACAO")),
                        desvantagensParaRemover = listOf("DESASTRADO", "DESASTRADO (Menor)")
                    )
                )
            )
        )
    )

    private fun humanos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "HUMANOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "baixa_gravidade",
                    nome = "Baixa Gravidade",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaRemoverPorNome = listOf("ADAPTÁVEL", "ADAPTAVEL"),
                        desvantagensParaAdicionar = listOf(TraitAddition("HABITANTE DE GRAVIDADE BAIXA", "HABITANTE_DE_GRAVIDADE_BAIXA"))
                    )
                ),
                VariantOption(
                    id = "minerador",
                    nome = "Minerador",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("DEPENDÊNCIA ATMOSFÉRICA (Maior)", "DEPENDENCIA_ATMOSFERICA_MAIOR"))
                    ),
                    // Seleção aninhada (mesmo padrão de humanoFantasia() —
                    // VariantOption.selecoes): o jogador escolhe Força OU
                    // Vigor pro d6 inicial ("Planeta de Mineração"). Antes o
                    // marcador MINERADOR_ATRIBUTO só sinalizava a escolha,
                    // sem custo cadastrado (a raça toda não passava pelo
                    // validador de orçamento por opção); agora o traço
                    // resolvido (ATTRIBUTE_BOOST, 2 pts) entra de verdade em
                    // habilidades[] via CriadorState, mesmo mecanismo de
                    // Meio-Orc/Feral acima.
                    selecoes = listOf(
                        SelectionDef(
                            id = "humano_minerador_atributo",
                            rotulo = "Planeta de Mineração",
                            tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                            targetKind = TraitTargetKind.ATTRIBUTE,
                            targetOptions = listOf("Força", "Vigor"),
                            defaultTargetChoice = "Força",
                            injectionTemplate = "{alvo} d6 (Planeta de Mineração)"
                        )
                    )
                )
            )
        )
    )

    private fun centaux(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "CENTAUX",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        // Mesmos ids que já existem nativamente em
                        // ancestralidades.json pra Centaux — "Padrão" só
                        // reafirma o que a raça base já concede (existe pra
                        // "Gazela" poder trocar por 2x MOVIMENTACAO abaixo).
                        // TAMANHO_MAIS_1/MOVIMENTACAO são os traços empilháveis
                        // do livro (ver RacialTraitPointCatalog.VEZES_MAX) —
                        // "vezes" é quantas compras esta raça tem, não um id
                        // por valor final.
                        tracosParaAdicionar = listOf(
                            TraitAddition("TAMANHO +2", "TAMANHO_MAIS_1", vezes = 2),
                            TraitAddition("MOVIMENTAÇÃO +2", "MOVIMENTACAO", vezes = 1)
                        )
                    )
                ),
                VariantOption(
                    id = "gazela",
                    nome = "Gazela",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("MOVIMENTAÇÃO +4", "MOVIMENTACAO", vezes = 2)),
                        tracosParaRemoverPorNome = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                        desvantagensParaRemover = listOf("GRANDE")
                    )
                )
            )
        )
    )

    private fun drakens(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "DRAKENS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                // Padrão não adiciona nada além da raça base: Força d6 e
                // Resistência +2 já vêm de habilidades[] em ancestralidades.json
                // (FORTE/RESISTENCIA), então já estão presentes mesmo com o
                // compêndio de variantes do Sci-Fi desligado. Antes esta opção
                // somava os dois de novo por cima da base — dobrava a
                // Resistência (+4 em vez de +2) sempre que "Padrão" era
                // selecionado (o default quando o compêndio está ativo).
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage()
                ),
                VariantOption(
                    id = "dragao",
                    nome = "Dragão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("ARMA DE SOPRO (Fogo)", "ARMA_DE_SOPRO_FOGO")),
                        tracosParaRemoverPorNome = listOf("FORTE")
                    )
                )
            )
        )
    )

    private fun ferais(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "FERAIS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("DIMINUTO (Tamanho -3)", "DIMINUTO_TAMANHO_3"))
                    )
                ),
                VariantOption(
                    id = "menor",
                    nome = "Menor",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("DIMINUTO (Tamanho -4)", "DIMINUTO_TAMANHO_4")),
                        tracosParaRemoverPorNome = listOf("ESPIRITUOSO"),
                        desvantagensParaAdicionar = listOf(TraitAddition("TRANSTORNO DE SEPARAÇÃO", "TRANSTORNO_DE_SEPARACAO")),
                        desvantagensParaRemover = listOf("ALTA/BAIXA TECNOLOGIA", "ALTA/BAIXA TECNOLOGIA (Maior)")
                    )
                )
            )
        )
    )

    private fun florans(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "FLORANS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("ROBUSTO", "ROBUSTO")))
                ),
                VariantOption(
                    id = "defensivo",
                    nome = "Defensivo",
                    // Toque Venenoso (Paralisante) custa 3 — deixava a raça 1
                    // ponto acima do orçamento. O livro é Nocauteador (2), não
                    // Paralisante (3): base(0) + Nocauteador(2) = 2, fecha.
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("TOQUE VENENOSO (Nocauteador)", "TOQUE_VENENOSO_NOCAUTEADOR"))
                    )
                )
            )
        )
    )

    private fun gelatinoides(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "GELATINOIDES",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("REGENERAÇÃO", "REGENERACAO")))
                ),
                VariantOption(
                    id = "ameba",
                    nome = "Ameba",
                    // Camuflagem Total (2), não a Camuflagem básica (1) — bate
                    // com a Regeneração (2) que a raça perde ao trocar de
                    // Padrão pra Ameba.
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("CAMUFLAGEM TOTAL", "CAMUFLAGEM_TOTAL")))
                )
            )
        )
    )

    private fun insetoidesScifi(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "INSETOIDES",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("ARMADURA +2", "ARMADURA"),
                            TraitAddition("GARRAS", "GARRAS")
                        ),
                        armasNaturaisParaAdicionar = listOf(ArmaNatural(nome = "Garras", dano = "For+d4", pa = 2, id = "GARRAS")),
                        naturalArmor = 2
                    )
                ),
                VariantOption(
                    id = "vespa",
                    nome = "Vespa",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("FERRÃO (Mordida For+d4)", "FERRAO_MORDIDA_FOR_D4"),
                            // Mesmo id de Fadas/Avianos (voo_6, ver
                            // RacialTraitPointCatalog.LABEL/CUSTOS) — mesmo
                            // tier de Voo, só concedido por outra raça.
                            TraitAddition("VOO (Movimentação 6)", "VOO_MOV_6"),
                            TraitAddition("TOQUE VENENOSO (Moderado)", "TOQUE_VENENOSO_MODERADO")
                        ),
                        armasNaturaisParaAdicionar = listOf(ArmaNatural(nome = "Ferrão", dano = "For+d4"))
                    )
                )
            )
        )
    )

    private fun mimicos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MIMICOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        // Mesmo id oficial de "Mudar de Forma" usado no
                        // catálogo de custos (RacialTraitPointCatalog.CUSTOS
                        // "MUDAR_DE_FORMA"), não um slug novo pro mesmo conceito.
                        tracosParaAdicionar = listOf(TraitAddition("MUDANÇA DE FORMA", "MUDAR_DE_FORMA"))
                    )
                ),
                VariantOption(
                    id = "resistente",
                    nome = "Resistente",
                    // Livro: usa o valor oficial de "Mudança de Forma (Sem
                    // variação de Tamanho)" (4) e a Variante fica 1 ponto
                    // acima do orçamento. Provavelmente o livro pretendia um
                    // tier de 3 pontos pra esse traço aqui e não formalizou —
                    // sem fonte oficial, usamos um id pontual só pra esta
                    // raça fechar a conta (MUDAR_DE_FORMA_AJUSTE_MIMICOS, 3
                    // pontos, mesmo texto de exibição — não muda Tamanho, ver
                    // RacialTraitPointCatalog). Não cadastrado como traço
                    // oficial escolhível em nenhum editor de Variante custom.
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("RESISTÊNCIA +1", "RESISTENCIA"),
                            TraitAddition("MUDANÇA DE FORMA (Sem variação de tamanho)", "MUDAR_DE_FORMA_AJUSTE_MIMICOS")
                        )
                    )
                )
            )
        )
    )

    private fun mineradoresGeneticos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MINERADORES GENETICOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        // Livro: tier "a cada minuto" (-2), não o tier base (-1) de
                        // DEPENDENCIA_ATMOSFERICA — ver RacialTraitPointCatalog.
                        // Id igual ao de habilidades[] na raça base (dedupe por
                        // addIfAbsent em CriadorState.kt), senão soma duas vezes.
                        tracosParaAdicionar = listOf(
                            TraitAddition("FORTE", "FORTE"),
                            TraitAddition("DEPENDÊNCIA ATMOSFÉRICA (Maior)", "DEPENDENCIA_ATMOSFERICA_MAIOR")
                        )
                    )
                ),
                VariantOption(
                    id = "zero_g",
                    nome = "Zero G",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(TraitAddition("ADAPTAÇÃO GRAVITACIONAL", "ADAPTACAO_GRAVITACIONAL")),
                        vantagensGratisIds = listOf("adaptacao_gravitacional"),
                        tracosParaRemoverPorNome = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA (Maior)"),
                        desvantagensParaAdicionar = listOf(
                            TraitAddition("HABITANTE DE GRAVIDADE ZERO/BAIXA (Maior)", "HABITANTE_DE_GRAVIDADE_ZERO_BAIXA_MAIOR")
                        ),
                        desvantagensParaRemover = listOf("DEPENDÊNCIA ATMOSFÉRICA", "DEPENDÊNCIA ATMOSFÉRICA (Maior)")
                    )
                )
            )
        )
    )

    private fun oraculos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ORACULOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(TraitAddition("NOÇÃO DO PERIGO", "NOCAO_DO_PERIGO")),
                        vantagensGratisIds = listOf("nocao_do_perigo")
                    )
                ),
                VariantOption(
                    id = "aterrorizado",
                    nome = "Aterrorizado",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisIds = listOf("poderes_misticos"),
                        tracosParaAdicionar = listOf(TraitAddition("PODERES MÍSTICOS (TELEPATA)", "PODERES_MISTICOS_TELEPATA")),
                        tracosParaRemoverPorNome = listOf("NOÇÃO DO PERIGO", "NOCAO_DO_PERIGO")
                    )
                )
            )
        )
    )

    private fun possessores(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "POSSESSORES",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(tracosParaRemoverPorNome = listOf("NOÇÃO DO PERIGO", "NOCAO DO PERIGO"))
                ),
                VariantOption(
                    id = "energia",
                    nome = "Energia",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("FORMA DE ENERGIA", "FORMA_DE_ENERGIA")),
                        tracosParaRemoverPorNome = listOf("NOÇÃO DO PERIGO", "NOCAO DO PERIGO"),
                        // Nota pro mestre, não uma desvantagem de verdade —
                        // pertence a `anotacoes`, não a `desvantagensParaAdicionar`
                        // (onde só cabem traços reais com id).
                        anotacoes = listOf(
                            "Combine com o mestre de jogo para equilibrar com 4 pontos de habilidades negativas que façam sentido no cenário."
                        )
                    )
                )
            )
        )
    )

    private fun quadroides(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "QUADROIDES",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                // "Padrão" não é uma Variante de verdade — Ação Adicional
                // (Física) e Sensível (Maior) já vêm de habilidades[] na raça
                // base (ancestralidades.json), então esta opção não precisa
                // adicionar nada (mesmo padrão de Drakens/Elementais — ver
                // auditoria de raças).
                VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "habilidoso",
                    nome = "Habilidoso",
                    // Troca Ação Adicional (Física, 4) pela versão que ignora
                    // penalidade de Ações Múltiplas (5) — 1 ponto mais forte,
                    // por isso o livro pede pro mestre equilibrar com 1 ponto
                    // de traço negativo. Isso agora é escolha de verdade do
                    // jogador (ver quadroidesTracoNegativoSelecionado em
                    // CriadorState + o bloco QUADROIDES em
                    // ResolveAncestrySpecificAdjustmentsUseCase, que injeta o
                    // traço escolhido — ou o primeiro da lista, se nada foi
                    // escolhido ainda), não mais um lembrete solto em
                    // anotações.
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "AÇÃO ADICIONAL (Ignora 2 pontos de penalidade por Ações Múltiplas)",
                                "ACAO_ADICIONAL_IGNORA_PENALIDADE_ACOES_MULTIPLAS"
                            )
                        ),
                        tracosParaRemoverPorNome = listOf("AÇÃO ADICIONAL (Física)")
                    ),
                    // Âncora só de documentação/lookup (ver
                    // ResolveAncestrySpecificAdjustmentsUseCase, mesmo padrão
                    // de anao_ciber_tracos_negativos em anoes() acima) — não é
                    // interpretada genericamente, o traço negativo é injetado
                    // à mão no use case.
                    selecoes = listOf(
                        SelectionDef(
                            id = "quadroides_traco_negativo",
                            rotulo = "Escolha 1 ponto de traço racial negativo",
                            tipo = SelectionType.BUDGETED_CATALOG,
                            catalogId = "quadroides_negativo"
                        )
                    )
                )
            )
        )
    )

    private fun soldadosGeneticos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "SOLDADOS GENETICOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(
                            TraitAddition("NERVOS DE AÇO", "NERVOS_DE_ACO"),
                            TraitAddition("REFLEXOS DE COMBATE", "REFLEXOS_DE_COMBATE")
                        )
                    )
                ),
                VariantOption(
                    id = "fuzileiro_zero_g",
                    nome = "Fuzileiro Zero G",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(
                            TraitAddition("ADAPTAÇÃO GRAVITACIONAL", "ADAPTACAO_GRAVITACIONAL"),
                            TraitAddition("REFLEXOS DE COMBATE", "REFLEXOS_DE_COMBATE")
                        )
                    )
                )
            )
        )
    )

    // Robôs e Seres Sintéticos: confirmado contra ancestralidades.json que
    // "Padrão" É uma opção real de `opcoes` (não um `else` de segurança sem
    // texto correspondente) — o que tinha ficado pendente de checar quando
    // o lote 2 foi migrado. Nota: Robôs Guerreiro troca a Complicação
    // Pacifista (Maior) por Sem Escrúpulos (Maior) na descrição da raça
    // ("variantes" no JSON), mas o Result original nunca removia
    // "PACIFISTA (Maior)" — a habilidade base CIRCUITOS_DE_ASIMOV (que
    // concede Pacifista Maior) fica, então Guerreiro acumula as duas
    // Complicações. Preservado como estava (não é uma regressão desta
    // migração) — sinalizado pro usuário decidir se é bug de conteúdo.
    // A habilidade base de Robôs já concede CIRCUITOS DE ASIMOV (nome de
    // sabor pra Pacifista Maior, ver descrição em ancestralidades.json) e
    // PROGRAMADO (Maior) incondicionalmente — então Padrão/Limitado não
    // precisam pedir de novo (o "ensure" antigo pedia "PACIFISTA (Maior)",
    // um texto que nunca batia com "CIRCUITOS DE ASIMOV (Maior)", e por
    // isso empilhava as duas Complicações como se fossem diferentes; bug
    // de conteúdo pré-existente, não introduzido por esta migração).
    // Guerreiro troca Pacifista por Sem Escrúpulos (Maior) de verdade,
    // removendo CIRCUITOS DE ASIMOV pelo nome real da habilidade base.
    private fun robos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ROBOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "guerreiro",
                    nome = "Guerreiro",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("SEM ESCRÚPULOS (Maior)", "SEM_ESCRUPULOS_MAIOR")),
                        desvantagensParaRemover = listOf(
                            "CIRCUITOS DE ASIMOV (Maior)", "CIRCUITOS DE ASIMOV", "PACIFISTA (Maior)", "PACIFISTA"
                        )
                    )
                ),
                VariantOption(
                    id = "limitado",
                    nome = "Limitado",
                    pacoteFixo = ResolvedTraitPackage(
                        // Id fixo e previsível porque isPericiaBasicaEfetiva/
                        // periciaStartRawInternal em CriadorState checam esse
                        // id diretamente pra saber se removem o d4 grátis de
                        // todas as perícias básicas (periciasBasicasReduzidasTotalId).
                        tracosParaAdicionar = listOf(
                            TraitAddition("PERÍCIAS BÁSICAS REDUZIDAS (TOTAL)", "PERICIAS_BASICAS_REDUZIDAS_TOTAL")
                        ),
                        anotacoes = listOf("Robôs Limitado: Combine com o mestre compensação de Perícias Reduzidas.")
                    )
                )
            )
        )
    )

    private fun seresSinteticos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "SERES SINTETICOS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("PROGRAMADO", "PROGRAMADO"))
                    )
                ),
                VariantOption(
                    id = "maquina_procurado",
                    nome = "Máquina (Procurado)",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("PROCURADO (Maior)", "PROCURADO_MAIOR")),
                        desvantagensParaRemover = listOf("PROGRAMADO (Maior)")
                    )
                ),
                VariantOption(
                    id = "maquina_forasteiro",
                    nome = "Máquina (Forasteiro)",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(TraitAddition("FORASTEIRO (Maior)", "FORASTEIRO_MAIOR")),
                        desvantagensParaRemover = listOf("PROGRAMADO (Maior)")
                    )
                )
            )
        )
    )

    private fun yetis(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "YETIS",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "sopro",
                    nome = "Sopro",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("ARMA DE SOPRO (Frio)", "ARMA_DE_SOPRO_FRIO")),
                        desvantagensParaAdicionar = listOf(TraitAddition("DEPENDÊNCIA", "DEPENDENCIA"))
                    )
                )
            )
        )
    )

    // --- Terracota: Seleção de pacote fixo (Voto OU Obrigação, ambas
    // Complicação Maior "de nascença" — todo Terracota tem uma das duas). ---
    private fun terracota(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "TERRACOTA",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "terracota_complicacao",
                rotulo = "Escolha a Complicação de nascença",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption(
                        id = "voto",
                        nome = "Voto (Maior)",
                        pacote = ResolvedTraitPackage(desvantagensParaAdicionar = listOf(TraitAddition("VOTO (Maior)", "VOTO_MAIOR")))
                    ),
                    FixedPackageOption(
                        id = "obrigacao",
                        nome = "Obrigação (Maior)",
                        pacote = ResolvedTraitPackage(desvantagensParaAdicionar = listOf(TraitAddition("OBRIGAÇÃO (Maior)", "OBRIGACAO_MAIOR")))
                    )
                )
            )
        )
    )

    // --- Umvee (Filhos da Lua): Seleção de pacote fixo, 1 de 6 "Dons da
    // Natureza", todos calibrados em 2 pontos. Efeitos conferidos exatamente
    // como o código atual já faz em ResolveAncestrySpecificAdjustmentsUseCase
    // (linhas ~714-780): a maioria é só texto automático (tracosParaAdicionar
    // -> ensureAutomaticAdvantages na camada de wiring), mas "Vínculo
    // Bestial" concede uma Vantagem de verdade (vantagensGratisParaAdicionar
    // -> ensureAdvantageNames, com todos os ganchos mecânicos da vantagem
    // "Senhor das Feras"). "Pedregoso" também define naturalArmorFromRace=2
    // no Result — isso fica como exceção pontual na camada de wiring, não
    // faz parte do pacote genérico (não é comum o suficiente pra merecer
    // campo próprio no schema).
    private fun umvee(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "UMVEE (FILHOS DA LUA)",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "umvee_dom_da_natureza",
                rotulo = "Escolha o Dom da Natureza",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    // GARRAS_SEM_PA (2, For+d4 sem PA) — GARRAS puro (3) inclui
                    // PA, que este dom não dá.
                    FixedPackageOption("apice", "Ápice", ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("Ápice", "GARRAS_SEM_PA")))),
                    FixedPackageOption(
                        "vinculo_bestial", "Vínculo Bestial",
                        ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf(TraitAddition("SENHOR DAS FERAS", "SENHOR_DAS_FERAS")))
                    ),
                    // Duas metades de 1 ponto cada: Aparar +1 de verdade
                    // (APARAR) e "Emanar Luz", sem efeito mecânico próprio
                    // (PELE_LUMINOSA) — ver applyAncestryVariantAdjustments.
                    FixedPackageOption(
                        "pele_iluminada_pela_lua", "Pele Iluminada pela Lua",
                        ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Pele Iluminada pela Lua (Aparar)", "APARAR"),
                                TraitAddition("Pele Iluminada pela Lua (Emanar Luz)", "PELE_LUMINOSA")
                            )
                        )
                    ),
                    // Visão no Escuro (1) + Perceber d6 (1) — Ocultismo d4 NÃO
                    // faz parte deste dom (é NATURALMENTE_SOBRENATURAL, traço
                    // base de todo Umvee).
                    FixedPackageOption(
                        "gatoruja", "Gatoruja",
                        ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("VISÃO NO ESCURO", "VISAO_NO_ESCURO"),
                                TraitAddition("Perceber d6", "PERCEBER_D6")
                            )
                        )
                    ),
                    FixedPackageOption(
                        "correnteza", "Correnteza",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("MOVIMENTAÇÃO +2", "MOVIMENTACAO")))
                    ),
                    FixedPackageOption(
                        // Id/vezes têm que bater EXATAMENTE com o que
                        // CriadorState.applyAncestryVariantAdjustments já
                        // injeta em newHabilidades pro caso "Pedregoso" — os
                        // dois caminhos rodam pra Umvee (esse aqui é só
                        // bookkeeping redundante de vantagensRaciais), então
                        // um id ou vezes diferente pro mesmo efeito contaria a
                        // Resistência/Armadura em dobro (o mesmo tipo de bug
                        // já corrigido pro Tamanho de Fadas/Povo Rato — ver
                        // ModifierEngineAdgAncestryTest).
                        "pedregoso", "Pedregoso",
                        ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Pedregoso (Resistência)", "RESISTENCIA"),
                                TraitAddition("Pedregoso (Armadura)", "ARMADURA")
                            )
                        )
                    )
                )
            )
        )
    )

    // --- Elementais (Sci-Fi): apesar de ter "Padrão" entre as opções (o que
    // normalmente indicaria Variante), o usuário confirmou que este é o caso
    // excepcional: é Seleção de elemento mesmo (todo elemental É de algum
    // elemento, igual ao Descendente Elemental de Fantasia). Efeitos
    // idênticos ao que o livro descreve: Padrão mantém Forte e Resistência
    // +2 (a raça é de pedra/terra, física e resistente); Ar, Fogo ou Água
    // troca os dois por Forma de Energia (o corpo já não é mais sólido nem
    // musculoso).
    //
    // Elementais é candidato único em ancestralidades.json (só existe no
    // Sci-Fi) e não faz parte de `scifiVariantDrivenKeys` (não tem
    // `grupoVariante`, é Seleção pura) — mas CriadorState
    // .applyAncestryVariantAdjustments() lê estes `pacotesFixos` de verdade
    // via ResolveAncestryVariantPackageUseCase.resolve(), no mesmo padrão
    // (tracosParaRemoverPorNome/tracosParaAdicionar) que as raças de
    // scifiVariantDrivenKeys usam — zero `if` de nome de raça decidindo QUAIS
    // traços trocam, só QUANDO trocar (a condição de qual pacote escolher
    // ainda lê `key == "ELEMENTAIS"`, mas o conteúdo do pacote mora aqui).
    // "Ajuste de Orçamento (Forma de Energia)" fecha a diferença de pontos
    // entre remover Forte+Resistência (6 pts) e ganhar só Forma de Energia
    // (4 pts) — sem efeito mecânico próprio, só bookkeeping (ver
    // TraitAddition.pontos/invisivel), pra manter as duas opções em
    // pontosRaciaisEsperados = 2, igual sempre foi.
    private fun elementaisScifi(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ELEMENTAIS",
        livro = "SCI_FI",
        selecoes = listOf(
            SelectionDef(
                id = "elementais_scifi_elemento",
                rotulo = "Escolha o elemento",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption("padrao", "Padrão", ResolvedTraitPackage()),
                    FixedPackageOption(
                        "ar_fogo_ou_agua", "Ar, Fogo ou Água",
                        ResolvedTraitPackage(
                            tracosParaRemoverPorNome = listOf("MUITO FORTE", "RESISTÊNCIA +2"),
                            tracosParaAdicionar = listOf(
                                TraitAddition("Forma de Energia", "FORMA_DE_ENERGIA"),
                                TraitAddition(
                                    "Ajuste de Orçamento (Forma de Energia)", "AJUSTE_FORMA_DE_ENERGIA",
                                    pontos = 2, invisivel = true
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    // --- Descendente Elemental (Fantasia): Seleção de elemento, mesmo padrão
    // de elementaisScifi() acima. Base fixa em ancestralidades.json
    // (Resistência Ambiental +1, sempre presente — NÃO é substituída pela
    // escolha, é um traço permanente à parte; Forasteiro Menor -1) mais o
    // marcador "Elemento Ancestral" (ELEMENTO_ANCESTRAL, custo 2 —
    // resolveMarkedSelection REMOVE esse marcador e injeta o traço real do
    // elemento escolhido, que já custa 2 também, então o catálogo estático
    // (RESISTENCIA_AMBIENTAL 1 + ELEMENTO_ANCESTRAL 2 + FORASTEIRO -1 = 2)
    // e o resolvido ao vivo (RESISTENCIA_AMBIENTAL 1 + <elemento> 2 +
    // FORASTEIRO -1 = 2) fecham no mesmo total — mesmo padrão de HERANCA
    // (Meio-Elfo), sem precisar mexer no JSON nem na lista de exceções de
    // AncestralidadeCatalogBudgetTest.
    //
    // Achado real (rodada 36): antes desta rodada, o bloco que resolvia
    // `descendenteElementalSelecionado` em CriadorState.applyAncestryVariantAdjustments
    // ficava DEPOIS de `resolveSciFiVariantSelectionFor(base.nome, base.opcoes)
    // ?: return base` — e Descendente Elemental não tem `opcoes` (raça sem
    // Variante de livro, só Seleção), então esse early-return disparava
    // sempre primeiro e o bloco inteiro nunca era alcançado: escolher um
    // elemento não tinha efeito mecânico nenhum (bug real em produção,
    // confirmado rodando o app — não hipotético). Corrigido movendo a
    // resolução pra antes desse early-return, mesmo lugar de Herança/
    // Signo/Meio-Orc/Feral.
    private fun descendenteElemental(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "DESCENDENTE ELEMENTAL",
        livro = "FANTASIA",
        selecoes = listOf(
            SelectionDef(
                id = "descendente_elemental_elemento",
                rotulo = "Escolha o elemento ancestral",
                tipo = SelectionType.FIXED_PACKAGE,
                marcadorTraitId = "ELEMENTO_ANCESTRAL",
                pacotesFixos = listOf(
                    FixedPackageOption(
                        "agua", "Água",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("AQUÁTICO", "AQUATICO")))
                    ),
                    FixedPackageOption(
                        "ar", "Ar",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("AR INTERNO", "AR_INTERNO")))
                    ),
                    FixedPackageOption(
                        "fogo", "Fogo",
                        // targetRef = id real de vantagens.json ("rapido") —
                        // resolveMarkedSelection injeta isso como
                        // traitId=GRANTED_EDGE + targetRef, o par que
                        // resolvedVantagensGratis() já lê pra conceder a
                        // Vantagem de verdade (mesmo mecanismo de
                        // Kitsunemimi/Tanukimimi/Meio-Demônio).
                        ResolvedTraitPackage(
                            vantagensGratisParaAdicionar = listOf(
                                TraitAddition("RÁPIDO", "RAPIDO", targetRef = "rapido")
                            )
                        )
                    ),
                    FixedPackageOption(
                        "terra", "Terra",
                        ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition(
                                    "SÓLIDO COMO ROCHA", "SOLIDO_COMO_ROCHA",
                                    traitId = "ATTRIBUTE_BOOST", targetRef = "Vigor", value = 1
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    // --- Anões: Variante real "Ciber" (mestre reconfigura pro cenário) com
    // Seleção aninhada (até 2 pontos de traços negativos, orçamento
    // delegado a AnaoCiberTraitCatalog). ---
    private fun anoes(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ANOES",
        livro = "SCI_FI",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "ciber",
                    nome = "Ciber",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(TraitAddition("CIBERTOLERÂNCIA", "CIBERTOLERANCIA"))
                    ),
                    selecoes = listOf(
                        SelectionDef(
                            id = "anao_ciber_tracos_negativos",
                            rotulo = "Escolha até 2 pontos de traços raciais negativos (nenhum maior que -2)",
                            tipo = SelectionType.BUDGETED_CATALOG,
                            catalogId = "anao_ciber"
                        )
                    )
                )
            )
        )
    )

    // --- Humanos (Fantasia): Variante real (o mestre/grupo escolhe o Pacote
    // Cultural do personagem pra a mesa) — substitui o antigo sistema
    // dedicado de Pacote Cultural (PACOTES_CULTURAIS_FANTASIA em
    // CriadorState, removido), agora só mais uma Variante registrada como
    // qualquer outra. "Humano padrão" não tem VariantOption própria (mesmo
    // padrão de "Padrão" nas raças Sci-Fi/Anão Ciber): resolver()
    // com variantOptionId nulo já cai no ResolvedTraitPackage() vazio, que
    // mantém Adaptável (só removido pelo bloco de habilidades[] em
    // CriadorState.applyAncestryVariantAdjustments quando outro pacote é
    // escolhido).
    //
    // d6 inicial de perícia/atributo (NOMADES_DESERTO_SOBREVIVENCIA,
    // POVO_MONTANHA_VIGOR, POVO_MAR_ATLETISMO/NAVEGAR, SENHORES_CAVALOS_CAVALGAR)
    // usa os mesmos ids já cadastrados em RacialTraitPointCatalog.EFEITOS —
    // entram em habilidades[] da raça (via applyAncestryVariantAdjustments),
    // lidos pelo mesmo loop genérico de PericiaStep/AtributoStep que qualquer
    // outra raça já usa.
    //
    // Fraqueza/Resistência Ambiental e Penalidade em Cavalgar são só texto
    // informativo (modificam testes "em jogo", sem valor de construção do
    // personagem — regra do usuário: só entram como id+descrição, nunca como
    // efeito mecânico). Procurado (Maior)/Código de Honra/Sem Escrúpulos/
    // Analfabeto são Complicações reais do catálogo (complicacoes.json) — o
    // texto tem que bater com o nome catalogado pra
    // ResolveRacialAutomaticComplicationsUseCase reconhecer e conceder
    // automaticamente. Nascido na Sela é Vantagem real (vantagens.json,
    // id "nascido_na_sela") — concedida via vantagensGratisIds, não por nome
    // solto, pra garantir a rerrolagem/movimentação mecânica dela (o sistema
    // antigo só guardava o nome em bookkeeping, sem conceder o efeito de
    // verdade).
    private fun humanoFantasia(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "HUMANOS",
        livro = "FANTASIA",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(id = "padrao", nome = "Padrão", pacoteFixo = ResolvedTraitPackage()),
                VariantOption(
                    id = "nomades_do_deserto",
                    nome = "Nômades do Deserto",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "Sobrevivência d6", "NOMADES_DESERTO_SOBREVIVENCIA",
                                traitId = "SKILL_BOOST", targetRef = "Sobrevivência", value = 1
                            )
                        ),
                        tracosNegativosParaAdicionar = listOf(TraitAddition("Fraqueza Ambiental (Frio)", "FRAQUEZA_AMBIENTAL_FRIO")),
                        vantagensGratisParaAdicionar = listOf(TraitAddition("Resistência Ambiental (Calor)", "RESISTENCIA_AMBIENTAL_CALOR"))
                    )
                ),
                VariantOption(
                    id = "povo_da_montanha",
                    nome = "Povo da Montanha",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "Vigor d6", "POVO_MONTANHA_VIGOR",
                                traitId = "ATTRIBUTE_BOOST", targetRef = "Vigor", value = 1
                            )
                        ),
                        tracosNegativosParaAdicionar = listOf(TraitAddition("Fraqueza Ambiental (Calor)", "FRAQUEZA_AMBIENTAL_CALOR")),
                        vantagensGratisParaAdicionar = listOf(TraitAddition("Resistência Ambiental (Frio)", "RESISTENCIA_AMBIENTAL_FRIO"))
                    )
                ),
                VariantOption(
                    id = "povo_do_mar",
                    nome = "Povo do Mar",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "Atletismo d6", "POVO_MAR_ATLETISMO",
                                traitId = "SKILL_BOOST", targetRef = "Atletismo", value = 1
                            ),
                            TraitAddition(
                                "Navegar d6", "POVO_MAR_NAVEGAR",
                                traitId = "SKILL_BOOST", targetRef = "Navegar", value = 1
                            )
                        )
                    ),
                    selecoes = listOf(
                        SelectionDef(
                            id = "povo_do_mar_compensacao",
                            rotulo = "Compensação (a critério do Mestre)",
                            tipo = SelectionType.FIXED_PACKAGE,
                            pacotesFixos = listOf(
                                FixedPackageOption("nenhuma", "Nenhuma", ResolvedTraitPackage()),
                                FixedPackageOption(
                                    "penalidade_cavalgar", "Penalidade em Cavalgar",
                                    ResolvedTraitPackage(
                                        tracosNegativosParaAdicionar = listOf(
                                            TraitAddition("Penalidade em Cavalgar", "PENALIDADE_CAVALGAR")
                                        )
                                    )
                                ),
                                FixedPackageOption(
                                    "procurado_maior", "Procurado (Maior)",
                                    ResolvedTraitPackage(
                                        desvantagensParaAdicionar = listOf(TraitAddition("PROCURADO (Maior)", "PROCURADO_MAIOR"))
                                    )
                                )
                            )
                        )
                    )
                ),
                VariantOption(
                    id = "senhores_dos_cavalos",
                    nome = "Senhores dos Cavalos",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "Cavalgar d6", "SENHORES_CAVALOS_CAVALGAR",
                                traitId = "SKILL_BOOST", targetRef = "Cavalgar", value = 1
                            )
                        )
                    ),
                    selecoes = listOf(
                        SelectionDef(
                            id = "senhores_cavalos_grupo",
                            rotulo = "Grupo cultural (a critério do Mestre)",
                            tipo = SelectionType.FIXED_PACKAGE,
                            pacotesFixos = listOf(
                                FixedPackageOption("nenhum", "Nenhum", ResolvedTraitPackage()),
                                // "Nascido na Sela" sozinho (sem Complicação) foi removido: pelo
                                // livro, ganhar a Vantagem precisa vir com uma Complicação pra
                                // compensar (Código de Honra ou Sem Escrúpulos + Analfabeto) —
                                // as duas opções abaixo. Sem Complicação nenhuma, a única opção
                                // válida é "Nenhum" (só o Cavalgar d6 do pacote cultural).
                                FixedPackageOption(
                                    "codigo_de_honra", "Nascido na Sela + Código de Honra",
                                    ResolvedTraitPackage(
                                        vantagensGratisIds = listOf("nascido_na_sela"),
                                        desvantagensParaAdicionar = listOf(TraitAddition("CÓDIGO DE HONRA", "CODIGO_DE_HONRA"))
                                    )
                                ),
                                FixedPackageOption(
                                    "sem_escrupulos_analfabeto", "Nascido na Sela + Sem Escrúpulos e Analfabeto",
                                    ResolvedTraitPackage(
                                        vantagensGratisIds = listOf("nascido_na_sela"),
                                        desvantagensParaAdicionar = listOf(
                                            TraitAddition("SEM ESCRÚPULOS (Menor)", "SEM_ESCRUPULOS_MENOR"),
                                            TraitAddition("ANALFABETO", "ANALFABETO")
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    // --- Meio-Elfo (Básico/Fantasia/Horror/Super): Seleção de pacote fixo
    // (Herança Élfica x Herança Humana), mesmo formato de Terracota — a
    // raça carrega o traço "HERANCA" como placeholder (custo 2, igual às
    // duas opções abaixo, então o valor de livro da raça não muda seja qual
    // for a escolha), e cada opção troca esse placeholder pelo traço
    // resolvido de verdade. As 4 entradas (uma por livro) compartilham o
    // mesmo conteúdo através desta função — evita duplicar o pacote 4 vezes
    // e o risco de as cópias saírem do sincronismo. Meio-Elfo do Pathfinder
    // fica de fora de propósito: tem "Flexibilidade" (atributo livre à
    // escolha) em vez de "Herança", raça mecanicamente diferente que só
    // compartilha o nome de exibição.
    private fun meioElfoHeranca(livro: String): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MEIO-ELFOS",
        livro = livro,
        selecoes = listOf(
            SelectionDef(
                id = "meio_elfo_heranca",
                rotulo = "Escolha a Herança",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption(
                        id = "agil",
                        nome = "Herança Élfica (Agilidade d6)",
                        pacote = ResolvedTraitPackage(
                            tracosParaRemoverPorId = listOf("HERANCA"),
                            tracosParaAdicionar = listOf(TraitAddition("Ágil", "AGIL"))
                        )
                    ),
                    FixedPackageOption(
                        id = "adaptavel",
                        nome = "Herança Humana (Adaptável)",
                        pacote = ResolvedTraitPackage(
                            tracosParaRemoverPorId = listOf("HERANCA"),
                            tracosParaAdicionar = listOf(TraitAddition("Adaptável", "ADAPTAVEL"))
                        )
                    )
                )
            )
        )
    )

    // --- Meio-Demônio (Cidade do Sol a Vapor): mesmo formato do Meio-Elfo
    // acima, só que o placeholder é "ADAPTAVEL_OU_ANTECEDENTE_ARCANO_
    // DEMONIO" (custo 2, igual Adaptável — ver RacialTraitPointCatalog) e as
    // opções são Adaptável x Antecedente Arcano (Demônio) diluído
    // (aa_demonio_meio_demonio — mesmo id de vantagem já usado em
    // ResolveAncestrySpecificAdjustmentsUseCase, não um clone novo).
    //
    // Ficou só cadastrado aqui (sem aplicação de verdade) da rodada em que
    // foi criado até a rodada 38: `resolveMarkedSelection` só lia
    // `vantagensGratisParaAdicionar` sem setar `targetRef` (só `id`), o que
    // teria trocado silenciosamente o alvo da concessão de
    // "aa_demonio_meio_demonio" (id real da Vantagem, o que
    // `resolvedVantagensGratis()` espera) pra "ANTECEDENTE_ARCANO_DEMONIO_MEIO"
    // (id do TRAÇO, que não existe no catálogo de Vantagens) — quebrando a
    // concessão sem nenhum teste acusar na hora. A rodada 36 (Descendente
    // Elemental) já resolveu isso threadando `targetRef` de verdade em
    // `TraitAddition`/`resolveMarkedSelection`; `vantagensGratisIds` abaixo
    // nunca chegou a ser consumido por nada pra esta raça (não existe bloco
    // de Meio-Demônio em `ResolveAncestrySpecificAdjustmentsUseCase`,
    // confirmado por grep) — removido por virar dado morto/confuso agora
    // que `targetRef` é o mecanismo de verdade.
    private fun meioDemonio(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MEIO-DEMONIO",
        livro = "CIDADE_SOL_VAPOR",
        selecoes = listOf(
            SelectionDef(
                id = "meio_demonio_traco",
                rotulo = "Escolha o Traço Racial",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption(
                        id = "adaptavel",
                        nome = "Adaptável",
                        pacote = ResolvedTraitPackage(
                            tracosParaRemoverPorId = listOf("ADAPTAVEL_OU_ANTECEDENTE_ARCANO_DEMONIO"),
                            tracosParaAdicionar = listOf(
                                TraitAddition("Adaptável", "ADAPTAVEL")
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "antecedente_arcano",
                        nome = "Antecedente Arcano (Demônio)",
                        pacote = ResolvedTraitPackage(
                            tracosParaRemoverPorId = listOf("ADAPTAVEL_OU_ANTECEDENTE_ARCANO_DEMONIO"),
                            vantagensGratisParaAdicionar = listOf(
                                TraitAddition(
                                    nome = "Antecedente Arcano (Demônio)",
                                    id = "ANTECEDENTE_ARCANO_DEMONIO_MEIO",
                                    targetRef = "aa_demonio_meio_demonio"
                                )
                            )
                        )
                    )
                )
            )
        )
    )

    // --- Humano (Império San, Arte da Guerra): Signos de Nascença. Seleção
    // de pacote fixo, 1 de 14 opções (13 signos + "Nenhum") — mesmo formato
    // de Terracota/Umvee, só maior. A raça base carrega só o traço
    // "SIGNOS_DE_NASCENCA" (custo 0, o texto de referência com os 13
    // signos), sempre presente independente da escolha — ele é o marcador
    // usado pelas guardas em CriadorState (nunca por nome de raça). Cada
    // opção abaixo é o que a raça REALMENTE concede quando ativa; "Nenhum"
    // é o único caso onde a raça tem Adaptável + Pontos de Perícia — as
    // outras 13 removem esses dois de vez (não fazem sentido pra quem tem
    // um Signo de verdade).
    //
    // Cobertura mecânica: cada opção só carrega os efeitos que JÁ TÊM um
    // gancho mecânico real no app (aumento de atributo/perícia, Vantagem
    // concedida, bônus de Reserva de Chi — tudo generalizado nesta mesma
    // rodada). Bônus puramente situacionais que o livro descreve mas que
    // nenhuma parte do app aplica automaticamente a uma rolagem específica
    // (Tigre inteiro; a penalidade de Finalização da Tartaruga; o d4+1 sem
    // treino do Macaco; a redução de Exausto do Urso; os +1 pontuais de
    // Basabasa/Boi/Dragão/Raposa em perícias específicas; o uso de Bene da
    // Lebre; os efeitos percentuais/de diferença do Jogar-ou-Performance da
    // Serpente) ficam de fora do pacote (sem custo fabricado pra "fechar" a
    // conta) e documentados em `anotacoes` — o validador de orçamento por
    // opção (ValidateAncestryOptionBudgetsUseCase) vai mostrar essas opções
    // abaixo de pontosRaciaisEsperados=3, de propósito: é um retrato honesto
    // do que já está implementado, não um "quase lá" escondido atrás de um
    // ajuste de pontos inventado. Garça soma 4 (acima de 3) pelo motivo
    // oposto: os 3 efeitos dela (Aparar, Acrobacia, Atletismo) JÁ têm gancho
    // mecânico pronto e reaproveitado (nenhum inventado), e o livro
    // aparentemente não calibra os Signos entre si com o mesmo rigor que
    // Terracota/Meio-Elfo — sinais de conteúdo, não bugs de código.
    private fun humanoArteDaGuerraSignos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "HUMANOS",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "signo_de_nascenca",
                rotulo = "Escolha o Signo de Nascença",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption(
                        id = "nenhum",
                        nome = "Nenhum",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Adaptável", "ADAPTAVEL"),
                                TraitAddition("Pontos de Perícia", "PONTOS_DE_PERICIA")
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "basabasa",
                        nome = "Basabasa",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Atraente (Vantagem concedida)", "ATRAENTE"),
                                TraitAddition(
                                    "Bônus de Perícia (+1): Provocar ou Intimidar (à escolha)",
                                    "BONUS_PERICIA_1"
                                )
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "boi",
                        nome = "Boi",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Força d6 (Boi)", "FORTE"),
                                TraitAddition("Bônus de Perícia (+1): Atletismo (esforço físico)", "BONUS_PERICIA_1")
                            ),
                            anotacoes = listOf(
                                "Com a Vantagem Brutamontes, o bônus de Atletismo vale para toda rolagem, não só esforço físico — ainda não modelado automaticamente."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "tigre",
                        nome = "Tigre",
                        pacote = ResolvedTraitPackage(
                            anotacoes = listOf(
                                "Alcance de Comando +4 quadros, +1 em testes de Medo e -1 no resultado da Tabela de Medo (cumulativo com Corajoso) — nenhum efeito numérico modelado ainda."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "lebre",
                        nome = "Lebre",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Cura d6 (Lebre)", "LEBRE_CURA")),
                            anotacoes = listOf(
                                "1x por aventura, pode gastar um Bene pra tratar um Ferimento até 4 dias depois como se ainda estivesse na Hora de Ouro — ainda não modelado."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "garca",
                        nome = "Garça",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Aparar +1 (Garça)", "APARAR"),
                                TraitAddition("Acrobacia d4 (Garça)", "GARCA_ACROBACIA"),
                                TraitAddition("Atletismo d6 (Garça)", "GARCA_ATLETISMO")
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "serpente",
                        nome = "Serpente",
                        pacote = ResolvedTraitPackage(
                            anotacoes = listOf(
                                "Jogar d6 OU Performance d6 (à escolha do jogador, ver CriadorState.signoSerpentePericiaEscolhida — não modelado como Seleção estruturada porque o alvo muda por jogador). Bônus de +1/-1 na diferença de Jogar e os percentuais alterados de Performance ainda não modelados."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "dragao",
                        nome = "Dragão",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Espírito d6 (Dragão)", "ESPIRITUAL"),
                                TraitAddition(
                                    "Bônus de Perícia (+1): Conhecimento Geral (situação desconhecida)",
                                    "BONUS_PERICIA_1"
                                )
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "kirin",
                        nome = "Kirin",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Sorte (Vantagem concedida)", "SORTE"),
                                TraitAddition("Reserva de Chi +1 (Kirin)", "KIRIN_CHI")
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "macaco",
                        nome = "Macaco",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Astúcia d6 (Macaco)", "ASTUCIA")),
                            anotacoes = listOf(
                                "Perícias não treinadas de Astúcia rolam d4+1 em vez do padrão (sem valer pro dado selvagem) — ainda não modelado."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "raposa",
                        nome = "Raposa",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("Elevar a Moral (Vantagem concedida)", "ELEVAR_O_MORAL"),
                                TraitAddition("Bônus de Perícia (+1): Persuadir", "BONUS_PERICIA_1")
                            ),
                            anotacoes = listOf("+1 na Tabela de Reação ainda não modelado.")
                        )
                    ),
                    FixedPackageOption(
                        id = "lobo",
                        nome = "Lobo",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Elo Comum (Vantagem concedida)", "ELO_COMUM")),
                            anotacoes = listOf("+1 na Reação Inicial da Tabela de Reação ainda não modelado.")
                        )
                    ),
                    FixedPackageOption(
                        id = "tartaruga",
                        nome = "Tartaruga",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Resistência +1 (Tartaruga)", "RESISTENCIA")),
                            anotacoes = listOf(
                                "Quem tentar a manobra Finalização contra este personagem sofre -1 no ataque e no dano — ainda não modelado."
                            )
                        )
                    ),
                    FixedPackageOption(
                        id = "urso",
                        nome = "Urso",
                        pacote = ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(TraitAddition("Vigor d6 (Urso)", "VIGOROSO")),
                            anotacoes = listOf("Penalidade de Exausto reduzida pra -1 em vez de -2 — ainda não modelada.")
                        )
                    )
                )
            )
        )
    )

    // --- Meio-Orc (Fantasia) e Feral (Arte da Guerra): Seleção do tipo
    // TARGET_ATTRIBUTE_OR_SKILL — o jogador escolhe QUAL atributo recebe o
    // d6 inicial, não um pacote nomeado (mesma ideia de Herança/Signo acima,
    // mas o "efeito mecânico" é o próprio alvo escolhido, não um pacote de
    // traços fixo por opção). O marcador (ENDURECIDO/PRIMITIVO) já existe em
    // ancestralidades.json com custo cadastrado em RacialTraitPointCatalog.CUSTOS
    // (2, "oficial: aumento_atributo") — continua valendo pro cálculo de
    // orçamento da raça BASE (ResolveVariantPointBudgetUseCase.valorTotalDe),
    // já que o traço resolvido (ATTRIBUTE_BOOST) substitui o marcador em
    // habilidades[] em tempo de execução, não no JSON.
    private fun meioOrc(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MEIO-ORCS",
        livro = "FANTASIA",
        selecoes = listOf(
            SelectionDef(
                id = "meio_orc_atributo",
                rotulo = "Endurecido",
                tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                targetKind = TraitTargetKind.ATTRIBUTE,
                targetOptions = listOf("Força", "Vigor"),
                // Livro não define um padrão pra "Endurecido" — "Vigor"
                // preserva o comportamento default de antes desta raça
                // migrar pro mesmo mecanismo de Feral/Minerador.
                defaultTargetChoice = "Vigor",
                injectionTemplate = "{alvo} d6 (Endurecido)",
                marcadorTraitId = "ENDURECIDO"
            )
        )
    )

    private fun feralArteDaGuerra(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "FERAL",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "feral_atributo",
                rotulo = "Primitivo",
                tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                targetKind = TraitTargetKind.ATTRIBUTE,
                targetOptions = listOf("Força", "Vigor", "Agilidade"),
                defaultTargetChoice = "Força",
                injectionTemplate = "{alvo} d6 (Primitivo)",
                marcadorTraitId = "PRIMITIVO"
            )
        )
    )

    // --- Kitsunemimi (Raposa, Arte da Guerra): "Preparado" — escolhe 1 de 5
    // perícias listadas pelo livro pra começar em d4 (passos=1, mesmo custo
    // 1pt já cadastrado em RacialTraitPointCatalog pro marcador PREPARADO).
    // Achado real (rodada 36): antes só existia como `if` avulso em
    // CriadorState (habilidadeIdsPericia.contains("PREPARADO") +
    // kitsunemimiPericiaEscolhida) — funcionava, mas fora do sistema de
    // Seleção; migrado agora pro mesmo padrão de Meio-Orc/Feral.
    private fun kitsunemimiArteDaGuerra(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "KITSUNEMIMI (RAPOSA)",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "kitsunemimi_preparado",
                rotulo = "Preparado",
                tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                targetKind = TraitTargetKind.SKILL,
                targetOptions = listOf(
                    "Conhecimento Acadêmico", "Convenção", "Intimidar", "Pesquisar", "Provocar"
                ),
                defaultTargetChoice = "Conhecimento Acadêmico",
                // passos=0: livro diz "começar com d4" — perícia nasce
                // destreinada, então d4 já é o primeiro patamar (não "d6",
                // que seria passos=1/padrão da classe). Custo 1pt (ver
                // RacialTraitPointCatalog.custoDe SKILL_BOOST).
                passos = 0,
                injectionTemplate = "{alvo} d4 (Preparado)",
                marcadorTraitId = "PREPARADO"
            )
        )
    )

    // --- Gnomo (Pathfinder): "Obsessivos" — escolhe 1 perícia baseada em
    // Astúcia (dentre as do próprio livro Pathfinder) pra começar em d4.
    // Lista estática (mesma ideia de Meio-Orc Força/Vigor), gerada a partir
    // de todas as perícias de Astúcia cadastradas em pericias.json com
    // "PATHFINDER" nos livros (12 no total — a rodada 36 migrou isso com
    // essa lista faltando "Provocar" por engano; corrigido na rodada 37 ao
    // conferir contra o catálogo real, não de memória).
    private fun gnomoPathfinder(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "GNOMO",
        livro = "PATHFINDER",
        selecoes = listOf(
            SelectionDef(
                id = "gnomo_obsessivos",
                rotulo = "Obsessivos",
                tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                targetKind = TraitTargetKind.SKILL,
                targetOptions = listOf(
                    "Conhecimento de Batalha", "Ciência", "Conhecimento Acadêmico",
                    "Conhecimento Geral", "Conjurar", "Consertar", "Curar", "Jogar",
                    "Ocultismo", "Perceber", "Provocar", "Sobrevivência"
                ),
                defaultTargetChoice = "Conhecimento Acadêmico",
                // passos=0: mesmo caso de Kitsunemimi acima — "d4" é o
                // primeiro patamar de uma perícia destreinada.
                passos = 0,
                injectionTemplate = "{alvo} d4 (Obsessivos)",
                marcadorTraitId = "OBSESSIVOS"
            )
        )
    )

    // --- Usagimimi (Coelho, Arte da Guerra): "Definido pelo Ofício" —
    // escolhe 1 perícia dentre TODAS as da Arte da Guerra (exceto
    // Idiomas/Jutsu, que não são perícias "normais" pra esse efeito) pra
    // começar em d6 — não d4: o livro já concede o patamar treinado direto
    // (`passos=1`, o default de SelectionDef, então nem precisa declarar).
    // Lista de 29 perícias gerada a partir de pericias.json (livros
    // contendo "ARTE_DA_GUERRA", nome não começando com "Idiomas"/"Jutsu")
    // — a UI antiga já mostrava exatamente esse conjunto calculado em tempo
    // de execução (`state.listaPericias.filter{...}`); virou lista estática
    // aqui, mesmo padrão de Kitsunemimi/Gnomo (achado real da rodada 36: um
    // "picker de qualquer perícia" preocupava por ser ilimitado, mas o
    // livro já restringe a um conjunto finito e pequeno o bastante pra
    // enumerar, igual às outras duas raças).
    private fun usagimimiArteDaGuerra(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "USAGIMIMI (COELHO)",
        livro = "ARTE_DA_GUERRA",
        selecoes = listOf(
            SelectionDef(
                id = "usagimimi_definido_pelo_oficio",
                rotulo = "Definido pelo Ofício",
                tipo = SelectionType.TARGET_ATTRIBUTE_OR_SKILL,
                targetKind = TraitTargetKind.SKILL,
                targetOptions = listOf(
                    "Acrobacia", "Atirar", "Atletismo", "Cavalgar", "Ciência",
                    "Conhecimento Acadêmico", "Conhecimento Geral", "Conhecimento de Batalha",
                    "Consertar", "Convenção", "Curar", "Dirigir", "Foco", "Furtividade",
                    "Intimidar", "Jogar", "Ladinagem", "Lutar", "Navegar", "Ocultismo",
                    "Ofício", "Perceber", "Performance", "Persuadir", "Pesquisar",
                    "Pilotar", "Provocar", "Sobrevivência", "Transição"
                ),
                defaultTargetChoice = "Conhecimento Acadêmico",
                injectionTemplate = "{alvo} d6 (Definido pelo Ofício)",
                marcadorTraitId = "DEFINIDO_PELO_OFICIO"
            )
        )
    )
}
