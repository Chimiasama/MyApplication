package com.example.swadebuilder.model

import com.example.swadebuilder.util.keyify
import com.example.swadebuilder.util.semAcentos
import kotlinx.serialization.Serializable

@Serializable
data class RacialAbility(
    val nome: String,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null,
    val id: String? = null,
    val category: String? = null,
    val severity: String? = null
) {
    /**
     * Retorna o ID canônico equivalente para compatibilidade retroativa com IDs legados
     * salvos em edições anteriores ou caixas altas.
     */
    val canonicalId: String?
        get() {
            val raw = id?.lowercase()?.trim() ?: return null
            return when (raw) {
                "adaptavel", "adaptavel_ou_signo", "heranca", "heranca_mista", "signos_de_nascenca", "pontos_de_pericia" -> "adaptavel"
                "agil", "astucia", "astuto", "espiritual", "espirituoso", "em_forma", "forte", "inteligencia",
                "muito_forte", "muito_resistente", "primitivo", "resistente", "solido_como_rocha", "vigoroso",
                "durao", "endurecido", "flexibilidade", "forca_sobrenatural" -> "aumento_atributo"
                "armadura_2", "armadura_racial", "resistencia_2" -> "armadura_racial"
                "aparar" -> "aparar_positivo"
                "aparar_baixo" -> "aparar_baixo"
                "movimentacao_reduzida", "movimentacao_reduzida_1", "lento", "articulacoes_limitadas", "despretensiosos_e_barrigudos" -> "movimentacao_reduzida_1"
                "movimentacao_reduzida_2" -> "movimentacao_reduzida_2"
                "movimentacao_2", "movimentacao_4", "movimentacao", "velocidade_da_lebre" -> "movimentacao_bonus"
                "tamanho_1", "tamanho_menos_1", "pequenos" -> "tamanho_menos_1"
                "tamanho_mais_1", "tamanho_3", "tamanho_mais_2" -> "tamanho_mais_1"
                "diminuto" -> "diminuto_minusculo"
                "visao_no_escuro", "visao_escuro", "visao_na_penumbra" -> "visao_escuro"
                "visao_total_no_escuro", "visao_total_escuro" -> "visao_total_escuro"
                "infravisao" -> "infravisao"
                "visao_de_360", "visao_360" -> "visao_360"
                "sentidos_agucados", "sentidos_aprimorados", "sentidos_apurados" -> "sentidos_agucados_visao"
                "garras", "mordidagarras", "mordida_garras", "mordida_ou_garra" -> "garras_d4"
                "mordida", "caninos" -> "mordida"
                "chifres", "cascos" -> "chifres"
                "aquatico" -> "aquatico"
                "fragil", "esguios" -> "fragil"
                "fraco", "desajeitado", "cabeca_dura", "cabecas_duras", "sem_instrucao", "mente_primitiva" -> "penalidade_atributo_1"
                "brutal", "desagradavel", "rude", "sem_nocao", "obvio" -> "penalidade_pericia_1"
                "ariscos", "treinados_para_a_guerra", "aversao_animal" -> "penalidade_pericia_2"
                "caes_de_guarda", "brincando_com_o_destino", "conhecimento_geral", "definido_pelo_oficio",
                "dicas_culturais", "fe", "integrado_a_natureza", "pesfirmes", "sorrateiro", "trapalhoes_travessos" -> "pericia_racial_d6"
                "brincalhao", "intimidante", "naturalmente_sobrenatural", "obsessivos", "preparado" -> "pericia_racial_d4"
                "construto", "metade_construto" -> "construto"
                "robo" -> "robo"
                "morto_vivo" -> "morto_vivo"
                "espacial" -> "espacial"
                "nao_respira", "ar_interno" -> "nao_respira"
                "forma_de_energia" -> "forma_energia"
                "andar_nas_paredes" -> "andar_paredes"
                "estavel" -> "estavel"
                "resistencia_ambiental", "resistencia_ao_frio", "tripas_resistente", "magia_elfica" -> "resistencia_ambiental"
                "fraqueza_ambiental", "sensibilidade_a_luz_solar" -> "fraqueza_ambiental"
                "resistencia_natural", "imune_a_doencas_e_venenos", "digestao_gloriosa" -> "imune_doencas_venenos"
                "forasteiro", "almofadinha", "ancestralidade_infame", "azarado", "boca_grande", "bom_conselheiro",
                "cauteloso", "chi_reduzido", "ciber_resistencia", "desastrado", "esquisitices", "excessivamente_detalhistas",
                "fobia", "ganancioso", "habito", "impulsivo", "leal", "limitacoes_tecnicas", "matilha", "metade_carne",
                "peculiaridade", "pouco_imponente", "segredo", "suspeitoso" -> "complicacao_racial_menor"
                "sanguinario", "pacifista", "voto", "codigo_de_honra", "programado", "circuitos_de_asimov",
                "alta_tecnologia", "baixa_tecnologia", "covarde", "curioso", "arrogante", "sensivel", "guiado",
                "insanidade", "mal_humorado", "mente_de_colmeia", "sem_escrupulos" -> "complicacao_racial_maior"
                "inimigo_racial", "inimigo_ancestral", "inimigo_racial_demonios_e_diabos" -> "inimigo_racial"
                "mudar_de_forma" -> "mudanca_forma"
                "telepatia" -> "telepatia"
                "membros_extras" -> "membros_extras"
                "acoes_adicionais", "acao_adicional" -> "acao_adicional"
                "sorte", "sucateiro", "rapido", "campeao", "nocao_do_perigo", "atraente", "calculista",
                "reflexos_de_combate", "nervos_de_aco", "carismatico", "dons_da_natureza", "fortuna_da",
                "pensamentos_positivos", "prontidao", "socialmente_sofisticados" -> "vantagem_racial"
                "antecedente_arcano_milagres", "antecedente_arcano_demonio", "magia_gnomica", "opcao_magica" -> "poder_racial"
                "resistencia", "resistencia_1", "resistencia_racial", "ferocidade_orc", "vigorosos" -> "resistencia_racial"
                "forma_incomum", "formato_corporal_incomum" -> "forma_alienigena"
                "grande" -> "volumoso"
                else -> raw
            }
        }
}

