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

    // === NOVO: toggle global (por enquanto via MainActivity) ===
    var multiplosAAHabilitados: Boolean = false
        private set

    fun setMultiplosAAHabilitados(enabled: Boolean) {
        multiplosAAHabilitados = enabled
    }

    // === NOVO: utilitários para reconhecer IDs de AA ===
    private fun isIdArcano(id: String): Boolean =
        id == "antecedente_arcano" || id.startsWith("antecedente_arcano_")

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

    // === NOVO: consultas para requisitos ===
    fun hasAnyArcano(): Boolean =
        state.vantagensSelecionadas.any { isIdArcano(it.id) && it.id != "antecedente_arcano" || (it.id == "antecedente_arcano" && it.choice != null) }

    fun hasArcano(subtipo: String): Boolean {
        val alvo = when (subtipo.trim().uppercase()) {
            "DOM"                -> "antecedente_arcano_dom"
            "MAGIA"              -> "antecedente_arcano_magia"
            "MILAGRES"           -> "antecedente_arcano_milagres"
            "PSIÔNICOS", "PSIONICOS" -> "antecedente_arcano_psionicos"
            "CIÊNCIA ESTRANHA", "CIENCIA ESTRANHA" -> "antecedente_arcano_ciencia_estranha"
            else -> null
        } ?: return false
        return state.vantagensSelecionadas.any { it.id == alvo }
    }

    fun countArcanos(): Int =
        state.vantagensSelecionadas.count { it.id.startsWith("antecedente_arcano_") }

    /**
     * Reinicia o estado para criação de um novo personagem.
     */
    fun resetStateParaNovoPersonagem(
        cartaSelvagem: Boolean,
        maisPontosPericias: Boolean,
        modoSupers: Boolean,
        usarEspecializacoesDePericia: Boolean = false
    ) {
        // 1) Define se estamos no modo “Supers”
        state.modoSupers = modoSupers
        // Flags correlatas para telas/filtros
        state.modoSuperequip = modoSupers
        state.modoSuperComplicacoes = modoSupers

        // 2) Identificador e nome
        state.idAtual = null
        state.nomePersonagem = ""

        // 3) Flags iniciais
        state.cartaSelvagem = cartaSelvagem
        state.maisPontosPericias = maisPontosPericias
        state.usarEspecializacoesDePericia = usarEspecializacoesDePericia
        state.especializacoesPorPericia.clear()

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
        state.novosPoderesStacksPorArcano.clear()

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
            // 11.1) Remove qualquer antecedente arcano previamente selecionado (base + variantes)
            state.vantagensSelecionadas.removeAll { it.id == "antecedente_arcano" || it.id.startsWith("antecedente_arcano_") }

            // 11.2) Adiciona a vantagem “Superpoderes” (automática, não removível)
            val superVant: Vantagem = listaVantagens
                .firstOrNull { it.nome.equals("Superpoderes", ignoreCase = true) }
                ?: Vantagem(
                    id               = UUID.randomUUID().toString(),
                    nome             = "Superpoderes",
                    categoria        = Categoria.PODER,
                    origem           = "SUPER",
                    nivel            = "N",  // “Novato”
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
                )
            state.vantagensSelecionadas.add(superVant)
            state.vantagensAutomaticas.add(superVant.nome)

            // 11.3) Cria slots vazios para todos os arcanos (compatibilidade visual)
            state.poderSlotsPorArcano.clear()
            arcanoInfo.forEach { (arcKey, triple) ->
                val slots = triple.first
                state.poderSlotsPorArcano[arcKey] = mutableStateListOf<String?>().apply {
                    repeat(slots) { add(null) }
                }
            }
        }
    }

    /**
     * Reidrata um personagem salvo, incluindo modos Supers e superequipamentos.
     * - categoriasBasico: categorias de equipamento com origem BASICO
     * - categoriasSuper:  categorias de equipamento com origem SUPER
     */
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
