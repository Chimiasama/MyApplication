// CriadorViewModel.kt
package com.example.swadebuilder.model

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.swadebuilder.CriadorState
import com.example.swadebuilder.Pericia
import com.example.swadebuilder.arcanoInfo
import com.example.swadebuilder.listaComplicacoes
import com.example.swadebuilder.listaPericias
import com.example.swadebuilder.listaVantagens
import com.example.swadebuilder.util.keyify
import java.util.UUID

/**
 * ViewModel que gerencia o estado de criação de personagem.
 */
class CriadorViewModel : ViewModel() {
    val state = CriadorState()

    /**
     * Reinicia o estado para criação de um novo personagem.
     */
    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean = true,
        maisPontosPericias: Boolean = true,
        modoSupers: Boolean = false
    ) {
        // 1) Define se estamos no modo “Supers”
        state.modoSupers = modoSupers

        // 2) Identificador e nome
        state.idAtual = null
        state.nomePersonagem = ""

        // 3) Flags iniciais
        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias

        // 4) Volta ancestralidade para “HUMANOS” e limpa listas
        state.ancestralidade = "HUMANOS"
        state.vantagensSelecionadas.clear()
        state.complicacoesSelecionadas.clear()
        state.vantagensAutomaticas.clear()
        state.desvantagensAutomaticas.clear()
        state.aplicarAncestralidade("HUMANOS")

        // 5) Equipamentos
        state.equipamentosComprados.clear()

        // 6) Pilhas de compra / recursos
        state.cpRecursosStack.clear()
        state.cpPaStack.clear()
        state.cpPvStack.clear()
        state.cpSpStack.clear()
        state.comprasPpPorEstagio.keys.forEach    { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach  { state.comprasAttrPorEstagio[it] = 0 }
        state.paCostStackPorAtributo.values.forEach    { it.clear() }
        state.compCostStackPorPericia.values.forEach   { it.clear() }
        state.spCostStackPorPericia.values.forEach     { it.clear() }
        state.poderSlotsPorArcano.clear()

        // 7) Recursos gerais
        state.dinheiro = 500
        state.progresso = 0
        state.progressosDisponiveis = 0
        state.frozenAdvCount = 0
        state.emProgresso = false

        // 8) Atributos de base (todos em 4) e recalcula pontos
        state.valoresAtributos.forEach { (_, holder) -> holder.intValue = 4 }
        state.recalcularPontosAtributo()

        // 9) Perícias: zera incrementos e faz rebuild
        listaPericias.forEach { per ->
            state.baseIncsPorPericia[per] = 0
            state.spCostStackPorPericia.getValue(per).clear()
            state.compCostStackPorPericia[per]?.clear()
        }
        state.rebuildAllPericiaStacks()

        // 10) Pontos de vantagem / atributo / complicação
        state.pontosVantagem =
            if (state.vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0
        state.pontosAtributo = 5
        state.pontosComplicacaoGastos = 0

        // 11) Se estivermos no modo Supers, aplica as vantagens especiais
        if (modoSupers) {
            // 11.1) Remove qualquer antecedente arcano previamente selecionado
            state.vantagensSelecionadas.removeAll {
                it.nome.keyify().startsWith("antecedente arcano")
            }

            // 11.2 Adiciona a vantagem “Superpoderes” (automática, não removível)
            val superVant: Vantagem = listaVantagens
                .firstOrNull { it.nome.equals("Superpoderes", ignoreCase = true) }
                ?: Vantagem(
                    id               = UUID.randomUUID().toString(),
                    nome             = "Superpoderes",
                    categoria        = Categoria.PODER,
                    origem           = "SUPERS",
                    nivel            = "N",  // nível “Novato” por padrão
                    requisitos       = Requisito(
                        estagio            = "N",
                        atributoMin        = emptyMap(),
                        periciaMin         = emptyMap(),
                        periciaMinOpcional = emptyMap(),
                        vantagensPrevias   = emptyList(),
                        observacoes        = ""
                    ),
                    limiteCompra     = "uma_vez",
                    vinculadoPericia = false,
                    ganhaAoComprar   = emptyList(),
                    descricao        = "Modo Supers: libera Superpoderes."
                    // os campos @Transient (“choice”, etc.) usarão os defaults da data class
                )
            state.vantagensSelecionadas.add(superVant)
            state.vantagensAutomaticas.add(superVant.nome)

            // 11.3 Cria slots vazios para todos os arcanos (mesmo que não usemos arcano normalmente)
            state.poderSlotsPorArcano.clear()
            arcanoInfo.forEach { (arcKey, triple) ->
                val slots = triple.first
                state.poderSlotsPorArcano[arcKey] = mutableStateListOf<String?>().apply {
                    repeat(slots) { add(null) }
                }
            }
        }
    }

    fun loadFromSalvo(
        salvo: PersonagemSalvo,
        categoriasEquip: List<EquipamentoCategoria>
    ) {
        resetStateParaNovoPersonagem()
        state.idAtual = salvo.id
        state.nomePersonagem = salvo.nome

        // 1) Atributos e perícias
        state.valoresAtributos.forEach { (key, holder) ->
            holder.intValue = salvo.atributos[key] ?: 4
        }
        val desiredPericias: Map<Pericia, Int> = listaPericias.associateWith { per ->
            salvo.pericias[per.nome] ?: state.rawTotal(per)
        }
        state.rebuildPericias(desiredPericias)

        // 2) Ancestralidade, vantagens e complicações
        state.maisPontosPericias = salvo.maisPontosPericias
        state.cartaSelvagem      = salvo.cartaSelvagem
        state.heroisSemArmadura  = salvo.heroisSemArmadura
        state.ancestralidade     = salvo.ancestralidade
        state.aplicarAncestralidade(salvo.ancestralidade)

        state.vantagensSelecionadas.clear()
        state.vantagensSelecionadas.addAll(
            listaVantagens.filter { it.nome in salvo.vantagens }
        )

        state.complicacoesSelecionadas.clear()
        salvo.complicacoes.forEach { nomeComp ->
            listaComplicacoes
                .find { it.id == nomeComp }
                ?.let { comp ->
                    // Por default, restaura como “Menor”
                    state.complicacoesSelecionadas[comp] = "Menor"
                }
        }

        // 3) Equipamentos
        state.equipamentosComprados.clear()
        salvo.equipamentos.forEach { nomeEq ->
            categoriasEquip
                .flatMap { it.itens }
                .firstOrNull { it.nome == nomeEq }
                ?.let { eq ->
                    state.equipamentosComprados.add(eq)
                }
        }

        // 4) Poderes (antecedentes arcanos)
        state.poderSlotsPorArcano.clear()
        salvo.poderes.forEach { (arcano, poderesLista) ->
            val capacidade = arcanoInfo[arcano]?.first ?: 0
            state.poderSlotsPorArcano[arcano] = mutableStateListOf<String?>().apply {
                repeat(capacidade) { idx ->
                    add(poderesLista.getOrNull(idx))
                }
            }
        }

        // 5) Pontos restantes e dinheiro
        state.pontosVantagem = salvo.pontosRestantes
        state.dinheiro = salvo.dinheiro
    }
}
