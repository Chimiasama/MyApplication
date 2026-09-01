package com.example.swadebuilder.registry

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.ArmaNatural
import com.example.swadebuilder.model.FixedPackageOption
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.TraitAddition
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
 * pelo motor novo (ex.: Feral — tem traços fixos, não variante nem seleção).
 */
object AncestryVariantRegistry {

    private val configs: Map<String, AncestryVariantConfig> = listOf(
        terracota(),
        umvee(),
        elementaisScifi(),
        anoes(),
        rakashanos(),
        sauriosScifi(),
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
        seresSinteticos()
    ).associateBy { it.ancestralidadeId }

    fun get(ancestralidadeId: String): AncestryVariantConfig? = configs[ancestralidadeId]

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
        "RAKASHANOS", "SAURIOS", "AQUARIANOS", "AVIANOS", "ELFOS", "HUMANOS",
        "CENTAUX", "DRAKENS", "FERAIS", "FLORANS", "GELATINOIDES", "INSETOIDES",
        "MIMICOS", "MINERADORES GENETICOS", "ORACULOS", "POSSESSORES",
        "QUADROIDES", "SOLDADOS GENETICOS", "YETIS", "ROBOS", "SERES SINTETICOS"
    )

    // --- Lote 2: raças Sci-Fi com Variante real de 2 opções (Básico/Padrão +
    // 1 reconfiguração de cenário), migradas de ResolveAncestrySpecificAdjustmentsUseCase
    // (bloco `if (isSciFiActive) { if (ancKey == "X") ... }`). Mesmo padrão do
    // lote piloto: cada opção carrega só as ADIÇÕES/REMOÇÕES de traços — a
    // Armadura Natural (quando difere de 0, ex.: Sáurios/Insetoides) e o
    // `forceArmorZero` seguem como exceção pontual na camada de wiring
    // (ResolveAncestrySpecificAdjustmentsUseCase), igual ao Umvee Pedregoso.

    private fun rakashanos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "RAKASHANOS",
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

    private fun sauriosScifi(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "SAURIOS",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "basico",
                    nome = "Básico",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(TraitAddition("PRONTIDÃO", "PRONTIDAO")),
                        tracosParaAdicionar = listOf(TraitAddition("MORDIDA", "MORDIDA")),
                        // Sáurios "Mordida" nunca foi um campo fixo no JSON
                        // da raça (só existe pra Básico) — sem isso aqui,
                        // extrairArmasNaturais só achava a arma por
                        // casamento de palavra-chave em texto solto.
                        armasNaturaisParaAdicionar = listOf(ArmaNatural(nome = "Mordida", dano = "For+d4")),
                        naturalArmor = 2
                    )
                ),
                VariantOption(
                    id = "cuspidor",
                    nome = "Cuspidor",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("TOQUE VENENOSO (Cuspidor)", "TOQUE_VENENOSO_CUSPIDOR")),
                        naturalArmor = 2
                    )
                )
            )
        )
    )

    private fun aquarianos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "AQUARIANOS",
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
                    )
                )
            )
        )
    )

    private fun centaux(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "CENTAUX",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        // Mesmos ids que já existem nativamente em
                        // ancestralidades.json pra Centaux/Aurax — "Padrão" só
                        // reafirma o que a raça base já concede (existe pra
                        // "Gazela" poder trocar por MOVIMENTACAO_4 abaixo).
                        tracosParaAdicionar = listOf(
                            TraitAddition("TAMANHO +2", "TAMANHO_MAIS_2"),
                            TraitAddition("MOVIMENTAÇÃO +2", "MOVIMENTACAO_2")
                        )
                    )
                ),
                VariantOption(
                    id = "gazela",
                    nome = "Gazela",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("MOVIMENTAÇÃO +4", "MOVIMENTACAO_4")),
                        tracosParaRemoverPorNome = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"),
                        desvantagensParaRemover = listOf("GRANDE")
                    )
                )
            )
        )
    )

    private fun drakens(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "DRAKENS",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("FORTE", "FORTE"),
                            TraitAddition("RESISTÊNCIA +2", "RESISTENCIA_2")
                        )
                    )
                ),
                VariantOption(
                    id = "dragao",
                    nome = "Dragão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("ARMA DE SOPRO (Fogo)", "ARMA_DE_SOPRO_FOGO"))
                    )
                )
            )
        )
    )

    private fun ferais(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "FERAIS",
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
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("TOQUE VENENOSO (Paralisante)", "TOQUE_VENENOSO_PARALISANTE"))
                    )
                )
            )
        )
    )

    private fun gelatinoides(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "GELATINOIDES",
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("CAMUFLAGEM", "CAMUFLAGEM")))
                )
            )
        )
    )

    private fun insetoidesScifi(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "INSETOIDES",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("ARMADURA +2", "ARMADURA_2"),
                            TraitAddition("GARRAS", "GARRAS")
                        ),
                        armasNaturaisParaAdicionar = listOf(ArmaNatural(nome = "Garras", dano = "For+d4", pa = 2, escalavel = true)),
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
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("RESISTÊNCIA +1", "RESISTENCIA_1"),
                            TraitAddition("MUDANÇA DE FORMA (Sem variação de tamanho)", "MUDAR_DE_FORMA_SEM_VARIACAO_DE_TAMANHO")
                        )
                    )
                )
            )
        )
    )

    private fun mineradoresGeneticos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "MINERADORES GENETICOS",
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition("FORTE", "FORTE"),
                            TraitAddition("DEPENDÊNCIA ATMOSFÉRICA", "DEPENDENCIA_ATMOSFERICA")
                        )
                    )
                ),
                VariantOption(
                    id = "zero_g",
                    nome = "Zero G",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf(TraitAddition("ADAPTAÇÃO GRAVITACIONAL", "ADAPTACAO_GRAVITACIONAL")),
                        vantagensGratisIds = listOf("adaptacao_gravitacional"),
                        tracosParaRemoverPorNome = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA"),
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
        grupoVariante = VariantGroup(
            opcoes = listOf(
                VariantOption(
                    id = "padrao",
                    nome = "Padrão",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(TraitAddition("AÇÃO ADICIONAL (Física)", "ACAO_ADICIONAL_FISICA")),
                        desvantagensParaAdicionar = listOf(TraitAddition("SENSÍVEL (Maior)", "SENSIVEL_MAIOR"))
                    )
                ),
                VariantOption(
                    id = "habilidoso",
                    nome = "Habilidoso",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf(
                            TraitAddition(
                                "AÇÃO ADICIONAL (Ignora 2 pontos de penalidade por Ações Múltiplas)",
                                "ACAO_ADICIONAL_IGNORA_PENALIDADE_ACOES_MULTIPLAS"
                            )
                        ),
                        desvantagensParaAdicionar = listOf(TraitAddition("SENSÍVEL (Maior)", "SENSIVEL_MAIOR")),
                        // Nota pro mestre, não uma desvantagem de verdade —
                        // pertence a `anotacoes`, não a `desvantagensParaAdicionar`.
                        anotacoes = listOf(
                            "Combine com o mestre de jogo para equilibrar com 1 ponto de habilidade negativa que faça sentido ao cenário."
                        )
                    )
                )
            )
        )
    )

    private fun soldadosGeneticos(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "SOLDADOS GENETICOS",
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
    // Natureza". Efeitos conferidos exatamente como o código atual já faz em
    // ResolveAncestrySpecificAdjustmentsUseCase (linhas ~714-780): a maioria
    // é só texto automático (tracosParaAdicionar -> ensureAutomaticAdvantages
    // na camada de wiring), mas "Vínculo Bestial" concede uma Vantagem de
    // verdade (vantagensGratisParaAdicionar -> ensureAdvantageNames, com
    // todos os ganchos mecânicos da vantagem "Senhor das Feras"). "Pedregoso"
    // também define naturalArmorFromRace=2 no Result — isso fica como
    // exceção pontual na camada de wiring, não faz parte do pacote genérico
    // (não é comum o suficiente pra merecer campo próprio no schema).
    // A injeção de "Perceber d6"/"Ocultismo d4" do Gatoruja continua vindo
    // de applyAncestryVariantAdjustments (não duplicada aqui).
    private fun umvee(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "UMVEE (FILHOS DA LUA)",
        selecoes = listOf(
            SelectionDef(
                id = "umvee_dom_da_natureza",
                rotulo = "Escolha o Dom da Natureza",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption("apice", "Ápice", ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("GARRAS", "GARRAS")))),
                    FixedPackageOption(
                        "vinculo_bestial", "Vínculo Bestial",
                        ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf(TraitAddition("SENHOR DAS FERAS", "SENHOR_DAS_FERAS")))
                    ),
                    FixedPackageOption(
                        "pele_iluminada_pela_lua", "Pele Iluminada pela Lua",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("APARAR +1", "APARAR_1")))
                    ),
                    FixedPackageOption(
                        "gatoruja", "Gatoruja",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("VISÃO NO ESCURO", "VISAO_NO_ESCURO")))
                    ),
                    FixedPackageOption(
                        "correnteza", "Correnteza",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("MOVIMENTAÇÃO +2", "MOVIMENTACAO_2")))
                    ),
                    FixedPackageOption(
                        // Id "RESISTENCIA" (bare), não "RESISTENCIA_1": tem que
                        // ser EXATAMENTE o mesmo id que CriadorState.
                        // applyAncestryVariantAdjustments já injeta em
                        // newHabilidades pro caso "Pedregoso" — os dois
                        // caminhos rodam pra Umvee (esse aqui é só bookkeeping
                        // redundante de vantagensRaciais), então um id
                        // diferente pro mesmo efeito contaria a Resistência em
                        // dobro (o mesmo tipo de bug já corrigido pro Tamanho
                        // de Fadas/Povo Rato — ver ModifierEngineAdgAncestryTest).
                        "pedregoso", "Pedregoso",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("RESISTÊNCIA +1", "RESISTENCIA")))
                    )
                )
            )
        )
    )

    // --- Elementais (Sci-Fi): apesar de ter "Padrão" entre as opções (o que
    // normalmente indicaria Variante), o usuário confirmou que este é o caso
    // excepcional: é Seleção de elemento mesmo (todo elemental É de algum
    // elemento, igual ao Descendente Elemental de Fantasia) — só foi
    // implementado no sistema de variante antigo por falta de alternativa na
    // época. Efeitos idênticos aos que já existiam no "when" fixo de
    // ResolveAncestrySpecificAdjustmentsUseCase: Padrão mantém Forte e
    // Resistência +2 (a raça é de pedra/terra, física e resistente); Ar,
    // Fogo ou Água troca os dois por Forma de Energia (o corpo já não é mais
    // sólido nem musculoso). ---
    private fun elementaisScifi(): AncestryVariantConfig = AncestryVariantConfig(
        ancestralidadeId = "ELEMENTAIS",
        selecoes = listOf(
            SelectionDef(
                id = "elementais_scifi_elemento",
                rotulo = "Escolha o elemento",
                tipo = SelectionType.FIXED_PACKAGE,
                pacotesFixos = listOf(
                    FixedPackageOption(
                        "padrao",
                        "Padrão",
                        ResolvedTraitPackage(
                            tracosParaAdicionar = listOf(
                                TraitAddition("FORTE", "FORTE"),
                                TraitAddition("RESISTÊNCIA +2", "RESISTENCIA_2")
                            )
                        )
                    ),
                    FixedPackageOption(
                        "ar_fogo_ou_agua",
                        "Ar, Fogo ou Água",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf(TraitAddition("FORMA DE ENERGIA", "FORMA_DE_ENERGIA")))
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
}
