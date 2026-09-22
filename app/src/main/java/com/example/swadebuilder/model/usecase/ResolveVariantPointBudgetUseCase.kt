package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitEffect
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.toDiceString
import com.example.swadebuilder.util.keyify

/**
 * Item já resolvido (traço racial, Vantagem ou Complicação, oficial ou
 * removido da raça base) com seu custo em pontos, pronto pra entrar no
 * cálculo de orçamento de uma Variante custom. Ver `RacialTraitPointCatalog`
 * pra como cada custo é decidido.
 */
data class VariantBudgetItem(
    val label: String,
    val custo: Int,
    val habilidadeId: String? = null,
    val vantagemId: String? = null,
    val complicacaoId: String? = null
)

/**
 * Calcula o saldo TOTAL em pontos de uma Variante custom de raça: começa do
 * valor de livro da própria raça base (soma do custo de TODOS os seus
 * traços, tocados ou não — ver [valorTotalDe]), subtrai o custo de cada
 * item removido e soma o custo de cada item
 * adicionado. Não é o delta só das mudanças: é quanto a raça resultante
 * vale no total, do mesmo jeito que se estivesse sendo construída do zero.
 *
 * Toda raça oficial do livro já fecha exatamente em [DEFAULT_ORCAMENTO] (2)
 * pontos — é assim que o próprio livro básico calibra as raças (Humanos:
 * só Adaptável, 2; Anões: -1+2+1, também 2). Por isso, sem mexer em nada, a
 * Variante de uma raça-livro já nasce fechada em 2 e pode ser salva
 * imediatamente; ela só sai do orçamento quando o jogador começa a
 * remover/adicionar coisas sem compensar. Passando `semLimite = true` (a
 * opção de "sem limite" pra raças mais fortes ou mais fracas) a validação
 * sempre passa, qualquer saldo.
 *
 * Não decide SE algo pode ser removido/adicionado (isso é responsabilidade
 * da UI/fluxo de criação) — só soma e valida o saldo dos itens que já
 * chegaram resolvidos.
 */
class ResolveVariantPointBudgetUseCase {

    data class Result(
        val saldo: Int,
        val orcamento: Int,
        val semLimite: Boolean,
        val dentroDoOrcamento: Boolean
    )

    fun resolve(
        valorBaseRaca: Int,
        itensRemovidos: List<VariantBudgetItem>,
        itensAdicionados: List<VariantBudgetItem>,
        orcamento: Int = DEFAULT_ORCAMENTO,
        semLimite: Boolean = false
    ): Result {
        val saldo = valorBaseRaca - itensRemovidos.sumOf { it.custo } + itensAdicionados.sumOf { it.custo }
        return Result(
            saldo = saldo,
            orcamento = orcamento,
            semLimite = semLimite,
            dentroDoOrcamento = semLimite || saldo == orcamento
        )
    }

