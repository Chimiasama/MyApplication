package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify

/**
 * Custo em pontos de cada traço racial (entradas de `habilidades[]` em
 * ancestralidades.json), indexado pelo mesmo id estável usado em
 * `RacialAbility.id`. Positivo = pontos que o traço custa pra "comprar" ao
 * montar uma Variante custom de raça; negativo = pontos que o traço devolve
 * ao orçamento por ser uma desvantagem.
 *
 * A fonte de verdade é `basico_habilidades_raciais.json` — o catálogo
 * oficial de criação de raças (111 entradas) que já existe no app e já
 * alimenta a tela de "Criar Raça" do conteúdo customizado. Cada um dos ~205
 * traços aqui foi conferido contra esse catálogo por conceito (não por id —
 * os ids são de namespaces diferentes): quando existe um traço oficial
 * equivalente, o custo daqui é o dele; sem equivalente oficial (traços bem
 * específicos/narrativos de uma raça, tipo "Ancestralidade Infame" dos
 * Onigem), o custo é julgamento próprio calibrado na mesma escala do
 * catálogo oficial. Comentário de cada entrada indica quando o valor vem
 * direto de lá.
 *
 * Duas correções relevantes que esse catálogo trouxe: penalidade em
 * ATRIBUTO (Agilidade/Astúcia/Espírito/Força/Vigor) vale -2, não -1 —
 * diferente de penalidade numa PERÍCIA (-1), porque afeta várias perícias
 * derivadas daquele atributo; e perícia começando em d6 vale 2 pontos,
 * perícia começando em d4 vale 1 (não os dois no mesmo valor).
 *
 * Escala geral (mesma do catálogo oficial):
 *   +1  perícia d4 inicial, bônus +1 pontual, arma natural básica (For+d4),
 *       Resistência/Armadura +1, sentido especial simples
 *   +2  um passo de atributo (d4->d6), perícia d6 inicial, Vantagem
 *       concedida de graça, Movimentação +2
 *   +3 a +10  efeitos fortes ou pacotes com vários efeitos combinados
 *       (Espacial=3, Voo Mov 12=4, Robô=6, Construto/Morto-Vivo=8,
 *       Ações Adicionais (Maior) do Fantasia=10 — o maior valor confirmado
 *       nos 3 livros)
 *   -1  Complicação Menor equivalente, penalidade -1 numa perícia
 *   -2  Complicação Maior equivalente, penalidade -1 num ATRIBUTO,
 *       penalidade -2 numa perícia
 *   -3/-4  pacote de desvantagens combinadas ou penalidade -2 num atributo
 *
 * Alguns ids ficam com custo 0 de propósito: são placeholders de Seleção, não
 * um efeito único e fixo — o jogador escolhe entre várias opções dentro do
 * próprio traço (ex.: "Dons da Natureza" do Umvee escolhe 1 de 6; "Signos de
 * Nascença" escolhe 1 de 13) e é a opção resolvida que carrega efeito
 * mecânico de verdade. Contar pontos no placeholder também contaria de novo
 * no traço injetado pela escolha — Terracota/Umvee/Elementais já são
 * resolvidos assim em AncestryVariantRegistry.
 */
/**
 * Efeito mecânico estruturado de um traço racial, quando ele sobe um
 * atributo ou perícia de alvo FIXO (a raça sempre sobe o mesmo, ex.: Ágil
 * sempre é Agilidade) — não cobre traços de alvo à ESCOLHA do jogador (ex.:
 * Meio-Orc escolhe Força ou Vigor; Feral escolhe entre 3 atributos), que
 * continuam resolvidos pelo state dedicado de cada raça, como já eram.
 *
 * É isso que substitui os `if (habilidadeIds.contains("AGIL") && attrKey ==
 * "AGILIDADE")` fixos que existiam em `atributoBaseRacial()`: em vez do
 * código saber "de cor" qual atributo cada id sobe, o dado já vem com essa
 * informação — o traço só precisa estar presente, o próprio `RacialTraitEffect`
 * diz o quê e quanto.
 */
sealed class RacialTraitEffect {
    data class AtributoStep(val atributo: String, val passos: Int = 1) : RacialTraitEffect()
    data class PericiaStep(val pericia: String, val passos: Int = 1) : RacialTraitEffect()
    // Bônus fixo (não "passo de dado") de Resistência/Passo/Aparar — mesma
    // ideia de AtributoStep/PericiaStep, só que pra alvos que o ModifierEngine
    // já trata como valor plano (ModifierTarget.TOUGHNESS_FLAT/PACE/PARRY),
    // não como tipo de dado. Reúne num só lugar o que antes era um
    // `val hasX = ...; if (hasX) modifiers.add(...)` por traço dentro do
    // ModifierEngine — o traço só precisa estar presente (por id ou, pra
    // grants ainda guardados como texto solto em vantagensGratis/desvantagens,
    // por nome), o catálogo já diz o alvo e o valor.
    data class ResistenciaBonus(val valor: Int) : RacialTraitEffect()
    data class PassoBonus(val valor: Int) : RacialTraitEffect()
    data class ApararBonus(val valor: Int) : RacialTraitEffect()
    // Tamanho é ele mesmo (ModifierTarget.SIZE_DISPLAY) e também alimenta a
    // Resistência (ModifierTarget.SIZE_TOUGHNESS) — mesma fórmula do SWADE
    // (Resistência = 2 + metade do Vigor + Tamanho). `minusculo` marca as
    // raças com o traço Diminuto/Minúsculo do livro (Fadas, Povo Rato,
    // Ferais): Tamanho de personagem normalmente não passa de -1 na tela
    // (visualmente "trava" em -1), mas Diminuto é a exceção documentada que
    // permite mostrar -3/-4 de verdade — ModifierEngine.sizeDisplay() já
    // decide isso a partir de um Modifier com id "racial_diminuto", que o
    // loop de efeitos abaixo continua emitindo quando minusculo=true.
    data class TamanhoBonus(val valor: Int, val minusculo: Boolean = false) : RacialTraitEffect()
    // Armadura Natural de valor fixo (Sáurios, Draconianos, Golens,
    // Insetoides, Umvee Pedregoso). Não vira Modifier no ModifierEngine (a
    // Armadura final do personagem já é resolvida à parte, em
    // ResolveAncestrySpecificAdjustmentsUseCase/naturalArmorFromRace, que lê
    // este mesmo efeito por id) — existe aqui só pra esse cálculo e o
    // catálogo de custo/rótulo terem uma única fonte de verdade.
    data class ArmaduraBonus(val valor: Int) : RacialTraitEffect()
    // Um traço com mais de um efeito numérico ao mesmo tempo (ex.:
    // Despretensiosos e Barrigudos dos Tanukimimi: -1 Aparar E -1
    // Movimentação juntos, não um traço genérico por efeito). Cada
    // consumidor de RacialTraitEffect resolve os sub-efeitos com a mesma
    // lógica que já usa pro efeito único.
    data class Composite(val efeitos: List<RacialTraitEffect>) : RacialTraitEffect()
    data object Nenhum : RacialTraitEffect()
}

