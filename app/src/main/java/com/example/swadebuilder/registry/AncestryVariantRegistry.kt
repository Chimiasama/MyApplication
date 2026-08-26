package com.example.swadebuilder.registry

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.FixedPackageOption
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionDef
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.VariantGroup
import com.example.swadebuilder.model.VariantOption

/**
 * Catálogo central de Variantes/Seleções por ancestralidade, indexado por id
 * estável (mesmo valor de `ancestralidade.keyify()` usado no resto do app).
 * Lote piloto: Terracota, Umvee, Elementais (Sci-Fi), Anões. As outras 21+
 * raças com variante/seleção entram em lotes seguintes, no mesmo padrão.
 *
 * Uma raça ausente daqui simplesmente não tem variante nem seleção conhecida
 * pelo motor novo (ex.: Feral — tem traços fixos, não variante nem seleção).
 */
object AncestryVariantRegistry {

    private val configs: Map<String, AncestryVariantConfig> = listOf(
        terracota(),
        umvee(),
        elementaisScifi(),
        anoes()
    ).associateBy { it.ancestralidadeId }

    fun get(ancestralidadeId: String): AncestryVariantConfig? = configs[ancestralidadeId]

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
