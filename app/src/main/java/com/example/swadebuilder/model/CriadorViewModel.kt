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

/**
 * ViewModel que gerencia o estado de criação de personagem.
 */
class CriadorViewModel : ViewModel() {
    val state = CriadorState()

    // === NOVO: toggle global (por enquanto via MainActivity) ===
    var multiplosAAHabilitados: Boolean = false
        private set

    fun setMultiplosAAHabilitados(enabled: Boolean) {
        multiplosAAHabilitados = enabled
    }

    private fun mapChoiceToArcanoId(choice: String?): String? {
        return when (choice?.trim()?.uppercase()) {
            "DOM"                -> "antecedente_arcano_dom"
            "MAGIA"              -> "antecedente_arcano_magia"
            "MILAGRES"           -> "antecedente_arcano_milagres"
            "PSIÔNICOS", "PSIONICOS" -> "antecedente_arcano_psionicos"
            "CIÊNCIA ESTRANHA", "CIENCIA ESTRANHA" -> "antecedente_arcano_ciencia_estranha"
            else -> null
        }
    }

    // === NOVO: compatibilidade ao carregar saves antigos ===
    fun normalizeArcanoIdsNoCarregamento() {
        // 1) converte AA base + choice em AA específico
        val convertidos = state.vantagensSelecionadas.map { v ->
            if (v.id == "antecedente_arcano" && v.choice != null) {
                val novoId = mapChoiceToArcanoId(v.choice)
                val novo = listaVantagens.find { it.id == novoId }
                novo ?: v
            } else v
        }
        state.vantagensSelecionadas.clear()
        state.vantagensSelecionadas.addAll(convertidos.distinctBy { it.id })
    }

    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        usarEspecializacoesDePericia: Boolean = false,
        grandesResponsabilidades: Boolean = false, // ← NOVO
    ) {

        state.modoSupers = modoSupers
        state.modoSuperequip = modoSupers
        state.modoSuperComplicacoes = modoSupers
        state.grandesResponsabilidades = grandesResponsabilidades
        state.modoSuperComplicacoes = modoSupers

        state.idAtual = null
        state.nomePersonagem = ""

        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()

        state.ancestralidade = "HUMANOS"
        state.vantagensSelecionadas.clear()
        state.complicacoesSelecionadas.clear()
        state.vantagensAutomaticas.clear()
        state.desvantagensAutomaticas.clear()
        state.aplicarAncestralidade("HUMANOS")

        state.equipamentosComprados.clear()

        state.cpRecursosStack.clear()
        state.cpPaStack.clear()
        state.cpPvStack.clear()
        state.cpSpStack.clear()
        state.comprasPpPorEstagio.keys.forEach   { state.comprasPpPorEstagio[it] = 0 }
        state.comprasAttrPorEstagio.keys.forEach { state.comprasAttrPorEstagio[it] = 0 }
        state.paCostStackPorAtributo.values.forEach  { it.clear() }
        state.compCostStackPorPericia.values.forEach { it.clear() }
        state.spCostStackPorPericia.values.forEach   { it.clear() }
        state.poderSlotsPorArcano.clear()
        state.novosPoderesStacksPorArcano.clear()

        state.dinheiro = 500
        state.progresso = 0
        state.progressosDisponiveis = 0
        state.frozenAdvCount = 0
        state.emProgresso = false

        state.valoresAtributos.forEach { (_, holder) -> holder.intValue = 4 }
        state.recalcularPontosAtributo()

        listaPericias.forEach { per ->
            state.baseIncsPorPericia[per] = 0
            state.spCostStackPorPericia.getValue(per).clear()
            state.compCostStackPorPericia[per]?.clear()
        }
        state.rebuildAllPericiaStacks()

        state.pontosVantagem =
            if (state.vantagensAutomaticas.any { it.keyify() == "ADAPTAVEL" }) 1 else 0
    }

    fun loadFromSalvo(
        salvo: PersonagemSalvo,
        categoriasBasico: List<EquipamentoCategoria>,
        categoriasSuper:  List<EquipamentoCategoria>
    ) {
        // Reinicia o estado com as flags corretas vindas do save
        resetStateParaNovoPersonagem(
            cartaSelvagem = salvo.cartaSelvagem,
            maisPontosPericias = salvo.maisPontosPericias,
            modoSupers = salvo.modoSupers,
            usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia

        )
        // Demais flags de modo (mantêm comportamento de telas/filtros)
        state.modoSuperequip = salvo.modoSuperequip
        state.modoSuperComplicacoes = salvo.modoSuperComplicacoes

        // Identidade e nome
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

        // 2) Ancestralidade e flags gerais
        state.maisPontosPericias = salvo.maisPontosPericias
        state.cartaSelvagem      = salvo.cartaSelvagem
        state.heroisSemArmadura  = salvo.heroisSemArmadura
        state.ancestralidade     = salvo.ancestralidade
        state.aplicarAncestralidade(salvo.ancestralidade)

        // 3) Vantagens — prioriza ID; fallback por nome (case-insensitive)
        state.vantagensSelecionadas.clear()
        val mapPorId   = listaVantagens.associateBy { it.id }
        val mapPorNome = listaVantagens.associateBy { it.nome.trim().uppercase() }

        salvo.vantagens.forEach { saved ->
            val trimmed = saved.trim()
            val byId = mapPorId[trimmed]
            if (byId != null) {
                state.vantagensSelecionadas.add(byId)
            } else {
                val byName = mapPorNome[trimmed.uppercase()]
                if (byName != null) {
                    state.vantagensSelecionadas.add(byName)
                }
            }
        }

        // 4) Complicações — já são IDs no formato atual
        state.complicacoesSelecionadas.clear()
        salvo.complicacoes.forEach { compId ->
            listaComplicacoes.find { it.id == compId }?.let { comp ->
                // Por default, restaura como “Menor”
                state.complicacoesSelecionadas[comp] = "Menor"
            }
        }

        // 5) Equipamentos — busca em BÁSICO + SUPER (corrige sumiço pós-load)
        state.equipamentosComprados.clear()
        val todasCategorias = (categoriasBasico + categoriasSuper)
        val mapaItensPorNome = todasCategorias
            .flatMap { it.itens }
            .associateBy { it.nome.trim().uppercase() }

        salvo.equipamentos.forEach { nomeSalvo ->
            mapaItensPorNome[nomeSalvo.trim().uppercase()]?.let { item ->
                state.equipamentosComprados.add(item)
            }
        }

        // 6) Poderes arcanos (slots por AA)
        state.poderSlotsPorArcano.clear()
        salvo.poderes.forEach { (arcano, poderesLista) ->
            val capacidade = arcanoInfo[arcano]?.first ?: 0
            state.poderSlotsPorArcano[arcano] = mutableStateListOf<String?>().apply {
                repeat(capacidade) { idx -> add(poderesLista.getOrNull(idx)) }
            }
        }

        // 7) (Removido) superPoderesComprados — seu state espera PurchasedPower, não Strings.

        // 8) Dinheiro, pontos, especializações
        state.pontosVantagem = salvo.pontosRestantes
        state.dinheiro = salvo.dinheiro

        state.usarEspecializacoesDePericia = salvo.usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()
        state.especializacoesPorPericia.putAll(salvo.especializacoesPorPericia)

        // 9) Recalcular derivados conforme seu fluxo atual
        state.recalcularPontosAtributo()
        state.rebuildAllPericiaStacks()
        normalizeArcanoIdsNoCarregamento()
    }
}