object RacialTraitPointCatalog {

    /**
     * Só os traços com efeito de alvo fixo num atributo/perícia (ver
     * `RacialTraitEffect`). Ausência aqui = `Nenhum`: ou o traço não tem
     * gancho mecânico numérico modelável, ou já é coberto pelo mecanismo de
     * injeção de texto que o ModifierEngine lê (Aparar/Resistência/
     * Movimentação, genérico desde o início desta unificação).
     */
    val EFEITOS: Map<String, RacialTraitEffect> = mapOf(
        "AGIL" to RacialTraitEffect.AtributoStep("Agilidade"),
        "ASTUCIA" to RacialTraitEffect.AtributoStep("Astúcia"),
        "ASTUTO" to RacialTraitEffect.AtributoStep("Astúcia"),
        "BAIXA_GRAVIDADE_AGIL" to RacialTraitEffect.AtributoStep("Agilidade"), // sintético, injetado por Humanos Sci-Fi "Baixa Gravidade" (ver applyAncestryVariantAdjustments)
        "DURAO" to RacialTraitEffect.AtributoStep("Vigor"),
        "EM_FORMA" to RacialTraitEffect.AtributoStep("Vigor"),
        "ESPIRITUAL" to RacialTraitEffect.AtributoStep("Espírito"),
        "ESPIRITUOSO" to RacialTraitEffect.AtributoStep("Espírito"),
        "FELIZES_POR_NATUREZA" to RacialTraitEffect.AtributoStep("Espírito"),
        "FORCA_SOBRENATURAL" to RacialTraitEffect.AtributoStep("Força"),
        "FORTE" to RacialTraitEffect.AtributoStep("Força"),
        "INTELIGENCIA" to RacialTraitEffect.AtributoStep("Astúcia"),
        "MUITO_AGIL" to RacialTraitEffect.AtributoStep("Agilidade", passos = 2), // sintético, usado pelo Template de Monstro Heroico Lobisomem (Horror)
        "MUITO_FORTE" to RacialTraitEffect.AtributoStep("Força", passos = 2),
        "MUITO_RESISTENTE" to RacialTraitEffect.AtributoStep("Vigor", passos = 2),
        "RESISTENTE" to RacialTraitEffect.AtributoStep("Vigor"),
        "ROBUSTO" to RacialTraitEffect.AtributoStep("Vigor"),
        "SOLIDO_COMO_ROCHA" to RacialTraitEffect.AtributoStep("Vigor"),
        "VIGOROSO" to RacialTraitEffect.AtributoStep("Vigor"),

        "CAES_DE_GUARDA" to RacialTraitEffect.PericiaStep("Perceber"),
        "FE" to RacialTraitEffect.PericiaStep("Fé"),
        "INTEGRADO_A_NATUREZA" to RacialTraitEffect.PericiaStep("Sobrevivência"),
        "PESFIRMES" to RacialTraitEffect.PericiaStep("Atletismo"),
        "SENTIDOS_AGUCADOS" to RacialTraitEffect.PericiaStep("Perceber"),
        "SENTIDOS_APRIMORADOS" to RacialTraitEffect.PericiaStep("Perceber"),
        "SENTIDOS_APURADOS" to RacialTraitEffect.PericiaStep("Perceber"),
        "SORRATEIRO" to RacialTraitEffect.PericiaStep("Furtividade"),
        "TRAPALHOES_TRAVESSOS" to RacialTraitEffect.PericiaStep("Furtividade"),

        // Resistência/Passo/Aparar de valor fixo. Cada id abaixo tem um valor
        // único e consistente conferido contra a própria descrição da
        // habilidade em ancestralidades.json (ex.: Terracota "recebem +3 em
        // Resistência", Meio-Orc "Recebem Resistência +1") — não são um
        // "chute" de código.
        "LENTO" to RacialTraitEffect.PassoBonus(-1),
        "MOVIMENTACAO_REDUZIDA" to RacialTraitEffect.PassoBonus(-1),
        "METADE_CONSTRUTO" to RacialTraitEffect.ResistenciaBonus(3),
        "MORTO_VIVO" to RacialTraitEffect.ResistenciaBonus(2),
        "VELOCIDADE_RACIAL" to RacialTraitEffect.PassoBonus(2), // sintético: Template de Monstro Heroico Lobisomem (Horror)

        // --- Traços EMPILHÁVEIS (ver VEZES_MAX abaixo) ---
        // Os 3 livros (Básico/Fantasia/Sci-Fi) marcam cada traço do catálogo
        // de criação de ancestralidade com "(N)" ou "(S)" — quantas vezes
        // pode ser comprado — e o efeito escala linearmente por compra (ex.:
        // "Armadura (3): ... Armadura +2 cada vez que é comprada", até +6).
        // O valor abaixo é sempre o de UMA compra — RacialAbility.vezes (ou
        // TraitAddition.vezes) multiplica na hora de aplicar o efeito (ver
        // ModifierEngine.aplicarEfeito) — nunca um id novo por total
        // (ex.: não existe mais "RESISTENCIA_2"; é RESISTENCIA com vezes=2).
        // Isso substitui os ids sintéticos por valor final que essa mesma
        // rodada de auditoria tinha criado (RESISTENCIA_1/_2, ARMADURA_2,
        // TAMANHO_MAIS_1/_2/TAMANHO_3, MOVIMENTACAO_2/_4, APARAR_1/
        // APARAR_MENOS_1, FRAGIL_MAIOR) — cada duplicata colapsa numa só
        // entrada aqui, com a raça/traço que tinha o valor maior passando a
        // carregar `vezes` > 1 em vez de um id próprio.
        "RESISTENCIA" to RacialTraitEffect.ResistenciaBonus(1), // livro: "Resistência (3)", +1/compra
        "APARAR" to RacialTraitEffect.ApararBonus(1), // livro: "Aparar (3)", +1/compra
        "APARAR_BAIXO" to RacialTraitEffect.ApararBonus(-1), // livro: "Aparar Baixo (3)", -1/compra
        "TAMANHO_MAIS_1" to RacialTraitEffect.TamanhoBonus(1), // livro: "Tamanho +1 (3)", +1/compra
        "FRAGIL" to RacialTraitEffect.ResistenciaBonus(-1), // livro: "Frágil (2)", -1/compra
        "MOVIMENTACAO" to RacialTraitEffect.PassoBonus(2), // livro: "Movimentação (2)", +2/compra
        "ARMADURA" to RacialTraitEffect.ArmaduraBonus(2), // livro: "Armadura (3)", +2/compra

        // Tamanho por id — substitui os regex `TAMANHO\s*([+-]\s*\d+)` sobre
        // nome/descrição do traço que existiam antes em ModifierEngine.
        // Valores conferidos contra a própria descrição de cada raça em
        // ancestralidades.json.
        "TAMANHO_MENOS_1" to RacialTraitEffect.TamanhoBonus(-1), // Pequeninos, Gnomos, Povo Ratazana, Gnomo/Halfling (Pathfinder) — livro: "Tamanho -1 (1)", não empilha
        "PEQUENOS" to RacialTraitEffect.TamanhoBonus(-1), // Goblins (mesmo efeito de Tamanho -1, id próprio)
        // Diminuto/Minúsculo: traço de TIER único (não empilhável — o livro
        // marca "(1)" mas com 3 custos internos conforme o tier escolhido:
        // Pequeno/Muito Pequeno/Minúsculo), diferente do empilhável acima.
        // Sempre Tamanho -4 nas raças oficiais que usam este id (Fadas, Povo
        // Rato). "DIMINUTO_TAMANHO_3"/"_4" são os ids escritos à mão em
        // AncestryVariantRegistry (ver TraitAddition) pros textos "DIMINUTO
        // (Tamanho -3)"/"DIMINUTO (Tamanho -4)" que a Variante de Ferais
        // (Padrão/Menor) injeta.
        "DIMINUTO" to RacialTraitEffect.TamanhoBonus(-4, minusculo = true),
        "DIMINUTO_TAMANHO_3" to RacialTraitEffect.TamanhoBonus(-3, minusculo = true),
        "DIMINUTO_TAMANHO_4" to RacialTraitEffect.TamanhoBonus(-4, minusculo = true),

        // Único traço do catálogo com dois efeitos numéricos ao mesmo tempo
        // (ver Composite acima) — Tanukimimi (Arte da Guerra): -1 Aparar e
        // -1 Movimentação juntos, conferido contra a própria descrição do
        // traço em ancestralidades.json.
        "DESPRETENSIOSOS_E_BARRIGUDOS" to RacialTraitEffect.Composite(
            listOf(RacialTraitEffect.ApararBonus(-1), RacialTraitEffect.PassoBonus(-1))
        )
    )