    companion object {
        /** Valor de livro que toda raça oficial fecha — ver o comentário da classe. */
        const val DEFAULT_ORCAMENTO = 2

        /**
         * Todos os itens removíveis da raça base: só `habilidades[]` — toda raça
         * (oficial ou custom) representa Vantagem/Complicação de graça como
         * traço vinculado por id ali (traitId=GRANTED_EDGE/RACIAL_HINDRANCE +
         * targetRef), nunca mais como string solta em vantagensGratis/desvantagens.
         * `allVantagens` (catálogo geral, opcional) permite cobrar o custo REAL de um
         * GRANTED_EDGE pelo Estágio da Vantagem concedida em vez do fallback fixo de 2 —
         * ver RacialTraitPointCatalog.custoDe/custoDeVantagem.
         */
        fun itensRemoviveisDe(base: RacialModifier, allVantagens: List<Vantagem> = emptyList()): List<VariantBudgetItem> =
            base.habilidades.map { habilidadeComoItem(it, allVantagens) }

        /** Valor de livro total da raça base: soma do custo de TODOS os itens removíveis dela. */
        fun valorTotalDe(base: RacialModifier, allVantagens: List<Vantagem> = emptyList()): Int =
            itensRemoviveisDe(base, allVantagens).sumOf { it.custo }

        // Rótulo mecânico (ex.: "Atributo aumentado d6: Vigor", "Voo (Movimentação
        // 12)") quando o id do traço resolve pra um efeito/rótulo conhecido em
        // RacialTraitPointCatalog — o mesmo catálogo que a aba "Características"
        // já usa (ver RacialCaracteristicasResolver). Sem id reconhecido, cai no
        // nome cru do livro, que é como esses ~200 traços já vinham antes deste
        // catálogo existir.
        // internal (não private): reaproveitado por ValidateAncestryOptionBudgetsUseCase
        // pra achar o custo de itens removidos por uma opção de Seleção/Variante,
        // mesma lógica de custo que o editor de Variante custom já usa.
        internal fun habilidadeComoItem(habilidade: RacialAbility, allVantagens: List<Vantagem> = emptyList()): VariantBudgetItem {
            val id = habilidade.id?.let { it.ifBlank { null } }
            // Efeito/custo pelo id RESOLVIDO (traitId ?: id — ver resolvedTraitId()),
            // não pelo id cru: mesmo padrão já usado por RacialAbility.resolvedPontos()/
            // RacialTraitAuditFormatter/AncestralidadeCatalogBudgetTest. Sem isso, um
            // traço migrado pro par genérico ATTRIBUTE_BOOST/SKILL_BOOST (traitId
            // parametrizado + targetRef/value, id mantido só pra identidade/auditoria)
            // resolvia pra `Nenhum` aqui assim que o id bruto saísse do catálogo —
            // `id` continua sendo o que identifica/remove o traço (habilidadeId
            // abaixo), só o CÁLCULO passa a olhar o par genérico quando presente.
            val resolvedId = habilidade.resolvedTraitId().ifBlank { null }
            val efeito = RacialTraitPointCatalog.efeitoDe(resolvedId, habilidade.targetRef, habilidade.value)
            val vezes = habilidade.vezes.coerceAtLeast(1)
            // Traços EMPILHÁVEIS (ver RacialTraitPointCatalog.VEZES_MAX) mostram
            // e cobram o valor final (ex.: "Resistência +2", 2 pontos), não o
            // de 1 compra só — removê-los da raça base tem que devolver o
            // custo de TODAS as compras que a raça já tinha, senão o saldo da
            // Variante fecha errado.
            val label = if (vezes > 1) {
                RacialTraitPointCatalog.labelComVezes(id, vezes)
            } else {
                RacialTraitPointCatalog.LABEL[id?.keyify()]
                    ?: when (efeito) {
                        is RacialTraitEffect.AtributoStep -> {
                            val dado = (4 + 2 * efeito.passos).toDiceString()
                            "Atributo aumentado $dado: ${efeito.atributo}"
                        }
                        is RacialTraitEffect.PericiaStep -> {
                            val dado = (4 + (efeito.passos - 1) * 2).toDiceString()
                            "Perícia inicial $dado: ${efeito.pericia}"
                        }
                        else -> habilidade.nome
                    }
            }
            // `severity` precisa ir junto pros 5 ids "Menor ou Maior" (Forasteiro,
            // Pacifista, Sem Escrúpulos, Sensível, Voto — ver custoDe) cobrarem
            // certo: sem isso, remover um Forasteiro (Menor) da raça base sempre
            // devolvia o valor de Maior (-2), fechando o saldo da Variante errado.
            // `pontos` (RacialAbility.pontos) é o mesmo escape-hatch que
            // resolvedPontos() já prioriza pra traço com valor calibrado à mão,
            // fora da escala genérica do id (ex.: Draconianos Mordida/Garras
            // com PA — ver ancestralidades.json) — sem passar aqui, o editor de
            // Variante ignorava o valor calibrado e usava o genérico do id.
            return VariantBudgetItem(
                label = label,
                custo = RacialTraitPointCatalog.custoDe(
                    resolvedId,
                    value = habilidade.value,
                    severity = habilidade.severity,
                    pontos = habilidade.pontos,
                    targetRef = habilidade.targetRef,
                    nome = habilidade.nome,
                    allVantagens = allVantagens
                ) * vezes,
                habilidadeId = id
            )
        }

        /**
         * Custo de adicionar uma Vantagem do catálogo geral como traço racial da Variante —
         * delega pra RacialTraitPointCatalog.custoDeVantagem, a mesma fórmula que
         * custoDe("GRANTED_EDGE", ...) agora usa pra cobrar o custo real (por Estágio) de um
         * GRANTED_EDGE oficial, em vez de duas cópias divergentes da mesma escala.
         */
        fun custoDeAdicionarVantagem(vantagem: Vantagem): Int = RacialTraitPointCatalog.custoDeVantagem(vantagem)

        fun vantagemComoItemAdicionado(vantagem: Vantagem): VariantBudgetItem = VariantBudgetItem(
            label = vantagem.nome,
            custo = custoDeAdicionarVantagem(vantagem),
            vantagemId = vantagem.id
        )

        /**
         * Custo de adicionar uma Complicação do catálogo geral. Quando a
         * Complicação permite "Menor ou Maior", `comoMaior` decide qual
         * severidade o criador da Variante está escolhendo pra essa raça —
         * sem isso não dá pra saber se vale -1 ou -2.
         */
        fun custoDeAdicionarComplicacao(complicacao: Complicacao, comoMaior: Boolean): Int {
            val severidade = complicacao.severity.trim().lowercase()
            return when {
                severidade == "maior" -> -2
                severidade == "menor" -> -1
                severidade.contains("menor") && severidade.contains("maior") -> if (comoMaior) -2 else -1
                else -> -1
            }
        }

        fun complicacaoComoItemAdicionado(complicacao: Complicacao, comoMaior: Boolean): VariantBudgetItem = VariantBudgetItem(
            label = complicacao.name,
            custo = custoDeAdicionarComplicacao(complicacao, comoMaior),
            complicacaoId = complicacao.id
        )
    }
}
