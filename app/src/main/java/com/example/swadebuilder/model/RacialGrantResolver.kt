package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify

/**
 * Resolução de uma entrada solta de `RacialModifier.vantagensGratis` ou
 * `.desvantagens` (hoje `List<String>`: às vezes um nome de exibição, às
 * vezes já um id) pro id real de Vantagem/Complicação do catálogo geral —
 * quando existe um — e pro custo em pontos correspondente. Isso é o que
 * permite remover um desses grants numa Variante custom e devolver o ponto
 * certo, em vez de só apagar um texto solto sem saber quanto ele valia.
 *
 * Só 10 vantagensGratis e 21 desvantagens distintas existem hoje em
 * ancestralidades.json (bem menor que as 205 de habilidades[]), então cada
 * uma foi conferida à mão contra vantagens.json/complicacoes.json — não é
 * fuzzy match automático. Onde o texto corresponde a uma Vantagem/Complicação
 * real do catálogo, o custo vem de lá (Complicação: Menor = -1, Maior = -2,
 * usando a severidade que a própria ancestralidade escolheu quando a
 * Complicação do catálogo permite "Menor ou Maior"); onde não existe
 * contrapartida real (ex.: "RESISTÊNCIA"/"MORDIDA" nesses campos são, na
 * prática, o mesmo traço bespoke de sempre, só que colocado nessas listas em
 * vez de em habilidades[]), o custo cai pra `RacialTraitPointCatalog`.
 */
data class RacialGrantLink(
    val texto: String,
    val custo: Int,
    val vantagemId: String? = null,
    val complicacaoId: String? = null
)

object RacialGrantResolver {

    private val VANTAGENS_GRATIS: Map<String, RacialGrantLink> = listOf(
        // "Adaptável" aqui é o traço racial dos Humanos (Vantagem Novato à
        // escolha), não uma Vantagem específica do catálogo — sem vantagemId.
        RacialGrantLink("ADAPTÁVEL", RacialTraitPointCatalog.custoDe("ADAPTAVEL")),
        RacialGrantLink("ANTECEDENTE ARCANO (Milagres)", 3, vantagemId = "antecedente_arcano_milagres"),
        RacialGrantLink("CAMPEÃO", 2, vantagemId = "campeao"),
        RacialGrantLink("CATIVAR O AMBIENTE", 2, vantagemId = "cativar_o_ambiente"),
        RacialGrantLink("IMPULSO", 2, vantagemId = "impulso"),
        // "Mordida"/"Resistência" nesses dois casos (Inumimi) são o traço de
        // arma natural / Resistência +1 de sempre, só registrados em
        // vantagensGratis em vez de habilidades[] — não são Vantagens reais.
        RacialGrantLink("MORDIDA", RacialTraitPointCatalog.custoDe("MORDIDA")),
        RacialGrantLink("PRONTIDÃO", 2, vantagemId = "prontidao"),
        RacialGrantLink("RESISTÊNCIA", RacialTraitPointCatalog.custoDe("RESISTENCIA")),
        RacialGrantLink("Sorte", 2, vantagemId = "sorte"),
        // Único caso hoje em que o valor já vem como id em vez de nome.
        RacialGrantLink("aa_agente_syn", 3, vantagemId = "aa_agente_syn")
    ).associateBy { it.texto.keyify() }

    private val DESVANTAGENS: Map<String, RacialGrantLink> = listOf(
        // Strings que só existem como injeção de texto pro ModifierEngine
        // (mesmo padrão usado em toda a Fase 6), sem Complicação real por trás.
        RacialGrantLink("APARAR -1", RacialTraitPointCatalog.custoDe("APARAR_BAIXO")),
        RacialGrantLink("MOVIMENTAÇÃO -1", RacialTraitPointCatalog.custoDe("MOVIMENTACAO_REDUZIDA")),
        RacialGrantLink("Ancestralidade Infame", RacialTraitPointCatalog.custoDe("ANCESTRALIDADE_INFAME")),
        RacialGrantLink("FRÁGIL", RacialTraitPointCatalog.custoDe("FRAGIL")), // não existe como Complicação geral, só traço racial
        RacialGrantLink("FRAQUEZA AMBIENTAL (Frio)", RacialTraitPointCatalog.custoDe("FRAQUEZA_AMBIENTAL")),
        RacialGrantLink("INIMIGO RACIAL", RacialTraitPointCatalog.custoDe("INIMIGO_RACIAL")), // diferente da Complicação geral "Inimigo"
        RacialGrantLink("INIMIGO RACIAL (Demônios e Diabos)", RacialTraitPointCatalog.custoDe("INIMIGO_RACIAL_DEMONIOS_E_DIABOS")),
        // Mesmo efeito de Sorte/Fortuna Dá, sem Vantagem vinculada nesse caso.
        RacialGrantLink(
            "Bene adicional por sessão (benefício racial; acumula com Sorte e Sorte Grande).",
            2
        ),

        // Complicações reais do catálogo (complicacoes.json), custo pela
        // severity de lá. AZARADO: o JSON de Nekomimi anota "Menor", mas a
        // Complicação real só existe como Maior — o catálogo geral é a fonte
        // de verdade, custo -2.
        RacialGrantLink("AZARADO", -2, complicacaoId = "azarado"),
        RacialGrantLink("CAUTELOSO", -1, complicacaoId = "cauteloso"),
        RacialGrantLink("DEPENDÊNCIA", -2, complicacaoId = "dependencia"),
        RacialGrantLink("DESASTRADO", -1, complicacaoId = "desastrado"),
        RacialGrantLink("HÁBITO", -1, complicacaoId = "habito"),
        RacialGrantLink("LEAL", -1, complicacaoId = "leal"),
        RacialGrantLink("PECULIARIDADE", -1, complicacaoId = "peculiaridade"),

        // Forasteiro/Pacifista/Voto existem no catálogo com severidade "Menor
        // ou Maior" (a ancestralidade escolhe qual) — linka pro id genérico,
        // custo pela severidade que o texto já traz entre parênteses.
        RacialGrantLink("FORASTEIRO (Maior)", -2, complicacaoId = "forasteiro"),
        RacialGrantLink("FORASTEIRO (Menor)", -1, complicacaoId = "forasteiro"),
        RacialGrantLink("PACIFISTA (Maior)", -2, complicacaoId = "pacifista"),
        RacialGrantLink("VOTO (Maior)", -2, complicacaoId = "voto"),
        RacialGrantLink("VOTO (Maior: Proteger a Humanidade)", -2, complicacaoId = "voto"),
        RacialGrantLink("VOTO (Maior: Proteger a humanidade)", -2, complicacaoId = "voto")
    ).associateBy { it.texto.keyify() }

    fun resolveVantagemGratis(texto: String): RacialGrantLink =
        VANTAGENS_GRATIS[texto.keyify()] ?: RacialGrantLink(texto = texto, custo = 0)

    fun resolveDesvantagem(texto: String): RacialGrantLink =
        DESVANTAGENS[texto.keyify()] ?: RacialGrantLink(texto = texto, custo = 0)
}