    fun efeitoDe(id: String?, targetRef: String? = null, value: Int = 1): RacialTraitEffect {
        if (id == null) return RacialTraitEffect.Nenhum
        val key = id.keyify()
        return when (key) {
            "ATTRIBUTE_BOOST" -> if (!targetRef.isNullOrBlank()) RacialTraitEffect.AtributoStep(targetRef, value) else RacialTraitEffect.Nenhum
            "SKILL_BOOST" -> if (!targetRef.isNullOrBlank()) RacialTraitEffect.PericiaStep(targetRef, value) else RacialTraitEffect.Nenhum
            "TOUGHNESS_FLAT" -> RacialTraitEffect.ResistenciaBonus(value)
            "PACE_CHANGE" -> RacialTraitEffect.PassoBonus(value)
            "PARRY_BOOST" -> RacialTraitEffect.ApararBonus(value)
            "SIZE_CHANGE" -> RacialTraitEffect.TamanhoBonus(value, minusculo = (value <= -3))
            "NATURAL_ARMOR" -> RacialTraitEffect.ArmaduraBonus(value)
            else -> EFEITOS[key] ?: RacialTraitEffect.Nenhum
        }
    }

    /**
     * Teto de vezes que um traço EMPILHÁVEL pode ser comprado, direto dos 3
     * livros (indicador "(N)"/"(S)" ao lado do nome no catálogo de criação de
     * ancestralidade — ver RacialAbility.vezes). Ausência aqui = 1 (padrão:
     * não empilha). "-1" representa "S" (sem limite no livro).
     */
    val VEZES_MAX: Map<String, Int> = mapOf(
        "ARMADURA" to 3, // livro: "Armadura (3)"
        "RESISTENCIA" to 3, // livro: "Resistência (3)"
        "APARAR" to 3, // livro: "Aparar (3)"
        "APARAR_BAIXO" to 3, // livro: "Aparar Baixo (3)"
        "TAMANHO_MAIS_1" to 3, // livro: "Tamanho +1 (3)"
        "FRAGIL" to 2, // livro: "Frágil (2)"
        "MOVIMENTACAO" to 2 // livro: "Movimentação (2)"
    )

