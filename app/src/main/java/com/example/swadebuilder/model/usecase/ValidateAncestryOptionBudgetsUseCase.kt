package com.example.swadebuilder.model.usecase

import com.example.swadebuilder.model.AncestryVariantConfig
import com.example.swadebuilder.model.RacialModifier
import com.example.swadebuilder.model.RacialTraitPointCatalog
import com.example.swadebuilder.model.ResolvedTraitPackage
import com.example.swadebuilder.model.SelectionType
import com.example.swadebuilder.model.TraitAddition
import com.example.swadebuilder.model.Vantagem
import com.example.swadebuilder.util.keyify

/**
 * Confere o orçamento de pontos de CADA OPÇÃO de uma raça (VariantOption de
 * `grupoVariante`, ou FixedPackageOption de uma SelectionDef FIXED_PACKAGE)
 * isoladamente — não a soma de `habilidades[]` como lista plana e
 * permanente, que é o que `ResolveVariantPointBudgetUseCase` já faz pro
 * editor de Variante custom.
 *
 * O motivo de existir separado: uma raça com Seleção (ex.: Humano Arte da
 * Guerra, 14 Signos) só deveria ter UMA opção ativa em `habilidades[]` por
 * vez — cada opção, sozinha, precisa fechar contra `pontosRaciaisEsperados`,
 * do mesmo jeito que o livro calibra cada Signo pra valer o mesmo tanto que
 * qualquer outro. Se uma opção específica não fechar, o problema é só
 * daquela opção — não faz a raça inteira parecer desbalanceada, e não devia
 * ser escondido numa soma agregada de tudo junto.
 *
 * Reaproveita `ResolveVariantPointBudgetUseCase.resolve()` pra cada opção,
 * alimentado pelos itens que aquela opção adiciona/remove em vez das
 * escolhas manuais de um mestre — mesma conta, fonte diferente.
 *
 * Escopo: só `grupoVariante` e `selecoes` do tipo FIXED_PACKAGE, que são os
 * únicos formatos hoje com um pacote fixo e enumerável por opção.
 * BUDGETED_CATALOG (ex.: Anão Ciber) não tem "opções" fixas pra validar —
 * são combinações livres dentro de um orçamento próprio, já validadas na
 * hora da escolha. TARGET_ATTRIBUTE_OR_SKILL não é usado por nenhuma raça
 * cadastrada hoje (ver comentário em SelectionDef).
 */
class ValidateAncestryOptionBudgetsUseCase {

    data class OptionBudgetResult(
        val optionId: String,
        val optionLabel: String,
        val saldo: Int,
        val orcamento: Int,
        val dentroDoOrcamento: Boolean
    )

    fun execute(base: RacialModifier, config: AncestryVariantConfig, allVantagens: List<Vantagem> = emptyList()): List<OptionBudgetResult> {
        val valorBaseRaca = ResolveVariantPointBudgetUseCase.valorTotalDe(base, allVantagens)
        val orcamento = base.pontosRaciaisEsperados
        val results = mutableListOf<OptionBudgetResult>()

        config.grupoVariante?.opcoes?.forEach { opcao ->
            results += validarPacote(opcao.id, opcao.nome, opcao.pacoteFixo, base, valorBaseRaca, orcamento, allVantagens)
        }

        config.selecoes
            .filter { it.tipo == SelectionType.FIXED_PACKAGE }
            .forEach { selecao ->
                selecao.pacotesFixos.orEmpty().forEach { pacoteOpcao ->
                    results += validarPacote(
                        pacoteOpcao.id,
                        pacoteOpcao.nome,
                        pacoteOpcao.pacote,
                        base,
                        valorBaseRaca,
                        orcamento,
                        allVantagens
                    )
                }
            }

        return results
    }

    private fun validarPacote(
        optionId: String,
        optionLabel: String,
        pacote: ResolvedTraitPackage,
        base: RacialModifier,
        valorBaseRaca: Int,
        orcamento: Int,
        allVantagens: List<Vantagem>
    ): OptionBudgetResult {
        val itensRemovidos = itensRemovidosDoPacote(pacote, base, allVantagens)
        val itensAdicionados = itensAdicionadosDoPacote(pacote)
        val resultado = ResolveVariantPointBudgetUseCase().resolve(
            valorBaseRaca = valorBaseRaca,
            itensRemovidos = itensRemovidos,
            itensAdicionados = itensAdicionados,
            orcamento = orcamento
        )
        return OptionBudgetResult(
            optionId = optionId,
            optionLabel = optionLabel,
            saldo = resultado.saldo,
            orcamento = resultado.orcamento,
            dentroDoOrcamento = resultado.dentroDoOrcamento
        )
    }

    // Itens removidos: casados contra habilidades[] da raça base por nome/id
    // (mesmo casamento que CriadorState.applyAncestryVariantAdjustments já
    // usa pra aplicar a opção de verdade) — o custo de remover é o custo que
    // aquele traço JÁ tem na raça base, não um valor novo.
    private fun itensRemovidosDoPacote(pacote: ResolvedTraitPackage, base: RacialModifier, allVantagens: List<Vantagem>): List<VariantBudgetItem> {
        val porNome = pacote.tracosParaRemoverPorNome.map { it.keyify() }.toSet()
        val porId = pacote.tracosParaRemoverPorId.map { it.keyify() }.toSet()
        if (porNome.isEmpty() && porId.isEmpty()) return emptyList()
        return base.habilidades
            .filter { hab ->
                (hab.id != null && hab.id.keyify() in porId) || hab.nome.keyify() in porNome
            }
            .map { ResolveVariantPointBudgetUseCase.habilidadeComoItem(it, allVantagens) }
    }

    // Itens adicionados: toda lista de TraitAddition do pacote (positivos,
    // negativos, vantagens e desvantagens grátis) — cada um já carrega id +
    // pontos explícitos (ver TraitAddition), sem precisar casar por nome.
    private fun itensAdicionadosDoPacote(pacote: ResolvedTraitPackage): List<VariantBudgetItem> {
        val todos = pacote.tracosParaAdicionar +
            pacote.tracosNegativosParaAdicionar +
            pacote.vantagensGratisParaAdicionar +
            pacote.desvantagensParaAdicionar
        return todos.map { traitAdditionComoItem(it) }
    }

    private fun traitAdditionComoItem(traco: TraitAddition): VariantBudgetItem {
        val vezes = traco.vezes.coerceAtLeast(1)
        val custoUnitario = RacialTraitPointCatalog.custoDe(traco.id, pontos = traco.pontos)
        return VariantBudgetItem(
            label = RacialTraitPointCatalog.LABEL[traco.id.keyify()] ?: traco.nome,
            custo = custoUnitario * vezes,
            habilidadeId = traco.id
        )
    }
}