@Serializable
data class RacialModifier(
    val id: String? = null,
    val nome: String,
    val originalName: String? = null,
    val originalDescription: String? = null,
    val descricao: String? = null,
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String> = emptyList(),
    val desvantagens: List<String> = emptyList(),
    val habilidades: List<RacialAbility> = emptyList(),
    val origem: String = "BASICO",
    val movimentacao: Int = 0,
    val tags: List<String> = emptyList(),
    val opcoes: List<String> = emptyList()
)

@Serializable
data class HabilidadeCriacao(
    val nome: String,
    val custo: Int,
    val descricao: String,
    // Resumo genérico para a edição Lite (não reproduz o texto do livro original).
    val descricaoLite: String? = null
) {
    fun exibida(): HabilidadeCriacao =
        if (!com.example.swadebuilder.EditionConfig.isFullEdition && !descricaoLite.isNullOrBlank()) copy(descricao = descricaoLite) else this
}

data class RacialAbilitySignature(val nome: String, val descricao: String)

data class RacialSignature(
    val atributos: Map<String, Int>,
    val pericias: Map<String, Int>,
    val vantagensGratis: List<String>,
    val desvantagens: List<String>,
    val habilidades: List<RacialAbilitySignature>
)

fun RacialModifier.signature(): RacialSignature {
    fun normalizeList(values: List<String>): List<String> = values.sortedBy { it.uppercase().semAcentos() }

    return RacialSignature(
        atributos = atributos,
        pericias = pericias,
        vantagensGratis = normalizeList(vantagensGratis),
        desvantagens = normalizeList(desvantagens),
        habilidades = habilidades
            .map { RacialAbilitySignature(it.nome, it.descricao) }
            .sortedWith(compareBy({ it.nome.uppercase().semAcentos() }, { it.descricao.uppercase().semAcentos() }))
    )
}

fun stripAncestralidadeScenarioSuffix(nome: String): String =
    nome.replace(Regex("\\s*\\([^)]*\\)\\s*$"), "").trim()

/**
 * Colapsa candidatos de ancestralidade — possivelmente vindos de múltiplos livros ativos ao
 * mesmo tempo (Modo Livre, ou um livro companheiro somado ao Básico) — em grupos prontos para
 * exibição em uma lista de seleção de raça.
 *
 * Duas etapas:
 * 1. Por nome exato: quando o mesmo nome existe em mais de um livro ativo, mantém a versão do
 *    livro de maior [originPriority] (em vez da primeira do arquivo), já que livros de
 *    cenário/companheiros costumam trazer uma versão mais específica que o Básico.
 * 2. Por (nome-base sem sufixo de cenário, assinatura mecânica): funde apenas quando AMBOS
 *    coincidem, nunca só a assinatura — duas raças diferentes podem ter atributos/perícias/
 *    habilidades idênticos por coincidência (ex.: Kalianos reaproveita o mesmo bloco de
 *    habilidades de Quadroides no Sci-Fi) sem serem a mesma raça. Exigir o nome-base também
 *    preserva a fusão legítima de variantes de nome da mesma raça entre livros (ex.: "Humano"
 *    e "Humano (Buscatrilha)").
 */
fun groupAncestralidadesForDisplay(items: List<RacialModifier>): List<List<RacialModifier>> {
    val prioritized = items.groupBy { it.nome.keyify() }
        .map { (_, duplicates) ->
            if (duplicates.size == 1) duplicates.first() else duplicates.maxBy { originPriority(it.origem) }
        }

    return prioritized
        .groupBy { stripAncestralidadeScenarioSuffix(it.nome).keyify() to it.signature() }
        .values
        .toList()
}