    /** Teto de vezes que o id pode ser comprado (1 se não estiver listado). */
    fun vezesMaxDe(id: String?): Int = id?.let { VEZES_MAX[it.keyify()] } ?: 1

    /**
     * Rótulo de exibição por id de traço — a fonte única que tanto o
     * ModifierEngine (nome da fonte do Modifier) quanto a lista de
     * "Características" da aba Ancestralidades usam. Cobre ids sem efeito
     * mecânico numérico (puramente narrativos, ex.: VISAO_NO_ESCURO) além dos
     * que já estão em EFEITOS — um traço pode ter rótulo sem ter efeito, mas
     * todo traço com efeito devia ter rótulo aqui.
     *
     * Pros ids EMPILHÁVEIS (ver VEZES_MAX), este é só o rótulo de 1 compra —
     * use [labelComVezes] pra mostrar o valor final (ex.: "Resistência +2").
     */
    val LABEL: Map<String, String> = mapOf(
        "APARAR" to "Aparar +1",
        "APARAR_BAIXO" to "Aparar -1",
        "ARMADURA" to "Armadura +2",
        "TAMANHO_MAIS_1" to "Tamanho +1",
        "MOVIMENTACAO" to "Movimentação +2",
        "ESGUIOS" to "Esguios",
        "FEROCIDADE_ORC" to "Ferocidade Orc",
        "FRAGIL" to "Frágil",
        "LENTO" to "Lento",
        "METADE_CONSTRUTO" to "Metade Construto",
        "MOVIMENTACAO_REDUZIDA" to "Movimentação Reduzida",
        "MORTO_VIVO" to "Morto-Vivo",
        "RESISTENCIA" to "Resistência",
        "VELOCIDADE_RACIAL" to "Velocidade",
        "VISAO_NO_ESCURO" to "Visão no Escuro",

        // Voo tem 3 tiers oficiais (voo_6/voo_12/voo_24, ver
        // basico_habilidades_raciais.json) — o rótulo já carrega o valor de
        // Movimentação pra não precisar abrir a descrição pra saber qual tier
        // a raça tem (ex.: Fadas é Mov 6, Avianos/Celestiais são Mov 12).
        "VOO_MOV_6" to "Voo (Movimentação 6)",
        "VOO_MOV_24" to "Voo (Movimentação 24)",
        "ASAS_DE_ANJO" to "Voo (Movimentação 12)",
        "RECLUSO" to "Recluso (-2 Conhecimento Geral)",

        // Habilidades puramente narrativas dos 8 Templates de Monstro Heroico
        // (Horror) — sem efeito numérico modelado, então não aparecem em
        // EFEITOS, só aqui, pra "Características" não cair no fallback de
        // nome cru.
        "EMBELEZAR_ANJO" to "Embelezar",
        "IMUNE_DOENCAS_VENENOS" to "Imune a Doenças e Venenos",
        "NAO_ENVELHECE" to "Não Envelhece",
        "VOO_MOV_12" to "Voo (Movimentação 12)",
        "ARRUINAR_DEMONIO" to "Arruinar",
        "NEGOCIADOR_DEMONIO" to "Negociador",
        "NAO_RESPIRA" to "Não Respira",
        "RESISTENCIA_AMBIENTAL" to "Resistência Ambiental",
        "VISAO_TOTAL_ESCURO" to "Visão Total no Escuro",
        "ETEREO_FANTASMA" to "Etéreo",
        "INFRAVISAO" to "Infravisão",
        "MORDIDA_GARRAS_LOBISOMEM" to "Mordida/Garras",
        "TRANSFORMACAO_LOBISOMEM" to "Transformação",
        "CIENCIA_RETALHOS" to "Ciência!",
        "FURIA_RETALHOS" to "Fúria",
        "PARTES_RECOSTURAR" to "Partes (Recosturar)",
        "REGENERACAO_LENTA" to "Regeneração (Lenta)",
        "ROBUSTO_REVIVIDO" to "Robusto",
        "MORDIDA_VAMPIRO" to "Mordida"
    )

    /**
     * Rótulo de exibição já considerando quantas vezes o traço foi comprado
     * — pros 7 ids EMPILHÁVEIS (ver VEZES_MAX), mostra o valor final (ex.:
     * vezes=2 em "RESISTENCIA" vira "Resistência +2", igual ao livro), não
     * "Resistência (x2)". Pra qualquer outro id, ou vezes<=1, cai no rótulo
     * normal de [LABEL] (ou no próprio id, sem entrada).
     */
    fun labelComVezes(id: String?, vezes: Int): String {
        val key = id?.keyify()
        val base = key?.let { LABEL[it] } ?: id ?: ""
        if (vezes <= 1 || key == null) return base
        val efeito = EFEITOS[key] ?: return base
        fun sinal(v: Int) = if (v >= 0) "+$v" else "$v"
        return when (efeito) {
            is RacialTraitEffect.ResistenciaBonus -> "Resistência ${sinal(efeito.valor * vezes)}"
            is RacialTraitEffect.PassoBonus -> "Movimentação ${sinal(efeito.valor * vezes)}"
            is RacialTraitEffect.ApararBonus -> "Aparar ${sinal(efeito.valor * vezes)}"
            is RacialTraitEffect.TamanhoBonus -> "Tamanho ${sinal(efeito.valor * vezes)}"
            is RacialTraitEffect.ArmaduraBonus -> "Armadura ${sinal(efeito.valor * vezes)}"
            else -> base
        }
    }

