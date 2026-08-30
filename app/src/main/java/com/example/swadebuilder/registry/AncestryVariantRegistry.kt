package com.example.swadebuilder.registry

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.ArmaNatural
import com.example.swadebuilder.model.FixedPackageOption
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
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
                    pacoteFixo = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("SANGUINÁRIO (Maior)"))
                ),
                VariantOption(
                    id = "brincalhao",
                    nome = "Brincalhão",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf("CURIOSO (Maior)"),
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
                        vantagensGratisParaAdicionar = listOf("PRONTIDÃO"),
                        tracosParaAdicionar = listOf("MORDIDA"),
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
                        tracosParaAdicionar = listOf("TOQUE VENENOSO (Cuspidor)"),
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
                        tracosParaAdicionar = listOf("SEMIAQUÁTICO", "TOQUE VENENOSO"),
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
                    pacoteFixo = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("FRÁGIL", "NÃO SABE NADAR"))
                ),
                VariantOption(
                    id = "ave_de_rapina",
                    nome = "Ave de rapina",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf(
                            "HABITANTE DE GRAVIDADE ZERO/BAIXA",
                            "FORMA ALIENÍGENA",
                            "SENTIDOS AGUÇADOS (Olhos de Águia)"
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
                    pacoteFixo = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("DESASTRADO (Menor)"))
                ),
                VariantOption(
                    id = "comunitario",
                    nome = "Comunitário",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("COMUNITÁRIO"),
                        tracosParaRemoverPorNome = listOf("DESASTRADO"),
                        desvantagensParaAdicionar = listOf("TRANSTORNO DE SEPARAÇÃO"),
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
                        desvantagensParaAdicionar = listOf("HABITANTE DE GRAVIDADE BAIXA")
                    )
                ),
                VariantOption(
                    id = "minerador",
                    nome = "Minerador",
                    pacoteFixo = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("DEPENDÊNCIA ATMOSFÉRICA (Maior)"))
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("TAMANHO +2", "MOVIMENTAÇÃO +2"))
                ),
                VariantOption(
                    id = "gazela",
                    nome = "Gazela",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("MOVIMENTAÇÃO +4"),
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("FORTE", "RESISTÊNCIA +2"))
                ),
                VariantOption(
                    id = "dragao",
                    nome = "Dragão",
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("ARMA DE SOPRO (Fogo)"))
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("DIMINUTO (Tamanho -3)"))
                ),
                VariantOption(
                    id = "menor",
                    nome = "Menor",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("DIMINUTO (Tamanho -4)"),
                        tracosParaRemoverPorNome = listOf("ESPIRITUOSO"),
                        desvantagensParaAdicionar = listOf("TRANSTORNO DE SEPARAÇÃO"),
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("ROBUSTO"))
                ),
                VariantOption(
                    id = "defensivo",
                    nome = "Defensivo",
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("TOQUE VENENOSO (Paralisante)"))
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("REGENERAÇÃO"))
                ),
                VariantOption(
                    id = "ameba",
                    nome = "Ameba",
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("CAMUFLAGEM"))
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
                        tracosParaAdicionar = listOf("ARMADURA +2", "GARRAS"),
                        armasNaturaisParaAdicionar = listOf(ArmaNatural(nome = "Garras", dano = "For+d4", pa = 2, escalavel = true)),
                        naturalArmor = 2
                    )
                ),
                VariantOption(
                    id = "vespa",
                    nome = "Vespa",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("FERRÃO (Mordida For+d4)", "VOO (Movimentação 6)", "TOQUE VENENOSO (Moderado)"),
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("MUDANÇA DE FORMA"))
                ),
                VariantOption(
                    id = "resistente",
                    nome = "Resistente",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("RESISTÊNCIA +1", "MUDANÇA DE FORMA (Sem variação de tamanho)")
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
                    pacoteFixo = ResolvedTraitPackage(tracosParaAdicionar = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA"))
                ),
                VariantOption(
                    id = "zero_g",
                    nome = "Zero G",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisParaAdicionar = listOf("ADAPTAÇÃO GRAVITACIONAL"),
                        vantagensGratisIds = listOf("adaptacao_gravitacional"),
                        tracosParaRemoverPorNome = listOf("FORTE", "DEPENDÊNCIA ATMOSFÉRICA"),
                        desvantagensParaAdicionar = listOf("HABITANTE DE GRAVIDADE ZERO/BAIXA (Maior)"),
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
                        vantagensGratisParaAdicionar = listOf("NOÇÃO DO PERIGO"),
                        vantagensGratisIds = listOf("nocao_do_perigo")
                    )
                ),
                VariantOption(
                    id = "aterrorizado",
                    nome = "Aterrorizado",
                    pacoteFixo = ResolvedTraitPackage(
                        vantagensGratisIds = listOf("poderes_misticos"),
                        tracosParaAdicionar = listOf("PODERES MÍSTICOS (TELEPATA)"),
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
                        tracosParaAdicionar = listOf("FORMA DE ENERGIA"),
                        tracosParaRemoverPorNome = listOf("NOÇÃO DO PERIGO", "NOCAO DO PERIGO"),
                        desvantagensParaAdicionar = listOf(
                            "Combine com o mestre de jogo para equilibrar com 4 pontos de habilidades negativas que façam sentido\nno cenário."
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
                        tracosParaAdicionar = listOf("AÇÃO ADICIONAL (Física)"),
                        desvantagensParaAdicionar = listOf("SENSÍVEL (Maior)")
                    )
                ),
                VariantOption(
                    id = "habilidoso",
                    nome = "Habilidoso",
                    pacoteFixo = ResolvedTraitPackage(
                        tracosParaAdicionar = listOf("AÇÃO ADICIONAL (Ignora 2 pontos de penalidade por Ações Múltiplas)"),
                        desvantagensParaAdicionar = listOf(
                            "SENSÍVEL (Maior)",
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
                    pacoteFixo = ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf("NERVOS DE AÇO", "REFLEXOS DE COMBATE"))
                ),
                VariantOption(
                    id = "fuzileiro_zero_g",
                    nome = "Fuzileiro Zero G",
                    pacoteFixo = ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf("ADAPTAÇÃO GRAVITACIONAL", "REFLEXOS DE COMBATE"))
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
                        desvantagensParaAdicionar = listOf("SEM ESCRÚPULOS (Maior)"),
                        desvantagensParaRemover = listOf(
                            "CIRCUITOS DE ASIMOV (Maior)", "CIRCUITOS DE ASIMOV", "PACIFISTA (Maior)", "PACIFISTA"
                        )
                    )
                ),
                VariantOption(
                    id = "limitado",
                    nome = "Limitado",
                    pacoteFixo = ResolvedTraitPackage(
                        // Id fixo e previsível (não o slug auto-derivado do
                        // texto) porque isPericiaBasicaEfetiva/periciaStartRawInternal
                        // em CriadorState checam esse id diretamente pra saber
                        // se removem o d4 grátis de todas as perícias básicas.
                        tracosParaAdicionar = listOf("PERÍCIAS BÁSICAS REDUZIDAS (TOTAL)"),
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
                    pacoteFixo = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("PROGRAMADO"))
                ),
                VariantOption(
                    id = "maquina_procurado",
                    nome = "Máquina (Procurado)",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf("PROCURADO (Maior)"),
                        desvantagensParaRemover = listOf("PROGRAMADO (Maior)")
                    )
                ),
                VariantOption(
                    id = "maquina_forasteiro",
                    nome = "Máquina (Forasteiro)",
                    pacoteFixo = ResolvedTraitPackage(
                        desvantagensParaAdicionar = listOf("FORASTEIRO (Maior)"),
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
                        tracosParaAdicionar = listOf("ARMA DE SOPRO (Frio)"),
                        desvantagensParaAdicionar = listOf("DEPENDÊNCIA")
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
                        pacote = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("VOTO (Maior)"))
                    ),
                    FixedPackageOption(
                        id = "obrigacao",
                        nome = "Obrigação (Maior)",
                        pacote = ResolvedTraitPackage(desvantagensParaAdicionar = listOf("OBRIGAÇÃO (Maior)"))
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
                    FixedPackageOption("apice", "Ápice", ResolvedTraitPackage(tracosParaAdicionar = listOf("GARRAS"))),
                    FixedPackageOption("vinculo_bestial", "Vínculo Bestial", ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf("SENHOR DAS FERAS"))),
                    FixedPackageOption("pele_iluminada_pela_lua", "Pele Iluminada pela Lua", ResolvedTraitPackage(tracosParaAdicionar = listOf("APARAR +1"))),
                    FixedPackageOption("gatoruja", "Gatoruja", ResolvedTraitPackage(tracosParaAdicionar = listOf("VISÃO NO ESCURO"))),
                    FixedPackageOption("correnteza", "Correnteza", ResolvedTraitPackage(tracosParaAdicionar = listOf("MOVIMENTAÇÃO +2"))),
                    FixedPackageOption("pedregoso", "Pedregoso", ResolvedTraitPackage(tracosParaAdicionar = listOf("RESISTÊNCIA +1")))
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
                        ResolvedTraitPackage(tracosParaAdicionar = listOf("FORTE", "RESISTÊNCIA +2"))
                    ),
                    FixedPackageOption(
                        "ar_fogo_ou_agua",
                        "Ar, Fogo ou Água",
                        ResolvedTraitPackage(tracosParaAdicionar = listOf("FORMA DE ENERGIA"))
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
                    pacoteFixo = ResolvedTraitPackage(vantagensGratisParaAdicionar = listOf("CIBERTOLERÂNCIA")),
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
