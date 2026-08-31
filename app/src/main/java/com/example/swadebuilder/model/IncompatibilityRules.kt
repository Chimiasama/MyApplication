package com.example.swadebuilder.model

/**
 * Única fonte de verdade para conflitos Vantagem x Complicação (não pode ter as duas ao mesmo
 * tempo). Antes desta classe existiam 3 cópias manuais do mesmo mapa (ValidateConflictsUseCase,
 * RequirementValidator, CriadorState.mensagemConflitoPara*), chaveadas por nome/texto em vez de
 * id — o que já tinha causado divergência real entre o validador de criação e o de progressão.
 *
 * Todas as entradas abaixo foram conferidas contra vantagens.json/complicacoes.json (ids reais)
 * e, para os casos de Antecedente Arcano, contra o texto de Cidade do Sol a Vapor (Livro dos
 * Mortais) que descreve a regra ("Esta Complicação não pode ser escolhida se você possuir o
 * Antecedente Arcano (Milagres/Tecnomagia)", "Tecnofobia não pode ser combinada com as Vantagens
 * Tarô da Nova Era (V. Engenheiro), Mestre das Caldeiras ou Mecânico Cego").
 */
object IncompatibilityRules {

    /** Vantagem.id -> conjunto de Complicacao.id incompatíveis com ela. */
    private val vantagemConflitaComComplicacao: Map<String, Set<String>> = mapOf(
        "ligeiro" to setOf("lento", "lento_ch"),
        "musculoso" to setOf("obeso"),
        "rico" to setOf("pobreza"),
        "podre_de_rico" to setOf("pobreza"),
        "escolhido" to setOf("inimigo", "inimigo_ch"),

        // "Antecedente Arcano (Milagres)" tem 3 ids no catálogo (base, Deadlands, Pathfinder) —
        // Alma Penhorada/Alma Vendida (Cidade do Sol a Vapor) conflitam com qualquer um deles.
        "antecedente_arcano_milagres" to setOf("comp_alma_penhorada", "comp_alma_vendida"),
        "aa_milagres" to setOf("comp_alma_penhorada", "comp_alma_vendida"),
        "antecedente_arcano_milagres_pf" to setOf("comp_alma_penhorada", "comp_alma_vendida"),

        "aa_tecnomagia" to setOf("comp_maldicao_gremlin"),

        "taro_engenheiro" to setOf("comp_tecnofobia"),
        "mestre_das_caldeiras" to setOf("comp_tecnofobia"),
        "mecanico_cego" to setOf("comp_tecnofobia")
    )

    /** Índice reverso derivado do mapa acima — nunca editado à mão, não pode divergir. */
    private val complicacaoConflitaComVantagem: Map<String, Set<String>> =
        buildMap<String, MutableSet<String>> {
            vantagemConflitaComComplicacao.forEach { (vantagemId, complicacaoIds) ->
                complicacaoIds.forEach { complicacaoId ->
                    getOrPut(complicacaoId) { mutableSetOf() }.add(vantagemId)
                }
            }
        }

    fun complicacoesIncompativeisCom(vantagemId: String): Set<String> =
        vantagemConflitaComComplicacao[vantagemId].orEmpty()

    fun vantagensIncompativeisCom(complicacaoId: String): Set<String> =
        complicacaoConflitaComVantagem[complicacaoId].orEmpty()
}