    val CUSTOS: Map<String, Int> = mapOf(
        // "Ação Adicional" é um traço só, comprável uma vez, com 3 versões de
        // custo/efeito conforme o livro (Básico/Sci-Fi 5, Sci-Fi condicional 4,
        // Fantasia "Maior" 10 — ver docs/swade_basico|fantasia|scifi e
        // basico_habilidades_raciais.json "grupoEscolha": "acao_adicional").
        // Cada versão é um id próprio (não empilhável entre si).
        "ACAO_ADICIONAL" to 5, // oficial: acao_adicional
        "ACOES_ADICIONAIS" to 4, // oficial: acoes_adicionais (variante condicional, exige ação física/mental)
        "ACOES_ADICIONAIS_MAIOR" to 10, // oficial: acoes_adicionais_maior (Fantasia, reduz 4 pontos p/ qualquer ação)
        "ADAPTAVEL" to 2, // oficial: adaptavel
        "ADAPTAVEL_OU_SIGNO" to 2, // mesmo efeito de Adaptável
        "AGIL" to 2, // oficial: aumento_atributo
        "ALMOFADINHA" to -1, // oficial: complicacao_racial_menor
        "ALTA_TECNOLOGIA" to -2, // oficial: complicacao_racial_maior
        "ANALFABETO" to -1, // oficial: complicacao_racial_menor
        "ANCESTRALIDADE_INFAME" to -2, // sem equivalente oficial: -2 Persuadir + reações sempre Não Cooperativas
        "ANDAR_NAS_PAREDES" to 1, // oficial: andar_paredes
        "ANTECEDENTE_ARCANO_DEMONIO" to 4, // sem equivalente oficial (AA completo + poder inicial + 3 extra + 10 PP), acima de vantagem_racial
        "ANTECEDENTE_ARCANO_MILAGRES" to 3, // sem equivalente oficial (AA completo, menos detalhado que o de cima)
        "APARAR" to 1, // oficial: aparar_positivo (+1 = 1pt/compra — ver VEZES_MAX, até 3x)
        "APARAR_BAIXO" to -1, // oficial: aparar_baixo (-1 = -1pt/compra — ver VEZES_MAX, até 3x)
        "APTIDAO_COM_PEDRAS" to 1, // sem equivalente oficial, bônus situacional estreito
        "AQUATICO" to 2, // oficial: aquatico (não se afoga, Movimentação completa)
        "ARISCOS" to -3, // duas perícias a -2 cada (Provocar resistida, Intimidar) — oficial penalidade_pericia_2 é só uma perícia
        "ARMADURA" to 1, // oficial: armadura_racial (+2 = 1pt/compra — ver VEZES_MAX, até 3x)
        "ARMA_DE_SOPRO" to 2, // oficial: arma_de_sopro
        "ARROGANTE" to -2, // oficial: complicacao_racial_maior
        "ARTICULACOES_LIMITADAS" to -1, // oficial: movimentacao_reduzida_1
        "AR_INTERNO" to 2, // oficial: nao_respira (+ imune toxina inalada, não afoga/sufoca)
        "ASAS_DE_ANJO" to 4, // oficial: voo_12 (Movimentação 12) — mesma base de VOO_MOV_12, com regras extras (recolher/recriar asas, tetos de altitude no Limbo, -1 Movimentação por peso) descritas no próprio traço
        "ASTUCIA" to 2, // oficial: aumento_atributo
        "ASTUTO" to 2, // oficial: aumento_atributo
        "ATRAENTE" to 2, // oficial: vantagem_racial
        "AVERSAO_ANIMAL" to -1, // sem equivalente oficial, penalidade situacional
        "AZARADO" to -2, // Complicação Maior no catálogo real (complicacoes.json) — a severidade "Menor" anotada em ancestralidades.json pra Nekomimi não existe pra essa Complicação, corrigido pra bater com o catálogo
        "BAIXA_GRAVIDADE_AGIL" to 2, // oficial: aumento_atributo — sintético (Humanos Sci-Fi "Baixa Gravidade"), Agilidade d4->d6
        "BAIXA_TECNOLOGIA" to -2, // oficial: complicacao_racial_maior
        "BEBEDOR_DE_SANGUE" to 1, // sem equivalente oficial, utilidade condicional 1x/sessão
        "BOCA_GRANDE" to -1, // oficial: complicacao_racial_menor
        "BOM_CONSELHEIRO" to -1, // oficial: complicacao_racial_menor (Peculiaridade)
        "BRINCALHAO" to 1, // oficial: pericia_racial_d4 (Provocar d4)
        "BRINCANDO_COM_O_DESTINO" to 2, // oficial: pericia_racial_d6 (Jogar d6, não d4)
        "BRUTAL" to -1, // oficial: penalidade_pericia_1 (-1 Persuadir, perícia)
        "CABECADA" to 1, // oficial: chifres (For+d4) — o próprio traço diz "conta como Chifres"
        "CABECAS_DURAS" to -2, // oficial: penalidade_atributo_1 (-1 num ATRIBUTO, Astúcia, vale -2 — não -1 de perícia)
        "CABECA_DURA" to -2, // idem (Elementais, -1 Astúcia)
        "CAES_DE_GUARDA" to 2, // oficial: pericia_racial_d6 (Perceber d6)
        "CALCULISTA" to 2, // oficial: vantagem_racial
        "CAMPEAO" to 2, // oficial: vantagem_racial
        "CANINOS" to 2, // oficial mordida=1 + utilidade extra de Prender/Enredar
        "CARISMATICO" to 2, // oficial: vantagem_racial
        "CASCA" to 2, // oficial: casca (id igual, mesmo conceito)
        "CASCOS" to 1, // oficial: chifres/mordida (arma natural básica For+d4)
        "CHIFRES" to 1, // oficial: chifres (For+d4; o bônus de corrida é situacional, não muda o tier)
        "CHI_REDUZIDO" to -1, // sem equivalente oficial, -1 na Reserva de Chi inicial
        "CIBER_RESISTENCIA" to -1, // oficial: complicacao_racial_menor
        "CIRCUITOS_DE_ASIMOV" to -2, // oficial: complicacao_racial_maior (Pacifista)
        "CODIGO_DE_HONRA" to -2, // oficial: complicacao_racial_maior
        "COMUNITARIO" to 1, // oficial: comunitario
        "CONHECIMENTO_GERAL" to 2, // oficial: pericia_racial_d6 (Conhecimento Geral d6, não d4)
        "CONSTITUICAO_DE_FERRO" to 1, // sem equivalente oficial, bônus estreito
        "CONSTRUTO" to 8, // oficial: construto
        "COVARDE" to -2, // oficial: complicacao_racial_maior
        "CURIOSO" to -2, // oficial: complicacao_racial_maior
        "DEFINIDO_PELO_OFICIO" to 2, // oficial: pericia_racial_d6 (1 perícia à escolha d6)
        "DEPENDENCIA" to -2, // oficial: dependencia
        "DEPENDENCIA_ATMOSFERICA" to -1, // oficial: dependencia_atmosferica_1 (tier "Horas", sem qualificador explícito no JSON)
        "DESAGRADAVEL" to -1, // oficial: complicacao_racial_menor
        "DESAJEITADO" to -3, // duas perícias a -2 cada (Atletismo, Furtividade) — oficial penalidade_pericia_2 é só uma perícia
        "DESASTRADO" to -1, // oficial: complicacao_racial_menor
        "DESPRETENSIOSOS_E_BARRIGUDOS" to -2, // -1 Aparar + -1 Movimentação + corrida d4, pacote de três penalidades leves
        "DICAS_CULTURAIS" to 2, // oficial: pericia_racial_d6 (Convenção d6)
        "DIGESTAO_GLORIOSA" to 1, // sem equivalente oficial exato, imunidade estreita (só ingestão)
        "DIMINUTO" to 6, // oficial: diminuto_minusculo (Tamanho -4)
        "DIMINUTO_TAMANHO_3" to 5, // mesmo pacote de DIMINUTO, só que Tamanho -3 em vez de -4 (Ferais Padrão)
        "DIMINUTO_TAMANHO_4" to 6, // mesmo valor de DIMINUTO — Tamanho -4 (Ferais Menor)
        "DONS_DA_NATUREZA" to 0, // placeholder de Seleção (Umvee/Feral escolhem 1 de 6 dons; o dom resolvido é que pontua)
        "DURAO" to 2, // oficial: aumento_atributo
        "EM_FORMA" to 2, // oficial: aumento_atributo
        "ENDURECIDO" to 2, // oficial: aumento_atributo (escolha entre Força/Vigor)
        "ESGUIOS" to -3, // oficial penalidade_atributo_1 (-1 Vigor = -2) + -1 Resistência (~-1) combinados
        "ESPACIAL" to 3, // oficial: espacial
        "ESPIRITUAL" to 2, // oficial: aumento_atributo
        "ESPIRITUOSO" to 2, // oficial: aumento_atributo
        "ESQUISITICES" to -2, // duas Complicações Menores (Hábito + Peculiaridade), oficial -1 cada
        "ESTAVEL" to 1, // oficial: estavel
        "EXCESSIVAMENTE_DETALHISTAS" to -1, // oficial: complicacao_racial_menor (Cauteloso)
        "FE" to 2, // oficial: pericia_racial_d6 (Fé d6)
        "FELIZES_POR_NATUREZA" to 2, // oficial: aumento_atributo
        "FEROCIDADE_ORC" to 1, // oficial: resistencia_racial (+1)
        "FLEXIBILIDADE" to 2, // oficial: aumento_atributo (escolha)
        "FOBIA" to -1, // oficial: complicacao_racial_menor
        "FORASTEIRO" to -2, // Complicação real (complicacoes.json), severidade Maior escolhida pela raça
        "FORCA_SOBRENATURAL" to 2, // oficial: aumento_atributo
        "FORMA_ALIENIGENA" to -1, // oficial: forma_alienigena — mesmo traço reaproveitado por
        // Centauros ("FORMA INCOMUM") e Insetoides Fantasia ("FORMATO CORPORAL INCOMUM"),
        // cada um com seu próprio nome de exibição em vez de "Forma Alienígena" (nome do
        // livro Sci-Fi, onde o traço é catalogado com custo -1). Ids antigos removidos
        // (FORMATO_CORPORAL_INCOMUM/FORMA_INCOMUM) — Avianos "Ave de Rapina" já usava este id
        // via AncestryVariantRegistry mas não tinha entrada aqui (custava 0 por engano).
        "FORMA_DE_ENERGIA" to 4, // oficial: forma_energia
        "FORTE" to 2, // oficial: aumento_atributo
        "FORTUNA_DA" to 2, // sem equivalente oficial exato, Bene extra por sessão (tier de vantagem_racial)
        "FRACO" to -2, // oficial penalidade_atributo_1 (-1 num ATRIBUTO, Força)
        "FRAGIL" to -1, // oficial: fragil (âncora: AnaoCiberNegativeTrait.fragil)
        "FRAQUEZA_AMBIENTAL" to -1, // oficial: fraqueza_ambiental
        "GANANCIOSO" to -1, // oficial: complicacao_racial_menor
        "GARRAS" to 2, // oficial garras_d4=2 (For+d4, PA 2 e bônus de Atletismo são o extra que já cabe nesse tier)
        "GELATINOSO" to 2, // oficial: gelatinoso_2
        "GUIADO" to -2, // oficial: complicacao_racial_maior
        "HERANCA" to 2, // oficial: vantagem_racial OU aumento_atributo (escolha, mesmo tier)
        "HERANCA_MISTA" to 2, // oficial: vantagem_racial
        "IMPULSIVO" to -1, // oficial: complicacao_racial_menor
        "IMUNE_A_DOENCAS_E_VENENOS" to 1, // oficial: imune_doencas_venenos
        "INCAPAZ_DE_FALAR" to -2, // oficial: complicacao_racial_maior
        "INFRAVISAO" to 1, // oficial: infravisao
        "INIMIGO_ANCESTRAL" to -1, // oficial: inimigo_racial
        "INIMIGO_RACIAL" to -1, // oficial: inimigo_racial
        "INIMIGO_RACIAL_DEMONIOS_E_DIABOS" to -1, // oficial: inimigo_racial (escopo mais estreito)
        "INSANIDADE" to 0, // concede Furioso (Vantagem) e Sanguinário (Complicação Maior) juntos - se cancelam no saldo
        "INTEGRADO_A_NATUREZA" to 2, // oficial: pericia_racial_d6 (Sobrevivência d6)
        "INTELIGENCIA" to 2, // oficial: aumento_atributo
        "INTIMIDANTE" to 1, // oficial: pericia_racial_d4 (Intimidar d4, teto ampliado)
        "LEAL" to -1, // oficial: complicacao_racial_menor
        "LENTO" to -1, // oficial: movimentacao_reduzida_1
        "LIMITACOES_TECNICAS" to -1, // sem equivalente oficial, restrição narrativa (Técnicas de Chi)
        "MAGIA_ELFICA" to 1, // sem equivalente oficial, utilidade defensiva estreita
        "MAGIA_GNOMICA" to 2, // oficial: poder_racial
        "MAL_HUMORADO" to -2, // oficial: complicacao_racial_maior (Arrogante)
        "MATILHA" to -1, // oficial: complicacao_racial_menor (Leal)
        "MEMBROS_EXTRAS" to 2, // oficial: membros_extras
        "MENTE_DE_COLMEIA" to -3, // duas Complicações juntas: Guiado (Maior, -2) + Leal (-1)
        "MENTE_PRIMITIVA" to -1, // sem equivalente oficial, teto de Astúcia travado em d6 na criação
        "METADE_CARNE" to 0, // só esclarece que ainda precisa comer/descansar (contraponto narrativo de Metade Construto, sem efeito mecânico próprio)
        "METADE_CONSTRUTO" to 6, // entre robo (6) e construto (8): pacote quase completo (+3 Resistência, não respira, imune veneno/doença, sem dano extra de Ataque Localizado) mas não cura naturalmente
        "MORDIDA" to 1, // oficial: mordida (For+d4)
        "MORDIDAGARRAS" to 1, // oficial: mordida (mesmo texto/efeito, id duplicado)
        "MORDIDA_GARRAS" to 1, // oficial: mordida
        "MORDIDA_OU_GARRA" to 1, // oficial: mordida
        "MORDIDA_VENENOSA" to 2, // oficial mordida (1) + toque_venenoso moderado (1)
        "MORTO_VIVO" to 8, // oficial: morto_vivo
        "MOVIMENTACAO" to 2, // oficial: movimentacao_bonus (+2 = 2pts/compra — ver VEZES_MAX, até 2x)
        "MOVIMENTACAO_REDUZIDA" to -1, // oficial: movimentacao_reduzida_1
        "MUDAR_DE_FORMA" to 4, // oficial: mudanca_forma
        "MUITO_FORTE" to 4, // 2x oficial aumento_atributo (dois passos de Força)
        "MUITO_RESISTENTE" to 4, // 2x oficial aumento_atributo (dois passos de Vigor)
        "NAO_PODE_CURAR" to -1, // oficial: nao_pode_curar
        "NAO_SABE_NADAR" to -1, // oficial: complicacao_racial_menor
        "NATURALMENTE_SOBRENATURAL" to 1, // oficial: pericia_racial_d4 (Ocultismo d4)
        "NATUREZA_DIABOLICA" to 1, // oficial: bonus_pericia_1 (+1 Intimidar)
        "NERVOS_DE_ACO" to 2, // oficial: vantagem_racial
        "NOCAO_DO_PERIGO" to 2, // oficial: vantagem_racial
        "OBSESSIVOS" to 1, // oficial: pericia_racial_d4
        "OBVIO" to -1, // oficial: penalidade_pericia_1 (-1 Furtividade, perícia)
        "OPCAO_MAGICA" to 2, // sem equivalente oficial exato, acesso a escolher AA (Demônio) como Vantagem Novato
        "PACIFISTA" to -2, // Complicação real (complicacoes.json)/oficial complicacao_racial_maior
        "PENSAMENTOS_POSITIVOS" to 2, // oficial: vantagem_racial
        "PEQUENOS" to -1, // oficial: tamanho_menos_1
        "PERICIAS_BASICAS_REDUZIDAS" to -1, // oficial: pericias_basicas_reduzidas
        "PESFIRMES" to 2, // oficial: pericia_racial_d6 (Atletismo d6)
        "PONTOS_DE_PERICIA" to 2, // sem equivalente oficial exato, +3 pontos de perícia iniciais
        "POUCO_IMPONENTE" to -1, // oficial: complicacao_racial_menor (Almofadinha)
        "PREPARADO" to 1, // oficial: pericia_racial_d4
        "PRIMITIVO" to 2, // oficial: aumento_atributo (escolha)
        "PROGRAMADO" to -2, // oficial: complicacao_racial_maior
        "PRONTIDAO" to 2, // oficial: vantagem_racial
        "RAPIDO" to 2, // oficial: vantagem_racial
        "RECLUSO" to -2, // oficial: penalidade_pericia_2 (-2 numa perícia comum, Conhecimento Geral)
        "REDUCAO_DE_SONO" to 1, // oficial: reducao_sono
        "REFLEXOS_DE_COMBATE" to 3, // oficial vantagem_racial (2) + bônus extra de +2 Espírito recuperar Abalado
        "RESISTENCIA" to 1, // oficial: resistencia_racial (+1 = 1pt/compra — ver VEZES_MAX, até 3x)
        "RESISTENCIA_AMBIENTAL" to 1, // oficial: resistencia_ambiental
        "RESISTENCIA_AO_FRIO" to 1, // oficial: resistencia_ambiental (mesmo conceito, frio)
        "RESISTENCIA_NATURAL" to 1, // oficial: imune_doencas_venenos
        "RESISTENTE" to 2, // oficial: aumento_atributo
        "ROBO" to 6, // oficial: robo
        "ROBUSTO" to 2, // oficial: robusto (id igual, mesmo conceito)
        "RUDE" to -2, // oficial: penalidade_pericia_2 (-2 Persuadir, perícia)
        "SANGUE_FRIO" to -3, // oficial: sangue_frio (id igual, mesmo conceito)
        "SANGUINARIO" to -2, // oficial: complicacao_racial_maior
        "SEGREDO" to -2, // oficial: complicacao_racial_maior
        "SEM_ESCRUPULOS" to -2, // oficial: complicacao_racial_maior
        "SEM_INSTRUCAO" to -2, // oficial penalidade_atributo_1 (-1 num ATRIBUTO, Astúcia)
        "SEM_NOCAO" to -2, // duas perícias a -1 cada (Conhecimento Geral, Perceber)
        "SEM_ORGAOS_VITAIS" to 1, // oficial: sem_orgaos_vitais
        "SEM_SANGUE" to 1, // oficial: sem_sangue
        "SENSIBILIDADE_A_LUZ_SOLAR" to -2, // sem equivalente oficial exato, Estado Distraído sob luz solar sem proteção
        "SENSIVEL" to -2, // oficial: complicacao_racial_maior
        "SENTIDOS_AGUCADOS" to 2, // oficial: pericia_racial_d6 (Perceber d6, não é o bônus estreito de "sentidos aguçados")
        "SENTIDOS_APRIMORADOS" to 2, // oficial: pericia_racial_d6 (Perceber d6)
        "SENTIDOS_APURADOS" to 2, // oficial: pericia_racial_d6 (Perceber d6)
        "SIGNOS_DE_NASCENCA" to 0, // placeholder de Seleção (Humano Império San escolhe 1 de 13 signos; o signo resolvido é que pontua)
        "SOCIALMENTE_SOFISTICADOS" to 2, // oficial: vantagem_racial
        "SOLIDO_COMO_ROCHA" to 2, // oficial: aumento_atributo
        "SORRATEIRO" to 2, // oficial: pericia_racial_d6 (Furtividade d6)
        "SORTE" to 2, // sem equivalente oficial exato, Bene extra por sessão (tier de vantagem_racial)
        "SUCATEIRO" to 2, // oficial: vantagem_racial
        "SUSPEITOSO" to -1, // oficial: complicacao_racial_menor
        "TAMANHO_MAIS_1" to 1, // oficial: tamanho_mais_1 (+1pt/compra — ver VEZES_MAX, até 3x)
        "TAMANHO_MENOS_1" to -1, // oficial: tamanho_menos_1 (âncora: AnaoCiberNegativeTrait.tamanho_menos_1)
        "TELEPATIA" to 1, // oficial: telepatia
        "TRANSTORNO_DE_SEPARACAO" to -2, // oficial: transtorno_separacao (âncora: AnaoCiberNegativeTrait.transtorno_separacao)
        "TRAPALHOES_TRAVESSOS" to 2, // oficial: pericia_racial_d6 (Furtividade d6)
        "TREINADOS_PARA_A_GUERRA" to -2, // sem equivalente oficial exato, -4 numa perícia só (Conhecimento Geral), tier de penalidade_pericia_2
        "TRIPAS_RESISTENTE" to 2, // sem equivalente oficial exato, +1 em várias rolagens (Medo, Absorção, resistir veneno/náusea)
        "VELOCIDADE_DA_LEBRE" to 2, // oficial: movimentacao_bonus
        "VIGOROSO" to 2, // oficial: aumento_atributo
        "VIGOROSOS" to 1, // oficial: resistencia_racial (+1)
        "VISAO_DE_360" to 1, // oficial: visao_360
        "VISAO_NA_PENUMBRA" to 1, // oficial: visao_escuro (mesmo tier)
        "VISAO_NO_ESCURO" to 1, // oficial: visao_escuro
        "VISAO_TOTAL_NO_ESCURO" to 1, // oficial: visao_total_escuro
        "VOLUMOSO" to -2, // oficial: volumoso (Básico) — mesmo traço reaproveitado por Golens,
        // Meio-Gigantes, Minotauros, Ogros, Centaux, Elementais, Yetis e Aurax, cada um com
        // "GRANDE" como nome de exibição (confirmado contra os 3 livros: descrição idêntica —
        // -2 em Característica com equipamento não personalizado, sem armadura/roupa, custo em
        // dobro). Id antigo "GRANDE" tinha -1 por engano (comentário achava que não existia
        // equivalente oficial); o valor certo é -2, igual ao catálogo de criação de raça.
        "VOO_MOV_6" to 2, // oficial: voo_6 (Movimentação 6) — Fadas (Fantasia)
        "VOO_MOV_12" to 4, // oficial: voo_12 (Movimentação 12) — Avianos, Celestiais
        "VOO_MOV_24" to 6, // oficial: voo_24 (Movimentação 24, corrida +2d6) — sem raça oficial usando este tier ainda, id reservado pra manter os 3 tiers do catálogo oficial
        "VOTO" to -2 // Complicação real (complicacoes.json)/oficial complicacao_racial_maior
    )

    /** Custo em pontos do traço, pelo id ou parâmetro direto `pontos` (0 se não estiver no catálogo). */
    fun custoDe(id: String?, pontos: Int = 0): Int {
        if (pontos != 0) return pontos
        return id?.let { CUSTOS[it.keyify()] } ?: 0
    }
}
