package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.Complicacao
import com.example.swadebuilder.model.RacialAbility
import com.example.swadebuilder.model.RacialGrantResolver
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.Vantagem
import kotlin.math.abs

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
 * Calcula o saldo em pontos de uma Variante custom de raça: o que foi
 * REMOVIDO da raça base devolve o custo dele ao orçamento (tirar um traço
 * forte libera pontos; tirar uma desvantagem custa pontos, "comprando de
 * volta" a Complicação); o que foi ADICIONADO gasta o custo dele.
 *
 * Por padrão o saldo final precisa ficar dentro de ±2 pontos (mesmo teto da
 * Seleção de traços negativos do Anão Ciber — lá também é "até 2 pontos",
 * não "exatamente 2": ver AnaoCiberTraitCatalog.MAX_PONTOS e seu uso em
 * ResolveAncestrySpecificAdjustmentsUseCase, que aceita `pontosUsados <=
 * MAX_PONTOS`). Saldo 0 (nada adicionado além do que foi removido, ou
 * nenhuma mudança) é válido; só passar de 2 pra qualquer lado é inválido.
 * Passando `semLimite = true` (a opção de "sem limite" pra raças mais
 * fortes, tipo Pathfinder) a validação sempre passa, qualquer saldo,
 * positivo ou negativo.
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
        itensRemovidos: List<VariantBudgetItem>,
        itensAdicionados: List<VariantBudgetItem>,
        orcamento: Int = DEFAULT_ORCAMENTO,
        semLimite: Boolean = false
    ): Result {
        val saldo = itensAdicionados.sumOf { it.custo } - itensRemovidos.sumOf { it.custo }
        return Result(
            saldo = saldo,
            orcamento = orcamento,
            semLimite = semLimite,
            dentroDoOrcamento = semLimite || abs(saldo) <= orcamento
        )
    }

    companion object {
        /** Mesmo teto usado na Seleção de traços negativos do Anão Ciber. */
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

        /** Adicionar um traço bespoke do catálogo geral (fora de Vantagem/Complicação), pelo id. */
        fun tracoComoItemAdicionado(id: String, label: String): VariantBudgetItem =
            VariantBudgetItem(label = label, custo = RacialTraitPointCatalog.custoDe(id), habilidadeId = id)
    }
}
