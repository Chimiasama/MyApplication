package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialGrantResolver
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.Vantagem

/**
 * Item já resolvido (traço racial, Vantagem ou Complicação, oficial ou
 * removido da raça base) com seu custo em pontos, pronto pra entrar no
 * cálculo de orçamento de uma Variante custom. Ver `RacialTraitPointCatalog`
 * e `RacialGrantResolver` pra como cada custo é decidido.
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
 * traços/vantagensGratis/desvantagens, tocados ou não — ver [valorTotalDe]),
 * subtrai o custo de cada item removido e soma o custo de cada item
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

        /** Todos os itens removíveis da raça base: habilidades[], vantagensGratis e desvantagens. */
        fun itensRemoviveisDe(base: RacialModifier): List<VariantBudgetItem> {
            val doHabilidades = base.habilidades.map { habilidadeComoItem(it) }
            val doVantagensGratis = base.vantagensGratis.map { texto ->
                val link = RacialGrantResolver.resolveVantagemGratis(texto)
                VariantBudgetItem(label = texto, custo = link.custo, vantagemId = link.vantagemId)
            }
            val doDesvantagens = base.desvantagens.map { texto ->
                val link = RacialGrantResolver.resolveDesvantagem(texto)
                VariantBudgetItem(label = texto, custo = link.custo, complicacaoId = link.complicacaoId)
            }
            return doHabilidades + doVantagensGratis + doDesvantagens
        }

        /** Valor de livro total da raça base: soma do custo de TODOS os itens removíveis dela. */
        fun valorTotalDe(base: RacialModifier): Int = itensRemoviveisDe(base).sumOf { it.custo }

        private fun habilidadeComoItem(habilidade: RacialAbility): VariantBudgetItem =
            VariantBudgetItem(label = habilidade.nome, custo = RacialTraitPointCatalog.custoDe(habilidade.id), habilidadeId = habilidade.id)

        /**
         * Custo de adicionar uma Vantagem do catálogo geral como traço racial
         * da Variante: não existe uma escala oficial de "pontos de Vantagem"
         * no livro, então usa o Estágio (Novato/Experiente/Veterano/Heroico)
         * como proxy de força — mesma ideia de "uma Vantagem grátis custa 2"
         * já usada em RacialTraitPointCatalog/RacialGrantResolver, um degrau
         * a mais por Estágio acima de Novato.
         */
        fun custoDeAdicionarVantagem(vantagem: Vantagem): Int = when (vantagem.requisitos.estagio.trim().lowercase()) {
            "experiente" -> 3
            "veterano" -> 4
            "heroico", "lendário", "lendario" -> 5
            else -> 2 // Novato ou sem estágio definido
        }

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
